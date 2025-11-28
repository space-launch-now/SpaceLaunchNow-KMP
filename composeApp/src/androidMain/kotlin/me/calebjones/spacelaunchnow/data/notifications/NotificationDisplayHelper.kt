package me.calebjones.spacelaunchnow.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.NotificationCompat
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.calebjones.spacelaunchnow.MainActivity
import me.calebjones.spacelaunchnow.R
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.model.SpaceLaunchNotificationChannel
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.util.LocaleUtil
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Helper class for displaying notifications with consistent formatting
 * Used by both Firebase messaging service and test notifications
 */
object NotificationDisplayHelper {

    // Legacy channel constants for backward compatibility
    @Deprecated("Use SpaceLaunchNotificationChannel enum instead")
    private const val CHANNEL_LAUNCHES_ID = "space_launch_notifications"

    @Deprecated("Use SpaceLaunchNotificationChannel enum instead")
    private const val CHANNEL_LAUNCHES_NAME = "Launch Notifications"

    @Deprecated("Use SpaceLaunchNotificationChannel enum instead")
    private const val CHANNEL_EVENTS_ID = "space_event_notifications"

    @Deprecated("Use SpaceLaunchNotificationChannel enum instead")
    private const val CHANNEL_EVENTS_NAME = "Event Notifications"

    @Deprecated("Use SpaceLaunchNotificationChannel enum instead")
    private const val CHANNEL_NEWS_ID = "space_news_notifications"

    @Deprecated("Use SpaceLaunchNotificationChannel enum instead")
    private const val CHANNEL_NEWS_NAME = "News & Updates"

    private const val NOTIFICATION_ID = 1

    /**
     * Get the notification channel ID for a specific NotificationTopic
     * This provides the mapping between topics and channels
     */
    fun getChannelForTopic(topic: NotificationTopic): String {
        return SpaceLaunchNotificationChannel.Companion.getChannelForTopic(topic).id
    }

    /**
     * Determine which notification channel to use based on notification type
     * Maps notification types to appropriate channels with correct importance levels
     */
    private fun getChannelId(notificationType: String): String {
        return when {
            // Critical/Imminent notifications (1-10 minutes)
            notificationType.contains("tenMinutes", ignoreCase = true) ||
                    notificationType.contains("oneMinute", ignoreCase = true) ->
                SpaceLaunchNotificationChannel.LAUNCH_IMMINENT.id

            // High priority status updates
            notificationType.contains("inFlight", ignoreCase = true) ||
                    notificationType.contains("success", ignoreCase = true) ||
                    notificationType.contains("failure", ignoreCase = true) ||
                    notificationType.contains("partial_failure", ignoreCase = true) ->
                SpaceLaunchNotificationChannel.LAUNCH_STATUS_UPDATES.id

            // High priority schedule changes
            notificationType.contains("netstampChanged", ignoreCase = true) ||
                    notificationType.contains("change", ignoreCase = true) ->
                SpaceLaunchNotificationChannel.SCHEDULE_CHANGES.id

            // Standard launch reminders (1-24 hours)
            notificationType.contains("twentyFourHour", ignoreCase = true) ||
                    notificationType.contains("oneHour", ignoreCase = true) ||
                    notificationType.contains("hour", ignoreCase = true) ->
                SpaceLaunchNotificationChannel.LAUNCH_REMINDERS.id

            // Webcast notifications
            notificationType.contains("webcastLive", ignoreCase = true) ||
                    notificationType.contains("webcastOnly", ignoreCase = true) ||
                    notificationType.contains("webcast", ignoreCase = true) ->
                SpaceLaunchNotificationChannel.WEBCAST_NOTIFICATIONS.id

            // Space events
            notificationType.equals("event", ignoreCase = true) ->
                SpaceLaunchNotificationChannel.SPACE_EVENTS.id

            // News and updates
            notificationType.equals("news", ignoreCase = true) ||
                    notificationType.contains("featured_news", ignoreCase = true) ->
                SpaceLaunchNotificationChannel.NEWS_UPDATES.id

            // Default fallback to standard launch reminders
            else -> SpaceLaunchNotificationChannel.LAUNCH_REMINDERS.id
        }
    }

    /**
     * Create all notification channels (Android O+)
     * Now creates granular channels for each notification type
     * Should be called once during app initialization
     */
    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create all granular notification channels
        SpaceLaunchNotificationChannel.values().forEach { channelConfig ->
            val channel = NotificationChannel(
                channelConfig.id,
                channelConfig.displayName,
                channelConfig.importance
            ).apply {
                description = channelConfig.description

                // Enable lights and vibration with patterns based on channel type
                when (channelConfig) {
                    SpaceLaunchNotificationChannel.LAUNCH_IMMINENT -> {
                        // Critical: Urgent, rapid pattern for imminent launches (1-10 minutes)
                        enableLights(true)
                        lightColor = Color.RED
                        enableVibration(true)
                        vibrationPattern =
                            longArrayOf(0, 200, 100, 200, 100, 200, 100, 400) // Urgent triple-buzz
                    }

                    SpaceLaunchNotificationChannel.LAUNCH_STATUS_UPDATES -> {
                        // High priority: Strong pattern for launch status (success/failure/in-flight)
                        enableLights(true)
                        lightColor = Color.BLUE
                        enableVibration(true)
                        vibrationPattern =
                            longArrayOf(0, 300, 200, 300, 200, 500) // Strong double-buzz
                    }

                    SpaceLaunchNotificationChannel.SCHEDULE_CHANGES -> {
                        // High priority: Distinctive pattern for schedule changes
                        enableLights(true)
                        lightColor = Color.YELLOW
                        enableVibration(true)
                        vibrationPattern =
                            longArrayOf(0, 100, 100, 100, 100, 100, 200, 400) // Triple tap + hold
                    }

                    SpaceLaunchNotificationChannel.LAUNCH_REMINDERS -> {
                        // Default: Standard notification pattern (1-24 hours)
                        enableLights(true)
                        lightColor = Color.BLUE
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 250, 250, 250) // Standard double-buzz
                    }

                    SpaceLaunchNotificationChannel.WEBCAST_NOTIFICATIONS -> {
                        // Default: Gentle pattern for webcast notifications
                        enableLights(true)
                        lightColor = Color.GREEN
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 200, 100, 200) // Gentle double-tap
                    }

                    else -> {
                        // Low priority: Simple single vibration for events/news
                        if (channelConfig.importance >= NotificationManager.IMPORTANCE_DEFAULT) {
                            enableLights(true)
                            lightColor = Color.BLUE
                            enableVibration(true)
                            vibrationPattern = longArrayOf(0, 250) // Simple single buzz
                        }
                    }
                }
            }

            notificationManager.createNotificationChannel(channel)
        }

        // Migration: Delete old channels if they exist
        migrateLegacyChannels(notificationManager)
    }

    /**
     * Remove old channels and migrate users to new granular channels
     */
    private fun migrateLegacyChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Delete old broad channels
                @Suppress("DEPRECATION")
                notificationManager.deleteNotificationChannel(CHANNEL_LAUNCHES_ID)
                @Suppress("DEPRECATION")
                notificationManager.deleteNotificationChannel(CHANNEL_EVENTS_ID)
                @Suppress("DEPRECATION")
                notificationManager.deleteNotificationChannel(CHANNEL_NEWS_ID)
            } catch (e: Exception) {
                // Channels may not exist, ignore
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use createNotificationChannels() instead
     */
    @Deprecated(
        "Use createNotificationChannels() instead",
        ReplaceWith("createNotificationChannels(context)")
    )
    fun createNotificationChannel(context: Context) {
        createNotificationChannels(context)
    }

    /**
     * Load image from URL as Bitmap using Coil
     * This method uses runBlocking to make it synchronous, suitable for notification contexts
     */
    private fun loadImageFromUrlSync(context: Context, imageUrl: String?): Bitmap? {
        println("🖼️ [ImageLoader] Starting image load...")
        println("🖼️ [ImageLoader] URL: $imageUrl")

        if (imageUrl.isNullOrBlank()) {
            println("🖼️ [ImageLoader] ❌ URL is null or blank, skipping image load")
            return null
        }

        return try {
            println("🖼️ [ImageLoader] Creating ImageLoader and request...")
            val bitmap = runBlocking(Dispatchers.IO) {
                println("🖼️ [ImageLoader] Running in IO dispatcher...")
                val imageLoader = ImageLoader(context)
                println("🖼️ [ImageLoader] ImageLoader created: $imageLoader")

                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(Size(512, 512)) // Limit size for notifications
                    .allowHardware(false) // Software bitmaps required for notifications
                    .build()
                println("🖼️ [ImageLoader] Request built: ${request.data}")

                println("🖼️ [ImageLoader] Executing request...")
                val result = imageLoader.execute(request)
                println("🖼️ [ImageLoader] Result received")
                println("🖼️ [ImageLoader] Result image: ${result.image}")
                println("🖼️ [ImageLoader] Result image type: ${result.image?.javaClass?.simpleName}")

                // Coil3 returns a BitmapImage, extract the bitmap using toBitmap()
                val bitmap = result.image?.toBitmap()
                if (bitmap != null) {
                    println("🖼️ [ImageLoader] ✅ Successfully extracted bitmap: ${bitmap.width}x${bitmap.height}")
                    bitmap
                } else {
                    println("🖼️ [ImageLoader] ❌ Failed to extract bitmap from image")
                    null
                }
            }

            if (bitmap != null) {
                println("🖼️ [ImageLoader] ✅ Final bitmap ready: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")
            } else {
                println("🖼️ [ImageLoader] ❌ Final bitmap is null")
            }

            bitmap
        } catch (e: Exception) {
            println("🖼️ [ImageLoader] ❌ Exception during image load: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Load image from URL as Bitmap using Coil (suspending version)
     * For use in coroutine contexts
     */
    private suspend fun loadImageFromUrl(context: Context, imageUrl: String?): Bitmap? {
        println("🖼️ [ImageLoader-Async] Starting async image load...")
        println("🖼️ [ImageLoader-Async] URL: $imageUrl")

        if (imageUrl.isNullOrBlank()) {
            println("🖼️ [ImageLoader-Async] ❌ URL is null or blank")
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                println("🖼️ [ImageLoader-Async] Creating ImageLoader...")
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(Size(512, 512)) // Limit size for notifications
                    .allowHardware(false) // Software bitmaps required for notifications
                    .build()
                println("🖼️ [ImageLoader-Async] Executing request...")

                val result = imageLoader.execute(request)
                println("🖼️ [ImageLoader-Async] Result: ${result.image}")

                // Coil3 returns a BitmapImage, extract the bitmap using toBitmap()
                val bitmap = result.image?.toBitmap()
                if (bitmap != null) {
                    println("🖼️ [ImageLoader-Async] ✅ Bitmap extracted: ${bitmap.width}x${bitmap.height}")
                    bitmap
                } else {
                    println("🖼️ [ImageLoader-Async] ❌ Failed to extract bitmap")
                    null
                }
            } catch (e: Exception) {
                println("🖼️ [ImageLoader-Async] ❌ Exception: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Draw a "🔴 LIVE" badge on the bitmap for webcasts
     * Creates a red badge with white text in the top-right corner
     */
    private fun drawLiveBadge(originalBitmap: Bitmap): Bitmap {
        println("🎨 [LiveBadge] Drawing LIVE badge on bitmap...")

        // Create a mutable copy of the bitmap
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Calculate badge dimensions (proportional to image size)
        val badgeWidth = (mutableBitmap.width * 0.25f).toInt() // 25% of image width
        val badgeHeight = (mutableBitmap.height * 0.08f).toInt() // 8% of image height
        val cornerRadius = badgeHeight * 0.3f
        val padding = (mutableBitmap.width * 0.02f).toInt() // 2% padding from edge

        // Position in top-right corner
        val left = mutableBitmap.width - badgeWidth - padding
        val top = padding
        val right = mutableBitmap.width - padding
        val bottom = top + badgeHeight

        // Draw red rounded rectangle background
        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#E31B23") // Bright red for LIVE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val badgeRect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, backgroundPaint)

        // Draw white border for contrast
        val borderPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, borderPaint)

        // Draw "🔴 LIVE" text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = badgeHeight * 0.5f // 50% of badge height
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val text = "🔴 LIVE"
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        // Center text in badge
        val textX = (left + right) / 2f
        val textY = (top + bottom) / 2f + textBounds.height() / 2f

        canvas.drawText(text, textX, textY, textPaint)

        println("🎨 [LiveBadge] ✅ LIVE badge drawn successfully")
        return mutableBitmap
    }

    /**
     * Convert ISO 8601 timestamp to pretty time format
     * Input: "2025-10-15T12:00:00Z"
     * Output: "12:00 PM" (en-US) or "12:00" (most other locales with 24-hour format)
     * Respects user's locale preference and UTC setting
     */
    private fun formatLaunchDate(context: Context, launchNet: String): String {
        return try {
            // Parse ISO 8601 format using ROOT locale to avoid locale-specific parsing issues
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = inputFormat.parse(launchNet)

            if (date == null) {
                println("⚠️ Failed to parse launch date: $launchNet")
                return launchNet
            }

            // Get user's locale preference
            val userLocale = Locale.forLanguageTag(LocaleUtil.getLocaleTag())

            // Create output formatter with user's locale
            val outputFormat = DateFormat.getTimeInstance(DateFormat.SHORT, userLocale)

            // Get user's UTC preference from AppPreferences
            // Note: This is a synchronous call in notification context, which is acceptable
            // since notifications are already processed in background threads
            val useUtc = try {
                runBlocking {
                    val dataStore = createDataStore(context)
                    val prefs = AppPreferences(dataStore)
                    prefs.getUseUtc()
                }
            } catch (e: Exception) {
                println("⚠️ Failed to get UTC preference: ${e.message}")
                false // Default to local time if preference can't be read
            }

            // Set timezone based on user preference
            if (useUtc) {
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            } else {
                // Use device's local timezone
                outputFormat.timeZone = TimeZone.getDefault()
            }

            val formattedTime = outputFormat.format(date)

            // Append UTC suffix if user prefers UTC display
            if (useUtc) "$formattedTime UTC" else formattedTime
        } catch (e: Exception) {
            println("⚠️ Failed to parse launch date: $launchNet - ${e.message}")
            launchNet // Return original if parsing fails
        }
    }

    /**
     * Get notification body message based on notification type
     */
    private fun getNotificationBody(
        context: Context,
        notificationType: String,
        launchNet: String,
        launchLocation: String
    ): String {
        val formattedDate = formatLaunchDate(context, launchNet)

        return when (notificationType.lowercase()) {
            "netstampchanged" -> context.getString(
                R.string.notification_schedule_changed,
                formattedDate
            )

            "success" -> context.getString(R.string.notification_launch_successful)
            "failure" -> context.getString(R.string.notification_launch_failure)
            "partialfailure" -> context.getString(R.string.notification_launch_partial_failure)
            "inflight" -> context.getString(R.string.notification_launch_liftoff)
            "oneminute" -> context.getString(R.string.notification_one_minute, formattedDate)
            "tenminutes" -> context.getString(R.string.notification_ten_minutes, formattedDate)
            "onehour" -> context.getString(R.string.notification_one_hour, formattedDate)
            "twentyfourhour" -> context.getString(
                R.string.notification_twenty_four_hours,
                formattedDate
            )

            else -> "Launch from $launchLocation" // Fallback
        }
    }

    /**
     * Display a notification using NotificationData
     * This is the unified method used by both Firebase and test notifications
     *
     * Note: This method loads images synchronously on the calling thread.
     * Consider calling from a background thread for better performance.
     */
    fun showNotification(
        context: Context,
        notificationData: NotificationData,
        title: String? = null
    ) {
        // Ensure channels exist
        createNotificationChannels(context)

        // Determine which channel to use
        val channelId = getChannelId(notificationData.notificationType)

        // Use provided title or generate from notification data
        // Add 🔴 emoji to title if webcast is available (like old app)
        val baseTitle = title ?: notificationData.launchName
        val displayTitle = if (notificationData.isWebcastLive()) {
            "🔴 $baseTitle"
        } else {
            baseTitle
        }

        // Always generate body with proper date formatting from NotificationData
        val displayBody = getNotificationBody(
            context,
            notificationData.notificationType,
            notificationData.launchNet,
            notificationData.launchLocation
        )

        // Create intent with launch data
        // Use launchUuid for navigation since API 2.4.0 requires UUID for launch details
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("launch_id", notificationData.launchUuid) // Use UUID for API 2.4.0
            putExtra("launch_uuid", notificationData.launchUuid)
            putExtra("launch_name", notificationData.launchName)
            putExtra("launch_net", notificationData.launchNet)
            putExtra("launch_location", notificationData.launchLocation)
            putExtra("webcast", notificationData.webcast)
            putExtra("agency_id", notificationData.agencyId)
            putExtra("location_id", notificationData.locationId)
            putExtra("notification_type", notificationData.notificationType)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationData.launchId.hashCode(), // Use launch_id hash as unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build base notification
        println("📱 [Notification] Building notification...")
        println("📱 [Notification] Title: $displayTitle")
        println("📱 [Notification] Body: $displayBody")
        println("📱 [Notification] Channel: $channelId")

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setSmallIcon(R.drawable.ic_rocket_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)

        // Load and display image if available using Coil
        println("📱 [Notification] Image URL from notificationData: ${notificationData.launchImage}")
        println("📱 [Notification] Webcast available: ${notificationData.webcast}")

        var imageBitmap = loadImageFromUrlSync(context, notificationData.launchImage)
        println("📱 [Notification] Image bitmap result: $imageBitmap")

        // Add LIVE badge if webcast is available
        if (imageBitmap != null && notificationData.isWebcastLive()) {
            println("📱 [Notification] 🔴 Adding LIVE badge (webcast available)")
            imageBitmap = drawLiveBadge(imageBitmap)
        }

        if (imageBitmap != null) {
            println("📱 [Notification] ✅ Setting large icon and BigPictureStyle")
            println("📱 [Notification] Bitmap size: ${imageBitmap.width}x${imageBitmap.height}")
            println("📱 [Notification] Bitmap config: ${imageBitmap.config}")

            notificationBuilder
                .setLargeIcon(imageBitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(imageBitmap)
                        .bigLargeIcon(null as Bitmap?) // Hide large icon when expanded
                )
            println("📱 [Notification] BigPictureStyle applied")
        } else {
            println("📱 [Notification] ❌ No image bitmap, using BigTextStyle fallback")
            // Use BigTextStyle if no image available
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(displayBody)
            )
        }

        println("📱 [Notification] Building final notification...")
        val notification = notificationBuilder.build()
        println("📱 [Notification] Notification built successfully")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        println("📱 [Notification] NotificationManager obtained")

        // Use launchId as the notification tag (collapse key)
        // This ensures multiple notifications for the same launch replace each other
        println("📱 [Notification] Showing notification with:")
        println("📱 [Notification]   - Tag: ${notificationData.launchId}")
        println("📱 [Notification]   - ID: ${notificationData.launchId.hashCode()}")

        notificationManager.notify(
            notificationData.launchId, // Tag for collapsing notifications
            notificationData.launchId.hashCode(), // Notification ID
            notification
        )
        println("📱 [Notification] ✅ Notification shown successfully!")
    }

    /**
     * Display a notification from raw data map (used by Firebase)
     * This converts the map to NotificationData and calls showNotification
     * Note: body parameter is only used for fallback when parsing fails
     */
    fun showNotificationFromMap(
        context: Context,
        data: Map<String, String>,
        title: String? = null,
        body: String? = null
    ) {
        val notificationData = NotificationData.Companion.fromMap(data)
        if (notificationData != null) {
            // Don't pass body - always use formatted body from NotificationData
            showNotification(context, notificationData, title)
        } else {
            // Fallback: show basic notification if parsing fails
            showBasicNotification(
                context,
                title ?: "Space Launch Now",
                body ?: "New launch notification"
            )
        }
    }

    /**
     * Fallback method for showing a basic notification without NotificationData
     */
    private fun showBasicNotification(
        context: Context,
        title: String,
        body: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        @Suppress("DEPRECATION")
        val notification =
            NotificationCompat.Builder(context, SpaceLaunchNotificationChannel.LAUNCH_REMINDERS.id)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_rocket_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}