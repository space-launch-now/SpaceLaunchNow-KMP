package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * News Notification Payload
 *
 * Represents a featured-news notification received from the server via the V5 path with a flat
 * payload. News notifications are distinguished from launch notifications by the presence of an
 * 'article_id' field (and the absence of 'event_id' / 'lsp_id') in the data payload.
 *
 * News notifications are a broadcast type: gated only by the global kill switch AND the
 * [NotificationTopic.FEATURED_NEWS] per-type toggle (not agency/location filtered).
 *
 * Tapping a news notification opens [articleUrl] externally in the browser, matching the
 * news-list behavior.
 */
@Serializable
data class NewsNotificationPayload(
    val notificationType: String,       // "featured_news"
    val title: String,                  // Server-provided title
    val body: String,                   // Server-provided body
    val articleId: String,              // Article ID (marker / collapse key)
    val articleTitle: String,           // Article headline
    val newsSite: String,               // Source news site name
    val articleUrl: String,             // External URL opened on tap
    val articleImage: String            // Feature image URL (may be blank)
) {
    companion object {
        private val log by lazy { logger() }

        /**
         * Detect if payload is a news notification.
         *
         * News payloads are distinguished by the presence of the 'article_id' field.
         */
        fun isNewsPayload(data: Map<String, String>): Boolean {
            return data.containsKey(NotificationTopicConfig.PayloadFields.ARTICLE_ID)
        }

        /**
         * Parse news payload from FCM data map.
         *
         * @param data The FCM data payload as a Map<String, String>
         * @return NewsNotificationPayload or null if required fields are missing
         */
        fun fromMap(data: Map<String, String>): NewsNotificationPayload? {
            return try {
                val articleId = data[NotificationTopicConfig.PayloadFields.ARTICLE_ID] ?: return null

                NewsNotificationPayload(
                    notificationType = data[NotificationTopicConfig.PayloadFields.NOTIFICATION_TYPE]
                        ?: "featured_news",
                    title = data[NotificationTopicConfig.PayloadFields.TITLE]
                        ?: data[NotificationTopicConfig.PayloadFields.ARTICLE_TITLE]
                        ?: return null,
                    body = data[NotificationTopicConfig.PayloadFields.BODY] ?: "",
                    articleId = articleId,
                    articleTitle = data[NotificationTopicConfig.PayloadFields.ARTICLE_TITLE] ?: "",
                    newsSite = data[NotificationTopicConfig.PayloadFields.ARTICLE_NEWS_SITE] ?: "",
                    articleUrl = data[NotificationTopicConfig.PayloadFields.ARTICLE_URL] ?: return null,
                    articleImage = data[NotificationTopicConfig.PayloadFields.ARTICLE_IMAGE] ?: ""
                )
            } catch (e: Exception) {
                log.e(e) { "Failed to parse news notification payload: ${e.message}" }
                null
            }
        }
    }

    fun toDebugString(): String {
        return buildString {
            append("NewsNotification(")
            append("type=$notificationType, ")
            append("articleId=$articleId, ")
            append("articleTitle=$articleTitle, ")
            append("newsSite=$newsSite, ")
            append("url=$articleUrl)")
        }
    }
}
