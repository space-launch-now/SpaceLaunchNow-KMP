# Tasks: News and Events Screen

**Input**: Design documents from `/specs/008-news-events-screen/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/interfaces.md ✅, quickstart.md ✅

**Tests**: NOT requested - implementation tasks only

**Organization**: Tasks grouped by user story to enable independent implementation and testing

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1-US6)
- Include exact file paths in descriptions

## Path Conventions

Base path: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create directory structure and foundational files

- [ ] T001 Create newsevents UI directory structure at `ui/newsevents/` and `ui/newsevents/components/`
- [X] T002 [P] Create InfoApiExtensions.kt for SNAPI info endpoint at `api/snapi/extensions/InfoApiExtensions.kt`
- [X] T003 [P] Update EventsApiExtensions.kt with offset and search parameters at `api/extensions/EventsApiExtensions.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Repository layer and DI setup that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Create InfoRepository interface at `data/repository/InfoRepository.kt`
- [X] T005 Create InfoRepositoryImpl implementation at `data/repository/InfoRepositoryImpl.kt`
- [X] T006 Update ArticlesRepository.kt to add `getArticlesPaginated()` method signature at `data/repository/ArticlesRepository.kt`
- [X] T007 Update ArticlesRepositoryImpl.kt to implement `getArticlesPaginated()` at `data/repository/ArticlesRepositoryImpl.kt`
- [X] T008 Update EventsRepository.kt to add `getEventsPaginated()` method signature at `data/repository/EventsRepository.kt`
- [X] T009 Update EventsRepositoryImpl.kt to implement `getEventsPaginated()` at `data/repository/EventsRepositoryImpl.kt`
- [X] T010 Register InfoApi in ApiModule.kt at `di/ApiModule.kt`
- [X] T011 Register InfoRepository binding in AppModule.kt at `di/AppModule.kt`
- [X] T012 Add Screen.NewsEvents navigation route at `navigation/Screen.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Browse Space News Articles (Priority: P1) 🎯 MVP

**Goal**: Users can view and scroll through news articles with images, titles, and source information

**Independent Test**: Launch app, navigate to News & Events tab, verify articles display with images, titles, and source info. Tap article to confirm it opens external browser.

### Implementation for User Story 1

- [X] T013 Create NewsEventsTab enum and NewsEventsUiState data class at `ui/viewmodel/NewsEventsViewModel.kt`
- [X] T014 Implement NewsEventsViewModel with news loading, pagination, and refresh at `ui/viewmodel/NewsEventsViewModel.kt`
- [X] T015 Register NewsEventsViewModel in AppModule.kt at `di/AppModule.kt`
- [X] T016 [P] [US1] Create NewsListItem composable with image, title, source, date at `ui/newsevents/components/NewsListItem.kt`
- [X] T017 [P] [US1] Add dual previews (light/dark) for NewsListItem at `ui/newsevents/components/NewsListItem.kt`
- [X] T018 [US1] Create NewsEventsScreen with TabRow (News/Events tabs) at `ui/newsevents/NewsEventsScreen.kt`
- [X] T019 [US1] Implement news list with LazyColumn and pagination trigger at `ui/newsevents/NewsEventsScreen.kt`
- [X] T020 [US1] Add article tap handler to open URL in external browser at `ui/newsevents/NewsEventsScreen.kt`
- [X] T021 [US1] Add loading, error, and empty states for news tab at `ui/newsevents/NewsEventsScreen.kt`
- [X] T022 [US1] Add pull-to-refresh support for news list at `ui/newsevents/NewsEventsScreen.kt`
- [X] T023 [US1] Add dual previews (light/dark) for NewsEventsScreen at `ui/newsevents/NewsEventsScreen.kt`
- [X] T024 [US1] Add NewsEventsScreen composable entry in App.kt at `App.kt`
- [X] T025 [US1] Wire NewsEvents navigation from bottom navigation/menu at `App.kt`

**Checkpoint**: User Story 1 complete - users can browse and read news articles

---

## Phase 4: User Story 2 - Search News and Events (Priority: P2)

**Goal**: Users can search for specific topics across news and events

**Independent Test**: Open News & Events screen, type "Starship" in search bar, verify only matching articles appear

### Implementation for User Story 2

- [ ] T026 [US2] Add searchQuery state and updateSearchQuery() to ViewModel at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T027 [US2] Implement 300ms search debouncing with kotlinx.coroutines at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T028 [US2] Add search bar to NewsEventsScreen header at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T029 [US2] Add clear search button functionality at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T030 [US2] Add "no results" empty state for search at `ui/newsevents/NewsEventsScreen.kt`

**Checkpoint**: User Story 2 complete - search works for both tabs

---

## Phase 5: User Story 3 - Browse Space Events (Priority: P2)

**Goal**: Users can see upcoming space events with event details and navigate to event detail

**Independent Test**: Navigate to Events tab, verify events display with images, titles, dates, and event types. Tap event to confirm navigation to Event Detail screen.

### Implementation for User Story 3

- [ ] T031 [P] [US3] Create EventListItem composable with image, name, date, type at `ui/newsevents/components/EventListItem.kt`
- [ ] T032 [P] [US3] Add dual previews (light/dark) for EventListItem at `ui/newsevents/components/EventListItem.kt`
- [ ] T033 [US3] Add events loading and pagination to ViewModel at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T034 [US3] Implement events list with LazyColumn in Events tab at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T035 [US3] Add event tap handler to navigate to EventDetail screen at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T036 [US3] Add loading, error, and empty states for events tab at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T037 [US3] Add pull-to-refresh support for events list at `ui/newsevents/NewsEventsScreen.kt`

**Checkpoint**: User Story 3 complete - users can browse events and navigate to details

---

## Phase 6: User Story 4 - Filter Events by Type (Priority: P3)

**Goal**: Users can filter events by type (Spacewalk, Docking, etc.)

**Independent Test**: Open Events tab, tap filter button, select "Spacewalk" type, verify only spacewalk events display

### Implementation for User Story 4

- [ ] T038 [US4] Add event types loading from ConfigRepository to ViewModel at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T039 [US4] Add selectedEventTypes state and toggle methods to ViewModel at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T040 [US4] Create NewsEventsFilterBottomSheet composable at `ui/newsevents/NewsEventsFilterBottomSheet.kt`
- [ ] T041 [US4] Implement event type filter chips in bottom sheet at `ui/newsevents/NewsEventsFilterBottomSheet.kt`
- [ ] T042 [US4] Add clear filters and apply actions to bottom sheet at `ui/newsevents/NewsEventsFilterBottomSheet.kt`
- [ ] T043 [US4] Add dual previews (light/dark) for NewsEventsFilterBottomSheet at `ui/newsevents/NewsEventsFilterBottomSheet.kt`
- [ ] T044 [US4] Add filter icon button to Events tab header at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T045 [US4] Add filter badge showing active filter count at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T046 [US4] Wire filter bottom sheet show/hide in NewsEventsScreen at `ui/newsevents/NewsEventsScreen.kt`

**Checkpoint**: User Story 4 complete - event type filtering works

---

## Phase 7: User Story 5 - Filter News by Source (Priority: P3)

**Goal**: Users can filter news articles by source (SpaceNews, NASASpaceflight, etc.)

**Independent Test**: Open News tab, tap filter button, select "SpaceNews", verify only SpaceNews articles appear

### Implementation for User Story 5

- [ ] T047 [US5] Add news sites loading from InfoRepository to ViewModel at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T048 [US5] Add selectedNewsSites state and toggle methods to ViewModel at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T049 [US5] Implement news site filter chips in bottom sheet at `ui/newsevents/NewsEventsFilterBottomSheet.kt`
- [ ] T050 [US5] Add filter icon button to News tab header at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T051 [US5] Update filter badge to show news filter count on News tab at `ui/newsevents/NewsEventsScreen.kt`

**Checkpoint**: User Story 5 complete - news source filtering works

---

## Phase 8: User Story 6 - Toggle Upcoming/Past Events (Priority: P3)

**Goal**: Users can toggle between upcoming and past events

**Independent Test**: On Events tab, toggle to "Past" events, verify events displayed have dates in the past

### Implementation for User Story 6

- [ ] T052 [US6] Add showUpcomingEvents state and toggle method to ViewModel at `ui/viewmodel/NewsEventsViewModel.kt`
- [ ] T053 [US6] Add upcoming/past toggle chips to filter bottom sheet at `ui/newsevents/NewsEventsFilterBottomSheet.kt`
- [ ] T054 [US6] Update events query to filter by upcoming/past at `ui/viewmodel/NewsEventsViewModel.kt`

**Checkpoint**: User Story 6 complete - upcoming/past toggle works

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final integration and quality improvements

- [ ] T055 [P] Add accessibility contentDescription to all interactive elements in NewsEventsScreen at `ui/newsevents/NewsEventsScreen.kt`
- [ ] T056 [P] Add accessibility contentDescription to NewsListItem and EventListItem at `ui/newsevents/components/`
- [ ] T057 Verify all tap targets meet 48dp minimum accessibility requirement at `ui/newsevents/`
- [ ] T058 Run quickstart.md verification steps to validate feature at `specs/008-news-events-screen/quickstart.md`
- [ ] T059 Verify dual previews render correctly in both light and dark themes

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup
    ↓
Phase 2: Foundational (BLOCKS all user stories)
    ↓
Phase 3: US1 - Browse News (MVP) ←────────────────┐
    ↓                                              │
Phase 4: US2 - Search (depends on US1 UI)         │ Can start in parallel
    ↓                                              │ after US1 core is done
Phase 5: US3 - Browse Events ←────────────────────┘
    ↓
Phase 6: US4 - Filter Events (depends on US3)
    ↓
Phase 7: US5 - Filter News (depends on US1)
    ↓
Phase 8: US6 - Toggle Events (depends on US3, US4)
    ↓
Phase 9: Polish
```

### User Story Dependencies

| User Story | Depends On | Can Start After |
|------------|------------|-----------------|
| US1 (P1) | Foundational only | T012 complete |
| US2 (P2) | US1 screen created | T018 complete |
| US3 (P2) | US1 screen created | T018 complete |
| US4 (P3) | US3 events working | T037 complete |
| US5 (P3) | US1 news working | T025 complete |
| US6 (P3) | US4 filter sheet | T046 complete |

### Parallel Opportunities

Within each phase, tasks marked [P] can run in parallel when they touch different files.

---

## Parallel Example: Phase 1 Setup

```bash
# All Phase 1 tasks can run in parallel (different files):
T001: Create directory structure
T002: InfoApiExtensions.kt
T003: EventsApiExtensions.kt
```

## Parallel Example: User Story 1 Components

```bash
# These US1 tasks work on different files:
T016: NewsListItem.kt
T017: NewsListItem previews (same file as T016, sequential)
# Then T018-T025 are sequential (same screen file)
```

## Parallel Example: User Story 3 Components

```bash
# After US1 screen exists, US3 component work:
T031: EventListItem.kt (parallel with T033)
T033: ViewModel events loading
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T003)
2. Complete Phase 2: Foundational (T004-T012) — **CRITICAL GATE**
3. Complete Phase 3: User Story 1 (T013-T025)
4. **STOP and VALIDATE**: Test news browsing independently
5. Deploy/demo if ready

### Incremental Delivery

| Milestone | Stories Complete | User Value |
|-----------|-----------------|------------|
| MVP | US1 | Browse news articles |
| Search | US1 + US2 | Search news |
| Events | US1 + US2 + US3 | Full browsing |
| Filters | US1-US6 | Full feature |

### Recommended Order (Single Developer)

1. Phase 1 → Phase 2 → Phase 3 (MVP)
2. Add US2 (search enhances US1)
3. Add US3 (events tab completes screen)
4. Add US4, US5, US6 (filters are polish)
5. Phase 9 (final polish)

---

## Notes

- Reference AgencyListScreen.kt for canonical patterns (pagination, filters, viewmodel)
- All new composables MUST have dual previews (light + dark theme)
- Use existing `SearchBar` component from `ui/components/`
- Use `LocalUriHandler` for opening article URLs in browser
- Debounce search by 300ms following AgencyListViewModel pattern
- PAGE_SIZE = 20 for pagination (matches AgencyListScreen)
- Filter selections persist for session (in ViewModel state, not persisted)
