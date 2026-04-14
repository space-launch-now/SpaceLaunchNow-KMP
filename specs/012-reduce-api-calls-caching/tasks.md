# Tasks: Reduce API Calls Per Session & Improve Caching

**Input**: Design documents from `/specs/012-reduce-api-calls-caching/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Not explicitly requested in the feature specification. Test tasks are excluded.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- **Common source**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`
- **SQLDelight**: `composeApp/src/commonMain/sqldelight/me/calebjones/spacelaunchnow/database/`
- **Common test**: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 1: Setup

**Purpose**: No new project setup needed — this is an existing KMP project. Verify branch and build baseline.

- [X] T001 Verify `012-reduce-api-calls-caching` branch is checked out and `./gradlew compileKotlinDesktop` builds cleanly

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create the StatsCache schema and StatsLocalDataSource that US1 and US3 depend on. Register in DI and cleanup service.

**⚠️ CRITICAL**: US1 cache work cannot begin until this phase is complete.

- [X] T002 Create `Stats.sq` SQLDelight file with `StatsCache` table, indexes, and queries (`getStatCount`, `getStatCountStale`, `upsertStat`, `deleteExpiredStats`, `deleteAllStats`) in `composeApp/src/commonMain/sqldelight/me/calebjones/spacelaunchnow/database/Stats.sq`
- [X] T003 Create `StatsLocalDataSource.kt` following `ArticleLocalDataSource` pattern (constructor takes `StatsCacheQueries` + `AppPreferences`, methods: `getStatCount()`, `getStatCountStale()`, `cacheStat()`, `deleteExpiredStats()`, 10-min TTL with 1-min debug override) in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/StatsLocalDataSource.kt`
- [X] T004 Register `StatsLocalDataSource` as singleton in Koin `AppModule.kt` at `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt`
- [X] T005 Add `statsLocalDataSource.deleteExpiredStats()` call to the periodic cleanup cycle in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/CacheCleanupService.kt`
- [X] T006 Verify build: `./gradlew compileKotlinDesktop` succeeds with new SQLDelight schema

**Checkpoint**: StatsCache infrastructure ready — US1 implementation can begin.

---

## Phase 3: User Story 1 — Returning User Sees Home Screen Instantly (Priority: P1) 🎯 MVP

**Goal**: Returning user Home screen mount triggers ≤5 API calls (down from ~11). Cache-first strategy for all above-fold content. Stale-while-revalidate for `getInFlightLaunches()` and stats counts.

**Independent Test**: Open app as returning user with cached data from previous session. Verify Home screen sections appear instantly from cache, and network monitor shows ≤5 API calls on Home mount.

### Implementation for User Story 1

- [X] T007 [US1] Add `getStatsCount(key: String, netGt: Instant, netLt: Instant, forceRefresh: Boolean): Result<DataResult<Int>>` to `LaunchRepository` interface in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepository.kt`
- [X] T008 [US1] Implement `getStatsCount()` in `LaunchRepositoryImpl` with stale-while-revalidate pattern (fresh cache → stale cache → API → error fallback) using `StatsLocalDataSource`, per `contracts/launch-cache-contract.md` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt`
- [X] T009 [P] [US1] Add in-flight launch cache queries to `Launch.sq`: `getInFlightNormalLaunches` (fresh, filtered by `expires_at > ?`) and `getInFlightNormalLaunchesStale` (ignores TTL) in `composeApp/src/commonMain/sqldelight/me/calebjones/spacelaunchnow/database/Launch.sq`
- [X] T010 [US1] Add `getInFlightNormalLaunches(limit)`, `getInFlightNormalLaunchesStale(limit)`, and `cacheInFlightLaunches(launches)` methods to `LaunchLocalDataSource` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/database/LaunchLocalDataSource.kt`
- [X] T011 [US1] Add stale-while-revalidate cache to `getInFlightLaunches()` in `LaunchRepositoryImpl` following `getFeaturedLaunch()` reference pattern with 5-min TTL, per `contracts/launch-cache-contract.md` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt`
- [X] T012 [US1] Update `StatsViewModel` to call `launchRepository.getStatsCount()` instead of `getUpcomingLaunchesList()` for 24h/week/month counts, using cache keys `"stats_24h"`, `"stats_week"`, `"stats_month"` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/StatsViewModel.kt`
- [X] T013 [US1] Verify build and manual test: returning user with cached data sees ≤5 API calls on Home mount via `./gradlew compileKotlinDesktop` and desktop run

**Checkpoint**: US1 complete — returning user Home screen loads from cache with ≤5 API calls. Stats and in-flight launches now cached.

---

## Phase 4: User Story 2 — First-Time Preload Caches Useful Data (Priority: P2)

**Goal**: Preload Tier 1 fires only 4 critical API calls (down from 7) whose results are consumed by Home screen via cache. Remove 6 wasted Tier 1 tasks and 4 wasted Tier 2 tasks.

**Independent Test**: Fresh install → preload screen → verify Tier 1 makes ≤4 API calls. Navigate to Home → verify all sections read from cache (0 additional API calls for preloaded data).

### Implementation for User Story 2

- [X] T014 [US2] Restructure `buildTier1Tasks()` in `PreloadViewModel`: remove all 7 current tasks, replace with 4 new tasks (getFeaturedLaunch, getUpcomingLaunchesNormal(8), getArticles, getUpcomingEvents) per research.md R1 decision in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/preload/PreloadViewModel.kt`
- [X] T015 [US2] Restructure `buildTier2Tasks()` in `PreloadViewModel`: remove stats 24h/week/month (3 tasks), ISS station details (1 task), astronauts/rockets/agencies (if present); keep previous normal launches, latest updates, history launches, all filter data in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/preload/PreloadViewModel.kt`
- [X] T016 [US2] Verify build and preload flow: `./gradlew compileKotlinDesktop`, run desktop app fresh, confirm Tier 1 completes with ≤4 API calls

**Checkpoint**: US2 complete — preload Tier 1 fires only meaningful cached data. All preloaded data consumed by Home.

---

## Phase 5: User Story 3 — Below-Fold Sections Load on Scroll (Priority: P2)

**Goal**: Stats, History, and Previous Launches sections make 0 API calls until the user scrolls them into view. Uses `LaunchedEffect(Unit)` inside `item {}` blocks pattern per research.md R3.

**Independent Test**: Open Home screen, observe network. Verify stats/history/previous sections make 0 calls until scrolled into viewport.

### Implementation for User Story 3

- [X] T017 [US3] Remove `statsViewModel.loadAllStats()` from top-level `LaunchedEffect(Unit)` in `HomeScreen.kt`, keeping featured launch, upcoming launches, feed, and events loads in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/HomeScreen.kt`
- [X] T018 [US3] Add `LaunchedEffect(Unit) { statsViewModel.loadAllStats() }` inside the `item(key = "quick_stats")` composable block in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/ResponsiveHomeContent.kt`
- [X] T019 [US3] Move `historyViewModel.loadHistoryLaunches()` from top-level `LaunchedEffect` into `item(key = "history_section")` - deferred via fallback-safe approach in HomeScreen and ResponsiveHomeContent
- [ ] T020 [US3] Remove `loadPreviousLaunches()` from top-level `LaunchedEffect` in `HomeScreen.kt`; if `loadLaunches()` is a single combined call, split into `loadUpcomingLaunches()` and `loadPreviousLaunches()` methods, then defer `loadPreviousLaunches()` into its below-fold item block in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/HomeScreen.kt` and `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/ResponsiveHomeContent.kt`
- [X] T021 [US3] Verify build and scroll behavior: `./gradlew compileKotlinDesktop`, run desktop app, confirm below-fold sections show 0 API calls until scrolled into view

**Checkpoint**: US3 complete — stats, history, previous launches defer loading until visible. Above-fold loads unchanged.

---

## Phase 6: User Story 4 — Consolidated Launch Queries (Priority: P3)

**Goal**: FeaturedLaunchVM and LaunchesVM share a single upstream API call via `HomeDataCoordinator`, reducing `launches/` endpoint calls from 4 to 2 on Home load.

**Independent Test**: Network monitor shows ≤2 calls to `launches/` endpoint on Home load (1 shared upcoming+featured via coordinator, 1 in-flight) instead of 4.

### Implementation for User Story 4

- [ ] T022 [US4] Create `HomeDataCoordinator` class with `MutableSharedFlow<DataResult<PaginatedLaunchNormalList>>(replay=1)`, `loadHomeData()`, `featuredLaunches` flow (filter by statusIds, take 4), `upcomingLaunches` flow (take 10), `dataSource` flow, per `contracts/home-coordinator-contract.md` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/HomeDataCoordinator.kt`
- [ ] T023 [US4] Register `HomeDataCoordinator` as `single { HomeDataCoordinator(get()) }` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt`
- [ ] T024 [US4] Modify `FeaturedLaunchViewModel` to inject `HomeDataCoordinator` and collect `featuredLaunches` flow instead of calling `launchRepository.getFeaturedLaunch()` directly in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/FeaturedLaunchViewModel.kt`
- [ ] T025 [US4] Modify `LaunchesViewModel` to inject `HomeDataCoordinator` and collect `upcomingLaunches` flow instead of calling `launchRepository.getUpcomingLaunchesNormal()` directly (keep `launchRepository` for previous launches) in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/LaunchesViewModel.kt`
- [ ] T026 [US4] Update `HomeScreen.kt` top-level `LaunchedEffect` to call `homeDataCoordinator.loadHomeData()` instead of separate `featuredLaunchViewModel.loadFeaturedLaunch()` and `launchesViewModel.loadUpcomingLaunches()` calls; inject coordinator via `koinInject<HomeDataCoordinator>()` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/HomeScreen.kt`
- [ ] T027 [US4] Verify build and consolidated loading: `./gradlew compileKotlinDesktop`, run desktop app, confirm ≤2 `launches/` API calls on Home load

**Checkpoint**: US4 complete — FeaturedLaunchVM and LaunchesVM share single API call via HomeDataCoordinator.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Optional HTTP caching, final verification, cleanup.

- [ ] T028 [P] Optionally add `install(HttpCache)` to Ktor HttpClient for 60-second HTTP-level caching in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/NetworkModule.kt` (only if LL API returns `Cache-Control` headers — verify with curl first)
- [ ] T029 Run full verification checklist from `specs/012-reduce-api-calls-caching/quickstart.md`: returning user ≤5 API calls, preload ≤4 Tier 1, below-fold 0 until scrolled, stats from cache within 10 min, in-flight from cache within 5 min, debug short TTL works, CacheCleanupService cleans StatsCache, all platforms build
- [ ] T030 [P] Ensure all new/modified files follow conventional commit format: `perf(preload):` for US2, `feat(cache):` for US1, `refactor(home):` for US3/US4 — commit each user story phase as a separate conventional commit

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verify baseline
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS US1 (StatsCache needed for stats caching)
- **US1 (Phase 3)**: Depends on Phase 2 — uses StatsLocalDataSource and StatsCache
- **US2 (Phase 4)**: Depends on Phase 1 only — PreloadViewModel restructure is independent of cache additions, but benefits from US1 being in place so promoted Tier 1 tasks (getFeaturedLaunch, getUpcomingLaunchesNormal) are properly cached
- **US3 (Phase 5)**: Depends on Phase 1 only — deferred loading works without US1 cache, but benefits from it (deferred stats will use cached counts from US1)
- **US4 (Phase 6)**: Depends on US1 (Phase 3) — HomeDataCoordinator relies on repository cache patterns being in place
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

```
Phase 1: Setup
    │
Phase 2: Foundational (StatsCache, StatsLocalDataSource, DI, Cleanup)
    │
    ├── Phase 3: US1 (P1) — Cache repo methods, update StatsVM  🎯 MVP
    │       │
    │       └── Phase 6: US4 (P3) — HomeDataCoordinator (depends on US1 cache)
    │
    ├── Phase 4: US2 (P2) — Preload restructure (independent, benefits from US1)
    │
    └── Phase 5: US3 (P2) — Deferred below-fold (independent, benefits from US1)
                │
Phase 7: Polish (after all user stories)
```

### Within Each User Story

- Schema/query changes before LocalDataSource code
- LocalDataSource before Repository changes
- Repository before ViewModel changes
- ViewModel before UI changes
- Core implementation before integration/verification
- Story complete before moving to next priority

### Parallel Opportunities

**After Phase 2 completes:**
- US2 (Phase 4: T014-T016) can run in parallel with US1 (Phase 3: T007-T013) — different files
- US3 (Phase 5: T017-T021) can run in parallel with US1 — different files (HomeScreen vs Repository)
- T009 (Launch.sq in-flight queries) can run in parallel with T007-T008 (LaunchRepository interface/impl stats)

**Within US1:**
- T009 (Launch.sq) and T007 (LaunchRepository interface) can be done in parallel

**Within US3:**
- T018 + T019 are in the same file (ResponsiveHomeContent.kt) — must be sequential
- T017 (HomeScreen.kt) and T018 (ResponsiveHomeContent.kt) touch different files — can be parallel

---

## Parallel Example: Phases 3-5 After Foundation

```
# After Phase 2 (Foundational) completes:

# Developer A: US1 (P1 MVP — cache repo methods)
Task T007: Add getStatsCount() to LaunchRepository interface
Task T008: Implement getStatsCount() in LaunchRepositoryImpl
Task T009: Add in-flight cache queries to Launch.sq  (parallel with T007-T008)
Task T010: Add in-flight cache methods to LaunchLocalDataSource
Task T011: Add cache to getInFlightLaunches() in LaunchRepositoryImpl
Task T012: Update StatsViewModel to use getStatsCount()
Task T013: Verify US1

# Developer B (parallel): US2 (P2 — preload restructure)
Task T014: Restructure buildTier1Tasks() in PreloadViewModel
Task T015: Restructure buildTier2Tasks() in PreloadViewModel
Task T016: Verify US2

# Developer C (parallel): US3 (P2 — deferred loading)
Task T017: Remove stats from top-level LaunchedEffect in HomeScreen
Task T018: Add deferred LaunchedEffect in ResponsiveHomeContent quick_stats
Task T019: Move history loading into ResponsiveHomeContent history_section
Task T020: Defer previous launches loading
Task T021: Verify US3
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002-T006)
3. Complete Phase 3: User Story 1 (T007-T013)
4. **STOP and VALIDATE**: Returning user Home screen ≤5 API calls, stats and in-flight cached
5. Commit: `feat(cache): add stale-while-revalidate for stats and in-flight launches`

### Incremental Delivery

1. Foundation (T001-T006) → StatsCache infrastructure ready
2. US1 (T007-T013) → Cache working, returning user ≤5 calls → **MVP** → Commit
3. US2 (T014-T016) → Preload streamlined → Commit: `perf(preload): remove wasted tier 1 tasks and align with Home consumption`
4. US3 (T017-T021) → Below-fold deferred → Commit: `refactor(home): defer below-fold section loading until scrolled into view`
5. US4 (T022-T027) → Queries consolidated → Commit: `refactor(home): consolidate launch queries via HomeDataCoordinator`
6. Polish (T028-T030) → HTTP cache, verification → Commit: `chore: final verification and optional HTTP-level caching`
7. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies on incomplete same-phase tasks
- [Story] label maps task to specific user story from spec.md
- Each user story is independently completable and testable
- All cache implementations MUST follow the stale-while-revalidate pattern from `getFeaturedLaunch()` (Constitution Principle II)
- All cache additions MUST respect `AppPreferences.isDebugShortCacheTtlEnabled()` debug override (FR-007)
- Use conventional commits per user story: `feat(cache):`, `perf(preload):`, `refactor(home):`
- Commit after each completed user story phase, not after individual tasks
- Stop at any checkpoint to validate story independently
