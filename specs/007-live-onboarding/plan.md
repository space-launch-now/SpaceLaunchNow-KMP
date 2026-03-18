# Implementation Plan: Live Composable Onboarding

**Branch**: `007-live-onboarding` | **Date**: 2026-03-15 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/007-live-onboarding/spec.md`

## Summary

Create a multi-page onboarding carousel that renders **live production composables** inside platform-specific device frames, styled after the ClashMarket reference design. Each page has a full-bleed space-themed background image, a centered dark-bezel device frame showing a live composable, bold title and subtitle below, a wavy-line progress bar, and a full-width "Next" button at the bottom. A "Skip" text button sits in the top-right corner.

Each page showcases a real app feature (launch card, schedule, notification filters) using the same UI components with mock data, ensuring previews never go stale. The final page requests notification permission. The existing `OnboardingScreen` is renamed to `OnboardingPaywallScreen` and the new flow sequences as: Live Onboarding → Onboarding Paywall → Home.

### Visual Layout (per page, top-to-bottom)

```
┌──────────────────────────────────┐
│ [Full-bleed background image]    │
│                          Skip    │  ← Text button, top-right
│                                  │
│     ┌────────────────────┐       │
│     │ ▪ 03:23        ▪▪▪│       │  ← Device frame with dark bezel
│     │                    │       │     Status bar with live clock
│     │   [LIVE COMPOSABLE │       │     Android: pill camera cutout
│     │    CONTENT HERE]   │       │     iOS: Dynamic Island notch
│     │                    │       │
│     │                    │       │
│     └────────────────────┘       │
│                                  │
│     Track Every Launch           │  ← Bold title
│     See detailed launch info     │  ← Lighter subtitle
│     and countdown timers.        │
│                                  │
│  ─────〰〰〰─────────────────── │  ← Wavy-line progress bar
│                                  │
│  ┌────────────────────────────┐  │
│  │          Next              │  │  ← Full-width accent button
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

## Technical Context

**Language/Version**: Kotlin 2.1.0 (KMP), Java 21 + Compose Multiplatform  
**Primary Dependencies**: Compose Multiplatform (HorizontalPager, Material3), Koin DI, DataStore Preferences, Coil3  
**Storage**: DataStore Preferences (`AppPreferences`) — new `LIVE_ONBOARDING_COMPLETED` boolean key  
**Testing**: commonTest (unit), jvmTest, iosTest — mock data driven (no API calls)  
**Target Platform**: Android (primary), iOS, Desktop (secondary)  
**Project Type**: Mobile (KMP)  
**Performance Goals**: Each onboarding page renders <100ms; device frame composable adds <16ms overhead (single frame budget)  
**Constraints**: No network calls during onboarding; all preview data from `PreviewData` object; offline-capable  
**Scale/Scope**: 4 new composable files, 1 renamed file, 3 modified files (App.kt, Screen.kt, AppPreferences)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal) | ✅ PASS | Common composables in `commonMain`; device frame uses runtime platform detection for visual style; notification permission uses existing platform expect/actual |
| II. Pattern-Based Consistency | ✅ PASS | Uses `LaunchFormatUtil.formatLaunchTitle()` in mock data; uses `PreviewData` for mock launches; follows MVVM with StateFlow; uses `DateTimeUtil` for clock |
| III. Accessibility & UX | ✅ PASS | Dual light/dark previews required for all new composables; content descriptions on all icons; device frames scale responsively |
| IV. CI/CD & Conventional Commits | ✅ PASS | No CI/CD changes needed; commits will use `feat(onboarding):` prefix |
| V. Code Generation & API Management | ✅ PASS | No API calls during onboarding; mock data only from `PreviewData` |
| VI. Multiplatform Architecture | ✅ PASS | `DeviceFrame` in `commonMain` with runtime platform detection (`getPlatform()`); notification permission uses existing platform expect/actual |
| VII. Testing Standards | ✅ PASS | Stateless composables testable with preview data; no network dependencies to mock |

**Gate Result**: ✅ ALL PASS — proceed to Phase 0

## Project Structure

### Documentation (this feature)

```text
specs/007-live-onboarding/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (N/A — no APIs)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/
├── navigation/
│   └── Screen.kt                          # MODIFY: Add LiveOnboarding route
├── data/storage/
│   └── AppPreferences.kt                  # MODIFY: Add LIVE_ONBOARDING_COMPLETED key
├── ui/onboarding/
│   ├── OnboardingScreen.kt                # RENAME → OnboardingPaywallScreen.kt
│   ├── LiveOnboardingScreen.kt            # NEW: Multi-page carousel entry point
│   ├── OnboardingPage.kt                  # NEW: Single page layout (device frame + title + subtitle)
│   └── DeviceFrame.kt                     # NEW: Platform-aware device frame (runtime detection, no expect/actual)
├── ui/onboarding/pages/
│   ├── LaunchCardPage.kt                  # NEW: Page 1 — launch card preview
│   ├── SchedulePage.kt                    # NEW: Page 2 — schedule preview content
│   ├── NotificationFiltersPage.kt         # NEW: Page 3 — notification filter preview
│   └── NotificationPermissionPage.kt      # NEW: Page 4 — permission request
└── App.kt                                 # MODIFY: Update navigation sequencing
```

**Structure Decision**: All files in `commonMain` — no expect/actual needed for DeviceFrame (per Research R2: runtime platform detection via `getPlatform()` selects visual style). Pages split into individual files under `pages/` per Constitution III (keep files short).

## Complexity Tracking

> No constitution violations — no justifications required.
