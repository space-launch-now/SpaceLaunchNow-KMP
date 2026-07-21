# FCM Startup Diagnostics & Observable Push Registration — Spec

**Status:** Draft / proposal
**Date:** 2026-07-20
**Owner:** Caleb Jones
**Area:** Notifications (FCM), Observability (Datadog logging), Debug tooling

---

## 1. Goal

Two connected changes so we can actually tell *why* a user's notifications aren't working:

- **A — Make push registration observable at startup.** Promote the startup FCM / push-token
  sequence to log levels that survive the default diagnostic level, and add explicit log lines for
  the preconditions that are currently silent (OS notifications enabled, Play Services availability,
  token forwarded to RevenueCat, topic subscription results). End with a single structured
  **push-registration summary** line.
- **B — Add FCM/push health to the diagnostics report** — the shareable text users already paste
  into support (`Copy diagnostics report`). No new debug-screen UI: extend the existing report so
  permission/OS-enabled, Play Services, token present + last refresh, forwarded-to-RC, and subscribed
  topics appear alongside the current `Live:` / Datadog rows.

**Non-goals:** changing the notification *delivery* pipeline (`NotificationWorker`, V5 filtering),
adding new remote controls (that's [`docs/logging/REMOTE_LOG_SAMPLING_SPEC.md`](../../docs/logging/REMOTE_LOG_SAMPLING_SPEC.md)),
a standalone FCM debug-screen card (the report is the delivery vehicle), or iOS APNs-specific
diagnostics beyond parity where the common API already covers it.

---

## 2. Background — why this is needed

A premium user (RC id `$RCAnonymousID:bfd5f88…`, Galaxy S24+, Android 16) reported notifications not
arriving. Datadog held **21 logs** for them over the full retention window — all billing / Wear-sync
/ launch-repo at `warn`/`error` — and **zero** push logs: no token fetch, no forward-to-RC, no
`NotificationWorker`. The absence is not an instrumentation gap in *delivery*; it's that **the
startup push sequence logs at `debug`**, and debug logs do not upload at the default diagnostic
level, so a healthy-vs-broken push registration is indistinguishable from the backend.

### 2.1 Every startup push milestone currently logs at `debug`
- **`App.kt` (commonMain), startup token fetch** — logs `FCM Token: …xxxxxx` at `log.d`; only the
  `catch` uses `log.w`. A null/unavailable token is not surfaced at all.
  ```kotlin
  val token = pushMessaging.getToken().getOrNull() ?: "<unavailable>"
  log.d { "FCM Token: …${token.takeLast(6)}" }        // debug — filtered at STANDARD
  ```
- **`MainApplication.kt` Step 4c (androidMain), forward token → RevenueCat** — success, and the
  "token not available yet" skip, both at `log.d`; only the exception path is `log.w`:
  ```kotlin
  if (!fcmToken.isNullOrBlank()) { rcAttrs.setPushToken(fcmToken); log.d { "✅ FCM token forwarded to RevenueCat" } }
  else { log.d { "FCM token not available yet; skipping RC push token set" } }   // debug — the affected user's exact path
  ```
- **`DefaultRevenueCatAttributes.setPushToken` (commonMain)** — `log.d { "RC push token set (length=…)" }`,
  `log.w` on failure.
- **`AndroidPushMessaging.getToken()`** — `"Getting FCM token"` / `"SUCCESS - Got FCM token"` at
  `debug`; `"ERROR - Failed to get FCM token"` at `error` (this is the *only* push line we reliably
  see in prod today, and only when it throws).
- **`AndroidNotificationPermissionHandler`** — permission checks/requests all at `debug`.

### 2.2 Debug logs don't upload at the default diagnostic level
Per [`REMOTE_LOG_SAMPLING_SPEC.md`](../../docs/logging/REMOTE_LOG_SAMPLING_SPEC.md) §2.2: `DiagnosticLevel`
(`OFF`/`STANDARD`/`VERBOSE`) sets the Datadog severity threshold; fresh/release installs default low,
so `debug` is dropped while `warn`/`error` pass. That is exactly the observed pattern — billing
`warn`/`error` present, push `debug` absent. **Milestones and skips on the push happy-path must log
at `info`/`warn` to be visible without asking the user to raise their diagnostic level first.**

### 2.3 The preconditions that actually break push are never logged at startup
Nothing at startup records: whether **POST_NOTIFICATIONS** is granted / notifications enabled at the
OS level (`AndroidNotificationPermissionHandler.hasNotificationPermission()` exists but is only
called from settings/UI), or whether **Google Play Services** is available (no check exists today).
On the affected device the token was simply never obtained and the code took the silent `debug`
skip — indistinguishable in Datadog from "never launched."

---

## 3. Part A — Observable startup push registration

### 3.1 Log-level policy for the push sequence
| Event | Now | Change to |
|---|---|---|
| Token fetch attempt (`Getting FCM token`) | debug | debug (keep) |
| Token fetch **success** | debug | **info** (no token value; length/suffix only) |
| Token fetch returns null/blank (no exception) | *(silent / debug skip)* | **warn** — explicit "FCM token unavailable" |
| Token fetch **exception** | error | error (keep) |
| Forwarded to RevenueCat | debug | **info** |
| "token not available yet; skipping RC push token set" | debug | **warn** |
| RC `setPushToken` success / failure | debug / warn | info / warn |
| POST_NOTIFICATIONS / notifications-enabled at startup | *(not logged)* | **info if enabled, warn if not** |
| Google Play Services availability | *(not checked)* | **info if available, warn/error otherwise** |

Never log the raw token (keep the existing last-6/length redaction).

### 3.2 One structured summary line
After the sequence runs, emit a single line carrying the whole picture as attributes so it is
greppable and dashboard-able in Datadog — e.g.:

```
log.i { "Push registration summary" }   // + attrs:
//   push.token_present, push.token_suffix, push.forwarded_to_rc,
//   push.notifications_enabled, push.play_services (available|missing|update_required),
//   push.subscribed_topic_count, diagnostics.level
```

This gives a per-launch health record even at `STANDARD`, filterable by `@usr.id`. (Prereq for
`@usr.id`/`rc_user_id` attribution is tracked in REMOTE_LOG_SAMPLING_SPEC §2.5 / Phase 0 — reuse it,
don't duplicate.)

### 3.3 Add the missing precondition checks at startup
- Call `hasNotificationPermission()` once during the push init step and log the result at
  `info`/`warn`.
- Add a Play Services availability check (androidMain; `GoogleApiAvailability.isGooglePlayServicesAvailable`)
  behind a small `expect/actual` so common startup can log it; iOS actual returns "n/a".
- Log topic subscription outcomes at `info` (count) / `warn` (failures).

### 3.4 Record a cached snapshot (feeds both the summary line and the report)
The same values behind the §3.2 summary are written to a small in-memory holder
(`PushDiagnostics`, commonMain — last token suffix/length + refresh time, forwarded-to-RC outcome,
notifications-enabled, Play Services state, subscribed-topic count). This lets **Part B read the
outcomes synchronously at share-time** without re-running async work, and keeps the source of truth
in one place. It is a runtime cache only (no persistence needed).

---

## 4. Part B — FCM/push health in the diagnostics report

The shareable report is built in `ui/settings/DiagnosticsScreen.kt` (`Copy/Share diagnostics
report`). It already appends `Version` / `Diagnostic level` / `Datadog initialized…` / `Live: …` /
`Live agencies` / `Live locations`, and then folds in **platform rows** via the existing seam:

```kotlin
val platformRows = remember { platformNotificationDiagnostics() }   // commonMain expect
...
platformRows.forEach { (l, v) -> appendLine("$l: $v") }             // already in the report builder
```

`platformNotificationDiagnostics(): List<Pair<String, String>>` is the extension point. Today the
**Android actual returns `emptyList()`** (`DiagnosticsProviders.android.kt`); iOS returns NSE
App-Group rows. So the entire change is **populating that provider from the §3.4 `PushDiagnostics`
snapshot** — no edits to the report builder itself, and the new lines appear automatically in the
text users already paste into support, even at `OFF`/`STANDARD`.

Rows to add (label: value), redaction preserved (suffix/length only, never the raw token):

| Label | Source |
|---|---|
| `Notifications enabled` | `hasNotificationPermission()` (Android 13+ POST_NOTIFICATIONS; true below) |
| `Play Services` | `available` / `missing` / `update_required` (Android); `n/a` (iOS/Desktop) |
| `FCM token` | `present …<suffix> (len N)` or `unavailable` |
| `FCM token refreshed` | last-refresh time from the snapshot |
| `Forwarded to RevenueCat` | `yes @<time>` / `skipped: <reason>` |
| `Subscribed topics` | count (prod/debug topic already implied by `Live:` rows) |

- **Android:** implement the actual in `DiagnosticsProviders.android.kt` reading `PushDiagnostics`
  (+ synchronous permission / Play Services checks).
- **iOS:** append the equivalent APNs/token rows to its existing actual (keep NSE rows).
- **Diagnostic level / Datadog uploading** are already in the report (`Diagnostic level:` and
  `Datadog initialized: …, uploading: …`) — no duplication needed; they already tell us whether a
  user's logs can be collected.

No new screen, view-model, or persisted settings.

---

## 5. Component / file change list

| Change | Location |
|---|---|
| Promote startup token fetch to info/warn + explicit unavailable log | `composeApp/src/commonMain/.../App.kt` |
| Promote Step 4c forward-to-RC milestones/skip to info/warn | `composeApp/src/androidMain/.../MainApplication.kt` |
| `getToken()` success → info (keep error) | `composeApp/src/androidMain/.../data/notifications/AndroidPushMessaging.kt` (+ iOS/Desktop actuals) |
| Startup permission + Play Services checks; `push.*` summary line | startup init (common + `expect/actual` for Play Services) |
| Play Services availability `expect/actual` | `commonMain` expect + `androidMain`/`iosMain`/`desktopMain` actuals (new) |
| `PushDiagnostics` runtime snapshot holder | `commonMain/.../util/logging/` (new); written by startup (Part A) |
| Populate `platformNotificationDiagnostics()` from the snapshot | `util/logging/DiagnosticsProviders.android.kt` (was `emptyList()`), iOS actual adds token rows |
| Report builder — **no change** (already folds in `platformRows`) | `ui/settings/DiagnosticsScreen.kt` |
| Reuse `@usr.id`/`rc_user_id` attribution | per REMOTE_LOG_SAMPLING_SPEC §2.5 / Phase 0 (dependency, not owned here) |

---

## 6. Testing

- **Unit:** the summary-attribute builder (token-present/absent, forwarded/skipped,
  permission on/off, Play Services available/missing) maps to the expected fields; token value never
  appears in any log/attribute (only suffix/length).
- **Level policy:** happy-path milestones assert at `info`, skips at `warn`, failures at `error`
  (so they survive a `STANDARD` threshold).
- **Report:** `platformNotificationDiagnostics()` (Android) returns the expected rows from a seeded
  `PushDiagnostics` snapshot; the assembled report text contains the new `FCM token` / `Notifications
  enabled` / `Play Services` / `Forwarded to RevenueCat` lines; raw token never present.
- **Manual:** on a device with notifications **denied** and/or Play Services missing, confirm both
  the startup summary (Datadog, at `STANDARD`) and the copied **diagnostics report** flag it.
  Reproduce the reported case (token silently unavailable) → confirm it now shows as a `warn` in
  Datadog and `FCM token: unavailable` in the report, not silence.
- **Regression:** Desktop (no Firebase/Play Services) → no crash, "n/a" states; iOS report still
  includes its NSE rows.

---

## 7. Open questions

1. **Summary cadence.** Log the `push.*` summary once per cold start only, or also when the token
   refreshes / permission changes?
2. **Play Services on non-GMS devices.** Report `missing` as `warn` or `error`? (Huawei/AOSP installs
   legitimately have no GMS and can't receive FCM — likely `warn` + a distinct reason code.)
3. **Diagnostic-level nudge.** The report already prints `Diagnostic level` + `uploading`. Do we want
   the Diagnostics screen to also offer a one-tap "raise to VERBOSE for 72h" so a user's push logs
   become collectable, tying into REMOTE_LOG_SAMPLING_SPEC? (Screen action, not part of the report text.)
