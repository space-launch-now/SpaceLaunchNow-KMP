package me.calebjones.spacelaunchnow.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a received notification for debugging purposes
 */
@Serializable
data class NotificationHistoryItem(
    val id: String, // Unique ID for this notification receipt
    val receivedAt: Instant, // When the notification was received
    val notificationType: String, // e.g., "oneminute", "netstampchanged", etc.
    val launchId: String?,
    val launchUuid: String?,
    val launchName: String?,
    val launchImage: String?,
    val launchNet: String?,
    val launchLocation: String?,
    val webcast: String?,
    val webcastLive: String?,
    val agencyId: String?,
    val locationId: String?,
    val displayedTitle: String?, // The title shown to user
    val displayedBody: String?, // The body shown to user
    val rawData: Map<String, String>, // Complete raw FCM data payload
    val wasFiltered: Boolean, // Whether this notification was filtered out
    val filterReason: String?, // Why it was filtered (if applicable)
    val wasShown: Boolean // Whether the notification was actually displayed to user
)

/**
 * Summary statistics for notification history
 */
data class NotificationStats(
    val totalReceived: Int,
    val totalDisplayed: Int,
    val totalFiltered: Int,
    val notificationsByType: Map<String, Int>,
    val oldestNotification: Instant?,
    val newestNotification: Instant?
)
