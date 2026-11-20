package me.calebjones.spacelaunchnow.data.model

import android.app.NotificationManager
import androidx.annotation.StringRes
import me.calebjones.spacelaunchnow.shared.R

/**
 * Defines notification channels for granular user control
 * Each NotificationTopic gets its own channel so users can control
 * importance levels and DND behavior per notification type
 */
enum class SpaceLaunchNotificationChannel(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val importance: Int,
    val topics: List<NotificationTopic>
) {
    // Critical - Immediate launch events (1-10 minutes, can bypass DND)
    LAUNCH_IMMINENT(
        "launch_imminent_critical",
        R.string.channel_launch_imminent_name,
        R.string.channel_launch_imminent_description,
        NotificationManager.IMPORTANCE_HIGH,
        listOf(NotificationTopic.TEN_MINUTES, NotificationTopic.ONE_MINUTE)
    ),

    // High Priority - Real-time launch status updates
    LAUNCH_STATUS_UPDATES(
        "launch_status_high",
        R.string.channel_launch_status_updates_name,
        R.string.channel_launch_status_updates_description,
        NotificationManager.IMPORTANCE_HIGH,
        listOf(
            NotificationTopic.IN_FLIGHT, NotificationTopic.SUCCESS,
            NotificationTopic.FAILURE, NotificationTopic.PARTIAL_FAILURE
        )
    ),

    // High Priority - Schedule changes (important for planning)
    SCHEDULE_CHANGES(
        "schedule_changes_high",
        R.string.channel_schedule_changes_name,
        R.string.channel_schedule_changes_description,
        NotificationManager.IMPORTANCE_LOW,
        listOf(NotificationTopic.NETSTAMP_CHANGED)
    ),

    // Default Priority - Advance launch reminders
    LAUNCH_REMINDERS(
        "launch_reminders_default",
        R.string.channel_launch_reminders_name,
        R.string.channel_launch_reminders_description,
        NotificationManager.IMPORTANCE_DEFAULT,
        listOf(NotificationTopic.TWENTY_FOUR_HOUR, NotificationTopic.ONE_HOUR)
    ),

    // Default Priority - Webcast notifications
    WEBCAST_NOTIFICATIONS(
        "webcast_notifications_default",
        R.string.channel_webcast_notifications_name,
        R.string.channel_webcast_notifications_description,
        NotificationManager.IMPORTANCE_DEFAULT,
        listOf(NotificationTopic.WEBCAST_LIVE, NotificationTopic.WEBCAST_ONLY)
    ),

    // Default Priority - General space events
    SPACE_EVENTS(
        "space_events_default",
        R.string.channel_space_events_name,
        R.string.channel_space_events_description,
        NotificationManager.IMPORTANCE_DEFAULT,
        listOf(NotificationTopic.EVENTS)
    ),

    // Low Priority - News and updates
    NEWS_UPDATES(
        "news_updates_low",
        R.string.channel_news_updates_name,
        R.string.channel_news_updates_description,
        NotificationManager.IMPORTANCE_LOW,
        listOf(NotificationTopic.FEATURED_NEWS)
    ),

    // Default Priority - Admin announcements / custom broadcasts
    ANNOUNCEMENTS(
        "announcements_default",
        R.string.channel_announcements_name,
        R.string.channel_announcements_description,
        NotificationManager.IMPORTANCE_DEFAULT,
        listOf(NotificationTopic.ANNOUNCEMENTS)
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