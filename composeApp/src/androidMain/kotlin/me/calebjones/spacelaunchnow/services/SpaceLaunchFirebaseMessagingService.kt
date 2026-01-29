package me.calebjones.spacelaunchnow.services

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.calebjones.spacelaunchnow.data.notifications.NotificationDisplayHelper
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.workers.NotificationWorker

/**
 * Firebase Cloud Messaging service for handling push notifications
 *
 * IMPORTANT: This service delegates to WorkManager for actual notification processing
 * because FCM's onMessageReceived has a very short execution window (~10 seconds).
 * Image loading can take longer, so we use WorkManager to guarantee delivery.
 */
class SpaceLaunchFirebaseMessagingService : FirebaseMessagingService() {

    private val log = logger()

    override fun onCreate() {
        super.onCreate()
        // Create notification channels
        NotificationDisplayHelper.createNotificationChannel(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        log.i { "FCM notification received - delegating to WorkManager - messageId: ${remoteMessage.messageId}, from: ${remoteMessage.from}, dataSize: ${remoteMessage.data.size}, hasNotificationPayload: ${remoteMessage.notification != null}" }
        log.v { "FCM data: ${remoteMessage.data}" }

        // CRITICAL: Delegate to WorkManager immediately to avoid execution timeout
        // FCM onMessageReceived has ~10 second window, but image loading can take longer
        // WorkManager guarantees execution even if app is killed
        val workData = Data.Builder()

        // Add all FCM data payload
        remoteMessage.data.forEach { (key, value) ->
            workData.putString(key, value)
        }

        // Add FCM notification payload if present (for title/body)
        remoteMessage.notification?.let { notification ->
            workData.putString("fcm_title", notification.title)
            workData.putString("fcm_body", notification.body)
        }

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workData.build())
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        log.i { "Notification processing delegated to WorkManager - workRequestId: ${workRequest.id}, messageId: ${remoteMessage.messageId}" }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        log.i { "New FCM token generated - length: ${token.length}, prefix: ${token.take(10)}" }

        // TODO: Send token to backend if needed
    }
}