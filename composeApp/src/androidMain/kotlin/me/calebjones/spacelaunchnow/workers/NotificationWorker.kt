package me.calebjones.spacelaunchnow.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.calebjones.spacelaunchnow.data.model.FilterResult
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.model.V5NotificationFilter
import me.calebjones.spacelaunchnow.data.model.V5NotificationPayload
import me.calebjones.spacelaunchnow.data.notifications.NotificationDisplayHelper
import me.calebjones.spacelaunchnow.data.storage.NotificationHistoryStorage
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
 *
 * V5 Support:
 * - Detects V5 payloads by presence of 'lsp_id' field
 * - V5 payloads use V5NotificationFilter for extended filtering
 * - V4 payloads continue to use NotificationFilter for backward compatibility
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val log = logger()
    private val notificationStateStorage: NotificationStateStorage by inject()
    private val notificationHistoryStorage: NotificationHistoryStorage? by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            log.d { "🔔 NotificationWorker: Starting work..." }

            // Extract notification data from input
            val notificationDataMap = inputData.keyValueMap.mapValues { it.value.toString() }
            
            // Detect V5 vs V4 payload
            val isV5 = V5NotificationPayload.isV5Payload(notificationDataMap)
            log.d { "🔔 NotificationWorker: Payload version detected: ${if (isV5) "V5" else "V4"}" }

            if (isV5) {
                processV5Notification(notificationDataMap)
            } else {
                processV4Notification(notificationDataMap)
            }
        } catch (e: Exception) {
            log.e(e) { "❌ NotificationWorker failed: ${e.message}" }
            Result.failure()
        }
    }

    /**
     * Process V5 notification with extended filtering
     */
    private suspend fun processV5Notification(dataMap: Map<String, String>): Result {
        val v5Payload = V5NotificationPayload.fromMap(dataMap)
        if (v5Payload == null) {
            log.w { "⚠️ Failed to parse V5 notification data - keys: ${dataMap.keys.joinToString(",")}" }
            return Result.failure()
        }

        log.d { "🔔 V5 Parsed: ${v5Payload.toDebugString()}" }

        // Get user settings
        val settings = notificationStateStorage.getState()
        val v5Preferences = settings.v5Preferences

        // Apply V5 client-side filtering
        val filterResult = V5NotificationFilter.shouldShow(v5Payload, v5Preferences)

        // Use server-provided title and body for V5
        val title = v5Payload.title
        val body = v5Payload.body

        if (!filterResult.shouldShow()) {
            val reason = filterResult.getBlockReason() ?: "Unknown filter reason"
            log.i { "🔇 V5 Notification filtered out - ${v5Payload.launchName}: $reason" }

            // Save to history even if filtered (for debugging)
            saveToHistory(
                notificationType = v5Payload.notificationType,
                launchId = v5Payload.launchId,
                launchUuid = v5Payload.launchUuid,
                launchName = v5Payload.launchName,
                launchImage = v5Payload.launchImage,
                launchNet = v5Payload.launchNet,
                launchLocation = v5Payload.launchLocation,
                webcast = v5Payload.webcast.toString(),
                webcastLive = v5Payload.webcastLive.toString(),
                agencyId = v5Payload.lspId?.toString() ?: "",
                locationId = v5Payload.locationId?.toString() ?: "",
                displayedTitle = title,
                displayedBody = body,
                rawData = dataMap,
                wasFiltered = true,
                filterReason = reason
            )

            return Result.success()
        }

        // Show notification using server-provided title and body
        log.i { "✅ V5 Displaying notification - ${v5Payload.launchName}" }

        NotificationDisplayHelper.showV5Notification(
            context = applicationContext,
            payload = v5Payload,
            title = title,
            body = body
        )

        // Save to history
        saveToHistory(
            notificationType = v5Payload.notificationType,
            launchId = v5Payload.launchId,
            launchUuid = v5Payload.launchUuid,
            launchName = v5Payload.launchName,
            launchImage = v5Payload.launchImage,
            launchNet = v5Payload.launchNet,
            launchLocation = v5Payload.launchLocation,
            webcast = v5Payload.webcast.toString(),
            webcastLive = v5Payload.webcastLive.toString(),
            agencyId = v5Payload.lspId?.toString() ?: "",
            locationId = v5Payload.locationId?.toString() ?: "",
            displayedTitle = title,
            displayedBody = body,
            rawData = dataMap,
            wasFiltered = false
        )

        return Result.success()
    }

    /**
     * Process V4 notification (backward compatibility)
     */
    private suspend fun processV4Notification(dataMap: Map<String, String>): Result {
        val notificationData = NotificationData.fromMap(dataMap)
        if (notificationData == null) {
            log.w { "⚠️ Failed to parse V4 notification data - keys: ${dataMap.keys.joinToString(",")}" }
            return Result.failure()
        }

        log.d { "🔔 V4 Parsed: ${notificationData.launchName}" }

        // Get user settings
        val settings = notificationStateStorage.getState()

        // Apply V4 client-side filtering
        val shouldShow = NotificationFilter.shouldShowNotification(
            data = notificationData,
            state = settings
        )

        // Prepare display strings
        val title = inputData.getString("fcm_title") ?: notificationData.launchName
        val body = inputData.getString("fcm_body") 
            ?: "${notificationData.notificationType} for ${notificationData.launchName}"

        if (!shouldShow) {
            log.i { "🔇 V4 Notification filtered out - ${notificationData.launchName}" }

            // Save to history even if filtered
            saveToHistory(
                notificationType = notificationData.notificationType,
                launchId = notificationData.launchId,
                launchUuid = notificationData.launchUuid,
                launchName = notificationData.launchName,
                launchImage = notificationData.launchImage,
                launchNet = notificationData.launchNet,
                launchLocation = notificationData.launchLocation,
                webcast = notificationData.webcast,
                webcastLive = notificationData.webcastLive,
                agencyId = notificationData.agencyId,
                locationId = notificationData.locationId,
                displayedTitle = title,
                displayedBody = body,
                rawData = dataMap,
                wasFiltered = true,
                filterReason = "V4 user settings filtered this notification"
            )

            return Result.success()
        }

        // Show notification
        log.i { "✅ V4 Displaying notification - ${notificationData.launchName}" }

        NotificationDisplayHelper.showNotification(
            context = applicationContext,
            notificationData = notificationData,
            title = title
        )

        // Save to history
        saveToHistory(
            notificationType = notificationData.notificationType,
            launchId = notificationData.launchId,
            launchUuid = notificationData.launchUuid,
            launchName = notificationData.launchName,
            launchImage = notificationData.launchImage,
            launchNet = notificationData.launchNet,
            launchLocation = notificationData.launchLocation,
            webcast = notificationData.webcast,
            webcastLive = notificationData.webcastLive,
            agencyId = notificationData.agencyId,
            locationId = notificationData.locationId,
            displayedTitle = title,
            displayedBody = body,
            rawData = dataMap,
            wasFiltered = false
        )

        return Result.success()
    }

    /**
     * Save notification to history storage
     */
    private suspend fun saveToHistory(
        notificationType: String,
        launchId: String,
        launchUuid: String,
        launchName: String,
        launchImage: String?,
        launchNet: String,
        launchLocation: String,
        webcast: String,
        webcastLive: String?,
        agencyId: String,
        locationId: String,
        displayedTitle: String,
        displayedBody: String,
        rawData: Map<String, String>,
        wasFiltered: Boolean,
        filterReason: String? = null
    ) {
        notificationHistoryStorage?.addNotification(
            notificationType = notificationType,
            launchId = launchId,
            launchUuid = launchUuid,
            launchName = launchName,
            launchImage = launchImage,
            launchNet = launchNet,
            launchLocation = launchLocation,
            webcast = webcast,
            webcastLive = webcastLive,
            agencyId = agencyId,
            locationId = locationId,
            displayedTitle = displayedTitle,
            displayedBody = displayedBody,
            rawData = rawData,
            wasFiltered = wasFiltered,
            filterReason = filterReason
        )
    }

    companion object {
        const val WORK_NAME = "notification_processing"
    }
}
