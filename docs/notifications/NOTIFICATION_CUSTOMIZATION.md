# Notification Customization Guide

## Architecture Overview

The notification system now uses a **unified notification display helper** that ensures test notifications and Firebase notifications look and behave identically.

### Key Components

1. **NotificationDisplayHelper.kt** (androidMain)
   - Single source of truth for notification display
   - Used by both Firebase messaging service and test notifications
   - Centralizes all notification customization

2. **SpaceLaunchFirebaseMessagingService.kt**
   - Receives FCM messages
   - Applies client-side filtering via `NotificationFilter`
   - Delegates to `NotificationDisplayHelper` for display

3. **TestNotificationHelper.kt** (expect/actual)
   - Test notification trigger for debugging
   - Delegates to `NotificationDisplayHelper` for display
   - Ensures test notifications match real FCM notifications

## Notification Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Notification Sources                      │
├──────────────────────────┬──────────────────────────────────┤
│  Firebase (FCM)          │  Test Notification (Debug)       │
│  ↓                       │  ↓                               │
│  Parse NotificationData  │  Create NotificationData         │
│  ↓                       │  ↓                               │
│  Apply Filters           │  Apply Filters                   │
│  (NotificationFilter)    │  (NotificationFilter)            │
└──────────────────────────┴──────────────────────────────────┘
                           ↓
                ┌──────────────────────┐
                │ NotificationFilter   │
                │ shouldShowNotification│
                └──────────────────────┘
                           ↓
                    ┌─────────────┐
                    │  ALLOWED?   │
                    └─────────────┘
                     ↓           ↓
                   YES          NO
                    ↓            ↓
    ┌───────────────────────┐  (suppress)
    │ NotificationDisplayHelper │
    │   .showNotification()     │
    └───────────────────────┘
                ↓
    ┌───────────────────────┐
    │  Android Notification │
    │  - Title              │
    │  - Body               │
    │  - Icon               │
    │  - Actions (TODO)     │
    │  - Image (TODO)       │
    └───────────────────────┘
```

## Current Implementation

### Basic Notification (Current)

```kotlin
NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle(displayTitle)
    .setContentText(displayBody)
    .setSmallIcon(R.mipmap.ic_launcher_monochrome)
    .setContentIntent(pendingIntent)
    .setAutoCancel(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .build()
```

## Customization TODO List

All customizations should be added to `NotificationDisplayHelper.kt` in the `showNotification()` method.

### 1. Add Rich Text Style ✅ (Ready to implement)

```kotlin
.setStyle(
    NotificationCompat.BigTextStyle()
        .bigText("$displayBody\n\n" +
                "📍 ${notificationData.launchLocation}\n" +
                "🚀 ${notificationData.launchNet}\n" +
                "📡 Webcast: ${if (notificationData.hasWebcast()) "Available" else "Not Available"}")
)
```

### 2. Add Large Icon/Image 🖼️ (Requires image loading)

**Steps:**
1. Add Coil or another image loading library
2. Load image from `notificationData.launchImage` URL
3. Convert to Bitmap
4. Set as large icon

```kotlin
// Pseudo-code (needs implementation)
val bitmap = loadImageFromUrl(notificationData.launchImage)
.setLargeIcon(bitmap)
```

**Dependencies needed:**
```kotlin
// In libs.versions.toml
coil = "2.5.0"

// In build.gradle.kts
implementation("io.coil-kt:coil:2.5.0")
implementation("io.coil-kt:coil-compose:2.5.0")
```

### 3. Add Action Buttons 🔘 (Ready to implement)

```kotlin
// View Details button
val viewDetailsIntent = Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    putExtra("launch_id", notificationData.launchId)
    action = "VIEW_DETAILS"
}
val viewDetailsPendingIntent = PendingIntent.getActivity(
    context,
    notificationData.launchId.hashCode() + 1,
    viewDetailsIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

// Share button
val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, "Check out this launch: ${notificationData.launchName}")
}
val sharePendingIntent = PendingIntent.getActivity(
    context,
    notificationData.launchId.hashCode() + 2,
    Intent.createChooser(shareIntent, "Share launch"),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

// Add to notification builder
.addAction(
    R.drawable.ic_launch, // Need to create icon
    "View Details",
    viewDetailsPendingIntent
)
.addAction(
    R.drawable.ic_share, // Need to create icon
    "Share",
    sharePendingIntent
)
```

### 4. Add Notification Sound & Vibration 🔊 (Ready to implement)

```kotlin
.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
.setVibrate(longArrayOf(0, 500, 200, 500)) // Pattern: wait, vibrate, wait, vibrate
```

### 5. Add Progress Indicator (for countdown notifications) ⏱️

For "in-flight" or countdown notifications:

```kotlin
// Calculate progress based on time until launch
val progress = calculateProgress(notificationData.launchNet)
.setProgress(100, progress, false)
.setOngoing(true) // Keep notification visible during countdown
```

### 6. Add Custom Color/Theming 🎨

```kotlin
.setColor(context.getColor(R.color.primary))
.setColorized(true)
```

### 7. Add Group/Summary for Multiple Launches 📦

When multiple launches are happening:

```kotlin
.setGroup("launches")
.setGroupSummary(false)

// Separate summary notification
NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle("Multiple Launches")
    .setContentText("${launchCount} upcoming launches")
    .setGroup("launches")
    .setGroupSummary(true)
    .build()
```

## Testing Workflow

### Current Test Flow

1. Open Debug Settings → Test Notifications
2. Configure: agencyId, locationId, webcast, notificationType
3. Click "Send Test Notification"
4. **Notification goes through filter** (just like FCM)
5. If **ALLOWED**: Shows notification using `NotificationDisplayHelper`
6. If **BLOCKED**: Reports why it was blocked

### What This Enables

✅ Test notification appearance matches FCM exactly
✅ Test action buttons before deploying to production
✅ Test image loading and rendering
✅ Test notification grouping
✅ Test sound/vibration patterns
✅ Test on different Android versions

## Implementation Priority

### Phase 1: Basic Enhancements (Easy)
1. ✅ Add BigTextStyle with launch details
2. ✅ Add notification sound/vibration
3. ✅ Add color theming

### Phase 2: Actions (Medium)
4. 🔘 Add "View Details" action button
5. 🔘 Add "Share" action button
6. 🔘 Add "Remind Me" action (snooze)

### Phase 3: Rich Media (Complex)
7. 🖼️ Add image loading (Coil)
8. 🖼️ Display large icon from launch image
9. 🖼️ Add BigPictureStyle for expanded notifications

### Phase 4: Advanced Features (Optional)
10. ⏱️ Add progress indicator for countdowns
11. 📦 Add notification grouping for multiple launches
12. 🎯 Add direct reply action (quick notes)

## Code Location

**All customization happens in one place:**
```
composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NotificationDisplayHelper.kt
```

Look for the TODO comments in the `showNotification()` method:
```kotlin
// TODO: Add more customization here:
// .setStyle(NotificationCompat.BigTextStyle().bigText(displayBody))
// .setLargeIcon(bitmap) // Load from notificationData.launchImage
// .addAction(R.drawable.ic_launch, "View Details", pendingIntent)
// .addAction(R.drawable.ic_share, "Share", sharePendingIntent)
```

## Benefits of Unified Helper

✅ **Single source of truth** - All notifications look identical
✅ **Easy testing** - Test notifications = production notifications
✅ **Maintainable** - Change once, affects both FCM and test
✅ **Debuggable** - Test without Firebase backend
✅ **Rapid iteration** - See changes instantly in test notifications

## Next Steps

1. Implement BigTextStyle (5 minutes)
2. Add action buttons (30 minutes)
3. Integrate Coil for images (1 hour)
4. Test all variations using Debug Settings
5. Deploy to Firebase and verify FCM notifications match

## Resources

- [Android Notification API Guide](https://developer.android.com/develop/ui/views/notifications)
- [NotificationCompat.Builder](https://developer.android.com/reference/androidx/core/app/NotificationCompat.Builder)
- [Notification Styles](https://developer.android.com/develop/ui/views/notifications/expanded)
- [Coil Image Loading](https://coil-kt.github.io/coil/)
