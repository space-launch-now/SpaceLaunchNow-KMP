package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Custom Admin Notification Payload
 *
 * Represents a custom admin/broadcast notification received from the server via the V5 path with
 * a flat payload. Custom notifications are distinguished by `notification_type == "custom"` and
 * are detected FIRST (before event/news/launch) precisely because a custom notification may carry
 * a target_id resembling an event_id and must not be mis-detected.
 *
 * Custom notifications are a broadcast type: gated only by the global kill switch AND the
 * [NotificationTopic.ANNOUNCEMENTS] per-type toggle (not agency/location filtered).
 *
 * Tap routing is driven by [targetType]:
 *  - "launch" -> launch detail (targetId is the launch UUID)
 *  - "event"  -> event detail (targetId is the event ID)
 *  - "news"   -> open targetUrl externally
 *  - "none"   -> app home, no deep link
 */
@Serializable
data class CustomNotificationPayload(
    val notificationType: String,       // "custom"
    val title: String,                  // Server-provided title
    val body: String,                   // Server-provided body
    val customId: String,               // Custom ID (collapse key)
    val targetType: String,             // "launch" | "event" | "news" | "none"
    val targetId: String,               // Deep-link target identifier (launch UUID / event ID)
    val targetUrl: String,              // External URL (used when targetType == "news")
    val customImage: String             // Image URL (may be blank)
) {
    companion object {
        private val log by lazy { logger() }

        const val TARGET_LAUNCH = "launch"
        const val TARGET_EVENT = "event"
        const val TARGET_NEWS = "news"
        const val TARGET_NONE = "none"

        /**
         * Detect if payload is a custom notification.
         *
         * Custom payloads are distinguished by `notification_type == "custom"`. This MUST be
         * checked before event/news/launch detection in the worker's routing order.
         */
        fun isCustomPayload(data: Map<String, String>): Boolean {
            return data[NotificationTopicConfig.PayloadFields.NOTIFICATION_TYPE] == "custom"
        }

        /**
         * Parse custom payload from FCM data map.
         *
         * @param data The FCM data payload as a Map<String, String>
         * @return CustomNotificationPayload or null if required fields are missing
         */
        fun fromMap(data: Map<String, String>): CustomNotificationPayload? {
            return try {
                val customId = data[NotificationTopicConfig.PayloadFields.CUSTOM_ID] ?: return null

                CustomNotificationPayload(
                    notificationType = data[NotificationTopicConfig.PayloadFields.NOTIFICATION_TYPE]
                        ?: return null,
                    title = data[NotificationTopicConfig.PayloadFields.TITLE] ?: return null,
                    body = data[NotificationTopicConfig.PayloadFields.BODY] ?: "",
                    customId = customId,
                    targetType = data[NotificationTopicConfig.PayloadFields.TARGET_TYPE]
                        ?.ifBlank { TARGET_NONE }
                        ?: TARGET_NONE,
                    targetId = data[NotificationTopicConfig.PayloadFields.TARGET_ID] ?: "",
                    targetUrl = data[NotificationTopicConfig.PayloadFields.TARGET_URL] ?: "",
                    customImage = data[NotificationTopicConfig.PayloadFields.CUSTOM_IMAGE] ?: ""
                )
            } catch (e: Exception) {
                log.e(e) { "Failed to parse custom notification payload: ${e.message}" }
                null
            }
        }
    }

    fun toDebugString(): String {
        return buildString {
            append("CustomNotification(")
            append("customId=$customId, ")
            append("targetType=$targetType, ")
            append("targetId=$targetId, ")
            append("targetUrl=$targetUrl)")
        }
    }
}
