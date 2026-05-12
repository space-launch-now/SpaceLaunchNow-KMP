# Premium-Funnel Observability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Instrument the rewarded-ad → 24h-temporary-premium flow with `AnalyticsManager` events and attach RevenueCat subscriber attributes to every user.

**Architecture:** Event analytics flow through the existing `AnalyticsManager` interface (already wired to Datadog + Firebase). RevenueCat attributes are pushed by a single coroutine-scoped `RevenueCatAttributesSyncer` class that snapshots app state at startup and observes flows for deltas. The RC KMP SDK (`com.revenuecat.purchases.kmp`) exposes `Purchases.sharedInstance.setAttributes(map)` and `setPushToken(token)` from common code, so no expect/actual is needed for the SDK call itself — only for the platform-specific values being read.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin, DataStore, `com.revenuecat.purchases.kmp` SDK, AnalyticsManager interface (Datadog + Firebase).

**Spec reference:** `docs/superpowers/specs/2026-05-06-premium-funnel-observability-design.md`

---

## File Structure

### Phase A — Event Analytics (ships independently)

| File | Responsibility |
|---|---|
| `composeApp/src/commonMain/.../analytics/events/AnalyticsEvent.kt` | (Modify) Add 4 event classes for the rewarded-ad / temp-access flow. |
| `composeApp/src/commonMain/.../ui/subscription/TemporaryPremiumCard.kt` | (Modify) Accept `source: String`, inject `AnalyticsManager`, fire 4 events at the appropriate callbacks. |
| `composeApp/src/commonMain/.../ui/settings/ThemeCustomizationScreen.kt` | (Modify) Pass `source = "theme_settings"` to the card. |

### Phase B — Counter Storage

| File | Responsibility |
|---|---|
| `composeApp/src/commonMain/.../data/storage/TemporaryPremiumAccess.kt` | (Modify) Add 2 counter keys + `incrementGrantsTotal()` / `incrementAdsShownTotal()` + reader Flows. |
| `composeApp/src/commonTest/.../data/storage/TemporaryPremiumAccessCounterTest.kt` | (Create) Unit test counter increments using in-memory DataStore. |
| `composeApp/src/commonMain/.../ui/subscription/TemporaryPremiumCard.kt` | (Modify) Call `incrementAdsShownTotal()` in `onAdShown` and rely on per-feature `grantTemporaryAccess` for grant counter (added inside that fn — see Phase B step 4). |

### Phase C — Platform Info Reader

| File | Responsibility |
|---|---|
| `composeApp/src/commonMain/.../platform/AppEnvironmentInfo.kt` | (Create) `expect class AppEnvironmentInfo` with `appVersionName`, `appBuildNumber`, `osVersion`, `deviceModel`, `locale`, `country`, `formFactor`. |
| `composeApp/src/androidMain/.../platform/AppEnvironmentInfo.android.kt` | (Create) Android `actual` reading `Build.*`, `Locale.getDefault()`, `BuildConfig.VERSION_NAME/CODE`. |
| `composeApp/src/iosMain/.../platform/AppEnvironmentInfo.ios.kt` | (Create) iOS `actual` reading `UIDevice.currentDevice`, `NSLocale`, `NSBundle.mainBundle`. |
| `composeApp/src/desktopMain/.../platform/AppEnvironmentInfo.desktop.kt` | (Create) Desktop `actual` returning JVM `System.getProperty(...)` values. |

### Phase D — RC Attributes Wrapper + Syncer

| File | Responsibility |
|---|---|
| `composeApp/src/commonMain/.../data/billing/RevenueCatAttributes.kt` | (Create) Thin interface around `Purchases.sharedInstance.setAttributes` / `setPushToken`. Mockable for tests. |
| `composeApp/src/commonMain/.../data/billing/DefaultRevenueCatAttributes.kt` | (Create) Production impl using `com.revenuecat.purchases.kmp.Purchases.sharedInstance`. |
| `composeApp/src/commonMain/.../data/billing/RevenueCatAttributesSyncer.kt` | (Create) Owns "what attrs RC knows". `start(scope)` pushes startup snapshot, then collects flows for deltas with debounce. |
| `composeApp/src/commonTest/.../data/billing/RevenueCatAttributesSyncerTest.kt` | (Create) Unit test snapshot map and flow-driven deltas via fake `RevenueCatAttributes`. |
| `composeApp/src/commonMain/.../di/AppModule.kt` | (Modify) Register `RevenueCatAttributes` and `RevenueCatAttributesSyncer` as singletons. |
| `composeApp/src/androidMain/.../MainApplication.kt` | (Modify) Start syncer after `BillingManager.initialize()`. |
| `composeApp/src/iosMain/.../KoinInitializer.kt` | (Modify) Add a `startRevenueCatAttributesSyncer()` helper invoked by Swift after Koin starts. |

### Phase E — Push Token Bridge

| File | Responsibility |
|---|---|
| `composeApp/src/androidMain/.../MainApplication.kt` | (Modify) After billing init, call `PushMessaging.getToken()` and forward to `RevenueCatAttributes.setPushToken(...)`. |
| `iosApp/iosApp/AppDelegate.swift` | (Modify) In `didRegisterForRemoteNotificationsWithDeviceToken`, call `Purchases.shared().attribution.setPushToken(deviceToken)` directly (RC iOS SDK API, simpler than bridging Data → ByteArray through KMP). |

---

## Phase A — Event Analytics

Ships independently. Phase B and onward layer on top.

### Task A1: Add 4 events to `AnalyticsEvent.kt`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt`

- [ ] **Step 1: Open the file and find the last existing event section.**

The file ends with `ScreenViewed` at the bottom of the sealed class (around line 227). Add the new section **before** the closing `}` of `sealed class AnalyticsEvent`.

- [ ] **Step 2: Add the 4 events.**

Insert this block after `ScreenViewed` and before the closing brace of `sealed class AnalyticsEvent`:

```kotlin
    // ── Rewarded Ad / Temp Access Events ─────────────────────────────────────

    data class RewardedAdRequested(
        val source: String,
        val isExtension: Boolean
    ) : AnalyticsEvent("rewarded_ad_requested") {
        override fun toParameters() = mapOf(
            "source" to source,
            "is_extension" to isExtension
        )
    }

    data class RewardedAdShown(val source: String) :
        AnalyticsEvent("rewarded_ad_shown") {
        override fun toParameters() = mapOf("source" to source)
    }

    data class RewardedAdFailed(val source: String, val error: String) :
        AnalyticsEvent("rewarded_ad_failed") {
        override fun toParameters() = mapOf(
            "source" to source,
            "error" to error
        )
    }

    data class TemporaryAccessGranted(
        val feature: String,
        val source: String,
        val isExtension: Boolean
    ) : AnalyticsEvent("temporary_access_granted") {
        override fun toParameters() = mapOf(
            "feature" to feature,
            "source" to source,
            "is_extension" to isExtension
        )
    }
```

- [ ] **Step 3: Verify it compiles.**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit.**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt
git commit -m "feat(analytics): add rewarded-ad / temp-access events"
```

---

### Task A2: Add `source` param + analytics calls to `TemporaryPremiumCard`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/TemporaryPremiumCard.kt`

- [ ] **Step 1: Add the new param and Koin import.**

Add to the import block:

```kotlin
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import org.koin.compose.koinInject
```

Change the `fun TemporaryPremiumCard(...)` signature to add `source: String` as the second parameter (after `temporaryPremiumAccess`):

```kotlin
@Composable
fun TemporaryPremiumCard(
    temporaryPremiumAccess: TemporaryPremiumAccess,
    source: String,
    features: List<PremiumFeature> = listOf(
        PremiumFeature.CUSTOM_THEMES,
        PremiumFeature.ADVANCED_WIDGETS,
        PremiumFeature.WIDGETS_CUSTOMIZATION
    ),
    title: String = "24h Premium Access",
    description: String = "Watch an ad to unlock all premium features for 24 hours",
    icon: ImageVector,
    hasPermanentPremium: Boolean = false,
    modifier: Modifier = Modifier
) {
```

- [ ] **Step 2: Inject `AnalyticsManager` and snapshot `wasExtensionAtClick`.**

Inside the composable, just below `val log = SpaceLogger.getLogger("TemporaryPremiumCard")`, add:

```kotlin
val analyticsManager = koinInject<AnalyticsManager>()
var wasExtensionAtClick by remember { mutableStateOf(false) }
```

`remember`/`mutableStateOf` are already imported.

- [ ] **Step 3: Wrap both button onClick handlers to fire `RewardedAdRequested`.**

Locate the two buttons that set `showRewardedAd = true` (one inside `if (hasAnyAccess) { ... }` for the "Extend All Access" `OutlinedButton`, one in `else { ... }` for the "Watch Ad..." `Button`).

Replace the **OutlinedButton** onClick:

```kotlin
                OutlinedButton(
                    onClick = {
                        wasExtensionAtClick = true
                        analyticsManager.track(
                            AnalyticsEvent.RewardedAdRequested(
                                source = source,
                                isExtension = true
                            )
                        )
                        showRewardedAd = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
```

Replace the **Button** onClick:

```kotlin
                Button(
                    onClick = {
                        wasExtensionAtClick = false
                        analyticsManager.track(
                            AnalyticsEvent.RewardedAdRequested(
                                source = source,
                                isExtension = false
                            )
                        )
                        showRewardedAd = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
```

- [ ] **Step 4: Fire `RewardedAdShown` and `RewardedAdFailed` from the handler callbacks.**

Locate the `if (showRewardedAd) { RewardedAdHandler(...) }` block at the bottom of the composable. Replace the `onAdShown` and `onAdFailed` lambdas:

```kotlin
            onAdShown = {
                analyticsManager.track(AnalyticsEvent.RewardedAdShown(source = source))
                log.i { "✅ Rewarded ad shown for temporary premium access" }
            },
            onAdFailed = { error ->
                analyticsManager.track(
                    AnalyticsEvent.RewardedAdFailed(source = source, error = error)
                )
                log.e { "❌ Failed to show rewarded ad: $error" }
                showRewardedAd = false
            }
```

- [ ] **Step 5: Fire `TemporaryAccessGranted` per feature inside `onRewardEarned`.**

Replace the `onRewardEarned` lambda body:

```kotlin
            onRewardEarned = { rewardAmount, rewardType ->
                coroutineScope.launch {
                    val isExtensionSnapshot = wasExtensionAtClick
                    features.forEach { feature ->
                        temporaryPremiumAccess.grantTemporaryAccess(feature)
                        analyticsManager.track(
                            AnalyticsEvent.TemporaryAccessGranted(
                                feature = feature.name,
                                source = source,
                                isExtension = isExtensionSnapshot
                            )
                        )
                    }
                    val accessMap = features.associateWith { feature ->
                        temporaryPremiumAccess.getTemporaryAccessInfo(feature)
                    }
                    temporaryAccess = accessMap
                }
                showRewardedAd = false
            },
```

- [ ] **Step 6: Verify it compiles.**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Update the only call site to pass `source`.**

Edit `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/ThemeCustomizationScreen.kt`. Locate the existing `TemporaryPremiumCard(...)` call (around line 180). Add `source = "theme_settings"` immediately after `temporaryPremiumAccess`:

```kotlin
                TemporaryPremiumCard(
                    temporaryPremiumAccess = viewModel.temporaryPremiumAccess,
                    source = "theme_settings",
                    features = listOf(
                        PremiumFeature.CUSTOM_THEMES,
                        PremiumFeature.ADVANCED_WIDGETS,
                        PremiumFeature.WIDGETS_CUSTOMIZATION
                    ),
                    title = "24h Premium Access",
                    description = "Watch an ad to unlock premium theme features for 24 hours",
                    icon = Icons.Default.LockClock,
                    hasPermanentPremium = hasPermanentPremium
                )
```

- [ ] **Step 8: Verify the whole module compiles.**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit.**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/TemporaryPremiumCard.kt
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/ThemeCustomizationScreen.kt
git commit -m "feat(analytics): instrument rewarded-ad / temp-access flow"
```

---

### Task A3: Manual smoke test on Android

- [ ] **Step 1: Install debug build on a device or emulator.**

Run: `./gradlew installDebug`

- [ ] **Step 2: Trigger the flow.**

Open the app, navigate to **Settings → Theme Settings**. Scroll to the "24h Premium Access" card. Tap **Watch Ad for 24h Premium Access**.

- [ ] **Step 3: Verify the events fire.**

In Android Studio Logcat, filter on `Datadog` or `AnalyticsManager`. Expect to see, in order:

1. `rewarded_ad_requested` with `source=theme_settings`, `is_extension=false`
2. `rewarded_ad_shown` with `source=theme_settings`
3. `temporary_access_granted` × 3 (one per `PremiumFeature` in the default list)

If the ad fails to load (no fill), expect `rewarded_ad_failed` with the error string instead of steps 2 and 3.

- [ ] **Step 4: Trigger again to verify `is_extension=true`.**

Without restarting the app, tap **Extend All Access (+24h)**. Expect a new `rewarded_ad_requested` with `is_extension=true`, then the same downstream events.

- [ ] **Step 5: No commit (manual verification only).**

---

## Phase B — Counter Storage

### Task B1: Write counter test (TDD red)

**Files:**
- Create: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/storage/TemporaryPremiumAccessCounterTest.kt`

- [ ] **Step 1: Check if a similar test exists for reference.**

Run: `find composeApp/src/commonTest -name "*TemporaryPremium*"`
Expected: empty result; this is the first test for this file.

- [ ] **Step 2: Write the failing test.**

Create the file with:

```kotlin
package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TemporaryPremiumAccessCounterTest {

    private fun fakeDataStore(): DataStore<Preferences> {
        val state = MutableStateFlow(emptyPreferences())
        return object : DataStore<Preferences> {
            override val data = state
            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences
            ): Preferences {
                val updated = transform(state.value)
                state.value = updated
                return updated
            }
        }
    }

    @Test
    fun `incrementGrantsTotal increments by 1 each call`() = runTest {
        val ds = fakeDataStore()
        val sut = TemporaryPremiumAccess(ds)

        assertEquals(0L, sut.grantsTotalFlow.first())
        sut.incrementGrantsTotal()
        sut.incrementGrantsTotal()
        sut.incrementGrantsTotal()
        assertEquals(3L, sut.grantsTotalFlow.first())
    }

    @Test
    fun `incrementAdsShownTotal increments by 1 each call`() = runTest {
        val ds = fakeDataStore()
        val sut = TemporaryPremiumAccess(ds)

        assertEquals(0L, sut.adsShownTotalFlow.first())
        sut.incrementAdsShownTotal()
        assertEquals(1L, sut.adsShownTotalFlow.first())
    }
}
```

- [ ] **Step 3: Run the test to confirm it fails.**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccessCounterTest"`
Expected: COMPILATION FAILURE — `grantsTotalFlow`, `adsShownTotalFlow`, `incrementGrantsTotal`, `incrementAdsShownTotal` do not exist yet.

---

### Task B2: Implement counters in `TemporaryPremiumAccess`

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/TemporaryPremiumAccess.kt`

- [ ] **Step 1: Add counter keys to the companion object.**

After the existing `TEMP_*_EXPIRES_AT` keys (around line 33), add:

```kotlin
        private val TEMP_GRANTS_TOTAL = longPreferencesKey("temp_grants_total")
        private val REWARDED_ADS_SHOWN_TOTAL = longPreferencesKey("rewarded_ads_shown_total")
```

- [ ] **Step 2: Add increment functions and reader flows.**

After `clearAllTemporaryAccess()` (around line 255), before the closing brace of the class, add:

```kotlin
    val grantsTotalFlow: kotlinx.coroutines.flow.Flow<Long> = dataStore.data.map { preferences ->
        preferences[TEMP_GRANTS_TOTAL] ?: 0L
    }

    val adsShownTotalFlow: kotlinx.coroutines.flow.Flow<Long> = dataStore.data.map { preferences ->
        preferences[REWARDED_ADS_SHOWN_TOTAL] ?: 0L
    }

    suspend fun incrementGrantsTotal() {
        dataStore.edit { preferences ->
            preferences[TEMP_GRANTS_TOTAL] = (preferences[TEMP_GRANTS_TOTAL] ?: 0L) + 1L
        }
    }

    suspend fun incrementAdsShownTotal() {
        dataStore.edit { preferences ->
            preferences[REWARDED_ADS_SHOWN_TOTAL] =
                (preferences[REWARDED_ADS_SHOWN_TOTAL] ?: 0L) + 1L
        }
    }
```

- [ ] **Step 3: Run the test to confirm it passes.**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccessCounterTest"`
Expected: 2 tests passed.

- [ ] **Step 4: Wire the counter calls into `grantTemporaryAccess`.**

Inside `grantTemporaryAccess(feature)` (around line 63), at the very end of the function (after `notifyAccessChanged()` and the verification log), add:

```kotlin
        incrementGrantsTotal()
```

- [ ] **Step 5: Wire `incrementAdsShownTotal()` from the card.**

Edit `TemporaryPremiumCard.kt`. In the `onAdShown` lambda (modified in Task A2 step 4), add the increment call:

```kotlin
            onAdShown = {
                analyticsManager.track(AnalyticsEvent.RewardedAdShown(source = source))
                coroutineScope.launch {
                    temporaryPremiumAccess.incrementAdsShownTotal()
                }
                log.i { "✅ Rewarded ad shown for temporary premium access" }
            },
```

- [ ] **Step 6: Verify all tests still pass and the module compiles.**

Run: `./gradlew :composeApp:jvmTest :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit.**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/TemporaryPremiumAccess.kt
git add composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/storage/TemporaryPremiumAccessCounterTest.kt
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/TemporaryPremiumCard.kt
git commit -m "feat(billing): add temp-access + ad-shown counters in DataStore"
```

---

## Phase C — Platform Info Reader

### Task C1: Create `expect class AppEnvironmentInfo` in commonMain

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.kt`

- [ ] **Step 1: Write the expect class.**

```kotlin
package me.calebjones.spacelaunchnow.platform

/**
 * Read-only snapshot of device + app environment used for analytics and
 * RevenueCat subscriber attributes. Values are stable for the process lifetime.
 */
expect class AppEnvironmentInfo() {
    val appVersionName: String
    val appBuildNumber: String
    val osVersion: String
    val deviceModel: String
    val locale: String
    val country: String
    /** "phone" / "tablet" / "desktop" */
    val formFactor: String
}
```

- [ ] **Step 2: Verify it fails on every platform until actuals exist.**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: COMPILATION FAILURE — `Expected class AppEnvironmentInfo has no actual declaration`.

(This confirms the expect class is wired into the build; we will add actuals next.)

---

### Task C2: Implement `AppEnvironmentInfo` actuals on all three platforms

**Files:**
- Create: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.android.kt`
- Create: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.ios.kt`
- Create: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.desktop.kt`

- [ ] **Step 1: Android actual.**

```kotlin
package me.calebjones.spacelaunchnow.platform

import android.content.res.Configuration
import android.os.Build
import me.calebjones.spacelaunchnow.BuildConfig
import me.calebjones.spacelaunchnow.MainApplication
import java.util.Locale

actual class AppEnvironmentInfo actual constructor() {
    actual val appVersionName: String = BuildConfig.VERSION_NAME
    actual val appBuildNumber: String = BuildConfig.VERSION_CODE.toString()
    actual val osVersion: String = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    actual val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    actual val locale: String = Locale.getDefault().toLanguageTag()
    actual val country: String = Locale.getDefault().country
    actual val formFactor: String = run {
        val ctx = MainApplication.instance
        val isTablet = ctx?.resources?.configuration
            ?.smallestScreenWidthDp?.let { it >= 600 } ?: false
        if (isTablet) "tablet" else "phone"
    }
}
```

- [ ] **Step 2: iOS actual.**

```kotlin
package me.calebjones.spacelaunchnow.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad

actual class AppEnvironmentInfo actual constructor() {
    actual val appVersionName: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            ?: "unknown"
    actual val appBuildNumber: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
            ?: "unknown"
    actual val osVersion: String =
        "iOS ${UIDevice.currentDevice.systemVersion}"
    actual val deviceModel: String = UIDevice.currentDevice.model
    actual val locale: String =
        (NSLocale.currentLocale.localeIdentifier).replace('_', '-')
    actual val country: String = NSLocale.currentLocale.countryCode ?: ""
    actual val formFactor: String =
        if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) "tablet" else "phone"
}
```

- [ ] **Step 3: Desktop actual.**

```kotlin
package me.calebjones.spacelaunchnow.platform

import java.util.Locale

actual class AppEnvironmentInfo actual constructor() {
    actual val appVersionName: String = System.getProperty("app.version") ?: "desktop-dev"
    actual val appBuildNumber: String = System.getProperty("app.build") ?: "0"
    actual val osVersion: String =
        "${System.getProperty("os.name")} ${System.getProperty("os.version")}"
    actual val deviceModel: String = System.getProperty("os.arch") ?: "unknown"
    actual val locale: String = Locale.getDefault().toLanguageTag()
    actual val country: String = Locale.getDefault().country
    actual val formFactor: String = "desktop"
}
```

- [ ] **Step 4: Verify all three platforms compile.**

Run: `./gradlew :composeApp:compileKotlinDesktop :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL on both.
(iOS compile is verified during Xcode build; we run desktop + android here for fast feedback.)

- [ ] **Step 5: Commit.**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.kt
git add composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.android.kt
git add composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.ios.kt
git add composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/platform/AppEnvironmentInfo.desktop.kt
git commit -m "feat(platform): add AppEnvironmentInfo for analytics/RC attrs"
```

---

## Phase D — RC Attributes Wrapper + Syncer

### Task D1: Create `RevenueCatAttributes` interface + production impl

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatAttributes.kt`
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/DefaultRevenueCatAttributes.kt`

- [ ] **Step 1: Write the interface.**

```kotlin
package me.calebjones.spacelaunchnow.data.billing

/**
 * Thin wrapper over the RevenueCat KMP SDK's attribute APIs.
 * Mockable in tests; production implementation calls Purchases.sharedInstance.
 */
interface RevenueCatAttributes {

    /**
     * Push the given attribute map to RevenueCat. Null values clear an attribute.
     * Safe to call repeatedly; SDK coalesces writes.
     */
    fun set(attributes: Map<String, String?>)

    /**
     * Set the FCM (Android) or APNS (iOS) push token via the reserved attribute.
     * No-op on platforms without push.
     */
    fun setPushToken(token: String)
}
```

- [ ] **Step 2: Write the production impl.**

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.Purchases
import me.calebjones.spacelaunchnow.util.logging.logger

class DefaultRevenueCatAttributes : RevenueCatAttributes {

    private val log = logger()

    override fun set(attributes: Map<String, String?>) {
        try {
            Purchases.sharedInstance.setAttributes(attributes)
            log.d { "RC attributes pushed: ${attributes.keys}" }
        } catch (e: Throwable) {
            log.w(e) { "Failed to push RC attributes" }
        }
    }

    override fun setPushToken(token: String) {
        try {
            Purchases.sharedInstance.setPushToken(token)
            log.d { "RC push token set (length=${token.length})" }
        } catch (e: Throwable) {
            log.w(e) { "Failed to set RC push token" }
        }
    }
}
```

- [ ] **Step 3: Verify compile.**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL.

If `setAttributes` or `setPushToken` is not in the KMP SDK's public API surface (older SDK versions exposed `attribution.setAttributes(...)` instead of a top-level method), update the calls to:
```kotlin
Purchases.sharedInstance.attribution.setAttributes(attributes)
Purchases.sharedInstance.attribution.setPushToken(token)
```
and re-run the compile. Pick whichever overload the SDK in `gradle/libs.versions.toml` exposes.

- [ ] **Step 4: Commit.**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatAttributes.kt
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/DefaultRevenueCatAttributes.kt
git commit -m "feat(billing): add RevenueCatAttributes wrapper"
```

---

### Task D2: Write syncer test (TDD red)

**Files:**
- Create: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatAttributesSyncerTest.kt`

- [ ] **Step 1: Write the test using a fake `RevenueCatAttributes`.**

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.platform.AppEnvironmentInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RevenueCatAttributesSyncerTest {

    private class FakeAttributes : RevenueCatAttributes {
        val calls = mutableListOf<Map<String, String?>>()
        var lastPushToken: String? = null
        override fun set(attributes: Map<String, String?>) {
            calls += attributes
        }
        override fun setPushToken(token: String) {
            lastPushToken = token
        }
    }

    @Test
    fun `pushSnapshot includes app and device attributes`() = runTest {
        val fake = FakeAttributes()
        val syncer = RevenueCatAttributesSyncer(
            attributes = fake,
            envInfo = AppEnvironmentInfo(),
            subscriptionStateProvider = { "free" },
            themeModeProvider = { "system" },
            hasCustomThemeProvider = { false },
            grantsTotalProvider = { 0L },
            adsShownTotalProvider = { 0L },
            tempAccessActiveProvider = { false }
        )

        syncer.pushSnapshot()

        assertEquals(1, fake.calls.size)
        val snapshot = fake.calls.single()
        assertTrue(snapshot.containsKey("app_version"))
        assertTrue(snapshot.containsKey("platform"))
        assertTrue(snapshot.containsKey("os_version"))
        assertTrue(snapshot.containsKey("subscription_state"))
        assertEquals("free", snapshot["subscription_state"])
        assertEquals("0", snapshot["temporary_access_grants_total"])
    }

    @Test
    fun `pushSnapshot reflects updated providers on each call`() = runTest {
        val fake = FakeAttributes()
        var subState = "free"
        val syncer = RevenueCatAttributesSyncer(
            attributes = fake,
            envInfo = AppEnvironmentInfo(),
            subscriptionStateProvider = { subState },
            themeModeProvider = { "system" },
            hasCustomThemeProvider = { false },
            grantsTotalProvider = { 0L },
            adsShownTotalProvider = { 0L },
            tempAccessActiveProvider = { false }
        )

        syncer.pushSnapshot()
        subState = "premium"
        syncer.pushSnapshot()

        assertEquals(2, fake.calls.size)
        assertEquals("free", fake.calls[0]["subscription_state"])
        assertEquals("premium", fake.calls[1]["subscription_state"])
    }
}
```

- [ ] **Step 2: Run the test to confirm it fails (no syncer class yet).**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncerTest"`
Expected: COMPILATION FAILURE — `RevenueCatAttributesSyncer` does not exist.

---

### Task D3: Implement `RevenueCatAttributesSyncer`

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatAttributesSyncer.kt`

- [ ] **Step 1: Write the syncer.**

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.calebjones.spacelaunchnow.platform.AppEnvironmentInfo
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Builds and pushes the RevenueCat subscriber-attribute map.
 *
 * - `pushSnapshot()` reads providers synchronously and pushes one map.
 *   Used at app cold-start.
 * - `start(scope, ...)` collects flows and pushes deltas with a 1s debounce.
 *
 * Provider lambdas decouple the syncer from concrete repositories so it
 * remains unit-testable without a Koin graph.
 */
class RevenueCatAttributesSyncer(
    private val attributes: RevenueCatAttributes,
    private val envInfo: AppEnvironmentInfo,
    private val subscriptionStateProvider: () -> String,
    private val themeModeProvider: () -> String,
    private val hasCustomThemeProvider: () -> Boolean,
    private val grantsTotalProvider: () -> Long,
    private val adsShownTotalProvider: () -> Long,
    private val tempAccessActiveProvider: () -> Boolean,
) {
    private val log = logger()

    fun pushSnapshot() {
        val map = buildMap()
        attributes.set(map)
        log.i { "RC snapshot pushed (${map.size} attrs)" }
    }

    fun setPushToken(token: String) {
        attributes.setPushToken(token)
    }

    @OptIn(FlowPreview::class)
    fun start(
        scope: CoroutineScope,
        subscriptionStateFlow: Flow<String>,
        themeModeFlow: Flow<String>,
        hasCustomThemeFlow: Flow<Boolean>,
        grantsTotalFlow: Flow<Long>,
        adsShownTotalFlow: Flow<Long>,
        tempAccessActiveFlow: Flow<Boolean>,
    ) {
        // Initial push at startup.
        pushSnapshot()

        // Debounced delta pusher.
        combine(
            subscriptionStateFlow.distinctUntilChanged(),
            themeModeFlow.distinctUntilChanged(),
            hasCustomThemeFlow.distinctUntilChanged(),
            grantsTotalFlow.distinctUntilChanged(),
            adsShownTotalFlow.distinctUntilChanged(),
            tempAccessActiveFlow.distinctUntilChanged(),
        ) { _, _, _, _, _, _ -> Unit }
            .debounce(1_000)
            .onEach { pushSnapshot() }
            .launchIn(scope)
    }

    private fun buildMap(): Map<String, String?> = mapOf(
        "app_version" to envInfo.appVersionName,
        "app_build" to envInfo.appBuildNumber,
        "platform" to platformString(),
        "os_version" to envInfo.osVersion,
        "device_model" to envInfo.deviceModel,
        "locale" to envInfo.locale,
        "country" to envInfo.country.ifEmpty { null },
        "form_factor" to envInfo.formFactor,
        "subscription_state" to subscriptionStateProvider(),
        "theme_mode" to themeModeProvider(),
        "has_custom_theme" to hasCustomThemeProvider().toString(),
        "temporary_access_active" to tempAccessActiveProvider().toString(),
        "temporary_access_grants_total" to grantsTotalProvider().toString(),
        "rewarded_ads_shown_total" to adsShownTotalProvider().toString(),
    )

    private fun platformString(): String =
        envInfo.osVersion.substringBefore(' ').lowercase().let {
            when {
                it.contains("android") -> "android"
                it.contains("ios") -> "ios"
                else -> "desktop"
            }
        }
}
```

- [ ] **Step 2: Run the syncer test.**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncerTest"`
Expected: 2 tests passed.

- [ ] **Step 3: Commit.**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatAttributesSyncer.kt
git add composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatAttributesSyncerTest.kt
git commit -m "feat(billing): add RevenueCatAttributesSyncer with debounced flow push"
```

---

### Task D4: Wire syncer into Koin

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt`

- [ ] **Step 1: Add imports near the top.**

```kotlin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.billing.DefaultRevenueCatAttributes
import me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributes
import me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncer
import me.calebjones.spacelaunchnow.platform.AppEnvironmentInfo
```

- [ ] **Step 2: Add singletons inside `appModule = module { ... }`.**

After the `TemporaryPremiumAccess` registration (around line 313–321), add:

```kotlin
    single<RevenueCatAttributes> { DefaultRevenueCatAttributes() }

    single { AppEnvironmentInfo() }

    single {
        val tempAccess = get<TemporaryPremiumAccess>()
        val themePrefs = get<ThemePreferences>()
        val appPrefs = get<AppPreferences>()
        val subscriptionRepo = get<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
        RevenueCatAttributesSyncer(
            attributes = get(),
            envInfo = get(),
            subscriptionStateProvider = {
                subscriptionRepo.state.value.subscriptionType.name.lowercase()
            },
            themeModeProvider = {
                runBlocking { appPrefs.themeFlow.first().name.lowercase() }
            },
            hasCustomThemeProvider = {
                runBlocking { themePrefs.customPrimaryColorFlow.first() != null }
            },
            grantsTotalProvider = {
                runBlocking { tempAccess.grantsTotalFlow.first() }
            },
            adsShownTotalProvider = {
                runBlocking { tempAccess.adsShownTotalFlow.first() }
            },
            tempAccessActiveProvider = {
                runBlocking {
                    tempAccess.hasTemporaryAccess(
                        me.calebjones.spacelaunchnow.data.model.PremiumFeature.CUSTOM_THEMES
                    ) || tempAccess.hasTemporaryAccess(
                        me.calebjones.spacelaunchnow.data.model.PremiumFeature.ADVANCED_WIDGETS
                    ) || tempAccess.hasTemporaryAccess(
                        me.calebjones.spacelaunchnow.data.model.PremiumFeature.WIDGETS_CUSTOMIZATION
                    )
                }
            },
        )
    }
```

`runBlocking` is acceptable here because the providers are called from the syncer's coroutine scope (not the main thread) — they execute inside a `pushSnapshot()` whose callers are already off-main.

- [ ] **Step 3: Verify Koin still resolves.**

Run: `./gradlew :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit.**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt
git commit -m "chore(di): register RevenueCatAttributes + syncer in Koin"
```

---

### Task D5: Start syncer from Android `MainApplication`

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`

- [ ] **Step 1: Locate the billing-init coroutine.**

Find the `kotlinx.coroutines.GlobalScope.launch { ... }` block that initializes `BillingManager` (around line 171). After `repository.initialize()` and `syncer.syncNow()` succeed, add the RC attributes syncer start:

- [ ] **Step 2: Add the syncer-start call.**

Inside the same `try` block, after `log.d { "✅ Initial subscription sync complete" }` (around line 194), add:

```kotlin
                // Step 4b: Start RevenueCat attributes syncer
                val rcSyncer =
                    getKoin().get<me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncer>()
                val tempAccess =
                    getKoin().get<me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess>()
                val appPrefs =
                    getKoin().get<me.calebjones.spacelaunchnow.data.storage.AppPreferences>()
                val themePrefs =
                    getKoin().get<me.calebjones.spacelaunchnow.data.storage.ThemePreferences>()

                rcSyncer.start(
                    scope = kotlinx.coroutines.GlobalScope,
                    subscriptionStateFlow = repository.state.let { stateFlow ->
                        kotlinx.coroutines.flow.flow {
                            kotlinx.coroutines.flow.collectLatest(stateFlow) {
                                emit(it.subscriptionType.name.lowercase())
                            }
                        }
                    },
                    themeModeFlow = kotlinx.coroutines.flow.flow {
                        appPrefs.themeFlow.collect { emit(it.name.lowercase()) }
                    },
                    hasCustomThemeFlow = kotlinx.coroutines.flow.flow {
                        themePrefs.customPrimaryColorFlow.collect { emit(it != null) }
                    },
                    grantsTotalFlow = tempAccess.grantsTotalFlow,
                    adsShownTotalFlow = tempAccess.adsShownTotalFlow,
                    tempAccessActiveFlow = tempAccess.accessChangeTrigger.let { trigger ->
                        kotlinx.coroutines.flow.flow {
                            trigger.collect {
                                val active =
                                    tempAccess.hasTemporaryAccess(
                                        me.calebjones.spacelaunchnow.data.model.PremiumFeature.CUSTOM_THEMES
                                    ) || tempAccess.hasTemporaryAccess(
                                        me.calebjones.spacelaunchnow.data.model.PremiumFeature.ADVANCED_WIDGETS
                                    ) || tempAccess.hasTemporaryAccess(
                                        me.calebjones.spacelaunchnow.data.model.PremiumFeature.WIDGETS_CUSTOMIZATION
                                    )
                                emit(active)
                            }
                        }
                    },
                )
                log.d { "✅ RevenueCatAttributesSyncer started" }
```

The `state.let { ... }` and `accessChangeTrigger.let { ... }` wrappers convert the `StateFlow`s into derived `Flow`s that emit the values the syncer expects. This is verbose; a future refactor could move these adapters into the syncer itself, but for v1 keeping them at the call-site keeps the syncer pure.

- [ ] **Step 3: Verify Android compiles.**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit.**

```bash
git add composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt
git commit -m "feat(android): start RevenueCatAttributesSyncer after billing init"
```

---

### Task D6: Start syncer from iOS `KoinInitializer`

**Files:**
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/KoinInitializer.kt`

- [ ] **Step 1: Add a top-level helper that Swift can call.**

At the bottom of the file, after the `KoinHelper` companion `instance()`, append:

```kotlin
/**
 * Called from Swift (e.g. `AppDelegate.didFinishLaunchingWithOptions`) after
 * `initKoin()` and after RevenueCat has been configured by `IosBillingManager`.
 */
fun startRevenueCatAttributesSyncer() {
    val koin = getKoin()
    val rcSyncer =
        koin.get<me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncer>()
    val tempAccess =
        koin.get<me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess>()
    val appPrefs =
        koin.get<me.calebjones.spacelaunchnow.data.storage.AppPreferences>()
    val themePrefs =
        koin.get<me.calebjones.spacelaunchnow.data.storage.ThemePreferences>()
    val repository =
        koin.get<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()

    @Suppress("OPT_IN_USAGE")
    val scope = kotlinx.coroutines.GlobalScope

    rcSyncer.start(
        scope = scope,
        subscriptionStateFlow = kotlinx.coroutines.flow.flow {
            repository.state.collect { emit(it.subscriptionType.name.lowercase()) }
        },
        themeModeFlow = kotlinx.coroutines.flow.flow {
            appPrefs.themeFlow.collect { emit(it.name.lowercase()) }
        },
        hasCustomThemeFlow = kotlinx.coroutines.flow.flow {
            themePrefs.customPrimaryColorFlow.collect { emit(it != null) }
        },
        grantsTotalFlow = tempAccess.grantsTotalFlow,
        adsShownTotalFlow = tempAccess.adsShownTotalFlow,
        tempAccessActiveFlow = kotlinx.coroutines.flow.flow {
            tempAccess.accessChangeTrigger.collect {
                val active = tempAccess.hasTemporaryAccess(
                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.CUSTOM_THEMES
                ) || tempAccess.hasTemporaryAccess(
                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.ADVANCED_WIDGETS
                ) || tempAccess.hasTemporaryAccess(
                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.WIDGETS_CUSTOMIZATION
                )
                emit(active)
            }
        },
    )
}
```

- [ ] **Step 2: Call it from Swift.**

Edit `iosApp/iosApp/AppDelegate.swift`. After the existing `KoinInitializerKt.doInitKoin()` call (or wherever Koin is currently initialized — search the file for `initKoin` if uncertain), add:

```swift
// Start RC attributes syncer once the billing manager has had a chance to
// initialize. We dispatch with a short delay to let the Koin-driven billing
// init coroutine register the user before we push the snapshot.
DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
    KoinInitializerKt.startRevenueCatAttributesSyncer()
}
```

- [ ] **Step 3: Verify the iOS framework still builds.**

Run: `./gradlew :composeApp:linkPodDebugFrameworkIosArm64` (or whatever iOS framework target this project uses; check `composeApp/build.gradle.kts` if uncertain)
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit.**

```bash
git add composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/KoinInitializer.kt
git add iosApp/iosApp/AppDelegate.swift
git commit -m "feat(ios): start RevenueCatAttributesSyncer from AppDelegate"
```

---

## Phase E — Push Token Bridge

### Task E1: Forward FCM token to RC on Android

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`

- [ ] **Step 1: Add the call after the syncer starts.**

In the same billing-init coroutine, immediately after `log.d { "✅ RevenueCatAttributesSyncer started" }` from Task D5, append:

```kotlin
                // Step 4c: Forward FCM token to RevenueCat for re-engagement campaigns.
                try {
                    val pushMessaging =
                        getKoin().get<me.calebjones.spacelaunchnow.data.notifications.PushMessaging>()
                    val fcmToken = pushMessaging.getToken()
                    if (!fcmToken.isNullOrBlank()) {
                        val rcAttrs =
                            getKoin().get<me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributes>()
                        rcAttrs.setPushToken(fcmToken)
                        log.d { "✅ FCM token forwarded to RevenueCat" }
                    } else {
                        log.d { "FCM token not available yet; skipping RC push token set" }
                    }
                } catch (e: Exception) {
                    log.w(e) { "Failed to forward FCM token to RevenueCat" }
                }
```

If `PushMessaging` does not expose `getToken()` directly, look for the existing helper that the `DebugSettingsViewModel` uses to fetch the token (around line 240 of `DebugSettingsViewModel.kt`) and adopt the same call pattern — do not introduce a second token-fetch path.

- [ ] **Step 2: Verify Android compiles.**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit.**

```bash
git add composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt
git commit -m "feat(android): forward FCM token to RevenueCat on startup"
```

---

### Task E2: Forward APNS token to RC on iOS

**Files:**
- Modify: `iosApp/iosApp/AppDelegate.swift`

- [ ] **Step 1: Locate the existing APNS callback.**

Open `iosApp/iosApp/AppDelegate.swift` and find the method `application(_:didRegisterForRemoteNotificationsWithDeviceToken:)` (around line 128).

- [ ] **Step 2: Forward the token directly to RC's iOS SDK.**

Add (or merge into the existing implementation):

```swift
import RevenueCat

func application(
    _ application: UIApplication,
    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
) {
    // Existing FCM handling preserved here…

    // Forward APNS token to RevenueCat for re-engagement campaigns.
    Purchases.shared.attribution.setPushToken(deviceToken)
    NSLog("✅ APNS token forwarded to RevenueCat (length=\(deviceToken.count))")
}
```

If `RevenueCat` is not imported at the top of `AppDelegate.swift`, add `import RevenueCat`. The Swift SDK is already a dependency through the iOS Pods config (verify with `cat iosApp/Podfile | grep RevenueCat`).

- [ ] **Step 3: Build the iOS app to verify.**

Open `iosApp/iosApp.xcodeproj` in Xcode, select a simulator, build (`Cmd+B`).
Expected: Build succeeds. No "use of unresolved identifier 'Purchases'" errors.

- [ ] **Step 4: Commit.**

```bash
git add iosApp/iosApp/AppDelegate.swift
git commit -m "feat(ios): forward APNS token to RevenueCat in AppDelegate"
```

---

## Phase F — End-to-End Verification

### Task F1: Manual end-to-end check on Android

- [ ] **Step 1: Install and launch the debug build.**

Run: `./gradlew installDebug`
Expected: app launches without crashing.

- [ ] **Step 2: Open the RevenueCat dashboard.**

Navigate to project `projbe17841f` → Customers → search by the user ID shown in **Settings → Support Us → User ID** (already exposed via `RevenueCatUserIdCard`).

- [ ] **Step 3: Verify subscriber attributes are populated.**

Within ~10 seconds of cold-start the customer should show:

- `app_version`, `app_build`, `platform=android`, `os_version`, `device_model`, `locale`, `country`, `form_factor`
- `subscription_state` (free/legacy/premium/lifetime)
- `theme_mode`, `has_custom_theme`
- `temporary_access_active`, `temporary_access_grants_total`, `rewarded_ads_shown_total`
- `$fcmTokens` (under "Push Tokens" tab in the RC UI)

- [ ] **Step 4: Trigger an ad and verify counter delta.**

Note `temporary_access_grants_total` and `rewarded_ads_shown_total` values. Tap the "Watch Ad" button on Theme settings, complete the ad. Reload the RC customer page. Expect:

- `rewarded_ads_shown_total` incremented by 1.
- `temporary_access_grants_total` incremented by 3 (one per default feature).
- `temporary_access_active=true`.

- [ ] **Step 5: Trigger a theme change and verify delta.**

Change the app theme from System → Dark. Wait ~2 seconds (debounce). Reload the RC customer page. Expect `theme_mode=dark`.

- [ ] **Step 6: No commit. Manual verification only.**

---

### Task F2: Manual end-to-end check on iOS

- [ ] **Step 1: Build and run on a device.**

Open `iosApp/iosApp.xcodeproj` in Xcode, select a real device (push tokens require a physical device), run.

- [ ] **Step 2: Repeat F1 steps 2–5 with the iOS user.**

Expect `platform=ios` and `$apnsTokens` populated instead of `$fcmTokens`. All other attributes should match the Android behaviour.

- [ ] **Step 3: No commit. Manual verification only.**

---

## Self-Review Checklist (run after writing the plan)

- [x] **Spec coverage:**
    - Part 1 events (4) — Task A1.
    - Card wiring with `source` and `koinInject<AnalyticsManager>()` — Task A2.
    - Theme-screen call site — Task A2 step 7.
    - Counters in DataStore + tests — Tasks B1, B2.
    - Counter wiring (grant + ad-shown) — Task B2 steps 4 & 5.
    - `AppEnvironmentInfo` expect/actual — Tasks C1, C2.
    - `RevenueCatAttributes` interface + impl — Task D1.
    - `RevenueCatAttributesSyncer` + tests — Tasks D2, D3.
    - Koin wiring — Task D4.
    - Android startup wiring — Task D5.
    - iOS startup wiring — Task D6.
    - FCM token forward — Task E1.
    - APNS token forward — Task E2.
    - Manual verification — Tasks F1, F2.
- [x] **Placeholder scan:** No "TBD" / "TODO" / "implement later" tokens. Each step has the actual code or command.
- [x] **Type consistency:** `incrementGrantsTotal` / `incrementAdsShownTotal` / `grantsTotalFlow` / `adsShownTotalFlow` used consistently across B1, B2, D3, D4, D5, D6. `RevenueCatAttributesSyncer` constructor signature in D3 matches the test in D2 and the Koin registration in D4. `source` parameter on `TemporaryPremiumCard` is positional after `temporaryPremiumAccess` consistently across A2 and A2 step 7.
