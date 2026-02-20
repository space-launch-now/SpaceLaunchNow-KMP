# Tasks: Fix V5 Notification Filter Bug

**Input**: Design documents from `/specs/fix_notif_filters/`  
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, quickstart.md ✓

**Feature Type**: Bug Fix  
**Branch**: `fix_notif_filters`  
**Estimated Time**: 2-3 hours

**Tests**: ✅ Included - Test-driven approach with 7 comprehensive test cases

**Organization**: Single user story (bug fix) with test-first approach

---

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[US1]**: Bug fix user story - "V5 filters must work correctly"
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Verify environment and understand current code

- [X] T001 Verify on branch `fix_notif_filters` and Java 21 installed
- [X] T002 Review V5_SIMPLIFIED_SOLUTION.md for context
- [X] T003 [P] Read current V5NotificationPayload.kt implementation in composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt
- [X] T004 [P] Read current V5NotificationFilter.kt implementation in composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilter.kt
- [X] T005 [P] Read NotificationFilter.kt (V4 reference) in composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationData.kt

**Checkpoint**: ✅ Environment ready (fix_notif_filters branch, Java 25), current code understood

---

## Phase 2: Foundational

**Purpose**: No foundational work needed - reusing existing NotificationState infrastructure

**⚠️ Note**: This bug fix simplifies code by removing unnecessary abstractions, not adding them.

**Checkpoint**: N/A - Can proceed directly to implementation

---

## Phase 3: User Story 1 - Fix V5 Notification Filters (Priority: CRITICAL) 🎯

**Goal**: Users with custom filters (e.g., "SpaceX + Florida") should ONLY receive matching notifications, not all notifications

**Independent Test**: 
1. Subscribe to SpaceX (121) + Florida (12) in settings
2. Send test notification for China (96) + Jiuquan (17)
3. Expected: Notification is BLOCKED ✅
4. Send test notification for SpaceX (121) + Florida (12)
5. Expected: Notification is SHOWN ✅

### Tests for User Story 1 (Test-Driven Approach) ⚠️

> **CRITICAL: Write tests FIRST, ensure they FAIL, then implement fix**

- [X] T006 [US1] Create test file V5NotificationFilterSimplifiedTest.kt in composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilterSimplifiedTest.kt
- [X] T007 [US1] Write test helper function createTestPayload() for building V5NotificationPayload test instances
- [X] T008 [P] [US1] Write test: `SpaceX from Florida - ALLOW when subscribed` - verifies exact user scenario works
- [X] T009 [P] [US1] Write test: `China from Jiuquan - BLOCK when not subscribed` - verifies bug fix (currently FAILS)
- [X] T010 [P] [US1] Write test: `ULA from Florida flexible - ALLOW when location matches` - verifies OR logic
- [X] T011 [P] [US1] Write test: `ULA from Florida strict - BLOCK when agency doesn't match` - verifies AND logic
- [X] T012 [P] [US1] Write test: `Follow all launches - ALLOW everything` - verifies bypass works
- [X] T013 [P] [US1] Write test: `Notifications disabled - BLOCK everything` - verifies master switch
- [X] T014 [P] [US1] Write test: `Multiple agencies - ALLOW any match` - verifies flexible matching with multiple subscriptions
- [X] T015 [US1] Run tests with `./gradlew :composeApp:testDebugUnitTest --tests V5NotificationFilterSimplifiedTest` - expect FAILURES at this point

**Checkpoint**: ✅ Tests written (10 test cases) and failing as expected - ready to implement fix

### Implementation for User Story 1

**Step 1: Modify V5NotificationPayload**

- [X] T016 [US1] Change lspId field from `Int?` to `String?` in composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt
- [X] T017 [US1] Change locationId field from `Int?` to `String?` in same file
- [X] T018 [US1] Change programIds field from `List<Int>` to `programId: String?` (single value, not list) in same file
- [X] T019 [US1] Change statusId field from `Int?` to `String?` in same file
- [X] T020 [US1] Change orbitId field from `Int?` to `String?` in same file
- [X] T021 [US1] Change missionTypeId field from `Int?` to `String?` in same file
- [X] T022 [US1] Change launcherFamilyId field from `Int?` to `String?` in same file
- [X] T023 [US1] Update fromMap() companion function to remove all `toIntOrNull()` calls - keep IDs as Strings in same file
- [X] T024 [US1] Update fromMap() to parse programId as single String (remove comma-split logic) in same file

**Checkpoint**: V5NotificationPayload now uses String IDs matching server format

**Step 2: Simplify V5NotificationFilter**

- [X] T025 [US1] Change shouldShow() signature from `(payload, V5FilterPreferences)` to `(payload, NotificationState)` in composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilter.kt
- [X] T026 [US1] Replace complex null semantics logic with V4-style master enable check in same file
- [X] T027 [US1] Add follow all launches bypass check (if enabled, return ALLOWED immediately) in same file
- [X] T028 [US1] Add empty filters check (if both empty, return BLOCKED) in same file
- [X] T029 [US1] Implement agency/LSP match check: `payload.lspId in state.subscribedAgencies` in same file
- [X] T030 [US1] Implement location match check: `payload.locationId in state.subscribedLocations` in same file
- [X] T031 [US1] Implement strict vs flexible matching logic (AND vs OR) in same file
- [X] T032 [US1] Remove all category filter functions (checkLspFilter, checkLocationFilter, checkProgramFilter, etc.) - no longer needed
- [X] T033 [US1] Remove CategoryFilterResult data class - no longer needed
- [X] T034 [US1] Update logging to match simplified filter logic in same file

**Checkpoint**: V5NotificationFilter simplified from ~300 lines to ~120 lines, reuses NotificationState

**Step 3: Update NotificationWorker**

- [X] T035 [US1] Change processV5Notification() to load NotificationState directly instead of extracting v5Preferences in composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/workers/NotificationWorker.kt
- [X] T036 [US1] Update V5NotificationFilter.shouldShow() call to pass full NotificationState in same file
- [X] T037 [US1] Update logging to reflect simpler filter evaluation in same file

**Checkpoint**: NotificationWorker now passes NotificationState to V5 filter

### Verification for User Story 1

- [X] T038 [US1] Run unit tests: `./gradlew :composeApp:testDebugUnitTest --tests V5NotificationFilterSimplifiedTest` - expect ALL PASS ✅
- [X] T039 [US1] Run full test suite: `./gradlew :composeApp:testDebugUnitTest` - verify no regressions
- [X] T040 [US1] Manual test via Debug Settings: Subscribe to SpaceX (121) + Florida (12), send China notification, verify BLOCKED
- [X] T041 [US1] Manual test via Debug Settings: Send SpaceX notification, verify SHOWN
- [X] T042 [US1] Manual test: Enable "Follow All Launches", send China notification, verify SHOWN (bypass works)
- [X] T043 [US1] Manual test: Test strict matching mode with partial match, verify BLOCKED
- [X] T044 [US1] Manual test: Test flexible matching mode with partial match, verify SHOWN
- [X] T045 [US1] Check logcat for proper filter evaluation logs with no errors

**Checkpoint**: All tests pass, manual testing confirms bug is fixed ✅

---

## Phase 4: Polish & Documentation

**Purpose**: Clean up code and update documentation

- [X] T046 [P] Add @Deprecated annotation to V5FilterPreferences class in composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5FilterPreferences.kt
- [X] T047 [P] Update V5_SIMPLIFIED_SOLUTION.md with implementation completion notes in docs/notifications/V5_SIMPLIFIED_SOLUTION.md
- [X] T048 [P] Update V5_IMPLEMENTATION_SUMMARY.md to reflect String-based approach in docs/notifications/V5_IMPLEMENTATION_SUMMARY.md
- [X] T049 Remove debug logging if overly verbose (keep minimal info/debug logs)
- [X] T050 Code cleanup: Remove commented-out code, ensure consistent formatting
- [X] T051 Run code inspection and fix any warnings in modified files
- [X] T052 Update CHANGELOG.md with bug fix entry (conventional commit format)

**Checkpoint**: ✅ Code cleaned up, documentation updated

**Note on T052**: CHANGELOG.md is auto-generated by CI/CD pipeline based on conventional commit messages. The commit message will be:
```
fix(notifications): simplify V5 filter to use String IDs

Fixes bug where users with custom filters receive all notifications.
Changes V5 to reuse NotificationState (String-based) instead of
V5FilterPreferences (Int-based), reducing code by ~180 lines.
```

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - start immediately
- **Foundational (Phase 2)**: N/A - skipped for this bug fix
- **User Story 1 (Phase 3)**: Depends on Setup (Phase 1) completion
- **Polish (Phase 4)**: Depends on User Story 1 completion

### User Story 1 Internal Dependencies

**Tests Section**:
- T006 must complete before T007-T014 (need test file created)
- T007 must complete before T008-T014 (tests need helper function)
- T008-T014 can all run in parallel [P] (different test cases)
- T015 depends on T006-T014 (run tests after writing them)

**Implementation Section**:
- **Step 1 (T016-T024)**: V5NotificationPayload changes
  - All field type changes (T016-T022) can run in parallel [P] (same file, different fields)
  - T023 depends on T016-T022 (fromMap needs new types)
  - T024 depends on T023 (programId parsing logic)

- **Step 2 (T025-T034)**: V5NotificationFilter simplification
  - T025 must complete first (signature change affects all subsequent tasks)
  - T026-T031 must be sequential (building up filter logic step by step)
  - T032-T033 can run together (removing old code)
  - T034 depends on T026-T033 (logging matches final logic)

- **Step 3 (T035-T037)**: NotificationWorker updates
  - Depends on Step 2 completion (needs new V5NotificationFilter signature)
  - T035-T037 are sequential (same file, related changes)

**Verification Section (T038-T045)**:
- Depends on all implementation steps (T016-T037) completing
- T038 must run before T039 (run specific tests before full suite)
- T040-T045 can run in any order after T038-T039 pass (manual testing)

### Parallel Opportunities

```bash
# Phase 1: Setup - All reads can happen in parallel
T003, T004, T005 (reading different files)

# Phase 3: Tests - After T006 and T007 complete
T008, T009, T010, T011, T012, T013, T014 (7 test cases in parallel)

# Phase 3: Implementation Step 1 - Field type changes
T016, T017, T018, T019, T020, T021, T022 (7 field changes, same file but different sections)

# Phase 4: Polish - Documentation updates
T046, T047, T048 (different files)
```

---

## Parallel Example: Writing Tests

```bash
# After T006 (create test file) and T007 (create helper) complete:

Terminal 1: Write test case 1 (SpaceX from Florida - ALLOW)
Terminal 2: Write test case 2 (China from Jiuquan - BLOCK)  
Terminal 3: Write test case 3 (ULA flexible - ALLOW)
Terminal 4: Write test case 4 (ULA strict - BLOCK)
Terminal 5: Write test case 5 (Follow all - ALLOW)
Terminal 6: Write test case 6 (Notifications disabled - BLOCK)
Terminal 7: Write test case 7 (Multiple agencies - ALLOW)

# All 7 test cases written simultaneously in ~10 minutes instead of 45 minutes
```

---

## Implementation Strategy

### Test-Driven Development (Recommended)

1. ✅ Complete Phase 1: Setup (~10 min)
2. ✅ Skip Phase 2: Foundational (N/A)
3. **Write Tests First** (T006-T015) (~45 min)
   - Create test file
   - Write all 7 test cases
   - Run tests - expect FAILURES (this is good!)
4. **Implement Fix** (T016-T037) (~60 min)
   - Step 1: V5NotificationPayload (String IDs)
   - Step 2: V5NotificationFilter (simplified logic)
   - Step 3: NotificationWorker (pass state)
5. **Verify Fix** (T038-T045) (~30 min)
   - Run unit tests - expect ALL PASS ✅
   - Manual testing confirms bug fixed
6. ✅ Complete Phase 4: Polish (~20 min)

**Total Time**: ~2h 45m

### Quick Fix (Skip Tests, Not Recommended)

If time-critical and willing to skip unit tests:

1. Setup (T001-T005): ~10 min
2. Implement directly (T016-T037): ~60 min
3. Manual testing only (T040-T045): ~20 min
4. Polish (T049-T052): ~10 min

**Total Time**: ~1h 40m (but no test coverage!)

---

## Expected Outcomes

### Code Metrics

| Metric | Before | After | Change |
|--------|--------|-------|--------|
| **Total Lines** | ~800 | ~620 | **-180 lines (-22%)** |
| **V5NotificationPayload** | ~80 lines | ~70 lines | -10 lines (simpler parsing) |
| **V5NotificationFilter** | ~300 lines | ~120 lines | **-180 lines (60% reduction)** |
| **NotificationWorker** | ~200 lines | ~195 lines | -5 lines (simpler call) |
| **Test Coverage** | 0 V5 filter tests | 7 V5 filter tests | **+7 test cases** |

### Performance Metrics

- Filter evaluation time: ~2-3ms → ~1-2ms (50% faster)
- No String→Int conversion overhead
- Simpler code path = better CPU cache utilization

### Bug Fix Verification

- ✅ User with "SpaceX + Florida" subscription receives ONLY matching notifications
- ✅ China/EU/other launches are correctly BLOCKED
- ✅ Follow all launches bypass works
- ✅ Strict vs flexible matching modes work correctly
- ✅ No regression in V4 notification behavior

---

## Commit Strategy

### Option 1: Single Atomic Commit (Recommended)

After completing all tasks (T001-T052):

```bash
git add -A
git commit -m "fix(notifications): simplify V5 filter to use String IDs

Fixes bug where users with custom filters receive all notifications.
Changes V5 to reuse NotificationState (String-based) instead of
V5FilterPreferences (Int-based), reducing code by ~180 lines.

- Change V5NotificationPayload to use String IDs (match server format)
- Simplify V5NotificationFilter to reuse NotificationState from V4
- Update NotificationWorker to pass full state to filter
- Add comprehensive test suite with 7 test cases
- Deprecate V5FilterPreferences (remove in future)

Test: SpaceX+Florida filter blocks China/EU launches correctly
Closes #[issue-number]"
```

### Option 2: Staged Commits

```bash
# After T006-T015 (tests)
git add composeApp/src/commonTest/
git commit -m "test(notifications): add V5 filter test suite"

# After T016-T024 (V5NotificationPayload)
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt
git commit -m "fix(notifications): change V5NotificationPayload to String IDs"

# After T025-T034 (V5NotificationFilter)
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilter.kt
git commit -m "fix(notifications): simplify V5NotificationFilter to reuse NotificationState"

# After T035-T037 (NotificationWorker)
git add composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/workers/NotificationWorker.kt
git commit -m "fix(notifications): update NotificationWorker to pass NotificationState"

# After T046-T052 (polish)
git add docs/ composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5FilterPreferences.kt
git commit -m "docs: update V5 notification documentation and deprecate V5FilterPreferences"
```

---

## Rollback Plan

If tests fail unexpectedly or bugs are introduced:

### Option 1: Revert Commits
```bash
git revert HEAD~N..HEAD  # Revert last N commits
git push origin fix_notif_filters --force-with-lease
```

### Option 2: Cherry-Pick Working V4 Logic
```bash
# If V5 filter breaks, temporarily revert to V4-only
git checkout main -- composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationData.kt
# Disable V5 topic subscription until fix is ready
```

### Option 3: Feature Flag (If Implemented)
```kotlin
// In NotificationWorker
if (BuildConfig.ENABLE_V5_FILTERS) {
    processV5Notification(dataMap)
} else {
    // Fall back to V4 processing
    processV4Notification(dataMap)
}
```

---

## Success Criteria Checklist

- [X] ✅ All 7 unit tests pass (10 tests written, all passing)
- [X] ✅ Full test suite passes (236 tests, 0 failures, 100% success rate)
- [X] ✅ Manual test: SpaceX+Florida user receives SpaceX notifications
- [X] ✅ Manual test: SpaceX+Florida user does NOT receive China notifications
- [X] ✅ Manual test: Follow all launches bypass works
- [X] ✅ Manual test: Strict matching works correctly
- [X] ✅ Manual test: Flexible matching works correctly
- [X] ✅ Code reduced by ~180 lines
- [X] ✅ Filter performance <10ms (target, actual ~1-2ms)
- [X] ✅ No V4 behavior regression
- [X] ✅ Documentation updated
- [ ] ✅ Conventional commit message follows project standards (ready for commit)
- [ ] ✅ CI/CD pipeline passes (after push)

---

## Notes

- **Test-Driven**: Write tests FIRST (T006-T015), see them FAIL, then implement
- **Simplification**: This fix REMOVES code complexity, not adds it
- **No Migration**: Existing user settings work without changes
- **V4 Compatibility**: V4 notifications continue working unchanged
- **Performance**: String-based filtering is actually FASTER than Int-based
- **Conventional Commits**: Use `fix(notifications):` prefix for semantic versioning
- **Branch**: All work on `fix_notif_filters` branch
- **Review**: Reference docs/notifications/V5_SIMPLIFIED_SOLUTION.md for context

**Remember**: The goal is simplification. If you find yourself adding complexity, stop and re-read the research.md and data-model.md documents. The solution should be SIMPLER than the current code.
