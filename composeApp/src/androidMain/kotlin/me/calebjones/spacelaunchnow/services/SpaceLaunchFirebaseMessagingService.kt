package me.calebjones.spacelaunchnow.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.calebjones.spacelaunchnow.MainActivity
import me.calebjones.spacelaunchnow.R

class SpaceLaunchFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "space_launch_notifications"
        private const val CHANNEL_NAME = "Space Launch Notifications"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

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
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add any data from the notification
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with app icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Handle data-only messages for in-app updates
        when (data["type"]) {
            "launch" -> {
                // Handle launch-specific data
                val launchId = data["launch_id"]
                // TODO: Update launch data or trigger in-app refresh
            }

            "daily_summary" -> {
                // Handle daily summary data
                // TODO: Update daily summary data
            }
        }
    }
}