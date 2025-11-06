package me.calebjones.spacelaunchnow.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.notifications.NotificationDisplayHelper
import me.calebjones.spacelaunchnow.data.storage.NotificationPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker to handle FCM notifications in background
 * This ensures notifications are processed even if image loading takes a long time
 *
 * Why WorkManager?
 * - FCM onMessageReceived has ~10 second execution window
 * - Image loading can take longer, causing notification to be lost
 * - WorkManager guarantees execution even if app is killed
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val notificationPreferences: NotificationPreferences by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            println("[NotificationWorker] Starting work...")
            DatadogLogger.debug("NotificationWorker started")

            // Extract notification data from input
            val notificationDataMap = inputData.keyValueMap.mapValues { it.value.toString() }
            val notificationData = NotificationData.fromMap(notificationDataMap)

            if (notificationData == null) {
                println("⚠️ [NotificationWorker] Failed to parse notification data")
                DatadogLogger.warn(
                    "NotificationWorker: Failed to parse notification data", mapOf(
                        "dataKeys" to notificationDataMap.keys.joinToString(",")
                    )
                )
                return@withContext Result.failure()
            }

            println("🔧 [NotificationWorker] Parsed notification: ${notificationData.launchName}")

            // Get user settings
            val settings = notificationPreferences.getNotificationSettings()

            // Apply client-side filtering
            val shouldShow = NotificationFilter.shouldShowNotification(
                data = notificationData,
                state = settings
            )

            if (!shouldShow) {
                println("[NotificationWorker] Notification filtered out")
                DatadogLogger.info(
                    "NotificationWorker: Notification filtered out", mapOf(
                        "launchId" to notificationData.launchId,
                        "launchName" to notificationData.launchName,
                        "agencyId" to notificationData.agencyId,
                        "locationId" to notificationData.locationId
                    )
                )
                return@withContext Result.success()
            }

            // Show notification (image loading happens here, but we have time)
            // Use fcm_title if provided, otherwise fall back to launch name
            val title = inputData.getString("fcm_title") ?: notificationData.launchName

            println("[NotificationWorker] Showing notification")
            DatadogLogger.info(
                "NotificationWorker: Displaying notification", mapOf(
                    "launchId" to notificationData.launchId,
                    "launchName" to notificationData.launchName
                )
            )

            // NotificationDisplayHelper.showNotification always generates formatted body from NotificationData
            NotificationDisplayHelper.showNotification(
                context = applicationContext,
                notificationData = notificationData,
                title = title
            )

            Result.success()
        } catch (e: Exception) {
            println("❌ [NotificationWorker] Error: ${e.message}")
            e.printStackTrace()
            DatadogLogger.error(
                "NotificationWorker failed",
                e,
                mapOf(
                    "error" to e.message,
                    "stackTrace" to e.stackTraceToString()
                )
            )
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "notification_processing"
    }
}
