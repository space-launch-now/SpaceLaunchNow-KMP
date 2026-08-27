package me.calebjones.spacelaunchnow.services

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.notifications.NotificationDisplayHelper
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.workers.NotificationWorker
import org.koin.mp.KoinPlatform

/**
 * Firebase Cloud Messaging service for handling push notifications
 *
 * IMPORTANT: This service delegates to WorkManager for actual notification processing
 * because FCM's onMessageReceived has a very short execution window (~10 seconds).
 * Image loading can take longer, so we use WorkManager to guarantee delivery.
 */
class SpaceLaunchFirebaseMessagingService : FirebaseMessagingService() {

    private val log = logger()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

        // WorkManager initializes on demand (its androidx.startup auto-initializer is
        // removed in the manifest, issue #181), so initialization failures land here.
        // runCatching, not try/catch (e: Exception): the reported failure mode is a
        // NoSuchMethodError, which is an Error. A device that cannot init WorkManager
        // loses this notification rather than crashing the process.
        runCatching {
            WorkManager.getInstance(this).enqueue(workRequest)
        }.onSuccess {
            log.i { "Notification processing delegated to WorkManager - workRequestId: ${workRequest.id}, messageId: ${remoteMessage.messageId}" }
        }.onFailure { throwable ->
            log.e(throwable) { "WorkManager unavailable - dropping FCM notification work - messageId: ${remoteMessage.messageId}" }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        log.i { "New FCM token generated (len=${token.length}, …${token.takeLast(6)})" }
        PushDiagnostics.recordTokenSuccess(token)

        // Topic subscriptions bind to the Firebase installation, not the token
        // (firebase-android-sdk#5824), so a refresh changes nothing about what
        // this device receives. Reconcile and nothing more -- never clear the
        // subscription table here: we can only unsubscribe from topics we can
        // name, and wiping the record would orphan every stale subscription.
        serviceScope.launch {
            runCatching {
                KoinPlatform.getKoin().get<NotificationRepository>().reconcileSubscriptions()
            }.onFailure {
                log.w(it) { "Reconcile after token refresh failed; app-start pass will retry" }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}