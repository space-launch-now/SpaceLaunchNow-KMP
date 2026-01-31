# Implementation Plan: Astronaut Views

**Branch**: `add_astronaut_info` | **Date**: 2026-01-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/add_astronaut_info/spec.md`

## Summary

Implement comprehensive astronaut browsing and viewing capabilities in SpaceLaunchNow KMP. Users will be able to browse astronauts from Settings → Explore, view detailed astronaut profiles with career statistics and mission history, and see compact astronaut profile cards on Launch Detail screens for crewed missions. Implementation follows established patterns using generated API models, extension functions for clean interfaces, repository pattern with Result<T> wrappers, MVVM architecture with StateFlow, and reusable Compose components.

## Technical Context

**Language/Version**: Kotlin 2.1.0 (Multiplatform)  
**Primary Dependencies**: Compose Multiplatform 1.7.3, Kotlin Serialization, Koin 4.x, Coil3  
**Storage**: API-driven (Launch Library 2.4.0), future: local caching with SQLDelight  
**Testing**: JUnit + Kotest for unit tests, Compose UI testing for integration  
**Target Platform**: Android (primary), iOS, Desktop (JVM) via Kotlin Multiplatform  
**Project Type**: Mobile-first multiplatform application  
**Performance Goals**: List initial load <2s, smooth 60fps scrolling, pagination 20 items/page  
**Constraints**: API rate limiting (300 req/hr authenticated), offline support future enhancement  
**Scale/Scope**: 500+ astronauts in database, 20 items per page, detail views with 10+ data fields

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Mobile-First Development (Android & iOS Equal Priority)
**Status**: ✅ **PASS** - Kotlin Multiplatform architecture ensures equal priority  
**Evidence**: All astronaut code in `commonMain`, platform-specific UI via Compose Multiplatform

### II. Pattern-Based Consistency
**Status**: ✅ **PASS** - Follows all established patterns  
**Evidence**:
- Uses `LaunchFormatUtil` pattern for title formatting
- API Extension Functions (AstronautsApiExtensions) instead of generated methods
- Repository Pattern with `Result<T>` wrappers
- MVVM with StateFlow
- `SharedDetailScaffold` for detail views

### III. Accessibility & User Experience
**Status**: ✅ **PASS** - All UI components will have accessibility support  
**Planned**:
- `@Preview` annotations for all Composables
- Reuses components from `ui/components/`
- Content descriptions for images
- Semantic properties for screen readers
- Minimum 48dp touch targets

### IV. CI/CD & Conventional Commits (NON-NEGOTIABLE)
**Status**: ✅ **PASS** - Feature commits will follow format  
**Commit Strategy**:
- `feat(astronaut): add astronaut list and detail views` → Minor version bump
- `feat(astronaut): add profile cards to launch detail` → Minor version bump
- `fix(astronaut): resolve image loading issue` → Patch version bump

### V. Code Generation & API Management
**Status**: ✅ **PASS** - Uses generated models with extension functions  
**Evidence**:
- All models from `me.calebjones.spacelaunchnow.api.launchlibrary.models`
- Extension functions in `api/extensions/AstronautsApiExtensions.kt`
- No direct calls to generated API methods with 70+ parameters

### VI. Multiplatform Architecture
**Status**: ✅ **PASS** - Business logic in commonMain  
**Evidence**:
- Repository, ViewModels, and UI components in `commonMain`
- No platform-specific code needed for astronaut feature
- Uses Koin for dependency injection

### VII. Testing Standards
**Status**: ✅ **PASS** - Comprehensive test coverage planned  
**Testing Strategy**:
- Integration tests: `AstronautRepositoryTest.kt`
- ViewModel tests: `AstronautListViewModelTest.kt`, `AstronautDetailViewModelTest.kt`
- UI tests: Critical navigation flows (list → detail → back)

### Post-Design Re-evaluation
*Status: Not yet performed (execute after Phase 1 artifacts generated)*

## Project Structure

### Documentation (this feature)

```text
specs/add_astronaut_info/
├── plan.md                  # This file (/speckit.plan command output)
├── research.md              # Phase 0 output - COMPLETE
├── data-model.md            # Phase 1 output - COMPLETE
├── quickstart.md            # Phase 1 output - COMPLETE
├── contracts/               # Phase 1 output - COMPLETE
│   └── astronaut-endpoints.md
└── tasks.md                 # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/kotlin/
├── api/
│   └── extensions/
│       └── AstronautsApiExtensions.kt          # NEW: Extension functions for clean API
├── data/
│   └── repository/
│       ├── AstronautRepository.kt              # NEW: Repository interface
│       └── AstronautRepositoryImpl.kt          # NEW: Repository implementation
├── di/
│   └── AppModule.kt                            # MODIFY: Register astronaut DI
├── navigation/
│   └── Screen.kt                               # MODIFY: Add Astronauts, AstronautDetail routes
├── ui/
│   ├── astronaut/
│   │   ├── AstronautListScreen.kt              # NEW: Main list screen
│   │   ├── AstronautDetailView.kt              # NEW: Detail screen with SharedDetailScaffold
│   │   └── components/
│   │       ├── AstronautCard.kt                # NEW: List item card
│   │       ├── AstronautProfileCard.kt         # NEW: Horizontal profile (launch detail)
│   │       ├── AstronautInfoCard.kt            # NEW: Biography/info section
│   │       ├── AstronautStatsCard.kt           # NEW: Career statistics
│   │       └── AstronautFlightHistoryCard.kt   # NEW: Mission history
│   ├── detail/compose/
│   │   └── tabs/
│   │       └── OverviewTabContent.kt           # MODIFY: Add astronaut profile cards
│   ├── settings/
│   │   └── SettingsScreen.kt                   # MODIFY: Add astronaut list link
│   └── viewmodel/
│       ├── AstronautListViewModel.kt           # NEW: List state management
│       └── AstronautDetailViewModel.kt         # NEW: Detail state management
└── App.kt                                      # MODIFY: Register navigation routes

composeApp/src/commonTest/kotlin/
└── repository/
    ├── AstronautRepositoryTest.kt              # NEW: Repository tests
    └── viewmodel/
        ├── AstronautListViewModelTest.kt       # NEW: List ViewModel tests
        └── AstronautDetailViewModelTest.kt     # NEW: Detail ViewModel tests
```

**Structure Decision**: Using Option 3 (Mobile + API) structure as this is a Kotlin Multiplatform mobile application consuming the Launch Library REST API. All astronaut feature code resides in `commonMain` for cross-platform compatibility with platform-specific implementations only when necessary (none required for this feature).

## Complexity Tracking

> **No violations identified - All Constitution principles followed**

This feature adheres to all seven core principles:
1. ✅ Mobile-first (Android & iOS equal priority via KMP)
2. ✅ Pattern consistency (follows all established patterns)
3. ✅ Accessibility (previews, content descriptions, semantic properties)
4. ✅ Conventional commits (feature-based commit strategy)
5. ✅ Code generation (uses OpenAPI models with extension functions)
6. ✅ Multiplatform architecture (commonMain business logic)
7. ✅ Testing standards (unit, integration, and UI tests planned)

**No complexity justification required.**
