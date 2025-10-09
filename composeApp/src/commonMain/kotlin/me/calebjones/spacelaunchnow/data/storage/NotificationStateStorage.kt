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

class NotificationStateStorage(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        private val FOLLOW_ALL_LAUNCHES = booleanPreferencesKey("follow_all_launches")
        private val USE_STRICT_MATCHING = booleanPreferencesKey("use_strict_matching")
        private val NOTIFY_DAILY_SUMMARY = booleanPreferencesKey("notify_daily_summary")
        private val NOTIFY_BEFORE_LAUNCH = booleanPreferencesKey("notify_before_launch")
        private val NOTIFY_MINUTES_BEFORE = intPreferencesKey("notify_minutes_before")
        private val HIDE_TBD_LAUNCHES = booleanPreferencesKey("hide_tbd_launches")
        private val KEEP_LAUNCHES_FOR_24_HOURS = booleanPreferencesKey("keep_launches_for_24_hours")

        private val TOPIC_SETTINGS = stringPreferencesKey("topic_settings")
        private val SUBSCRIBED_AGENCIES = stringSetPreferencesKey("subscribed_agencies")
        private val SUBSCRIBED_LOCATIONS = stringSetPreferencesKey("subscribed_locations")
        private val SUBSCRIBED_TOPICS = stringSetPreferencesKey("subscribed_topics")
    }

    val stateFlow: Flow<NotificationState> = dataStore.data.map { preferences ->
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
            followAllLaunches = preferences[FOLLOW_ALL_LAUNCHES] ?: default.followAllLaunches,
            useStrictMatching = preferences[USE_STRICT_MATCHING] ?: default.useStrictMatching,
            hideTbdLaunches = preferences[HIDE_TBD_LAUNCHES] ?: default.hideTbdLaunches,
            keepLaunchesFor24Hours = preferences[KEEP_LAUNCHES_FOR_24_HOURS]
                ?: default.keepLaunchesFor24Hours,

            topicSettings = topicSettings,
            subscribedAgencies = (preferences[SUBSCRIBED_AGENCIES] ?: emptySet())
                .map { it }.toSet().ifEmpty { default.subscribedAgencies },
            subscribedLocations = (preferences[SUBSCRIBED_LOCATIONS] ?: emptySet())
                .map { it }.toSet().ifEmpty { default.subscribedLocations },
            subscribedTopics = preferences[SUBSCRIBED_TOPICS] ?: default.subscribedTopics
        )
    }

    suspend fun saveState(state: NotificationState) {
        dataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATIONS] = state.enableNotifications
            preferences[FOLLOW_ALL_LAUNCHES] = state.followAllLaunches
            preferences[USE_STRICT_MATCHING] = state.useStrictMatching
            preferences[HIDE_TBD_LAUNCHES] = state.hideTbdLaunches
            preferences[KEEP_LAUNCHES_FOR_24_HOURS] = state.keepLaunchesFor24Hours

            preferences[TOPIC_SETTINGS] = Json.encodeToString(state.topicSettings)
            preferences[SUBSCRIBED_AGENCIES] =
                state.subscribedAgencies.map { it.toString() }.toSet()
            preferences[SUBSCRIBED_LOCATIONS] =
                state.subscribedLocations.map { it.toString() }.toSet()
            preferences[SUBSCRIBED_TOPICS] = state.subscribedTopics
        }
    }

    suspend fun getState(): NotificationState {
        return stateFlow.first()
    }
}