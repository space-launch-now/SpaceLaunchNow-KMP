# Implementation Plan: Domain Model Layer Migration

**Branch**: `013-wear-os-premium` | **Date**: 2026-04-19 | **Spec**: [docs/tasks-phase3.md](../../docs/tasks-phase3.md)  
**Input**: Decouple UI/ViewModel layer from generated API response types by introducing a domain model layer. **Key requirement**: ONE unified `Launch` model replacing `LaunchBasic`/`LaunchNormal`/`LaunchDetailed`.

## Summary

Introduce a `domain/model/` package with unified data classes (`Launch`, `Event`, shared value types) and a `domain/mapper/` package with extension functions that convert generated API types to domain types. The repository layer becomes the mapping boundary — everything above (ViewModels, UI, cache) uses only domain types. SQLDelight cache remains unchanged (stores API JSON, maps to domain on read). Phase 3A covers Launch + Event (~80% of UI surfaces); Phase 3B covers secondary entities.

## Technical Context

**Language/Version**: Kotlin 2.1.x (KMP), Java 21  
**Primary Dependencies**: Ktor (HTTP), Koin (DI), Compose Multiplatform (UI), kotlinx.datetime, SQLDelight  
**Storage**: SQLDelight with JSON blob caching (no schema change needed)  
**Testing**: JUnit + kotlin.test (commonTest, jvmTest)  
**Target Platform**: Android (primary), iOS, Desktop  
**Project Type**: Mobile (KMP multiplatform)  
**Performance Goals**: No regression — mapper overhead is negligible (pure data copying)  
**Constraints**: Must not break stale-while-revalidate caching pattern  
**Scale/Scope**: ~40 files to modify in Phase 3A, ~30 additional in Phase 3B

## Constitution Check

### Pre-Design Gate

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First | ✅ PASS | Domain models are in `commonMain`, all platforms benefit |
| II. Pattern-Based Consistency | ✅ PASS | New mapper pattern is consistent, caching pattern preserved |
| III. Accessibility & UX | ✅ PASS | No UI behavioral change, only internal type changes |
| IV. CI/CD & Commits | ✅ PASS | Will use `refactor(domain):` commit prefix |
| V. Code Generation & API | ✅ PASS | Generated code untouched; mappers bridge generated → domain |
| VI. Multiplatform Architecture | ✅ PASS | All code in `commonMain`, all targets must compile |
| VII. Testing Standards | ✅ PASS | Mapper unit tests, repository tests, VM tests |
| VIII. Compose Best Practices | ✅ PASS | Domain models use `@Stable`/`@Immutable` for recomposition |

### Post-Design Gate

| Principle | Status | Notes |
|-----------|--------|-------|
| II. Pattern-Based Consistency | ✅ PASS | Extension function mappers follow established API extension pattern |
| V. Code Generation | ✅ PASS | Mappers are the ONLY non-repo code importing generated types |
| VII. Testing | ✅ PASS | Every mapper function gets unit test coverage |

## Project Structure

### Documentation (this feature)

```text
specs/dev/domain-model-layer/
├── plan.md              # This file
├── research.md          # Phase 0 research decisions
├── data-model.md        # Phase 1 entity definitions + mapping rules
├── quickstart.md        # Phase 1 step-by-step migration guide
└── contracts/           # (empty — no API surface changes)
```

### Source Code Changes

```text
composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/
├── domain/                              # NEW PACKAGE
│   ├── model/
│   │   ├── PaginatedResult.kt          # Generic paginated wrapper
│   │   ├── Common.kt                   # Provider, Pad, Location, Mission, etc.
│   │   ├── Launch.kt                   # Unified Launch data class
│   │   ├── Event.kt                    # Unified Event data class
│   │   ├── LaunchFilterParams.kt       # Launch filter params
│   │   └── EventFilterParams.kt        # Event filter params
│   └── mapper/
│       ├── CommonMappers.kt            # Shared type mappers
│       ├── LaunchMappers.kt            # LaunchBasic/Normal/Detailed → Launch
│       └── EventMappers.kt             # EventNormal/Detailed → Event
│
├── data/repository/
│   ├── LaunchRepository.kt             # MODIFIED — add domain-returning methods
│   ├── LaunchRepositoryImpl.kt         # MODIFIED — implement domain methods
│   ├── EventsRepository.kt             # MODIFIED — add domain-returning methods
│   └── EventsRepositoryImpl.kt         # MODIFIED — implement domain methods
│
├── ui/viewmodel/
│   ├── NextUpViewModel.kt              # MODIFIED — LaunchNormal? → Launch?
│   ├── LaunchViewModel.kt              # MODIFIED — LaunchDetailed? → Launch?
│   ├── LaunchesViewModel.kt            # MODIFIED — LaunchNormal → Launch
│   ├── ScheduleViewModel.kt            # MODIFIED — LaunchBasic → Launch
│   ├── EventsViewModel.kt              # MODIFIED — EventEndpointNormal → Event
│   ├── HistoryViewModel.kt             # MODIFIED — LaunchNormal → Launch
│   ├── EventViewModel.kt               # MODIFIED — if exists, EventDetailed → Event
│   ├── FeaturedLaunchViewModel.kt      # MODIFIED — LaunchNormal → Launch
│   ├── LaunchCarouselViewModel.kt      # MODIFIED — LaunchNormal → Launch
│   ├── StarshipViewModel.kt            # MODIFIED — LaunchNormal → Launch
│   └── FeedViewModel.kt                # MODIFIED — if uses launch types
│
├── ui/compose/
│   ├── LaunchCardHeader.kt             # MODIFIED — eliminate LaunchCardData sealed interface
│   └── [other composables]             # MODIFIED — replace API type params with Launch
│
├── cache/
│   └── LaunchCache.kt                  # MODIFIED — unify to single Launch cache
│
├── database/
│   └── LaunchLocalDataSource.kt        # MODIFIED — return domain Launch from JSON
│
└── util/
    ├── LaunchFormatUtil.kt             # MODIFIED — add Launch overload, deprecate old
    ├── SharingUtil.kt                  # MODIFIED — LaunchNormal → Launch
    └── VideoUtil.kt                    # MODIFIED — VidURL → VideoLink
```

## Complexity Tracking

No constitution violations. Domain model layer follows existing patterns (extension functions, Result wrapping, StateFlow exposure).

---

## Phase 3A: Core Migration — Launch + Event

### 3A-1: Create domain model package
Create all files in `domain/model/` as specified in [data-model.md](data-model.md).

### 3A-2: Create mapper functions
Create `domain/mapper/` files with `toDomain()` extension functions.

### 3A-3: Write mapper unit tests
Test every mapper function with representative API data.

### 3A-4: Update repository interfaces
Add new domain-returning methods. Keep old methods with `@Deprecated`.

### 3A-5: Update repository implementations
Implement new methods: call API → map → return domain type.

### 3A-6: Update ViewModels
Change all `StateFlow` types from API models to domain models.

### 3A-7: Eliminate `LaunchCardData` sealed interface
Replace with direct `Launch` parameter in composables.

### 3A-8: Update `LaunchFormatUtil`
Add `formatLaunchTitle(launch: Launch)` overload.

### 3A-9: Update `LaunchCache`
Unify to single `Launch` cache.

### 3A-10: Update `LaunchLocalDataSource`
Map from API JSON → domain types on read.

### 3A-11: Update remaining UI composables
Replace all API type usage in composables with domain types.

### 3A-12: Update utility files
`SharingUtil`, `VideoUtil`, platform-specific sharing files.

---

## Phase 3B: Secondary Entity Migration

Apply same pattern for: Agency, Astronaut, Vehicle, Launcher, Spacecraft, SpaceStation, Program.
Each is independent and can be done as separate PRs.

---

## Phase 3C: Verification

- `./gradlew compileKotlinDesktop` — zero errors
- `./gradlew test` — all tests pass
- Desktop: verify Home, Schedule, Launch Detail, Events
- Android: verify same on device/emulator
- Confirm no composable imports from `api.launchlibrary.models` (except mappers/repos)
