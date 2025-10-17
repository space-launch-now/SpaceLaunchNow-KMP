package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.data.model.NotificationState

class NotificationPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        private val SUBSCRIBED_TOPICS = stringSetPreferencesKey("subscribed_topics")
        private val SUBSCRIBED_AGENCIES = stringSetPreferencesKey("subscribed_agencies")
        private val SUBSCRIBED_LOCATIONS = stringSetPreferencesKey("subscribed_locations")
        private val USE_STRICT_MATCHING = booleanPreferencesKey("use_strict_matching")
        private val FOLLOW_ALL_LAUNCHES = booleanPreferencesKey("follow_all_launches")
        private val HIDE_TBD_LAUNCHES = booleanPreferencesKey("hide_tbd_launches")
        private val TOPIC_SETTINGS = stringPreferencesKey("topic_settings")

        // New fields in NotificationState
        private val NOTIFY_DAILY_SUMMARY = booleanPreferencesKey("notify_daily_summary")
        private val NOTIFY_BEFORE_LAUNCH = booleanPreferencesKey("notify_before_launch")
        private val NOTIFY_MINUTES_BEFORE = intPreferencesKey("notify_minutes_before")
    }

    val notificationSettingsFlow: Flow<NotificationState> = dataStore.data.map { preferences ->
        val default = NotificationState.DEFAULT

        // Parse topic settings JSON or use defaults
        val topicSettingsJson = preferences[TOPIC_SETTINGS]
        val topicSettings = if (topicSettingsJson != null) {
            try {
                Json.decodeFromString<Map<String, Boolean>>(topicSettingsJson)
            } catch (e: Exception) {
                println("Failed to parse topic settings, using defaults: ${e.message}")
                default.topicSettings
            }
        } else {
            default.topicSettings
        }

        NotificationState(
            enableNotifications = preferences[ENABLE_NOTIFICATIONS] ?: default.enableNotifications,
            subscribedTopics = preferences[SUBSCRIBED_TOPICS] ?: default.subscribedTopics,
            subscribedAgencies = (preferences[SUBSCRIBED_AGENCIES] ?: emptySet())
                .map { it }.toSet(),
            subscribedLocations = (preferences[SUBSCRIBED_LOCATIONS] ?: emptySet())
                .map { it }.toSet(),
            useStrictMatching = preferences[USE_STRICT_MATCHING] ?: default.useStrictMatching,
            followAllLaunches = preferences[FOLLOW_ALL_LAUNCHES] ?: default.followAllLaunches,
            hideTbdLaunches = preferences[HIDE_TBD_LAUNCHES] ?: default.hideTbdLaunches,
            topicSettings = topicSettings,


            // UI state fields (not persisted)
            isLoading = false,
            lastError = null
        )
    }

    suspend fun updateNotificationSettings(settings: NotificationState) {
        dataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATIONS] = settings.enableNotifications
            preferences[SUBSCRIBED_TOPICS] = settings.subscribedTopics
            preferences[SUBSCRIBED_AGENCIES] =
                settings.subscribedAgencies.map { it.toString() }.toSet()
            preferences[SUBSCRIBED_LOCATIONS] =
                settings.subscribedLocations.map { it.toString() }.toSet()
            preferences[USE_STRICT_MATCHING] = settings.useStrictMatching
            preferences[FOLLOW_ALL_LAUNCHES] = settings.followAllLaunches
            preferences[HIDE_TBD_LAUNCHES] = settings.hideTbdLaunches
            preferences[TOPIC_SETTINGS] = Json.encodeToString(settings.topicSettings)
        }
    }

    suspend fun getNotificationSettings(): NotificationState {
        return notificationSettingsFlow.first()
    }
}