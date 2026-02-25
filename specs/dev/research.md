# Research: V5 Client-Side Notification System

**Date**: 2026-01-26  
**Feature**: V5 Notification Handling  
**Status**: Complete

## Research Tasks

### 1. FCM Data-Only Messages (Android)

**Question**: How do data-only FCM messages work on Android, and what are the limitations?

**Decision**: Use WorkManager to process data-only FCM messages

**Rationale**:
- Data-only FCM messages (no `notification` block) are delivered to `onMessageReceived()` even when app is in background/killed
- However, `onMessageReceived()` has ~10 second execution window
- Image loading can exceed this window, causing notification to be lost
- WorkManager guarantees execution with no time limit
- Existing V4 implementation already uses this pattern successfully

**Alternatives Considered**:
- Direct notification construction in `onMessageReceived()` - Rejected: image loading may timeout
- Using `notification` block for display - Rejected: loses client-side filtering control

**Best Practices**:
```kotlin
// In FirebaseMessagingService
override fun onMessageReceived(message: RemoteMessage) {
    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInputData(workDataOf(message.data))
        .build()
    WorkManager.getInstance(context).enqueue(workRequest)
}
```

### 2. iOS Notification Service Extension (NSE)

**Question**: How to intercept notifications before display on iOS for client-side filtering?

**Decision**: Create Notification Service Extension with App Groups for shared preferences

**Rationale**:
- NSE is the ONLY way to intercept `mutable-content: 1` notifications before display
- NSE runs in separate process with ~30 second execution window
- App Groups required for NSE to access main app's UserDefaults
- Suppression achieved by delivering empty `UNNotificationContent`

**Alternatives Considered**:
- Using `willPresent` in main app - Rejected: only works when app is in foreground
- Silent push + local notification - Rejected: unreliable delivery, background execution limits

**Best Practices**:
```swift
class NotificationService: UNNotificationServiceExtension {
    override func didReceive(_ request: UNNotificationRequest, 
                            withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        // Access shared UserDefaults via App Group
        let defaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow")
        
        // Parse and filter
        if shouldFilter(request.content.userInfo, defaults: defaults) {
            // Suppress notification
            contentHandler(UNMutableNotificationContent())
        } else {
            contentHandler(request.content)
        }
    }
}
```

### 3. V5 Payload Schema Compatibility

**Question**: How to maintain backward compatibility with V4 while supporting V5 extended fields?

**Decision**: Check for `lsp_id` field presence to detect V5 messages

**Rationale**:
- V4 uses `agency_id`, V5 uses `lsp_id` (same concept, different name)
- Presence of `lsp_id` indicates V5 payload
- Can fallback to V4 parsing if V5 fields missing
- Server can send to both V4 and V5 topics during migration period

**Alternatives Considered**:
- Version field in payload - Rejected: requires server-side changes to all messages
- Topic-based detection only - Rejected: doesn't help with malformed messages

**Best Practices**:
```kotlin
fun parseNotificationPayload(data: Map<String, String>): NotificationPayload {
    return if (data.containsKey("lsp_id")) {
        parseV5Payload(data)
    } else {
        parseV4Payload(data)  // Backward compatibility
    }
}
```

### 4. Extended Filter Categories

**Question**: What filter categories should V5 support, and how to persist them?

**Decision**: Add 6 new filter categories: LSP, Location, Program, Orbit, Mission Type, Launcher Family

**Rationale**:
- These categories match server-side data model
- Enable fine-grained user control over notifications
- IDs are stable and provided in every V5 payload
- Can be stored as `Set<Int>` in preferences

**Filter Category Details**:

| Category | ID Field | Example Values | Storage |
|----------|----------|----------------|---------|
| LSP | `lsp_id` | 121 (SpaceX), 44 (NASA) | `Set<Int>` |
| Location | `location_id` | 27 (Florida), 11 (California) | `Set<Int>` |
| Program | `program_ids` | ISS, Starlink, etc. | `Set<Int>` |
| Orbit | `orbit_id` | LEO, GTO, etc. | `Set<Int>` |
| Mission Type | `mission_type_id` | Crew, Resupply, etc. | `Set<Int>` |
| Launcher Family | `launcher_family_id` | Falcon 9, Atlas V, etc. | `Set<Int>` |

**Alternatives Considered**:
- String-based IDs - Rejected: less type-safe, larger storage
- Enum-based filtering - Rejected: requires app update for new values

### 5. Topic Migration Strategy

**Question**: How to migrate users from V4 to V5 topics without losing notifications?

**Decision**: Subscribe to V5 topics first, then unsubscribe from V4 after confirmation

**Rationale**:
- Prevents notification gap during migration
- FCM topic operations are asynchronous; need confirmation before unsubscribe
- Server should send to both V4 and V5 during transition period
- Migration flag stored to prevent repeated migrations

**Migration Flow**:
```
1. Check migration flag (has_migrated_to_v5)
2. If not migrated:
   a. Subscribe to V5 topic (prod_v5_android / prod_v5_ios)
   b. Wait for confirmation
   c. Unsubscribe from V4 topic (k_prod_v4 / k_debug_v4)
   d. Set migration flag = true
3. If already migrated, skip
```

**Alternatives Considered**:
- Force immediate unsubscribe - Rejected: may lose notifications during async operation
- Keep both subscriptions permanently - Rejected: users receive duplicate notifications

### 6. Notification Type Topics

**Question**: Should notification type topics (tenMinutes, oneHour, etc.) be subscribed client-side or used for server-side filtering?

**Decision**: Subscribe to type topics for server-side condition filtering, use client-side filtering as backup

**Rationale**:
- Server can use FCM topic conditions to reduce unnecessary message delivery
- Example: `'tenMinutes' in topics && 'prod_v5_android' in topics`
- Client-side filtering catches edge cases and provides double-check
- Reduces data usage for users who disable certain notification types

**Implementation**:
```kotlin
// Subscribe to enabled notification types
val enabledTypes = listOf("tenMinutes", "oneHour", "twentyFourHour")
enabledTypes.forEach { type ->
    FirebaseMessaging.getInstance().subscribeToTopic(type)
}
```

## Open Questions (Resolved)

1. ~~How to handle notification images in data-only messages?~~ → Use Coil/Glide async load in WorkManager
2. ~~App Group identifier format?~~ → `group.me.calebjones.spacelaunchnow`
3. ~~V5 topic naming convention?~~ → `prod_v5_android`, `prod_v5_ios`, `debug_v5_android`, `debug_v5_ios`
4. ~~Default filter values?~~ → All categories enabled by default (maximum notifications)

## References

- [Firebase Data Messages (Android)](https://firebase.google.com/docs/cloud-messaging/android/receive)
- [UNNotificationServiceExtension (Apple)](https://developer.apple.com/documentation/usernotifications/unnotificationserviceextension)
- [App Groups (Apple)](https://developer.apple.com/documentation/xcode/configuring-app-groups)
- [FCM Topic Conditions](https://firebase.google.com/docs/cloud-messaging/send-message#send-messages-to-topics)
