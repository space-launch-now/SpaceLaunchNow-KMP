# V5 Client-Side Notification Implementation Specification

**Branch**: `dev`  
**Date**: 2026-01-26  
**Author**: Copilot Agent

## Overview

Implement V5 notification handling in the SpaceLaunchNow KMP app to support the new server notification format with enhanced client-side filtering capabilities.

### Key Changes from V4

| Aspect | V4 | V5 |
|--------|----|----|
| **Topic Format** | `k_prod_v4`, `k_debug_v4` | `prod_v5_android`, `prod_v5_ios`, `debug_v5_android`, `debug_v5_ios` |
| **Android Payload** | Mixed (notification + data) | Data-only FCM (app constructs notifications) |
| **iOS Payload** | APNs alert | APNs alert with `mutable-content: 1` (NSE intercepts) |
| **Filtering IDs** | `agency_id`, `location_id` | Extended: `lsp_id`, `location_id`, `program_ids`, `status_id`, `orbit_id`, `mission_type_id`, `launcher_family_id` |

## Requirements

### Functional Requirements

1. **FR-1**: Subscribe to platform-specific V5 topics (`prod_v5_android`/`debug_v5_android` on Android, `prod_v5_ios`/`debug_v5_ios` on iOS)
2. **FR-2**: Subscribe to notification type topics: `tenMinutes`, `oneHour`, `twentyFourHour`, `netstampChanged`, `inFlight`, `success`, `failure`, `webcastLive`
3. **FR-3**: Parse V5 notification payload with extended filtering IDs
4. **FR-4**: Apply client-side filtering based on user preferences before displaying notifications
5. **FR-5**: Construct and display notifications on Android (data-only messages)
6. **FR-6**: Intercept and filter notifications on iOS via Notification Service Extension
7. **FR-7**: Support deep linking to launch detail screen from notification tap
8. **FR-8**: Provide UI for users to configure notification filter preferences
9. **FR-9**: Migrate existing users to V5 topics on app update

### Non-Functional Requirements

1. **NFR-1**: Notification display latency < 500ms after receiving data payload
2. **NFR-2**: Filter evaluation < 10ms per notification
3. **NFR-3**: Maintain battery efficiency (no excessive background processing)
4. **NFR-4**: Support offline preference storage (App Groups on iOS)

## V5 Notification Payload Schema

```json
{
  "notification_type": "string",      // tenMinutes, oneHour, twentyFourHour, etc.
  "title": "string",                   // Server-provided title
  "body": "string",                    // Server-provided body
  "launch_uuid": "string",             // UUID for deep linking
  "launch_id": "string",               // Library ID
  "launch_name": "string",             // Display name
  "launch_image": "string",            // Image URL (optional)
  "launch_net": "string",              // ISO 8601 datetime
  "launch_location": "string",         // Location name
  "webcast": "boolean",                // Has webcast
  "webcast_live": "boolean",           // Is webcast live
  // Extended Filtering IDs (V5 additions)
  "lsp_id": "number",                  // Launch Service Provider ID
  "location_id": "number",             // Launch location ID
  "program_ids": "string",             // Comma-separated program IDs
  "status_id": "number",               // Launch status ID (optional)
  "orbit_id": "number",                // Target orbit ID (optional)
  "mission_type_id": "number",         // Mission type ID (optional)
  "launcher_family_id": "number"       // Launcher family ID (optional)
}
```

## Android Implementation Details

### Topic Subscription
- Primary topic: `prod_v5_android` (release) or `debug_v5_android` (debug)
- Type topics for FCM topic condition filtering (server-side)

### Data-Only Message Handling
- FCM messages have NO `notification` block
- App MUST construct and display notification via `NotificationCompat.Builder`
- WorkManager handles background processing (image loading)
- `NotificationDisplayHelper` constructs notification with proper formatting

### Migration Strategy
- On first launch after update, unsubscribe from V4 topics
- Subscribe to V5 topics
- Migrate user preferences (agency IDs → lsp IDs if different)

## iOS Implementation Details

### Notification Service Extension
- Create `NotificationServiceExtension` target if not exists
- Intercept `mutable-content: 1` notifications before display
- Apply client-side filtering using shared preferences (App Groups)
- Suppress by delivering empty content if filtered

### App Groups Configuration
- Create shared App Group for main app + NSE
- Store user preferences in shared `UserDefaults`
- Extension reads preferences at runtime

### Migration Strategy
- Same approach as Android
- Ensure App Group is configured for existing users

## Success Criteria

1. ✅ Android receives and displays V5 data-only notifications
2. ✅ iOS intercepts and filters V5 mutable-content notifications  
3. ✅ User preferences correctly filter notifications on both platforms
4. ✅ Deep linking works from notification tap to launch detail
5. ✅ V4 → V5 migration completes without user data loss
