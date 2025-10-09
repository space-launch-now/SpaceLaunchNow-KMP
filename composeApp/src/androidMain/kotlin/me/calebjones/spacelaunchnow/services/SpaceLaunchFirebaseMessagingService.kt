package me.calebjones.spacelaunchnow.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.MainActivity
import me.calebjones.spacelaunchnow.data.storage.NotificationPreferences
import org.koin.android.ext.android.inject

class SpaceLaunchFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "space_launch_notifications"
        private const val CHANNEL_NAME = "Space Launch Notifications"
        private const val NOTIFICATION_ID = 1
    }

    // Use Koin to inject the singleton NotificationPreferences
    private val notificationPreferences: NotificationPreferences by inject()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check notificationsEnabled, and return early if false
        val notificationSettings = runBlocking {
            notificationPreferences.getNotificationSettings()
        }

        if (!notificationSettings.enableNotifications) {
            println("Notifications are disabled by user preference. Suppressing notification.")
            return
        }

        // Check webcast filtering
        val webcastOnly =
            notificationSettings.isTopicEnabled(me.calebjones.spacelaunchnow.data.model.NotificationTopic.WEBCAST_ONLY)
        val hasWebcast = remoteMessage.data["webcast"]?.lowercase() == "true"

        println("webcast field: ${remoteMessage.data["webcast"]}, hasWebcast: $hasWebcast")
        if (webcastOnly && !hasWebcast) {
            println("Webcast-only filter enabled and launch has no webcast. Suppressing notification.")
            return
        }

        println("Received FCM message: ${remoteMessage.data}")
        println("Received FCM notification: ${remoteMessage.notification}")
        println("Received FCM data: ${remoteMessage.data}")
        println("Received FCM from: ${remoteMessage.from}")
        println("Received FCM messageId: ${remoteMessage.messageId}")
        println("Received FCM sentTime: ${remoteMessage.sentTime}")
        println("Received FCM ttl: ${remoteMessage.ttl}")
        println("Received FCM collapseKey: ${remoteMessage.collapseKey}")
        println("Received FCM messageType: ${remoteMessage.messageType}")
        println("Received FCM to: ${remoteMessage.to}")
        println("Received FCM originalPriority: ${remoteMessage.originalPriority}")
        println("Received FCM priority: ${remoteMessage.priority}")
        println("Received FCM notification.title: ${remoteMessage.notification?.title}")
        println("Received FCM notification.body: ${remoteMessage.notification?.body}")
        println("Received FCM notification.icon: ${remoteMessage.notification?.icon}")

        // Handle FCM message
        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "Space Launch Now",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }

        // Handle data-only messages
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send token to backend if needed
        println("New FCM token: $token")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming space launches"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        // Extract launch_id from the notification data
        val launchId = data["launch_id"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add launch_id if available for direct navigation to launch detail
            launchId?.let { putExtra("launch_id", it) }
            // Add any additional data from the notification
            data.forEach { (key, value) ->
                if (key != "launch_id") { // Avoid duplicate launch_id
                    putExtra(key, value)
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            launchId?.hashCode() ?: 0, // Use launch_id hash as unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(me.calebjones.spacelaunchnow.R.mipmap.ic_launcher_monochrome)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(launchId?.hashCode() ?: NOTIFICATION_ID, notification)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Handle data-only messages for in-app updates
        when (data["type"]) {
            "launch" -> {
                // Handle launch-specific data
                println("Received data-only launch message for launch_id: ${data["launch_id"]}")
                // Data-only messages are typically used for in-app updates rather than notifications
                // The main notification logic is handled in onMessageReceived
            }
        }
    }
}