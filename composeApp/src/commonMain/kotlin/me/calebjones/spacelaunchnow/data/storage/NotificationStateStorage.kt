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
import me.calebjones.spacelaunchnow.util.logging.logger

class NotificationStateStorage(private val dataStore: DataStore<Preferences>) {
    private val log = logger()

    companion object {
        private val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        private val FOLLOW_ALL_LAUNCHES = booleanPreferencesKey("follow_all_launches")
        private val USE_STRICT_MATCHING = booleanPreferencesKey("use_strict_matching")
        private val NOTIFY_DAILY_SUMMARY = booleanPreferencesKey("notify_daily_summary")
        private val NOTIFY_BEFORE_LAUNCH = booleanPreferencesKey("notify_before_launch")
        private val NOTIFY_MINUTES_BEFORE = intPreferencesKey("notify_minutes_before")
        private val HIDE_TBD_LAUNCHES = booleanPreferencesKey("hide_tbd_launches")

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
                log.w(e) { "Failed to parse topic settings, using defaults: ${e.message}" }
                default.topicSettings
            }
        } else {
            default.topicSettings
        }

        // Important: Use stored values even if empty (user explicitly deselected all)
        // Only fall back to defaults if never set (null)
        // Handle sentinel value "__EMPTY__" which indicates user explicitly deselected all
        val storedAgencies = preferences[SUBSCRIBED_AGENCIES]
        val storedLocations = preferences[SUBSCRIBED_LOCATIONS]

        NotificationState(
            enableNotifications = preferences[ENABLE_NOTIFICATIONS] ?: default.enableNotifications,
            followAllLaunches = preferences[FOLLOW_ALL_LAUNCHES] ?: default.followAllLaunches,
            useStrictMatching = preferences[USE_STRICT_MATCHING] ?: default.useStrictMatching,
            hideTbdLaunches = preferences[HIDE_TBD_LAUNCHES] ?: default.hideTbdLaunches,

            topicSettings = topicSettings,
            subscribedAgencies = when {
                storedAgencies == null -> default.subscribedAgencies  // Never set - use defaults
                storedAgencies.contains("__EMPTY__") -> emptySet()    // Explicitly empty
                else -> storedAgencies.map { it }.toSet()              // Has values
            },
            subscribedLocations = when {
                storedLocations == null -> default.subscribedLocations  // Never set - use defaults
                storedLocations.contains("__EMPTY__") -> emptySet()     // Explicitly empty
                else -> storedLocations.map { it }.toSet()               // Has values
            },
            subscribedTopics = preferences[SUBSCRIBED_TOPICS] ?: default.subscribedTopics
        )
    }

    suspend fun saveState(state: NotificationState) {
        dataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATIONS] = state.enableNotifications
            preferences[FOLLOW_ALL_LAUNCHES] = state.followAllLaunches
            preferences[USE_STRICT_MATCHING] = state.useStrictMatching
            preferences[HIDE_TBD_LAUNCHES] = state.hideTbdLaunches

            preferences[TOPIC_SETTINGS] = Json.encodeToString(state.topicSettings)
            
            // Important: DataStore may remove keys with empty sets, so we use a sentinel value
            // to distinguish between "never set" (null) and "explicitly empty" (set with sentinel)
            preferences[SUBSCRIBED_AGENCIES] = if (state.subscribedAgencies.isEmpty()) {
                setOf("__EMPTY__")  // Sentinel value for empty selection
            } else {
                state.subscribedAgencies.map { it.toString() }.toSet()
            }
            
            preferences[SUBSCRIBED_LOCATIONS] = if (state.subscribedLocations.isEmpty()) {
                setOf("__EMPTY__")  // Sentinel value for empty selection
            } else {
                state.subscribedLocations.map { it.toString() }.toSet()
            }
            
            preferences[SUBSCRIBED_TOPICS] = state.subscribedTopics
        }
    }

    suspend fun getState(): NotificationState {
        return stateFlow.first()
    }
}