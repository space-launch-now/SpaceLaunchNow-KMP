package me.calebjones.spacelaunchnow.data.model

import android.app.NotificationManager

/**
 * Defines notification channels for granular user control
 * Each NotificationTopic gets its own channel so users can control
 * importance levels and DND behavior per notification type
 */
enum class SpaceLaunchNotificationChannel(
    val id: String,
    val displayName: String,
    val description: String,
    val importance: Int,
    val topics: List<NotificationTopic>
) {
    // Critical - Immediate launch events (1-10 minutes, can bypass DND)
    LAUNCH_IMMINENT(
        "launch_imminent_critical",
        "Launch Imminent (Critical)",
        "Final countdown notifications (1-10 minutes before launch)",
        NotificationManager.IMPORTANCE_HIGH,
        listOf(NotificationTopic.TEN_MINUTES, NotificationTopic.ONE_MINUTE)
    ),

    // High Priority - Real-time launch status updates
    LAUNCH_STATUS_UPDATES(
        "launch_status_high",
        "Launch Status Updates",
        "Real-time launch progress, success, and failure notifications",
        NotificationManager.IMPORTANCE_HIGH,
        listOf(
            NotificationTopic.IN_FLIGHT, NotificationTopic.SUCCESS,
            NotificationTopic.FAILURE, NotificationTopic.PARTIAL_FAILURE
        )
    ),

    // High Priority - Schedule changes (important for planning)
    SCHEDULE_CHANGES(
        "schedule_changes_high",
        "Schedule Changes",
        "Notifications when launch dates/times change",
        NotificationManager.IMPORTANCE_LOW,
        listOf(NotificationTopic.NETSTAMP_CHANGED)
    ),

    // Default Priority - Advance launch reminders
    LAUNCH_REMINDERS(
        "launch_reminders_default",
        "Launch Reminders",
        "Launch notifications 1-24 hours in advance",
        NotificationManager.IMPORTANCE_DEFAULT,
        listOf(NotificationTopic.TWENTY_FOUR_HOUR, NotificationTopic.ONE_HOUR)
    ),

    // Default Priority - Webcast notifications
    WEBCAST_NOTIFICATIONS(
        "webcast_notifications_default",
        "Webcast Notifications",
        "Notifications about webcast availability and when webcasts go live",
        NotificationManager.IMPORTANCE_DEFAULT,
        listOf(NotificationTopic.WEBCAST_LIVE, NotificationTopic.WEBCAST_ONLY)
    ),

    // Default Priority - General space events
    SPACE_EVENTS(
        "space_events_default",
        "Space Events",
        "Notifications for space-related events and milestones",
        NotificationManager.IMPORTANCE_DEFAULT,
        listOf(NotificationTopic.EVENTS)
    ),

    // Low Priority - News and updates
    NEWS_UPDATES(
        "news_updates_low",
        "News & Updates",
        "News and general updates about space missions",
        NotificationManager.IMPORTANCE_LOW,
        listOf(NotificationTopic.FEATURED_NEWS)
    );

    companion object {
        /**
         * Get the notification channel for a specific topic
         */
        fun getChannelForTopic(topic: NotificationTopic): SpaceLaunchNotificationChannel {
            return values().firstOrNull { channel ->
                channel.topics.contains(topic)
            } ?: LAUNCH_REMINDERS // Default fallback
        }

        /**
         * Get channel by ID
         */
        fun getChannelById(id: String): SpaceLaunchNotificationChannel? {
            return values().firstOrNull { it.id == id }
        }

        /**
         * Get all user-configurable channels (excludes system channels)
         */
        fun getUserConfigurableChannels(): List<SpaceLaunchNotificationChannel> {
            return values().toList()
        }
    }
}