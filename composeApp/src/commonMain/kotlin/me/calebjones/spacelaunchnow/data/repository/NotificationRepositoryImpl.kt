package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.calebjones.spacelaunchnow.data.model.NotificationSettings
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.storage.NotificationPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.util.BuildConfig

class NotificationRepositoryImpl(
    private val pushMessaging: PushMessaging,
    private val notificationPreferences: NotificationPreferences,
    private val debugPreferences: DebugPreferences? = null
) : NotificationRepository {

    override suspend fun initializeNotifications() {
        println("=== NotificationRepository: Starting initialization ===")
        try {
            val settings = getNotificationSettings()
            println("Loaded settings: enableNotifications=${settings.enableNotifications}, followAll=${settings.followAllLaunches}")
            println("Subscribed agencies: ${settings.subscribedAgencies}")
            println("Subscribed locations: ${settings.subscribedLocations}")
            println("Current FCM topics: ${settings.subscribedTopics}")

            // Only subscribe to topics if notifications are enabled
            if (settings.enableNotifications) {
                println("Initializing FCM topic subscriptions...")
                updateTopicSubscriptions()
                println("FCM topic subscriptions initialized successfully")
            } else {
                println("Notifications disabled - skipping FCM topic subscription")
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            println("ERROR: Failed to initialize notification subscriptions: ${e.message}")
            e.printStackTrace()
        }
        println("=== NotificationRepository: Initialization complete ===")
    }

    override suspend fun getNotificationSettings(): NotificationSettings {
        return notificationPreferences.getNotificationSettings().also { settings ->
            println("DEBUG: Retrieved settings - agencies: ${settings.subscribedAgencies.size}, locations: ${settings.subscribedLocations.size}, topics: ${settings.subscribedTopics.size}")
        }
    }

    override suspend fun updateNotificationSettings(settings: NotificationSettings) {
        println("DEBUG: Updating notification settings - agencies: ${settings.subscribedAgencies}, locations: ${settings.subscribedLocations}")
        notificationPreferences.updateNotificationSettings(settings)
    }

    override suspend fun subscribeToTopic(topic: NotificationTopic): Result<Unit> {
        return pushMessaging.subscribeToTopic(topic.id).also { result ->
            if (result.isSuccess) {
                val currentSettings = getNotificationSettings()
                val updatedTopics = currentSettings.subscribedTopics + topic.id
                updateNotificationSettings(currentSettings.copy(subscribedTopics = updatedTopics))
            }
        }
    }

    override suspend fun unsubscribeFromTopic(topic: NotificationTopic): Result<Unit> {
        return pushMessaging.unsubscribeFromTopic(topic.id).also { result ->
            if (result.isSuccess) {
                val currentSettings = getNotificationSettings()
                val updatedTopics = currentSettings.subscribedTopics - topic.id
                updateNotificationSettings(currentSettings.copy(subscribedTopics = updatedTopics))
            }
        }
    }

    override suspend fun requestNotificationPermission(): Boolean {
        // Platform-specific implementation will handle permission request
        return true
    }

    override fun getNotificationSettingsFlow(): Flow<NotificationSettings> {
        return notificationPreferences.notificationSettingsFlow
    }

    override suspend fun getAvailableAgencies(): List<NotificationAgency> {
        // TODO: Fetch from API - using server topic names
        return listOf(
            NotificationAgency(121, "SpaceX", "SpaceX"),
            NotificationAgency(44, "NASA", "NASA"),
            NotificationAgency(141, "Blue Origin", "BO"),
            NotificationAgency(147, "Rocket Lab", "RL"),
            NotificationAgency(124, "ULA", "ULA"),
            NotificationAgency(115, "Arianespace", "ASE"),
            NotificationAgency(111, "ROSCOSMOS", "RSC"), // Also includes 63, 163
            NotificationAgency(257, "Northrop Grumman", "NG")
        )
    }

    override suspend fun getAvailableLocations(): List<NotificationLocation> {
        // TODO: Fetch from API - using server location IDs and topic names
        return listOf(
            NotificationLocation(11, "Vandenberg AFB", "US"),
            NotificationLocation(27, "Cape Canaveral", "US"), // KSC - includes 12
            NotificationLocation(21, "Wallops Island", "US"),
            NotificationLocation(143, "Texas", "US"), // Includes 9999, 29
            NotificationLocation(15, "Russia and Kazakhstan", "RU"), // Includes 5, 6, 18
            NotificationLocation(13, "French Guiana", "GF"),
            NotificationLocation(10, "New Zealand", "NZ"),
            NotificationLocation(24, "Japan", "JP"), // Includes 26, 32
            NotificationLocation(14, "India", "IN"), // ISRO
            NotificationLocation(17, "China", "CN"), // Includes 19, 8, 16, 148
            NotificationLocation(25, "Kodiak", "US"),
            NotificationLocation(20, "Other Locations", null) // Includes 144, 22, 3
        )
    }

    private fun getAgencyTopicName(agencyId: Int): String? {
        return when (agencyId) {
            44 -> "nasa"
            115 -> "arianespace"
            121 -> "spacex"
            124 -> "ula"
            111, 63, 163 -> "roscosmos"
            141 -> "blueOrigin"
            147 -> "rocketLab"
            257 -> "northrop"
            else -> null
        }
    }

    private fun getLocationTopicName(locationId: Int): String? {
        return when (locationId) {
            27, 12 -> "ksc"
            15, 5, 6, 18 -> "russia"
            11 -> "van"
            21 -> "wallops"
            10 -> "newZealand"
            13 -> "frenchGuiana"
            143, 9999, 29 -> "texas"
            20, 144, 22, 3 -> "other"
            25 -> "kodiak"
            24, 26, 32 -> "japan"
            14 -> "isro"
            17, 19, 8, 16, 148 -> "china"
            else -> null
        }
    }

    override suspend fun subscribeToAgency(agency: NotificationAgency): Result<Unit> {
        val currentSettings = getNotificationSettings()
        val updatedAgencies = currentSettings.subscribedAgencies + agency.id
        updateNotificationSettings(currentSettings.copy(subscribedAgencies = updatedAgencies))
        return updateTopicSubscriptions()
    }

    override suspend fun unsubscribeFromAgency(agency: NotificationAgency): Result<Unit> {
        val currentSettings = getNotificationSettings()
        val updatedAgencies = currentSettings.subscribedAgencies - agency.id
        updateNotificationSettings(currentSettings.copy(subscribedAgencies = updatedAgencies))
        return updateTopicSubscriptions()
    }

    override suspend fun subscribeToLocation(location: NotificationLocation): Result<Unit> {
        val currentSettings = getNotificationSettings()
        val updatedLocations = currentSettings.subscribedLocations + location.id
        updateNotificationSettings(currentSettings.copy(subscribedLocations = updatedLocations))
        return updateTopicSubscriptions()
    }

    override suspend fun unsubscribeFromLocation(location: NotificationLocation): Result<Unit> {
        val currentSettings = getNotificationSettings()
        val updatedLocations = currentSettings.subscribedLocations - location.id
        updateNotificationSettings(currentSettings.copy(subscribedLocations = updatedLocations))
        return updateTopicSubscriptions()
    }

    override suspend fun updateTopicSubscriptions(): Result<Unit> {
        println("=== updateTopicSubscriptions: Starting ===")
        // Always get the current settings to avoid stale data
        val settings = getNotificationSettings()

        println("Current settings for topic generation:")
        println("  - followAllLaunches: ${settings.followAllLaunches}")
        println("  - useStrictMatching: ${settings.useStrictMatching}")
        println("  - subscribedAgencies: ${settings.subscribedAgencies}")
        println("  - subscribedLocations: ${settings.subscribedLocations}")

        // Generate topic names based on user selections and matching preference
        val topicsToSubscribe = mutableSetOf<String>()

        // Base topics that should always be subscribed to
        val baseTopics = mutableSetOf<String>()

        // Determine which topic version to use based on debug preferences
        val topicVersion = if (BuildConfig.DEBUG && debugPreferences != null) {
            val debugSettings = try {
                debugPreferences.getDebugSettings()
            } catch (e: Exception) {
                println("Failed to get debug settings, using default: ${e.message}")
                null
            }

            if (debugSettings?.useDebugTopics == true) {
                println("Using DEBUG topics (debug_v3)")
                "k_debug_v3"
            } else {
                println("Using PROD topics (prod_v3)")
                "k_prod_v3"
            }
        } else {
            println("Using PROD topics (prod_v3) - not in debug mode")
            "prod_v3"
        }

        baseTopics.add(topicVersion)
        baseTopics.add("launches") // notification type
        println("Added base topics: $topicVersion, launches")

        // Add additional notification topics based on settings
        if (settings.eventNotifications) {
            baseTopics.add("events")
            baseTopics.add("featured_news")
            println("Added event notification topics: events, featured_news")
        }

        if (settings.enableNotifications) {
            baseTopics.add("notificationEnabled")
            println("Added notification enabled topics: notificationEnabled, webcastLive")
        }

        if (settings.netstampChanged) {
            baseTopics.add("netstampChanged")
            println("Added netstamp changed topic")
        }

        if (settings.webcastOnly) {
            baseTopics.add("webcastOnly")
            println("Added webcast only topic")
        }

        if (settings.twentyFourHour) {
            baseTopics.add("twentyFourHour")
            println("Added 24 hour topic")
        }

        if (settings.oneHour) {
            baseTopics.add("oneHour")
            println("Added 1 hour topic")
        }

        if (settings.tenMinutes) {
            baseTopics.add("tenMinutes")
            println("Added 10 minutes topic")
        }

        if (settings.oneMinute) {
            baseTopics.add("oneMinute")
            println("Added 1 minute topic")
        }

        if (settings.inFlight) {
            baseTopics.add("inFlight")
            println("Added in-flight topic")
        }

        if (settings.success) {
            baseTopics.add("success")
            println("Added success topic")
        }

        // 'all' topic is just an additional topic if Follow All is enabled
        if (settings.followAllLaunches) {
            baseTopics.add("all")
            println("Added 'all' topic (Follow All enabled)")
        }

        // Add strict/not_strict topics ONLY if followAll is off
        if (!settings.followAllLaunches) {
            if (settings.useStrictMatching) {
                baseTopics.add("strict")
                println("Added 'strict' topic")
            } else {
                baseTopics.add("not_strict")
                println("Added 'not_strict' topic")
            }
        }

        // Always add agency topics (do NOT skip for followAll)
        settings.subscribedAgencies.forEach { agencyId ->
            getAgencyTopicName(agencyId)?.let { topicName ->
                baseTopics.add(topicName)
                println("Added agency topic: $topicName (ID: $agencyId)")
            } ?: println("WARNING: No topic name for agency ID: $agencyId")
        }

        // Always add location topics (do NOT skip for followAll)
        settings.subscribedLocations.forEach { locationId ->
            getLocationTopicName(locationId)?.let { topicName ->
                baseTopics.add(topicName)
                println("Added location topic: $topicName (ID: $locationId)")
            } ?: println("WARNING: No topic name for location ID: $locationId")
        }

        // Each individual topic gets subscribed to
        topicsToSubscribe.addAll(baseTopics)
        println("Final topics to subscribe to: $topicsToSubscribe")

        // Subscribe to new topics and unsubscribe from old ones
        val currentTopics = settings.subscribedTopics
        val topicsToAdd = topicsToSubscribe - currentTopics
        val topicsToRemove = currentTopics - topicsToSubscribe

        println("Topic changes:")
        println("  - Topics to add: $topicsToAdd")
        println("  - Topics to remove: $topicsToRemove")

        var allSuccessful = true

        topicsToAdd.forEach { topic ->
            println("Subscribing to topic: $topic")
            val result = pushMessaging.subscribeToTopic(topic)
            if (result.isFailure) {
                println("ERROR: Failed to subscribe to topic $topic: ${result.exceptionOrNull()?.message}")
                allSuccessful = false
            } else {
                println("SUCCESS: Subscribed to topic: $topic")
            }
        }

        topicsToRemove.forEach { topic ->
            println("Unsubscribing from topic: $topic")
            val result = pushMessaging.unsubscribeFromTopic(topic)
            if (result.isFailure) {
                println("ERROR: Failed to unsubscribe from topic $topic: ${result.exceptionOrNull()?.message}")
                allSuccessful = false
            } else {
                println("SUCCESS: Unsubscribed from topic: $topic")
            }
        }

        if (allSuccessful) {
            // Get the very latest settings and only update the subscribedTopics field
            val latestSettings = getNotificationSettings()
            updateNotificationSettings(latestSettings.copy(subscribedTopics = topicsToSubscribe))
            println("=== updateTopicSubscriptions: SUCCESS ===")
            return Result.success(Unit)
        } else {
            println("=== updateTopicSubscriptions: FAILED (some operations failed) ===")
            return Result.failure(Exception("Failed to update some topic subscriptions"))
        }
    }
}