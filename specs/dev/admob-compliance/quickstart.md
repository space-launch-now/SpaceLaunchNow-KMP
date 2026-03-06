# Quickstart: AdMob Compliance Remediation

**Date**: 2026-03-04  
**Purpose**: Step-by-step remediation guide for ad loading lifecycle compliance issues

---

## Prerequisites

- Java 21 (JetBrains JDK)
- `.env` file with ad unit IDs
- Access to AdMob console for production App ID

---

## Fix 1: AndroidManifest App ID (CRITICAL — C-1)

### Steps

1. **Add `ADMOB_APP_ID` to `.env`**:
   ```env
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
   ```

2. **Read it in `build.gradle.kts`** (after existing env reads, ~line 340):
   ```kotlin
   val admobAppId = envProps.getProperty("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"
   ```

3. **Add manifest placeholder** (in `defaultConfig`, ~line 385):
   ```kotlin
   manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
   ```

4. **Override for debug** (in `buildTypes.debug`, ~line 422):
   ```kotlin
   manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
   ```

5. **Update AndroidManifest.xml** (~line 55):
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="${ADMOB_APP_ID}" />
   ```

6. **Update CI/CD secrets** — add `ADMOB_APP_ID` to GitHub Actions secrets and `.env` generation

### Verification
```bash
./gradlew assembleRelease
# Check merged manifest for correct App ID:
# build/intermediates/merged_manifests/release/AndroidManifest.xml
```

---

## Fix 2: Consent-Before-Ads Gate (CRITICAL — C-2)

### Steps

1. **In `App.kt`, add consent state tracking** (~line 255):
   ```kotlin
   var isConsentResolved by remember { mutableStateOf(false) }
   ```

2. **Modify `AdConsentPopup` to report resolution**:
   ```kotlin
   AdConsentPopup(
       onFailure = { log.w(it) { "Consent popup failure" } },
       onConsentResolved = { isConsentResolved = true }
   )
   ```

3. **Gate `WithPreloadedAds` on consent**:
   ```kotlin
   if (isConsentResolved) {
       WithPreloadedAds(context = contextFactory.getActivity()) {
           // NavHost content
       }
   } else {
       // Show content without ads while consent is pending
       // NavHost content (no ads)
   }
   ```

4. **Update `AdConsentPopup` expect/actual** to add `onConsentResolved` parameter

5. **In Android actual**: Call `onConsentResolved` when consent status changes from UNKNOWN

6. **In iOS actual**: Call `onConsentResolved` after consent popup completes or when not required

### Alternative (Simpler)
If `basic-ads` library's `rememberConsent()` returns state that indicates resolution, observe that state directly instead of adding a callback.

### Verification
- Enable debug logging
- Launch app fresh (clear data)
- Verify: consent dialog appears BEFORE any ad network requests in Logcat
- Use Charles Proxy or Network Inspector to confirm no AdMob requests before consent

---

## Fix 3: SDK Init on Main Thread (CRITICAL — C-3)

### Steps

1. **In `App.kt`, move ad init to main thread** (~line 199):
   ```kotlin
   // Inside the LaunchedEffect, wrap ad init in Main context
   kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
       val adInitSuccess = AdInitializer.initialize(context = contextFactory.getActivity())
       if (adInitSuccess) {
           AdInitializer.configure(BuildConfig.IS_DEBUG, testDeviceIds)
       }
   }
   ```

2. **Keep other initialization on Default** — only the ad SDK calls need Main thread

### Verification
- Add a thread assertion in `AdInitializer.android.kt`:
  ```kotlin
  check(Looper.myLooper() == Looper.getMainLooper()) { "Must initialize on main thread" }
  ```
- Run app and verify no crash

---

## Fix 4: Content Rating Unification (MODERATE — M-1)

### Steps

1. **Update `AdInitializer.ios.kt`** (~line 43):
   ```kotlin
   BasicAds.configuration = RequestConfiguration(
       maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_PG,  // Was: T
       publisherPrivacyPersonalizationState = RequestConfiguration.PublisherPrivacyPersonalizationState.ENABLED,
       tagForChildDirectedTreatment = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE,  // Was: FALSE (OK)
       tagForUnderAgeOfConsent = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE,  // Was: FALSE (OK)
       testDeviceIds = testDeviceIds
   )
   ```

2. **Update `AdInitializer.android.kt`** (~line 44):
   ```kotlin
   BasicAds.configuration = RequestConfiguration(
       maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_PG,  // Already PG ✓
       publisherPrivacyPersonalizationState = RequestConfiguration.PublisherPrivacyPersonalizationState.ENABLED,
       tagForChildDirectedTreatment = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE,  // Was: UNSPECIFIED
       tagForUnderAgeOfConsent = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE,  // Was: UNSPECIFIED
       testDeviceIds = testDeviceIds
   )
   ```

### Verification
- Build and run on both platforms
- Verify ad content is age-appropriate (PG rating)

---

## Fix 5: Interstitial Comment Fix (MODERATE — M-2)

### Steps

1. **Update comment in `InterstitialAdHandler.android.kt`** (~line 33):
   Change: "every 4th detail view visit" → "every 10th detail view visit"

2. **Update comment in `InterstitialAdHandler.ios.kt`** similarly

### Verification
- Code review only

---

## Fix 6: Duplicate Reward Fix (MODERATE — M-3)

### Steps

1. **In `RewardedAdHandler.android.kt`** (~line 128):
   Add guard to the `RewardedAd` callback:
   ```kotlin
   RewardedAd(
       loadedAd = rewardedAd,
       onRewardEarned = {
           if (!rewardGranted) {
               log.d { "User earned reward (via callback)!" }
               onRewardEarned?.invoke(1, "reward")
               rewardGranted = true
           }
       }
   )
   ```

2. **Remove duplicate in `LaunchedEffect`** (~line 106):
   ```kotlin
   AdState.SHOWN -> {
       log.d { "Ad has finished showing" }
       // Reward granted via onRewardEarned callback — no duplicate call here
   }
   ```

3. **Apply same fix to `RewardedAdHandler.ios.kt`**

### Verification
- Set a breakpoint on `onRewardEarned`
- Watch a rewarded ad to completion
- Verify callback fires exactly once

---

## Testing Checklist

| Test | Expected |
|------|----------|
| Fresh install → consent dialog appears before any ads | ✅ |
| Consent denied → ads still load (non-personalized) | ✅ |
| Release build → real ads serve (not test ads) | ✅ |
| Debug build → test ads serve | ✅ |
| Premium user → no ads visible anywhere | ✅ |
| 10th detail view → interstitial shows | ✅ |
| Rapid detail views (< 5 min) → interstitial blocked | ✅ |
| Rewarded ad → reward granted exactly once | ✅ |
| Both platforms → same content rating (PG) | ✅ |
