# Tasks: Live Favorite Launches Card

**Input**: Design documents from `/specs/010-live-favorite-launches/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/in-flight-api.md ✅, quickstart.md ✅

**Tests**: Not requested in spec - implementation tasks only.

**Organization**: Tasks organized by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- File paths relative to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 1: Foundational (Blocking Prerequisites)

**Purpose**: Repository method that ALL user stories depend on

**⚠️ CRITICAL**: No UI work can begin until this phase is complete

- [X] T001 Add `getInFlightLaunches()` method signature to `data/repository/LaunchRepository.kt`
- [X] T002 Implement `getInFlightLaunches()` with status_ids=6 filter in `data/repository/LaunchRepositoryImpl.kt`

**Checkpoint**: Repository can now fetch in-flight launches - UI implementation can begin

---

## Phase 2: User Story 1+2+3 - LIVE Launch Card (Priority: P1) 🎯 MVP

**Goal**: Display a visually distinct LIVE card when an in-flight launch matches user filters, with navigation to launch details

**Stories Covered**:
- US1: View Live Launch Card
- US2: LIVE Card Visual Distinction  
- US3: Navigate to Live Launch Details

**Independent Test**: When a launch has status_id=6 and matches user filters, a LIVE card with animated badge and Blue 500 border appears. Tapping navigates to launch detail page.

### Implementation for User Story 1+2+3

- [X] T003 [P] [US1+2] Create `LiveIndicator.kt` Composable with pulsing animation in `ui/compose/LiveIndicator.kt`
- [X] T004 [P] [US1+2] Create `LiveLaunchCard.kt` Composable with Blue 500 border in `ui/home/components/LiveLaunchCard.kt`
- [X] T005 [US1] Add `inFlightLaunchState: StateFlow<ViewState<LaunchNormal?>>` to `ui/viewmodel/HomeViewModel.kt`
- [X] T006 [US1] Implement `loadInFlightLaunch()` function in `ui/viewmodel/HomeViewModel.kt`
- [X] T007 [US1] Call `loadInFlightLaunch()` from `loadHomeScreenData()` in `ui/viewmodel/HomeViewModel.kt`
- [X] T008 [US3] Add click handler with navigation to LaunchDetail in `ui/home/components/LiveLaunchCard.kt`

**Checkpoint**: LIVE card components exist and ViewModel exposes in-flight launch state

---

## Phase 3: User Story 4 - Home Screen Integration (Priority: P2)

**Goal**: Position LIVE card at top of home page, above featured launch

**Independent Test**: With a LIVE launch active, the LIVE card appears as the first visible item on Home screen, above the existing NextLaunchView.

### Implementation for User Story 4

- [X] T009 [US4] Add LIVE card as first item in Home screen LazyColumn in `ui/screens/HomeScreen.kt`
- [X] T010 [US4] Conditionally render LIVE card only when `inFlightLaunchState.data != null` in `ui/screens/HomeScreen.kt`
- [X] T011 [US4] Add spacing between LIVE card and NextLaunchView in `ui/screens/HomeScreen.kt`

**Checkpoint**: LIVE card appears at top of home page when in-flight launch exists

---

## Phase 4: User Story 5 - Multiple In-Flight Launches (Priority: P3)

**Goal**: Handle edge case when multiple launches are in flight simultaneously

**Independent Test**: When 2+ launches are in-flight matching filters, at least one LIVE card is displayed (first match).

### Implementation for User Story 5

- [X] T012 [US5] Update repository to return full list from `getInFlightLaunches()` in `data/repository/LaunchRepositoryImpl.kt`
- [X] T013 [US5] Select first in-flight launch for display (future: could show carousel) in `ui/viewmodel/HomeViewModel.kt`

**Checkpoint**: Multiple in-flight launches handled gracefully

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Previews, accessibility, and code quality

- [X] T014 [P] Add light theme `@Preview` for LiveIndicator in `ui/compose/LiveIndicator.kt`
- [X] T015 [P] Add dark theme `@Preview` for LiveIndicator in `ui/compose/LiveIndicator.kt`
- [X] T016 [P] Add light theme `@Preview` for LiveLaunchCard in `ui/home/components/LiveLaunchCard.kt`
- [X] T017 [P] Add dark theme `@Preview` for LiveLaunchCard in `ui/home/components/LiveLaunchCard.kt`
- [X] T018 [P] Add contentDescription for accessibility to LiveIndicator badge in `ui/compose/LiveIndicator.kt`
- [X] T019 [P] Add contentDescription for launch image in LiveLaunchCard in `ui/home/components/LiveLaunchCard.kt`
- [ ] T020 Verify build compiles with `./gradlew compileKotlinDesktop`
- [ ] T021 Run quickstart.md verification steps

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Foundational)
    │
    ▼
Phase 2 (US1+2+3: LIVE Card MVP)  ◄── 🎯 MVP COMPLETE HERE
    │
    ▼
Phase 3 (US4: Home Integration)
    │
    ▼
Phase 4 (US5: Multiple Launches)
    │
    ▼
Phase 5 (Polish)
```

### Task Dependencies Within Phases

**Phase 1**: T001 → T002 (interface before implementation)

**Phase 2**:
- T003, T004 can run in parallel (different files)
- T005 → T006 → T007 (must be sequential in ViewModel)
- T008 depends on T004 (LiveLaunchCard exists)
- T003-T008 all depend on T001-T002 (repository must exist)

**Phase 3**: T009 → T010 → T011 (sequential integration)

**Phase 4**: T012 → T013 (repository before ViewModel)

**Phase 5**: T014-T019 can ALL run in parallel (different concerns)

### Parallel Opportunities

```bash
# After Phase 1 completes, run T003 and T004 in parallel:
# - T003: LiveIndicator.kt (new file)
# - T004: LiveLaunchCard.kt (new file)

# All Polish tasks (T014-T019) can run in parallel
```

---

## Implementation Strategy

### MVP Scope (Recommended First Delivery)

**Phases 1-2 Only** (~25 minutes):
- Repository method (T001-T002)
- LiveIndicator + LiveLaunchCard components (T003-T004)
- HomeViewModel state and loading (T005-T007)
- Navigation (T008)

This delivers a fully functional LIVE card feature. Integration (Phase 3) can be added immediately after.

### Task Count Summary

| Phase | Tasks | Parallelizable |
|-------|-------|----------------|
| Phase 1: Foundational | 2 | 0 |
| Phase 2: LIVE Card MVP | 6 | 2 |
| Phase 3: Integration | 3 | 0 |
| Phase 4: Multiple Launches | 2 | 0 |
| Phase 5: Polish | 8 | 6 |
| **Total** | **21** | **8** |

### File Summary

| File | Action | Tasks |
|------|--------|-------|
| `data/repository/LaunchRepository.kt` | Modify | T001 |
| `data/repository/LaunchRepositoryImpl.kt` | Modify | T002, T012 |
| `ui/compose/LiveIndicator.kt` | **Create** | T003, T014, T015, T018 |
| `ui/home/components/LiveLaunchCard.kt` | **Create** | T004, T008, T016, T017, T019 |
| `ui/viewmodel/HomeViewModel.kt` | Modify | T005, T006, T007, T013 |
| `ui/screens/HomeScreen.kt` | Modify | T009, T010, T011 |

---

## Commit Strategy

Following conventional commits per project guidelines:

```bash
# After Phase 1
git commit -m "feat(data): add getInFlightLaunches repository method"

# After Phase 2
git commit -m "feat(home): add LIVE launch card with animated indicator"

# After Phase 3
git commit -m "feat(home): integrate LIVE card at top of home screen"

# After Phase 4
git commit -m "feat(home): handle multiple in-flight launches"

# After Phase 5
git commit -m "chore(ui): add previews and accessibility for LIVE card"
```
