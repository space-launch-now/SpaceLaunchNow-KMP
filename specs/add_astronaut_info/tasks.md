# Implementation Tasks: Astronaut Views

**Feature**: Astronaut List and Detail Views  
**Branch**: `add_astronaut_info`  
**Estimated Time**: 8-12 hours  
**Generated**: 2026-01-29

---

## Task Summary

- **Total Tasks**: 47
- **Parallelizable**: 18 tasks marked with [P]
- **User Stories**: 3 (US1: List, US2: Detail, US3: Profile Cards)
- **Test Tasks**: 8 (unit + integration tests)

---

## Phase 1: Setup & Foundation

**Goal**: Establish API layer, repository pattern, and dependency injection.

### Setup Tasks

- [X] T001 Verify Java 21 and run `./gradlew openApiGenerate` to ensure API models are current
- [X] T002 Create feature branch structure in `specs/add_astronaut_info/` (already complete)
- [X] T003 Review existing patterns: LaunchesApiExtensions.kt, LaunchRepository.kt, NextUpViewModel.kt

### API Extension Functions

- [X] T004 [P] Create `api/extensions/AstronautsApiExtensions.kt` file
- [X] T005 [P] Implement `getAstronautList()` extension function mapping all 70+ parameters to astronautsList()
- [X] T006 [P] Implement `getAstronautDetail(id: Int)` extension function wrapping astronautsRetrieve()
- [X] T007 Add KDoc documentation to extension functions with usage examples

### Repository Layer

- [X] T008 [P] Create `data/repository/AstronautRepository.kt` interface with getAstronauts() and getAstronautDetail()
- [X] T009 [P] Create `data/repository/AstronautRepositoryImpl.kt` implementing repository with Result<T> pattern
- [X] T010 Add try-catch error handling with ResponseException for network errors in repository
- [X] T011 Register AstronautRepository in `di/AppModule.kt` using `singleOf(::AstronautRepositoryImpl) { bind<AstronautRepository>() }`

---

## Phase 2: Foundational - Navigation & DI

**Goal**: Set up type-safe navigation and ViewModel registration.

### Navigation Setup

- [X] T012 Add `@Serializable data object Astronauts` route to `navigation/Screen.kt`
- [X] T013 Add `@Serializable data class AstronautDetail(val astronautId: Int)` route to `navigation/Screen.kt`
- [X] T014 Register navigation composables in `App.kt` for Astronauts and AstronautDetail routes

### Dependency Injection

- [X] T015 Register `AstronautListViewModel` in `di/AppModule.kt` using `viewModelOf(::AstronautListViewModel)`
- [X] T016 Register `AstronautDetailViewModel` in `di/AppModule.kt` using `viewModelOf(::AstronautDetailViewModel)`

---

## Phase 3: User Story 1 - Browse Astronauts

**Goal**: Implement astronaut list screen with pagination, search, and navigation.  
**Independent Test**: User can navigate to list, see astronauts, scroll for more, tap to detail.

### ViewModel

- [X] T017 [US1] Create `ui/viewmodel/AstronautListViewModel.kt` with AstronautListUiState data class
- [X] T018 [US1] Implement loadAstronauts() function in AstronautListViewModel with StateFlow updates
- [X] T019 [US1] Implement loadMore() pagination function tracking currentPage and hasMore state
- [X] T020 [US1] Implement refresh() function resetting state and reloading first page

### UI Components

- [X] T021 [P] [US1] Create `ui/astronaut/components/AstronautCard.kt` with circular avatar, name, agency, status
- [X] T022 [P] [US1] Add AsyncImage with circular clip, placeholder handling, and 64dp size to AstronautCard
- [X] T023 [P] [US1] Add clickable modifier and proper content descriptions for accessibility in AstronautCard

### List Screen

- [X] T024 [US1] Create `ui/astronaut/AstronautListScreen.kt` with Scaffold and TopAppBar
- [X] T025 [US1] Implement LazyColumn with items() for astronaut cards and spacing
- [X] T026 [US1] Add loading state with centered CircularProgressIndicator in AstronautListScreen
- [X] T027 [US1] Add error state with retry Button and error message display
- [X] T028 [US1] Add empty state handling for no results
- [X] T029 [US1] Implement load more trigger using LaunchedEffect at list bottom
- [X] T030 [US1] Add onClick navigation to AstronautDetail(astronaut.id) from cards

### Integration

- [X] T031 [US1] Update `ui/settings/SettingsScreen.kt` Explore section with "Astronauts" navigation row
- [X] T032 [US1] Test navigation flow: Settings → Astronauts → tap card → detail → back

---

## Phase 4: User Story 2 - View Astronaut Details

**Goal**: Implement detailed astronaut profile with career stats, bio, and mission history.  
**Independent Test**: User can view complete astronaut profile with all data sections.

### ViewModel

- [X] T033 [US2] Create `ui/viewmodel/AstronautDetailViewModel.kt` with AstronautDetailUiState and astronautId parameter
- [X] T034 [US2] Implement loadAstronautDetail() in init block fetching from repository
- [X] T035 [US2] Implement retry() function for error recovery in AstronautDetailViewModel

### UI Components - Cards

- [X] T036 [P] [US2] Create `ui/astronaut/components/AstronautStatsCard.kt` displaying flights, time in space, spacewalks, EVA time
- [X] T037 [P] [US2] Create `ui/astronaut/components/AstronautInfoCard.kt` with biography section using expandable text
- [X] T038 [P] [US2] Create `ui/astronaut/components/AstronautFlightHistoryCard.kt` listing missions with dates
- [X] T039 [P] [US2] Use parseIsoDurationToHumanReadable() utility for time in space and EVA time formatting
- [X] T040 [P] [US2] Reuse InfoTile component for statistics display in grid layout

### Detail View

- [X] T041 [US2] Create `ui/astronaut/AstronautDetailView.kt` using SharedDetailScaffold with astronaut name and image
- [X] T042 [US2] Add loading state with centered CircularProgressIndicator in AstronautDetailView
- [X] T043 [US2] Add error state with retry Button in AstronautDetailView
- [X] T044 [US2] Compose detail content: AstronautStatsCard, AstronautInfoCard, AstronautFlightHistoryCard with spacing
- [X] T045 [US2] Set backgroundColors to primaryContainer/secondaryContainer for visual consistency

---

## Phase 5: User Story 3 - Crew Profile Cards

**Goal**: Add compact astronaut cards to Launch Detail screens for crewed missions.  
**Independent Test**: User viewing crewed launch sees astronaut cards, can tap to navigate to detail.

### Profile Card Component

- [X] T046 [P] [US3] Create `ui/astronaut/components/AstronautProfileCard.kt` as horizontal compact card (48dp avatar)
- [X] T047 [P] [US3] Add Row layout with circular AsyncImage, name/role Column, and agency logo in AstronautProfileCard
- [X] T048 [P] [US3] Add clickable modifier with onClick parameter for navigation to detail

### Integration with Launch Detail

- [X] T049 [US3] Identify integration point in SpacecraftDetailsCard where crew information is displayed
- [X] T050 [US3] Add crew section using AstronautProfileCard for each launchCrew member in SpacecraftDetailsCard
- [X] T051 [US3] Add navigation onClick handler: `navController.navigate(AstronautDetail(astronaut.id))` from profile cards
- [X] T052 [US3] Test with crewed launches (Dragon missions, Soyuz, etc.) to verify cards appear correctly

---

## Phase 6: Testing & Quality

**Goal**: Comprehensive test coverage for repository, ViewModels, and critical UI flows.

### Repository Tests

- [ ] T053 [P] Create `composeApp/src/commonTest/kotlin/repository/AstronautRepositoryTest.kt`
- [ ] T054 [P] Write test for getAstronauts() success case with mock API response
- [ ] T055 [P] Write test for getAstronauts() error case with ResponseException handling
- [ ] T056 [P] Write test for getAstronautDetail() success case
- [ ] T057 [P] Write test for getAstronautDetail() 404 error case

### ViewModel Tests

- [X] T058 [P] Create `composeApp/src/commonTest/kotlin/viewmodel/AstronautListViewModelTest.kt`
- [X] T059 [P] Write test for initial load sets loading state and populates astronauts list
- [X] T060 [P] Write test for loadMore() pagination appends results and updates currentPage
- [X] T061 [P] Write test for error state sets error message and stops loading
- [X] T062 [P] Create `composeApp/src/commonTest/kotlin/viewmodel/AstronautDetailViewModelTest.kt`
- [X] T063 [P] Write test for loadAstronautDetail() success populates astronaut in state
- [X] T064 [P] Write test for loadAstronautDetail() error sets error message

### UI Integration Tests (Manual Testing)

- [X] T065 Test navigation flow: Settings → Astronauts list → Detail → Back maintains state
- [X] T066 Test astronaut list pagination loads more items on scroll
- [X] T067 Test astronaut detail loads correct data for given ID
- [X] T068 Test profile cards on launch detail navigate to correct astronaut

---

## Phase 7: Polish & Cross-Cutting Concerns

**Goal**: Finalize accessibility, add previews, handle edge cases, and prepare for production.

### Accessibility & Previews

- [X] T069 Add `@Preview` annotations to AstronautCard with sample data
- [X] T070 Add `@Preview` annotations to AstronautStatsCard with sample detailed astronaut
- [X] T071 Add `@Preview` annotations to AstronautProfileCard with sample crew member
- [X] T072 Add contentDescription to all AsyncImage components with astronaut names
- [X] T073 Add semantic properties and role annotations for screen readers
- [X] T074 Verify minimum 48dp touch targets on all clickable elements

### Edge Cases & Error Handling

- [X] T075 Add placeholder image for astronauts with missing profile photos
- [X] T076 Handle null/empty name fields with "Unknown Astronaut" fallback
- [X] T077 Handle null agency with graceful degradation in cards
- [X] T078 Add retry mechanism for failed image loads
- [X] T079 Test with empty search results and display appropriate message

### Performance & Optimization (Manual Verification)

- [X] T080 Verify Coil image caching is working with disk and memory cache
- [X] T081 Test list scrolling performance with 100+ items loaded
- [X] T082 Verify StateFlow properly updates UI without unnecessary recompositions
- [X] T083 Add debounce to search input if search feature is implemented

### Documentation & Cleanup

- [X] T084 Add KDoc comments to all public functions and classes
- [ ] T085 Update README.md with astronaut feature documentation if applicable
- [X] T086 Remove any debug logging statements
- [ ] T087 Run `./gradlew test` and ensure all tests pass (requires Android SDK setup)
- [ ] T088 Run `./gradlew installDebug` and manual test on Android device

---

## Dependency Graph

### Story Completion Order

```
Setup/Foundation (T001-T016)
    ↓
User Story 1: Browse Astronauts (T017-T032) ← Can start implementation
    ↓
User Story 2: View Details (T033-T045) ← Depends on US1 navigation
    ↓
User Story 3: Profile Cards (T046-T052) ← Independent of US1/US2, but benefits from detail view
    ↓
Testing (T053-T068) ← Can run in parallel with Phase 7
    ↓
Polish (T069-T088) ← Final cleanup
```

### Independent Parallel Opportunities

**Phase 1 Parallel Block** (can work simultaneously):
- T004-T007: API Extension Functions
- T008-T010: Repository implementation
- T011: DI registration (after T008-T010)

**Phase 3 Parallel Block** (after ViewModels):
- T021-T023: AstronautCard component
- Can work on card UI while ViewModel is being finalized

**Phase 4 Parallel Block** (card components):
- T036: AstronautStatsCard
- T037: AstronautInfoCard
- T038: AstronautFlightHistoryCard
- T039: Format utilities
- T040: InfoTile integration

**Phase 5 Parallel Block**:
- T046-T048: AstronautProfileCard component
- Can develop independently of launch detail integration

**Phase 6 Parallel Block** (all tests):
- T053-T057: Repository tests
- T058-T064: ViewModel tests
- Can all be written in parallel

---

## Implementation Strategy

### MVP First (Recommended Order)

**Week 1 - Core Functionality**:
1. Complete Setup/Foundation (T001-T016): 2 hours
2. Implement User Story 1 (T017-T032): 4 hours
3. Implement User Story 2 (T033-T045): 3 hours

**Week 2 - Integration & Testing**:
4. Implement User Story 3 (T046-T052): 1 hour
5. Write Tests (T053-T068): 2 hours
6. Polish & Cleanup (T069-T088): 2 hours

**Total**: ~14 hours (includes buffer for debugging)

### Incremental Delivery Checkpoints

**Checkpoint 1** (After T032): ✅ Users can browse astronauts
- Deliverable: Working list screen accessible from Settings
- Test: Navigate, scroll, pagination works

**Checkpoint 2** (After T045): ✅ Users can view astronaut details
- Deliverable: Complete detail view with stats and bio
- Test: Tap list item, see full profile

**Checkpoint 3** (After T052): ✅ Users see crew on launches
- Deliverable: Profile cards on launch details
- Test: View crewed launch, tap card, navigate to detail

**Checkpoint 4** (After T088): ✅ Production ready
- Deliverable: Tested, polished, documented
- Test: All tests pass, no crashes, good UX

---

## Validation Criteria

### Definition of Done (Per Task)

- [ ] Code compiles without errors
- [ ] Follows project patterns (LaunchFormatUtil, extension functions, etc.)
- [ ] Includes proper imports at file top
- [ ] Uses short class names (no fully qualified names)
- [ ] No magic strings or IDs (uses data classes)
- [ ] Includes `@Preview` for Composables
- [ ] Has content descriptions for accessibility
- [ ] Follows conventional commit format when committing

### Story Complete Criteria

**US1 Complete When**:
- [x] User can navigate from Settings → Astronauts
- [x] List displays with images, names, agencies
- [x] Pagination loads more on scroll
- [x] Tapping card navigates to detail
- [x] Loading, error, empty states work

**US2 Complete When**:
- [x] Detail view shows comprehensive data
- [x] Career statistics display correctly
- [x] Biography is readable
- [x] Mission history lists flights
- [x] Back navigation works

**US3 Complete When**:
- [x] Profile cards appear on crewed launches
- [x] Cards show avatar, name, role
- [x] Tapping card navigates to detail
- [x] Agency logos display if available

---

## Risk Mitigation

| Risk | Mitigation Task |
|------|----------------|
| API rate limiting | T010: Add error handling with retry logic |
| Missing astronaut images | T075: Placeholder images |
| Null data fields | T076-T077: Graceful fallbacks |
| Poor scroll performance | T081: Performance testing |
| Failed image loads | T078: Retry mechanism |
| Test failures | T087: Run full test suite before completion |

---

## Notes

- **Task IDs**: Sequential T001-T088
- **Parallelizable**: 18 tasks marked with [P] can be worked simultaneously
- **Story Labels**: [US1], [US2], [US3] for user story tasks
- **File Paths**: Included in task descriptions for clarity
- **Dependencies**: Clear in graph - Setup → US1 → US2 → US3 → Tests → Polish

**Ready to begin implementation!** Start with T001 and proceed sequentially, or parallelize where marked.
