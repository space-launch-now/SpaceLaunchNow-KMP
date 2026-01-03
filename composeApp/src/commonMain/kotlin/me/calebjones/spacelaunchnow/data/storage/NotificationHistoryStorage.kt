package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.data.model.NotificationHistoryItem
import me.calebjones.spacelaunchnow.data.model.NotificationStats
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Storage for notification history for debugging
 * Stores the last 100 notifications received
 */
class NotificationHistoryStorage(private val dataStore: DataStore<Preferences>) {

    private val log = SpaceLogger.getLogger("NotificationHistoryStorage")
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val NOTIFICATION_HISTORY_KEY = stringPreferencesKey("notification_history_json")
        private const val MAX_HISTORY_SIZE = 100
    }

    /**
     * Flow of notification history
     */
    val historyFlow: Flow<List<NotificationHistoryItem>> = dataStore.data.map { preferences ->
        val jsonString = preferences[NOTIFICATION_HISTORY_KEY]
        if (jsonString.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<NotificationHistoryItem>>(jsonString)
            } catch (e: Exception) {
                log.e { "Failed to decode notification history: ${e.message}" }
                emptyList()
            }
        }
    }

    /**
     * Get current notification history
     */
    suspend fun getHistory(): List<NotificationHistoryItem> {
        return historyFlow.first()
    }

    /**
     * Add a notification to history
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun addNotification(
        notificationType: String,
        launchId: String?,
        launchUuid: String?,
        launchName: String?,
        launchImage: String?,
        launchNet: String?,
        launchLocation: String?,
        webcast: String?,
        webcastLive: String?,
        agencyId: String?,
        locationId: String?,
        displayedTitle: String?,
        displayedBody: String?,
        rawData: Map<String, String>,
        wasFiltered: Boolean,
        filterReason: String? = null,
        wasShown: Boolean = true
    ) {
        dataStore.edit { preferences ->
            val currentHistory = getHistory().toMutableList()

            // Add new notification at the beginning
            val newItem = NotificationHistoryItem(
                id = Uuid.random().toString(),
                receivedAt = Clock.System.now(),
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
                filterReason = filterReason,
                wasShown = wasShown
            )

            currentHistory.add(0, newItem)

            // Keep only the most recent MAX_HISTORY_SIZE notifications
            if (currentHistory.size > MAX_HISTORY_SIZE) {
                currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
            }

            // Save back to storage
            val jsonString = json.encodeToString(currentHistory)
            preferences[NOTIFICATION_HISTORY_KEY] = jsonString

            log.d { "Added notification to history: $notificationType for ${launchName ?: "unknown"}, wasFiltered=$wasFiltered, total history size=${currentHistory.size}" }
        }
    }

    /**
     * Clear all notification history
     */
    suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences.remove(NOTIFICATION_HISTORY_KEY)
        }
        log.i { "Cleared notification history" }
    }

    /**
     * Get statistics about notification history
     */
    suspend fun getStats(): NotificationStats {
        val history = getHistory()

        if (history.isEmpty()) {
            return NotificationStats(
                totalReceived = 0,
                totalDisplayed = 0,
                totalFiltered = 0,
                notificationsByType = emptyMap(),
                oldestNotification = null,
                newestNotification = null
            )
        }

        val notificationsByType = history
            .groupBy { it.notificationType }
            .mapValues { it.value.size }

        return NotificationStats(
            totalReceived = history.size,
            totalDisplayed = history.count { !it.wasFiltered },
            totalFiltered = history.count { it.wasFiltered },
            notificationsByType = notificationsByType,
            oldestNotification = history.minOfOrNull { it.receivedAt },
            newestNotification = history.maxOfOrNull { it.receivedAt }
        )
    }
}
