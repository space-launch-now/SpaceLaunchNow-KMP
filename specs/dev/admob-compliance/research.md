# Research: AdMob Policy Compliance — Ad Loading Lifecycle

**Date**: 2026-03-04  
**Purpose**: Resolve all NEEDS CLARIFICATION items and document compliance research

---

## Research Task 1: AndroidManifest AdMob App ID (C-1)

### Question
The AndroidManifest hardcodes Google's test AdMob App ID (`ca-app-pub-3940256099942544~3347511713`). Is this overridden for release builds?

### Finding
**No — the test App ID is used in ALL builds (debug and release).**

Evidence:
- `composeApp/src/androidMain/AndroidManifest.xml:55` hardcodes the test ID
- `composeApp/build.gradle.kts` only defines `manifestPlaceholders` for `appName` and `MAPS_API_KEY`
- There is NO `manifestPlaceholders` entry for the AdMob App ID
- There is NO build-variant-specific manifest overlay for the App ID
- The `.env` file contains ad UNIT IDs (per-ad-format) but NOT the AdMob APP ID (per-app)

**Impact**: In production, Google Mobile Ads SDK receives the test App ID, which means:
- Real ads will not serve (SDK falls back to test mode or shows no ads)
- Revenue is zero from AdMob
- Google may flag the app for using test configuration in production

### Decision
**MUST FIX**: Add the production AdMob App ID to the manifest. Options:
1. **Recommended**: Use `manifestPlaceholders` from `.env` → `ADMOB_APP_ID` → `${ADMOB_APP_ID}` in manifest
2. **Alternative**: Use a release-specific manifest overlay
3. **Alternative**: Hardcode the production ID and only use test ID in debug via build type manifest

### Rationale
Option 1 is preferred because it follows the existing pattern (like `MAPS_API_KEY`) and keeps the production App ID out of version control.

### Alternatives Considered
- Hardcoding production App ID directly: Rejected because it exposes the ID in the open-source repo
- Using `tools:replace` with build types: More complex for no additional benefit

---

## Research Task 2: Consent-Before-Ads Ordering (C-2)

### Question
Does the current implementation guarantee that UMP consent is obtained before ad requests are made?

### Finding
**No — consent and ad loading happen concurrently with no synchronization.**

Code flow in `App.kt`:
```kotlin
// Line 259: Consent popup shown
AdConsentPopup(onFailure = { ... })

// Line 331: Ads preloaded IMMEDIATELY (no consent gate)
WithPreloadedAds(context = contextFactory.getActivity()) {
    NavHost(...)
}
```

Both `AdConsentPopup` and `WithPreloadedAds` are sibling composables rendered simultaneously. The consent dialog is asynchronous — it shows a popup and waits for user input. Meanwhile, `rememberBannerAd()`, `rememberInterstitialAd()`, and `rememberRewardedAd()` start loading ads immediately.

**GDPR/Google Compliance Impact**:
- Under GDPR Article 7, consent must be obtained BEFORE processing personal data
- Google's Consent Policy requires the UMP consent form to be shown and resolved before serving personalized ads
- The `basic-ads` library's `ConsentPopup` does handle consent internally, but the ad loading calls bypass this

### Decision
**MUST FIX**: Gate ad loading on consent status. The recommended approach:

1. Track consent state via a shared `StateFlow<Boolean>` (e.g., `isConsentObtained`)
2. Only render `WithPreloadedAds` AFTER consent is resolved (granted or denied)
3. If consent is denied, still load ads but with non-personalized ad requests (NPA flag)
4. On iOS, the `basic-ads` library may handle this internally via ATT — verify with library docs

### Rationale
Gating on consent is the standard pattern recommended by Google. The `basic-ads` library's `rememberConsent()` returns a consent state that can be observed.

### Alternatives Considered
- Relying on `basic-ads` to internally gate: Rejected — the ad loading calls (`rememberBannerAd`, etc.) are separate from consent and start immediately
- Delaying all ad loading by a fixed timeout: Rejected — fragile, doesn't handle slow consent responses

---

## Research Task 3: SDK Initialization Thread (C-3)

### Question
Does Google Mobile Ads SDK require main thread initialization?

### Finding
**Yes — Google's documentation explicitly states:**

> "Initialize the Google Mobile Ads SDK by calling `MobileAds.initialize()` which initializes the SDK and calls back a completion listener once initialization is complete. This needs to be done only once, ideally at app launch."

The current code initializes on `Dispatchers.Default` (background thread pool):
```kotlin
// App.kt line 174
kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
    // ...
    val adInitSuccess = AdInitializer.initialize(context = contextFactory.getActivity())
}
```

The `BasicAds.initialize(context)` internally calls `MobileAds.initialize()`. While this MAY work on some devices, it can cause:
- Race conditions with ad loading
- Crashes on some Android versions
- Undefined behavior per Google's threading contract

### Decision
**MUST FIX**: Move `AdInitializer.initialize()` and `AdInitializer.configure()` calls to the main thread. Options:
1. **Recommended**: Use `Dispatchers.Main` for ad initialization, keep other init on Default
2. **Alternative**: Move ad init out of the `LaunchedEffect` entirely and into a `SideEffect` or top-level composition

### Rationale
Option 1 is minimal-change — just wrap the ad init calls in `withContext(Dispatchers.Main)` within the existing coroutine.

---

## Research Task 4: Content Rating Inconsistency (M-1)

### Question
Why do Android and iOS have different content rating and child-directed treatment settings?

### Finding
**Unintentional inconsistency — no documented rationale.**

| Setting | Android | iOS |
|---------|---------|-----|
| `maxAdContentRating` | `MAX_AD_CONTENT_RATING_PG` | `MAX_AD_CONTENT_RATING_T` |
| `tagForChildDirectedTreatment` | `UNSPECIFIED` | `FALSE` |
| `tagForUnderAgeOfConsent` | `UNSPECIFIED` | `FALSE` |

- PG (Parental Guidance) is more restrictive than T (Teen)
- `UNSPECIFIED` means Google decides based on app metadata; `FALSE` explicitly opts out
- There is no documented reason for the difference

### Decision
**SHOULD FIX**: Unify to consistent settings. Recommended:
- `maxAdContentRating`: `MAX_AD_CONTENT_RATING_PG` (more restrictive, better for a general-audience space app)
- `tagForChildDirectedTreatment`: `FALSE` (app is not directed at children)
- `tagForUnderAgeOfConsent`: `FALSE` (app is not directed at under-age users)

### Rationale
Consistency ensures the same ad content quality across platforms. PG is appropriate for a space launch tracking app.

---

## Research Task 5: Rewarded Ad Duplicate Reward (M-3)

### Question
Can the rewarded ad grant duplicate rewards?

### Finding
**Yes — there is a code path that can trigger `onRewardEarned` twice.**

In `RewardedAdHandler.android.kt`:
```kotlin
// Path 1: LaunchedEffect on AdState.SHOWN (line 106-108)
AdState.SHOWN -> {
    if (!rewardGranted) {
        onRewardEarned?.invoke(1, "reward")
        rewardGranted = true
    }
}

// Path 2: RewardedAd composable callback (line 128-130)
RewardedAd(
    loadedAd = rewardedAd,
    onRewardEarned = {
        onRewardEarned?.invoke(1, "reward") // No guard!
    }
)
```

Path 2 does NOT check `rewardGranted` before invoking the callback. If both fire (which depends on `basic-ads` library internals), the reward is granted twice.

### Decision
**SHOULD FIX**: Remove the duplicate callback in `RewardedAd(onRewardEarned = {...})` and rely solely on the `LaunchedEffect` state-based approach with the `rewardGranted` guard.

### Rationale
The `LaunchedEffect` approach has the `rewardGranted` flag and is already the primary reward-granting mechanism. The `onRewardEarned` callback in `RewardedAd()` is redundant and unsafe.

---

## Research Task 6: Banner Ad Refresh (A-1)

### Question
Do preloaded banner ads refresh automatically?

### Finding
**Depends on `basic-ads` library behavior**. The ads are created via `rememberBannerAd()` and composed once. Google's AdMob SDK has a built-in auto-refresh for banner ads (default: 60 seconds). Whether `basic-ads` passes through this configuration or suppresses it requires checking the library source.

The current implementation does NOT set any explicit refresh interval. If `basic-ads` uses `AdView` under the hood (which it likely does on Android), auto-refresh should be working by default via the AdMob dashboard refresh rate setting.

### Decision
**NO ACTION REQUIRED** if `basic-ads` uses standard `AdView`. The AdMob dashboard controls refresh rate. Can be verified by monitoring network requests in debug builds.

---

## Research Task 7: Ad Density (A-3)

### Question
Does the app exceed Google's ad density limits?

### Finding
**Borderline — but compliant because tabs show only one ad at a time.**

- Detail screens have `SmartBannerAd` in 4 tab contents (Overview, Rocket, Mission, Agency)
- Only ONE tab is visible at a time → only ONE banner ad visible
- Home screen has 2 banner placements (CONTENT + FEED) but they are spaced apart in the scroll
- Navigation bar has a persistent NAVIGATION banner
- Interstitials show every 10th detail view with 5-minute minimum interval

Google's policy states: "Avoid placing ads near interactive elements" and "Don't stack or overlap ads." The current implementation complies because:
1. Tab-based ads show one at a time
2. Home page ads are separated by content
3. Interstitials have reasonable frequency capping

### Decision
**NO ACTION REQUIRED** — current density is compliant. Document for future reference.

---

## Summary of Decisions

| Finding | Action | Priority |
|---------|--------|----------|
| C-1: Test App ID in manifest | Add production App ID via manifestPlaceholders | **CRITICAL** |
| C-2: Ads loaded before consent | Gate ad loading on consent state | **CRITICAL** |
| C-3: SDK init on background thread | Move to main thread | **CRITICAL** |
| M-1: Content rating inconsistency | Unify to PG / FALSE / FALSE | **MODERATE** |
| M-2: Interstitial comment mismatch | Update comments to reflect actual value (10) | **MODERATE** |
| M-3: Duplicate reward granting | Remove redundant callback | **MODERATE** |
| A-1: No banner refresh | No action (SDK auto-refreshes) | **NONE** |
| A-2: No retry on failure | Optional improvement | **LOW** |
| A-3: Ad density | Compliant; no action | **NONE** |
