package me.calebjones.spacelaunchnow.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.storage.NotificationPreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.NotificationDisplayHelper
import org.koin.android.ext.android.inject

class SpaceLaunchFirebaseMessagingService : FirebaseMessagingService() {

    // Use Koin to inject the singleton NotificationPreferences
    private val notificationPreferences: NotificationPreferences by inject()

    override fun onCreate() {
        super.onCreate()
        // Create notification channel
        NotificationDisplayHelper.createNotificationChannel(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        println("=== FCM Message Received ===")
        println("From: ${remoteMessage.from}")
        println("MessageId: ${remoteMessage.messageId}")
        println("Data: ${remoteMessage.data}")

        // Get user notification settings
        val notificationSettings = runBlocking {
            notificationPreferences.getNotificationSettings()
        }

        // Parse notification data (v4 format)
        val notificationData = me.calebjones.spacelaunchnow.data.model.NotificationData.fromMap(remoteMessage.data)
        
        if (notificationData == null) {
            println("⚠️ Failed to parse notification data, showing notification anyway")
            // Fallback to showing notification with FCM notification payload
            remoteMessage.notification?.let { notification ->
                NotificationDisplayHelper.showNotificationFromMap(
                    context = this,
                    data = remoteMessage.data,
                    title = notification.title ?: "Space Launch Now",
                    body = notification.body ?: ""
                )
            }
            return
        }

        println("Parsed notification data: type=${notificationData.notificationType}, launch=${notificationData.launchName}, agency=${notificationData.agencyId}, location=${notificationData.locationId}")

        // Apply client-side filtering
        val shouldShow = me.calebjones.spacelaunchnow.data.model.NotificationFilter.shouldShowNotification(
            data = notificationData,
            state = notificationSettings
        )

        if (!shouldShow) {
            println("🔇 Notification filtered out by user preferences")
            return
        }

        println("✅ Notification passed filters, showing to user")

        // Show notification using the unified helper
        val title = remoteMessage.notification?.title ?: notificationData.launchName
        val body = remoteMessage.notification?.body ?: "Launch from ${notificationData.launchLocation}"
        
        NotificationDisplayHelper.showNotification(
            context = this,
            notificationData = notificationData,
            title = title,
            body = body
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send token to backend if needed
        println("New FCM token: $token")
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