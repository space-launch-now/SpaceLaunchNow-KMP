# 018 Monetization Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the app-code items of the 018 monetization backlog — revive the dead rewarded-ad flow (US2), make purchase failures attributable and purchase revenue measurable (US1), and give the notification funnel a true denominator plus a working tap event (US5).

**Architecture:** Rewarded ads move from a dead preloaded-CompositionLocal pattern to on-demand loading via basic-ads `rememberRewardedAd(...)`, mirroring the proven `InterstitialAdHandler` on-demand pattern already shipped in `fcf99328`. Purchase analytics are fixed entirely in commonMain (`SubscriptionViewModel` + `AnalyticsEvent`) because RevenueCat KMP's typed `PurchasesTransactionException` flows through `Result.failure` to the ViewModel on both platforms. Notification funnel events are fired at the real display/tap sites: Android's `NotificationDisplayHelper.notify()` calls and `MainActivity` intent handling; iOS's `AppDelegate.scheduleNotification` and `handleNotificationTap`.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, basic-ads 1.1.1 (`app.lexilabs.basic`), RevenueCat purchases-kmp 3.3.0, Firebase Analytics (GitLive KMP on Kotlin side, native `FirebaseAnalytics` on Swift side), Koin, kotlin.test + kotlinx-coroutines-test.

**Spec:** `specs/018-monetization-fixes/BACKLOG.md` (the referenced `./spec.md` does not exist; the backlog is the source of truth). Items implemented: #1 (US2 FR-2.1/2.2/2.3), #2 (US1 FR-1.1/1.2), #4 (US5). Items NOT in this plan: #3 (iOS release — release ops, no app code), #5 (banner parity — investigation-first, no fix defined), #6–#8 (investigation / console config / context).

**Resolved open questions (with evidence):**
- **OQ-1 (fix vs remove rewarded):** FIX. The feature never worked in production (preloaded locals are never provided — see Task 2 evidence), so the 13-user usage figure is not evidence of demand. The backlog's own "Fix:" line documents the on-demand approach.
- **OQ-2 (iOS sheet-dismissal vs billing error):** YES, distinguishable. `Purchases.awaitPurchase` (purchases-kmp 3.3.0, `ktx/Coroutines.kt:197-199`) throws `PurchasesTransactionException(error, userCancelled: Boolean)`; `PurchasesException` exposes `code: PurchasesErrorCode` (includes `PurchaseCancelledError`, `StoreProblemError`, `NetworkError`, …). Both `AndroidBillingManager` and `IosBillingManager` catch `Exception` and wrap with `Result.failure(e)`, so the typed exception reaches the ViewModel intact on both platforms.
- **OQ-4 (rename notification_received):** keep the name, per the backlog. Not touched by this plan.

## Global Constraints

- JDK 21 required; `.env` must exist at repo root before any Gradle build.
- Conventional Commits mandatory (`feat:`/`fix:`/`chore:` with scopes). **Do NOT add Claude as co-author.**
- **Commits are HELD until Caleb tests.** Caleb's standing preference: stop at the testable checkpoint, leave work uncommitted, commit only on his confirmation. Each task below ends with a "stage commit message" step — record the message, do NOT run `git commit`. Task 12 is the checkpoint.
- Analytics must never break the feature it instruments — every new `track(...)` call in notification/ad paths is wrapped in try/catch.
- ADR-0001 layering: no `api.*.models` imports outside `api/extensions/` + `domain/mapper/` (not relevant to these files, but binding).
- Fast local verify: `./gradlew :composeApp:desktopTest` (common tests — note: CLAUDE.md's `:composeApp:jvmTest` task does not exist in this build; neither does `ktlintCheck`) and `./gradlew compileKotlinDesktop` (commonMain compile). Android-only code: `./gradlew :composeApp:compileDebugKotlinAndroid`. **iOS Kotlin (`iosMain`) and Swift cannot be compiled on this Windows machine** — those tasks end at "written + reviewed"; compile verification happens in Xcode on the Mac (or CI) before release.
- GA4 event-name collision rule from the backlog: our custom events must not collide with Firebase auto-collected names (`notification_receive`, `notification_open` are Firebase's; `notification_shown` / `notification_tapped` are safe and `notification_tapped` already exists in code).

---

### Task 1: Add `onAdDismissed` to the RewardedAdHandler contract (expect + desktop actual)

The on-demand pattern needs a dismissal callback: basic-ads `rememberRewardedAd` **auto-reloads whenever state is NONE or DISMISSED**, so the caller must be told about dismissal to drop `shouldShow` and unmount the handler, or the ad re-shows in a loop. The current expect signature has no such callback.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdComposables.kt:81-87`
- Modify: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/RewardedAdHandler.desktop.kt`

**Interfaces:**
- Produces (used by Tasks 2, 3, 4):
  ```kotlin
  @Composable
  expect fun RewardedAdHandler(
      shouldShow: Boolean = false,
      onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)? = null,
      onAdShown: (() -> Unit)? = null,
      onAdFailed: ((String) -> Unit)? = null,
      onAdDismissed: (() -> Unit)? = null
  )
  ```

- [ ] **Step 1: Update the expect declaration**

In `AdComposables.kt`, replace the `RewardedAdHandler` expect fun (lines 81–87) with:

```kotlin
/**
 * Rewarded ad handler that loads an ad on demand and shows it.
 *
 * On Android/iOS:
 * - Loads the rewarded ad on demand when [shouldShow] becomes true (no preloading)
 * - Calls reward callback when user completes watching the ad
 * - Calls [onAdDismissed] if the user closes the ad before earning the reward —
 *   callers MUST drop [shouldShow] in response or the ad will reload and re-show
 *
 * On Desktop:
 * - Does nothing (no-op)
 *
 * @param shouldShow Whether to load and show the rewarded ad
 * @param onRewardEarned Called when the user earns a reward (amount, type)
 * @param onAdShown Called when a rewarded ad is successfully shown
 * @param onAdFailed Called when a rewarded ad fails to load or show
 * @param onAdDismissed Called when the user dismisses the ad without earning the reward
 */
@Composable
expect fun RewardedAdHandler(
    shouldShow: Boolean = false,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)? = null,
    onAdShown: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null,
    onAdDismissed: (() -> Unit)? = null
)
```

- [ ] **Step 2: Update the desktop actual**

Open `RewardedAdHandler.desktop.kt`; make the actual a no-op with the matching signature (keep the file's existing package/imports; the body stays empty):

```kotlin
@Composable
actual fun RewardedAdHandler(
    shouldShow: Boolean,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)?,
    onAdShown: (() -> Unit)?,
    onAdFailed: ((String) -> Unit)?,
    onAdDismissed: (() -> Unit)?
) {
    // No-op on desktop - ads not supported
}
```

If the desktop file also declares a `TriggerRewardedAdIfReady` helper, leave it compiling by passing the new parameter through (or delete it — it has no production call sites; verify with grep first: `grep -rn "TriggerRewardedAdIfReady" composeApp/src --include='*.kt' | grep -v worktrees`).

- [ ] **Step 3: Compile check (will fail for android until Task 2 — that's expected)**

Run: `./gradlew compileKotlinDesktop`
Expected: PASS (desktop actual matches new expect). Android/iOS actuals still have the old signature — they are fixed in Tasks 2–3; do not run Android compile yet.

- [ ] **Step 4: Stage commit message (do not commit)**

`fix(ads): load rewarded ads on demand instead of dead preloaded handles` — Tasks 1–3 form one commit.

---

### Task 2: Rewrite Android RewardedAdHandler to load on demand

**Root cause being fixed (evidence):** `LocalPreloadedRewardedAd` / `LocalPreloadedInterstitialAd` are declared in `AdCompositionLocals.android.kt:57-62` (default `null`) but appear in **no** `provides` list — `AdSupport.android.kt:171-179` provides only banner locals. The handler's null-check at `RewardedAdHandler.android.kt:47-57` therefore always early-returns. Same on iOS.

**Files:**
- Rewrite: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/RewardedAdHandler.android.kt`
- Pattern reference (do not modify): `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/InterstitialAdHandler.android.kt`

**Interfaces:**
- Consumes: `rememberRewardedAd(adUnitId: String, onLoad, onFailure): MutableState<RewardedAdHandler>` and `RewardedAd(loadedAd, onRewardEarned: (RewardItem) -> Unit, onDismissed, onShown, onFailure)` from `app.lexilabs.basic.ads.composable`; `GlobalAdManager.getPlatformAdUnitId(AdType.REWARDED)` (exists: `GlobalAdManager.android.kt:312,320`).
- Produces: the actual matching Task 1's expect signature.

- [ ] **Step 1: Replace the file contents**

```kotlin
package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.RewardedAd
import app.lexilabs.basic.ads.composable.rememberRewardedAd
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.koinInject

private val log by lazy { SpaceLogger.getLogger("RewardedAdHandler") }

/**
 * Android implementation of RewardedAdHandler using BasicAds library.
 *
 * Loads the rewarded ad **on demand** when [shouldShow] becomes true, mirroring the
 * on-demand InterstitialAdHandler pattern. The old preloaded-CompositionLocal path was
 * never provided after preloading was removed, which left this handler permanently
 * early-returning and the "Watch Ad for 24h Premium Access" flow dead (spec 018 US2).
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
actual fun RewardedAdHandler(
    shouldShow: Boolean,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)?,
    onAdShown: (() -> Unit)?,
    onAdFailed: ((String) -> Unit)?,
    onAdDismissed: (() -> Unit)?
) {
    if (!shouldShow) return

    val contextFactory = LocalContextFactory.current
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    val subscriptionRepo =
        koinInject<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsStateWithLifecycle()

    if (subscriptionState.isLoading ||
        hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null
    ) {
        log.w { "Not showing rewarded ad due to conditions" }
        return
    }

    // Track terminal outcomes so the (auto-reloading) ad is never shown twice per request.
    var rewardGranted by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    // ON-DEMAND LOAD: only requested once the user has tapped "Watch Ad".
    val rewardedAd by rememberRewardedAd(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.REWARDED),
        onFailure = { e ->
            log.e { "Rewarded ad failed to load: ${e.message}" }
            if (!finished) {
                finished = true
                onAdFailed?.invoke(e.message ?: "Failed to load")
            }
        }
    )

    LaunchedEffect(rewardedAd.state) {
        when (rewardedAd.state) {
            AdState.READY -> log.d { "Rewarded ad loaded and ready to show" }
            AdState.LOADING -> log.d { "Rewarded ad is loading..." }
            AdState.FAILING -> {
                log.e { "Rewarded ad failed" }
                if (!finished) {
                    finished = true
                    onAdFailed?.invoke("Failed to load")
                }
            }
            else -> log.v { "Rewarded ad state: ${rewardedAd.state}" }
        }
    }

    if (!finished && rewardedAd.state == AdState.READY) {
        RewardedAd(
            loadedAd = rewardedAd,
            onRewardEarned = {
                if (!rewardGranted) {
                    log.d { "User earned reward" }
                    rewardGranted = true
                    onRewardEarned?.invoke(1, "reward")
                }
            },
            onShown = { onAdShown?.invoke() },
            onDismissed = {
                log.d { "Rewarded ad dismissed" }
                if (!finished) {
                    finished = true
                    // Reward callbacks can arrive before dismissal; only report an
                    // unrewarded dismissal if no reward was granted.
                    if (!rewardGranted) onAdDismissed?.invoke()
                }
            },
            onFailure = { e ->
                log.e { "Rewarded ad failed to show: ${e.message}" }
                if (!finished) {
                    finished = true
                    onAdFailed?.invoke(e.message ?: "Failed to show")
                }
            }
        )
    }
}
```

**Watch out:** every callback must go through the `finished` flag so `onAdFailed`/`onAdDismissed` fire at most once per request (`rememberRewardedAd` auto-reloads after DISMISSED/FAILING, which would otherwise re-trigger them).

- [ ] **Step 2: Compile check**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: PASS. (If the task name differs, discover it with `./gradlew :composeApp:tasks --all | grep -i compile | grep -i debug`.)

- [ ] **Step 3: Stage commit message (same commit as Task 1)**

---

### Task 3: Rewrite iOS RewardedAdHandler to load on demand

**Files:**
- Rewrite: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/RewardedAdHandler.ios.kt`
- Pattern reference: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/InterstitialAdHandler.ios.kt` (identical structure to Android's)

**Interfaces:** same as Task 2; `GlobalAdManager.getPlatformAdUnitId(AdType.REWARDED)` exists on iOS (`GlobalAdManager.ios.kt:326,334`).

- [ ] **Step 1: Replace the file contents**

Use **exactly the Task 2 code** with only these differences (the iOS interstitial file differs from Android's the same way):
- Package stays `me.calebjones.spacelaunchnow.ui.ads` (same).
- Keep iOS-style emoji log prefixes if desired to match `InterstitialAdHandler.ios.kt` conventions (`🎯`, `✅`, `❌`) — content otherwise identical.
- The KDoc first line reads "iOS implementation of RewardedAdHandler using BasicAds library."

- [ ] **Step 2: Verification (limited on Windows)**

iOS Kotlin cannot compile on this machine. Do a structural self-check instead: confirm the actual's signature matches Task 1's expect **exactly** (param names, order, nullability), and confirm imports match what `InterstitialAdHandler.ios.kt` uses plus `RewardedAd`/`rememberRewardedAd`. Full compile happens in Xcode on the Mac before release.

- [ ] **Step 3: Stage commit message (same commit as Tasks 1–2)**

---

### Task 4: Loading state + dismissal handling in TemporaryPremiumCard

With on-demand loading there are now 1–5 seconds between tap and ad display. The card must show progress, and must reset when the user dismisses the ad without a reward (previously unhandled — with auto-reload it would loop).

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/TemporaryPremiumCard.kt`

**Interfaces:**
- Consumes: Task 1's expect signature (`onAdDismissed`).

- [ ] **Step 1: Add loading UI state**

In `TemporaryPremiumCard`, next to `var showRewardedAd by remember { mutableStateOf(false) }` (line 70), the `showRewardedAd == true` period IS the loading period (the handler unmounts when it flips false). Change both buttons (lines 187–210 and 213–234) to render a progress state while loading:

For the primary button (line 213):
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
    enabled = !showRewardedAd,
    modifier = Modifier.fillMaxWidth()
) {
    if (showRewardedAd) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Loading ad…")
    } else {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Watch Ad for 24h Premium Access")
    }
}
```

Apply the same `enabled = !showRewardedAd` + loading-content pattern to the `OutlinedButton` (extension path, line 187), with loading text `"Loading ad…"` replacing `"Extend All Access (+24h)"`.

Add import: `androidx.compose.material3.CircularProgressIndicator`.

- [ ] **Step 2: Handle dismissal and add a load timeout**

In the `if (showRewardedAd)` block (line 239), pass the new callback:

```kotlin
onAdDismissed = {
    log.i { "Rewarded ad dismissed before reward" }
    showRewardedAd = false
}
```

And add a 30-second give-up guard next to the other `LaunchedEffect`s (covers the case where the SDK never reaches FAILING):

```kotlin
// Give up if the ad hasn't resolved within 30s so the button doesn't spin forever.
LaunchedEffect(showRewardedAd) {
    if (showRewardedAd) {
        delay(30_000)
        if (showRewardedAd) {
            log.w { "Rewarded ad load timed out" }
            analyticsManager.track(
                AnalyticsEvent.RewardedAdFailed(source = source, error = "timeout")
            )
            showRewardedAd = false
        }
    }
}
```

(`kotlinx.coroutines.delay` is already imported at line 34.) Note the existing `onAdShown` sets no state — after the ad actually shows, the timeout must not kill it: guard the timeout by moving `showRewardedAd = false` into a check that the ad hasn't shown. Add `var adShowing by remember { mutableStateOf(false) }`, set it `true` inside the existing `onAdShown` lambda, reset to `false` wherever `showRewardedAd` is set `false`, and change the timeout condition to `if (showRewardedAd && !adShowing)`.

- [ ] **Step 3: Compile + test check**

Run: `./gradlew compileKotlinDesktop && ./gradlew :composeApp:jvmTest`
Expected: both PASS (no existing tests cover this file; this catches compile/regression fallout).

- [ ] **Step 4: Stage commit message (do not commit)**

`feat(ui): show loading state while rewarded ad loads on demand`

---

### Task 5: Delete the dead preloaded interstitial/rewarded CompositionLocals

FR-2.3: nothing provides or (after Tasks 2–3) reads them.

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/AdCompositionLocals.android.kt:53-62` (delete `LocalPreloadedInterstitialAd`, `LocalPreloadedRewardedAd` + their KDoc + now-unused `InterstitialAdHandler`/`RewardedAdHandler` imports)
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/AdCompositionLocals.ios.kt:57-62` (same)
- Modify: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/AdCompositionLocals.desktop.kt:18-19` (same)
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdSupport.android.kt:26-27` (delete the two dead imports)
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdSupport.ios.kt:30-31` (same)

- [ ] **Step 1: Confirm zero remaining references**

Run: `grep -rn "LocalPreloadedRewardedAd\|LocalPreloadedInterstitialAd" composeApp/src --include='*.kt' | grep -v worktrees`
Expected after Tasks 2–3: only the declaration files and the AdSupport imports listed above. If anything else appears, fix it first.

- [ ] **Step 2: Delete the declarations and imports** (files/lines listed above)

- [ ] **Step 3: Compile check**

Run: `./gradlew compileKotlinDesktop && ./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: PASS.

- [ ] **Step 4: Stage commit message (do not commit)**

`chore(ads): delete unused preloaded interstitial/rewarded CompositionLocals`

---

### Task 6: FR-1.2 — attach revenue to purchase_completed

`PurchaseCompleted` already accepts `revenue: Double?` and omits it when null (`AnalyticsEvent.kt:152-158`). Both paywalls call `purchaseProduct(product: ProductInfo)` where `ProductInfo.priceAmountMicros: Long` is available (`SupportUsScreen.kt:313,336,372`, `OnboardingPaywallScreen.kt:158`).

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt:180-212`
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModelTest.kt`

**Interfaces:**
- Produces: `fun purchaseProduct(productId: String, basePlanId: String? = null, priceAmountMicros: Long? = null)` — Task 7 edits the same function's `onFailure` branch.

- [ ] **Step 1: Write the failing test**

In `SubscriptionViewModelTest.kt`, the test class builds `AnalyticsManagerImpl(emptyList())` (line 36). Add a `FakeAnalyticsProvider`-backed instance for the new tests (import `me.calebjones.spacelaunchnow.analytics.FakeAnalyticsProvider`, `me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent`, and `kotlinx.coroutines.test.advanceUntilIdle`):

```kotlin
@Test
fun `purchase completed event carries revenue derived from price micros`() = runTest(testDispatcher) {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, AnalyticsManagerImpl(listOf(fake)))

    vm.purchaseProduct(
        ProductInfo(
            productId = "yearly_sub",
            basePlanId = "yearly-base",
            title = "Yearly",
            description = "Annual subscription",
            formattedPrice = "$39.99",
            priceAmountMicros = 39990000L,
            currencyCode = "USD"
        )
    )
    advanceUntilIdle()

    val completed = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseCompleted>().single()
    assertEquals("yearly_sub", completed.productId)
    assertEquals(39.99, completed.revenue!!, 0.0001)
}
```

Note: `AnalyticsManagerImpl.track` may dispatch on its own scope — if `advanceUntilIdle` doesn't flush it, check how `AnalyticsManagerImplTest.kt` awaits tracked events and use the same mechanism.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModelTest"`
Expected: FAIL — `revenue` is null (event currently constructed without it).

- [ ] **Step 3: Implement**

In `SubscriptionViewModel.kt`:

```kotlin
fun purchaseProduct(productId: String, basePlanId: String? = null, priceAmountMicros: Long? = null) {
```

and line 191 becomes:

```kotlin
analyticsManager.track(
    AnalyticsEvent.PurchaseCompleted(
        productId = productId,
        revenue = priceAmountMicros?.let { it / 1_000_000.0 }
    )
)
```

and the `ProductInfo` overload (line 210) becomes:

```kotlin
fun purchaseProduct(product: ProductInfo) {
    purchaseProduct(product.productId, product.basePlanId, product.priceAmountMicros)
}
```

(If `ProductInfo.priceAmountMicros` turns out to be nullable, `product.priceAmountMicros` still type-checks against `Long?` — no change needed.)

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModelTest"`
Expected: PASS (all tests in the class, old and new).

- [ ] **Step 5: Stage commit message (do not commit)**

`feat(analytics): attach revenue to purchase_completed`

---

### Task 7: FR-1.1 — purchase_failed event with step + coarse error code

73% of purchase attempts currently end in silence (`SubscriptionViewModel.kt` onFailure only logs).

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt` (add event after `PurchaseCompleted`, ~line 158)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt:194-200`
- Modify: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/billing/MockBillingManager.kt` (configurable failure exception)
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/AnalyticsEventTest.kt`, `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModelTest.kt`

**Interfaces:**
- Produces:
  ```kotlin
  data class PurchaseFailed(
      val productId: String,
      val step: String,       // "setup" | "store_purchase" | "unknown"
      val errorCode: String   // "user_cancelled" | PurchasesErrorCode.name | "product_not_found" | "not_initialized" | "unknown"
  ) : AnalyticsEvent("purchase_failed")
  ```
- Consumes: `com.revenuecat.purchases.kmp.models.PurchasesTransactionException` (has `userCancelled: Boolean`, inherits `code: PurchasesErrorCode` from `PurchasesException`). **DEVIATION APPLIED DURING EXECUTION:** purchases-kmp is deliberately androidMain/iosMain-only (no desktop target), so commonMain cannot import it. Implemented instead via a platform-neutral `PurchaseFlowException(step, errorCode, userCancelled, message, cause)` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/PurchaseFlowException.kt`; `AndroidBillingManager.launchPurchaseFlow` and `IosBillingManager.launchPurchaseFlow` catch `PurchasesTransactionException` / `PurchasesException` and wrap them into it before `Result.failure`. The ViewModel maps `PurchaseFlowException → (step, errorCode)` and keeps the `IllegalArgumentException`/`IllegalStateException`/unknown fallbacks. Tests construct `PurchaseFlowException` directly — no RC dependency needed in commonTest.

- [ ] **Step 1: Write the failing event tests**

In `AnalyticsEventTest.kt`, following the file's existing one-liner style (see lines 33-34):

```kotlin
@Test fun `PurchaseFailed has correct name`() =
    assertEquals("purchase_failed", AnalyticsEvent.PurchaseFailed("p", "store_purchase", "user_cancelled").name)

@Test
fun `PurchaseFailed exposes step and error code params`() {
    val params = AnalyticsEvent.PurchaseFailed("yearly_sub", "store_purchase", "NetworkError").toParameters()
    assertEquals("yearly_sub", params["product_id"])
    assertEquals("store_purchase", params["step"])
    assertEquals("NetworkError", params["error_code"])
}
```

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.analytics.AnalyticsEventTest"`
Expected: FAIL — `PurchaseFailed` unresolved.

- [ ] **Step 3: Add the event**

In `AnalyticsEvent.kt` directly after `PurchaseCompleted`:

```kotlin
/**
 * A purchase attempt ended without completion. `step` is where it died
 * ("setup" = product lookup / not initialized, "store_purchase" = native store flow),
 * `errorCode` is coarse ("user_cancelled" for sheet dismissal, else the
 * RevenueCat PurchasesErrorCode name). Spec 018 FR-1.1.
 */
data class PurchaseFailed(
    val productId: String,
    val step: String,
    val errorCode: String
) : AnalyticsEvent("purchase_failed") {
    override fun toParameters() = mapOf(
        "product_id" to productId,
        "step" to step,
        "error_code" to errorCode
    )
}
```

- [ ] **Step 4: Write the failing ViewModel tests**

First extend `MockBillingManager.kt`: add `var purchaseFailureException: Exception? = null` and in `launchPurchaseFlow` change the failure branch to `Result.failure(purchaseFailureException ?: Exception("Mock purchase failed"))` (keep `shouldLaunchPurchaseFail` as the trigger).

Then in `SubscriptionViewModelTest.kt` (imports: `com.revenuecat.purchases.kmp.models.PurchasesError`, `PurchasesErrorCode`, `PurchasesTransactionException`):

```kotlin
@Test
fun `user cancellation tracks purchase_failed with user_cancelled`() = runTest(testDispatcher) {
    val fake = FakeAnalyticsProvider()
    billingManager.shouldLaunchPurchaseFail = true
    billingManager.purchaseFailureException = PurchasesTransactionException(
        PurchasesError(PurchasesErrorCode.PurchaseCancelledError, null),
        userCancelled = true
    )
    val vm = SubscriptionViewModel(repository, billingManager, AnalyticsManagerImpl(listOf(fake)))

    vm.purchaseProduct("yearly_sub", "yearly-base")
    advanceUntilIdle()

    val failed = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseFailed>().single()
    assertEquals("user_cancelled", failed.errorCode)
    assertEquals("store_purchase", failed.step)
}

@Test
fun `store error tracks purchase_failed with the error code name`() = runTest(testDispatcher) {
    val fake = FakeAnalyticsProvider()
    billingManager.shouldLaunchPurchaseFail = true
    billingManager.purchaseFailureException = PurchasesTransactionException(
        PurchasesError(PurchasesErrorCode.NetworkError, null),
        userCancelled = false
    )
    val vm = SubscriptionViewModel(repository, billingManager, AnalyticsManagerImpl(listOf(fake)))

    vm.purchaseProduct("yearly_sub", "yearly-base")
    advanceUntilIdle()

    val failed = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseFailed>().single()
    assertEquals("NetworkError", failed.errorCode)
}
```

(`PurchasesError`'s constructor signature may differ — check `rc-models` sources under the scratchpad or the IDE; if it takes `(code, underlyingErrorMessage)` adjust the second arg. If constructing it in common test code is awkward, fall back to a plain `Exception` test asserting `errorCode == "unknown"` plus the two typed tests moved to a jvm-only source set — but try the direct construction first.)

- [ ] **Step 5: Run to verify failure**

Run: `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModelTest"`
Expected: FAIL — no `PurchaseFailed` tracked.

- [ ] **Step 6: Implement the onFailure mapping**

In `SubscriptionViewModel.kt`, imports: `com.revenuecat.purchases.kmp.models.PurchasesException`, `com.revenuecat.purchases.kmp.models.PurchasesTransactionException`. Replace the `onFailure` branch of `purchaseProduct` (lines 194-200):

```kotlin
onFailure = { error ->
    _uiState.value = _uiState.value.copy(
        isProcessing = false,
        errorMessage = error.message ?: "Purchase failed"
    )
    val (step, errorCode) = when (error) {
        is PurchasesTransactionException ->
            "store_purchase" to if (error.userCancelled) "user_cancelled" else error.code.name
        is PurchasesException -> "store_purchase" to error.code.name
        is IllegalArgumentException -> "setup" to "product_not_found"
        is IllegalStateException -> "setup" to "not_initialized"
        else -> "unknown" to "unknown"
    }
    analyticsManager.track(
        AnalyticsEvent.PurchaseFailed(productId = productId, step = step, errorCode = errorCode)
    )
    log.e(error) { "Purchase failed for $productId ($step/$errorCode)" }
}
```

- [ ] **Step 7: Run tests to verify pass**

Run: `./gradlew :composeApp:jvmTest`
Expected: PASS (full common suite — guards against regressions in other analytics tests).

- [ ] **Step 8: Stage commit message (do not commit)**

`feat(analytics): add purchase_failed event with step and error code`

---

### Task 8: NotificationShown event class

The true CTR denominator: fired only when a notification is actually posted to the OS shade (vs `notification_received` outcome=displayed, which fires before display and even when OS-level notification permission is revoked).

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt` (after `NotificationReceived`, ~line 182)
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/AnalyticsEventTest.kt`

**Interfaces:**
- Produces (used by Tasks 9, 11):
  ```kotlin
  data class NotificationShown(val type: String, val platform: String? = null) : AnalyticsEvent("notification_shown")
  ```

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test fun `NotificationShown has correct name`() =
    assertEquals("notification_shown", AnalyticsEvent.NotificationShown("launch").name)

@Test
fun `NotificationShown includes platform only when set`() {
    assertEquals(mapOf("type" to "launch"), AnalyticsEvent.NotificationShown("launch").toParameters())
    assertEquals(
        mapOf("type" to "launch", "platform" to "android"),
        AnalyticsEvent.NotificationShown("launch", "android").toParameters()
    )
}
```

- [ ] **Step 2: Run to verify failure** — `./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.analytics.AnalyticsEventTest"` → FAIL (unresolved).

- [ ] **Step 3: Add the event**

```kotlin
/**
 * A notification was actually posted to the OS notification shade — the true
 * denominator for notification CTR (notification_tapped / notification_shown).
 * Unlike notification_received{outcome=displayed}, this is NOT fired when
 * OS-level notification permission is off. Spec 018 US5.
 */
data class NotificationShown(
    val type: String,
    /** "android" or "ios". */
    val platform: String? = null
) : AnalyticsEvent("notification_shown") {
    override fun toParameters() = buildMap {
        put("type", type)
        platform?.let { put("platform", it) }
    }
}
```

- [ ] **Step 4: Run to verify pass** — same command → PASS.

- [ ] **Step 5: Stage commit message (do not commit)**

`feat(analytics): add notification_shown display-time event` — Tasks 8–9 form one commit.

---

### Task 9: Fire notification_shown at Android display time

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NotificationDisplayHelper.kt`

There are six `notify()` sites: lines ~618 (`showNotification`), 687 (`showBasicNotification`), 796 (`showV5Notification`), 904 (`showEventNotification`), 993 (`showNewsNotification`), 1100 (`showCustomNotification`). (Line numbers shift as the file is edited — locate by method.)

- [ ] **Step 1: Add the tracking helper to the object**

```kotlin
/**
 * Fire notification_shown after an actual notify() call. Only when OS-level
 * notifications are enabled — a notify() with notifications blocked is a silent
 * no-op and must not count toward the CTR denominator. Spec 018 US5.
 */
private fun trackShown(context: Context, notificationType: String) {
    try {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        KoinPlatform.getKoin().get<AnalyticsManager>().track(
            AnalyticsEvent.NotificationShown(type = notificationType, platform = "android")
        )
    } catch (e: Exception) {
        // Analytics must never break notification display.
        log.w(e) { "Failed to track notification_shown" }
    }
}
```

Imports to add (check which already exist): `androidx.core.app.NotificationManagerCompat`, `org.koin.mp.KoinPlatform`, `me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager`, `me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent`. If the object has no `log`, follow whatever logging pattern the file already uses.

- [ ] **Step 2: Call it after each notify()**

Immediately after each of the six `notificationManager.notify(...)` calls add `trackShown(context, <the type>)`, where `<the type>` is the notification type already in scope in that method: `notificationData.notificationType` (showNotification), `payload.notificationType` (V5/event/news/custom). For `showBasicNotification` use the type parameter it receives — if it has none, pass the best available identifier (check its signature; fall back to literal `"basic"`).

- [ ] **Step 3: Compile check**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: PASS.

- [ ] **Step 4: Stage commit message (same commit as Task 8)**

---

### Task 10: Fire notification_tapped on Android

**Evidence:** `AnalyticsEvent.NotificationTapped` exists (`AnalyticsEvent.kt:184`) but `grep -rn "NotificationTapped" composeApp/src --include='*.kt'` shows only the event class and tests — zero production call sites. That's the "zero GA4 rows in seven months".

**Files:**
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainActivity.kt`
- Modify (extras audit): `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NotificationDisplayHelper.kt`

- [ ] **Step 1: Ensure every notification content intent carries `notification_type`**

V4 (line ~548), V5 (~737), and event (~845) intents already `putExtra("notification_type", ...)`. Audit the **news** and **custom** intent blocks (in `showNewsNotification` / `showCustomNotification`) and `showBasicNotification`'s intent (~662, currently has no extras): add `putExtra("notification_type", payload.notificationType)` (news/custom) and for basic add the same type string used in Task 9 Step 2. News taps open the article; if the news intent routes to MainActivity with `news_url` extras, add the type extra beside them.

- [ ] **Step 2: Add tap tracking to MainActivity**

Field (after line 37, matching the file's `by inject()` style):
```kotlin
private val analyticsManager: me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager by inject()
```

Private helper:
```kotlin
/**
 * Track a notification tap when this intent came from a notification
 * (notification_type extra is set only by NotificationDisplayHelper).
 * Spec 018 US5 — notification_tapped had zero production call sites.
 */
private fun trackNotificationTapIfPresent(intent: Intent?) {
    val type = intent?.getStringExtra("notification_type") ?: return
    try {
        analyticsManager.track(
            me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent.NotificationTapped(
                type = type,
                launchId = intent.getStringExtra("launch_uuid")
                    ?: intent.getStringExtra("launch_id")
            )
        )
    } catch (e: Exception) {
        log.w(e) { "Failed to track notification tap" }
    }
}
```

Call sites:
- In `onCreate`, right before the existing "Check if launched from notification" block (line ~77), guarded against configuration-change re-delivery: `if (savedInstanceState == null) trackNotificationTapIfPresent(intent)` (`onCreate` has the `savedInstanceState` parameter).
- In `onNewIntent` (line ~231), first line after `super.onNewIntent(intent)`: `trackNotificationTapIfPresent(intent)`.

- [ ] **Step 3: Compile check**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid`
Expected: PASS.

- [ ] **Step 4: Stage commit message (do not commit)**

`feat(analytics): fire notification_tapped from Android notification intents` — or fold with Task 11 into one `feat(analytics): fire notification_tapped on Android and iOS` commit if both land before commit time.

---

### Task 11: Fire notification_tapped + notification_shown on iOS (Swift)

**Files:**
- Modify: `iosApp/iosApp/AppDelegate.swift`

Evidence: FirebaseAnalytics is linked in the Xcode project (`project.pbxproj` lists `FirebaseAnalytics`). The tap funnel is `handleNotificationTap(userInfo:)` (line ~777, called from both the tap delegate at ~413 and the cold-start pending-tap path at ~104). The display funnel is `scheduleNotification(content:identifier:)` (line ~562).

- [ ] **Step 1: Add the import**

`import FirebaseAnalytics` alongside the existing `import FirebaseCore` (line 2).

- [ ] **Step 2: Track taps inside handleNotificationTap**

First lines of `handleNotificationTap(userInfo:)`:

```swift
// Spec 018 US5: notification_tapped had zero call sites — GA4 shows 0 rows in 7 months.
// Kept in this single funnel so both the tap delegate and the cold-start pending-tap
// path are counted. Event name + params match the Kotlin AnalyticsEvent definitions.
let tapType = (userInfo["notification_type"] as? String) ?? "unknown"
var tapParams: [String: Any] = ["type": tapType]
if let launchId = (userInfo["launch_uuid"] as? String) ?? (userInfo["launch_id"] as? String) {
    tapParams["launch_id"] = launchId
}
Analytics.logEvent("notification_tapped", parameters: tapParams)
```

- [ ] **Step 3: Track shown inside scheduleNotification**

`scheduleNotification` adds a `UNNotificationRequest` via `center.add(...)`. In its completion handler (or immediately after a success path if it has no completion), when there is **no error**:

```swift
let shownType = (content.userInfo["notification_type"] as? String) ?? "unknown"
Analytics.logEvent("notification_shown", parameters: [
    "type": shownType,
    "platform": "ios",
])
```

Read the actual function body first — if `center.add` is called with a completion closure, put the log inside it guarded by `error == nil`; if fire-and-forget, add a completion closure. Known limitation to note in the code: notifications rendered by the NotificationServiceExtension (alert-type pushes) do not pass through this path and are not counted; the data-only → local-schedule path is the primary iOS display path.

- [ ] **Step 4: Verification (limited on Windows)**

Swift cannot compile here. Self-check: event names and param keys exactly match the Kotlin definitions (`notification_tapped`: `type`, `launch_id`; `notification_shown`: `type`, `platform`). Xcode build on the Mac before release; if `FirebaseAnalytics` isn't attached to the iosApp target despite appearing in the project, attach the SPM product in Xcode (General → Frameworks).

- [ ] **Step 5: Stage commit message** (see Task 10 Step 4 — single tapped commit preferred)

---

### Task 12: Full verification + Caleb's testing checkpoint

- [ ] **Step 1: Full local gate**

Run, in order:
1. `./gradlew :composeApp:jvmTest` — all common tests pass
2. `./gradlew compileKotlinDesktop` — commonMain compiles
3. `./gradlew :composeApp:compileDebugKotlinAndroid` — androidMain compiles
4. `./gradlew ktlintCheck` — formatting (soft-fail in CI, still fix what it flags in touched files)

Expected: all PASS. Fix anything that fails before proceeding.

- [ ] **Step 2: Build an installable debug APK for Caleb**

Run: `./gradlew installDebug` (device attached) or `./gradlew :composeApp:assembleDebug` and report the APK path.

- [ ] **Step 3: STOP — hand to Caleb with this manual test script**

1. **Rewarded ad:** Support Us screen → "Watch Ad for 24h Premium Access" → button shows "Loading ad…" → test ad displays (debug uses AdMob test unit IDs) → watch to completion → temporary access granted, card shows time remaining. Repeat and dismiss the ad early → button resets, no reward, no re-show loop.
2. **Purchase failure event:** start a purchase, cancel the Play sheet → logcat (`adb logcat | grep -i "purchase failed"`) shows `store_purchase/user_cancelled`; DebugView (`adb shell setprop debug.firebase.analytics.app me.calebjones.spacelaunchnow.kmpdebug`) shows `purchase_failed`.
3. **Purchase revenue:** complete a test purchase (or verify in DebugView param list) → `purchase_completed` carries `revenue`.
4. **Notifications:** send a test FCM notification (debug menu) → DebugView shows `notification_shown`; tap it → `notification_tapped` with correct `type`.

- [ ] **Step 4: After Caleb confirms — commit in order (conventional messages staged per task):**

1. `fix(ads): load rewarded ads on demand instead of dead preloaded handles` (Tasks 1–3)
2. `feat(ui): show loading state while rewarded ad loads on demand` (Task 4)
3. `chore(ads): delete unused preloaded interstitial/rewarded CompositionLocals` (Task 5)
4. `feat(analytics): attach revenue to purchase_completed` (Task 6)
5. `feat(analytics): add purchase_failed event with step and error code` (Task 7)
6. `feat(analytics): add notification_shown display-time event` (Tasks 8–9)
7. `feat(analytics): fire notification_tapped on Android and iOS` (Tasks 10–11)

No Claude co-author. iOS release train (backlog item #3) then ships these together with the already-on-main interstitial fix.
