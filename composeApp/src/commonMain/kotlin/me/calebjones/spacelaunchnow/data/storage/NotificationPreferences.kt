package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.calebjones.spacelaunchnow.data.model.NotificationSettings

class NotificationPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        private val NOTIFY_DAILY_SUMMARY = booleanPreferencesKey("notify_daily_summary")
        private val NOTIFY_BEFORE_LAUNCH = booleanPreferencesKey("notify_before_launch")
        private val NOTIFY_MINUTES_BEFORE = intPreferencesKey("notify_minutes_before")
        private val SUBSCRIBED_TOPICS = stringSetPreferencesKey("subscribed_topics")
        private val SUBSCRIBED_AGENCIES = stringSetPreferencesKey("subscribed_agencies")
        private val SUBSCRIBED_LOCATIONS = stringSetPreferencesKey("subscribed_locations")
        private val USE_STRICT_MATCHING = booleanPreferencesKey("use_strict_matching")
        private val FOLLOW_ALL_LAUNCHES = booleanPreferencesKey("follow_all_launches")
        private val HIDE_TBD_LAUNCHES = booleanPreferencesKey("hide_tbd_launches")
        private val KEEP_LAUNCHES_FOR_24_HOURS = booleanPreferencesKey("keep_launches_for_24_hours")

        // Additional notification topics
        private val EVENT_NOTIFICATIONS = booleanPreferencesKey("event_notifications")
        private val NETSTAMP_CHANGED = booleanPreferencesKey("netstamp_changed")
        private val WEBCAST_ONLY = booleanPreferencesKey("webcast_only")
        private val TWENTY_FOUR_HOUR = booleanPreferencesKey("twenty_four_hour")
        private val ONE_HOUR = booleanPreferencesKey("one_hour")
        private val TEN_MINUTES = booleanPreferencesKey("ten_minutes")
        private val ONE_MINUTE = booleanPreferencesKey("one_minute")
        private val IN_FLIGHT = booleanPreferencesKey("in_flight")
        private val SUCCESS = booleanPreferencesKey("success")
    }

    val notificationSettingsFlow: Flow<NotificationSettings> = dataStore.data.map { preferences ->
        NotificationSettings(
            enableNotifications = preferences[ENABLE_NOTIFICATIONS] ?: true,
            notifyDailySummary = preferences[NOTIFY_DAILY_SUMMARY] ?: false,
            notifyBeforeLaunch = preferences[NOTIFY_BEFORE_LAUNCH] ?: true,
            notifyMinutesBefore = preferences[NOTIFY_MINUTES_BEFORE] ?: 30,
            subscribedTopics = preferences[SUBSCRIBED_TOPICS] ?: emptySet(),
            subscribedAgencies = (preferences[SUBSCRIBED_AGENCIES] ?: emptySet())
                .mapNotNull { it.toIntOrNull() }.toSet(),
            subscribedLocations = (preferences[SUBSCRIBED_LOCATIONS] ?: emptySet())
                .mapNotNull { it.toIntOrNull() }.toSet(),
            useStrictMatching = preferences[USE_STRICT_MATCHING] ?: false,
            followAllLaunches = preferences[FOLLOW_ALL_LAUNCHES] ?: false,
            hideTbdLaunches = preferences[HIDE_TBD_LAUNCHES] ?: false,
            keepLaunchesFor24Hours = preferences[KEEP_LAUNCHES_FOR_24_HOURS] ?: true,

            // Additional notification topics
            eventNotifications = preferences[EVENT_NOTIFICATIONS] ?: true,
            netstampChanged = preferences[NETSTAMP_CHANGED] ?: false,
            webcastOnly = preferences[WEBCAST_ONLY] ?: true,
            twentyFourHour = preferences[TWENTY_FOUR_HOUR] ?: false,
            oneHour = preferences[ONE_HOUR] ?: false,
            tenMinutes = preferences[TEN_MINUTES] ?: true,
            oneMinute = preferences[ONE_MINUTE] ?: false,
            inFlight = preferences[IN_FLIGHT] ?: false,
            success = preferences[SUCCESS] ?: true
        )
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        dataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATIONS] = settings.enableNotifications
            preferences[NOTIFY_DAILY_SUMMARY] = settings.notifyDailySummary
            preferences[NOTIFY_BEFORE_LAUNCH] = settings.notifyBeforeLaunch
            preferences[NOTIFY_MINUTES_BEFORE] = settings.notifyMinutesBefore
            preferences[SUBSCRIBED_TOPICS] = settings.subscribedTopics
            preferences[SUBSCRIBED_AGENCIES] =
                settings.subscribedAgencies.map { it.toString() }.toSet()
            preferences[SUBSCRIBED_LOCATIONS] =
                settings.subscribedLocations.map { it.toString() }.toSet()
            preferences[USE_STRICT_MATCHING] = settings.useStrictMatching
            preferences[FOLLOW_ALL_LAUNCHES] = settings.followAllLaunches
            preferences[HIDE_TBD_LAUNCHES] = settings.hideTbdLaunches
            preferences[KEEP_LAUNCHES_FOR_24_HOURS] = settings.keepLaunchesFor24Hours

            // Additional notification topics
            preferences[EVENT_NOTIFICATIONS] = settings.eventNotifications
            preferences[NETSTAMP_CHANGED] = settings.netstampChanged
            preferences[WEBCAST_ONLY] = settings.webcastOnly
            preferences[TWENTY_FOUR_HOUR] = settings.twentyFourHour
            preferences[ONE_HOUR] = settings.oneHour
            preferences[TEN_MINUTES] = settings.tenMinutes
            preferences[ONE_MINUTE] = settings.oneMinute
            preferences[IN_FLIGHT] = settings.inFlight
            preferences[SUCCESS] = settings.success
        }
    }

    suspend fun getNotificationSettings(): NotificationSettings {
        return notificationSettingsFlow.first()
    }
}