package me.calebjones.spacelaunchnow.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.notifications.NotificationDisplayHelper
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.logging.logger
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

    private val log = logger()
    private val notificationStateStorage: NotificationStateStorage by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            log.d { "Starting work..." }

            // Extract notification data from input
            val notificationDataMap = inputData.keyValueMap.mapValues { it.value.toString() }
            val notificationData = NotificationData.fromMap(notificationDataMap)

            if (notificationData == null) {
                log.w { "Failed to parse notification data - dataKeys: ${notificationDataMap.keys.joinToString(",")}" }
                return@withContext Result.failure()
            }

            log.d { "Parsed notification: ${notificationData.launchName}" }

            // Get user settings
            val settings = notificationStateStorage.getState()

            // Apply client-side filtering
            val shouldShow = NotificationFilter.shouldShowNotification(
                data = notificationData,
                state = settings
            )

            if (!shouldShow) {
                log.i { "Notification filtered out - launchId: ${notificationData.launchId}, launchName: ${notificationData.launchName}, agencyId: ${notificationData.agencyId}, locationId: ${notificationData.locationId}" }
                return@withContext Result.success()
            }

            // Show notification (image loading happens here, but we have time)
            // Use fcm_title if provided, otherwise fall back to launch name
            val title = inputData.getString("fcm_title") ?: notificationData.launchName

            log.i { "Displaying notification - launchId: ${notificationData.launchId}, launchName: ${notificationData.launchName}" }

            // NotificationDisplayHelper.showNotification always generates formatted body from NotificationData
            NotificationDisplayHelper.showNotification(
                context = applicationContext,
                notificationData = notificationData,
                title = title
            )

            Result.success()
        } catch (e: Exception) {
            log.e(e) { "NotificationWorker failed: ${e.message}" }
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "notification_processing"
    }
}
