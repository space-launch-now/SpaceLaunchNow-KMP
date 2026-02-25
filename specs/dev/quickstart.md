# Quickstart: V5 Client-Side Notification System

**Date**: 2026-01-26  
**Feature**: V5 Notification Handling

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Xcode 15.0 or later
- JDK 21 (JetBrains JDK recommended)
- Firebase project configured for both Android and iOS

## Quick Setup

### 1. Android Setup (5 minutes)

No additional setup required. The existing Firebase configuration supports V5 messages.

**Verification**:
```bash
# Build and run on Android
./gradlew installDebug
```

### 2. iOS Setup (15 minutes)

#### A. Create Notification Service Extension

1. In Xcode, select **File → New → Target**
2. Choose **Notification Service Extension**
3. Name it `NotificationServiceExtension`
4. Set Team and Bundle ID: `me.calebjones.spacelaunchnow.NotificationServiceExtension`

#### B. Configure App Groups

1. In project settings, select **iosApp** target
2. Go to **Signing & Capabilities**
3. Click **+ Capability** → **App Groups**
4. Add group: `group.me.calebjones.spacelaunchnow`

5. Select **NotificationServiceExtension** target
6. Add same App Group: `group.me.calebjones.spacelaunchnow`

#### C. Add Firebase to Extension

In `NotificationServiceExtension/Info.plist`, add:
```xml
<key>NSExtensionPrincipalClass</key>
<string>$(PRODUCT_MODULE_NAME).NotificationService</string>
```

### 3. Topic Subscription

V5 topics are automatically subscribed on app launch. For manual testing:

**Android (Kotlin)**:
```kotlin
// Subscribe to V5 topic
FirebaseMessaging.getInstance().subscribeToTopic("debug_v5_android")
```

**iOS (Swift)**:
```swift
// Subscribe to V5 topic
Messaging.messaging().subscribe(toTopic: "debug_v5_ios")
```

## Testing Notifications

### Option 1: Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project → **Cloud Messaging**
3. Click **Create your first campaign** → **Firebase Notification messages**
4. Under **Target**, select **Topic** and enter `debug_v5_android` or `debug_v5_ios`
5. Enter notification content
6. Click **Additional options (optional)** → **Custom data**
7. Add the V5 payload fields (see below)
8. Click **Review** → **Publish**

### Option 2: FCM HTTP v1 API

```bash
# Get OAuth2 token (replace with your service account)
TOKEN=$(gcloud auth print-access-token)

# Send V5 notification to Android
curl -X POST \
  "https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "topic": "debug_v5_android",
      "data": {
        "notification_type": "tenMinutes",
        "title": "🚀 Test Launch",
        "body": "Launch in 10 minutes!",
        "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
        "launch_id": "99999",
        "launch_name": "Test Rocket | Test Mission",
        "launch_net": "2026-01-26T15:00:00Z",
        "launch_location": "Test Launch Site",
        "webcast": "true",
        "webcast_live": "false",
        "lsp_id": "121",
        "location_id": "27",
        "program_ids": "1,5",
        "status_id": "1",
        "orbit_id": "8",
        "mission_type_id": "2",
        "launcher_family_id": "5"
      }
    }
  }'
```

### Option 3: In-App Debug Menu

1. Open the app
2. Navigate to **Settings** → **Debug** (long-press on version number)
3. Tap **Send Test Notification**
4. Select **V5 Format**
5. Configure test payload
6. Tap **Send**

## Verifying Filter Behavior

### Test Case 1: Filter by LSP

1. In Settings → Notifications → LSPs, uncheck SpaceX (ID: 121)
2. Send test notification with `lsp_id: "121"`
3. **Expected**: Notification should NOT appear

### Test Case 2: Filter by Location

1. In Settings → Notifications → Locations, uncheck Florida (ID: 27)
2. Send test notification with `location_id: "27"`
3. **Expected**: Notification should NOT appear

### Test Case 3: Webcast Only

1. In Settings → Notifications, enable "Webcast Only"
2. Send test notification with `webcast: "false"`
3. **Expected**: Notification should NOT appear

### Test Case 4: Strict Matching

1. In Settings → Notifications, enable "Strict Matching"
2. Check only SpaceX (121) in LSPs
3. Check only Florida (27) in Locations
4. Send notification with `lsp_id: "121"` and `location_id: "11"` (California)
5. **Expected**: Notification should NOT appear (both must match)

## Debugging

### Android Logcat

```bash
# Filter for notification logs
adb logcat | grep -E "(NotificationWorker|FCM|V5)"
```

**Expected output on success**:
```
D/NotificationWorker: Starting work...
D/NotificationWorker: Parsed V5 notification: Test Rocket
D/V5NotificationFilter: ✅ ALLOWED: All filters passed
D/NotificationWorker: Displaying notification - launchId: 99999
```

**Expected output on filter block**:
```
D/NotificationWorker: Starting work...
D/NotificationWorker: Parsed V5 notification: Test Rocket
D/V5NotificationFilter: 🔇 BLOCKED: LSP ID 121 not in subscribed set
I/NotificationWorker: Notification filtered out
```

### iOS Console

```bash
# View extension logs
log stream --predicate 'subsystem == "me.calebjones.spacelaunchnow.NotificationServiceExtension"'
```

**Expected output on success**:
```
📩 NSE: Received notification
✅ NSE: Filter passed, delivering notification
```

**Expected output on filter block**:
```
📩 NSE: Received notification
🔇 NSE: Filtered - LSP not subscribed
```

## Common Issues

### Issue: Notifications not received on Android

**Cause**: App not subscribed to V5 topic
**Fix**: Check logcat for subscription confirmation:
```
D/SubscriptionProcessor: ✅ Subscribed to: debug_v5_android
```

### Issue: iOS NSE not intercepting

**Cause**: Missing `mutable-content: 1` in APNs payload
**Fix**: Verify server sends correct APNs headers

### Issue: Filter not working

**Cause**: Preferences not persisted correctly
**Fix**: Clear app data and re-configure preferences

## Next Steps

1. Configure filter preferences in Settings
2. Test with real server notifications
3. Monitor notification history for debugging
4. Report any issues to the development team
