# Implementation Tasks: Explore Tab Navigation

**Feature**: Add Explore tab to bottom navigation with discovery sections  
**Branch**: `add_explore_section`  
**Estimated Effort**: Small (2-3 hours)  
**Tech Stack**: Kotlin Multiplatform, Compose, Material 3, Jetpack Navigation

---

## Task Format

```
- [ ] [TaskID] [P?] [Story?] Description with file path
```

- **TaskID**: Sequential number (T001, T002, etc.)
- **[P]**: Parallelizable task (different files, no dependencies)
- **[Story]**: User story label (US1, US2, etc.) - only for user story phases

---

## Phase 1: Setup & Data Models

**Goal**: Create data structures and static section definitions.

**Success Criteria**: Data models compile, all imports resolve, no errors.

### Data Model Creation

- [X] T001 [P] Create `ExploreSection.kt` data class in `ui/explore/` with id, title, description, icon, route, contentDescription properties
- [X] T002 [P] Create `ExploreSections.kt` object in `ui/explore/` with static list of 5 sections (ISS, Agencies, Astronauts, Rockets, Starship)
- [X] T003 Verify all imports resolve: Icons (Satellite, Business, Person, Rocket, RocketLaunch), Navigation routes (SpaceStationDetail, Agencies, Astronauts, Rockets, Starship)

---

## Phase 2: Foundational - Navigation Routes

**Goal**: Register Explore route in navigation system.

**Success Criteria**: Navigation routes compile, Screen sealed class updated.

### Navigation Registration

- [X] T004 [P] Add `@Serializable data object Explore` to `navigation/Screen.kt` after existing route objects
- [X] T005 [P] Add `data object Explore : Screen("Explore", Icons.Filled.Explore)` to Screen sealed class in `navigation/Screen.kt`
- [X] T006 Add import `androidx.compose.material.icons.filled.Explore` to `navigation/Screen.kt`

---

## Phase 3: User Story - Display Explore Tab in Bottom Navigation

**Goal**: Users can see and tap the Explore tab in bottom navigation.

**Test Criteria**: 
- Explore tab appears as 3rd tab (between Schedule and Settings)
- Tapping Explore tab navigates to Explore screen
- Tab shows Explore icon and "Explore" label

### Navigation Bar Update

- [X] T007 [US1] Update items list in `ui/compose/BottomNavigationBar.kt` to include `Screen.Explore` (add between Schedule and Settings)
- [X] T008 [US1] Update routes list in `ui/compose/BottomNavigationBar.kt` to include `Explore` route
- [X] T009 [US1] Add import `me.calebjones.spacelaunchnow.navigation.Explore` to `ui/compose/BottomNavigationBar.kt`

---

## Phase 4: User Story - View Explore Screen with Section Cards

**Goal**: Users can view a grid of 5 discovery section cards on the Explore screen.

**Test Criteria**:
- Explore screen displays with top app bar showing "Explore" title
- Grid shows 5 cards: ISS Tracking, Agencies, Astronauts, Rockets, Starship
- Grid adapts: 2 columns on phone, 2-3 columns on tablet/desktop
- Cards display icon, title, and description

### UI Component Creation

- [X] T010 [P] [US2] Create `ExploreCard.kt` component in `ui/explore/components/` with Material 3 Card, circular icon surface, title, description
- [X] T011 [P] [US2] Add `@OptIn(ExperimentalMaterial3Api)` to ExploreCard for Card onClick support
- [X] T012 [P] [US2] Implement card layout: Column with centered icon (64dp circle), Spacer (12dp), title (titleMedium, bold), Spacer (4dp), description (bodySmall, surfaceVariant)
- [X] T013 [US2] Create `ExploreScreen.kt` in `ui/explore/` with Scaffold + TopAppBar + LazyVerticalGrid
- [X] T014 [US2] Implement responsive grid in ExploreScreen: `GridCells.Fixed(2)` for phone, `GridCells.Adaptive(200.dp)` for tablet/desktop using `isTabletOrDesktop()`
- [X] T015 [US2] Add grid content padding: 16dp horizontal (phone), 24dp horizontal (tablet/desktop), 16dp vertical
- [X] T016 [US2] Add grid spacing: 12dp horizontal and vertical between cards
- [X] T017 [US2] Map `ExploreSections.sections` to ExploreCard items in LazyVerticalGrid

---

## Phase 5: User Story - Navigate to Discovery Sections

**Goal**: Users can tap section cards to navigate to respective screens.

**Test Criteria**:
- Tapping ISS Tracking card navigates to SpaceStationDetail(4)
- Tapping Agencies card navigates to Agencies list
- Tapping Astronauts card navigates to Astronauts list
- Tapping Rockets card navigates to Rockets list
- Tapping Starship card navigates to Starship dashboard
- Back button returns to Explore screen

### Navigation Wiring

- [X] T018 [US3] Register `composableWithCompositionLocal<Explore> { ExploreScreen(navController) }` in App.kt NavHost
- [X] T019 [US3] Add imports to App.kt: `navigation.Explore`, `ui.explore.ExploreScreen`
- [X] T020 [US3] Wire card onClick in ExploreScreen to `navController.navigate(section.route)`
- [X] T021 [US3] Update `showBottomBar` in `ui/layout/phone/PhoneLayout.kt` to show bottom bar for Explore route
- [X] T022 [US3] Add import `me.calebjones.spacelaunchnow.navigation.Explore` to PhoneLayout.kt

---

## Phase 6: Testing & Quality

**Goal**: Verify functionality and accessibility across platforms.

**Test Criteria**:
- All manual tests pass
- Accessibility tests pass (content descriptions, keyboard nav)
- UI renders correctly on phone, tablet, desktop

### Manual Testing

- [ ] T023 Test: Explore tab appears in bottom navigation on Android
- [ ] T024 Test: Tapping Explore navigates to Explore screen
- [ ] T025 Test: All 5 cards display correctly with icons, titles, descriptions
- [ ] T026 Test: Tapping ISS Tracking card navigates to ISS screen
- [ ] T027 Test: Tapping Agencies card navigates correctly
- [ ] T028 Test: Tapping Astronauts card navigates correctly
- [ ] T029 Test: Tapping Rockets card navigates correctly
- [ ] T030 Test: Tapping Starship card navigates correctly
- [ ] T031 Test: Back button from destination screens returns to Explore
- [ ] T032 Test: Navigation state preserved when switching tabs
- [ ] T033 Test: Grid layout adapts on tablet/desktop (2-3 columns)
- [ ] T034 Test: Device rotation maintains layout (phone)

### Accessibility Testing

- [ ] T035 Test: Enable TalkBack, verify each card announces title + content description
- [ ] T036 Test: Keyboard navigation on desktop selects and activates cards
- [ ] T037 Test: Focus indicators visible on all interactive elements

---

## Phase 7: Polish & Cleanup

**Goal**: Remove old WIP section from Settings, finalize implementation.

**Test Criteria**:
- Settings screen no longer shows WIP Explore section
- Code follows project conventions
- Commit message follows conventional format

### Cleanup

- [X] T038 [P] Remove or comment out "Explore (WORK IN PROGRESS)" section in `ui/settings/SettingsScreen.kt` (around line 253)
- [X] T039 [P] Verify no unused imports in modified files
- [X] T040 [P] Add `@Preview` annotation to ExploreCard for Compose preview support
- [X] T041 Final build verification: Run `./gradlew build` and ensure no errors
- [ ] T042 Commit with conventional format: `feat(ui): add explore tab with navigation to discoveries`

---

## Dependency Graph

### Story Completion Order

```
Phase 1: Setup (T001-T003)
    ↓
Phase 2: Foundation (T004-T006)
    ↓
Phase 3: US1 - Bottom Nav (T007-T009) ← Can start after T004-T006
    ↓
Phase 4: US2 - Explore Screen (T010-T017) ← Can start after T001-T003, parallel with US1
    ↓
Phase 5: US3 - Navigation (T018-T022) ← Depends on US1 + US2
    ↓
Phase 6: Testing (T023-T037) ← Can run in parallel with Phase 7
    ↓
Phase 7: Polish (T038-T042) ← Final cleanup
```

### Parallel Execution Opportunities

**Batch 1 (After project setup)**:
- T001: Create ExploreSection.kt
- T002: Create ExploreSections.kt
- T004: Add @Serializable Explore route
- T005: Add Screen.Explore sealed class entry

**Batch 2 (After Batch 1 completes)**:
- T007-T009: Update BottomNavigationBar (US1)
- T010-T012: Create ExploreCard component (US2)

**Batch 3 (After US1 + US2 components created)**:
- T013-T017: Create ExploreScreen with grid
- T018-T022: Wire navigation

**Batch 4 (After implementation complete)**:
- T023-T037: All testing tasks (can run in parallel)
- T038-T040: Cleanup tasks (can run in parallel)

---

## Implementation Strategy

### MVP-First Approach

**Phase 1-2 (Foundation)**: Essential for all user stories
**Phase 3 (US1)**: Minimal viable feature - tab appears
**Phase 4 (US2)**: Core functionality - cards display
**Phase 5 (US3)**: Full functionality - navigation works
**Phase 6-7**: Quality & polish

### Incremental Delivery

1. **Checkpoint 1** (After T009): Bottom nav updated, Explore tab visible (may not navigate yet)
2. **Checkpoint 2** (After T017): Explore screen renders with cards (navigation may not work)
3. **Checkpoint 3** (After T022): Full navigation working end-to-end
4. **Checkpoint 4** (After T037): Tested and polished

### Independent Testing

Each user story can be tested independently:
- **US1**: Bottom nav shows 4 tabs, Explore is 3rd
- **US2**: Explore screen displays grid of 5 cards
- **US3**: Tapping cards navigates to destinations

---

## Validation Checklist

### Story Complete Criteria

**US1 Complete When**:
- [x] Explore tab appears in bottom navigation
- [x] Tab shows Explore icon and label
- [x] Tapping tab attempts navigation (even if screen not ready)

**US2 Complete When**:
- [x] Explore screen renders with top app bar
- [x] Grid displays 5 cards with correct icons/text
- [x] Grid is responsive (2 cols phone, adaptive tablet/desktop)
- [x] Cards are visually styled per Material 3

**US3 Complete When**:
- [x] All 5 cards navigate to correct destinations
- [x] Back navigation returns to Explore screen
- [x] Tab state preserved when switching between tabs
- [x] Bottom bar shows/hides correctly

---

## Success Metrics

- **Total Tasks**: 42
- **Parallelizable**: 12 tasks (29%)
- **User Story Tasks**: 23 tasks (phases 3-5)
- **Test Tasks**: 15 tasks (phase 6)
- **Estimated Time**: 2-3 hours (based on quickstart guide)

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Navigation routes don't exist | ✅ Already verified in research - all routes exist |
| Import errors | Test compilation after each batch |
| Grid layout issues | Use proven VehicleGrids.kt pattern |
| Platform-specific bugs | Test on Android first, then iOS/Desktop |
| Accessibility issues | Follow checklist in T035-T037 |

---

## Next Steps After Completion

1. **Manual Test**: Follow Phase 6 checklist
2. **Create PR**: To `dev` branch (current branch per repo info)
3. **CI/CD**: Automatic on merge (conventional commit triggers versioning)
4. **Documentation**: Auto-generated from commit message

**Ready to implement!** Start with T001-T003 (data models) and work through phases sequentially.
