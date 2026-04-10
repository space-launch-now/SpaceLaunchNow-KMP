package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Event Notification Payload
 *
 * Represents an event notification received from the server via V5 FCM topics.
 * Event notifications are distinguished from launch notifications by the presence
 * of an 'event_id' field in the data payload.
 *
 * Deep-links to EventDetail screen using the event ID (Int).
 */
@Serializable
data class EventNotificationPayload(
    val notificationType: String,       // event_notification, event_webcast
    val title: String,                  // Server-provided title (event name)
    val body: String,                   // Server-provided body

    val eventId: Int,                   // Event ID for deep linking
    val eventName: String,              // Display name
    val eventDescription: String,       // Event description
    val eventTypeId: String,            // Event type ID
    val eventTypeName: String,          // Event type name (e.g., "Spacewalk")
    val eventDate: String,              // ISO 8601 datetime
    val eventLocation: String,          // Location display name
    val eventNewsUrl: String,           // Info URL
    val eventVideoUrl: String,          // Video URL
    val eventWebcastLive: Boolean,      // Is webcast currently live
    val eventFeatureImage: String?,     // Feature image URL
    val webcast: Boolean                // Has webcast
) {
    companion object {
        private val log by lazy { logger() }

        /**
         * Detect if payload is an event notification.
         *
         * Event payloads are distinguished by the presence of 'event_id' field.
         */
        fun isEventPayload(data: Map<String, String>): Boolean {
            return data.containsKey(NotificationTopicConfig.PayloadFields.EVENT_ID)
        }

        /**
         * Parse event payload from FCM data map.
         *
         * @param data The FCM data payload as a Map<String, String>
         * @return EventNotificationPayload or null if required fields are missing
         */
        fun fromMap(data: Map<String, String>): EventNotificationPayload? {
            return try {
                val eventIdStr = data[NotificationTopicConfig.PayloadFields.EVENT_ID] ?: return null
                val eventId = eventIdStr.toIntOrNull() ?: return null

                EventNotificationPayload(
                    notificationType = data[NotificationTopicConfig.PayloadFields.NOTIFICATION_TYPE] ?: return null,
                    title = data[NotificationTopicConfig.PayloadFields.TITLE]
                        ?: data[NotificationTopicConfig.PayloadFields.EVENT_NAME]
                        ?: return null,
                    body = data[NotificationTopicConfig.PayloadFields.BODY] ?: "",
                    eventId = eventId,
                    eventName = data[NotificationTopicConfig.PayloadFields.EVENT_NAME] ?: return null,
                    eventDescription = data[NotificationTopicConfig.PayloadFields.EVENT_DESCRIPTION] ?: "",
                    eventTypeId = data[NotificationTopicConfig.PayloadFields.EVENT_TYPE_ID] ?: "",
                    eventTypeName = data[NotificationTopicConfig.PayloadFields.EVENT_TYPE_NAME] ?: "",
                    eventDate = data[NotificationTopicConfig.PayloadFields.EVENT_DATE] ?: "",
                    eventLocation = data[NotificationTopicConfig.PayloadFields.EVENT_LOCATION] ?: "",
                    eventNewsUrl = data[NotificationTopicConfig.PayloadFields.EVENT_NEWS_URL] ?: "",
                    eventVideoUrl = data[NotificationTopicConfig.PayloadFields.EVENT_VIDEO_URL] ?: "",
                    eventWebcastLive = data[NotificationTopicConfig.PayloadFields.EVENT_WEBCAST_LIVE]?.lowercase() == "true",
                    eventFeatureImage = data[NotificationTopicConfig.PayloadFields.EVENT_FEATURE_IMAGE]?.ifBlank { null },
                    webcast = data[NotificationTopicConfig.PayloadFields.WEBCAST]?.lowercase() == "true"
                )
            } catch (e: Exception) {
                log.e(e) { "Failed to parse event notification payload: ${e.message}" }
                null
            }
        }
    }

    fun toDebugString(): String {
        return buildString {
            append("EventNotification(")
            append("type=$notificationType, ")
            append("eventId=$eventId, ")
            append("name=$eventName, ")
            append("eventType=$eventTypeName, ")
            append("location=$eventLocation, ")
            append("webcast=$webcast)")
        }
    }
}
