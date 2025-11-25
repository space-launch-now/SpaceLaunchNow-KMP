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
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
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

        // Use stored preferences if available, otherwise use defaults
        // Important: null means "never set" (use defaults), empty set means "explicitly deselected all"
        val storedAgencies = preferences[SUBSCRIBED_AGENCIES]
        val subscribedAgencies = if (storedAgencies != null) {
            storedAgencies.map { it }.toSet()  // Use stored value even if empty
        } else {
            default.subscribedAgencies  // Only use defaults if never set
        }

        val storedLocations = preferences[SUBSCRIBED_LOCATIONS]
        val subscribedLocations = if (storedLocations != null) {
            storedLocations.map { it }.toSet()  // Use stored value even if empty
        } else {
            default.subscribedLocations  // Only use defaults if never set
        }

        // Helper function to map IDs to names
        fun getAgencyNames(ids: Set<String>): List<String> {
            val allAgencies = NotificationAgency.getAll()
            return ids.mapNotNull { id ->
                allAgencies.find { it.id.toString() == id }?.name
            }
        }

        fun getLocationNames(ids: Set<String>): List<String> {
            val allLocations = NotificationLocation.getAll()
            return ids.mapNotNull { id ->
                allLocations.find { it.id.toString() == id }?.name
            }
        }

        val agencyNames = getAgencyNames(subscribedAgencies)
        val locationNames = getLocationNames(subscribedLocations)

        println("=== NotificationPreferences: Loading Settings ===")
        println("Subscribed Agencies (${subscribedAgencies.size}): $subscribedAgencies")
        println("Agency Names: ${agencyNames.joinToString(", ")}")
        println("Subscribed Locations (${subscribedLocations.size}): $subscribedLocations")
        println("Location Names: ${locationNames.joinToString(", ")}")
        println("Follow All Launches: ${preferences[FOLLOW_ALL_LAUNCHES] ?: default.followAllLaunches}")
        println("Use Strict Matching: ${preferences[USE_STRICT_MATCHING] ?: default.useStrictMatching}")

        DatadogLogger.debug(
            "Notification settings loaded", mapOf(
                "subscribedAgenciesCount" to subscribedAgencies.size,
                "subscribedAgencies" to subscribedAgencies.joinToString(","),
                "agencyNames" to agencyNames.joinToString(","),
                "subscribedLocationsCount" to subscribedLocations.size,
                "subscribedLocations" to subscribedLocations.joinToString(","),
                "locationNames" to locationNames.joinToString(","),
                "followAllLaunches" to (preferences[FOLLOW_ALL_LAUNCHES]
                    ?: default.followAllLaunches),
                "useStrictMatching" to (preferences[USE_STRICT_MATCHING]
                    ?: default.useStrictMatching),
                "enableNotifications" to (preferences[ENABLE_NOTIFICATIONS]
                    ?: default.enableNotifications)
            )
        )

        NotificationState(
            enableNotifications = preferences[ENABLE_NOTIFICATIONS] ?: default.enableNotifications,
            subscribedTopics = preferences[SUBSCRIBED_TOPICS] ?: default.subscribedTopics,
            subscribedAgencies = subscribedAgencies,
            subscribedLocations = subscribedLocations,
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
        // Get agency and location names for logging
        val allAgencies = NotificationAgency.getAll()
        val allLocations = NotificationLocation.getAll()
        
        val agencyNames = settings.subscribedAgencies.mapNotNull { agencyId ->
            allAgencies.find { it.id.toString() == agencyId }?.name
        }
        val locationNames = settings.subscribedLocations.mapNotNull { locationId ->
            allLocations.find { it.id.toString() == locationId }?.name
        }

        // Log the settings being saved
        println("NotificationPreferences: Saving notification settings")
        println("  Agencies (${settings.subscribedAgencies.size}): ${settings.subscribedAgencies} -> $agencyNames")
        println("  Locations (${settings.subscribedLocations.size}): ${settings.subscribedLocations} -> $locationNames")
        println("  Enable Notifications: ${settings.enableNotifications}")
        println("  Follow All Launches: ${settings.followAllLaunches}")
        println("  Use Strict Matching: ${settings.useStrictMatching}")
        println("  Hide TBD Launches: ${settings.hideTbdLaunches}")
        println("  Subscribed Topics: ${settings.subscribedTopics}")

        DatadogLogger.debug(
            "Saving notification settings",
            mapOf(
                "subscribedAgencies" to settings.subscribedAgencies.joinToString(),
                "agencyNames" to agencyNames.joinToString(),
                "subscribedLocations" to settings.subscribedLocations.joinToString(),
                "locationNames" to locationNames.joinToString(),
                "enableNotifications" to settings.enableNotifications,
                "followAllLaunches" to settings.followAllLaunches,
                "useStrictMatching" to settings.useStrictMatching,
                "hideTbdLaunches" to settings.hideTbdLaunches,
                "subscribedTopics" to settings.subscribedTopics.joinToString()
            )
        )

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

        println("NotificationPreferences: Settings saved successfully")
        DatadogLogger.info("Notification settings saved successfully")
    }

    suspend fun getNotificationSettings(): NotificationState {
        return notificationSettingsFlow.first()
    }
}