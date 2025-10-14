# Notification Image Loading with Coil

## Problem
Manual image loading using `URL` and `BitmapFactory` was failing silently with network issues:
```
⚠️ Failed to load notification image from https://...jpg: null
```

## Solution
Replaced manual image loading with **Coil3** image library for robust, cached image loading.

## Changes Made

### 1. Updated Imports
```kotlin
import android.graphics.drawable.BitmapDrawable
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
```

### 2. Replaced Image Loading Functions

#### Synchronous Loading (for notification contexts)
```kotlin
private fun loadImageFromUrlSync(context: Context, imageUrl: String?): Bitmap? {
    if (imageUrl.isNullOrBlank()) return null

    return try {
        runBlocking(Dispatchers.IO) {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(Size(512, 512)) // Limit size for notifications
                .allowHardware(false) // Software bitmaps required for notifications
                .build()

            val result = imageLoader.execute(request)
            (result.image as? BitmapDrawable)?.bitmap
        }
    } catch (e: Exception) {
        println("⚠️ Failed to load notification image from $imageUrl: ${e.message}")
        e.printStackTrace()
        null
    }
}
```

#### Asynchronous Loading (for coroutine contexts)
```kotlin
private suspend fun loadImageFromUrl(context: Context, imageUrl: String?): Bitmap? {
    if (imageUrl.isNullOrBlank()) return null

    return withContext(Dispatchers.IO) {
        try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(Size(512, 512))
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)
            (result.image as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            println("⚠️ Failed to load notification image from $imageUrl: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
```

### 3. Updated Function Call
```kotlin
// OLD: loadImageFromUrlSync(notificationData.launchImage)
// NEW: loadImageFromUrlSync(context, notificationData.launchImage)
val imageBitmap = loadImageFromUrlSync(context, notificationData.launchImage)
```

## Benefits of Coil

### 1. **Automatic Network Handling**
- Built-in HTTP client (uses Ktor)
- Proper error handling
- Retry logic
- Connection pooling

### 2. **Image Caching**
- **Memory cache**: Fast repeated access
- **Disk cache**: Reduces network usage
- **Automatic cache management**: No manual cleanup needed

### 3. **Performance Optimizations**
- **Size limiting**: `Size(512, 512)` ensures notifications don't load huge images
- **Hardware bitmap control**: `allowHardware(false)` ensures compatibility with notifications
- **Format handling**: Supports JPEG, PNG, WebP, GIF, etc.

### 4. **Better Error Handling**
- Detailed exception messages
- Stack traces for debugging
- Graceful fallback to null

### 5. **Kotlin Coroutines Support**
- Suspending functions for async loading
- `runBlocking` for synchronous contexts
- `withContext(Dispatchers.IO)` for proper threading

## Configuration

### Size Limiting
```kotlin
.size(Size(512, 512))
```
- Notifications don't need full-resolution images
- 512x512 is sufficient for notification large icon and big picture
- Reduces memory usage and loading time

### Hardware Bitmap Disabled
```kotlin
.allowHardware(false)
```
- Android notifications require software bitmaps
- Hardware bitmaps can't be used in notifications
- This ensures compatibility

## Usage in Notifications

```kotlin
val imageBitmap = loadImageFromUrlSync(context, notificationData.launchImage)
if (imageBitmap != null) {
    notificationBuilder
        .setLargeIcon(imageBitmap)
        .setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(imageBitmap)
                .bigLargeIcon(null as Bitmap?)
        )
} else {
    // Fallback to BigTextStyle
    notificationBuilder.setStyle(
        NotificationCompat.BigTextStyle()
            .bigText(displayBody)
    )
}
```

## Testing

### Test Image URL
```
https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/starship_on_the_image_20250111100520.jpg
```

### Expected Behavior
- ✅ Image loads successfully
- ✅ Displayed in notification as large icon (collapsed)
- ✅ Displayed as big picture (expanded)
- ✅ Cached for future notifications
- ✅ Falls back to text-only if loading fails

## Coil Dependencies

Already included in `gradle/libs.versions.toml`:

```toml
[versions]
coilCompose = "3.3.0"

[libraries]
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coilCompose" }
coil-compose-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coilCompose" }
coil-compose-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
```

Included in `composeApp/build.gradle.kts`:

```kotlin
commonMain {
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.ktor)
}

androidMain {
    implementation(libs.coil.compose.android)
}
```

## Cache Behavior

### Memory Cache
- Stores decoded bitmaps in memory
- Fast access for repeated notifications
- Automatically cleared when memory is low

### Disk Cache
- Stores downloaded images on disk
- Persists across app restarts
- Default location: `context.cacheDir/image_cache/`

### Cache Size
- Memory: ~10-20% of available heap
- Disk: ~250MB default (configurable)

## Future Enhancements

### Custom ImageLoader Configuration
```kotlin
val imageLoader = ImageLoader.Builder(context)
    .memoryCachePolicy(CachePolicy.ENABLED)
    .diskCachePolicy(CachePolicy.ENABLED)
    .crossfade(true)
    .logger(DebugLogger())
    .build()
```

### Placeholder Images
```kotlin
.placeholder(R.drawable.placeholder_rocket)
.error(R.drawable.error_image)
```

### Transformations
```kotlin
.transformations(CircleCropTransformation())
.transformations(RoundedCornersTransformation(16f))
```

## Debugging

### Enable Coil Logging
```kotlin
val imageLoader = ImageLoader.Builder(context)
    .logger(DebugLogger())
    .build()
```

### Check Cache Status
```kotlin
val memoryCache = imageLoader.memoryCache
val diskCache = imageLoader.diskCache
println("Memory cache size: ${memoryCache?.size}")
println("Disk cache size: ${diskCache?.size}")
```

## Performance Metrics

| Metric | Manual Loading | Coil |
|--------|---------------|------|
| **Network handling** | Basic `URLConnection` | Ktor with pooling |
| **Caching** | None | Memory + Disk |
| **Size optimization** | Full image | Configurable resize |
| **Format support** | JPEG, PNG | JPEG, PNG, WebP, GIF, etc. |
| **Error handling** | Basic try-catch | Detailed exceptions |
| **Threading** | Manual | Automatic with coroutines |

## Related Files

- `NotificationDisplayHelper.kt` - Image loading implementation
- `gradle/libs.versions.toml` - Coil dependencies
- `composeApp/build.gradle.kts` - Dependency configuration

## References

- [Coil Documentation](https://coil-kt.github.io/coil/)
- [Coil3 Migration Guide](https://coil-kt.github.io/coil/upgrading_to_coil3/)
- [Android Notification Best Practices](https://developer.android.com/develop/ui/views/notifications)
