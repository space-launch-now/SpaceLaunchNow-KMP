# Data Model: Ad Lifecycle State

**Date**: 2026-03-04  
**Purpose**: Define the ad lifecycle state model for compliance remediation

---

## Entities

### 1. AdConsentState

Tracks whether user consent has been obtained before ad loading begins.

```kotlin
enum class AdConsentStatus {
    UNKNOWN,        // App just launched, consent not yet checked
    REQUIRED,       // Consent form needs to be shown
    OBTAINED,       // User granted consent (personalized ads OK)
    DENIED,         // User denied consent (non-personalized ads only)
    NOT_REQUIRED,   // Region doesn't require consent (e.g., US without CCPA)
    ERROR           // Consent check failed
}

data class AdConsentState(
    val status: AdConsentStatus = AdConsentStatus.UNKNOWN,
    val canLoadAds: Boolean = false,          // True when status is OBTAINED, DENIED, or NOT_REQUIRED
    val canLoadPersonalizedAds: Boolean = false, // True only when OBTAINED or NOT_REQUIRED
    val timestamp: Long = 0L
)
```

**Validation Rules**:
- `canLoadAds` is `true` only when `status` is `OBTAINED`, `DENIED`, or `NOT_REQUIRED`
- `canLoadPersonalizedAds` is `true` only when `status` is `OBTAINED` or `NOT_REQUIRED`
- Ads MUST NOT load while `status` is `UNKNOWN`, `REQUIRED`, or `ERROR`

### 2. AdInitializationState

Tracks SDK initialization status and thread compliance.

```kotlin
data class AdInitializationState(
    val isInitialized: Boolean = false,
    val isConfigured: Boolean = false,
    val initializedOnMainThread: Boolean = false,
    val error: String? = null
)
```

**Validation Rules**:
- `initializedOnMainThread` must be `true` for compliance
- `isConfigured` requires `isInitialized` to be `true` first

### 3. AdConfiguration

Unified content rating configuration across platforms.

```kotlin
data class AdContentRatingConfig(
    val maxAdContentRating: String,              // Must be same on Android & iOS
    val tagForChildDirectedTreatment: String,   // Must be same on Android & iOS
    val tagForUnderAgeOfConsent: String          // Must be same on Android & iOS
)

// Canonical configuration (single source of truth)
object AdContentRating {
    val DEFAULT = AdContentRatingConfig(
        maxAdContentRating = "PG",           // Parental Guidance — suitable for general audience
        tagForChildDirectedTreatment = "FALSE", // App is NOT directed at children
        tagForUnderAgeOfConsent = "FALSE"       // App is NOT for under-age users
    )
}
```

### 4. InterstitialFrequencyState

Tracks interstitial ad frequency capping.

```kotlin
data class InterstitialFrequencyState(
    val visitCount: Int = 0,
    val lastShownTimestamp: Long = 0L,
    val visitsBeforeShow: Int = 10,          // Show every Nth visit
    val minIntervalMs: Long = 300_000L       // 5 minutes minimum between shows
) {
    val shouldShow: Boolean
        get() = (visitCount % visitsBeforeShow == 0) &&
                (System.currentTimeMillis() - lastShownTimestamp >= minIntervalMs)
}
```

### 5. RewardState

Tracks rewarded ad reward granting to prevent duplicates.

```kotlin
data class RewardState(
    val isGranted: Boolean = false,
    val grantTimestamp: Long = 0L,
    val rewardAmount: Int = 0,
    val rewardType: String = ""
)
```

**Validation Rules**:
- `isGranted` can only transition from `false` → `true`, never back
- A new `RewardState` must be created for each new rewarded ad session

---

## State Transitions

### Ad Loading Lifecycle (Compliant)

```
START
  │
  ▼
[SDK Init on Main Thread]
  │
  ▼
[Check Consent Status]
  │
  ├── UNKNOWN → Show Consent Form → OBTAINED/DENIED
  ├── NOT_REQUIRED → Proceed
  └── ERROR → Retry or load non-personalized
  │
  ▼
[canLoadAds == true]
  │
  ▼
[Preload Ads]
  ├── Banner (x4 sizes)
  ├── Interstitial (x1)
  └── Rewarded (x1)
  │
  ▼
[Display Ads with Premium Gating]
  ├── Check PremiumFeature.AD_FREE
  ├── Check interstitial frequency
  └── Render or suppress
```

### Current vs. Compliant Flow

```
CURRENT (NON-COMPLIANT):
  App.kt LaunchedEffect → SDK Init (BG thread) ─┐
  App.kt Composition → AdConsentPopup() ──────────┤── CONCURRENT (no ordering)
  App.kt Composition → WithPreloadedAds() ────────┘

COMPLIANT (TARGET):
  App.kt Composition → SDK Init (Main thread)
                          │
                          ▼
                     AdConsentPopup()
                          │
                          ▼ (consent resolved)
                     WithPreloadedAds()
                          │
                          ▼
                     NavHost (content)
```

---

## Relationships

```
AdConsentState ──controls──▶ WithPreloadedAds (gate)
AdInitializationState ──precedes──▶ AdConsentState
AdConfiguration ──configures──▶ AdInitializer (both platforms)
InterstitialFrequencyState ──gates──▶ InterstitialAdHandler
RewardState ──guards──▶ RewardedAdHandler (single grant)
PremiumFeature.AD_FREE ──suppresses──▶ All ad composables
```
