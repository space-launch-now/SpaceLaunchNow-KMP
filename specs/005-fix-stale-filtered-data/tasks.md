# Tasks: Fix Stale Filtered Data on Cold Start

**Input**: Design documents from `/specs/005-fix-stale-filtered-data/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Not requested in spec — test tasks omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

- **Common business logic**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`
- **Android widgets**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/`
- **iOS bridge**: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/`
- **iOS widget (Swift)**: `iosApp/LaunchWidget/`

---

## Phase 1: Setup

**Purpose**: No setup required — this is a bug fix using existing infrastructure, patterns, and dependencies.

(No tasks in this phase)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: No foundational tasks required — all dependencies (`LaunchFilterService`, `NotificationStateStorage`, `filterLaunchesByPreferences()`) already exist and are registered in Koin.

(No tasks in this phase)

---

## Phase 3: User Story 1 — Homepage Respects Filters on Cold Start (Priority: P1) 🎯 MVP

**Goal**: Ensure `LaunchRepositoryImpl` applies `filterLaunchesByPreferences()` to the fresh cache path in all three methods, so cold start returns filtered data.

**Independent Test**: Set agency filter → kill app → reopen → homepage shows only launches from that agency. Enable "Follow All" → all launches shown.

### Implementation for User Story 1

- [x] T001 [US1] Apply `filterLaunchesByPreferences()` to fresh cache path in `getFeaturedLaunch()` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt`
- [x] T002 [US1] Apply `filterLaunchesByPreferences()` to fresh cache path in `getUpcomingLaunchesNormal()` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt`
- [x] T003 [US1] Apply `filterLaunchesByPreferences()` to fresh cache path in `getPreviousLaunchesNormal()` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt`

**Details for T001–T003**: In each method's fresh cache section, after retrieving `cachedLaunches` from `localDataSource`, call `filterLaunchesByPreferences(cachedLaunches, agencyIds, locationIds)`. If the filtered result is non-empty, return it with `DataSource.CACHE`. If filtered to empty, fall through to the stale cache / API path (do not return early). This matches the existing stale cache path pattern. Add a log statement noting the filter was applied. See `quickstart.md` Fix 1 for exact code pattern.

**Checkpoint**: Homepage cold start now returns filtered launches. US-1 is fully functional and testable.

---

## Phase 4: User Story 2 — Widgets Respect User Filter Preferences (Priority: P2)

**Goal**: Android and iOS widgets read user filter preferences and pass them to repository calls, so widget content matches the user's filtered view.

**Independent Test**: Set agency filter → add NextUp widget → widget shows only launches from that agency. Change filters → widget updates on next refresh.

### Implementation for User Story 2

- [x] T004 [P] [US2] Add filter-aware data fetching to `NextUpWidget.fetchNextLaunch()` in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/NextUpWidget.kt`
- [x] T005 [P] [US2] Add filter-aware data fetching to `LaunchListWidget.fetchUpcomingLaunches()` in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/widgets/LaunchListWidget.kt`
- [x] T006 [P] [US2] Update `KoinHelper.fetchUpcomingLaunchesOrNull()` to read and apply filter preferences in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/KoinInitializer.kt`

**Details for T004–T005**: Inject `NotificationStateStorage` and `LaunchFilterService` via `koinInject()` (same pattern as existing `LaunchRepository` injection). Read `notificationStateStorage.state.first()`, extract `agencyIds` and `locationIds` via `LaunchFilterService`, and pass them to the `getUpcomingLaunchesNormal()` call. Add necessary imports for `NotificationStateStorage`, `LaunchFilterService`, and `kotlinx.coroutines.flow.first`. See `quickstart.md` Fix 2 for exact code pattern.

**Details for T006**: Inject `NotificationStateStorage` and `LaunchFilterService` via `getKoin().get<>()` inside `KoinHelper`. Read notification state, extract filter params, and pass `agencyIds`/`locationIds` to the existing `launchRepository.getUpcomingLaunchesNormal()` call. No Swift code changes needed — `LaunchData.swift` calls `fetchUpcomingLaunchesOrNull(limit:)` which now handles filtering internally. See `quickstart.md` Fix 3 for exact code pattern.

**Checkpoint**: Both Android and iOS widgets now display filtered launches matching user preferences. US-2 is fully functional and testable.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Verification and documentation

- [x] T007 Manual verification: test all 5 acceptance criteria from spec.md (cold start filtering, widget filtering, filter change refresh, Follow All bypass, API fetch regression)
- [x] T008 Run quickstart.md validation — verify commit message format follows CI/CD conventions

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Empty — no setup needed
- **Phase 2 (Foundational)**: Empty — all dependencies exist
- **Phase 3 (US-1)**: No dependencies — can start immediately. Tasks T001–T003 operate on the same file but different methods; they can be done sequentially in a single edit session
- **Phase 4 (US-2)**: No dependency on Phase 3 — US-2 can proceed in parallel with US-1 since they modify different files. However, T004/T005 benefit from T001–T003 being complete (repository will properly filter cached data returned to widgets)
- **Phase 5 (Polish)**: Depends on Phase 3 + Phase 4 completion

### User Story Dependencies

- **US-1 (P1)**: Independent — modifies only `LaunchRepositoryImpl.kt`
- **US-2 (P2)**: Logically depends on US-1 (widgets call the repository, which needs to filter properly), but the widget-side changes (injecting filters into the call) are in different files and can be developed in parallel

### Within Each User Story

- **US-1**: T001, T002, T003 operate on the same file — execute sequentially
- **US-2**: T004, T005, T006 operate on different files — all marked [P] for parallel execution

### Parallel Opportunities

- T004, T005, T006 (all [P]) can be implemented simultaneously — they modify `NextUpWidget.kt`, `LaunchListWidget.kt`, and `KoinInitializer.kt` respectively
- US-1 and US-2 can be developed in parallel by different team members (different files)

---

## Parallel Example: User Story 2

```
# All three widget fixes can be done simultaneously:
Task T004: Add filter-aware fetch to NextUpWidget.kt
Task T005: Add filter-aware fetch to LaunchListWidget.kt
Task T006: Update KoinHelper.fetchUpcomingLaunchesOrNull in KoinInitializer.kt
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 3: User Story 1 (T001–T003)
2. **STOP and VALIDATE**: Set filters → kill app → reopen → verify filtered homepage
3. This alone fixes the primary user-reported bug (stale unfiltered data on cold start)

### Full Fix (Both User Stories)

4. Complete Phase 4: User Story 2 (T004–T006)
5. **VALIDATE**: Verify widgets display filtered launches
6. Complete Phase 5: Polish (T007–T008)
7. Commit with: `fix(data): apply user filters to fresh cache and widget data`
