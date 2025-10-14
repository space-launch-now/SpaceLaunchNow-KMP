# v4 Notification System - Client-Side Filtering

## Overview

The v4 notification system dramatically simplifies the topic subscription architecture by moving all filtering logic to the client side. Instead of managing dozens of complex FCM topic subscriptions, the app now subscribes to a single version-based topic and filters notifications locally based on user preferences.

## Architecture Changes

### Old System (v3)
- **Complex Topic Management**: Subscribed to 30+ FCM topics
- **Server-Side Filtering**: Topics like `spacex`, `nasa`, `twentyFourHour`, `strict`, etc.
- **Subscription Hell**: Every preference change triggered multiple FCM subscribe/unsubscribe operations
- **Hard to Maintain**: Adding new filters required backend topic changes

### New System (v4)
- **Simple Topic Subscription**: Only subscribe to `k_prod_v4` OR `k_debug_v4`
- **Client-Side Filtering**: All filtering done in the app based on notification data
- **Instant Feedback**: Preference changes take effect immediately without FCM operations
- **Easy to Extend**: New filters just require app changes, no backend work

## Server Data Format

The server sends notifications with this data payload:

```json
{
  "notification_type": "twentyFourHour",      // Timing type
  "launch_id": "1234",                        // Library ID
  "launch_uuid": "550e8400-...",              // UUID
  "launch_name": "Falcon 9 Block 5",
  "launch_image": "https://...",              
  "launch_net": "October 13, 2025 14:30:00 UTC",
  "launch_location": "Kennedy Space Center",
  "webcast": "true",                          // Boolean as string
  "agency_id": "121",                         // SpaceX = 121
  "location_id": "27"                         // KSC = 27
}
```

## Client-Side Filtering Logic

The `NotificationFilter.shouldShowNotification()` method applies these checks in order:

### 1. Global Notifications Toggle
```kotlin
if (!state.enableNotifications) return false
```
Master switch - if disabled, no notifications shown.

### 2. Webcast-Only Filter
```kotlin
if (webcastOnly && !data.hasWebcast()) return false
```
If user only wants launches with webcasts, filter out those without.

### 3. Notification Type (Timing) Filter
```kotlin
isNotificationTypeEnabled(data.notificationType, state)
```
Check if user enabled this timing notification:
- `twentyFourHour` → 24 hours before launch
- `oneHour` → 1 hour before launch
- `tenMinutes` → 10 minutes before launch
- `oneMinute` → 1 minute before launch
- `inFlight` → During launch
- `success` → Launch success
- `netstampChanged` → Launch time changed

### 4. Follow All Launches (Skip Filtering)
```kotlin
if (state.followAllLaunches) return true
```
If following all launches, skip agency/location filtering.

### 5. Agency & Location Filtering
```kotlin
val agencyMatch = state.subscribedAgencies.contains(data.agencyId)
val locationMatch = state.subscribedLocations.contains(data.locationId)
```

### 6. Strict vs Flexible Matching
```kotlin
if (state.useStrictMatching) {
    // Strict: BOTH agency AND location must match
    return agencyMatch && locationMatch
} else {
    // Flexible: EITHER agency OR location must match
    return agencyMatch || locationMatch
}
```

**Important**: `useStrictMatching` is **ignored** when `followAllLaunches` is enabled. This is intentional - if you're following all launches, you want ALL launches regardless of matching mode.

## Key Components

### 1. NotificationData Model
**File**: `data/model/NotificationData.kt`
**Platform**: Common (shared across iOS and Android)

Parses FCM data payload into typed structure:
```kotlin
val data = NotificationData.fromMap(remoteMessage.data)
```

### 2. NotificationFilter Object  ⭐ PLATFORM-AGNOSTIC
**File**: `data/model/NotificationData.kt`
**Platform**: Common (shared across iOS and Android)

**This is the magic** - one filter that works everywhere!

Contains filtering logic that works on both iOS and Android:
```kotlin
// Used by both platforms!
val shouldShow = NotificationFilter.shouldShowNotification(data, state)
```

**Convenience method for iOS Swift interop**:
```kotlin
// For iOS: convert [String: String] dictionary to Kotlin Map
val shouldShow = NotificationFilter.shouldShowFromMap(dataMap, state)
```

**Key Features**:
- ✅ Written once in common code
- ✅ Same behavior on iOS and Android  
- ✅ Easy to test (platform-agnostic unit tests)
- ✅ Easy to maintain (one place to update)

### 3. SpaceLaunchFirebaseMessagingService (Android)
**File**: `androidMain/.../services/SpaceLaunchFirebaseMessagingService.kt`
**Platform**: Android

Receives FCM messages and applies filtering:
```kotlin
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val data = NotificationData.fromMap(remoteMessage.data)
    val shouldShow = NotificationFilter.shouldShowNotification(data, state)
    if (shouldShow) showNotification(...)
}
```

### 4. NotificationDelegate (iOS - Future)
**File**: `iosApp/iosApp/NotificationDelegate.swift`
**Platform**: iOS

Will receive notifications and use the SAME filtering:
```swift
func userNotificationCenter(...) {
    let dataDict = extractDataDictionary(from: userInfo)
    let settings = getNotificationSettings()
    
    // Uses SAME filter as Android!
    let shouldShow = NotificationFilter.shared.shouldShowFromMap(
        dataMap: dataDict,
        state: settings
    )
    
    if shouldShow {
        completionHandler([.banner, .sound, .badge])
    } else {
        completionHandler([])
    }
}
```

### 5. SubscriptionProcessor
**File**: `data/repository/SubscriptionProcessor.kt`
**Platform**: Common (shared across iOS and Android)

Simplified to only subscribe to version topic:
```kotlin
private suspend fun calculateRequiredTopics(state: NotificationState): Set<String> {
    val topics = mutableSetOf<String>()
    val versionTopic = getVersionTopic() // "k_prod_v4" or "k_debug_v4"
    topics.add(versionTopic)
    return topics
}
```

### 6. NotificationState Model
**File**: `data/model/NotificationState.kt`
**Platform**: Common (shared across iOS and Android)

Changed to store **agency/location IDs** instead of topic names:
```kotlin
// Old (v3): subscribedAgencies = ["spacex", "nasa", "blueOrigin"]
// New (v4): subscribedAgencies = ["121", "44", "141"]
val subscribedAgencies: Set<String> = getDefaultAgencyIds()
```

## Benefits

### Platform-Agnostic Filtering ⭐
- ✅ **Write Once**: Filtering logic written once in common code
- ✅ **Use Everywhere**: Same code runs on iOS, Android, and even Desktop
- ✅ **Consistent Behavior**: Users get identical filtering on all platforms
- ✅ **Easy Testing**: Platform-agnostic unit tests
- ✅ **Easy Maintenance**: One place to update, automatically applies everywhere

### Performance
- ✅ **90% Fewer FCM Operations**: From 30+ topics to just 1
- ✅ **Instant Preference Changes**: No waiting for FCM subscribe/unsubscribe
- ✅ **Reduced Network Usage**: Fewer FCM API calls

### Maintainability
- ✅ **Client-Only Changes**: New filters don't require backend updates
- ✅ **Simpler Code**: No complex topic calculation logic
- ✅ **Better Testing**: Filter logic is pure functions

### User Experience
- ✅ **Immediate Feedback**: Preference changes take effect instantly
- ✅ **More Control**: Can add sophisticated filters without backend changes
- ✅ **Reliable**: No FCM subscription race conditions

## Migration Notes

### Breaking Changes
1. **Topic Names → IDs**: Agency/location storage changed from topic names to numeric IDs
2. **Version Topics**: Changed from `prod_v3`/`debug_v3` to `k_prod_v4`/`k_debug_v4`
3. **No Topic Subscriptions for Preferences**: Changing agency/location preferences no longer triggers FCM operations

### Data Migration
Old data will automatically migrate:
- Old topic names will be removed from subscriptions
- Default agencies/locations will be set using numeric IDs
- User preferences will reset to defaults (acceptable for major version)

### Server Requirements
Server must send notifications with the v4 data format:
```python
v4_data = {
    "notification_type": notification_type,
    "launch_id": str(launch.launch_library_id),
    "launch_uuid": str(launch.id),
    "launch_name": launch.name,
    "launch_image": image,
    "launch_net": launch.net.strftime("%B %d, %Y %H:%M:%S %Z"),
    "launch_location": launch.pad.location.name,
    "webcast": str(webcast),
    "agency_id": str(get_agency_topic(launch)),
    "location_id": str(get_location_topic(launch)),
}
```

## Testing Checklist

- [ ] Subscribe to `k_debug_v4` topic
- [ ] Send test notification with v4 data format
- [ ] Verify global toggle disables all notifications
- [ ] Verify webcast-only filter works
- [ ] Verify timing filters work (24h, 1h, 10m, etc.)
- [ ] Verify "Follow All" bypasses agency/location filters
- [ ] Verify strict matching requires both agency AND location
- [ ] Verify flexible matching accepts agency OR location
- [ ] Verify notification shown when filters pass
- [ ] Verify notification suppressed when filters fail

## Known Limitations

1. **All Notifications Received**: Device still receives all notifications from `k_prod_v4`, but they're filtered locally. This is acceptable as the data payload is small.
2. **Battery Impact**: Minimal - filtering is very fast and only happens when notification arrives.
3. **Storage**: Agency/location IDs stored as strings for flexibility, could be integers.

## Future Enhancements

Possible improvements for future versions:
- [ ] Add notification history/log for debugging
- [ ] Add "why was this shown/hidden" debug info
- [ ] Add custom filter rules (advanced users)
- [ ] Add notification scheduling (quiet hours, etc.)
- [ ] Add notification grouping by agency/type

## Example Scenarios

### Scenario 1: SpaceX Only, No Webcasts Required
```kotlin
state = NotificationState(
    enableNotifications = true,
    followAllLaunches = false,
    subscribedAgencies = setOf("121"), // SpaceX
    subscribedLocations = setOf("27"), // KSC
)
```
**Result**: Only shows SpaceX launches from KSC (flexible matching)

### Scenario 2: All Launches, Webcast Only
```kotlin
state = NotificationState(
    enableNotifications = true,
    followAllLaunches = true,
    topicSettings = mapOf("webcastOnly" to true)
)
```
**Result**: Shows all launches but only if they have webcasts

### Scenario 3: Strict Matching
```kotlin
state = NotificationState(
    enableNotifications = true,
    followAllLaunches = false,
    useStrictMatching = true,
    subscribedAgencies = setOf("121"), // SpaceX
    subscribedLocations = setOf("27"), // KSC
)
```
**Result**: ONLY shows SpaceX launches from KSC (both must match)

## Debugging

Enable verbose logging:
```kotlin
println("=== FCM Message Received ===")
println("Parsed notification data: type=${data.notificationType}")
println("Filter result: $shouldShow")
```

Check logs for these messages:
- `🔇 Notifications disabled globally`
- `🔇 Webcast-only filter: launch has no webcast`
- `🔇 Notification type 'X' is disabled`
- `✅ Following all launches - notification allowed`
- `🔇 Filtered out - agency: false, location: false`
- `✅ Filter passed - agency: true, location: true`
- `✅ Notification passed filters, showing to user`
