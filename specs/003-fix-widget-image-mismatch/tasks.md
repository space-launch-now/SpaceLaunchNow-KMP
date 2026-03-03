# Tasks: Fix iOS Widget Image/Launch Mismatch on Refresh

**Input**: Design documents from `/specs/003-fix-widget-image-mismatch/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/image-cache-contract.md, quickstart.md

**Tests**: Included — unit tests required per project policy.

**Organization**: Tasks grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: No project scaffolding needed — this is a bug fix in an existing file. Setup phase covers only prerequisite verification.

- [x] T001 Verify CryptoKit availability by confirming iOS deployment target >= 13.0 in `iosApp/iosApp.xcodeproj/project.pbxproj`
- [x] T002 Verify widget extension target `LaunchWidgetExtension` inherits iOS 13.0+ deployment target in `iosApp/iosApp.xcodeproj/project.pbxproj`

**Checkpoint**: Prerequisites confirmed — implementation can proceed.

---

## Phase 2: User Story 1 — Correct Image per Launch in Widget (Priority: P1) 🎯 MVP

**Goal**: Each launch entry in the widget displays the correct image for that specific launch, not an image from a different launch.

**Independent Test**: Add NextUpWidget and LaunchListWidget to the home screen, trigger a timeline refresh, and verify every launch entry shows its own correct image.

### Infrastructure (required before tests can compile)

- [x] T003 [US1] Add Xcode test target `LaunchWidgetTests` to `iosApp/iosApp.xcodeproj` if not already present, linking to the `LaunchWidgetExtension` module so test files can `@testable import` and access internal symbols
- [x] T004 [US1] Make `cacheFile(for:)` in `iosApp/LaunchWidget/LaunchData.swift` internal (remove `private`) so it is accessible from the test target `LaunchWidgetTests`

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T005 [P] [US1] Create test file `iosApp/LaunchWidgetTests/CacheKeyTests.swift` with XCTest import, test class `CacheKeyTests`, and test case `testDistinctURLsProduceDistinctCacheKeys` that verifies two different CDN URLs (e.g., `https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/falcon925/image1.jpg` and `.../falcon926/image2.jpg`) produce different cache file paths
- [x] T006 [P] [US1] Add test case `testSameURLProducesSameCacheKey` in `iosApp/LaunchWidgetTests/CacheKeyTests.swift` verifying the same URL always returns the same cache file path (deterministic)
- [x] T007 [P] [US1] Add test case `testCacheKeyUsesFullURLNotPrefix` in `iosApp/LaunchWidgetTests/CacheKeyTests.swift` verifying that URLs differing only in the last path segment produce different keys (regression test for the base64 truncation bug)

### Implementation for User Story 1

- [x] T008 [US1] Add `import CryptoKit` to the imports section at the top of `iosApp/LaunchWidget/LaunchData.swift`
- [x] T009 [US1] Replace the `cacheFile(for:)` function in `iosApp/LaunchWidget/LaunchData.swift` (line ~258) to use SHA-256 hashing via `SHA256.hash(data:)` instead of base64 + `prefix(64)` truncation — per the contract in `specs/003-fix-widget-image-mismatch/contracts/image-cache-contract.md`

**Checkpoint**: All T005–T007 tests should now PASS. Each launch gets a unique cache file. Core bug is fixed.

---

## Phase 3: User Story 2 — Image Cache Reliability (Priority: P2)

**Goal**: Widget loads images efficiently via cache with correct per-URL isolation, never serving a wrong cached image.

**Independent Test**: Clear the app group container's `widget_image_cache` directory, trigger a widget refresh, verify all images download with unique filenames.

### Tests for User Story 2

- [x] T010 [P] [US2] Add test case `testCacheKeyOutputIs64CharHex` in `iosApp/LaunchWidgetTests/CacheKeyTests.swift` verifying the filename (excluding `.jpg`) is exactly 64 lowercase hexadecimal characters
- [x] T011 [P] [US2] Add test case `testCacheKeyHandlesEmptyString` in `iosApp/LaunchWidgetTests/CacheKeyTests.swift` verifying an empty URL string produces a valid (non-crashing) cache file path
- [x] T012 [P] [US2] Add test case `testCacheKeyHandlesSpecialCharacters` in `iosApp/LaunchWidgetTests/CacheKeyTests.swift` verifying URLs with query parameters, fragments, and unicode characters produce valid filenames

**Checkpoint**: All T010–T012 tests PASS. Cache key function is robust and well-tested.

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Cleanup and verification across both user stories.

- [x] T013 [US1] Verify FR-004 (image-loop pairing): Review `fetchLaunches()` in `iosApp/LaunchWidget/LaunchData.swift` and confirm the download loop iterates by index, pairing each `LaunchData` entry with its own `downloadImage()` call — no code change needed, existing behavior is correct per research.md Task 4
- [ ] T014 Build `LaunchWidgetExtension` target in Xcode to verify no compile errors in `iosApp/LaunchWidget/LaunchData.swift`
- [ ] T015 Run all `LaunchWidgetTests` to confirm all 6 test cases pass (T005–T007, T010–T012)
- [ ] T016 Manually verify on iOS simulator: add NextUpWidget (medium) and LaunchListWidget to home screen, confirm each launch displays its own correct image after refresh
- [ ] T017 Inspect cache directory on simulator (`group.me.calebjones.spacelaunchnow/widget_image_cache/`) to confirm multiple distinct `.jpg` files exist — not a single shared file
- [ ] T018 Run quickstart.md validation steps from `specs/003-fix-widget-image-mismatch/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verification only
- **User Story 1 (Phase 2)**: Depends on Phase 1 confirmation. T003 (test target) and T004 (visibility) must complete before tests T005–T007. Implementation T008–T009 makes the tests pass.
- **User Story 2 (Phase 3)**: Tests T010–T012 use the same test target and function from Phase 2. No additional infrastructure needed.
- **Polish (Phase 4)**: Depends on Phase 2 and Phase 3 completion

### Within Each User Story

- Infrastructure (T003–T004) MUST be completed first so tests can compile
- Tests (T005–T007, T010–T012) MUST be written and FAIL before implementation
- Implementation tasks (T008–T009) make the tests pass
- Story complete before moving to next priority

### Parallel Opportunities

- T001 and T002 can run in parallel (both read-only checks)
- T005, T006, T007 can all run in parallel (separate test cases, same file but no dependencies)
- T008 and T009 are sequential (import must come before function usage)
- T010, T011, T012 can all run in parallel (separate test cases)
- T013–T018 are sequential verification steps

---

## Parallel Example: User Story 1

```
# Infrastructure first (sequential):
Task T003: Add LaunchWidgetTests Xcode test target
Task T004: Make cacheFile(for:) internal for test access

# Write all US1 test cases in parallel:
Task T005: testDistinctURLsProduceDistinctCacheKeys
Task T006: testSameURLProducesSameCacheKey
Task T007: testCacheKeyUsesFullURLNotPrefix

# Then implement sequentially:
Task T008: Add import CryptoKit
Task T009: Replace cacheFile(for:) with SHA-256
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Verify prerequisites (T001–T002)
2. Set up infrastructure: Test target (T003) and visibility change (T004)
3. Write US1 tests (T005–T007) — they should FAIL
4. Implement fix (T008–T009) — tests should PASS
5. **STOP and VALIDATE**: Build and run tests
6. Core bug is fixed — can ship immediately

### Incremental Delivery

1. Phase 1 → Prerequisites confirmed
2. Phase 2 (US1) → Infrastructure + core bug fixed, 3 tests passing → Deployable MVP
3. Phase 3 (US2) → Robustness tests added, 6 total tests passing
4. Phase 4 → FR-004 verified, manual verification complete, ready for PR

---

## Notes

- Total tasks: 18
- US1 tasks: 7 (2 infrastructure + 3 tests + 2 implementation)
- US2 tasks: 3 (3 tests)
- Setup: 2 (verification only)
- Polish: 6 (FR-004 verification + build + tests + manual testing)
- Files modified: 1 (`iosApp/LaunchWidget/LaunchData.swift`)
- Files created: 1 (`iosApp/LaunchWidgetTests/CacheKeyTests.swift`)
- Commit message: `fix(ios): use SHA-256 hash for widget image cache keys to prevent collisions`
