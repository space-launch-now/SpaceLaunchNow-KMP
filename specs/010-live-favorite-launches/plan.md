# Implementation Plan: Live Favorite Launches Card

**Branch**: `010-live-favorite-launches` | **Date**: 2026-04-01 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/010-live-favorite-launches/spec.md`

## Summary

Display a prominent "LIVE" card on the home page when a launch is currently in flight (status_id=6) that matches the user's filter preferences (subscribed agencies/locations). The LIVE card will have visual distinction with the In Flight color (Blue 500), an animated LIVE badge, and appear at the top of the home page above the regular featured launch card.

**Technical Approach**:
1. Add new repository method `getInFlightLaunches()` using `status__ids=6` API parameter
2. Add new ViewState in HomeViewModel for in-flight launch tracking
3. Create new `LiveLaunchCard` composable with animated LIVE indicator
4. Integrate into home page layout above featured launch
5. Apply user filter preferences to in-flight launches

## Technical Context

**Language/Version**: Kotlin 2.1.0 (KMP), Compose Multiplatform 1.7.x  
**Primary Dependencies**: Ktor Client, Koin DI, SQLDelight (caching), Coil (images), Compose Material3  
**Storage**: SQLDelight local database (LaunchLocalDataSource), DataStore (preferences)  
**Testing**: commonTest, jvmTest, iosTest for multiplatform tests  
**Target Platform**: Android (primary), iOS, Desktop  
**Project Type**: Mobile KMP (Kotlin Multiplatform Compose)  
**Performance Goals**: LIVE card visible within 2 seconds of page load, 60fps animations  
**Constraints**: Parallel API loading (don't block home page), efficient animation (battery)  
**Scale/Scope**: Single-screen feature, 3 new files, ~300 lines of code

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Mobile-First Development | ✅ PASS | Android & iOS both supported via KMP |
| II. Pattern-Based Consistency | ✅ PASS | Uses existing patterns: API extensions, Repository Result<T>, ViewState, LaunchFormatUtil, Stale-While-Revalidate caching |
| III. Accessibility & UX | ✅ PASS | Dual previews required, content descriptions for LIVE badge, sufficient contrast for Blue 500 color |
| IV. CI/CD & Conventional Commits | ✅ PASS | Will use `feat(home):` prefix for commits |
| V. Code Generation & API Management | ✅ PASS | Uses existing `getLaunchList()` extension with `statusIds` parameter |
| VI. Multiplatform Architecture | ✅ PASS | All code in commonMain, animations via Compose (cross-platform) |
| VII. Testing Standards | ✅ PASS | Repository tests for in-flight filtering, ViewModel unit tests |
| VIII. Jetpack Compose Best Practices | ✅ PASS | StateFlow, `collectAsStateWithLifecycle()`, proper effect handlers |

**Gate Result**: ✅ PASS - No violations. Proceed to Phase 0 research.

## Project Structure

### Documentation (this feature)

```text
specs/010-live-favorite-launches/
├── plan.md              # This file
├── spec.md              # Feature specification (created)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── in-flight-api.md
└── tasks.md             # Phase 2 output (speckit.tasks command)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/
├── data/
│   └── repository/
│       ├── LaunchRepository.kt        # Add getInFlightLaunches() method
│       └── LaunchRepositoryImpl.kt    # Implement with status_ids=6 filter
├── ui/
│   ├── viewmodel/
│   │   └── HomeViewModel.kt           # Add inFlightLaunchState, loadInFlightLaunch()
│   ├── home/
│   │   └── components/
│   │       └── LiveLaunchCard.kt      # NEW: LIVE card composable
│   └── compose/
│       └── LiveIndicator.kt           # NEW: Animated LIVE badge component
└── api/
    └── extensions/
        └── LaunchesApiExtensions.kt   # Already supports statusIds parameter

composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/
└── data/
    └── repository/
        └── LaunchRepositoryInFlightTest.kt  # NEW: Test in-flight filtering
```

**Structure Decision**: Uses existing KMP mobile structure with new files in `ui/home/components/` for the LIVE card and `ui/compose/` for the reusable LIVE indicator animation.

## Complexity Tracking

> No constitutional violations - no entries needed.

---

## Post-Design Constitution Re-Check

*Re-evaluated after Phase 1 design completion*

| Principle | Status | Post-Design Evidence |
|-----------|--------|----------------------|
| I. Mobile-First Development | ✅ PASS | Design uses only KMP-compatible Compose APIs |
| II. Pattern-Based Consistency | ✅ PASS | Uses ViewState<T>, Result<T>, LaunchFormatUtil, Stale-While-Revalidate (see data-model.md) |
| III. Accessibility & UX | ✅ PASS | LiveIndicator uses text "LIVE" (not just color), sufficient contrast verified |
| IV. CI/CD & Conventional Commits | ✅ PASS | Commits will use `feat(home): add LIVE launch card` format |
| V. Code Generation & API Management | ✅ PASS | Uses existing `getLaunchList()` extension - no direct generated API calls |
| VI. Multiplatform Architecture | ✅ PASS | All new files in commonMain, no platform-specific code needed |
| VII. Testing Standards | ✅ PASS | Test file scoped in quickstart.md, covers repository and ViewModel |
| VIII. Jetpack Compose Best Practices | ✅ PASS | Animation uses `rememberInfiniteTransition` (efficient), proper state hoisting |

**Post-Design Gate Result**: ✅ PASS - Ready for task generation (Phase 2)

---

## Phase Completion Summary

### Phase 0: Research ✅ Complete
- [research.md](research.md) - All unknowns resolved

### Phase 1: Design & Contracts ✅ Complete
- [data-model.md](data-model.md) - Entity model and state machine
- [contracts/in-flight-api.md](contracts/in-flight-api.md) - API contract specification
- [quickstart.md](quickstart.md) - Implementation guide with code samples

### Phase 2: Tasks
- Pending: Run `@speckit.tasks` to generate `tasks.md`
