# V6 Notification Subscriptions (KMP Client) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the KMP client subscribe devices to the `v6_*` FCM topics the server already dual-sends, with a durable per-topic subscription ledger, unsubscribe-first reconciliation, a one-time V5→V6 changeover, save-triggered + app-start reconciliation, diagnostics, and backup exclusion.

**Architecture:** All derivation logic lives in one pure function (`V6Topics.requiredTopics`); all I/O lives in a dumb loop (`V6SubscriptionReconciler`) over a SQLDelight table (`TopicSubscription`) whose `confirmed` column is written only on FCM success callbacks. The reconciler is triggered by explicit Save, once per app start, on Android token refresh, and immediately when the master kill switch flips.

**Tech Stack:** Kotlin Multiplatform (commonMain), SQLDelight 2 (`SpaceLaunchDatabase`), DataStore preferences, Koin DI, Compose Multiplatform, kotlin.test (+ `JdbcSqliteDriver` in-memory for desktopTest).

**Spec:** `docs/superpowers/specs/2026-08-16-v6-notification-subscriptions-kmp-design.md` (this repo). Topic vocabulary: `contracts/notification-topics.v6.json`, already pinned by `V6TopicContractTest`.

## Global Constraints

- **Workspace:** the existing worktree `D:\code\SpaceLaunchNow\SpaceLaunchNow-KMP-Main\.worktrees\v6-notification-subscriptions`, branch `feat/v6-notification-subscriptions`. Do NOT create a new worktree; the three gitignored local files a fresh worktree needs are already present here.
- **Gradle:** every gradle command needs `$env:JAVA_HOME = "D:\tools\Android Studio\jbr"` first (PowerShell). Test task is `:composeApp:desktopTest` (the JVM target is `jvm("desktop")`; `src/jvmTest` is dead). commonTest classes run under desktopTest.
- **Verification floor before any "done" claim:** `:composeApp:desktopTest` green AND `:composeApp:compileDebugKotlinAndroid` + `:composeApp:compileDebugUnitTestKotlinAndroid` exit 0. iOS Kotlin/Swift cannot compile on this Windows machine — iOS-touching steps are review-verified here and compile-verified by CI.
- **Commits:** one-line subject only, conventional prefix, NO `Co-Authored-By` trailer, no body.
- **Topic grammar (from the contract, verbatim):** attribute `v6_{env}_{group}`; type `v6_{env}_{platform}_{audienceClass}_{notificationType}`; broadcast `v6_{env}_{platform}_{broadcastKind}`. `env ∈ {prod, debug}`, `platform ∈ {android, ios}`. Audience classes: `all, flex, strict, all_w, flex_w, strict_w`. Topic names are constructed, never parsed.
- **The 10 launch types (exactly these, no more):** `twentyFourHour, oneHour, tenMinutes, oneMinute, netstampChanged, webcastLive, inFlight, success, failure, partial_failure`. `webcastOnly` is the class modifier, NOT a type topic. `events/featured_news/announcements` are broadcast toggles, NOT type topics.
- **Broadcast translation (setting id → wire token):** `events → events`, `featured_news → news`, `announcements → announce`. Never use the setting id as the topic segment.
- **Invariants:** `confirmed` is written ONLY in an FCM success callback; every unsubscribe is issued before any subscribe within a reconcile pass; `onNewToken` / token refresh never clears the table; `clearAll()`-style wipes without unsubscribing are forbidden.
- **Derivation tests assert literal topic strings**, never a shared format helper.

## Recorded deviations from the spec (decided at planning time)

1. **Plain `INTEGER` 0/1 columns, not `INTEGER AS Boolean`.** No `.sq` file in this repo uses column adapters; `AS Boolean` would change the generated `SpaceLaunchDatabase` constructor signature repo-wide. The store class converts at its boundary.
2. **Migration file is `10.sqm`, not `11.sqm`.** Repo convention: `N.sqm` migrates schema N→N+1; current version is 10 with migrations `1.sqm`–`9.sqm`. `version = 11` in build.gradle.kts, as the spec says.
3. **`confirm()` also resets `attempts` to 0.** The spec's SQL sketch leaves `attempts` untouched but its test #8 says "both clear on success"; the test wording wins (a clean row permanently reading `attempts=3` misleads diagnostics).
4. **`setNotificationsEnabled` triggers an immediate reconcile** in addition to Save/app-start. The spec makes unsubscribing "the real kill switch"; a kill switch that waits for a Save press or next app start is not one.
5. **The whole `spacelaunchnow.db` is excluded from backup**, not just the table — SQLite backup granularity is per-file, and every other table in that DB is TTL'd cache. (Android had NO backup rules at all before this plan; both rules files are net-new.)
6. **iOS token refresh:** there is no Kotlin-side `onNewToken` hook on iOS (token refresh lands in `AppDelegate.swift`). The app-start reconcile covers iOS; Android gets the explicit hook.
7. **"Last clean reconcile" lives in `PushDiagnostics` (in-memory)**, not persisted — a reconcile runs on every app start, so the value exists moments after launch, and it avoids new storage keys.

## Out of scope (follow-up plan, gated on the device matrix per the spec)

- iOS NSE enrichment-only rewrite, deletion of `NSENotificationFilter.swift` / `NSEFilterPreferences.swift`, NSE filter keys in `NSEPreferenceBridge.kt`, `AppDelegate.willPresent` filtering.
- Android `NotificationWorker.kt` filter-call removal, `V5NotificationFilter.kt` deletion.
- Deletion of `SubscriptionProcessor.kt`, `NotificationState.subscribedTopics`, `NotificationTopicConfig` V5/V4 constants, `NotificationTopic.LAUNCHES_ALL`. (This plan *disconnects* the V5 subscription path — nothing may re-subscribe to `prod_v5_*` once the changeover unsubscribes it — but deletes no files.)

## File Structure

New files (package `me.calebjones.spacelaunchnow.data.notifications.v6` unless noted):

| File | Responsibility |
|---|---|
| `composeApp/src/commonMain/kotlin/.../data/notifications/v6/V6Topics.kt` | Pure derivation: settings → exact topic set. Zero I/O. |
| `composeApp/src/commonMain/kotlin/.../data/notifications/v6/TopicMessaging.kt` | 2-method FCM boundary interface + `PushTopicMessaging` adapter over `PushMessaging`. Exists so a fake can live in test code (PushMessaging is an `expect class`, not fakeable). |
| `composeApp/src/commonMain/kotlin/.../data/notifications/v6/TopicSubscriptionStore.kt` | Thin wrapper over the generated queries; Boolean↔Long conversion; the one transaction. |
| `composeApp/src/commonMain/kotlin/.../data/notifications/v6/V6SubscriptionReconciler.kt` | The dumb loop: changeover, unsubscribe-first reconcile, forceResubscribe. |
| `composeApp/src/commonMain/sqldelight/me/calebjones/spacelaunchnow/database/TopicSubscription.sq` | Table + queries. |
| `composeApp/src/commonMain/sqldelight/migrations/10.sqm` | Migration creating the table. |
| `composeApp/src/androidMain/res/xml/backup_rules.xml`, `data_extraction_rules.xml` | Backup exclusion. |
| Tests: `commonTest/.../data/notifications/v6/V6TopicsTest.kt`; `desktopTest/.../data/notifications/v6/TopicSubscriptionStoreTest.kt`, `V6SubscriptionReconcilerTest.kt`, `FakeTopicMessaging.kt` | Derivation is pure → commonTest. Store/reconciler need a real driver → desktopTest with in-memory `JdbcSqliteDriver`. |

Modified: `NotificationState.kt` (OTHER_AGENCY row + `hasCompletedV6Changeover`), `V6TopicContractTest.kt` (flip the otherAgency pin), `NotificationStateStorage.kt` (new key), `NotificationRepository.kt` / `NotificationRepositoryImpl.kt` (reconcile API, disconnect V5 processor), `AppModule.kt` (DI), `SettingsViewModel.kt` + `NotificationSettingsScreen.kt` (Save), `SpaceLaunchFirebaseMessagingService.kt` (onNewToken), `AppDelegate.swift` (delete k_debug_v4 auto-subscribe), `DiagnosticsScreen.kt` + `PushDiagnostics.kt` (diagnostics), `AndroidManifest.xml`, `DatabaseDriverFactory.ios.kt` (backup), `App.kt` (remove stale diagnostics line), `composeApp/build.gradle.kts` (schema version).

---

### Task 1: "Other Agencies" settings row

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationState.kt:400-410` (NotificationAgency companion)
- Test: `composeApp/src/desktopTest/kotlin/me/calebjones/spacelaunchnow/data/model/V6TopicContractTest.kt:129-138`

**Interfaces:**
- Consumes: nothing.
- Produces: `NotificationAgency.OTHER_AGENCY: NotificationAgency` with `id = -1`, `topicName = "otherAgency"`, `name = "Other Agencies"`, present in `NotificationAgency.getAll()`. Task 2's derivation test uses the stored id `"-1"`.

- [ ] **Step 1: Flip the contract test to expect full coverage (failing test)**

In `V6TopicContractTest.kt`, replace the test at lines 129-138 with:

```kotlin
    @Test
    fun `every subscribable group has a settings row that reaches it`() {
        // otherAgency gained its row when the spec was approved (2026-08-16).
        // From here on, any subscribable group with no row is an accident: a
        // group the server sends to that no user can select.
        val offered = (NotificationAgency.getAll().map { it.topicName } +
            NotificationLocation.getAll().map { it.topicName }).toSet()
        val missing = (subscribableGroups("agencyGroups") + subscribableGroups("locationGroups"))
            .filterNot { it in offered }
        assertEquals(emptyList(), missing)
    }
```

- [ ] **Step 2: Run it to verify it fails**

```powershell
$env:JAVA_HOME = "D:\tools\Android Studio\jbr"
.\gradlew.bat :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.data.model.V6TopicContractTest"
```

Expected: FAIL — `missing` contains `otherAgency`.

- [ ] **Step 3: Add the row**

In `NotificationState.kt`, immediately after the `ISRO` declaration (line 400), add:

```kotlin
        // Catch-all row approved 2026-08-16. The server maps every agency ID
        // absent from its curated table to the "otherAgency" group; without
        // this row, strict-matching users can never reach a LandSpace or
        // Firefly launch no matter what they select. The -1 id exists only to
        // satisfy the model shape — it is never sent anywhere; the server owns
        // ID-to-group mapping and only the topicName goes over the wire.
        val OTHER_AGENCY = NotificationAgency(-1, "otherAgency", "Other Agencies")
```

And change `getAll()` (lines 405-410) to include it last:

```kotlin
        fun getAll(): List<NotificationAgency> {
            return listOf(
                SPACEX, NASA, BLUE_ORIGIN, ROCKET_LAB, VIRGIN_GALACTIC, NORTHROP_GRUMMAN,
                ULA, ARIANESPACE, RUSSIA, CHINA, ISRO, OTHER_AGENCY
            )
        }
```

- [ ] **Step 4: Run the full contract test class — all pass**

Same command as Step 2. Expected: PASS (10 tests). Note: the settings screen builds its agency list from `getAll()`, so the row appears in the UI with no screen change; `withFollowAllLaunches` will now also store `"-1"`, which is correct (follow-all classes ignore attribute topics anyway).

- [ ] **Step 5: Commit**

```powershell
git add -A; git commit -m "feat(notifications): add the Other Agencies settings row for the otherAgency catch-all"
```

---

### Task 2: Pure derivation — `V6Topics`

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/V6Topics.kt`
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/V6TopicsTest.kt`

**Interfaces:**
- Consumes: `NotificationState`, `NotificationTopic`, `NotificationAgency`, `NotificationLocation` (Task 1's OTHER_AGENCY).
- Produces: `V6Topics.requiredTopics(state: NotificationState, env: String, platform: String): Set<String>` and `V6Topics.audienceClass(state: NotificationState): String`. Used by Tasks 4, 9.

- [ ] **Step 1: Write the failing tests**

Create `V6TopicsTest.kt`:

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.data.model.NotificationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V6TopicsTest {

    // Flexible matching, SpaceX (121) + Florida (27), only tenMinutes enabled,
    // webcastOnly off, all broadcast toggles off. Topic strings are asserted
    // against literals on purpose: a format helper shared with production code
    // would drift with it and prove nothing.
    private fun baseState() = NotificationState(
        enableNotifications = true,
        followAllLaunches = false,
        useStrictMatching = false,
        topicSettings = mapOf(
            "tenMinutes" to true,
            "twentyFourHour" to false, "oneHour" to false, "oneMinute" to false,
            "netstampChanged" to false, "webcastLive" to false, "inFlight" to false,
            "success" to false, "failure" to false, "partial_failure" to false,
            "webcastOnly" to false,
            "events" to false, "featured_news" to false, "announcements" to false,
        ),
        subscribedAgencies = setOf("121"),
        subscribedLocations = setOf("27"),
    )

    @Test
    fun audienceClassCoversAllEightSettingCombinations() {
        fun state(followAll: Boolean, strict: Boolean, webcast: Boolean) = baseState().copy(
            followAllLaunches = followAll,
            useStrictMatching = strict,
            topicSettings = baseState().topicSettings + ("webcastOnly" to webcast),
        )
        assertEquals("all", V6Topics.audienceClass(state(true, false, false)))
        assertEquals("all_w", V6Topics.audienceClass(state(true, false, true)))
        // Follow-all wins over strict, matching the UI which disables the
        // strict toggle when follow-all is on.
        assertEquals("all", V6Topics.audienceClass(state(true, true, false)))
        assertEquals("all_w", V6Topics.audienceClass(state(true, true, true)))
        assertEquals("strict", V6Topics.audienceClass(state(false, true, false)))
        assertEquals("strict_w", V6Topics.audienceClass(state(false, true, true)))
        assertEquals("flex", V6Topics.audienceClass(state(false, false, false)))
        assertEquals("flex_w", V6Topics.audienceClass(state(false, false, true)))
    }

    @Test
    fun flexClassEmitsTypeAndAttributeTopics() {
        assertEquals(
            setOf("v6_prod_ios_flex_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            V6Topics.requiredTopics(baseState(), env = "prod", platform = "ios"),
        )
    }

    @Test
    fun disabledNotificationsDeriveTheEmptySet() {
        val state = baseState().copy(enableNotifications = false)
        assertEquals(emptySet(), V6Topics.requiredTopics(state, "prod", "ios"))
    }

    @Test
    fun followAllEmitsNoAttributeTopics() {
        val state = baseState().copy(followAllLaunches = true)
        assertEquals(
            setOf("v6_prod_ios_all_tenMinutes"),
            V6Topics.requiredTopics(state, "prod", "ios"),
        )
    }

    @Test
    fun webcastOnlyIsAClassSuffixNotATypeTopic() {
        val state = baseState().copy(
            topicSettings = baseState().topicSettings + ("webcastOnly" to true)
        )
        val topics = V6Topics.requiredTopics(state, "prod", "ios")
        assertEquals(
            setOf("v6_prod_ios_flex_w_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            topics,
        )
        assertTrue(topics.none { it.endsWith("_webcastOnly") })
    }

    @Test
    fun broadcastTogglesTranslateSettingIdsToWireTokens() {
        val state = baseState().copy(
            topicSettings = baseState().topicSettings +
                mapOf("events" to true, "featured_news" to true, "announcements" to true)
        )
        val topics = V6Topics.requiredTopics(state, "prod", "android")
        assertTrue("v6_prod_android_events" in topics)
        // featured_news -> news, announcements -> announce. Subscribing to
        // v6_prod_android_featured_news would reach nothing.
        assertTrue("v6_prod_android_news" in topics)
        assertTrue("v6_prod_android_announce" in topics)
        assertTrue(topics.none { it.contains("featured_news") || it.contains("announcements") })
    }

    @Test
    fun everyEnabledLaunchTypeGetsAClassScopedTypeTopic() {
        val allOn = baseState().copy(
            topicSettings = baseState().topicSettings + mapOf(
                "twentyFourHour" to true, "oneHour" to true, "oneMinute" to true,
                "netstampChanged" to true, "webcastLive" to true, "inFlight" to true,
                "success" to true, "failure" to true, "partial_failure" to true,
            )
        )
        val typeTopics = V6Topics.requiredTopics(allOn, "debug", "android")
            .filter { it.startsWith("v6_debug_android_flex_") }
        assertEquals(
            setOf(
                "v6_debug_android_flex_twentyFourHour", "v6_debug_android_flex_oneHour",
                "v6_debug_android_flex_tenMinutes", "v6_debug_android_flex_oneMinute",
                "v6_debug_android_flex_netstampChanged", "v6_debug_android_flex_webcastLive",
                "v6_debug_android_flex_inFlight", "v6_debug_android_flex_success",
                "v6_debug_android_flex_failure", "v6_debug_android_flex_partial_failure",
            ),
            typeTopics.toSet(),
        )
    }

    @Test
    fun attributeTopicsAreSharedAcrossPlatforms() {
        val ios = V6Topics.requiredTopics(baseState(), "prod", "ios")
        val android = V6Topics.requiredTopics(baseState(), "prod", "android")
        assertTrue("v6_prod_spacex" in ios)
        assertTrue("v6_prod_spacex" in android)
    }

    @Test
    fun otherAgencySelectionDerivesItsAttributeTopic() {
        val state = baseState().copy(subscribedAgencies = setOf("-1"))
        assertTrue("v6_prod_otherAgency" in V6Topics.requiredTopics(state, "prod", "ios"))
    }

    @Test
    fun unknownStoredIdsAreSkippedNotCrashed() {
        val state = baseState().copy(
            subscribedAgencies = setOf("121", "99999"),
            subscribedLocations = setOf("27", "99999"),
        )
        assertEquals(
            setOf("v6_prod_ios_flex_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            V6Topics.requiredTopics(state, "prod", "ios"),
        )
    }
}
```

- [ ] **Step 2: Run to verify they fail to compile (V6Topics does not exist)**

```powershell
.\gradlew.bat :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.data.notifications.v6.V6TopicsTest"
```

Expected: compilation FAILURE, unresolved reference `V6Topics`.

- [ ] **Step 3: Implement `V6Topics.kt`**

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic

/**
 * Pure derivation of the V6 topic set. No I/O, no suspend, no FCM, no database:
 * given settings, the exact set of FCM topics this device should hold. Under V6
 * the subscription IS the filter, so this function is the whole client-side
 * complexity of the scheme. Topic vocabulary is pinned by V6TopicContractTest;
 * assembly is pinned by V6TopicsTest against literal strings.
 */
object V6Topics {

    // Exactly the contract's 10 notificationTypes. NOT getUserConfigurableTopics():
    // that list also carries webcastOnly (the class modifier) and the three
    // broadcast toggles, none of which is a type topic.
    private val LAUNCH_TYPE_TOPICS: List<NotificationTopic> = listOf(
        NotificationTopic.TWENTY_FOUR_HOUR,
        NotificationTopic.ONE_HOUR,
        NotificationTopic.TEN_MINUTES,
        NotificationTopic.ONE_MINUTE,
        NotificationTopic.NETSTAMP_CHANGED,
        NotificationTopic.WEBCAST_LIVE,
        NotificationTopic.IN_FLIGHT,
        NotificationTopic.SUCCESS,
        NotificationTopic.FAILURE,
        NotificationTopic.PARTIAL_FAILURE,
    )

    // Persisted setting id -> wire token. featured_news/announcements differ:
    // the ids are persisted map keys (renaming would reset every user's toggle),
    // while the server only ever sends to the token form.
    private val BROADCAST_TOKENS: Map<NotificationTopic, String> = mapOf(
        NotificationTopic.EVENTS to "events",
        NotificationTopic.FEATURED_NEWS to "news",
        NotificationTopic.ANNOUNCEMENTS to "announce",
    )

    /** Exactly one class per device. Follow-all wins over strict, matching the UI. */
    fun audienceClass(state: NotificationState): String {
        val webcastOnly = state.isTopicEnabled(NotificationTopic.WEBCAST_ONLY)
        return when {
            state.followAllLaunches -> if (webcastOnly) "all_w" else "all"
            state.useStrictMatching -> if (webcastOnly) "strict_w" else "strict"
            else -> if (webcastOnly) "flex_w" else "flex"
        }
    }

    fun requiredTopics(state: NotificationState, env: String, platform: String): Set<String> {
        // Unsubscribing is the real kill switch now, not a local check.
        if (!state.enableNotifications) return emptySet()

        val audienceClass = audienceClass(state)
        val topics = mutableSetOf<String>()

        LAUNCH_TYPE_TOPICS.filter { state.isTopicEnabled(it) }.forEach { type ->
            topics += "v6_${env}_${platform}_${audienceClass}_${type.id}"
        }

        // Follow-all conditions are the type topic alone; attribute
        // subscriptions under follow-all are dead weight that would leave a
        // later switch out of follow-all starting dirty.
        if (!state.followAllLaunches) {
            state.subscribedAgencies.forEach { id ->
                NotificationAgency.getAll().firstOrNull { it.id.toString() == id }
                    ?.let { topics += "v6_${env}_${it.topicName}" }
            }
            state.subscribedLocations.forEach { id ->
                NotificationLocation.getAll().firstOrNull { it.id.toString() == id }
                    ?.let { topics += "v6_${env}_${it.topicName}" }
            }
        }

        BROADCAST_TOKENS.forEach { (topic, token) ->
            if (state.isTopicEnabled(topic)) topics += "v6_${env}_${platform}_${token}"
        }

        return topics
    }
}
```

- [ ] **Step 4: Run the tests — all pass**

Same command as Step 2. Expected: PASS (10 tests).

- [ ] **Step 5: Commit**

```powershell
git add -A; git commit -m "feat(notifications): derive the V6 topic set as a pure function"
```

---

### Task 3: `TopicSubscription` table + store

**Files:**
- Create: `composeApp/src/commonMain/sqldelight/me/calebjones/spacelaunchnow/database/TopicSubscription.sq`
- Create: `composeApp/src/commonMain/sqldelight/migrations/10.sqm`
- Modify: `composeApp/build.gradle.kts:591-605` (version comment + `version = 11`)
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/TopicSubscriptionStore.kt`
- Test: `composeApp/src/desktopTest/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/TopicSubscriptionStoreTest.kt`

**Interfaces:**
- Consumes: generated `SpaceLaunchDatabase` (`me.calebjones.spacelaunchnow.database`).
- Produces (used by Tasks 4, 6, 9):

```kotlin
class TopicSubscriptionStore(database: SpaceLaunchDatabase) {
    fun replaceDesired(required: Set<String>)
    fun pendingSubscribes(): List<String>
    fun pendingUnsubscribes(): List<String>
    fun confirm(topic: String, confirmed: Boolean, nowMillis: Long)
    fun recordFailure(topic: String, error: String?, nowMillis: Long)
    fun deleteSettled()
    fun confirmedTopics(): List<String>
    fun deleteRow(topic: String)
    fun mismatchedRows(): List<TopicSubscription>   // generated row type
    fun counts(): SubscriptionCounts
}
data class SubscriptionCounts(val confirmed: Long, val pendingSubscribe: Long, val pendingUnsubscribe: Long)
```

- [ ] **Step 1: Create the schema and queries**

`TopicSubscription.sq`:

```sql
-- One row per FCM topic this device has an opinion about. This table is the
-- ONLY record of what the device is subscribed to: FCM offers clients no way
-- to read a token's subscriptions, so this is correctness-critical state, not
-- a cache. `confirmed` changes ONLY on an FCM success callback -- it records
-- what FCM acknowledged, never what we intended. desired/confirmed are 0/1.

CREATE TABLE TopicSubscription (
    topic        TEXT NOT NULL PRIMARY KEY,
    desired      INTEGER NOT NULL,
    confirmed    INTEGER NOT NULL,
    last_attempt INTEGER,
    last_error   TEXT,
    attempts     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_topic_pending ON TopicSubscription(desired, confirmed);

-- INSERT OR IGNORE + UPDATE pair rather than ON CONFLICT DO UPDATE: the
-- project's SQLDelight dialect predates upsert syntax.
insertDesired:
INSERT OR IGNORE INTO TopicSubscription(topic, desired, confirmed)
VALUES (?, 1, 0);

markDesired:
UPDATE TopicSubscription SET desired = 1 WHERE topic = ?;

clearDesiredExcept:
UPDATE TopicSubscription SET desired = 0 WHERE topic NOT IN ?;

clearAllDesired:
UPDATE TopicSubscription SET desired = 0;

pendingUnsubscribes:
SELECT topic FROM TopicSubscription WHERE desired = 0 AND confirmed = 1;

pendingSubscribes:
SELECT topic FROM TopicSubscription WHERE desired = 1 AND confirmed = 0;

confirm:
UPDATE TopicSubscription
SET confirmed = ?, last_error = NULL, attempts = 0, last_attempt = ?
WHERE topic = ?;

recordFailure:
UPDATE TopicSubscription
SET last_error = ?, last_attempt = ?, attempts = attempts + 1
WHERE topic = ?;

deleteSettled:
DELETE FROM TopicSubscription WHERE desired = 0 AND confirmed = 0;

confirmedTopics:
SELECT topic FROM TopicSubscription WHERE confirmed = 1;

deleteRow:
DELETE FROM TopicSubscription WHERE topic = ?;

mismatchedRows:
SELECT * FROM TopicSubscription WHERE desired != confirmed;

countConfirmed:
SELECT COUNT(*) FROM TopicSubscription WHERE confirmed = 1;

countPendingSubscribes:
SELECT COUNT(*) FROM TopicSubscription WHERE desired = 1 AND confirmed = 0;

countPendingUnsubscribes:
SELECT COUNT(*) FROM TopicSubscription WHERE desired = 0 AND confirmed = 1;
```

`migrations/10.sqm`:

```sql
-- Adds TopicSubscription: the device's durable record of its V6 FCM topic
-- subscriptions (see TopicSubscription.sq for semantics).

CREATE TABLE TopicSubscription (
    topic        TEXT NOT NULL PRIMARY KEY,
    desired      INTEGER NOT NULL,
    confirmed    INTEGER NOT NULL,
    last_attempt INTEGER,
    last_error   TEXT,
    attempts     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_topic_pending ON TopicSubscription(desired, confirmed);
```

In `composeApp/build.gradle.kts`, after the `// Version 10 adds StatsCache table (9.sqm)` comment line, add `// Version 11 adds TopicSubscription table for V6 FCM subscriptions (10.sqm)` and change `version = 10` to `version = 11`.

- [ ] **Step 2: Write the failing store test**

`TopicSubscriptionStoreTest.kt` (desktopTest — needs the JVM sqlite driver):

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopicSubscriptionStoreTest {

    private fun newStore(): TopicSubscriptionStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return TopicSubscriptionStore(SpaceLaunchDatabase(driver))
    }

    @Test
    fun replaceDesiredCreatesPendingSubscribeRows() {
        val store = newStore()
        store.replaceDesired(setOf("a", "b"))
        assertEquals(setOf("a", "b"), store.pendingSubscribes().toSet())
        assertEquals(emptyList(), store.pendingUnsubscribes())
    }

    @Test
    fun droppedTopicBecomesPendingUnsubscribeAndSettlesAfterConfirm() {
        val store = newStore()
        store.replaceDesired(setOf("a", "b"))
        store.confirm("a", confirmed = true, nowMillis = 1)
        store.confirm("b", confirmed = true, nowMillis = 1)

        store.replaceDesired(setOf("a"))
        assertEquals(listOf("b"), store.pendingUnsubscribes())

        store.confirm("b", confirmed = false, nowMillis = 2)
        store.deleteSettled()
        assertEquals(emptyList(), store.pendingUnsubscribes())
        assertEquals(listOf("a"), store.confirmedTopics())
    }

    @Test
    fun emptyRequiredSetMarksEverythingUndesired() {
        val store = newStore()
        store.replaceDesired(setOf("a"))
        store.confirm("a", confirmed = true, nowMillis = 1)
        store.replaceDesired(emptySet())
        assertEquals(listOf("a"), store.pendingUnsubscribes())
    }

    @Test
    fun failureRecordsErrorAndCountsAttempts_confirmClearsBoth() {
        val store = newStore()
        store.replaceDesired(setOf("a"))
        store.recordFailure("a", "boom", nowMillis = 5)
        store.recordFailure("a", "boom again", nowMillis = 6)

        val row = store.mismatchedRows().single()
        assertEquals("a", row.topic)
        assertEquals(2L, row.attempts)
        assertEquals("boom again", row.last_error)
        assertEquals(6L, row.last_attempt)

        store.confirm("a", confirmed = true, nowMillis = 7)
        assertEquals(emptyList(), store.mismatchedRows())
        assertEquals(listOf("a"), store.confirmedTopics())
    }

    @Test
    fun countsReflectTableState() {
        val store = newStore()
        store.replaceDesired(setOf("a", "b", "c"))
        store.confirm("a", confirmed = true, nowMillis = 1)
        store.replaceDesired(setOf("a", "b"))   // c -> undesired but unconfirmed
        store.confirm("c", confirmed = true, nowMillis = 1)  // simulate it was confirmed earlier
        val counts = store.counts()
        assertEquals(2L, counts.confirmed)          // a, c
        assertEquals(1L, counts.pendingSubscribe)   // b
        assertEquals(1L, counts.pendingUnsubscribe) // c
        assertTrue(store.mismatchedRows().isNotEmpty())
    }
}
```

- [ ] **Step 3: Run to verify failure**

```powershell
.\gradlew.bat :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.data.notifications.v6.TopicSubscriptionStoreTest"
```

Expected: compilation FAILURE (`TopicSubscriptionStore` unresolved). If instead SQLDelight generation errors, fix the `.sq` before proceeding.

- [ ] **Step 4: Implement the store**

`TopicSubscriptionStore.kt`:

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.database.TopicSubscription

data class SubscriptionCounts(
    val confirmed: Long,
    val pendingSubscribe: Long,
    val pendingUnsubscribe: Long,
)

/**
 * Thin wrapper over the generated TopicSubscription queries. Booleans are
 * stored as 0/1 INTEGER (repo convention -- no column adapters anywhere in the
 * schema); this class is the only place that converts.
 */
class TopicSubscriptionStore(private val database: SpaceLaunchDatabase) {

    private val queries get() = database.topicSubscriptionQueries

    /**
     * Atomically rewrite the desired-set. Rows outside [required] flip to
     * desired=0; rows in it are created (desired=1, confirmed=0) or re-marked
     * desired. `confirmed` is untouched -- only FCM callbacks may change it.
     */
    fun replaceDesired(required: Set<String>) {
        database.transaction {
            if (required.isEmpty()) {
                queries.clearAllDesired()
            } else {
                queries.clearDesiredExcept(required)
            }
            required.forEach { topic ->
                queries.insertDesired(topic)
                queries.markDesired(topic)
            }
        }
    }

    fun pendingSubscribes(): List<String> = queries.pendingSubscribes().executeAsList()

    fun pendingUnsubscribes(): List<String> = queries.pendingUnsubscribes().executeAsList()

    fun confirm(topic: String, confirmed: Boolean, nowMillis: Long) {
        queries.confirm(if (confirmed) 1L else 0L, nowMillis, topic)
    }

    fun recordFailure(topic: String, error: String?, nowMillis: Long) {
        queries.recordFailure(error ?: "unknown", nowMillis, topic)
    }

    fun deleteSettled() = queries.deleteSettled()

    fun confirmedTopics(): List<String> = queries.confirmedTopics().executeAsList()

    fun deleteRow(topic: String) = queries.deleteRow(topic)

    fun mismatchedRows(): List<TopicSubscription> = queries.mismatchedRows().executeAsList()

    fun counts(): SubscriptionCounts = SubscriptionCounts(
        confirmed = queries.countConfirmed().executeAsOne(),
        pendingSubscribe = queries.countPendingSubscribes().executeAsOne(),
        pendingUnsubscribe = queries.countPendingUnsubscribes().executeAsOne(),
    )
}
```

Note: if the generated query parameter order differs (SQLDelight orders parameters as they appear in the statement), follow the compiler — the statement text above puts `confirmed`/`last_error` first and `topic` last.

- [ ] **Step 5: Run the store tests — pass**

Same command as Step 3. Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```powershell
git add -A; git commit -m "feat(notifications): add the TopicSubscription ledger table and store (schema v11)"
```

---

### Task 4: Reconciler core — unsubscribe first, confirm only on success

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/TopicMessaging.kt`
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/V6SubscriptionReconciler.kt`
- Test: `composeApp/src/desktopTest/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/FakeTopicMessaging.kt`
- Test: `composeApp/src/desktopTest/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/V6SubscriptionReconcilerTest.kt`

**Interfaces:**
- Consumes: `V6Topics.requiredTopics` (Task 2), `TopicSubscriptionStore` (Task 3), `NotificationState`.
- Produces (used by Tasks 5, 6):

```kotlin
interface TopicMessaging {
    suspend fun subscribe(topic: String): Result<Unit>
    suspend fun unsubscribe(topic: String): Result<Unit>
}
class PushTopicMessaging(pushMessaging: PushMessaging) : TopicMessaging

data class V6ReconcileResult(val attempted: Int, val failed: Int, val skipped: Boolean = false) {
    val clean: Boolean get() = !skipped && failed == 0
}

class V6SubscriptionReconciler(
    store: TopicSubscriptionStore,
    messaging: TopicMessaging,
    platform: String?,                       // "android" | "ios" | null => no-op
    envProvider: suspend () -> String,       // "prod" | "debug"
    stateProvider: suspend () -> NotificationState,
    markChangeoverComplete: suspend () -> Unit,
    nowMillis: () -> Long,
) {
    suspend fun reconcile(): V6ReconcileResult
    suspend fun forceResubscribe(): V6ReconcileResult   // implemented in Task 5
}
```

- [ ] **Step 1: Write `TopicMessaging.kt`** (no test of its own — it is two delegating lines; the adapter is exercised by Android compile and the fake replaces it in tests)

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.data.notifications.PushMessaging

/**
 * The reconciler's FCM boundary. PushMessaging is an `expect class`, which a
 * common-source fake cannot implement -- this interface exists so tests can
 * record and order calls. PushMessaging itself needs no new methods (spec:
 * subscriptions bind to the installation, not the token; the existing three
 * methods suffice).
 */
interface TopicMessaging {
    suspend fun subscribe(topic: String): Result<Unit>
    suspend fun unsubscribe(topic: String): Result<Unit>
}

class PushTopicMessaging(private val pushMessaging: PushMessaging) : TopicMessaging {
    override suspend fun subscribe(topic: String): Result<Unit> =
        pushMessaging.subscribeToTopic(topic)

    override suspend fun unsubscribe(topic: String): Result<Unit> =
        pushMessaging.unsubscribeFromTopic(topic)
}
```

- [ ] **Step 2: Write the failing reconciler tests**

`FakeTopicMessaging.kt`:

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

/** Records every call in order; fails topics on the deny-lists. */
class FakeTopicMessaging : TopicMessaging {
    val calls = mutableListOf<String>()          // "sub:<topic>" / "unsub:<topic>"
    var failSubscribes: Set<String> = emptySet()
    var failUnsubscribes: Set<String> = emptySet()

    override suspend fun subscribe(topic: String): Result<Unit> {
        calls += "sub:$topic"
        return if (topic in failSubscribes) Result.failure(Exception("boom")) else Result.success(Unit)
    }

    override suspend fun unsubscribe(topic: String): Result<Unit> {
        calls += "unsub:$topic"
        return if (topic in failUnsubscribes) Result.failure(Exception("boom")) else Result.success(Unit)
    }
}
```

`V6SubscriptionReconcilerTest.kt` — the harness plus the core tests. The changeover is Task 5; until then every test starts from a state with `hasCompletedV6Changeover = true` so the changeover path stays out of the way (the field is added in Task 5 — until then use the `Harness.changeoverDone` flag as shown):

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V6SubscriptionReconcilerTest {

    // Flexible matching, SpaceX + Florida, tenMinutes only -- 3 topics.
    private val flexState = NotificationState(
        enableNotifications = true,
        followAllLaunches = false,
        useStrictMatching = false,
        topicSettings = mapOf(
            "tenMinutes" to true,
            "twentyFourHour" to false, "oneHour" to false, "oneMinute" to false,
            "netstampChanged" to false, "webcastLive" to false, "inFlight" to false,
            "success" to false, "failure" to false, "partial_failure" to false,
            "webcastOnly" to false,
            "events" to false, "featured_news" to false, "announcements" to false,
        ),
        subscribedAgencies = setOf("121"),
        subscribedLocations = setOf("27"),
        hasCompletedV6Changeover = true,   // Task 5 adds this field
    )

    private class Harness(initialState: NotificationState) {
        val fake = FakeTopicMessaging()
        val store: TopicSubscriptionStore
        var state: NotificationState = initialState
        var now = 1_000L
        val reconciler: V6SubscriptionReconciler

        init {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            SpaceLaunchDatabase.Schema.create(driver)
            store = TopicSubscriptionStore(SpaceLaunchDatabase(driver))
            reconciler = V6SubscriptionReconciler(
                store = store,
                messaging = fake,
                platform = "ios",
                envProvider = { "prod" },
                stateProvider = { state },
                markChangeoverComplete = { state = state.copy(hasCompletedV6Changeover = true) },
                nowMillis = { now },
            )
        }
    }

    @Test
    fun firstReconcileSubscribesTheDerivedSet() = runTest {
        val h = Harness(flexState)
        val result = h.reconciler.reconcile()
        assertEquals(3, result.attempted)
        assertEquals(0, result.failed)
        assertEquals(
            setOf("v6_prod_ios_flex_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            h.store.confirmedTopics().toSet(),
        )
    }

    @Test
    fun classSwitchIssuesEveryUnsubscribeBeforeAnySubscribe() = runTest {
        // THE duplicate-delivery guard -- asserted on call order, not final state.
        val h = Harness(flexState)
        h.reconciler.reconcile()
        h.fake.calls.clear()

        h.state = h.state.copy(useStrictMatching = true)   // flex -> strict rewrite
        h.reconciler.reconcile()

        val firstSub = h.fake.calls.indexOfFirst { it.startsWith("sub:") }
        val lastUnsub = h.fake.calls.indexOfLast { it.startsWith("unsub:") }
        assertTrue(firstSub > lastUnsub, "unsubscribes must all precede subscribes: ${h.fake.calls}")
        assertTrue("unsub:v6_prod_ios_flex_tenMinutes" in h.fake.calls)
        assertTrue("sub:v6_prod_ios_strict_tenMinutes" in h.fake.calls)
    }

    @Test
    fun interruptedClassSwitchNeverHoldsTwoClasses() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()

        h.state = h.state.copy(useStrictMatching = true)
        h.fake.failSubscribes = setOf("v6_prod_ios_strict_tenMinutes")
        h.reconciler.reconcile()

        // Under-subscribed (a transient gap), never double-classed.
        val confirmedTypes = h.store.confirmedTopics().filter { it.contains("_flex_") || it.contains("_strict_") }
        assertEquals(emptyList(), confirmedTypes)
    }

    @Test
    fun failedSubscribeLeavesRowPendingAndRetriesNextReconcile() = runTest {
        val h = Harness(flexState)
        h.fake.failSubscribes = setOf("v6_prod_spacex")
        val first = h.reconciler.reconcile()
        assertEquals(1, first.failed)
        assertEquals(listOf("v6_prod_spacex"), h.store.pendingSubscribes())

        h.fake.failSubscribes = emptySet()
        val second = h.reconciler.reconcile()
        assertEquals(0, second.failed)
        assertTrue("v6_prod_spacex" in h.store.confirmedTopics())
    }

    @Test
    fun failedUnsubscribeStaysConfirmedAndRetries() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()

        h.state = h.state.copy(subscribedAgencies = setOf("121"), subscribedLocations = setOf("27", "143"))
        h.reconciler.reconcile()   // adds texas
        h.state = h.state.copy(subscribedLocations = setOf("27"))
        h.fake.failUnsubscribes = setOf("v6_prod_texas")
        h.reconciler.reconcile()
        assertTrue("v6_prod_texas" in h.store.pendingUnsubscribes())

        h.fake.failUnsubscribes = emptySet()
        h.reconciler.reconcile()
        assertTrue("v6_prod_texas" !in h.store.confirmedTopics())
    }

    @Test
    fun noChangeReconcileIsZeroFcmOperations() = runTest {
        // Repeated saves are free; also the token-refresh contract -- reconcile,
        // and the rows survive untouched.
        val h = Harness(flexState)
        h.reconciler.reconcile()
        h.fake.calls.clear()

        val result = h.reconciler.reconcile()
        assertEquals(0, result.attempted)
        assertEquals(emptyList(), h.fake.calls)
        assertEquals(3, h.store.confirmedTopics().size)
    }

    @Test
    fun killSwitchDrivesTheTableToEmpty() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()
        h.state = h.state.copy(enableNotifications = false)
        h.reconciler.reconcile()
        assertEquals(emptyList(), h.store.confirmedTopics())
        assertEquals(emptyList(), h.store.pendingUnsubscribes())
    }

    @Test
    fun desktopPlatformIsANoOp() = runTest {
        val h = Harness(flexState)
        val desktop = V6SubscriptionReconciler(
            store = h.store,
            messaging = h.fake,
            platform = null,
            envProvider = { "prod" },
            stateProvider = { h.state },
            markChangeoverComplete = { },
            nowMillis = { 0L },
        )
        val result = desktop.reconcile()
        assertTrue(result.skipped)
        assertEquals(emptyList(), h.fake.calls)
    }
}
```

- [ ] **Step 3: Run to verify compilation failure** (`V6SubscriptionReconciler`, `hasCompletedV6Changeover` unresolved)

```powershell
.\gradlew.bat :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.data.notifications.v6.V6SubscriptionReconcilerTest"
```

- [ ] **Step 4: Add the `hasCompletedV6Changeover` field** (needed by the test harness now; persistence wiring is Task 5)

In `NotificationState.kt`, after `hasCompletedV5Migration` (line 32), add:

```kotlin
    // V6 changeover: set once the legacy V5/V4 topic unsubscribes have all
    // succeeded. Until then every reconcile retries them first.
    val hasCompletedV6Changeover: Boolean = false,
```

- [ ] **Step 5: Implement the reconciler**

`V6SubscriptionReconciler.kt`:

```kotlin
package me.calebjones.spacelaunchnow.data.notifications.v6

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.util.logging.logger

data class V6ReconcileResult(
    val attempted: Int,
    val failed: Int,
    val skipped: Boolean = false,
) {
    val clean: Boolean get() = !skipped && failed == 0
}

/**
 * The dumb loop over the TopicSubscription table. All thinking lives in
 * V6Topics.requiredTopics; this class only writes `desired`, walks the pending
 * queries, and records FCM outcomes.
 *
 * One invariant: `confirmed` is written only in an FCM success callback. A
 * failure leaves the row disagreeing, so the next reconciliation retries it --
 * no retry queue, no backoff bookkeeping.
 *
 * Unsubscribes run first, always. The audience class is baked into every type
 * topic, so a class switch rewrites that whole dimension; if subscribes ran
 * first and the unsubscribes then failed, the device would hold two classes
 * and receive every launch twice until the next reconcile. Failing toward a
 * brief gap instead of lasting duplicates is the correct direction.
 */
class V6SubscriptionReconciler(
    private val store: TopicSubscriptionStore,
    private val messaging: TopicMessaging,
    private val platform: String?,                    // "android" | "ios" | null: no-op (desktop)
    private val envProvider: suspend () -> String,    // "prod" | "debug"
    private val stateProvider: suspend () -> NotificationState,
    private val markChangeoverComplete: suspend () -> Unit,
    private val nowMillis: () -> Long,
) {
    private val log = logger()
    private val mutex = Mutex()

    suspend fun reconcile(): V6ReconcileResult {
        val platform = platform ?: return V6ReconcileResult(0, 0, skipped = true)
        mutex.withLock {
            val state = stateProvider()
            if (!state.hasCompletedV6Changeover) runChangeover(platform)
            return reconcileLocked(state, platform)
        }
    }

    private suspend fun reconcileLocked(state: NotificationState, platform: String): V6ReconcileResult {
        val required = V6Topics.requiredTopics(state, envProvider(), platform)
        store.replaceDesired(required)

        var attempted = 0
        var failed = 0

        store.pendingUnsubscribes().forEach { topic ->
            attempted++
            messaging.unsubscribe(topic)
                .onSuccess { store.confirm(topic, confirmed = false, nowMillis = nowMillis()) }
                .onFailure { failed++; store.recordFailure(topic, it.message, nowMillis()) }
        }
        store.deleteSettled()

        store.pendingSubscribes().forEach { topic ->
            attempted++
            messaging.subscribe(topic)
                .onSuccess { store.confirm(topic, confirmed = true, nowMillis = nowMillis()) }
                .onFailure { failed++; store.recordFailure(topic, it.message, nowMillis()) }
        }

        if (failed > 0) log.w { "V6 reconcile: $failed of $attempted FCM operations failed; will retry next pass" }
        return V6ReconcileResult(attempted, failed)
    }

    /**
     * One-time V5/V4 changeover: unsubscribe the legacy topics so this device
     * stops receiving the V5 broadcast the server still dual-sends. Marked
     * complete only when every legacy unsubscribe succeeded -- a partial
     * changeover retries on the next reconcile (transient double delivery is
     * collapsed by apns-collapse-id / collapse_key).
     */
    private suspend fun runChangeover(platform: String) {
        val legacy = listOf("prod_v5_$platform", "debug_v5_$platform", "k_prod_v4", "k_debug_v4")
        // map-then-all: every topic must be attempted; .all{} would short-circuit.
        val outcomes = legacy.map { topic -> messaging.unsubscribe(topic).isSuccess }
        if (outcomes.all { it }) {
            markChangeoverComplete()
            log.i { "V6 changeover complete: legacy topics unsubscribed ($legacy)" }
        } else {
            log.w { "V6 changeover incomplete; will retry next reconcile" }
        }
    }

    /**
     * The only correct reset (spec: "resubscribe from scratch"): explicitly
     * unsubscribe every topic we hold a confirmed row for, then rebuild from
     * settings. Never clear the table without unsubscribing -- we can only
     * unsubscribe from topics we can name, so a wiped record makes stale
     * subscriptions permanently invisible.
     */
    suspend fun forceResubscribe(): V6ReconcileResult {
        val platform = platform ?: return V6ReconcileResult(0, 0, skipped = true)
        mutex.withLock {
            store.confirmedTopics().forEach { topic ->
                messaging.unsubscribe(topic)
                    .onSuccess { store.deleteRow(topic) }
                    .onFailure { store.recordFailure(topic, it.message, nowMillis()) }
            }
        }
        return reconcile()
    }
}
```

(`forceResubscribe` releases the mutex before calling `reconcile()` — kotlinx `Mutex` is not reentrant.)

- [ ] **Step 6: Run the reconciler tests — pass**

Same command as Step 3. Expected: PASS (8 tests). Note the tests exercise changeover implicitly off (`hasCompletedV6Changeover = true`); Task 5 tests the changeover itself.

- [ ] **Step 7: Commit**

```powershell
git add -A; git commit -m "feat(notifications): add the V6 reconciler with unsubscribe-first ordering and per-row FCM outcomes"
```

---

### Task 5: Changeover + forceResubscribe tests + changeover persistence

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/NotificationStateStorage.kt:22-35, 59-77, 91-118`
- Test: extend `composeApp/src/desktopTest/kotlin/me/calebjones/spacelaunchnow/data/notifications/v6/V6SubscriptionReconcilerTest.kt`

**Interfaces:**
- Consumes: Task 4's reconciler (changeover + forceResubscribe already implemented there).
- Produces: `hasCompletedV6Changeover` persisted through `NotificationStateStorage` (used by Task 6's repository wiring).

- [ ] **Step 1: Write the failing changeover/reset tests** (append to `V6SubscriptionReconcilerTest.kt`)

```kotlin
    @Test
    fun changeoverUnsubscribesLegacyTopicsBeforeAnythingElse() = runTest {
        val h = Harness(flexState.copy(hasCompletedV6Changeover = false))
        h.reconciler.reconcile()

        val legacy = listOf("unsub:prod_v5_ios", "unsub:debug_v5_ios", "unsub:k_prod_v4", "unsub:k_debug_v4")
        assertEquals(legacy, h.fake.calls.take(4))
        assertTrue(h.state.hasCompletedV6Changeover)
        // And the V6 subscribes still happened afterwards.
        assertEquals(3, h.store.confirmedTopics().size)
    }

    @Test
    fun failedChangeoverRetriesOnNextReconcile() = runTest {
        val h = Harness(flexState.copy(hasCompletedV6Changeover = false))
        h.fake.failUnsubscribes = setOf("prod_v5_ios")
        h.reconciler.reconcile()
        assertTrue(!h.state.hasCompletedV6Changeover)

        h.fake.failUnsubscribes = emptySet()
        h.fake.calls.clear()
        h.reconciler.reconcile()
        assertTrue(h.state.hasCompletedV6Changeover)
        assertTrue("unsub:prod_v5_ios" in h.fake.calls)
    }

    @Test
    fun changeoverRunsOnceAndNeverAgain() = runTest {
        val h = Harness(flexState.copy(hasCompletedV6Changeover = false))
        h.reconciler.reconcile()
        h.fake.calls.clear()
        h.reconciler.reconcile()
        assertTrue(h.fake.calls.none { it.contains("_v5_") || it.contains("k_prod_v4") || it.contains("k_debug_v4") })
    }

    @Test
    fun forceResubscribeUnsubscribesEveryConfirmedRowThenRebuilds() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()
        h.fake.calls.clear()

        val result = h.reconciler.forceResubscribe()
        val firstSub = h.fake.calls.indexOfFirst { it.startsWith("sub:") }
        val unsubCount = h.fake.calls.take(firstSub).count { it.startsWith("unsub:") }
        assertEquals(3, unsubCount)
        assertEquals(0, result.failed)
        assertEquals(3, h.store.confirmedTopics().size)

        // Running it twice converges to the same state.
        val again = h.reconciler.forceResubscribe()
        assertEquals(0, again.failed)
        assertEquals(3, h.store.confirmedTopics().size)
    }

    @Test
    fun forceResubscribeWithPartialFailureKeepsFailedRowsForRetry() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()

        h.fake.failUnsubscribes = setOf("v6_prod_spacex")
        h.reconciler.forceResubscribe()

        // The failed row survived with its error recorded; nothing was lost.
        val row = h.store.mismatchedRows().singleOrNull { it.topic == "v6_prod_spacex" }
            ?: h.store.confirmedTopics().let { confirmed ->
                assertTrue("v6_prod_spacex" in confirmed, "row must not vanish"); null
            }
        if (row != null) assertTrue(row.attempts >= 1)
    }
```

- [ ] **Step 2: Run — the changeover tests should PASS already** (Task 4 implemented the logic); any failure here is a bug in Task 4's implementation — fix it now, don't proceed.

```powershell
.\gradlew.bat :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.data.notifications.v6.V6SubscriptionReconcilerTest"
```

Expected: PASS (13 tests).

- [ ] **Step 3: Persist the flag**

In `NotificationStateStorage.kt`:

Add to the companion (after line 34):

```kotlin
        private val HAS_COMPLETED_V6_CHANGEOVER = booleanPreferencesKey("has_completed_v6_changeover")
```

In `stateFlow`'s `NotificationState(...)` construction (after the `subscribedTopics` line, 76):

```kotlin
            subscribedTopics = preferences[SUBSCRIBED_TOPICS] ?: default.subscribedTopics,
            hasCompletedV6Changeover = preferences[HAS_COMPLETED_V6_CHANGEOVER] ?: false
```

In `saveState` (after the `SUBSCRIBED_TOPICS` write, line 116):

```kotlin
                    preferences[HAS_COMPLETED_V6_CHANGEOVER] = state.hasCompletedV6Changeover
```

- [ ] **Step 4: Full desktop suite + Android compile**

```powershell
.\gradlew.bat :composeApp:desktopTest :composeApp:compileDebugKotlinAndroid
```

Expected: both green.

- [ ] **Step 5: Commit**

```powershell
git add -A; git commit -m "feat(notifications): persist the V6 changeover flag and pin changeover and reset behaviour"
```

---

### Task 6: Repository + DI wiring — V6 replaces the V5 trigger path

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/NotificationRepository.kt:30-34`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/NotificationRepositoryImpl.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt:136-148, 310-316`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PushDiagnostics.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt` (~line 265)

**Interfaces:**
- Consumes: `V6SubscriptionReconciler`, `TopicSubscriptionStore`, `PushTopicMessaging`, `V6ReconcileResult`.
- Produces (used by Tasks 7, 8, 9): `NotificationRepository.reconcileSubscriptions(): V6ReconcileResult` and `NotificationRepository.forceResubscribe(): V6ReconcileResult`; Koin single `TopicSubscriptionStore`; `PushDiagnostics.recordCleanReconcile(nowEpochSeconds)` + `PushDiagnosticsSnapshot.lastCleanReconcileEpochSeconds`.

- [ ] **Step 1: Extend the interface**

In `NotificationRepository.kt` after `getAvailableLocations()` (line 29), add:

```kotlin
    // V6 subscription reconciliation. Triggered by explicit Save, once per app
    // start (inside initialize()), on Android token refresh, and immediately on
    // the master kill switch.
    suspend fun reconcileSubscriptions(): V6ReconcileResult
    suspend fun forceResubscribe(): V6ReconcileResult
```

with import `me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult`.

- [ ] **Step 2: Rewire `NotificationRepositoryImpl`**

Constructor gains the store (new third parameter):

```kotlin
class NotificationRepositoryImpl(
    private val pushMessaging: PushMessaging,
    private val storage: NotificationStateStorage,
    private val topicSubscriptionStore: TopicSubscriptionStore,
    private val debugPreferences: DebugPreferences? = null
) : NotificationRepository {
```

Delete the `subscriptionProcessor` property (lines 42-53) and `updateSubscribedTopics` (lines 194-217); delete the `subscriptionProcessor.requestUpdate(...)` calls in `initialize()` (line 73) and `updateState` (lines 183-185). The `SubscriptionProcessor.kt` FILE stays (deletion is the follow-up plan) — only its wiring goes. Then add:

```kotlin
    private val reconciler = V6SubscriptionReconciler(
        store = topicSubscriptionStore,
        messaging = PushTopicMessaging(pushMessaging),
        platform = when (getPlatform().type) {
            PlatformType.ANDROID -> "android"
            PlatformType.IOS -> "ios"
            PlatformType.DESKTOP -> null   // desktop is a no-op, as today
        },
        envProvider = { if (useDebugTopics()) "debug" else "prod" },
        stateProvider = { storage.getState() },
        markChangeoverComplete = { updateState { it.copy(hasCompletedV6Changeover = true) } },
        nowMillis = { Clock.System.now().toEpochMilliseconds() },
    )

    // Same env decision the V5 path used: debug topics only in debug builds,
    // and only when the debug setting asks for them.
    private suspend fun useDebugTopics(): Boolean {
        if (!BuildConfig.IS_DEBUG || debugPreferences == null) return false
        return try {
            debugPreferences.getDebugSettings().useDebugTopics
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun reconcileSubscriptions(): V6ReconcileResult {
        val result = reconciler.reconcile()
        recordReconcileDiagnostics(result)
        return result
    }

    override suspend fun forceResubscribe(): V6ReconcileResult {
        val result = reconciler.forceResubscribe()
        recordReconcileDiagnostics(result)
        return result
    }

    private fun recordReconcileDiagnostics(result: V6ReconcileResult) {
        if (result.skipped) return
        PushDiagnostics.recordSubscribedTopicCount(topicSubscriptionStore.counts().confirmed.toInt())
        if (result.clean) PushDiagnostics.recordCleanReconcile()
    }
```

Imports to add: `me.calebjones.spacelaunchnow.PlatformType`, `me.calebjones.spacelaunchnow.getPlatform`, `me.calebjones.spacelaunchnow.util.BuildConfig`, `me.calebjones.spacelaunchnow.util.logging.PushDiagnostics`, `kotlinx.datetime.Clock`, and the four `data.notifications.v6` types.

In `initialize()`, replace the deleted `subscriptionProcessor.requestUpdate(persistedState)` line with:

```kotlin
            // App-start reconcile: the retry path for anything that failed at
            // save time, and the iOS token-refresh cover (iOS has no Kotlin-side
            // onNewToken hook).
            reconcileSubscriptions()
```

Replace `setNotificationsEnabled` (lines 83-87) with:

```kotlin
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        updateState { currentState ->
            currentState.copy(enableNotifications = enabled)
        }
        // The kill switch must not wait for Save or next app start: under V6
        // the unsubscribe IS the switch-off (and the resubscribe the switch-on).
        reconcileSubscriptions()
    }
```

- [ ] **Step 3: DI**

In `AppModule.kt`, after `single { StatsLocalDataSource(get(), get()) }` (line 148), add:

```kotlin
    single { TopicSubscriptionStore(get()) }
```

(import `me.calebjones.spacelaunchnow.data.notifications.v6.TopicSubscriptionStore`), and update the `NotificationRepository` single (lines 310-316):

```kotlin
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            pushMessaging = get(),
            storage = get<NotificationStateStorage>(),
            topicSubscriptionStore = get(),
            debugPreferences = getOrNull<DebugPreferences>()
        )
    }
```

- [ ] **Step 4: PushDiagnostics additions**

In `PushDiagnostics.kt`: add to `PushDiagnosticsSnapshot` a final field `val lastCleanReconcileEpochSeconds: Long? = null`; add a recorder alongside the others:

```kotlin
    fun recordCleanReconcile(nowEpochSeconds: Long = Clock.System.now().epochSeconds) {
        _snapshot.update { it.copy(lastCleanReconcileEpochSeconds = nowEpochSeconds) }
    }
```

(match the file's existing clock import/style — `recordTokenSuccess` already takes `nowEpochSeconds`), and in `reportRows(...)` append a row `"Last clean reconcile (V6)" to (snapshot.lastCleanReconcileEpochSeconds?.toString() ?: "never this session")`.

In `App.kt`, delete the line `PushDiagnostics.recordSubscribedTopicCount(currentState.subscribedTopics.size)` (~line 265) — the count is now recorded from the ledger by every reconcile, and `subscribedTopics` no longer updates.

- [ ] **Step 5: Run everything; fix pinned diagnostics tests**

```powershell
.\gradlew.bat :composeApp:desktopTest
```

`PushDiagnosticsTest` / `PushSummaryLoggingTest` may pin `reportRows` contents — if they fail, update their expected rows to include the new "Last clean reconcile (V6)" row. No other behaviour change is acceptable collateral; anything else failing means Step 2 broke something.

- [ ] **Step 6: Android compile**

```powershell
.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileDebugUnitTestKotlinAndroid
```

Expected: exit 0.

- [ ] **Step 7: Commit**

```powershell
git add -A; git commit -m "feat(notifications): wire the V6 reconciler through the repository and retire the V5 subscribe trigger"
```

---

### Task 7: Save-based settings screen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SettingsViewModel.kt` (after `clearSnackbarMessage`, ~line 250)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/NotificationSettingsScreen.kt` (end of the LazyColumn)

**Interfaces:**
- Consumes: `NotificationRepository.reconcileSubscriptions()` (Task 6), existing `_snackbarMessage` flow.
- Produces: `SettingsViewModel.saveNotificationSettings()`, `SettingsViewModel.isSavingNotifications: StateFlow<Boolean>`.

Toggles keep persisting immediately (that behaviour is unchanged and safe — persistence is local); what no longer happens per-toggle is FCM traffic, because Task 6 removed the debounce trigger. Save is the single reconcile point; the app-start reconcile self-heals an unsaved exit.

- [ ] **Step 1: ViewModel**

Add after `clearSnackbarMessage()`:

```kotlin
    // V6: FCM reconciliation is save-triggered. A class switch is ~20 FCM
    // operations, so per-toggle reconciling would fire a full rewrite on every
    // debounce expiry while a user explores; Save batches everything into one.
    private val _isSavingNotifications = MutableStateFlow(false)
    val isSavingNotifications: StateFlow<Boolean> = _isSavingNotifications.asStateFlow()

    fun saveNotificationSettings() {
        viewModelScope.launch {
            _isSavingNotifications.value = true
            try {
                val result = notificationRepository.reconcileSubscriptions()
                _snackbarMessage.value = when {
                    result.skipped -> "Push subscriptions are not available on this platform"
                    result.failed == 0 -> "Notification subscriptions updated"
                    else -> "Some subscriptions failed — they will retry at next app start"
                }
            } finally {
                _isSavingNotifications.value = false
            }
        }
    }
```

- [ ] **Step 2: Screen**

At the end of the `LazyColumn` content in `NotificationSettingsScreen.kt` (after the last existing `item`), add:

```kotlin
            item {
                val isSaving by viewModel.isSavingNotifications.collectAsStateWithLifecycle()
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::saveNotificationSettings,
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "Applying…" else "Save & apply")
                }
                Text(
                    text = "Changes take effect when you save. If you leave without saving, they are applied automatically the next time the app starts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
```

(`Button` import likely already present; add if not.)

- [ ] **Step 3: Compile + test**

```powershell
.\gradlew.bat :composeApp:desktopTest :composeApp:compileDebugKotlinAndroid
```

Expected: green. (UI change itself is device-verified later, per the spec's device matrix.)

- [ ] **Step 4: Commit**

```powershell
git add -A; git commit -m "feat(notifications): make the notification filters screen save-based with a single reconcile per save"
```

---

### Task 8: Token-refresh hook (Android) + kill the iOS k_debug_v4 auto-subscribe

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/services/SpaceLaunchFirebaseMessagingService.kt:61-67`
- Modify: `iosApp/iosApp/AppDelegate.swift:219-227` (delete only)

**Interfaces:**
- Consumes: `NotificationRepository.reconcileSubscriptions()` via Koin.
- Produces: nothing new.

- [ ] **Step 1: Android `onNewToken` reconciles — and never clears the table**

In `SpaceLaunchFirebaseMessagingService.kt`, add a service scope near the top of the class:

```kotlin
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
```

with `override fun onDestroy() { super.onDestroy(); serviceScope.cancel() }`, and replace `onNewToken`:

```kotlin
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        log.i { "New FCM token generated (len=${token.length}, …${token.takeLast(6)})" }
        PushDiagnostics.recordTokenSuccess(token)

        // Topic subscriptions bind to the Firebase installation, not the token
        // (firebase-android-sdk#5824), so a refresh changes nothing about what
        // this device receives. Reconcile and nothing more -- never clear the
        // subscription table here: we can only unsubscribe from topics we can
        // name, and wiping the record would orphan every stale subscription.
        serviceScope.launch {
            runCatching {
                KoinPlatform.getKoin().get<NotificationRepository>().reconcileSubscriptions()
            }.onFailure {
                log.w(it) { "Reconcile after token refresh failed; app-start pass will retry" }
            }
        }
    }
```

Imports: `kotlinx.coroutines.CoroutineScope`, `kotlinx.coroutines.Dispatchers`, `kotlinx.coroutines.SupervisorJob`, `kotlinx.coroutines.cancel`, `kotlinx.coroutines.launch`, `org.koin.mp.KoinPlatform`, `me.calebjones.spacelaunchnow.data.repository.NotificationRepository`.

- [ ] **Step 2: Delete the iOS auto-subscribe**

In `AppDelegate.swift`, inside `messaging(_:didReceiveRegistrationToken:)`, delete the entire block at lines 219-227 that prints `🔧 DEBUG: Auto-subscribing to k_debug_v4 topic...` and calls `Messaging.messaging().subscribe(toTopic: "k_debug_v4")`. It runs unconditionally (release builds included), re-subscribes a legacy topic the changeover just unsubscribed, and silently corrupts the ledger's model of ground truth. Nothing replaces it — iOS reconciliation happens at app start. **Swift cannot compile on this machine; this step is verified by careful re-read of the diff (it must be a pure deletion) and by CI.**

- [ ] **Step 3: Android compile**

```powershell
.\gradlew.bat :composeApp:compileDebugKotlinAndroid
```

Expected: exit 0.

- [ ] **Step 4: Commit**

```powershell
git add -A; git commit -m "feat(notifications): reconcile on Android token refresh and drop the iOS k_debug_v4 auto-subscribe"
```

---

### Task 9: Diagnostics — the subscription section and the reset action

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DiagnosticsScreen.kt` (injections ~line 139, new card after "Live notification filters" ~line 114, reset button + report lines ~line 154-176)

**Interfaces:**
- Consumes: `TopicSubscriptionStore` (koinInject), `NotificationRepository.forceResubscribe()`, `V6Topics`, `PushDiagnostics.snapshot.lastCleanReconcileEpochSeconds`, generated row type `me.calebjones.spacelaunchnow.database.TopicSubscription`.
- Produces: nothing consumed downstream.

- [ ] **Step 1: Inject and read the ledger**

Next to the existing `koinInject()` lines (~139):

```kotlin
    val topicStore: TopicSubscriptionStore = koinInject()
    val notificationRepository: NotificationRepository = koinInject()
    var subsRefresh by remember { mutableStateOf(0) }
    val subscriptionCounts by produceState<SubscriptionCounts?>(initialValue = null, subsRefresh) {
        value = withContext(Dispatchers.Default) { topicStore.counts() }
    }
    val mismatchedRows by produceState<List<TopicSubscription>?>(initialValue = null, subsRefresh) {
        value = withContext(Dispatchers.Default) { topicStore.mismatchedRows() }
    }
```

- [ ] **Step 2: The card** — insert a new `item { ... }` directly after the `"Live notification filters (in-app)"` card:

```kotlin
                item {
                    DiagnosticsCard("Push subscriptions (V6)") {
                        val s = liveState
                        val counts = subscriptionCounts
                        if (s == null || counts == null) {
                            DiagRow("State", "loading…")
                        } else {
                            val platformName = when (getPlatform().type) {
                                PlatformType.ANDROID -> "android"
                                PlatformType.IOS -> "ios"
                                PlatformType.DESKTOP -> null
                            }
                            DiagRow("Audience class", V6Topics.audienceClass(s))
                            // Set size is env/platform independent; "prod"/"android"
                            // stand in when the real values are irrelevant or absent.
                            DiagRow("Required topics", V6Topics.requiredTopics(s, "prod", platformName ?: "android").size.toString())
                            DiagRow("Confirmed", counts.confirmed.toString())
                            DiagRow("Pending subscribe", counts.pendingSubscribe.toString())
                            DiagRow("Pending unsubscribe", counts.pendingUnsubscribe.toString())
                            DiagRow(
                                "Last clean reconcile",
                                PushDiagnostics.snapshot.lastCleanReconcileEpochSeconds?.toString() ?: "never this session"
                            )
                            (mismatchedRows ?: emptyList()).forEach { row ->
                                Text(
                                    "${row.topic} desired=${row.desired} confirmed=${row.confirmed} attempts=${row.attempts} lastAttempt=${row.last_attempt ?: "-"} error=${row.last_error ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
```

- [ ] **Step 3: The reset action** — after the existing "Share diagnostics report" `OutlinedButton`, add:

```kotlin
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.Default) { notificationRepository.forceResubscribe() }
                            subsRefresh++
                        }
                    }
                ) { Text("Reset push subscriptions (resubscribe from scratch)") }
                Text(
                    // "Resubscribe from scratch", NOT "fix all subscriptions": it
                    // repairs only what the ledger knows about, and nothing on the
                    // FCM side can repair what it doesn't.
                    "Unsubscribes every push topic this install has a record of, then resubscribes from your settings. Notifications may pause briefly while it runs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
```

- [ ] **Step 4: The shareable report** — inside the report `buildString` (after the `Live locations:` line), add:

```kotlin
                            subscriptionCounts?.let { c ->
                                appendLine("V6 subs: confirmed=${c.confirmed} pendingSub=${c.pendingSubscribe} pendingUnsub=${c.pendingUnsubscribe} lastClean=${PushDiagnostics.snapshot.lastCleanReconcileEpochSeconds ?: "never"}")
                            }
                            (mismatchedRows ?: emptyList()).forEach { r ->
                                appendLine("V6 mismatch ${r.topic} desired=${r.desired} confirmed=${r.confirmed} attempts=${r.attempts} err=${r.last_error ?: "-"}")
                            }
```

- [ ] **Step 5: Compile + test**

```powershell
.\gradlew.bat :composeApp:desktopTest :composeApp:compileDebugKotlinAndroid
```

Expected: green.

- [ ] **Step 6: Commit**

```powershell
git add -A; git commit -m "feat(notifications): surface the V6 subscription ledger and a resubscribe-from-scratch action in diagnostics"
```

---

### Task 10: Backup exclusion

**Files:**
- Create: `composeApp/src/androidMain/res/xml/backup_rules.xml`
- Create: `composeApp/src/androidMain/res/xml/data_extraction_rules.xml`
- Modify: `composeApp/src/androidMain/AndroidManifest.xml:12-19`
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/database/DatabaseDriverFactory.ios.kt`

**Interfaces:** none.

Why: a backup restored onto a fresh install would carry `confirmed = 1` rows belonging to the *old* Firebase installation; reconciliation would trust them and never subscribe — silent under-delivery, the dangerous direction. SQLite backup granularity is per-file, and everything else in `spacelaunchnow.db` is TTL'd cache, so the whole file is excluded (recorded deviation #5).

- [ ] **Step 1: Android rules files**

`backup_rules.xml` (API ≤ 30, `fullBackupContent`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- spacelaunchnow.db is TTL'd cache plus the TopicSubscription ledger. The
     ledger records THIS installation's FCM subscriptions; restored onto a new
     install it would claim subscriptions the install does not have and the
     reconciler would never subscribe. Excluded so restores start empty and
     rebuild correctly. -->
<full-backup-content>
    <exclude domain="database" path="spacelaunchnow.db" />
    <exclude domain="database" path="spacelaunchnow.db-journal" />
    <exclude domain="database" path="spacelaunchnow.db-wal" />
    <exclude domain="database" path="spacelaunchnow.db-shm" />
</full-backup-content>
```

`data_extraction_rules.xml` (API 31+; device transfer has the same hazard):

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="database" path="spacelaunchnow.db" />
        <exclude domain="database" path="spacelaunchnow.db-journal" />
        <exclude domain="database" path="spacelaunchnow.db-wal" />
        <exclude domain="database" path="spacelaunchnow.db-shm" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="spacelaunchnow.db" />
        <exclude domain="database" path="spacelaunchnow.db-journal" />
        <exclude domain="database" path="spacelaunchnow.db-wal" />
        <exclude domain="database" path="spacelaunchnow.db-shm" />
    </device-transfer>
</data-extraction-rules>
```

- [ ] **Step 2: Manifest attributes** — in the `<application ...>` tag, after `android:allowBackup="true"`, add:

```xml
        android:fullBackupContent="@xml/backup_rules"
        android:dataExtractionRules="@xml/data_extraction_rules"
```

- [ ] **Step 3: iOS iCloud exclusion**

Replace `DatabaseDriverFactory.ios.kt` with:

```kotlin
package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(
            schema = SpaceLaunchDatabase.Schema,
            name = "spacelaunchnow.db"
        )
        excludeDatabaseFromBackup()
        return driver
    }

    // The TopicSubscription ledger records THIS installation's FCM
    // subscriptions; restored from iCloud onto a new install it would claim
    // subscriptions the install does not have, and reconciliation would never
    // subscribe -- silent under-delivery. The rest of the DB is TTL'd cache.
    // Runs on every launch: the attribute does not always survive restores.
    private fun excludeDatabaseFromBackup() {
        val path = DatabaseFileContext.databasePath("spacelaunchnow.db", null)
        NSURL.fileURLWithPath(path)
            .setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    }
}
```

**iOS Kotlin does not compile on this machine.** If CI reports `DatabaseFileContext.databasePath` does not exist or has a different signature (it comes from `co.touchlab.sqliter`, the library under the SQLDelight native driver), the fix is to resolve the same default path SQLiter uses and mark that URL — check the SQLiter version in `gradle/libs.versions.toml` and its `DatabaseFileContext` API. The Diagnostics reset action (Task 9) is the spec's sanctioned fallback if exclusion proves impractical; do NOT ship silently without one of the two.

- [ ] **Step 4: Android build check** (manifest + resources are validated by the Android build)

```powershell
.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:processDebugMainManifest
```

Expected: exit 0. (If `processDebugMainManifest` is not a task name in this AGP version, `assembleDebug`'s manifest step will validate — run `.\gradlew.bat :composeApp:packageDebugResources` instead; any task that runs resource merging suffices.)

- [ ] **Step 5: Commit**

```powershell
git add -A; git commit -m "feat(notifications): exclude the subscription ledger database from platform backup on both platforms"
```

---

### Task 11: Final verification + docs

**Files:**
- Modify: `docs/superpowers/specs/2026-08-16-v6-notification-subscriptions-kmp-design.md:3` (status line)

- [ ] **Step 1: Full test suite**

```powershell
$env:JAVA_HOME = "D:\tools\Android Studio\jbr"
.\gradlew.bat :composeApp:desktopTest
```

Expected: 0 failures (including the ~28 new V6 tests and the 10 contract tests).

- [ ] **Step 2: Android compile gates**

```powershell
.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileDebugUnitTestKotlinAndroid
```

Expected: exit 0 for both.

- [ ] **Step 3: Verify no path re-subscribes to V5** — the changeover's guarantee dies if anything still calls the old path:

```powershell
Select-String -Path "composeApp\src\commonMain\kotlin\me\calebjones\spacelaunchnow\data\repository\NotificationRepositoryImpl.kt" -Pattern "subscriptionProcessor|SubscriptionProcessor"
```

Expected: zero matches. Also confirm `AppDelegate.swift` no longer contains `k_debug_v4` (`Select-String -Path "iosApp\iosApp\AppDelegate.swift" -Pattern "k_debug_v4"` → zero matches).

- [ ] **Step 4: Update the spec status line**

Change line 3 of the spec from `**Status:** design, not yet implemented` to `**Status:** implemented on feat/v6-notification-subscriptions — device-matrix verification pending; V5 path removal is a follow-up gated on that matrix`.

- [ ] **Step 5: Commit**

```powershell
git add -A; git commit -m "docs(notifications): mark the V6 subscription spec implemented pending device verification"
```

---

## Spec coverage self-check (planner's map, for the reviewer)

| Spec section | Task |
|---|---|
| Derivation pure function, class table, empty-set kill switch, follow-all no attributes, desktop no-op | 2 (desktop no-op enforced in 4) |
| `TopicSubscription` table, two-boolean state machine, queries | 3 |
| Reconciliation loop, unsubscribe-first, confirmed-only-on-success | 4 |
| Save + app-start triggers | 6, 7 |
| One-time changeover (V5/V4 unsubscribes, `hasCompletedV6Changeover`) | 4, 5 |
| Table is source of truth; `onNewToken` reconciles, never clears | 4 (test), 8 (wiring) |
| `forceResubscribe` reset | 4, 5 (tests), 9 (UI) |
| Restore-from-backup mitigation | 10 |
| No preference migration | (nothing to do — storage stays ID-keyed; derivation bridges at subscribe time, Task 2) |
| Names from the contract, broadcast translation | 2 (+ existing V6TopicContractTest) |
| Diagnostics section + reset action | 9 |
| "Other Agencies" row | 1 |
| Spec tests 1–3 (derivation) | 2 |
| Spec tests 4–11 (reconciliation) | 4, 5 |
| Build fresh in `notifications/v6`, old paths disconnected not deleted | 6 (+ Out-of-scope list) |
| Device testing, NSE/worker filter removal, V5 deletion | follow-up plan (out of scope, gated on device matrix) |
