package me.calebjones.spacelaunchnow.ui.viewmodel

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
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Helper class for displaying notifications with consistent formatting
 * Used by both Firebase messaging service and test notifications
 */
object NotificationDisplayHelper {

    // Notification Channels
    private const val CHANNEL_LAUNCHES_ID = "space_launch_notifications"
    private const val CHANNEL_LAUNCHES_NAME = "Launch Notifications"

    private const val CHANNEL_EVENTS_ID = "space_event_notifications"
    private const val CHANNEL_EVENTS_NAME = "Event Notifications"

    private const val CHANNEL_NEWS_ID = "space_news_notifications"
    private const val CHANNEL_NEWS_NAME = "News & Updates"

    private const val NOTIFICATION_ID = 1

    /**
     * Determine which notification channel to use based on notification type
     */
    private fun getChannelId(notificationType: String): String {
        return when {
            notificationType.equals("event", ignoreCase = true) -> CHANNEL_EVENTS_ID
            notificationType.equals("news", ignoreCase = true) -> CHANNEL_NEWS_ID
            else -> CHANNEL_LAUNCHES_ID // Default to launches channel
        }
    }

    /**
     * Create all notification channels (Android O+)
     * Should be called once during app initialization
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Launch Notifications Channel
            val launchChannel = NotificationChannel(
                CHANNEL_LAUNCHES_ID,
                CHANNEL_LAUNCHES_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming space launches and launch updates"
            }

            // Event Notifications Channel
            val eventChannel = NotificationChannel(
                CHANNEL_EVENTS_ID,
                CHANNEL_EVENTS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for space-related events"
            }

            // News Notifications Channel
            val newsChannel = NotificationChannel(
                CHANNEL_NEWS_ID,
                CHANNEL_NEWS_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "News and updates about space launches and missions"
            }

            notificationManager.createNotificationChannel(launchChannel)
            notificationManager.createNotificationChannel(eventChannel)
            notificationManager.createNotificationChannel(newsChannel)
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
     * Convert ISO 8601 timestamp to pretty date format
     * Input: "2025-10-15T12:00:00Z"
     * Output: "Oct 15, 2025 at 12:00 PM" (localized)
     */
    private fun formatLaunchDate(launchNet: String): String {
        return try {
            // Parse ISO 8601 format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = inputFormat.parse(launchNet)

            // Format to pretty date (user's local timezone)
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: launchNet
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
        val formattedDate = formatLaunchDate(launchNet)

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
        title: String? = null,
        body: String? = null
    ) {
        // Ensure channels exist
        createNotificationChannels(context)

        // Determine which channel to use
        val channelId = getChannelId(notificationData.notificationType)

        // Use provided title/body or generate from notification data and strings
        // Add 🔴 emoji to title if webcast is available (like old app)
        val baseTitle = title ?: notificationData.launchName
        val displayTitle = if (notificationData.webcast.equals("true", ignoreCase = true)) {
            "🔴 $baseTitle"
        } else {
            baseTitle
        }
        
        val displayBody = getNotificationBody(
            context,
            notificationData.notificationType,
            notificationData.launchNet,
            notificationData.launchLocation
        )

        // Create intent with launch data
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("launch_id", notificationData.launchId)
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
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Load and display image if available using Coil
        println("📱 [Notification] Image URL from notificationData: ${notificationData.launchImage}")
        println("📱 [Notification] Webcast available: ${notificationData.webcast}")
        
        var imageBitmap = loadImageFromUrlSync(context, notificationData.launchImage)
        println("📱 [Notification] Image bitmap result: $imageBitmap")
        
        // Add LIVE badge if webcast is available
        if (imageBitmap != null && notificationData.webcast.equals("true", ignoreCase = true)) {
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
     */
    fun showNotificationFromMap(
        context: Context,
        data: Map<String, String>,
        title: String? = null,
        body: String? = null
    ) {
        val notificationData = NotificationData.fromMap(data)
        if (notificationData != null) {
            showNotification(context, notificationData, title, body)
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

        val notification = NotificationCompat.Builder(context, CHANNEL_LAUNCHES_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
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