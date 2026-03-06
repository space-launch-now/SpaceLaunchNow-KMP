# Ad Lifecycle Contract: Compliant Loading Order

**Date**: 2026-03-04  
**Purpose**: Define the expected ad lifecycle contract for Google AdMob policy compliance

---

## Contract: Ad Initialization and Loading Order

### Preconditions

1. App has launched and `Activity`/`ViewController` is available
2. Network connectivity exists (or graceful fallback)
3. AdMob App ID is correctly configured in manifest/Info.plist

### Guaranteed Execution Order

```
STEP 1: SDK Initialization (MAIN THREAD ONLY)
  ├── Input: Activity context (Android) or nil (iOS)
  ├── Call: BasicAds.initialize(context)
  ├── Thread: Main / UI thread
  ├── Postcondition: isInitialized == true
  └── Error: Log and set isInitialized = false

STEP 2: SDK Configuration (MAIN THREAD)
  ├── Input: isDebug, testDeviceIds
  ├── Call: BasicAds.configuration = RequestConfiguration(...)
  ├── Configuration MUST be identical on Android and iOS:
  │   ├── maxAdContentRating = MAX_AD_CONTENT_RATING_PG
  │   ├── tagForChildDirectedTreatment = FALSE
  │   ├── tagForUnderAgeOfConsent = FALSE
  │   └── publisherPrivacyPersonalizationState = ENABLED
  └── Postcondition: isConfigured == true

STEP 3: Consent Check (ASYNC, BLOCKS AD LOADING)
  ├── Input: Activity (Android) or ViewController (iOS)
  ├── Call: rememberConsent(activity) → ConsentPopup()
  ├── Wait: User responds or consent not required
  ├── Output: AdConsentStatus (OBTAINED | DENIED | NOT_REQUIRED | ERROR)
  └── Postcondition: canLoadAds == true (for OBTAINED, DENIED, NOT_REQUIRED)

STEP 4: Ad Preloading (ONLY AFTER CONSENT RESOLVED)
  ├── Gate: canLoadAds == true
  ├── Personalization: canLoadPersonalizedAds controls NPA flag
  ├── Load:
  │   ├── rememberBannerAd() x4 (BANNER, LARGE_BANNER, MEDIUM_RECTANGLE, NAVIGATION)
  │   ├── rememberInterstitialAd() x1
  │   └── rememberRewardedAd() x1
  └── Provide via CompositionLocalProvider

STEP 5: Ad Display (GATED BY PREMIUM + FREQUENCY)
  ├── Banner: Check PremiumFeature.AD_FREE → show if free user
  ├── Interstitial: Check premium + frequency cap (every 10th visit, 5min interval)
  └── Rewarded: Check premium + user trigger + single reward grant
```

### Postconditions

1. No ad request is made before consent is resolved
2. SDK is initialized on the main thread
3. Content rating is identical on Android and iOS
4. Interstitial ads respect frequency caps
5. Rewarded ads grant exactly one reward per ad view
6. Premium users never see ads

---

## Contract: AndroidManifest App ID

### Current (NON-COMPLIANT)

```xml
<!-- WRONG: Test App ID in all builds -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713" />
```

### Target (COMPLIANT)

```xml
<!-- CORRECT: Production App ID via manifestPlaceholder -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="${ADMOB_APP_ID}" />
```

```kotlin
// build.gradle.kts
defaultConfig {
    // Existing...
    manifestPlaceholders["ADMOB_APP_ID"] = admobAppId  // From .env
}

buildTypes {
    getByName("debug") {
        // Override with test App ID for debug builds
        manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
    }
}
```

```env
# .env (production)
ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
```

---

## Contract: Content Rating Configuration

### Unified Configuration (Both Platforms)

```kotlin
// MUST be identical in AdInitializer.android.kt AND AdInitializer.ios.kt
BasicAds.configuration = RequestConfiguration(
    maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_PG,
    publisherPrivacyPersonalizationState = RequestConfiguration.PublisherPrivacyPersonalizationState.ENABLED,
    tagForChildDirectedTreatment = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE,
    tagForUnderAgeOfConsent = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE,
    testDeviceIds = testDeviceIds
)
```

---

## Contract: Rewarded Ad Single Reward

### Current (BUGGY)

```kotlin
// TWO paths can invoke onRewardEarned:
// Path 1: LaunchedEffect (GUARDED)
AdState.SHOWN -> {
    if (!rewardGranted) {
        onRewardEarned?.invoke(1, "reward")
        rewardGranted = true
    }
}

// Path 2: RewardedAd callback (UNGUARDED)
RewardedAd(
    loadedAd = rewardedAd,
    onRewardEarned = {
        onRewardEarned?.invoke(1, "reward") // ← DUPLICATE!
    }
)
```

### Target (CORRECT)

```kotlin
// SINGLE path with guard:
RewardedAd(
    loadedAd = rewardedAd,
    onRewardEarned = {
        if (!rewardGranted) {
            onRewardEarned?.invoke(1, "reward")
            rewardGranted = true
        }
    }
)

// LaunchedEffect ONLY tracks state, does NOT grant reward
AdState.SHOWN -> {
    log.d { "Ad has finished showing" }
    // Reward already granted via onRewardEarned callback
}
```

---

## Contract: Interstitial Frequency Capping

### Configuration

| Parameter | Value | Policy Basis |
|-----------|-------|-------------|
| `visitsBeforeInterstitial` | 10 | Google recommends clear content between interstitials |
| `minInterstitialInterval` | 300,000ms (5 min) | Google policy: no overlapping interstitials |
| Premium check | 3-layer gate: `hasAdFree`, `isLoading`, `isSubscribed` | Business rule |

### Compliance Notes
- Current frequency (every 10 visits, 5 min gap) is **compliant**
- Code comment should say "every 10th visit" not "every 4th visit"
