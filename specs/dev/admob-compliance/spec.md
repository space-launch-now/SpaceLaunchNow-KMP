# AdMob Policy Compliance Audit — Ad Loading Lifecycle

**Branch**: `dev`  
**Date**: 2026-03-04  
**Author**: Copilot Agent

## Overview

Audit the complete ad loading lifecycle in SpaceLaunchNow KMP to verify compliance with Google AdMob policies. The app uses the `basic-ads` (LexiLabs) KMP wrapper around Google Mobile Ads SDK, serving banner, interstitial, and rewarded ads on Android and iOS.

## Scope

### In Scope

1. SDK initialization order and thread safety
2. UMP consent flow timing relative to ad loading
3. Ad unit ID configuration (test vs. production)
4. Content rating and child-directed treatment configuration
5. Ad density / placement policy compliance
6. Interstitial ad frequency capping and user-experience compliance
7. Rewarded ad reward-granting lifecycle
8. Banner ad refresh behavior
9. Platform parity (Android vs. iOS configuration consistency)

### Out of Scope

- Revenue optimization strategy
- A/B testing of ad placements
- SKAdNetwork attribution (covered in spec 002)
- Premium/subscription billing logic (only ad-free gating is relevant)

## Requirements

### Compliance Requirements

1. **CR-1**: Ad SDK MUST be initialized on the main thread per Google documentation
2. **CR-2**: User consent MUST be obtained before loading personalized ads (GDPR/CCPA)
3. **CR-3**: AndroidManifest MUST contain the production AdMob App ID for release builds
4. **CR-4**: Content rating and child-directed treatment MUST be consistent across platforms
5. **CR-5**: No more than one interstitial ad per user action without clear content between
6. **CR-6**: Banner ad density MUST not exceed Google's maximum visible ads per screen
7. **CR-7**: Rewarded ad callbacks MUST not grant duplicate rewards
8. **CR-8**: Ad unit IDs MUST use test IDs in debug builds and production IDs in release builds

## Current Architecture

### Ad Loading Lifecycle (Current)

```
App.kt LaunchedEffect(Unit) on Dispatchers.Default
  ├── AdInitializer.initialize(context)     ← Background thread
  ├── AdInitializer.configure(isDebug, testDeviceIds)
  └── (returns)

App.kt Composition Tree
  ├── SpaceLaunchNowTheme
  │   ├── AdConsentPopup()                  ← Consent shown (async, no blocking)
  │   └── WithPreloadedAds(context)         ← Ads loaded IMMEDIATELY
  │       ├── rememberBannerAd (x4)
  │       ├── rememberInterstitialAd (x1)
  │       ├── rememberRewardedAd (x1)
  │       └── NavHost (content)
```

### Key Files

| File | Role |
|------|------|
| `App.kt` | SDK init + composition tree |
| `AdInitializer.{android,ios}.kt` | Platform SDK initialization |
| `AdSupport.{android,ios}.kt` | Consent + preloading |
| `GlobalAdManager.{android,ios}.kt` | Frequency capping, config |
| `SmartBannerAd.{android,ios}.kt` | Banner display logic |
| `InterstitialAdHandler.{android,ios}.kt` | Interstitial display logic |
| `RewardedAdHandler.{android,ios}.kt` | Rewarded display logic |
| `AdMobConfig.{android,ios,desktop}.kt` | Ad unit ID resolution |
| `AndroidManifest.xml` | AdMob App ID declaration |
| `Info.plist` | iOS AdMob App ID |
