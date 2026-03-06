# Implementation Plan: AdMob Policy Compliance Audit

**Branch**: `dev` | **Date**: 2026-03-04 | **Spec**: [specs/dev/admob-compliance/spec.md](spec.md)
**Input**: Ad loading lifecycle compliance analysis

## Summary

Comprehensive audit of the SpaceLaunchNow KMP ad loading lifecycle against Google AdMob policies. The analysis identified **3 critical**, **3 moderate**, and **3 minor** compliance issues across SDK initialization, consent flow timing, manifest configuration, content rating consistency, ad density, interstitial frequency, and rewarded ad reward-granting. This plan documents findings and provides a remediation roadmap.

## Technical Context

**Language/Version**: Kotlin 2.0.21, Swift 5.9, Java 21  
**Primary Dependencies**: basic-ads 0.2.7 (LexiLabs KMP wrapper), Google Mobile Ads SDK 23.6.0, Google UMP 3.1.0  
**Storage**: N/A (ad state is in-memory only)  
**Testing**: JUnit5 + kotlinx-coroutines-test, XCTest  
**Target Platform**: Android 8.0+ (API 26), iOS 15+, Desktop (no-op stubs)  
**Project Type**: Mobile (KMP — Android, iOS, Desktop)  
**Performance Goals**: Ad load < 2s, no UI jank during ad loading  
**Constraints**: Must comply with Google AdMob policies, GDPR, CCPA; must not show ads to premium subscribers  
**Scale/Scope**: ~55 ad-related source files, 14 screens with banner ads, 5 screens with interstitials, 1 rewarded ad flow

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Phase 0 Check (PASSED with NOTES)

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal Priority) | ✅ PASS | Both platforms have full ad implementations |
| II. Pattern-Based Consistency | ⚠️ NOTE | Content rating config differs between Android (PG) and iOS (T) — inconsistent pattern |
| III. Accessibility & User Experience | ✅ PASS | Ads have proper content descriptions; "Remove Ads" button available |
| IV. CI/CD & Conventional Commits | ✅ PASS | No deployment changes; commits follow format |
| V. Code Generation & API Management | ✅ N/A | No API spec changes |
| VI. Multiplatform Architecture | ✅ PASS | expect/actual pattern used correctly for ads |
| VII. Testing Standards | ⚠️ NOTE | No unit tests exist for ad lifecycle logic (consent ordering, frequency capping) |

### Post-Phase 1 Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First | ✅ PASS | Remediation plan covers both Android and iOS equally |
| II. Pattern-Based | ✅ PASS | Proposed fixes unify content rating across platforms |
| III. Accessibility | ✅ PASS | No accessibility regressions in proposed changes |
| IV. CI/CD | ✅ PASS | Manifest fix requires build config change; conventional commit required |
| V. Code Gen | ✅ N/A | No OpenAPI changes |
| VI. Multiplatform | ✅ PASS | Consent gate logic goes in commonMain; platform actuals updated |
| VII. Testing | ✅ PASS | Remediation includes test requirements for consent ordering and frequency capping |

## Project Structure

### Documentation (this feature)

```text
specs/dev/admob-compliance/
├── plan.md              # This file
├── research.md          # Phase 0: Compliance findings
├── data-model.md        # Phase 1: Ad lifecycle state model
├── quickstart.md        # Phase 1: Remediation quickstart
└── contracts/           # Phase 1: Expected ad lifecycle contracts
    └── ad-lifecycle-contract.md
```

### Source Code (affected files)

```text
composeApp/
├── src/
│   ├── commonMain/kotlin/.../
│   │   ├── App.kt                          # Ad SDK init + composition order
│   │   └── ui/ads/
│   │       ├── AdComposables.kt            # Common expect declarations
│   │       ├── AdInitializer.kt            # SDK initialization
│   │       └── GlobalAdManager.kt          # Frequency capping
│   ├── androidMain/
│   │   ├── AndroidManifest.xml             # AdMob App ID (CRITICAL)
│   │   └── kotlin/.../ui/ads/
│   │       ├── AdInitializer.android.kt    # Android SDK init
│   │       ├── AdSupport.android.kt        # Consent + preloading
│   │       ├── GlobalAdManager.android.kt  # Frequency config
│   │       ├── InterstitialAdHandler.android.kt
│   │       ├── RewardedAdHandler.android.kt
│   │       └── SmartBannerAd.android.kt
│   └── iosMain/kotlin/.../ui/ads/
│       ├── AdInitializer.ios.kt            # iOS SDK init
│       ├── AdSupport.ios.kt                # Consent + preloading
│       └── GlobalAdManager.ios.kt          # Frequency config
```

**Structure Decision**: Existing multiplatform architecture with expect/actual pattern. No structural changes needed — fixes are in-place modifications.

## Compliance Findings

### CRITICAL (Must Fix)

| ID | Finding | Policy Violated | Location |
|----|---------|-----------------|----------|
| C-1 | AndroidManifest hardcodes **test** AdMob App ID (`ca-app-pub-3940256099942544~3347511713`) for ALL build types including release | [AdMob App ID requirement](https://developers.google.com/admob/android/quick-start#update_your_androidmanifest.xml) | `AndroidManifest.xml:55` |
| C-2 | Ads preloaded **before** UMP consent is obtained — `WithPreloadedAds` runs concurrently with `AdConsentPopup`, no synchronization | GDPR Art. 7, [Google Consent Policy](https://support.google.com/admob/answer/10115027) | `App.kt:259,331` |
| C-3 | Ad SDK initialized on **background thread** (`Dispatchers.Default`) — Google requires main thread initialization | [Google Mobile Ads SDK docs](https://developers.google.com/admob/android/quick-start#initialize_the_mobile_ads_sdk) | `App.kt:199` |

### MODERATE (Should Fix)

| ID | Finding | Policy Concern | Location |
|----|---------|----------------|----------|
| M-1 | Content rating inconsistency: Android=PG, iOS=T(Teen); child-directed flags also differ | Inconsistent content filtering could serve inappropriate ads on one platform | `AdInitializer.android.kt:44` vs `AdInitializer.ios.kt:43` |
| M-2 | Interstitial comment says "every 4th visit" but code uses `visitsBeforeInterstitial = 10` | Code/documentation mismatch; actual frequency is compliant but misleading | `InterstitialAdHandler.android.kt:33` + `GlobalAdManager.android.kt:49` |
| M-3 | Rewarded ad potentially grants **duplicate rewards** — `onRewardEarned` called in both `LaunchedEffect(AdState.SHOWN)` AND `RewardedAd(onRewardEarned = {...})` | Unintended premium access grants | `RewardedAdHandler.android.kt:106-108,128-130` |

### MINOR / ADVISORY

| ID | Finding | Note | Location |
|----|---------|------|----------|
| A-1 | No banner ad refresh mechanism — ads preloaded once and reused indefinitely | Google recommends 30-120s refresh for revenue optimization | `AdSupport.android.kt:78-92` |
| A-2 | No ad load failure retry strategy — failed ads silently disappear | Reduces fill rate; not a policy violation | `SmartBannerAd.android.kt:231` |
| A-3 | Detail screen tabs each have their own `SmartBannerAd` — up to 4 banner ads when scrolling between tabs | Borderline ad density; tabs mitigate since only one visible at a time | `OverviewTabContent.kt`, `RocketTabContent.kt`, `MissionTabContent.kt`, `AgencyTabContent.kt` |
