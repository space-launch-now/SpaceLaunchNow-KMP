# Plan: iOS V5 Notification Service Extension

**Parent Feature**: V5 Client-Side Notification System  
**Platform**: iOS only  
**Status**: 🔜 Deferred

---

## Technical Approach

### Architecture

**Notification Service Extension (NSE)** pattern:
- Separate iOS app extension that runs when notification with `mutable-content: 1` arrives
- Can modify notification before display or suppress it entirely
- Shares data with main app via App Groups

### Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| NSE Language | Swift | 5.9+ |
| iOS Minimum | iOS | 15.0+ |
| Data Sharing | App Groups | UserDefaults |
| Parsing | Swift Codable | Built-in |
| Filtering | Swift port | From common V5NotificationFilter |

### File Structure

```
iosApp/
├── NotificationServiceExtension/
│   ├── NotificationService.swift         # NSE entry point
│   ├── V5NotificationFilter.swift        # Swift port of filter logic
│   ├── Info.plist                        # NSE configuration
│   └── NotificationServiceExtension.entitlements
├── iosApp/
│   ├── AppDelegate.swift                 # Write preferences to App Group
│   ├── V5NotificationData.swift          # Already exists (Phase 3)
│   └── iosApp.entitlements               # Add App Group
└── iosApp.xcodeproj/                     # Updated with NSE target
```

---

## Implementation Strategy

### Phase 1: Xcode Project Setup

**Deliverable**: NSE target exists and can build

1. Create Notification Service Extension target
2. Add App Groups capability to both main app and NSE
3. Configure bundle identifiers correctly
4. Ensure NSE builds without errors

**Validation**: Run app, NSE target appears in scheme dropdown

---

### Phase 2: Data Sharing

**Deliverable**: Preferences sync between app and NSE

1. Update main app to write V5FilterPreferences to App Group UserDefaults
2. Create `V5PreferencesSyncManager.swift` for serialization
3. Test reading preferences in NSE

**Validation**: Change filter in app, read in NSE, values match

---

### Phase 3: V5 Parsing in NSE

**Deliverable**: NSE can parse V5 payloads

1. Add `V5NotificationData.swift` to NSE target (already exists)
2. Parse FCM data dictionary in NSE
3. Detect V5 payload via `lsp_id` field

**Validation**: Send V5 test notification, NSE logs parsed payload

---

### Phase 4: Filter Logic Port

**Deliverable**: V5 filter logic works in Swift

1. Port `V5NotificationFilter.kt` to `V5NotificationFilter.swift`
2. Implement all filter types (LSP, location, programs, etc.)
3. Support strict vs flexible matching
4. Add unit tests for Swift filter logic

**Validation**: Unit tests pass matching Kotlin behavior

---

### Phase 5: Notification Modification

**Deliverable**: NSE can block or allow notifications

1. Implement allow path: pass notification through unchanged
2. Implement block path: deliver empty `UNMutableNotificationContent`
3. Add logging for filter decisions

**Validation**: Test both allow and block scenarios

---

### Phase 6: Testing & Polish

**Deliverable**: Production-ready NSE

1. Add error handling for malformed payloads
2. Performance validation (< 1 second execution)
3. Test edge cases (missing fields, null values)
4. Add debug logging toggle

**Validation**: All test cases pass

---

## Data Flow

### Main App → NSE

```swift
// Main app writes preferences
let preferences = V5FilterPreferences(
    enableNotifications: true,
    subscribedLspIds: [121, 122],
    // ... other settings
)

let encoder = JSONEncoder()
let data = try encoder.encode(preferences)
let json = String(data: data, encoding: .utf8)

let sharedDefaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow")
sharedDefaults?.set(json, forKey: "v5FilterPreferences")
```

### NSE Reads & Filters

```swift
// NSE reads preferences
let sharedDefaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow")
guard let json = sharedDefaults?.string(forKey: "v5FilterPreferences"),
      let data = json.data(using: .utf8),
      let preferences = try? JSONDecoder().decode(V5FilterPreferences.self, from: data)
else {
    // Default: allow all if preferences unavailable
    contentHandler(request.content)
    return
}

// Parse V5 payload
guard let payload = V5NotificationData(userInfo: request.content.userInfo) else {
    contentHandler(request.content)
    return
}

// Apply filter
let result = V5NotificationFilter.shouldShow(payload: payload, preferences: preferences)

if result.shouldShow {
    // Allow notification
    contentHandler(request.content)
} else {
    // Block notification (silent drop)
    let emptyContent = UNMutableNotificationContent()
    contentHandler(emptyContent)
}
```

---

## Testing Strategy

### Unit Tests

**V5NotificationFilter.swift**:
- Test all filter types match Kotlin behavior
- Test strict vs flexible matching
- Test edge cases (nil values, empty arrays)

### Integration Tests

**NSE Flow**:
1. Send V5 notification with allowed LSP → notification appears
2. Send V5 notification with blocked LSP → no notification
3. Send V5 notification with webcast filter → correct behavior
4. Send V4 notification → passes through (fallback)

### Manual Testing Checklist

- [ ] Filter by SpaceX only → Only SpaceX notifications appear
- [ ] Filter by KSC only → Only KSC notifications appear
- [ ] Webcast-only filter → Only webcasted launches notify
- [ ] Disable all notifications → No notifications appear
- [ ] Change filter in app → NSE uses new filter immediately
- [ ] Test with airplane mode → Graceful degradation

---

## Security Considerations

1. **App Group Permissions**: Only accessible by main app and NSE
2. **Entitlements**: Properly configured in both targets
3. **Data Validation**: NSE must validate all incoming data
4. **Fallback Behavior**: If filter unavailable, default to ALLOW (user safety)

---

## Performance Targets

| Metric | Target | Reasoning |
|--------|--------|-----------|
| NSE Execution | < 1s | Apple 30s limit, aim for sub-second |
| Filter Evaluation | < 10ms | Minimal processing time |
| Memory Usage | < 10MB | NSE memory constrained |
| Preference Read | < 50ms | UserDefaults read fast |

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| NSE crashes | No notifications shown | Extensive error handling, default to allow |
| App Group misconfigured | Preferences unavailable | Default to allow all, log error |
| Swift filter diverges from Kotlin | Inconsistent behavior | Shared unit test suite |
| Performance issues | Battery drain | Profile with Instruments, optimize |
| iOS version fragmentation | Some users no filtering | Minimum iOS 15, acceptable |

---

## Rollout Plan

### Beta Phase

1. Deploy iOS beta with NSE to TestFlight
2. Monitor crash logs and filter behavior
3. A/B test: 50% NSE enabled, 50% disabled
4. Collect feedback on notification accuracy

### Production Phase

1. Full rollout after 2 weeks beta validation
2. Monitor Firebase Console for topic subscription changes
3. Watch for NSE crash rate (target: < 0.1%)
4. User feedback collection via in-app survey

---

## Success Criteria

- [ ] NSE builds and runs without errors
- [ ] Filter logic matches Kotlin common code 100%
- [ ] Preferences sync between app and NSE reliably
- [ ] Notification allow/block behavior correct in all test cases
- [ ] Performance targets met (< 1s execution, < 10MB memory)
- [ ] No production crashes related to NSE
- [ ] User feedback positive (fewer unwanted notifications)

---

## References

- Apple NSE Documentation: https://developer.apple.com/documentation/usernotifications/unnotificationserviceextension
- App Groups Guide: https://developer.apple.com/documentation/xcode/configuring-app-groups
- Firebase iOS Setup: https://firebase.google.com/docs/cloud-messaging/ios/client
