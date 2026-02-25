# Tasks: V5 Client-Side Notification System

**Input**: Design documents from `/specs/dev/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## User Stories (from spec.md)

| Story | Priority | Description |
|-------|----------|-------------|
| US1 | P1 | V5 Data Model & Parsing - Parse V5 payloads with extended filtering IDs |
| US2 | P1 | V5 Client-Side Filtering - Apply filters based on user preferences |
| US3 | P2 | Android V5 Notification Handling - Data-only message construction |
| US4 | P2 | iOS V5 Notification Service Extension - Mutable-content interception |
| US5 | P3 | V5 Topic Subscription - Platform-specific topic management |
| US6 | P3 | V4 → V5 Migration - Migrate existing users seamlessly |
| US7 | P3 | V5 Filter Settings UI - User configuration for filter preferences |

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project configuration and constants

- [X] T001 Create V5 topic constants in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationTopicConfig.kt`
- [X] T002 [P] Add V5 payload field constants to existing notification constants

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data models that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 Create `V5NotificationPayload` data class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt`
- [X] T004 [P] Create `V5FilterPreferences` data class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5FilterPreferences.kt`
- [X] T005 [P] Create `FilterResult` sealed class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/FilterResult.kt`
- [X] T006 Update `NotificationState.kt` to include `v5Preferences` and `hasCompletedV5Migration` fields in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationState.kt`

**Checkpoint**: Foundation ready - V5 data models available for all platforms

---

## Phase 3: User Story 1 - V5 Data Model & Parsing (Priority: P1) 🎯 MVP

**Goal**: Parse V5 notification payloads with extended filtering IDs from FCM data

**Independent Test**: Send a V5 payload via Firebase Console and verify it parses correctly in logs

### Implementation for User Story 1

- [X] T007 [US1] Implement `V5NotificationPayload.fromMap()` parsing logic with all V5 fields in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt`
- [X] T008 [US1] Implement `V5NotificationPayload.isV5Payload()` detection method (check for `lsp_id` field) in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt`
- [X] T009 [US1] Add V5 payload parsing support to `NotificationData.fromMap()` for backward compatibility in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationData.kt`
- [X] T010 [P] [US1] Create Swift `V5NotificationData` struct with parsing in `iosApp/iosApp/V5NotificationData.swift`
- [X] T011 [US1] Add unit tests for V5 payload parsing in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayloadTest.kt`

**Checkpoint**: V5 payloads can be parsed on both Android (Kotlin) and iOS (Swift)

---

## Phase 4: User Story 2 - V5 Client-Side Filtering (Priority: P1) 🎯 MVP

**Goal**: Filter V5 notifications based on user preferences before display

**Independent Test**: Configure filter preferences, send V5 notification, verify filtering in logs

### Implementation for User Story 2

- [X] T012 [US2] Create `V5NotificationFilter` object with `shouldShow()` method in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilter.kt`
- [X] T013 [US2] Implement master enable/disable check in V5NotificationFilter
- [X] T014 [US2] Implement notification type filtering in V5NotificationFilter
- [X] T015 [US2] Implement webcast-only filtering in V5NotificationFilter
- [X] T016 [US2] Implement LSP ID filtering in V5NotificationFilter
- [X] T017 [US2] Implement location ID filtering in V5NotificationFilter
- [X] T018 [US2] Implement program IDs filtering in V5NotificationFilter
- [X] T019 [US2] Implement orbit, mission type, launcher family filtering in V5NotificationFilter
- [X] T020 [US2] Implement strict vs flexible matching logic (AND vs OR) in V5NotificationFilter
- [X] T021 [US2] Add comprehensive unit tests for V5NotificationFilter in `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilterTest.kt`

**Checkpoint**: V5 filtering logic is complete and tested in common code

---

## Phase 5: User Story 3 - Android V5 Notification Handling (Priority: P2)

**Goal**: Receive data-only FCM messages and construct/display notifications on Android

**Independent Test**: Send V5 data-only FCM to debug topic, verify notification appears with correct title/body/image

### Implementation for User Story 3

- [X] T022 [US3] Update `NotificationWorker` to detect and parse V5 payloads in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/workers/NotificationWorker.kt`
- [X] T023 [US3] Add V5 filter evaluation in NotificationWorker before displaying
- [X] T024 [US3] Update `NotificationDisplayHelper` to handle V5 payload fields in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NotificationDisplayHelper.kt`
- [X] T025 [US3] Ensure notification uses server-provided `title` and `body` from V5 payload
- [X] T026 [US3] Add V5-specific logging for debugging in NotificationWorker
- [ ] T027 [P] [US3] Add instrumentation tests for V5 notification display in `composeApp/src/androidTest/kotlin/me/calebjones/spacelaunchnow/workers/NotificationWorkerV5Test.kt`

**Checkpoint**: Android displays V5 notifications with client-side filtering

---

## Phase 6: User Story 4 - iOS Notification Service Extension (Priority: P2)

**Status**: 🔜 Deferred - iOS-specific implementation moved to `/specs/dev/ios/`

**See**: [iOS V5 NSE Tasks](/specs/dev/ios/tasks.md) for detailed implementation plan

**Goal**: Intercept mutable-content notifications on iOS and apply client-side filtering

**Summary**: 9 tasks covering NSE target creation, App Groups setup, filter logic, and notification suppression

**Checkpoint**: iOS filters V5 notifications via NSE before display

---

## Phase 7: User Story 5 - V5 Topic Subscription (Priority: P3)

**Goal**: Subscribe to platform-specific V5 topics and notification type topics

**Independent Test**: Check FCM subscriptions in Firebase Console, verify correct topics

### Implementation for User Story 5

- [X] T037 [US5] Update `SubscriptionProcessor` to calculate V5 topic based on platform and debug flag in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SubscriptionProcessor.kt`
- [X] T038 [US5] Add V5 topic subscription logic for Android in `SubscriptionProcessor.calculateRequiredTopics()`
- [X] T039 [US5] Update `IosPushMessaging` to subscribe to V5 iOS topics in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosPushMessaging.kt`
- [X] T040 [US5] Update `AppDelegate.swift` to subscribe to V5 iOS topic on token refresh in `iosApp/iosApp/AppDelegate.swift`
- [X] T041 [US5] Add notification type topic subscriptions based on user preferences

**Checkpoint**: Both platforms subscribe to correct V5 topics

**Notes**: V4 and V5 topics now run in parallel - SubscriptionProcessor subscribes to both k_prod_v4 AND prod_v5_android/prod_v5_ios simultaneously.

---

## Phase 8: User Story 6 - V4 → V5 Migration (Priority: P3)

**Goal**: Migrate existing V4 users to V5 topics without notification loss

**Status**: ✅ NOT NEEDED - V4 and V5 run in parallel

**Reason**: SubscriptionProcessor now subscribes to BOTH v4 and v5 topics simultaneously. No migration needed since:
- V4 topic: `k_prod_v4` (existing)
- V5 topic: `prod_v5_android` / `prod_v5_ios` (new)
- Both active at same time
- Server can send to either/both
- Client handles both payload formats via `NotificationData.parseAuto()`

**Implementation**:

- [X] T042 [US6] ~~Create `V5MigrationHelper`~~ - Not needed, parallel operation
- [X] T043 [US6] ~~Implement migration check~~ - Not needed
- [X] T044 [US6] ~~V5 topic subscription before V4 unsubscription~~ - Both active simultaneously
- [X] T045 [US6] ~~V4 topic unsubscription after V5 confirmation~~ - V4 stays active
- [X] T046 [US6] ~~Implement preference migration~~ - Not needed, same preferences used for both
- [X] T047 [US6] ~~Set migration complete flag~~ - Not needed
- [X] T048 [US6] ~~Call migration check on app launch~~ - Not needed
- [X] T049 [P] [US6] ~~Add iOS migration logic~~ - Not needed

**Checkpoint**: V4 and V5 coexist seamlessly

---

## Phase 9: User Story 7 - V5 Filter Settings UI (Priority: P3)

**Goal**: Provide UI for users to configure V5 notification filter preferences

**Independent Test**: Open Settings, configure filters, send notification, verify filter applied

### Implementation for User Story 7

- [ ] T050 [US7] Add V5 filter section to `NotificationSettingsScreen.kt` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/NotificationSettingsScreen.kt`
- [ ] T051 [US7] Create LSP filter multi-select UI component
- [ ] T052 [US7] Create Location filter multi-select UI component
- [ ] T053 [US7] Create Program filter multi-select UI component (optional, can be phase 2)
- [ ] T054 [US7] Create Orbit filter multi-select UI component (optional, can be phase 2)
- [ ] T055 [US7] Create Mission Type filter multi-select UI component (optional, can be phase 2)
- [ ] T056 [US7] Create Launcher Family filter multi-select UI component (optional, can be phase 2)
- [ ] T057 [US7] Add strict matching toggle to V5 filter settings
- [ ] T058 [US7] Wire V5 filter UI to `NotificationRepository` for persistence
- [ ] T059 [US7] Add Compose Previews for all new V5 filter UI components

**Checkpoint**: Users can configure V5 filters through Settings UI

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Documentation, testing, and cleanup

- [X] T060 [P] Update `docs/notifications/V5_IMPLEMENTATION_SUMMARY.md` with V5 architecture documentation
- [ ] T061 [P] Update `docs/notifications/notifications-setup.md` with V5 testing instructions
- [ ] T062 Run `quickstart.md` validation steps to verify end-to-end flow
- [ ] T063 Code cleanup - remove deprecated V4-only code paths if no longer needed
- [ ] T064 Add V5 debug logging toggle to Debug Menu
- [ ] T065 Performance validation - verify filter evaluation < 10ms

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup) ──────────────────────────────────────────┐
                                                          │
Phase 2 (Foundational) ──────────────────────────────────┤
   ├── V5NotificationPayload                             │
   ├── V5FilterPreferences                               │
   ├── FilterResult                                      │
   └── NotificationState update                          │
                                                          │
        ┌─────────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│                    USER STORIES (can parallelize)             │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  Phase 3: US1 (Parsing)  ──┬──▶  Phase 4: US2 (Filtering)    │
│                            │                                  │
│                            ├──▶  Phase 5: US3 (Android)      │
│                            │                                  │
│                            └──▶  Phase 6: US4 (iOS NSE)      │
│                                                               │
│  Phase 7: US5 (Topics)   ──────▶  Phase 8: US6 (Migration)   │
│                                                               │
│  Phase 9: US7 (UI)       (can start after Phase 2)           │
│                                                               │
└───────────────────────────────────────────────────────────────┘
        │
        ▼
Phase 10 (Polish) ────────────────────────────────────────────────
```

### User Story Dependencies

| Story | Depends On | Can Run After |
|-------|------------|---------------|
| US1 (Parsing) | Phase 2 | Phase 2 complete |
| US2 (Filtering) | US1 | US1 complete |
| US3 (Android) | US1, US2 | US2 complete |
| US4 (iOS NSE) | US1, US2 | US2 complete |
| US5 (Topics) | Phase 2 | Phase 2 complete |
| US6 (Migration) | US5 | US5 complete |
| US7 (UI) | Phase 2 | Phase 2 complete |

### Parallel Opportunities

**Within Phase 2 (Foundational)**:
```
T003 (V5NotificationPayload)
T004 [P] (V5FilterPreferences)
T005 [P] (FilterResult)
```

**User Stories in Parallel** (with sufficient team capacity):
```
After US2 complete:
  - US3 (Android) can run in parallel with US4 (iOS)
  
After Phase 2 complete:
  - US7 (UI) can run in parallel with US1/US2
  - US5 (Topics) can run in parallel with US1/US2
```

---

## Implementation Strategy

### MVP First (US1 + US2 + US3)

1. ✅ Complete Phase 1: Setup
2. ✅ Complete Phase 2: Foundational
3. ✅ Complete Phase 3: US1 - V5 Parsing
4. ✅ Complete Phase 4: US2 - V5 Filtering
5. ✅ Complete Phase 5: US3 - Android V5 Handling
6. **STOP and VALIDATE**: Test V5 on Android with filtering
7. Deploy Android beta if ready

### Incremental Delivery

1. **MVP**: US1 + US2 + US3 → Android V5 working
2. **+iOS**: US4 → iOS V5 with NSE filtering
3. **+Topics**: US5 + US6 → Proper topic management and migration
4. **+UI**: US7 → User-facing filter configuration
5. **Polish**: Phase 10 → Documentation and cleanup

### Task Count Summary

| Phase | Tasks | Parallel Tasks | Notes |
|-------|-------|----------------|-------|
| Phase 1 (Setup) | 2 | 1 | |
| Phase 2 (Foundational) | 4 | 2 | |
| Phase 3 (US1 - Parsing) | 5 | 1 | |
| Phase 4 (US2 - Filtering) | 10 | 0 | |
| Phase 5 (US3 - Android) | 6 | 1 | |
| Phase 6 (US4 - iOS NSE) | 9 | 1 | **See /specs/dev/ios/** |
| Phase 7 (US5 - Topics) | 5 | 0 | |
| Phase 8 (US6 - Migration) | 8 | 1 | |
| Phase 9 (US7 - UI) | 10 | 0 | |
| Phase 10 (Polish) | 6 | 2 | |
| **Total** | **65** | **9** | **iOS: 9 tasks in /specs/dev/ios/** |

---

## Notes

- [P] tasks = different files, no dependencies on other tasks in same phase
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group using conventional commits
- Stop at any checkpoint to validate story independently
- **iOS NSE (US4)**: Detailed iOS-specific tasks moved to `/specs/dev/ios/` folder
- iOS NSE requires Xcode project changes - see `/specs/dev/ios/plan.md` for setup guide
