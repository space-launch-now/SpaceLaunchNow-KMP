# Implementation Plan: Onboarding Paywall

**Branch**: `006-onboarding-paywall` | **Date**: 2026-03-03 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/006-onboarding-paywall/spec.md`

## Summary

Replace the existing `BetaWarningDialog` with a full-screen onboarding experience that serves as the app's `startDestination` for first-time users. The screen combines the welcome introduction with a premium paywall — highlighting perks (Ad-Free, Premium Widgets, Calendar Sync, Themes) and presenting subscription options (Annual recommended, Monthly, Lifetime). A "Continue" / "Maybe Later" button navigates to Home. The `Onboarding` route is added to `Screen.kt`, and `startDestination` in the `NavHost` is conditionally set based on an `AppPreferences` DataStore flag. Reuses `SubscriptionViewModel` for product loading and purchase flows.

## Technical Context

**Language/Version**: Kotlin 2.1.0 (KMP), Java 21  
**Primary Dependencies**: Compose Multiplatform, RevenueCat SDK (via `SubscriptionViewModel`), Koin DI, DataStore Preferences  
**Storage**: DataStore Preferences (`AppPreferences`) — new `ONBOARDING_COMPLETED` boolean key (replaces need for `BETA_WARNING_SHOWN`)  
**Testing**: commonTest (unit), jvmTest (integration), androidTest (instrumentation)  
**Target Platform**: Android + iOS (equal priority per Constitution I), Desktop (secondary)  
**Project Type**: Mobile (KMP)  
**Performance Goals**: Onboarding screen visible within 300ms of app launch, 60fps scroll, smooth entrance animations  
**Constraints**: Common code only (`commonMain`), no platform-specific implementations needed. Must handle conditional `startDestination` without flash/flicker  
**Scale/Scope**: ~27,800 active users, 3,892 new users/28d — 1 new screen, 1 new route, conditional startDestination, BetaWarningDialog removal

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal) | PASS | All code in `commonMain`, no platform-specific impl needed |
| II. Pattern-Based Consistency | PASS | Reuses `SubscriptionViewModel`, `PremiumPerkCard`, `PricingCard` patterns from `SupportUsScreen`; follows DataStore preference pattern; uses type-safe navigation routes |
| III. Accessibility & UX | PASS | Dual previews (light + dark) required; content descriptions on all icons; soft dismiss (no hard gate); visually polished design with gradients, animations, and premium feel |
| IV. CI/CD & Conventional Commits | PASS | All commits will use `feat(ui): ...` format |
| V. Code Generation & API | N/A | No API changes needed — uses existing `SubscriptionViewModel` which wraps RevenueCat |
| VI. Multiplatform Architecture | PASS | Common code only, `AppPreferences` (DataStore) already multiplatform, `SubscriptionViewModel` already multiplatform |
| VII. Testing Standards | PASS | ViewModel state tests, composable preview verification, DataStore preference tests |

**Gate Result: ALL PASS** — No violations. Proceeding to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/
├── data/storage/
│   └── AppPreferences.kt              # MODIFY: Add ONBOARDING_COMPLETED key + flow + setter
├── ui/onboarding/
│   └── OnboardingScreen.kt            # NEW: Full-screen onboarding with welcome + paywall
├── ui/subscription/
│   └── SupportUsScreen.kt             # REFERENCE: Existing paywall (patterns to reuse)
├── ui/compose/
│   └── BetaWarningDialog.kt           # DELETE: Replaced by OnboardingScreen
├── App.kt                             # MODIFY: Conditional startDestination, add Onboarding route,
│                                      #         remove BetaWarningDialog() call
└── navigation/
    └── Screen.kt                      # MODIFY: Add Onboarding data object

composeApp/src/commonTest/kotlin/
└── me/calebjones/spacelaunchnow/
    └── ui/onboarding/
        └── OnboardingScreenTest.kt    # NEW: Unit tests for onboarding logic
```

**Structure Decision**: Full-screen navigation route (`Onboarding`) that serves as the conditional `startDestination`. This replaces the dialog-based `BetaWarningDialog` entirely. On dismiss, navigates to `Home` with `popUpTo(Onboarding) { inclusive = true }` so the user cannot navigate back.

## Complexity Tracking

No constitution violations. No complexity justifications needed.

## Post-Design Constitution Re-Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First | PASS | All `commonMain`, tested on Android + iOS + Desktop |
| II. Patterns | PASS | Uses type-safe `@Serializable` route (same as all other screens), reuses `SupportUsScreen` composable patterns, follows `composableWithCompositionLocal` nav pattern |
| III. Accessibility | PASS | Dual previews defined (light + dark), content descriptions on perk icons, soft dismiss |
| IV. CI/CD | PASS | Conventional commits: `feat(ui): add onboarding screen`, `refactor(ui): remove BetaWarningDialog` |
| V. Code Gen | N/A | No API changes |
| VI. Multiplatform | PASS | DataStore + Compose + Navigation = fully cross-platform |
| VII. Testing | PASS | Unit tests for conditional startDestination logic and onboarding completion |

**Post-Design Gate: ALL PASS**

### Breaking Change Note

Removing `BetaWarningDialog` is a behavioral change — existing users who haven't yet seen the welcome dialog will now see the full onboarding screen instead. The `BETA_WARNING_SHOWN` DataStore key should be checked: if `true`, also set `ONBOARDING_COMPLETED = true` to prevent existing users from seeing the onboarding screen. This migration logic runs once in `App.kt`.
