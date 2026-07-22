# FCM Startup Diagnostics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the startup FCM/push-registration sequence observable in Datadog at the default diagnostic level, and surface FCM/push health rows in the shareable diagnostics report.

**Architecture:** A new commonMain runtime snapshot (`PushDiagnostics`) is written by the startup push sequence (token fetch, forward-to-RC, permission check, Play Services check) and read synchronously by both the structured `Push registration summary` log line and the `platformNotificationDiagnostics()` report providers. A new `expect/actual` provides the Play Services check. Existing debug-level milestone logs get promoted to info/warn.

**Tech Stack:** Kotlin Multiplatform, Kermit (SpaceLogger), Datadog (`DatadogLogger` expect/actual), kotlinx-datetime, Koin, kotlin.test via `:composeApp:jvmTest`.

## Global Constraints

- **Never log or store the raw FCM token** — only `takeLast(6)` suffix and length, everywhere including test fixtures' assertions.
- Conventional Commits are mandatory (`feat(...)`, `fix(...)`); DO NOT add Claude as co-author.
- `.env` must exist at repo root before any Gradle build; generated API clients must exist (`./gradlew generateAllApiClients` after clean checkout — check for `composeApp/src/openApiLL/` first).
- JDK 21 toolchain; run tests with `./gradlew :composeApp:jvmTest` (commonTest runs on JVM).
- iOS Kotlin cannot be compiled on this Windows host — keep iosMain edits minimal and mirror existing syntax exactly; CI/Xcode verifies.
- Key decision (spec constraint): at `STANDARD` the Datadog writer threshold is `Warn` (`DiagnosticLevel.policy()`), so Kermit `log.i` does NOT upload at STANDARD. The summary therefore ALSO goes directly through `DatadogLogger.info(msg, attrs)` — gated so it fires only when the writer would drop Info (avoids duplicates at VERBOSE); Datadog consent still blocks everything at `OFF`. Do not change `DiagnosticLevel.policy()`.
- Resolved spec open questions: summary logs **once per cold start** (end of the App.kt push-init block); Play Services `missing` logs at **warn** (non-GMS devices are legitimate); the "raise to VERBOSE" nudge is **out of scope**.

---

### Task 1: `PushDiagnostics` snapshot + summary/report builders (commonMain, TDD)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PushDiagnostics.kt`
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.kt` (enum only in this task; `expect fun` comes in Task 2)
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/PushDiagnosticsTest.kt`

**Interfaces:**
- Produces: `enum PlayServicesAvailability { AVAILABLE, MISSING, UPDATE_REQUIRED, NOT_APPLICABLE, UNKNOWN }`
- Produces: `data class PushDiagnosticsSnapshot` and `object PushDiagnostics` with:
  - `val snapshot: PushDiagnosticsSnapshot`
  - `fun recordTokenSuccess(token: String, nowEpochSeconds: Long = Clock.System.now().epochSeconds)`
  - `fun recordTokenUnavailable(reason: String)`
  - `fun recordForwardedToRc(nowEpochSeconds: Long = Clock.System.now().epochSeconds)`
  - `fun recordForwardSkipped(reason: String)`
  - `fun recordNotificationsEnabled(enabled: Boolean)`
  - `fun recordPlayServices(state: PlayServicesAvailability)`
  - `fun recordSubscribedTopicCount(count: Int)`
  - `fun summaryAttributes(snapshot: PushDiagnosticsSnapshot, diagnosticLevelName: String?): Map<String, Any?>`
  - `fun reportRows(snapshot: PushDiagnosticsSnapshot, notificationsEnabled: Boolean?, playServices: PlayServicesAvailability?): List<Pair<String, String>>`
  - `fun logSummary(diagnosticLevelName: String?)`
  - `fun reset()` (test hook)

- [x] **Step 1: Write the failing test**

`composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/PushDiagnosticsTest.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PushDiagnosticsTest {

    private val rawToken = "fVeryLongRawFcmTokenValue_abcdef123456:APA91-xyzSUFFIX"

    @BeforeTest
    fun setUp() = PushDiagnostics.reset()

    @AfterTest
    fun tearDown() = PushDiagnostics.reset()

    @Test
    fun tokenSuccessStoresOnlySuffixAndLength() {
        PushDiagnostics.recordTokenSuccess(rawToken, nowEpochSeconds = 1_752_000_000)
        val snap = PushDiagnostics.snapshot
        assertEquals(true, snap.tokenPresent)
        assertEquals("SUFFIX", snap.tokenSuffix)
        assertEquals(rawToken.length, snap.tokenLength)
        assertEquals(1_752_000_000, snap.tokenRefreshedAtEpochSeconds)
    }

    @Test
    fun rawTokenNeverAppearsInSummaryAttributesOrReportRows() {
        PushDiagnostics.recordTokenSuccess(rawToken)
        val attrs = PushDiagnostics.summaryAttributes(PushDiagnostics.snapshot, "STANDARD")
        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, true, PlayServicesAvailability.AVAILABLE)
        assertFalse(attrs.values.any { it.toString().contains(rawToken) })
        assertFalse(rows.any { it.second.contains(rawToken) })
    }

    @Test
    fun summaryAttributesHealthyPath() {
        PushDiagnostics.recordTokenSuccess(rawToken)
        PushDiagnostics.recordForwardedToRc(nowEpochSeconds = 1_752_000_100)
        PushDiagnostics.recordNotificationsEnabled(true)
        PushDiagnostics.recordPlayServices(PlayServicesAvailability.AVAILABLE)
        PushDiagnostics.recordSubscribedTopicCount(2)

        val attrs = PushDiagnostics.summaryAttributes(PushDiagnostics.snapshot, "STANDARD")
        assertEquals(true, attrs["push.token_present"])
        assertEquals("SUFFIX", attrs["push.token_suffix"])
        assertEquals(rawToken.length, attrs["push.token_length"])
        assertEquals(true, attrs["push.forwarded_to_rc"])
        assertEquals(true, attrs["push.notifications_enabled"])
        assertEquals("available", attrs["push.play_services"])
        assertEquals(2, attrs["push.subscribed_topic_count"])
        assertEquals("STANDARD", attrs["diagnostics.level"])
    }

    @Test
    fun summaryAttributesBrokenPath() {
        PushDiagnostics.recordTokenUnavailable("null_or_blank")
        PushDiagnostics.recordForwardSkipped("token_unavailable")
        PushDiagnostics.recordNotificationsEnabled(false)
        PushDiagnostics.recordPlayServices(PlayServicesAvailability.MISSING)

        val attrs = PushDiagnostics.summaryAttributes(PushDiagnostics.snapshot, null)
        assertEquals(false, attrs["push.token_present"])
        assertEquals(false, attrs["push.forwarded_to_rc"])
        assertEquals("token_unavailable", attrs["push.forward_skip_reason"])
        assertEquals(false, attrs["push.notifications_enabled"])
        assertEquals("missing", attrs["push.play_services"])
        assertEquals(0, attrs["push.subscribed_topic_count"])
        assertEquals("unknown", attrs["diagnostics.level"])
    }

    @Test
    fun reportRowsSeededSnapshot() {
        PushDiagnostics.recordTokenSuccess(rawToken, nowEpochSeconds = 1_752_000_000)
        PushDiagnostics.recordForwardedToRc(nowEpochSeconds = 1_752_000_100)
        PushDiagnostics.recordSubscribedTopicCount(2)

        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, true, PlayServicesAvailability.AVAILABLE)
        val byLabel = rows.toMap()
        assertEquals("true", byLabel["Notifications enabled"])
        assertEquals("available", byLabel["Play Services"])
        assertEquals("present …SUFFIX (len ${rawToken.length})", byLabel["FCM token"])
        assertTrue(byLabel.getValue("FCM token refreshed").startsWith("2025") || byLabel.getValue("FCM token refreshed").startsWith("2026"))
        assertTrue(byLabel.getValue("Forwarded to RevenueCat").startsWith("yes @"))
        assertEquals("2", byLabel["Subscribed topics"])
    }

    @Test
    fun reportRowsUnavailableTokenAndSkip() {
        PushDiagnostics.recordTokenUnavailable("null_or_blank")
        PushDiagnostics.recordForwardSkipped("token_unavailable")

        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, false, PlayServicesAvailability.NOT_APPLICABLE)
        val byLabel = rows.toMap()
        assertEquals("false", byLabel["Notifications enabled"])
        assertEquals("n/a", byLabel["Play Services"])
        assertEquals("unavailable", byLabel["FCM token"])
        assertEquals("never", byLabel["FCM token refreshed"])
        assertEquals("skipped: token_unavailable", byLabel["Forwarded to RevenueCat"])
        assertEquals("unknown", byLabel["Subscribed topics"])
    }

    @Test
    fun reportRowsDefaultsWhenNothingRecorded() {
        val rows = PushDiagnostics.reportRows(PushDiagnostics.snapshot, null, null)
        val byLabel = rows.toMap()
        assertEquals("unknown", byLabel["Notifications enabled"])
        assertEquals("unknown", byLabel["Play Services"])
        assertEquals("unavailable", byLabel["FCM token"])
        assertEquals("no", byLabel["Forwarded to RevenueCat"])
    }
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.util.logging.PushDiagnosticsTest"`
Expected: FAIL to compile — `PushDiagnostics` / `PlayServicesAvailability` unresolved.

- [x] **Step 3: Write the implementation**

`composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

/** Google Play Services availability as seen at startup. NOT_APPLICABLE on iOS/Desktop. */
enum class PlayServicesAvailability { AVAILABLE, MISSING, UPDATE_REQUIRED, NOT_APPLICABLE, UNKNOWN }
```

`composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PushDiagnostics.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.analytics.DatadogLogger

/**
 * Runtime cache of the startup push-registration outcomes (spec 015 §3.4).
 * Written by the startup sequence; read synchronously by the push-registration
 * summary line and by platformNotificationDiagnostics() at report share-time.
 * Never holds the raw token — suffix/length only.
 */
data class PushDiagnosticsSnapshot(
    val tokenPresent: Boolean? = null,
    val tokenSuffix: String? = null,
    val tokenLength: Int? = null,
    val tokenRefreshedAtEpochSeconds: Long? = null,
    val tokenUnavailableReason: String? = null,
    val forwardedToRc: Boolean? = null,
    val forwardedAtEpochSeconds: Long? = null,
    val forwardSkipReason: String? = null,
    val notificationsEnabled: Boolean? = null,
    val playServices: PlayServicesAvailability? = null,
    val subscribedTopicCount: Int? = null,
)

object PushDiagnostics {

    private val log by lazy { SpaceLogger.getLogger("PushDiagnostics") }
    private val _snapshot = MutableStateFlow(PushDiagnosticsSnapshot())

    val snapshot: PushDiagnosticsSnapshot get() = _snapshot.value

    fun recordTokenSuccess(token: String, nowEpochSeconds: Long = Clock.System.now().epochSeconds) {
        _snapshot.update {
            it.copy(
                tokenPresent = true,
                tokenSuffix = token.takeLast(6),
                tokenLength = token.length,
                tokenRefreshedAtEpochSeconds = nowEpochSeconds,
                tokenUnavailableReason = null,
            )
        }
    }

    fun recordTokenUnavailable(reason: String) {
        _snapshot.update {
            it.copy(tokenPresent = false, tokenSuffix = null, tokenLength = null, tokenUnavailableReason = reason)
        }
    }

    fun recordForwardedToRc(nowEpochSeconds: Long = Clock.System.now().epochSeconds) {
        _snapshot.update {
            it.copy(forwardedToRc = true, forwardedAtEpochSeconds = nowEpochSeconds, forwardSkipReason = null)
        }
    }

    fun recordForwardSkipped(reason: String) {
        _snapshot.update { it.copy(forwardedToRc = false, forwardSkipReason = reason) }
    }

    fun recordNotificationsEnabled(enabled: Boolean) {
        _snapshot.update { it.copy(notificationsEnabled = enabled) }
    }

    fun recordPlayServices(state: PlayServicesAvailability) {
        _snapshot.update { it.copy(playServices = state) }
    }

    fun recordSubscribedTopicCount(count: Int) {
        _snapshot.update { it.copy(subscribedTopicCount = count) }
    }

    fun reset() {
        _snapshot.value = PushDiagnosticsSnapshot()
    }

    /** Attributes for the structured "Push registration summary" line (spec 015 §3.2). */
    fun summaryAttributes(snapshot: PushDiagnosticsSnapshot, diagnosticLevelName: String?): Map<String, Any?> = buildMap {
        put("push.token_present", snapshot.tokenPresent ?: false)
        put("push.token_suffix", snapshot.tokenSuffix ?: "")
        put("push.token_length", snapshot.tokenLength ?: 0)
        put("push.forwarded_to_rc", snapshot.forwardedToRc ?: false)
        snapshot.forwardSkipReason?.let { put("push.forward_skip_reason", it) }
        put("push.notifications_enabled", snapshot.notificationsEnabled ?: "unknown")
        put("push.play_services", (snapshot.playServices ?: PlayServicesAvailability.UNKNOWN).label())
        put("push.subscribed_topic_count", snapshot.subscribedTopicCount ?: 0)
        put("diagnostics.level", diagnosticLevelName ?: "unknown")
    }

    /** Label/value rows for the shareable diagnostics report (spec 015 §4). */
    fun reportRows(
        snapshot: PushDiagnosticsSnapshot,
        notificationsEnabled: Boolean?,
        playServices: PlayServicesAvailability?,
    ): List<Pair<String, String>> = buildList {
        add("Notifications enabled" to (notificationsEnabled?.toString() ?: "unknown"))
        add("Play Services" to (playServices ?: PlayServicesAvailability.UNKNOWN).label())
        add(
            "FCM token" to if (snapshot.tokenPresent == true) {
                "present …${snapshot.tokenSuffix} (len ${snapshot.tokenLength})"
            } else {
                "unavailable"
            }
        )
        add(
            "FCM token refreshed" to (
                snapshot.tokenRefreshedAtEpochSeconds
                    ?.let { Instant.fromEpochSeconds(it).toString() } ?: "never"
                )
        )
        add(
            "Forwarded to RevenueCat" to when {
                snapshot.forwardedToRc == true ->
                    "yes @${snapshot.forwardedAtEpochSeconds?.let { Instant.fromEpochSeconds(it).toString() } ?: "?"}"
                snapshot.forwardSkipReason != null -> "skipped: ${snapshot.forwardSkipReason}"
                else -> "no"
            }
        )
        add("Subscribed topics" to (snapshot.subscribedTopicCount?.toString() ?: "unknown"))
    }

    /**
     * Emit the once-per-cold-start summary. The Kermit line covers console/file (and
     * Datadog at VERBOSE). At STANDARD the DataDogLogWriter threshold is Warn and would
     * drop Info, so the structured copy goes directly to DatadogLogger — consent still
     * blocks it entirely at OFF. The severity gate prevents double upload at VERBOSE.
     */
    fun logSummary(diagnosticLevelName: String?) {
        val attrs = summaryAttributes(snapshot, diagnosticLevelName)
        log.i { "Push registration summary " + attrs.entries.joinToString(" ") { "${it.key}=${it.value}" } }
        if (SpaceLogger.getDataDogSeverity() > Severity.Info) {
            DatadogLogger.info(
                "Push registration summary",
                attrs + mapOf("tag" to "SLN-PushDiagnostics") + UserContext.getLogAttributes(),
            )
        }
    }
}

private fun PlayServicesAvailability.label(): String = when (this) {
    PlayServicesAvailability.AVAILABLE -> "available"
    PlayServicesAvailability.MISSING -> "missing"
    PlayServicesAvailability.UPDATE_REQUIRED -> "update_required"
    PlayServicesAvailability.NOT_APPLICABLE -> "n/a"
    PlayServicesAvailability.UNKNOWN -> "unknown"
}
```

- [x] **Step 4: Run test to verify it passes**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.util.logging.PushDiagnosticsTest"`
Expected: PASS (7 tests).

- [x] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PushDiagnostics.kt \
        composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.kt \
        composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/PushDiagnosticsTest.kt
git commit -m "feat(logging): add PushDiagnostics runtime snapshot with summary and report builders"
```

---

### Task 2: Play Services availability `expect/actual`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.kt` (add `expect fun`)
- Create: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.android.kt`
- Create: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.ios.kt`
- Create: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.desktop.kt`

**Interfaces:**
- Consumes: `PlayServicesAvailability` (Task 1), `PermissionHelper.context` (existing, `me.calebjones.spacelaunchnow.data.repository.PermissionHelper`).
- Produces: `expect fun checkPlayServicesAvailability(): PlayServicesAvailability` — called by Task 3 (App.kt) and Task 4 (Android report provider).

- [x] **Step 1: Add the expect declaration**

Append to `PlayServices.kt` (commonMain):

```kotlin
/** Synchronous Play Services check. Never throws; NOT_APPLICABLE on iOS/Desktop. */
expect fun checkPlayServicesAvailability(): PlayServicesAvailability
```

- [x] **Step 2: Android actual**

`composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.android.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import me.calebjones.spacelaunchnow.data.repository.PermissionHelper

actual fun checkPlayServicesAvailability(): PlayServicesAvailability = try {
    when (GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(PermissionHelper.context)) {
        ConnectionResult.SUCCESS -> PlayServicesAvailability.AVAILABLE
        ConnectionResult.SERVICE_MISSING,
        ConnectionResult.SERVICE_DISABLED,
        ConnectionResult.SERVICE_INVALID -> PlayServicesAvailability.MISSING
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
        ConnectionResult.SERVICE_UPDATING -> PlayServicesAvailability.UPDATE_REQUIRED
        else -> PlayServicesAvailability.UNKNOWN
    }
} catch (e: Exception) {
    PlayServicesAvailability.UNKNOWN
}
```

`GoogleApiAvailabilityLight` and `ConnectionResult` live in `play-services-basement`, already on the classpath transitively via `firebase-messaging`. **Fallback if unresolved at compile:** add to `gradle/libs.versions.toml` under `[libraries]`: `play-services-base = { module = "com.google.android.gms:play-services-base", version = "18.5.0" }`, add `implementation(libs.play.services.base)` to the `androidMain` dependencies block in `composeApp/build.gradle.kts`, and use `GoogleApiAvailability` instead of the Light variant.

- [x] **Step 3: iOS and Desktop actuals**

`composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.ios.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

actual fun checkPlayServicesAvailability(): PlayServicesAvailability = PlayServicesAvailability.NOT_APPLICABLE
```

`composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.desktop.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

actual fun checkPlayServicesAvailability(): PlayServicesAvailability = PlayServicesAvailability.NOT_APPLICABLE
```

- [x] **Step 4: Compile check**

Run: `./gradlew :composeApp:compileKotlinDesktop :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

- [x] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.kt \
        composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.android.kt \
        composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.ios.kt \
        composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/logging/PlayServices.desktop.kt
git commit -m "feat(logging): add Play Services availability expect/actual"
```

---

### Task 3: Promote startup push logs to info/warn and record the snapshot

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt` (~lines 209-235, inside the startup `LaunchedEffect`)
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt` (~lines 205-220, Step 4c)
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/AndroidPushMessaging.kt` (`getToken`)
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosPushMessaging.kt` (`getToken`)
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/services/SpaceLaunchFirebaseMessagingService.kt` (`onNewToken`, ~line 60-65)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/DefaultRevenueCatAttributes.kt` (`setPushToken`, ~line 26)

**Interfaces:**
- Consumes: `PushDiagnostics.record*` / `logSummary` (Task 1), `checkPlayServicesAvailability()` (Task 2), existing `NotificationRepository.hasNotificationPermission()`, `LoggingPreferences.getDiagnosticSettings(): Flow<DiagnosticSettings>` (`.level: DiagnosticLevel`).
- Produces: nothing new for later tasks (Task 4 reads only the snapshot).

- [x] **Step 1: `AndroidPushMessaging.getToken()` — success → info, redact, record**

Replace the `getToken` body (`AndroidPushMessaging.kt:42-52`):

```kotlin
    actual suspend fun getToken(): Result<String> {
        log.d { "Getting FCM token" }
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            PushDiagnostics.recordTokenSuccess(token)
            log.i { "SUCCESS - Got FCM token (len=${token.length}, …${token.takeLast(6)})" }
            Result.success(token)
        } catch (e: Exception) {
            PushDiagnostics.recordTokenUnavailable(e.message ?: "exception")
            log.e { "ERROR - Failed to get FCM token: ${e.message}" }
            Result.failure(e)
        }
    }
```

Add import: `me.calebjones.spacelaunchnow.util.logging.PushDiagnostics`.
(Note: this also fixes the existing raw `token.take(20)` prefix leak.)

- [x] **Step 2: `IosPushMessaging.getToken()` — redact + record (levels already info/error)**

Replace the result callback body (`IosPushMessaging.kt:50-57`):

```kotlin
        IosPushMessagingBridge.requestToken { result ->
            result.onSuccess { token ->
                PushDiagnostics.recordTokenSuccess(token)
                log.i { "SUCCESS - Got FCM token (len=${token.length}, …${token.takeLast(6)})" }
            }.onFailure { error ->
                PushDiagnostics.recordTokenUnavailable(error.message ?: "exception")
                log.e { "ERROR - Failed to get FCM token: ${error.message}" }
            }
            continuation.resume(result)
        }
```

Add import: `me.calebjones.spacelaunchnow.util.logging.PushDiagnostics`.

- [x] **Step 3: `SpaceLaunchFirebaseMessagingService.onNewToken` — redact + record refresh**

Replace (`SpaceLaunchFirebaseMessagingService.kt:60-65`):

```kotlin
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        log.i { "New FCM token generated (len=${token.length}, …${token.takeLast(6)})" }
        PushDiagnostics.recordTokenSuccess(token)

        // TODO: Send token to backend if needed
    }
```

Add import: `me.calebjones.spacelaunchnow.util.logging.PushDiagnostics`.
(Fixes the existing raw `token.take(10)` prefix leak.)

- [x] **Step 4: `App.kt` — token fetch block + precondition checks + summary**

Replace the token-fetch `try` block (`App.kt:209-215`) with:

```kotlin
                try {
                    // Startup FCM token check (last 6 chars only — token is sensitive)
                    val tokenResult = pushMessaging.getToken()
                    val token = tokenResult.getOrNull()
                    if (token.isNullOrBlank()) {
                        val reason = tokenResult.exceptionOrNull()?.message ?: "null_or_blank"
                        PushDiagnostics.recordTokenUnavailable(reason)
                        log.w { "FCM token unavailable at startup: $reason" }
                    } else {
                        log.i { "FCM token present (len=${token.length}, …${token.takeLast(6)})" }
                    }
                } catch (e: Exception) {
                    log.w(e) { "Failed to get FCM token" }
                }
```

Then, inside the notifications `try` block, immediately after `notificationRepository.initialize()` and the existing state logs (i.e. after `App.kt:232` `log.i { "Settings loaded - state management handled by repository" }`), insert:

```kotlin
                    // Push-registration preconditions (spec 015 §3.3) + summary (§3.2)
                    val notificationsEnabled = notificationRepository.hasNotificationPermission()
                    PushDiagnostics.recordNotificationsEnabled(notificationsEnabled)
                    if (notificationsEnabled) {
                        log.i { "OS notifications enabled" }
                    } else {
                        log.w { "OS notifications disabled (permission not granted)" }
                    }

                    val playServices = checkPlayServicesAvailability()
                    PushDiagnostics.recordPlayServices(playServices)
                    when (playServices) {
                        PlayServicesAvailability.MISSING,
                        PlayServicesAvailability.UPDATE_REQUIRED,
                        PlayServicesAvailability.UNKNOWN ->
                            log.w { "Google Play Services not available: $playServices" }
                        else -> log.i { "Google Play Services: $playServices" }
                    }

                    PushDiagnostics.recordSubscribedTopicCount(currentState.subscribedTopics.size)

                    val diagnosticLevelName = runCatching {
                        koin.get<LoggingPreferences>().getDiagnosticSettings().first().level.name
                    }.getOrNull()
                    PushDiagnostics.logSummary(diagnosticLevelName)
```

Add imports to `App.kt`:

```kotlin
import kotlinx.coroutines.flow.first
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.util.logging.PlayServicesAvailability
import me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
import me.calebjones.spacelaunchnow.util.logging.checkPlayServicesAvailability
```

(Check for existing imports first — `kotlinx.coroutines.flow.first` may already be present. If `LoggingPreferences.getDiagnosticSettings()` has a different accessor name, mirror whatever `DiagnosticsScreen.kt:61` uses.)

- [x] **Step 5: `MainApplication.kt` Step 4c — promote to info/warn + record**

Replace (`MainApplication.kt:205-220`):

```kotlin
                // Step 4c: Forward FCM token to RevenueCat for re-engagement campaigns.
                try {
                    val pushMessaging =
                        getKoin().get<me.calebjones.spacelaunchnow.data.notifications.PushMessaging>()
                    val fcmToken = pushMessaging.getToken().getOrNull()
                    if (!fcmToken.isNullOrBlank()) {
                        val rcAttrs =
                            getKoin().get<me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributes>()
                        rcAttrs.setPushToken(fcmToken)
                        me.calebjones.spacelaunchnow.util.logging.PushDiagnostics.recordForwardedToRc()
                        log.i { "✅ FCM token forwarded to RevenueCat" }
                    } else {
                        me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
                            .recordForwardSkipped("token_unavailable")
                        log.w { "FCM token not available yet; skipping RC push token set" }
                    }
                } catch (e: Exception) {
                    me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
                        .recordForwardSkipped("exception: ${e.message}")
                    log.w(e) { "Failed to forward FCM token to RevenueCat" }
                }
```

(Fully-qualified names match the file's existing style — see Steps 4b/5/6 around it.)

- [x] **Step 6: `DefaultRevenueCatAttributes.setPushToken` — success debug → info**

In `DefaultRevenueCatAttributes.kt:26`, change:

```kotlin
            log.d { "RC push token set (length=${token.length})" }
```

to:

```kotlin
            log.i { "RC push token set (length=${token.length})" }
```

- [x] **Step 7: Compile + full test check**

Run: `./gradlew :composeApp:compileKotlinDesktop :composeApp:compileDebugKotlinAndroid :composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass. (iOS edits verified by CI/Xcode — not compilable on this host.)

- [x] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt \
        composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt \
        composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/AndroidPushMessaging.kt \
        composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosPushMessaging.kt \
        composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/services/SpaceLaunchFirebaseMessagingService.kt \
        composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/DefaultRevenueCatAttributes.kt
git commit -m "feat(notifications): make startup push registration observable at info/warn"
```

---

### Task 4: FCM/push health rows in the diagnostics report

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.android.kt`
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.ios.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DiagnosticsScreen.kt` (card title only, line 118)

**Interfaces:**
- Consumes: `PushDiagnostics.reportRows(snapshot, notificationsEnabled, playServices)` (Task 1), `checkPlayServicesAvailability()` (Task 2), `AndroidNotificationPermissionHandler`, `PermissionHelper.context`.
- The report builder (`DiagnosticsScreen.kt:167` `platformRows.forEach { (l, v) -> appendLine("$l: $v") }`) is **not** modified — rows appear automatically.

- [x] **Step 1: Android provider**

Replace `DiagnosticsProviders.android.kt` entirely:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.AndroidNotificationPermissionHandler
import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb
import me.calebjones.spacelaunchnow.data.repository.PermissionHelper

actual fun platformNotificationDiagnostics(): List<Pair<String, String>> {
    val notificationsEnabled = runCatching {
        AndroidNotificationPermissionHandler(PermissionHelper.context).hasNotificationPermission()
    }.getOrNull()
    val playServices = runCatching { checkPlayServicesAvailability() }
        .getOrDefault(PlayServicesAvailability.UNKNOWN)
    return PushDiagnostics.reportRows(PushDiagnostics.snapshot, notificationsEnabled, playServices)
}

actual fun recentNseBreadcrumbs(): List<NseBreadcrumb> = emptyList()
actual fun shareCopiesToClipboard(): Boolean = false
```

- [x] **Step 2: iOS provider — append push rows, keep NSE rows**

In `DiagnosticsProviders.ios.kt`, change the `platformNotificationDiagnostics` return from `return listOf(...)` to `return listOf(...) + pushRows` by replacing the function with:

```kotlin
actual fun platformNotificationDiagnostics(): List<Pair<String, String>> {
    val snap = NSEPreferenceBridge.readStoredPrefs()
    fun present(v: Any?): String = v?.toString() ?: "MISSING"
    val pushSnap = PushDiagnostics.snapshot
    return listOf(
        "App Group available" to snap.appGroupAvailable.toString(),
        "Any key missing" to snap.anyKeyMissing.toString(),
        "NSE enableNotifications" to present(snap.enableNotifications),
        "NSE followAllLaunches" to present(snap.followAllLaunches),
        "NSE useStrictMatching" to present(snap.useStrictMatching),
        "NSE agencies (expanded)" to (snap.subscribedAgencies?.let { "${it.size}: ${it.take(12).joinToString(",")}" } ?: "MISSING"),
        "NSE locations (expanded)" to (snap.subscribedLocations?.let { "${it.size}: ${it.take(12).joinToString(",")}" } ?: "MISSING"),
    ) + PushDiagnostics.reportRows(pushSnap, pushSnap.notificationsEnabled, PlayServicesAvailability.NOT_APPLICABLE)
}
```

(iOS reads notifications-enabled from the snapshot — recorded by the common startup sequence in Task 3; there is no synchronous share-time permission API on iOS.)

- [x] **Step 3: Fix the debug-screen card title**

`platformRows` now render on Android too, inside a card titled for iOS. In `DiagnosticsScreen.kt:118`, change:

```kotlin
                    DiagnosticsCard("NSE App Group (used when app is killed)") {
```

to:

```kotlin
                    DiagnosticsCard("Push & notification state") {
```

(The shareable report text builder is untouched, per spec §4.)

- [x] **Step 4: Compile + test**

Run: `./gradlew :composeApp:compileKotlinDesktop :composeApp:compileDebugKotlinAndroid :composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, tests pass.

- [x] **Step 5: Commit**

```bash
git add composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.android.kt \
        composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.ios.kt \
        composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DiagnosticsScreen.kt
git commit -m "feat(logging): add FCM/push health rows to diagnostics report"
```

---

### Task 5: Level-policy regression test + final verification

**Files:**
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/PushSummaryLoggingTest.kt` (new)

**Interfaces:**
- Consumes: `SpaceLogger.initialize(LogConfig)`, `PushDiagnostics.logSummary`, Kermit `LogWriter`.

- [x] **Step 1: Write the test (summary logs at Info, message carries attrs, no raw token)**

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class CapturingWriter : LogWriter() {
    val entries = mutableListOf<Pair<Severity, String>>()
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        entries += severity to message
    }
}

class PushSummaryLoggingTest {

    private val writer = CapturingWriter()
    private val rawToken = "fVeryLongRawFcmTokenValue_abcdef123456:APA91-xyzSUFFIX"

    @BeforeTest
    fun setUp() {
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
    }

    @AfterTest
    fun tearDown() {
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, emptyList()))
    }

    @Test
    fun summaryLogsAtInfoWithAttributesInMessage() {
        PushDiagnostics.recordTokenSuccess(rawToken)
        PushDiagnostics.recordNotificationsEnabled(true)
        PushDiagnostics.recordPlayServices(PlayServicesAvailability.AVAILABLE)

        PushDiagnostics.logSummary("STANDARD")

        val summary = writer.entries.single { it.second.startsWith("Push registration summary") }
        assertEquals(Severity.Info, summary.first)
        assertTrue(summary.second.contains("push.token_present=true"))
        assertTrue(summary.second.contains("push.play_services=available"))
        assertTrue(summary.second.contains("diagnostics.level=STANDARD"))
        assertFalse(summary.second.contains(rawToken))
    }
}
```

Note: `PushDiagnostics.log` is `lazy` — if a previous test initialized SpaceLogger first, the cached logger still fans out to writers registered at *its* creation. If the capture misses, restructure `PushDiagnostics` to use `private val log get() = SpaceLogger.getLogger("PushDiagnostics")` (computed property, no caching) rather than `by lazy` — that is the acceptable fix, not weakening the test.

- [x] **Step 2: Run the new test**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.util.logging.PushSummaryLoggingTest"`
Expected: PASS.

- [x] **Step 3: Full verification**

Run: `./gradlew :composeApp:jvmTest ktlintCheck :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinDesktop`
Expected: jvmTest + compiles green. ktlint is soft-fail in CI; fix any violations in files this plan touched.

- [x] **Step 4: Commit**

```bash
git add composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/PushSummaryLoggingTest.kt
git commit -m "test(logging): assert push summary logs at info with redacted attributes"
```

---

## Manual verification (post-merge, from spec §6 — not automatable here)

- Device with notifications denied and/or no Play Services → startup summary shows it in Datadog at `STANDARD`; copied diagnostics report flags `Notifications enabled: false` / `Play Services: missing`.
- Reproduce the silent-token case → `FCM token unavailable at startup` appears as `warn` in Datadog; report shows `FCM token: unavailable`.
- Desktop run (`./gradlew desktopRun`) → no crash; report has no platform rows (desktop provider unchanged).
- iOS → report still includes NSE rows, now followed by push rows with `Play Services: n/a`.
