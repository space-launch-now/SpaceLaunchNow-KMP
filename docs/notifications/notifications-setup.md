# Notifications Setup Guide

This guide explains how to set up and use push notifications in the Space Launch Now KMP app.

## Overview

The app supports Firebase Cloud Messaging (FCM) for push notifications across Android, iOS, and
Desktop platforms:

- **Android**: Full FCM integration with notification channels and permission handling
- **iOS**: FCM integration (requires setup)
- **Desktop**: No-op implementation (notifications not supported)

## Features

- **Launch Notifications**: Get notified about upcoming space launches
- **Daily Summary**: Receive a daily digest of upcoming launches
- **Topic-based Subscriptions**: Subscribe to notifications from specific providers (SpaceX, NASA,
  etc.)
- **Permissions**: Proper Android 13+ notification permission handling

## Architecture

### Core Components

1. **PushMessaging** (expect/actual): Platform-specific push messaging implementation
2. **NotificationRepository**: Manages notification settings and subscriptions
3. **SettingsViewModel**: Handles UI state for notification settings
4. **Firebase Messaging Service**: Android service for handling incoming messages

### Data Models

- `NotificationSettings`: User notification preferences
- `NotificationTopic`: Available notification topics
- `PushMessage`: Incoming push message structure

## Android Setup

### 1. Firebase Configuration

1. Create a Firebase project
   at [https://console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name: `me.calebjones.spacelaunchnow.kmp`
3. Download `google-services.json` and place it in `composeApp/` directory
4. The project already includes the necessary Firebase dependencies and Google Services plugin

### 2. Permissions

The app automatically requests notification permissions on Android 13+ when the user enables
notifications in settings.

Required permissions (already added to manifest):

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### 3. Testing Notifications

You can test notifications using the Firebase Console:

1. Go to Firebase Console → Cloud Messaging
2. Create a new campaign
3. Target your app using the topic name (e.g., `launches_all`)
4. Send a test message

## iOS Setup (TODO)

iOS implementation requires additional setup:

1. Add Firebase iOS SDK via Swift Package Manager
2. Add `GoogleService-Info.plist` to iOS target
3. Enable Push Notifications capability
4. Configure APNs in Firebase
5. Implement Swift bridge for FCM integration

## Usage

### In Settings Screen

Users can:

- Enable/disable notifications
- Toggle daily summary notifications
- Request notification permissions (Android 13+)

### Available Topics

- `launches_all`: All upcoming launches
- `launches_spacex`: SpaceX launches only
- `launches_nasa`: NASA launches only
- `daily_summary`: Daily digest notifications

### Programmatic Usage

```kotlin
// Subscribe to a topic
notificationRepository.subscribeToTopic(NotificationTopic.LAUNCHES_ALL)

// Update notification settings
val settings = NotificationSettings(
    enableNotifications = true,
    notifyDailySummary = true
)
notificationRepository.updateNotificationSettings(settings)
```

## Testing

### Local Testing

1. Enable notifications in the app settings
2. Use Firebase Console to send test messages to topics
3. Verify notifications appear on device

### Message Format

Example FCM payload:

```json
{
  "message": {
    "topic": "launches_all",
    "notification": {
      "title": "🚀 Launch Alert",
      "body": "Falcon 9 launching in 30 minutes"
    },
    "data": {
      "type": "launch",
      "launch_id": "123",
      "provider": "spacex"
    }
  }
}
```

## Troubleshooting

### Common Issues

1. **Notifications not received**
    - Check if notifications are enabled in app settings
    - Verify Firebase configuration
    - Check device notification settings

2. **Permission denied**
    - For Android 13+, ensure POST_NOTIFICATIONS permission is granted
    - Users can manually enable in system settings

3. **Build issues**
    - Ensure `google-services.json` is in the correct location
    - Verify Google Services plugin is applied
    - Check Firebase dependencies are properly configured

### Logs

Monitor FCM logs in Android Studio/Xcode for debugging:

- Token registration
- Message reception
- Topic subscription status

## Future Enhancements

- [ ] Launch reminder scheduling with WorkManager
- [ ] Rich notifications with images and actions
- [ ] Notification history
- [ ] More granular topic subscriptions
- [ ] iOS implementation completion
- [ ] Desktop notification support (where applicable)