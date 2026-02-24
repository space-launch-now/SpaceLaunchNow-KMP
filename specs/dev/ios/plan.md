# Plan: Fix iOS V5 Notification Filter Bug

**Branch**: `fix_notif_filters` | **Date**: 2026-02-20 | **Spec**: `specs/dev/ios/fix_ios.md`  
**Status**: 🟢 Ready for Implementation  
**Depends On**: Android V5 filter fix branch (Kotlin `commonMain` + `androidMain` changes)

---

## Summary

V5 notification filtering is broken on iOS. Users with custom filters (e.g., "SpaceX + Florida") receive ALL notifications regardless of settings. The Kotlin-side fix (unifying V4/V5 filtering through `NotificationFilter` + `NotificationState` with String IDs) is handled in a separate Android branch. **This plan covers iOS-only work**: deleting the unused V5 Swift filter stack, implementing a lightweight NSE filter with UserDefaults preference bridge, and validating filtering works end-to-end on iOS.

## Prerequisites (From Android Branch)

The following changes are assumed complete (or will be merged before this branch):

- `NotificationData.fromMap()` reads `lsp_id` as fallback for `agency_id`
- `V5FilterPreferences.kt` deleted from `commonMain`
- `V5NotificationFilter.kt` deleted from `commonMain`
- `NotificationState` no longer has `v5Preferences` field
- `NotificationWorker.kt` uses unified filter for all payloads
- `IosNotificationBridge.kt` already uses V4 `NotificationFilter` (no change needed)
- Unified `NotificationFilterTest.kt` covers V5 payloads through V4 filter

## Technical Context

**Language/Version**: Swift 5.9+  
**Primary Dependencies**: UserNotifications framework, App Groups  
**Storage**: DataStore (Preferences) in App Groups via Kotlin  
**Target Platform**: iOS 15+  
**Constraints**: iOS NSE runs in separate process, 30-second execution limit  
**Scale/Scope**: ~610 lines deleted, ~200 lines added (iOS Swift + Kotlin iosMain)

## Constitution Check

*GATE: Checked before Phase 0. Re-checked after Phase 1.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS equal) | ✅ PASS | iOS-specific cleanup, Android handled in separate branch |
| II. Pattern-Based Consistency | ✅ PASS | Removes duplicate Swift filter; NSE uses lightweight Swift-native filter with shared UserDefaults bridge |
| III. Accessibility & UX | ✅ N/A | No UI changes |
| IV. CI/CD & Conventional Commits | ✅ PASS | Commit: `fix(ios): replace broken V5 filter stack with NSE UserDefaults filter` |
| V. Code Generation & API Management | ✅ N/A | No API changes |
| VI. Multiplatform Architecture | ✅ PASS | Kotlin bridge syncs preferences to UserDefaults; Swift NSE filters independently |
| VII. Testing Standards | ✅ PASS | iOS build verified; Kotlin tests cover V5 paths |

**Post-Design Re-check**: No violations introduced. Complexity reduced.

## Project Structure

### Documentation

```text
specs/dev/ios/
├── fix_ios.md           # Feature spec (existing)
├── plan.md              # This file (iOS-scoped)
├── research.md          # Phase 0 research findings
├── data-model.md        # Entity changes (iOS)
├── quickstart.md        # Implementation quick reference
└── tasks.md             # Task breakdown (iOS-only)
```

### Source Code (iOS files affected)

```text
iosApp/
├── iosApp/
│   ├── V5FilterPreferences.swift        # DELETE
│   ├── V5NotificationFilter.swift       # DELETE
│   ├── V5PreferencesSyncManager.swift   # DELETE
│   ├── V5NotificationData.swift         # KEEP (payload parsing for NSE image + filter)
│   ├── AppDelegate.swift                # NO CHANGE
│   └── NotificationDebugHelper.swift    # VERIFY: no V5 references
└── NotificationServiceExtension/
    ├── NotificationService.swift        # REWRITE: add filter logic using NSE filter
    ├── NSEFilterPreferences.swift       # NEW: reads filter prefs from UserDefaults
    └── NSENotificationFilter.swift      # NEW: lightweight Swift-native filter
```

### Kotlin Files (iosMain — IN SCOPE)

```text
composeApp/src/iosMain/.../notifications/
├── IosNotificationBridge.kt         # NO CHANGE (already uses V4 filter)
└── NSEPreferenceBridge.kt           # NEW: writes NotificationState to UserDefaults
```

### Kotlin Files (commonMain/androidMain — NOT in scope)

```text
composeApp/src/
├── commonMain/.../data/model/
│   ├── NotificationData.kt              # ← Android branch: fromMap() accepts lsp_id
│   ├── NotificationState.kt             # ← Android branch: remove v5Preferences
│   ├── V5FilterPreferences.kt           # ← Android branch: DELETE
│   └── V5NotificationFilter.kt          # ← Android branch: DELETE
├── androidMain/.../workers/
│   └── NotificationWorker.kt            # ← Android branch: unified filter
└── iosMain/.../notifications/
    └── IosNotificationBridge.kt         # NO CHANGE (already uses V4 filter)
```

**Structure Decision**: 3 files deleted, 3 new files created (2 Swift in NSE, 1 Kotlin in iosMain). Net reduction in complexity.

---

## Root Cause Analysis

### Why V5 Filtering Is Broken

**Two independent failures:**

1. **iOS**: `IosNotificationBridge` calls V4 `NotificationFilter` which expects `agency_id` key. V5 payloads send `lsp_id` instead. `NotificationData.fromMap()` returns `null` → filtering fails → notification passes through unfiltered.

2. **Android + iOS**: `V5NotificationFilter` reads from `V5FilterPreferences` which is always at defaults (no UI sets these values). Defaults = `subscribedLspIds: null` (follow all) → everything allowed.

3. **iOS NSE**: Reads `V5FilterPreferences` from App Groups via `V5PreferencesSyncManager`, but the app never writes there → always defaults → allows everything.

### Why V4 Works

V4 `NotificationFilter` uses `NotificationState.subscribedAgencies: Set<String>`. These are populated from user settings UI. String comparison: `"121" in {"121", "44"}` → works.

---

## Implementation Strategy

### Phase 1: Delete Unused V5 Swift Files

**Deliverable**: All V5-specific Swift filter code removed from iosApp

1. **Delete `V5FilterPreferences.swift`** — replaced by Kotlin `NotificationState` via `IosNotificationBridge`
2. **Delete `V5NotificationFilter.swift`** — replaced by Kotlin `NotificationFilter` via `IosNotificationBridge`
3. **Delete `V5PreferencesSyncManager.swift`** — no longer needed (NotificationState already in App Groups via DataStore)
4. **Clean `V5NotificationData.swift`** — verify no remaining `V5NotificationFilter` enum or duplicate code. Keep the `V5NotificationData` struct (used by NSE for image attachment).
5. **Verify no dangling references** in `NotificationDebugHelper.swift`, `NotificationTestHelper.swift`, or other Swift files

**Validation**: Xcode build succeeds for iosApp target. No unresolved symbol errors.

### Phase 2: Implement NSE Preference Bridge (Kotlin)

**Deliverable**: Kotlin writes `NotificationState` filter fields to shared UserDefaults whenever preferences change

1. **Create `NSEPreferenceBridge.kt`** in `composeApp/src/iosMain/.../notifications/`:
   - Writes filter fields to `NSUserDefaults(suiteName: "group.me.spacelaunchnow.spacelaunchnow")`
   - Keys: `nse_enable_notifications`, `nse_follow_all_launches`, `nse_use_strict_matching`, `nse_subscribed_agencies`, `nse_subscribed_locations`
   - Expands `additionalIds` from `NotificationAgency` and `NotificationLocation` at write time
   - Calls `synchronize()` to flush writes
2. **Hook into state save path**: Call `NSEPreferenceBridge.syncToUserDefaults()` whenever `NotificationStateStorage.saveState()` runs on iOS
3. **Sync on app launch**: Ensure UserDefaults is up-to-date when app starts (in case user changed settings offline)

**Validation**: UserDefaults contains expected keys after saving notification settings.

### Phase 3: Implement NSE Swift Filter

**Deliverable**: NSE has lightweight Swift-native filter that reads preferences from shared UserDefaults

1. **Create `NSEFilterPreferences.swift`** in `iosApp/NotificationServiceExtension/`:
   - `static func load() -> NSEFilterPreferences` — reads from shared UserDefaults
   - Defaults: `enableNotifications: true, followAllLaunches: true` if keys unset (fresh install)
2. **Create `NSENotificationFilter.swift`** in `iosApp/NotificationServiceExtension/`:
   - `static func shouldShow(payload:preferences:) -> Bool`
   - Logic: kill switch → follow all bypass → agency match → location match → strict/flexible
   - IDs pre-expanded, so filter just uses `Set.contains()`
3. **Update `NotificationService.swift`**:
   - Load `NSEFilterPreferences.load()` after parsing V5 payload
   - Call `NSENotificationFilter.shouldShow()` to decide allow/block
   - If blocked: deliver empty content (iOS won't display)
   - If allowed: enrich (title/body/image) and deliver
4. **Add new files to NSE target** in `project.pbxproj`

**Validation**: Xcode build succeeds for NSE target. Filter logic matches spec.

### Phase 4: iOS Verification

**Deliverable**: Confirmation that iOS notification filtering works end-to-end

1. **Xcode builds**: Both iosApp and NotificationServiceExtension targets compile cleanly
2. **Grep verification**: No remaining references to `V5FilterPreferences`, `V5NotificationFilter`, `V5PreferencesSyncManager` in any Swift file
3. **IosNotificationBridge verification**: Confirm `shouldShowNotification()` is called from `AppDelegate` and routes through Kotlin `NotificationFilter`
4. **Manual test plan** (on device): Send V5 test notification → verify Kotlin bridge filters it → verify NSE attaches image

**Validation**: Clean build. No dead references.

### Phase 4 (DEFERRED — Future PR): NSE Client-Side Filtering

**Status**: 🔜 Not in this PR. Would add defense-in-depth filtering directly in the NSE process.

Requires:
- Kotlin writes JSON sidecar to App Groups with `NotificationState` subset
- Unify App Group identifiers (`group.me.spacelaunchnow.spacelaunchnow` vs `group.me.calebjones.spacelaunchnow`)
- Simple String-based filter logic in Swift (mirroring Kotlin `NotificationFilter`)
- Swift unit tests for NSE filter logic

---

## Data Flow (iOS — After Fix)

```
Firebase Cloud Messaging
         │
         ▼
    APNs (mutable-content: 1)
         │
         ▼
┌──────────────────────────────────────┐
│ NSE (NotificationService)              │
│ • Detect V5 payload                     │
│ • Load NSEFilterPreferences (UserDefaults)│
│ • Apply NSENotificationFilter            │
│   ├─ ALLOWED: enrich + attach image     │
│   └─ BLOCKED: deliver empty content     │
└────────┬─────────────────────────────┘
         │
         ▼
    AppDelegate.didReceiveRemoteNotification
         │
         ▼
    IosNotificationBridge
        .shouldShowNotification(data)
         │
         ▼
    NotificationFilter.shouldShowFromMap    ← Kotlin (commonMain)
    NotificationData.fromMap(data)          ← reads agency_id OR lsp_id
         │
         ├── state.subscribedAgencies.contains("121")  ← String match
         ├── state.subscribedLocations (+ additionalIds)
         ├── state.followAllLaunches → bypass
         └── state.useStrictMatching → AND/OR
         │
    ALLOWED → schedule local notification
    BLOCKED → suppress, save to history
```

### Preference Sync Flow

```
User changes settings in UI
         │
         ▼
NotificationStateStorage.saveState()      ← DataStore (.preferences_pb)
         │
         ▼
NSEPreferenceBridge.syncToUserDefaults()  ← UserDefaults (App Group)
         │
    Keys written (expanded IDs):
    nse_enable_notifications: true
    nse_follow_all_launches: false
    nse_subscribed_agencies: ["121"]
    nse_subscribed_locations: ["27", "12"]
```

---

## Risk Assessment

| Risk | Severity | Likelihood | Mitigation |
|------|----------|------------|------------|
| NSE UserDefaults stale (user changes settings, app is killed before sync) | Low | Low | Sync on every save + on app launch. UserDefaults writes are near-instant. |
| Deleted Swift files still referenced in Xcode project | Low | Medium | Grep all `.swift` files and `.pbxproj` for references after deletion. |
| NSE filter logic diverges from Kotlin filter | Medium | Low | NSE filter is intentionally simple (subset of Kotlin logic). IDs pre-expanded at write time to minimize NSE complexity. |
| App Group ID inconsistency | Info | N/A | Both IDs in entitlements. This PR uses `group.me.spacelaunchnow.spacelaunchnow` (matches Kotlin). Cleanup deferred. |
| Android branch not merged yet | Medium | Medium | This branch should be rebased on Android branch before merge. Deletions are safe even before Kotlin changes land. |
| Fresh install: UserDefaults empty | Low | Certain | Defaults to `followAllLaunches: true` — allows everything, which is correct for new users. |

---

## Success Criteria

- [x] Root cause identified and documented
- [ ] V5 Swift filter stack removed (~610 lines)
- [ ] NSE preference bridge implemented (Kotlin writes to UserDefaults)
- [ ] NSE Swift-native filter implemented (reads UserDefaults, applies filter)
- [ ] NSE filters independently when app is killed/terminated
- [ ] Xcode builds succeed for both iosApp and NSE targets
- [ ] No dangling references to deleted V5 types in any Swift file
- [ ] IosNotificationBridge still works for V4 + V5 payloads (verified via Kotlin tests in Android branch)
- [ ] No data migration required

---

## References

- Feature Spec: `specs/dev/ios/fix_ios.md`
- Research: `specs/dev/ios/research.md`
- Data Model: `specs/dev/ios/data-model.md`
- Quickstart: `specs/dev/ios/quickstart.md`
- Android V5 Fix Branch: (separate — handles Kotlin commonMain + androidMain)
- V4 Filter (working): `composeApp/src/commonMain/.../NotificationData.kt`
- iOS Kotlin Bridge: `composeApp/src/iosMain/.../IosNotificationBridge.kt`
