# Data Model: Platform-Specific Welcome Dialog

**Date**: 2026-03-03 | **Plan**: [plan.md](plan.md)

## Entities

### No New Data Entities Required

This feature modifies UI text only. No new data models, database schemas, or API contracts are needed.

## Existing Entities Used

### PlatformType (existing, no changes)

```kotlin
// Location: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/Platform.kt
enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP;

    val isAndroid: Boolean get() = this == ANDROID
    val isIOS: Boolean get() = this == IOS
    val isDesktop: Boolean get() = this == DESKTOP
    val isMobile: Boolean get() = this == ANDROID || this == IOS
}
```

### AppPreferences (existing, no changes)

```kotlin
// Location: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/AppPreferences.kt
// Key: "beta_warning_shown" (Boolean, default false)
// Flow: betaWarningShownFlow
// Setter: setBetaWarningShown(shown: Boolean)
```

## Platform-Specific Text Mapping

| Text Element | Android | iOS | Desktop |
|-------------|---------|-----|---------|
| Store reference | "Google Play" | "the App Store" | _(omitted)_ |
| Platform guidelines | "guidelines from Google" | "guidelines from Apple" | "evolving platform requirements" |
| Description context | Mentions Google restrictions | Mentions Apple restrictions | Generic platform language |

## State Transitions

```
Dialog State Machine (unchanged):
  [App Launch] → check betaWarningShown
    ├─ true  → [No Dialog]
    └─ false → [Show Dialog] → user clicks "I Understand" → setBetaWarningShown(true) → [Dismiss]
```
