# Tasks: iOS V5 Notification Service Extension

**Parent Spec**: `/specs/dev/` (V5 Client-Side Notification System)  
**Prerequisites**: Common V5 models (Phase 1-2 complete), V5 parsing (Phase 3 complete)

## Overview

This task list covers iOS-specific implementation for V5 client-side notifications using Notification Service Extension (NSE).

**Status**: 🔜 Deferred - Waiting for Android V5 validation

---

## Phase 6: User Story 4 - iOS Notification Service Extension (Priority: P2)

**Goal**: Intercept mutable-content notifications on iOS and apply client-side filtering

**Independent Test**: Send V5 iOS notification, verify NSE intercepts and filters correctly

### Implementation for User Story 4

- [ ] T028 [US4] Create Notification Service Extension target in Xcode project `iosApp/NotificationServiceExtension/`
- [ ] T029 [US4] Configure App Groups capability for main app and NSE with identifier `group.me.calebjones.spacelaunchnow`
- [ ] T030 [US4] Create `NotificationService.swift` extension class in `iosApp/NotificationServiceExtension/NotificationService.swift`
- [ ] T031 [US4] Implement `didReceive(_:withContentHandler:)` to parse V5 payload
- [ ] T032 [US4] Implement filter logic reading preferences from shared UserDefaults (App Groups)
- [ ] T033 [US4] Implement notification suppression by delivering empty `UNMutableNotificationContent`
- [ ] T034 [US4] Add NSE Info.plist configuration in `iosApp/NotificationServiceExtension/Info.plist`
- [ ] T035 [US4] Update main app to write preferences to shared App Group UserDefaults
- [ ] T036 [P] [US4] Add logging for NSE filter decisions for debugging

**Checkpoint**: iOS filters V5 notifications via NSE before display

---

## Additional iOS-Specific Tasks

### Phase 7 iOS Topics (from main tasks.md)

- [X] T039 [US5] Update `IosPushMessaging` to subscribe to V5 iOS topics in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosPushMessaging.kt`
- [X] T040 [US5] Update `AppDelegate.swift` to subscribe to V5 iOS topic on token refresh in `iosApp/iosApp/AppDelegate.swift`

---

## Technical Context

### V5 Notification Flow on iOS

```
Firebase Cloud Messaging
    ↓
iOS APNs (mutable-content: 1)
    ↓
Notification Service Extension (NSE)
    ↓
Parse V5 payload from userInfo dict
    ↓
Read V5FilterPreferences from App Group UserDefaults
    ↓
Apply V5NotificationFilter logic
    ↓
BLOCKED? → Deliver empty content (silent drop)
ALLOWED? → Deliver full content with title/body/image
```

### App Groups Setup

**Identifier**: `group.me.calebjones.spacelaunchnow`

**Main App Writes**:
```swift
let sharedDefaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow")
sharedDefaults?.set(preferencesJSON, forKey: "v5FilterPreferences")
```

**NSE Reads**:
```swift
let sharedDefaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow")
let preferencesJSON = sharedDefaults?.string(forKey: "v5FilterPreferences")
```

### Xcode Project Changes Required

1. **Create NSE Target**:
   - File → New → Target → Notification Service Extension
   - Product Name: `NotificationServiceExtension`
   - Language: Swift
   - Minimum iOS: 15.0

2. **Configure Capabilities**:
   - Main App: Capabilities → App Groups → `group.me.calebjones.spacelaunchnow`
   - NSE: Capabilities → App Groups → `group.me.calebjones.spacelaunchnow`

3. **Bundle Identifier**:
   - Main: `me.calebjones.spacelaunchnow`
   - NSE: `me.calebjones.spacelaunchnow.NotificationServiceExtension`

4. **Entitlements**:
   - Both targets need App Group entitlement in `.entitlements` file

### Testing iOS NSE

**Send Test Notification via Firebase Console**:

```json
{
  "to": "/topics/debug_v5_ios",
  "mutable_content": true,
  "data": {
    "notification_type": "tenMinutes",
    "title": "🚀 SpaceX Launch in 10 Minutes",
    "body": "Falcon 9 Block 5 | Starlink Group 6-32",
    "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
    "lsp_id": "121",
    "location_id": "27",
    "webcast": "true"
  }
}
```

**Verify**:
1. NSE breakpoint in `didReceive()` hits
2. Payload parsed correctly with `lsp_id` field
3. Filter preferences loaded from App Group
4. Correct allow/block decision logged
5. Notification appears (allowed) or doesn't appear (blocked)

---

## Dependencies

**Requires from main spec**:
- ✅ Phase 1: V5 topic constants
- ✅ Phase 2: V5NotificationPayload, V5FilterPreferences, FilterResult
- ✅ Phase 3: V5 parsing logic (Swift V5NotificationData)
- ✅ Phase 4: V5NotificationFilter logic (needs Swift port)
- ✅ Phase 7: iOS topic subscription (T039, T040)

**Blocks**:
- Phase 10: Full iOS validation before production release

---

## Notes

- iOS NSE runs in separate process from main app - cannot share memory, only App Group UserDefaults
- NSE has 30-second execution limit - keep filter logic fast
- NSE cannot write to main app's storage - use shared App Group container
- For debugging: NSE logs appear in Xcode Console when device connected, or in Console app on macOS
- Alternative to App Group: Use shared keychain (more secure but slower)
