# Tasks: Fix iOS V5 Notification Filter Bug

**Plan**: `specs/dev/ios/plan.md`  
**Spec**: `specs/dev/ios/fix_ios.md`  
**Branch**: `fix_notif_filters`  
**Scope**: iOS Swift + Kotlin iosMain (commonMain/androidMain handled in Android branch)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[US1]**: Implement NSE preference bridge (Kotlin → UserDefaults)
- **[US2]**: Implement NSE Swift-native filter
- **[US3]**: Integrate NSE filter into NotificationService
- **[US4]**: Verification and validation
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Baseline verification — ensure project is in a clean state before changes.

- [x] T001 Verify branch is ready: ensure `iosApp/` builds before changes in Xcode (baseline confirmation)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Clean shared files that block all subsequent work. The duplicate `V5NotificationFilter` enum inside `V5NotificationData.swift` must be removed, and the 3 dead V5 Swift files must be deleted with pbxproj references cleaned.

**CRITICAL**: This phase MUST complete before US1, US2, US3, or US4 can proceed.

- [x] T002 Remove `V5NotificationFilter` enum and `V5FilterPreferences` reference from `iosApp/iosApp/V5NotificationData.swift` — keep only the `V5NotificationData` struct
- [x] T003 [P] Delete `iosApp/iosApp/V5FilterPreferences.swift` and remove all references from `iosApp/iosApp.xcodeproj/project.pbxproj`
- [x] T004 [P] Delete `iosApp/iosApp/V5NotificationFilter.swift` and remove all references from `iosApp/iosApp.xcodeproj/project.pbxproj`
- [x] T005 [P] Delete `iosApp/iosApp/V5PreferencesSyncManager.swift` and remove all references from `iosApp/iosApp.xcodeproj/project.pbxproj`
- [x] T006 Grep verification: `grep -rn "V5FilterPreferences\|V5NotificationFilter\|V5PreferencesSyncManager\|V5Sync" iosApp/ --include="*.swift"` returns 0 results (excluding NotificationService.swift which is rewritten in US3)

**Checkpoint**: 3 dead V5 Swift files deleted. ~610 lines removed. `V5NotificationData.swift` contains only the payload struct. No dangling symbol references.

---

## Phase 3: User Story 1 — NSE Preference Bridge (Priority: P1) 🎯 MVP

**Goal**: Kotlin writes `NotificationState` filter fields to shared UserDefaults via App Group whenever preferences change. This is the bridge that allows the NSE (separate process, can't call Kotlin) to read filter preferences.

**Independent Test**: After saving notification settings in the app, read UserDefaults for `group.me.spacelaunchnow.spacelaunchnow` and verify keys `nse_enable_notifications`, `nse_follow_all_launches`, `nse_use_strict_matching`, `nse_subscribed_agencies`, `nse_subscribed_locations` are populated with correct expanded values.

### Implementation for User Story 1

- [x] T007 [US1] Create `NSEPreferenceBridge.kt` at `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NSEPreferenceBridge.kt` — Kotlin object that writes filter fields to `NSUserDefaults(suiteName: "group.me.spacelaunchnow.spacelaunchnow")`. Must expand `additionalIds` from `NotificationAgency` and `NotificationLocation` (both defined in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationState.kt`) at write time. Keys: `nse_enable_notifications` (Bool), `nse_follow_all_launches` (Bool), `nse_use_strict_matching` (Bool), `nse_subscribed_agencies` ([String], expanded), `nse_subscribed_locations` ([String], expanded). Call `synchronize()` after writes.
- [x] T008 [US1] Hook `NSEPreferenceBridge.syncToUserDefaults(state)` into the notification state save path — call from iOS-specific code after `NotificationStateStorage.saveState()` at `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/NotificationStateStorage.kt`. Use expect/actual pattern or hook from the iOS ViewModel/repository layer so it only runs on iOS.
- [x] T009 [US1] Sync UserDefaults on app launch: call `NSEPreferenceBridge.syncToUserDefaults()` during iOS app initialization (e.g., in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.ios.kt` or on `IosNotificationBridge` init at `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosNotificationBridge.kt`) to ensure UserDefaults is current if user changed settings before the bridge was integrated.

**Checkpoint**: Kotlin writes expanded filter IDs to shared UserDefaults on every state save and on app launch. NSE can now read these keys.

---

## Phase 4: User Story 2 — NSE Swift Filter (Priority: P2)

**Goal**: Create a lightweight Swift-native filter for the NSE that reads preferences from shared UserDefaults and decides allow/block. This runs independently in the NSE process when the app is killed.

**Independent Test**: `NSENotificationFilter.shouldShow(payload:preferences:)` returns `false` for a China launch (`lspId=96`) when preferences have only SpaceX (`subscribedAgencies=["121"]`) and `followAllLaunches=false`. Returns `true` when `followAllLaunches=true`.

### Implementation for User Story 2

- [x] T010 [P] [US2] Create `NSEFilterPreferences.swift` at `iosApp/NotificationServiceExtension/NSEFilterPreferences.swift` — struct with `enableNotifications: Bool`, `followAllLaunches: Bool`, `useStrictMatching: Bool`, `subscribedAgencies: Set<String>`, `subscribedLocations: Set<String>`. Static `load()` method reads from `UserDefaults(suiteName: "group.me.spacelaunchnow.spacelaunchnow")` with keys prefixed `nse_`. Defaults: `enableNotifications: true`, `followAllLaunches: true`, `useStrictMatching: false`, empty sets.
- [x] T011 [P] [US2] Create `NSENotificationFilter.swift` at `iosApp/NotificationServiceExtension/NSENotificationFilter.swift` — struct with static `shouldShow(payload: V5NotificationData, preferences: NSEFilterPreferences) -> Bool`. Logic: (1) `!enableNotifications` → false, (2) `followAllLaunches` → true, (3) check `String(lspId)` in `subscribedAgencies`, (4) check `String(locationId)` in `subscribedLocations`, (5) strict=AND, flexible=OR. When both agency and location have no match and sets are non-empty → false.
- [x] T012 [US2] Add `NSEFilterPreferences.swift` and `NSENotificationFilter.swift` to NSE target — N/A: NSE uses PBXFileSystemSynchronizedRootGroup; files in NotificationServiceExtension/ are auto-included in `iosApp/iosApp.xcodeproj/project.pbxproj` — add PBXBuildFile, PBXFileReference, PBXGroup (under NotificationServiceExtension group), and Sources build phase entries for the NotificationServiceExtension target only (not the main iosApp target)

**Checkpoint**: Two new Swift files in NSE target. Filter logic mirrors Kotlin `NotificationFilter` (simplified). Ready to integrate into `NotificationService.swift`.

---

## Phase 5: User Story 3 — Integrate Filter into NotificationService (Priority: P3)

**Goal**: Update `NotificationService.swift` to load preferences, apply filter, and either deliver enriched content or suppress with empty content. The NSE must filter independently when the app is killed.

**Independent Test**: Send a V5 notification with `lsp_id: "96"` (China). With UserDefaults set to `subscribedAgencies: ["121"]` (SpaceX only) and `followAllLaunches: false`, the NSE delivers empty content (suppressed). With `followAllLaunches: true`, NSE delivers enriched content with image.

### Implementation for User Story 3

- [x] T013 [US3] Rewrite `iosApp/NotificationServiceExtension/NotificationService.swift` — after parsing V5 payload via `V5NotificationData.fromUserInfo()`, load `NSEFilterPreferences.load()`, call `NSENotificationFilter.shouldShow(payload:preferences:)`. If blocked: call `deliverEmptyNotification(contentHandler)` helper that delivers `UNMutableNotificationContent` with empty title/body and nil sound. If allowed: set title/body from v5Data, download+attach image if available, deliver. For non-V5 payloads: load preferences and check `enableNotifications` only — allow through if enabled, suppress if disabled. Keep `serviceExtensionTimeWillExpire()` and `downloadAndAttachImage()` helpers.
- [x] T014 [US3] Verify `V5NotificationData.swift` remains in NSE target membership in `iosApp/iosApp.xcodeproj/project.pbxproj` — confirm the 6 existing V5NotificationData references are intact (needed for payload parsing in NSE)

**Checkpoint**: NSE filters independently. Blocked notifications deliver empty content (iOS won't display). Allowed notifications get enriched with title/body/image.

---

## Phase 6: User Story 4 — Verification and Validation (Priority: P4)

**Goal**: Confirm everything builds cleanly, no dead references remain, the Kotlin bridge and NSE filter paths are both intact.

**Independent Test**: Both Xcode targets build. All grep checks pass. `IosNotificationBridge.kt` still calls `NotificationFilter.shouldShowFromMap()`. `NSEPreferenceBridge.kt` writes all 5 UserDefaults keys.

### Implementation for User Story 4

- [ ] T015 [P] [US4] Build iosApp target (Debug) in Xcode — requires Xcode — must succeed with no errors or warnings related to deleted or new files
- [ ] T016 [P] [US4] Build NotificationServiceExtension target in Xcode — requires Xcode — must succeed with no errors, new Swift filter files and rewritten NotificationService compiled
- [x] T017 [US4] Final grep verification: `grep -r "V5FilterPreferences" iosApp/ --include="*.swift"` → 0, `grep -r "V5PreferencesSyncManager" iosApp/ --include="*.swift"` → 0, `grep -r "V5NotificationFilter" iosApp/ --include="*.swift"` → 0 (the V5NotificationFilter enum in V5NotificationData.swift was already removed in T002), `grep -r "deliverEmptyNotification" iosApp/ --include="*.swift"` → only in `NotificationService.swift`
- [x] T018 [US4] Verify `IosNotificationBridge.kt` at `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosNotificationBridge.kt` — confirm `shouldShowNotification()` calls `NotificationFilter.shouldShowFromMap()` (V4 filter, unchanged). This ensures in-app filtering still works via Kotlin for foreground/background scenarios.
- [x] T019 [US4] Verify `NSEPreferenceBridge.kt` at `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NSEPreferenceBridge.kt` — confirm it writes all 5 UserDefaults keys (`nse_enable_notifications`, `nse_follow_all_launches`, `nse_use_strict_matching`, `nse_subscribed_agencies`, `nse_subscribed_locations`), expands `additionalIds` from `NotificationAgency`/`NotificationLocation`, and calls `synchronize()`

**Checkpoint**: iOS builds clean. Dead V5 filter code removed. Kotlin bridge writes to UserDefaults. NSE reads from UserDefaults and filters independently. Two-layer defense: NSE filters when app is killed, Kotlin bridge filters when app is running.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final cleanup, quickstart validation, commit

- [x] T020 Run `specs/dev/ios/quickstart.md` verification commands to validate all expected outcomes
- [ ] T021 Commit with message: `fix(ios): replace broken V5 filter stack with NSE UserDefaults filter`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — baseline build check ✅ DONE
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories ✅ DONE
- **US1 (Phase 3)**: Depends on Phase 2 — create Kotlin preference bridge
- **US2 (Phase 4)**: Depends on Phase 2 — create Swift filter files (can run **in parallel** with US1)
- **US3 (Phase 5)**: Depends on **BOTH** US1 AND US2 — integrate filter into NotificationService
- **US4 (Phase 6)**: Depends on US1 + US2 + US3 — verification
- **Polish (Phase 7)**: Depends on Phase 6

### User Story Dependencies

- **US1 (Kotlin bridge)** and **US2 (Swift filter)** can proceed **in parallel** — they are independent files in different targets/languages
- **US3 (Integration)**: Depends on BOTH US1 and US2 — needs UserDefaults written (US1) and filter classes available (US2)
- **US4 (Verification)**: Depends on ALL previous stories — validates the complete change

### Within Each User Story

- T010, T011 are parallel (different Swift files, no dependencies)
- T015, T016 are parallel (different Xcode targets)
- All other tasks are sequential within their story

### Parallel Opportunities

```
Phase 2 (T002-T006) ── DONE ──────────────────────────────────┐
                                                                │
Phase 3 (US1):  T007 ── T008 ── T009                           │
                    ↕ parallel with Phase 4 ↕                   │
Phase 4 (US2):  T010 ──┐                                       │
                T011 ──┼── parallel ── T012                     │
                                                                │
Phase 5 (US3):  T013 ── T014   (after both US1 + US2)          │
                                                                │
Phase 6 (US4):  T015 ──┐                                       │
                T016 ──┼── parallel ── T017 ── T018 ── T019    │
                                                                │
Phase 7:        T020 ── T021                                    │
```

---

## Implementation Strategy

### MVP First (US1 + US2 + US3)

1. Phase 1 + 2: ✅ DONE (files deleted, V5NotificationData cleaned, pbxproj cleaned)
2. Complete Phase 3 (US1): Kotlin preference bridge writes to UserDefaults
3. Complete Phase 4 (US2): Swift filter files created (parallel with US1)
4. Complete Phase 5 (US3): NotificationService.swift integrates filter
5. **STOP and VALIDATE**: NSE filters independently, both targets build

### Incremental Delivery

1. Foundation ✅ → Dead V5 code removed
2. US1 (Kotlin bridge) → Preferences accessible via UserDefaults
3. US2 (Swift filter) → Filter logic ready (parallel with US1)
4. US3 (Integration) → NSE filters notifications end-to-end
5. US4 (Verification) → Full confidence everything works
6. Polish → Commit with conventional message

---

## Summary

| Phase | Tasks | Description | Status |
|-------|-------|-------------|--------|
| Phase 1: Setup | T001 | Baseline verification | ✅ DONE |
| Phase 2: Foundational | T002–T006 | Delete V5 files, clean pbxproj, clean V5NotificationData | ✅ DONE |
| Phase 3: US1 — Kotlin Bridge | T007–T009 | NSEPreferenceBridge writes to UserDefaults | Pending |
| Phase 4: US2 — Swift Filter | T010–T012 | NSEFilterPreferences + NSENotificationFilter + pbxproj | Pending |
| Phase 5: US3 — Integration | T013–T014 | NotificationService.swift uses filter | Pending |
| Phase 6: US4 — Verification | T015–T019 | Build, grep, bridge verification | Pending |
| Phase 7: Polish | T020–T021 | Quickstart validation, commit | Pending |
| **Total** | **21 tasks** | **6 done, 15 remaining** | |
