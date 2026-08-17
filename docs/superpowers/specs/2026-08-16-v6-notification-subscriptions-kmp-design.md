# V6 Notification Subscriptions — KMP Client

**Status:** implemented on feat/v6-notification-subscriptions — device-matrix verification pending; V5 path removal is a follow-up gated on that matrix
**Supersedes:** the uncommitted draft `2026-08-13-v6-topic-targeted-notifications-kmp-design.md`
**Server side:** shipped on `feat/v6-topic-targeted-notifications` (PR #327) — **no server change required**
**Contract:** `contracts/notification-topics.v6.json`

## Problem

The server already dispatches V6: every launch notification fans out to one FCM condition per
audience class, naming the launch's own attributes. No shipped client subscribes to any `v6_*`
topic, so **every V6 send currently reaches zero devices** and all users are served by the V5
broadcast and its client-side filtering.

This spec covers the client work that makes V6 real. Its subject is narrow on purpose: deriving the
topic set, and knowing with confidence what the device is actually subscribed to. That is the whole
risk surface, because under V6 the subscription *is* the filter.

## The failure mode this introduces

Under V5 a failed topic subscription was nearly harmless — the app subscribed to one broadcast topic
and filtered locally. Under V6 **a failed subscribe silently removes notifications the user asked
for**, with no local evidence and no server-side visibility: topic sends report no per-device
outcome, so the server logs a success either way.

| | Cost | Visibility |
|---|---|---|
| **Over**-subscribed, same class | Extra notifications | User sees them; fixed by toggling anything |
| **Over**-subscribed, two classes | **Every notification twice** | User sees it; persists until next reconcile |
| **Under**-subscribed | Missing notifications | Silent. Invisible to user *and* server |

Every decision below follows from this table.

## Topic shape (as shipped — unchanged)

The audience class is part of each type topic: `v6_<env>_<platform>_<class>_<type>`. A device
subscribes to the type topics of exactly one class, which is what makes duplicate delivery
impossible by construction rather than by deduplication.

The consequence the client must handle: **changing class rewrites every type topic.** Unchecking
webcast-only moves `flex_w` → `flex`, which is 10 unsubscribes and 10 subscribes. Toggling
follow-all additionally drops up to 23 attribute topics. This is why reconciliation needs durable
per-topic state rather than a set written at the end of a loop — see below.

## Design

Two pieces, deliberately separated so all the thinking lives in a pure function and all the I/O
lives in a dumb loop over a table.

### 1. Derivation — a pure function

```kotlin
fun requiredTopics(
    state: NotificationState,
    env: String,        // "prod" | "debug"
    platform: String,   // "ios" | "android"
): Set<String>
```

No I/O, no suspend, no FCM, no database. Given settings, it returns the exact set the device should
hold. This is the entire complexity of V6 on the client and it is testable with plain assertions.

**Audience class** — exactly one per device:

```kotlin
val webcastOnly = state.isTopicEnabled(NotificationTopic.WEBCAST_ONLY)
val audienceClass = when {
    state.followAllLaunches -> if (webcastOnly) "all_w" else "all"
    state.useStrictMatching -> if (webcastOnly) "strict_w" else "strict"
    else                    -> if (webcastOnly) "flex_w" else "flex"
}
```

Follow-all wins over strict, matching the UI (which disables the strict toggle when follow-all is
on).

**The set:**

| Source | Topic |
|---|---|
| Each enabled notification type | `v6_<env>_<platform>_<class>_<type>` |
| Each selected agency group | `v6_<env>_<group>` |
| Each selected location group | `v6_<env>_<group>` |
| Each enabled broadcast toggle | `v6_<env>_<platform>_<token>` |

Rules:

- `enableNotifications == false` → **empty set**. Unsubscribing is the real kill switch now, not a
  local check.
- Follow-all classes emit **no attribute topics** — `..._all_<type>` is a single-topic condition, so
  attribute subscriptions are dead weight that must be dropped, or a later switch out of follow-all
  starts dirty.
- Desktop is a no-op, as today.

Typical device: ≤10 type + ≤23 attribute + ≤3 broadcast ≈ **36 topics**, well inside FCM limits.

### 2. Subscription state — a database table

FCM offers clients **no way to read a token's current subscriptions**; that only ever existed as the
deprecated server-side Instance ID API. The device's own record is therefore the only account of
what it is subscribed to, which makes that record correctness-critical rather than a cache.

It lives in SQLDelight beside the rest of the app's durable state.

**`composeApp/src/commonMain/sqldelight/me/calebjones/spacelaunchnow/database/TopicSubscription.sq`**

```sql
-- One row per FCM topic this device has an opinion about.
-- `confirmed` changes ONLY on an FCM success callback — it records what FCM
-- told us, never what we intended.
CREATE TABLE TopicSubscription (
    topic        TEXT NOT NULL PRIMARY KEY,
    desired      INTEGER AS Boolean NOT NULL,  -- do we want to be subscribed?
    confirmed    INTEGER AS Boolean NOT NULL,  -- has FCM acknowledged that state?
    last_attempt INTEGER,                      -- epoch millis, null until first try
    last_error   TEXT,                         -- last failure message, null on success
    attempts     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_topic_pending ON TopicSubscription(desired, confirmed);
```

Two booleans give four states, and the pending work is exactly the rows where they disagree:

| `desired` | `confirmed` | Meaning | Action |
|---|---|---|---|
| 1 | 1 | Subscribed and acknowledged | none — steady state |
| 1 | 0 | Wanted, not acknowledged | **subscribe** |
| 0 | 1 | No longer wanted, still subscribed | **unsubscribe** |
| 0 | 0 | Settled removal | delete the row |

Queries:

```sql
markDesired:      UPDATE TopicSubscription SET desired = ? WHERE topic = ?;
upsertDesired:    INSERT OR IGNORE INTO TopicSubscription(topic, desired, confirmed) VALUES (?, 1, 0);
clearDesiredExcept: -- set desired = 0 for every topic not in the required set
pendingUnsubscribes: SELECT topic FROM TopicSubscription WHERE desired = 0 AND confirmed = 1;
pendingSubscribes:   SELECT topic FROM TopicSubscription WHERE desired = 1 AND confirmed = 0;
confirm:          UPDATE TopicSubscription SET confirmed = ?, last_error = NULL, last_attempt = ? WHERE topic = ?;
recordFailure:    UPDATE TopicSubscription SET last_error = ?, last_attempt = ?, attempts = attempts + 1 WHERE topic = ?;
deleteSettled:    DELETE FROM TopicSubscription WHERE desired = 0 AND confirmed = 0;
confirmedTopics:  SELECT topic FROM TopicSubscription WHERE confirmed = 1;
clearAll:         DELETE FROM TopicSubscription;
```

Schema is at version 10; this adds `11.sqm` creating the table and bumps `version = 11` in
`composeApp/build.gradle.kts`. New table only — no existing table is touched.

### 3. Reconciliation — a dumb loop, unsubscribe first

```kotlin
suspend fun reconcile() {
    val required = requiredTopics(state, env, platform)

    db.transaction {                          // desired is written atomically
        clearDesiredExcept(required)
        required.forEach { upsertDesired(it) }
    }

    // Unsubscribes FIRST. See below — this ordering is correctness, not taste.
    pendingUnsubscribes().forEach { topic ->
        fcm.unsubscribe(topic)
            .onSuccess { confirm(topic, confirmed = false); }
            .onFailure { recordFailure(topic, it.message) }
    }
    deleteSettled()

    pendingSubscribes().forEach { topic ->
        fcm.subscribe(topic)
            .onSuccess { confirm(topic, confirmed = true) }
            .onFailure { recordFailure(topic, it.message) }
    }
}
```

**One invariant: `confirmed` is written only in an FCM success callback.** A failure leaves the row
disagreeing, so the next reconciliation picks it up automatically. There is no retry queue, no
backoff bookkeeping, and no way for an intended change to be mistaken for an achieved one.

**Reconciliation is triggered by an explicit Save, and once on every app start.** The notification
settings screen becomes save-based rather than reconciling per toggle. Decided over debouncing
because a class switch is ~20 FCM operations under the shipped topic shape: a user exploring the
toggles would fire a full class rewrite on every debounce expiry, while a Save button batches any
number of changes into exactly one reconciliation — and gives a natural place to surface "some
subscriptions failed, will retry" feedback. The app-start reconcile is the retry path for anything
that failed at save time, so a failed save self-heals without the user doing anything.

#### Why unsubscribes go first

Because the class is baked into the type topic, a class switch is a full rewrite of that dimension.
If subscribes ran first and the unsubscribes then failed, the device would hold **both**
`v6_prod_ios_flex_w_tenMinutes` and `v6_prod_ios_flex_tenMinutes`, match two server conditions, and
receive **every launch notification twice** until the next reconciliation — potentially days later,
at next app start.

Unsubscribing first makes a partial failure fail toward a brief gap instead of lasting duplicates.
Per the table at the top, that is the correct direction to fail: a gap is transient and self-heals on
the next pass, whereas duplicates persist and are the exact defect the disjoint-class design exists
to prevent.

#### Why a table rather than a `Set<String>` in preferences

- **Per-topic outcomes.** A set records only membership; the table records which topic failed, why,
  when, and how often — which is what makes diagnostics possible at all.
- **Crash safety mid-reconcile.** A class switch is ~20 operations. A set serialised once at the end
  of the loop loses the entire batch if the process dies partway; per-row commits lose nothing.
- **The pending work is a query,** not a diff computed in memory, so the unsubscribe-first ordering
  is expressed directly and cannot be accidentally reordered by a refactor.
- **Repeated attempts are visible.** A topic with a climbing `attempts` and a stable `last_error` is
  a bug report, not a mystery.

### 4. The table is the source of truth — deliberately

**Decision: the app tracks subscriptions manually, in the table above, and does not attempt to
reason about FCM's own identifiers.** No FID reads, no token comparisons, no
`FirebaseInstallations` dependency. Subscribe, get a success callback, record it; unsubscribe, get a
success callback, remove it. That is the entire model.

This is a deliberate narrowing, so the reasoning behind it is worth recording:

**Token identity tells you nothing useful.** Topic subscriptions are bound to the Firebase
Installation ID, not the registration token — the token merely contains the FID. Firebase
engineering, on
[firebase-android-sdk#5824](https://github.com/firebase/firebase-android-sdk/issues/5824#issuecomment-2122171289):

> "Each FCM registration token contains the FID to target devices for message delivery. When you
> delete the token (`deleteToken`), the FID is still the same. […] This leads to the same
> subscription being associated."

So a refreshed token changes nothing about what the device receives. `onNewToken` must **not** clear
the table — the subscriptions are still live, and wiping our record would leave every
no-longer-required topic subscribed and permanently invisible, since we can only unsubscribe from
topics we can name. `onNewToken` should reconcile and nothing more, and must be idempotent.

**Token churn is not a cleanup mechanism.** An earlier maintainer comment on the same issue:
*"The best course of action would be to just subscribe and unsubscribe to topics as needed."*
Invalidated tokens are only reaped by a daily internal job, so deleting one buys a delivery gap and
no cleanup at all.

`PushMessaging` therefore needs no new method — the existing three suffice.

#### What each mechanism can and cannot repair

| | Our record | FCM's actual state |
|---|---|---|
| `clearAll()` alone | wiped | **unchanged** — actively harmful, never do this |
| Explicit unsubscribe of every row | rows removed as confirmed | **cleared**, for topics we can name |
| `deleteToken()` / new token | irrelevant | **unchanged** — same FID, same subscriptions |

`clearAll()` alone is the trap: we only unsubscribe from topics we hold a row for, so wiping the
record makes stale subscriptions permanently invisible instead of fixing them. The only correct
reset is:

```kotlin
suspend fun forceResubscribe() {
    confirmedTopics().forEach { topic ->
        pushMessaging.unsubscribeFromTopic(topic)
            .onSuccess { deleteRow(topic) }
            .onFailure { recordFailure(topic, it.message) }
    }
    reconcile()
}
```

#### The one residual risk: restore-from-backup

Manual tracking is sound for every ordinary lifecycle event. A reinstall wipes app data, so the
table starts empty *and* the installation starts with no subscriptions — consistent.

The exception is a **backup restore onto a fresh installation**. The restored table would claim
subscriptions belonging to the *old* installation, while the new one has none. Because those rows
read `confirmed = 1`, reconciliation would consider them done and never subscribe — leaving the
device silently under-subscribed, which is the dangerous direction.

**Mitigation: exclude this table from platform backup** (Android auto-backup rules; iOS iCloud
backup exclusion), so a restored install starts empty and rebuilds correctly. That is a
configuration line, and it achieves what FID tracking would have without adding a dependency.

**TODO:** confirm the app's current backup rules and whether the SQLDelight database is included. If
excluding it is impractical, the fallback is the reset action above, surfaced in Diagnostics.

#### If the table is ever lost

Storage cleared while the installation survives is the one case the table cannot repair by itself.
In order of preference:

1. **Prevent it.** Audit any "clear data" or cache-reset affordance to confirm it cannot drop this
   table without first unsubscribing.
2. **Enumerated sweep.** Unsubscribe across the known universe (~87 topics for one env and platform)
   and reconcile. Inelegant, but bounded, targeted, and side-effect free — and since we own the
   whole `v6_*` namespace it provably covers everything. A deliberate recovery action, never
   steady-state behaviour.
3. Accept it. The device is over-subscribed within topics the user plausibly wanted anyway; extra
   notifications are the benign direction per the table at the top.

```kotlin
suspend fun forceResubscribe() {
    confirmedTopics().forEach { topic ->
        pushMessaging.unsubscribeFromTopic(topic)
            .onSuccess { deleteRow(topic) }
            .onFailure { recordFailure(topic, it.message) }
    }
    reconcile()
}
```

### Why not unsubscribe from every possible topic on each reconcile

Tempting, since FCM cannot tell us what is actually subscribed. Rejected:

- The per-device universe is ~87 topics (6 classes × 10 types, + 23 attribute, + 3 broadcast, for
  one env and platform). Sweeping before subscribing ~36 is ~123 operations **per settings change**.
- It opens a window in which the device is subscribed to nothing. If the app is killed mid-sweep the
  user is left silently under-subscribed — turning a rare, mild drift into a frequent instance of the
  worst failure in the table.

It remains the correct **last-resort repair** for a lost table, since no FCM call clears a
subscription we cannot name — see "If the table is ever lost" above. What it must never be is
routine.

## One-time changeover

Gated on a new `hasCompletedV6Changeover` flag, run before the first V6 reconciliation:

1. **Unsubscribe the legacy topics** — `prod_v5_ios` / `prod_v5_android` / `debug_v5_*`, plus the
   existing V4 unsubscribe. This is what prevents double delivery during the server's dual-send
   window: a V6 client that stays on the V5 topics receives everything twice.
2. Reconcile normally.

No V6 sweep is needed here: a device reaching this path has never subscribed to a `v6_*` topic, so
there is nothing stale to remove. If a changeover ever does need a clean slate, `forceResubscribe()`
is the tool — not an enumerated sweep.

**There is no preference migration.** See below.

## No preference migration

`subscribedAgencies` / `subscribedLocations` stay `Set<String>` of **numeric IDs** (`"121"`,
`"27"`). Topic names are derived at subscribe time:

```kotlin
NotificationAgency.getAll().firstOrNull { it.id.toString() == id }?.topicName
```

The earlier draft proposed migrating storage to group names. Rejected: it forfeits the property that
just made a real rename free. When the server renamed the India *location* group from `isro` to
`india`, no user's saved settings were touched, precisely because selections persist by ID and
`topicName` is derived. Keying storage by name makes every future group rename a breaking change
needing its own migration.

Keeping IDs also deletes work: no migration, no `hasCompletedV6Migration` flag, no
drop-unrecognised path.

**Consequence to accept:** a server-side group rename still requires an app release to update the
`topicName` constant. What it no longer requires is touching user data.

## Names come from the contract

Every topic segment is pinned in `contracts/notification-topics.v6.json`, enforced by
`V6TopicContractTest`. Two subtleties that test already guards:

- **`isroAgency`, not `isro`.** Agency and location groups share one flat attribute-topic namespace.
  The location group is `india`; the agency keeps its suffix so a bare `isro` can never be ambiguous
  again.
- **Broadcast toggles need translation.** The persisted setting ids are `events`, `featured_news`,
  `announcements`; the wire tokens are `events`, `news`, `announce`. Subscribing to
  `v6_prod_ios_featured_news` reaches nothing. The ids are deliberately *not* renamed to match —
  they are persisted map keys, and renaming them would reset every user's toggle to its default.

## Diagnostics

The Diagnostics screen gains a subscription section, read straight off the table:

- derived audience class, and the required-set size
- counts by state: confirmed, pending subscribe, pending unsubscribe
- **any row where `desired != confirmed`**, with `last_error`, `attempts`, and `last_attempt`
- timestamp of the last fully-clean reconciliation
- a **"Reset push subscriptions"** action: `forceResubscribe()` — unsubscribes every topic we hold a
  record of, then rebuilds from settings. Warn that notifications pause briefly while it runs. Word
  it *"resubscribe from scratch"*, **not** *"fix all subscriptions"*: it repairs only what the table
  knows about, and nothing on the FCM side can repair what it doesn't.

Once filtering is server-side this is the only way anyone — user or support — can answer "why am I
not getting notifications?" Without it a failed subscribe is invisible to everybody: the device shows
nothing and the server reports a successful send.

## Also required (unchanged from the earlier draft)

Restated so this document is self-contained.

**iOS — NSE becomes enrichment-only.** Delete all filtering and the empty-content suppression trick
from `NotificationService.swift`; keep image attachment and the re-alert policy. Every path calls
`contentHandler` with content that renders. `serviceExtensionTimeWillExpire` delivering the original
content is now *correct*, not a leak. Delete `NSENotificationFilter.swift` and
`NSEFilterPreferences.swift`; retire the filter keys from `NSEPreferenceBridge.kt`. `AppDelegate`'s
`willPresent` stops filtering. The resulting property is the point of the change: **if the NSE
crashes, times out, or never runs, iOS renders the original alert** — worst case unenriched, never
missing.

**Android — worker stops filtering.** In `NotificationWorker.kt` remove only the filter calls
(`V5NotificationFilter.shouldShow`, the agency/location/webcast checks, per-type toggle checks).
Detection order and rendering are unchanged. Delete `V5NotificationFilter.kt` or reduce it to what
`LaunchFilterService` genuinely shares.

**Kill switch.** A local `enableNotifications` check may be retained as defence against a failed
unsubscribe, but it **must fail open** — missing or unreadable state ⇒ show. Fail-closed local gates
are the defect being removed.

**"Other Agencies" row — approved 2026-08-16.** The server's `otherAgency` catch-all gets a settings
row (`NotificationAgency.OTHER_AGENCY = NotificationAgency(-1, "otherAgency", "Other Agencies")`,
added to `getAll()`; the synthetic `-1` id exists only to satisfy the model shape and is never sent
anywhere). Without it, strict-matching users can never reach LandSpace/Firefly launches.

## Testing

Derivation is pure, so it carries the bulk of the coverage:

1. All 8 combinations of `followAllLaunches` × `useStrictMatching` × `webcastOnly`, including
   follow-all winning over strict.
2. Type topics for the active class only; no attribute topics in follow-all classes; broadcast topics
   follow their toggles; `enableNotifications = false` ⇒ empty set.
3. Topic strings asserted against **literals**, not against a local format helper — a helper that
   drifts alongside the code under test proves nothing. `V6TopicContractTest` pins the vocabulary;
   these pin the assembly.

Reconciliation, against an in-memory driver and a fake FCM:

4. A failed subscribe leaves `confirmed = 0` and is retried on the next reconcile; a failed
   unsubscribe leaves `confirmed = 1` and likewise.
5. **A class switch issues every unsubscribe before any subscribe** — assert on call order, not just
   on the final set. This is the duplicate-delivery guard and is the single most important
   reconciliation test.
6. A class switch interrupted after the unsubscribes leaves the device under-subscribed, never
   holding two classes.
7. **Token refresh does NOT clear the table** — assert the rows survive `onNewToken`. Subscriptions
   are bound to the installation, not the token, so clearing here would orphan every topic no longer
   required.
8. `attempts` increments and `last_error` is populated on failure, and both clear on success.
9. `forceResubscribe()` unsubscribes every confirmed row before reconciling, and running it twice
   converges to the same state.
10. A `forceResubscribe()` whose unsubscribes partly fail leaves those rows present with their error
    recorded, so the next pass retries them rather than losing them.
11. Save with no effective change reconciles to zero FCM operations — repeated saves are free.

**Device testing is the gate**, per the matrix in the server spec, on both platforms — with the iOS
force-quit cases as primary acceptance criteria, since they are what is broken today. Also: NSE
forced to crash (renders unenriched), NSE forced to time out (original delivered), App Group emptied
(**no effect** — the precise condition believed to cause today's field failures).

## Build fresh, delete the old paths

**Decision 2026-08-16: the V6 subscription logic is built new, not evolved out of the V5/V4 code,
and the old paths are removed rather than left dormant.** The existing machinery encodes exactly the
assumptions V6 removes (one broadcast topic, client-side filtering, subscription-as-bookkeeping),
and this session already caught two dead artifacts from past migrations misleading review — the
stale `NOTIFICATION_TYPE_TOPICS` list and the unused `LAUNCHES_ALL` constant. Dormant paths are not
neutral; they get read as authoritative.

New, in a `notifications/v6` package: the derivation function, the `TopicSubscription` table +
queries, the reconciler, and the changeover step. `PushMessaging` (the platform FCM boundary) is
kept as-is.

**TODO — deprecate and remove during implementation** (each removal lands only after its V6
replacement is in place and the device matrix passes; verify call sites are dead before deleting,
in the spirit of `SubscriptionProcessor.kt`, `NotificationRepositoryImpl.kt:47,183`):

- [ ] `SubscriptionProcessor` V5/V4 topic computation and its 300ms debounce trigger — replaced by
      derivation + save-triggered reconciliation
- [ ] `NotificationState.subscribedTopics` field — superseded by the `TopicSubscription` table
- [ ] `V5NotificationFilter.kt` and the filter block in `NotificationData.kt` (keep whatever
      `LaunchFilterService` genuinely shares)
- [ ] `NotificationTopicConfig` V5/V4 topic constants and helpers, once the changeover unsubscribe
      is the only remaining consumer
- [ ] `NotificationTopic.LAUNCHES_ALL` (`"launches"`) — declared, referenced nowhere, reads like a
      master switch and is not one; same trap as the deleted `NOTIFICATION_TYPE_TOPICS`
- [ ] iOS NSE filter files and `NSEPreferenceBridge` filter keys (per "Also required" above)
- [ ] Android worker filter calls (per "Also required" above)

## Rollout

**The server must ship first — approved 2026-08-16, dual-send confirmed.** The server dual-sends V5
and V6, so a client upgrading before the server deploys would unsubscribe from V5 and go dark. This
ordering is a hard dependency, not a preference.

Staged release, watching delivery-regression reports before widening. Server-side,
`sln_v6_notifications_sent_total` shows V6 volume; it cannot show *delivery*, which is why the device
matrix is the gate.

Related server-side work, agreed but outside this spec (tracked for the cutover window):

- **Tracker fan-out goes async** — V6's up-to-12 sends currently run sequentially (30s timeout each)
  on the single-threaded `LaunchEventTracker` loop; to be parallelised server-side.
- **GitOps dashboard panels** for `sln_v6_notifications_sent_total`,
  `sln_notification_sends_skipped_total`, and `sln_notification_group_fallbacks_total` — until they
  exist, V6 dispatch is unobservable in Grafana.

## Non-goals

- Notification history, deep-linking, payload shape — unchanged.
- `hideTbdLaunches` — a list-display setting read by no notification path; gains no topic dimension.
- Per-device token registry (July's Option C) — a server-side concern, deferred there.
- Splitting the audience class into its own topic. It would cut a class switch from 20 operations to
  2, but requires reopening the shipped server condition builder and a contract bump; the durable
  per-topic table above makes the 20-operation switch safe, which was the actual concern.
- Tracking FCM-side identity (FID or token) to detect subscription state. Decided against
  2026-08-16: the table is the source of truth, and the restore-from-backup edge is handled by
  excluding the table from backup rather than by watching identifiers.
- Verifying the two copies of the contract JSON are identical; that remains a review responsibility.
