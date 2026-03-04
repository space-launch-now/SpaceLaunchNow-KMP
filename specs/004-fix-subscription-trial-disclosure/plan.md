# Implementation Plan: Platform-Specific Welcome Dialog

**Branch**: `004-fix-subscription-trial-disclosure` | **Date**: 2026-03-03 | **Spec**: [spec.md](spec.md)
**Input**: User request to make the welcome dialog platform-aware and improve its copy

## Summary

The `BetaWarningDialog` (welcome/intro dialog shown on first launch) contains hardcoded text referencing "Google" on all platforms, including iOS and Desktop. This plan covers making the welcome dialog text platform-aware using the existing `PlatformType` pattern, improving the copy to be more user-friendly, and bringing the component up to project standards (dual previews, `SpaceLaunchNowPreviewTheme`).

## Technical Context

**Language/Version**: Kotlin 2.x (KMP), Compose Multiplatform  
**Primary Dependencies**: Jetpack Compose, Koin DI, DataStore Preferences  
**Storage**: DataStore Preferences (`beta_warning_shown` flag via `AppPreferences`)  
**Testing**: Compose Preview verification (light + dark), manual platform testing  
**Target Platform**: Android, iOS, Desktop (KMP)  
**Project Type**: mobile (Kotlin Multiplatform)  
**Performance Goals**: N/A (static dialog, no performance concerns)  
**Constraints**: Must use existing `PlatformType` pattern, no hardcoded platform text  
**Scale/Scope**: Single file change (`BetaWarningDialog.kt`), ~1 composable affected

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal) | PASS | Change makes dialog platform-aware for both Android and iOS |
| II. Pattern-Based Consistency | PASS | Uses existing `when (platformType)` pattern from SupportUsScreen |
| III. Accessibility & UX | FIX NEEDED | Current dialog is missing dark preview; plan includes adding it |
| IV. CI/CD & Conventional Commits | PASS | No CI/CD impact |
| V. Code Generation & API Management | N/A | No generated code involved |
| VI. Multiplatform Architecture | PASS | Common code with `PlatformType` discrimination |
| VII. Testing Standards | PASS | Dual previews will be added for visual verification |

**Gate Result**: PASS (with fix for Principle III included in scope)

## Project Structure

### Documentation (this feature)

```text
specs/004-fix-subscription-trial-disclosure/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # N/A (UI-only change)
└── tasks.md             # Phase 2 output (separate command)
```

### Source Code (repository root)

```text
composeApp/src/
├── commonMain/kotlin/me/calebjones/spacelaunchnow/
│   ├── Platform.kt                           # PlatformType enum (ANDROID, IOS, DESKTOP)
│   ├── ui/compose/
│   │   └── BetaWarningDialog.kt              # PRIMARY: Welcome dialog to modify
│   └── data/storage/
│       └── AppPreferences.kt                 # betaWarningShownFlow preference
├── androidMain/kotlin/.../
│   └── AndroidPlatform.kt                    # actual fun getPlatform() → ANDROID
├── iosMain/kotlin/.../
│   └── IOSPlatform.kt                        # actual fun getPlatform() → IOS
└── desktopMain/kotlin/.../
    └── DesktopPlatform.kt                    # actual fun getPlatform() → DESKTOP
```

**Structure Decision**: Single-file UI change in commonMain using existing platform detection infrastructure. No new files needed.

## Complexity Tracking

> No violations to justify — all changes align with existing patterns.
