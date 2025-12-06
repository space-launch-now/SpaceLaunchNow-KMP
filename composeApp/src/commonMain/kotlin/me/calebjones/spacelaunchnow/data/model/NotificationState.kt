package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.logger

@Serializable
data class NotificationState(
    val enableNotifications: Boolean = true,
    val followAllLaunches: Boolean = true,
    val useStrictMatching: Boolean = false,
    val hideTbdLaunches: Boolean = false,

    // Topic settings (user-configurable notification timing)
    val topicSettings: Map<String, Boolean> = NotificationTopic.getDefaultTopicSettings(),

    // Agency/Location subscriptions (using IDs for v4 filtering)
    // Changed from topic names to actual agency/location IDs for client-side filtering
    // e.g., "121" for SpaceX, "143" for Texas
    val subscribedAgencies: Set<String> = getDefaultAgencyIds(),
    val subscribedLocations: Set<String> = getDefaultLocationIds(),

    // FCM topics (managed by repository)
    // v4: Only contains version topic like "k_prod_v4" or "k_debug_v4"
    val subscribedTopics: Set<String> = emptySet(),

    // UI state
    val isLoading: Boolean = false,
    val lastError: String? = null
) {
    companion object {
        private val log = logger()
        
        /**
         * Get default agency IDs for all available agencies (using numeric IDs for v4 filtering)
         */
        fun getDefaultAgencyIds(): Set<String> {
            return NotificationAgency.getAll().map { it.id.toString() }.toSet()
        }

        /**
         * Get default location IDs for all available launch sites (using numeric IDs for v4 filtering)
         */
        fun getDefaultLocationIds(): Set<String> {
            return NotificationLocation.getAll().map { it.id.toString() }.toSet()
        }

        val DEFAULT = NotificationState()

        /**
         * Create default settings with all available agencies and locations subscribed
         */
        fun createWithAllSubscriptions(
            availableAgencies: List<NotificationAgency>,
            availableLocations: List<NotificationLocation>
        ): NotificationState {
            return DEFAULT.copy(
                subscribedAgencies = availableAgencies.map { it.id.toString() }.toSet(),
                subscribedLocations = availableLocations.map { it.id.toString() }.toSet()
            )
        }
    }

    // Type-safe accessors
    fun isTopicEnabled(topic: NotificationTopic): Boolean {
        return topicSettings[topic.id] ?: topic.defaultEnabled
    }

    fun isAgencyEnabled(agency: NotificationAgency): Boolean {
        return subscribedAgencies.contains(agency.id.toString())
    }

    fun isAgencyEnabled(agencyId: String): Boolean {
        return subscribedAgencies.contains(agencyId)
    }

    fun isLocationEnabled(location: NotificationLocation): Boolean {
        return subscribedLocations.contains(location.id.toString())
    }

    fun isLocationEnabled(locationId: String): Boolean {
        return subscribedLocations.contains(locationId)
    }

    // State update helpers
    fun withTopicEnabled(topic: NotificationTopic, enabled: Boolean): NotificationState {
        return copy(topicSettings = topicSettings + (topic.id to enabled))
    }

    fun withAgencyEnabled(agency: NotificationAgency, enabled: Boolean): NotificationState {
        return withAgencyEnabled(agency.id.toString(), enabled)
    }

    fun withAgencyEnabled(agencyId: String, enabled: Boolean): NotificationState {
        // Prevent unchecking the last agency - require at least one to be checked
        if (!enabled && subscribedAgencies.size == 1 && subscribedAgencies.contains(agencyId)) {
            log.w { "⚠️ Cannot uncheck last agency - at least one must be selected" }
            return this  // Return unchanged state
        }

        val updatedAgencies = if (enabled) {
            subscribedAgencies + agencyId
        } else {
            subscribedAgencies - agencyId
        }

        // If disabling an agency while followAll is enabled, disable followAll
        val updatedFollowAll = if (!enabled && followAllLaunches) false else followAllLaunches

        return copy(
            subscribedAgencies = updatedAgencies,
            followAllLaunches = updatedFollowAll
        )
    }

    fun withLocationEnabled(location: NotificationLocation, enabled: Boolean): NotificationState {
        return withLocationEnabled(location.id.toString(), enabled)
    }

    fun withLocationEnabled(locationId: String, enabled: Boolean): NotificationState {
        // Prevent unchecking the last location - require at least one to be checked
        if (!enabled && subscribedLocations.size == 1 && subscribedLocations.contains(locationId)) {
            log.w { "⚠️ Cannot uncheck last location - at least one must be selected" }
            return this  // Return unchanged state
        }

        val updatedLocations = if (enabled) {
            subscribedLocations + locationId
        } else {
            subscribedLocations - locationId
        }

        // If disabling a location while followAll is enabled, disable followAll
        val updatedFollowAll = if (!enabled && followAllLaunches) false else followAllLaunches

        return copy(
            subscribedLocations = updatedLocations,
            followAllLaunches = updatedFollowAll
        )
    }

    fun withFollowAllLaunches(
        enabled: Boolean,
        allAgencies: List<NotificationAgency>,
        allLocations: List<NotificationLocation>
    ): NotificationState {
        return if (enabled) {
            // When enabling follow all, ensure all agencies and locations are subscribed
            copy(
                followAllLaunches = true,
                useStrictMatching = false, // Auto-disable strict matching
                subscribedAgencies = allAgencies.map { it.id.toString() }.toSet(),
                subscribedLocations = allLocations.map { it.id.toString() }.toSet()
            )
        } else {
            // When disabling, keep current subscriptions as they are
            copy(followAllLaunches = false)
        }
    }

    fun withError(error: String?): NotificationState {
        return copy(lastError = error)
    }

    fun withLoading(loading: Boolean): NotificationState {
        return copy(isLoading = loading)
    }
}

@Serializable
data class PushMessage(
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class NotificationTopic(
    val id: String,
    val name: String,
    val description: String? = null,
    val defaultEnabled: Boolean = false
) {
    companion object {
        // Core system topics
        val LAUNCHES_ALL = NotificationTopic("launches", "Launches", defaultEnabled = true)
        val EVENTS = NotificationTopic("events", "Events", defaultEnabled = true)
        val FEATURED_NEWS =
            NotificationTopic("featured_news", "Featured News", defaultEnabled = true)

        // Version topics (automatically managed)
        val PROD_V3 = NotificationTopic("prod_v3", "Production v3", defaultEnabled = true)
        val DEBUG_V3 = NotificationTopic("debug_v3", "Debug v3", defaultEnabled = false)

        // Matching topics (automatically managed)
        val ALL_LAUNCHES = NotificationTopic("all", "All Launches", defaultEnabled = true)
        val STRICT_MATCHING = NotificationTopic("strict", "Strict Matching", defaultEnabled = false)
        val NOT_STRICT_MATCHING =
            NotificationTopic("not_strict", "Flexible Matching", defaultEnabled = true)

        // User-configurable notification timing topics
        val NETSTAMP_CHANGED = NotificationTopic(
            "netstampChanged",
            "Launch Time Changes",
            "Get notified when launch times change",
            defaultEnabled = false
        )
        val WEBCAST_ONLY = NotificationTopic(
            "webcastOnly",
            "Webcast Only Launches",
            "Only launches with webcast available",
            defaultEnabled = false
        )
        val TWENTY_FOUR_HOUR = NotificationTopic(
            "twentyFourHour",
            "24 Hour Notice",
            "24 hours before launch",
            defaultEnabled = true
        )
        val ONE_HOUR = NotificationTopic(
            "oneHour",
            "1 Hour Notice",
            "1 hour before launch",
            defaultEnabled = false
        )
        val TEN_MINUTES = NotificationTopic(
            "tenMinutes",
            "10 Minute Notice",
            "10 minutes before launch",
            defaultEnabled = true
        )
        val ONE_MINUTE = NotificationTopic(
            "oneMinute",
            "1 Minute Notice",
            "1 minute before launch",
            defaultEnabled = false
        )
        val IN_FLIGHT = NotificationTopic(
            "inFlight",
            "In-Flight Updates",
            "Launch status updates during flight",
            defaultEnabled = false
        )
        val SUCCESS = NotificationTopic(
            "success",
            "Launch Success",
            "When launches complete successfully",
            defaultEnabled = true
        )
        val FAILURE = NotificationTopic(
            "failure",
            "Launch Failure",
            "When launches fail to complete successfully",
            defaultEnabled = true
        )
        val PARTIAL_FAILURE = NotificationTopic(
            "partial_failure",
            "Partial Launch Failure",
            "When launches partially fail (some objectives met)",
            defaultEnabled = true
        )
        val WEBCAST_LIVE = NotificationTopic(
            "webcastLive",
            "Webcast Started",
            "When launch webcast goes live",
            defaultEnabled = false
        )

        // Agency topics
        val AGENCY_NASA = NotificationTopic("nasa", "NASA", defaultEnabled = true)
        val AGENCY_SPACEX = NotificationTopic("spacex", "SpaceX", defaultEnabled = true)
        val AGENCY_BLUE_ORIGIN =
            NotificationTopic("blueOrigin", "Blue Origin", defaultEnabled = true)
        val AGENCY_ROCKET_LAB = NotificationTopic("rocketLab", "Rocket Lab", defaultEnabled = true)
        val AGENCY_ULA = NotificationTopic("ula", "ULA", defaultEnabled = true)
        val AGENCY_ARIANESPACE =
            NotificationTopic("arianespace", "Arianespace", defaultEnabled = true)
        val AGENCY_ROSCOSMOS = NotificationTopic("roscosmos", "Roscosmos", defaultEnabled = true)
        val AGENCY_NORTHROP =
            NotificationTopic("northrop", "Northrop Grumman", defaultEnabled = true)

        // Location topics
        val LOCATION_KSC = NotificationTopic("ksc", "Kennedy Space Center", defaultEnabled = true)
        val LOCATION_VANDENBERG = NotificationTopic("van", "Vandenberg", defaultEnabled = true)
        val LOCATION_WALLOPS = NotificationTopic("wallops", "Wallops", defaultEnabled = true)
        val LOCATION_TEXAS = NotificationTopic("texas", "Texas", defaultEnabled = true)
        val LOCATION_RUSSIA =
            NotificationTopic("russia", "Russia & Kazakhstan", defaultEnabled = true)
        val LOCATION_FRENCH_GUIANA =
            NotificationTopic("frenchGuiana", "French Guiana", defaultEnabled = true)
        val LOCATION_NEW_ZEALAND =
            NotificationTopic("newZealand", "New Zealand", defaultEnabled = true)
        val LOCATION_JAPAN = NotificationTopic("japan", "Japan", defaultEnabled = true)
        val LOCATION_ISRO = NotificationTopic("isro", "India (ISRO)", defaultEnabled = true)
        val LOCATION_CHINA = NotificationTopic("china", "China", defaultEnabled = true)
        val LOCATION_KODIAK = NotificationTopic("kodiak", "Kodiak", defaultEnabled = true)
        val LOCATION_OTHER = NotificationTopic("other", "Other Locations", defaultEnabled = true)

        /**
         * Get all user-configurable topics (timing topics that users can enable/disable)
         */
        fun getUserConfigurableTopics(): List<NotificationTopic> {
            return listOf(
                NETSTAMP_CHANGED, WEBCAST_ONLY, TWENTY_FOUR_HOUR, ONE_HOUR,
                TEN_MINUTES, ONE_MINUTE, IN_FLIGHT, SUCCESS, FAILURE,
                PARTIAL_FAILURE, WEBCAST_LIVE
            )
        }

        /**
         * Get default topic settings for user-configurable topics
         */
        fun getDefaultTopicSettings(): Map<String, Boolean> {
            return getUserConfigurableTopics().associate { it.id to it.defaultEnabled }
        }
    }
}

@Serializable
data class NotificationAgency(
    val id: Int,
    val topicName: String,
    val name: String,
    val abbreviation: String? = null,

    /**
     * Additional IDs to include when this location is selected.
     * Useful for grouped locations (e.g., KSC + Cape Canaveral Florida region)
     */
    val additionalIds: List<Int> = emptyList()
) {
    companion object {
        val SPACEX = NotificationAgency(121, "spacex", "SpaceX")
        val NASA = NotificationAgency(44, "nasa", "NASA")
        val BLUE_ORIGIN = NotificationAgency(141, "blueOrigin", "Blue Origin")
        val ROCKET_LAB = NotificationAgency(147, "rocketLab", "Rocket Lab")
        val VIRGIN_GALACTIC = NotificationAgency(1024, "virginGalactic", "Virgin Galactic")
        val ULA = NotificationAgency(124, "ula", "United Launch Alliance")
        val ARIANESPACE = NotificationAgency(115, "arianespace", "Arianespace")
        val RUSSIA = NotificationAgency(
            111,
            "roscosmos",
            "Russian Space Agencies",
            additionalIds = listOf(96, 193, 63)
        )
        val NORTHROP_GRUMMAN = NotificationAgency(257, "northrop", "Northrop Grumman")
        val CHINA =
            NotificationAgency(88, "casc", "Chinese Space Agencies", additionalIds = listOf(194))
        val ISRO = NotificationAgency(31, "isro", "Indian Space Research Organisation")

        /**
         * Get all available agencies
         */
        fun getAll(): List<NotificationAgency> {
            return listOf(
                SPACEX, NASA, BLUE_ORIGIN, ROCKET_LAB, VIRGIN_GALACTIC, NORTHROP_GRUMMAN,
                ULA, ARIANESPACE, RUSSIA, CHINA, ISRO
            )
        }
    }
}

@Serializable
data class NotificationLocation(
    val id: Int,
    val topicName: String,
    val name: String,
    val countryCode: String? = null,
    /**
     * Additional IDs to include when this location is selected.
     * Useful for grouped locations (e.g., KSC + Cape Canaveral Florida region)
     */
    val additionalIds: List<Int> = emptyList()
) {
    /**
     * Get all IDs associated with this location (primary + additional)
     */
    fun getAllIds(): List<Int> {
        return listOf(id) + additionalIds
    }

    companion object {
        val VANDENBERG = NotificationLocation(11, "van", "California", "US")

        // Example: Florida region that includes both KSC and Cape Canaveral
        val FLORIDA =
            NotificationLocation(27, "florida", "Florida", "US", additionalIds = listOf(12))
        val OTHER_USA = NotificationLocation(
            21,
            "wallops",
            "Misc. USA",
            "US",
            additionalIds = listOf(1, 25, 31, 155, 162)
        )
        val TEXAS =
            NotificationLocation(143, "texas", "Texas", "US", additionalIds = listOf(29))
        val RUSSIA = NotificationLocation(
            15,
            "russia",
            "Russia & Kazakhstan",
            "KZ",
            additionalIds = listOf(5, 6, 18, 30, 146)
        )
        val FRENCH_GUIANA = NotificationLocation(13, "frenchGuiana", "French Guiana", "GF")
        val NEW_ZEALAND = NotificationLocation(10, "newZealand", "New Zealand", "NZ")
        val JAPAN =
            NotificationLocation(24, "japan", "Japan", "JP", additionalIds = listOf(26, 32, 166))
        val INDIA = NotificationLocation(14, "isro", "India", "IN")
        val CHINA =
            NotificationLocation(17, "china", "China", "CN", additionalIds = listOf(8, 16, 19))
        val OTHER = NotificationLocation(
            20,
            "other",
            "Misc. (Sea, Air, etc)",
            null,
            additionalIds = listOf(3, 20, 144)
        )

        /**
         * Get all available locations for user selection
         */
        fun getAll(): List<NotificationLocation> {
            return listOf(
                VANDENBERG, FLORIDA, OTHER_USA, TEXAS, RUSSIA, FRENCH_GUIANA,
                NEW_ZEALAND, JAPAN, INDIA, CHINA, OTHER
            )
        }
    }
}