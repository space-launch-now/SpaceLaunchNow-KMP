# Notification Display System - Image Support & Channels

## Overview
Updated `NotificationDisplayHelper` to support:
1. **Three separate notification channels** (Launches, Events, News)
2. **Image loading and display** in notifications using BigPictureStyle
3. **Automatic channel selection** based on notification type

## Notification Channels

### 1. Launch Notifications (`space_launch_notifications`)
- **Name**: "Launch Notifications"
- **Importance**: Default
- **Description**: "Notifications for upcoming space launches and launch updates"
- **Used for**: All launch-related notifications (oneHour, tenMinutes, success, etc.)

### 2. Event Notifications (`space_event_notifications`)
- **Name**: "Event Notifications"
- **Importance**: Default
- **Description**: "Notifications for space-related events"
- **Used for**: Notifications with type = "event"

### 3. News Notifications (`space_news_notifications`)
- **Name**: "News & Updates"
- **Importance**: Low (less intrusive)
- **Description**: "News and updates about space launches and missions"
- **Used for**: Notifications with type = "news"

## Channel Selection Logic

```kotlin
private fun getChannelId(notificationType: String): String {
    return when {
        notificationType.equals("event", ignoreCase = true) -> CHANNEL_EVENTS_ID
        notificationType.equals("news", ignoreCase = true) -> CHANNEL_NEWS_ID
        else -> CHANNEL_LAUNCHES_ID // Default to launches
    }
}
```

## Image Display

### BigPictureStyle Implementation

When a notification includes a `launchImage` URL:
1. Image is loaded synchronously from the URL
2. Displayed as:
   - **Large icon** when notification is collapsed
   - **Big picture** when notification is expanded (full width)
   - Large icon hidden when expanded to show more picture

### Fallback Behavior

If no image or image loading fails:
- Uses **BigTextStyle** instead
- Shows full notification body text when expanded

### Image Loading

```kotlin
// Synchronous image loading (on calling thread)
private fun loadImageFromUrlSync(imageUrl: String?): Bitmap? {
    if (imageUrl.isNullOrBlank()) return null
    
    return try {
        val url = URL(imageUrl)
        val connection = url.openConnection()
        connection.doInput = true
        connection.connect()
        val input = connection.getInputStream()
        BitmapFactory.decodeStream(input)
    } catch (e: Exception) {
        println("⚠️ Failed to load notification image from $imageUrl: ${e.message}")
        null
    }
}
```

**Note**: Currently synchronous. Consider calling `showNotification()` from a background thread for better performance with large images.

### Asynchronous Version (Available but not used)

```kotlin
// Suspending function for coroutine contexts
private suspend fun loadImageFromUrl(imageUrl: String?): Bitmap? = 
    withContext(Dispatchers.IO) { /* ... */ }
```

## Notification Building

### With Image

```kotlin
NotificationCompat.Builder(context, channelId)
    .setLargeIcon(imageBitmap)
    .setStyle(
        NotificationCompat.BigPictureStyle()
            .bigPicture(imageBitmap)
            .bigLargeIcon(null) // Hide large icon when expanded
    )
```

### Without Image

```kotlin
NotificationCompat.Builder(context, channelId)
    .setStyle(
        NotificationCompat.BigTextStyle()
            .bigText(displayBody)
    )
```

## Example Notification Data

### Launch Notification with Image

```kotlin
NotificationData(
    notificationType = "oneHour",
    launchId = "abc-123",
    launchUuid = "uuid-456",
    launchName = "Falcon 9 | Starlink",
    launchImage = "https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/starship_on_the_image_20250111100520.jpg",
    launchNet = "2025-10-15T12:00:00Z",
    launchLocation = "Kennedy Space Center",
    webcast = "true",
    agencyId = "121",
    locationId = "27"
)
```

**Result**:
- Channel: Launch Notifications
- Large icon: Starship image (collapsed view)
- Big picture: Full-width Starship image (expanded view)

### Event Notification

```kotlin
NotificationData(
    notificationType = "event",
    // ... other fields
)
```

**Result**:
- Channel: Event Notifications
- Can include image if `launchImage` is provided

### News Notification

```kotlin
NotificationData(
    notificationType = "news",
    // ... other fields
)
```

**Result**:
- Channel: News & Updates (Low importance)
- Less intrusive, won't make sound on some devices

## User Benefits

### Separate Channels Allow Users To:
1. **Customize notification sounds** per channel
2. **Set different importance levels** (Launch alerts vs News)
3. **Disable specific types** (e.g., keep launches, hide news)
4. **Control vibration patterns** per channel

### Image Display Benefits:
1. **Visual identification** - Instantly recognize rocket/mission
2. **Enhanced engagement** - More appealing notifications
3. **Better context** - See what the launch looks like
4. **Professional appearance** - Matches modern app standards

## Migration Notes

### Breaking Changes
- `createNotificationChannel()` → `createNotificationChannels()` (plural)
- Old method deprecated but still works (calls new method)

### Existing Notifications
- Old notifications will continue working
- Channels are created on app startup in `MainApplication.onCreate()`
- Channels also created lazily when showing notifications

## Testing

### Test Notification Form
The debug settings screen now allows testing with custom images:

```kotlin
// Default test image
launchImage = "https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/starship_on_the_image_20250111100520.jpg"
```

Test different scenarios:
1. ✅ Valid image URL → Shows big picture
2. ✅ Invalid/blank URL → Shows big text instead
3. ✅ Different notification types → Routes to correct channel

## Performance Considerations

### Image Loading
- **Synchronous loading** blocks calling thread
- **Recommendation**: Call from background thread or service
- **Future**: Consider using Coil/Glide for:
  - Caching
  - Placeholder images
  - Better error handling
  - Async loading with callbacks

### Memory
- Images are loaded as Bitmaps (can be large)
- Android handles notification bitmap lifecycle
- Consider max image size limits in production

## Future Enhancements

### Potential Improvements
1. **Action buttons**: "View Launch", "Share", "Set Reminder"
2. **Custom notification sounds** per channel
3. **Image caching** to avoid repeated downloads
4. **Placeholder images** while loading
5. **Progress indicators** for in-flight launches
6. **Group notifications** for multiple launches
7. **Direct reply** for community features

### Image Loading Library
Consider integrating Coil:

```kotlin
// With Coil (future)
val bitmap = context.imageLoader.execute(
    ImageRequest.Builder(context)
        .data(notificationData.launchImage)
        .size(512, 512)
        .build()
).drawable?.toBitmap()
```

## Related Files

- `NotificationDisplayHelper.kt` - Main implementation
- `NotificationData.kt` - Data model with `launchImage` field
- `SpaceLaunchFirebaseMessagingService.kt` - FCM integration
- `TestNotificationHelper.kt` - Debug testing
- `DebugSettingsScreen.kt` - Test UI with image URL field
- `MainApplication.kt` - Channel initialization on startup

## Firebase Setup

For Firebase to send images, the FCM payload should include:

```json
{
  "data": {
    "notificationType": "oneHour",
    "launchId": "abc-123",
    "launchUuid": "uuid-456",
    "launchName": "Falcon 9 | Starlink",
    "launchImage": "https://example.com/image.jpg",
    "launchNet": "2025-10-15T12:00:00Z",
    "launchLocation": "Kennedy Space Center",
    "webcast": "true",
    "agencyId": "121",
    "locationId": "27"
  }
}
```

The `launchImage` field will be automatically loaded and displayed.
