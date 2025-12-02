package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Notification data structure for v4 notifications
 * Server sends this data structure and client filters based on user preferences
 */
@Serializable
data class NotificationData(
    val notificationType: String,        // e.g., "twentyFourHour", "oneHour", "tenMinutes", etc.
    val launchId: String,                 // Library ID as string
    val launchUuid: String,               // UUID as string
    val launchName: String,
    val launchImage: String?,
    val launchNet: String,                // Formatted date string
    val launchLocation: String,
    val webcast: String,                  // "true" or "false" as string
    val webcastLive: String?,              // "true" or "false" as string (optional)
    val agencyId: String,                 // e.g., "121" for SpaceX
    val locationId: String                // e.g., "143" for Texas
) {
    companion object {
        private val log = logger()
        
        /**
         * Parse notification data from FCM data payload
         * Returns null if required fields are missing
         */
        fun fromMap(data: Map<String, String>): NotificationData? {
            return try {
                NotificationData(
                    notificationType = data["notification_type"] ?: return null,
                    launchId = data["launch_id"] ?: return null,
                    launchUuid = data["launch_uuid"] ?: return null,
                    launchName = data["launch_name"] ?: return null,
                    launchImage = data["launch_image"],
                    launchNet = data["launch_net"] ?: return null,
                    launchLocation = data["launch_location"] ?: return null,
                    webcast = data["webcast"] ?: "false",
                    webcastLive = data["webcast_live"],
                    agencyId = data["agency_id"] ?: return null,
                    locationId = data["location_id"] ?: return null
                )
            } catch (e: Exception) {
                log.e(e) { "❌ Failed to parse notification data: ${e.message}" }
                null
            }
        }
    }

    /**
     * Check if webcast is available
     */
    fun hasWebcast(): Boolean {
        return webcast.lowercase() == "true"
    }

    /**
     * Check if webcast is live
     */
    fun isWebcastLive(): Boolean {
        return webcastLive?.lowercase() == "true"
    }

    /**
     * Get agency ID as integer
     */
    fun getAgencyIdInt(): Int? {
        return agencyId.toIntOrNull()
    }

    /**
     * Get location ID as integer
     */
    fun getLocationIdInt(): Int? {
        return locationId.toIntOrNull()
    }
}

/**
 * Notification filter - decides if a notification should be shown based on user preferences
 *
 * This is a PLATFORM-AGNOSTIC filter that works on both Android and iOS.
 * Call this from any platform's notification handler before showing notifications.
 *
 * Usage on Android:
 * ```kotlin
 * val data = NotificationData.fromMap(remoteMessage.data)
 * val settings = notificationStateStorage.getState()
 * if (NotificationFilter.shouldShowNotification(data, settings)) {
 *     showNotification(...)
 * }
 * ```
 *
 * Usage on iOS (Swift):
 * ```swift
 * // Get data from notification userInfo
 * let dataDict = userInfo as? [String: String]
 * // Convert to Kotlin map and filter
 * if NotificationFilter.shared.shouldShowFromMap(dataDict, state) {
 *     // Show notification
 * }
 * ```
 */
object NotificationFilter {
    private val log = logger()

    /**
     * Convenience method for filtering from raw data map
     * Useful for iOS Swift interop
     */
    fun shouldShowFromMap(
        dataMap: Map<String, String>,
        state: NotificationState
    ): Boolean {
        val data = NotificationData.fromMap(dataMap) ?: run {
            log.w { "⚠️ Failed to parse notification data, suppressing notification" }
            return false
        }
        return shouldShowNotification(data, state)
    }

    /**
     * Check if notification should be shown based on user preferences
     *
     * @param data Parsed notification data from server
     * @param state Current notification settings/preferences
     * @return true if notification should be shown, false if it should be suppressed
     */
    fun shouldShowNotification(
        data: NotificationData,
        state: NotificationState
    ): Boolean {
        log.d { "Evaluating notification - Type: ${data.notificationType}, Agency: ${data.agencyId}, Location: ${data.locationId}, Launch: ${data.launchName}, Webcast: ${data.webcast}" }

        // 1. Check if notifications are globally enabled
        if (!state.enableNotifications) {
            log.d { "🔇 BLOCKED: Notifications disabled globally" }
            return false
        }

        // 2. Check webcast-only filter
        val webcastOnly = state.isTopicEnabled(NotificationTopic.WEBCAST_ONLY)
        if (webcastOnly && !data.hasWebcast()) {
            log.d { "🔇 BLOCKED: Webcast-only filter enabled, launch has no webcast" }
            return false
        }

        // 3. Check notification type (timing) settings
        val notificationTypeEnabled = isNotificationTypeEnabled(data.notificationType, state)
        if (!notificationTypeEnabled) {
            log.d { "🔇 BLOCKED: Notification type '${data.notificationType}' is disabled" }
            return false
        }

        // 4. Check if following all launches
        // When followAllLaunches is enabled, show ALL notifications (skip agency/location filtering)
        // Note: useStrictMatching is ignored when followAllLaunches is enabled
        if (state.followAllLaunches) {
            log.d { "✅ ALLOWED: Following all launches (agency/location filtering skipped, strict matching ignored)" }
            return true
        }

        // 5. Check if both filters are empty (block everything)
        if (state.subscribedAgencies.isEmpty() && state.subscribedLocations.isEmpty()) {
            log.d { "🔇 BLOCKED: No agencies or locations subscribed (both filters empty)" }
            return false
        }

        // 6. Determine which filters are active
        val hasAgencyFilter = state.subscribedAgencies.isNotEmpty()
        val hasLocationFilter = state.subscribedLocations.isNotEmpty()

        log.v { "Agency filter: $hasAgencyFilter, Location filter: $hasLocationFilter" }

        // 7. Check agency filter (only if filter is active)
        val agencyMatch = if (hasAgencyFilter) {
            state.subscribedAgencies.contains(data.agencyId)
        } else {
            true // No agency filter, so don't block based on agency
        }

        // 8. Check location filter (only if filter is active)
        // Special cases:
        // - locationId="0" (Other) in subscribed list acts as a wildcard - matches ANY location
        // - Grouped locations (e.g., FLORIDA) may have additionalIds that should also match
        val locationMatch = if (hasLocationFilter) {
            // Direct match
            if (state.subscribedLocations.contains(data.locationId)) {
                true
            }
            // Wildcard match
            else if (state.subscribedLocations.contains("0")) {
                true
            }
            // Check if notification's location is in any subscribed location's additionalIds
            else {
                val allLocations = NotificationLocation.getAll()
                state.subscribedLocations.any { subscribedLocationId ->
                    val location = allLocations.find { it.id.toString() == subscribedLocationId }
                    location?.getAllIds()?.any { it.toString() == data.locationId } ?: false
                }
            }
        } else {
            true // No location filter, so don't block based on location
        }

        // 9. Apply strict vs flexible matching logic
        val shouldShow = if (state.useStrictMatching) {
            // Strict mode: BOTH agency AND location must match
            // If either filter is inactive (empty), we can't satisfy "BOTH" requirement
            if (!hasAgencyFilter || !hasLocationFilter) {
                log.d { "🔇 BLOCKED: Strict matching requires BOTH agency AND location filters to be active" }
                false
            } else {
                val result = agencyMatch && locationMatch
                if (!result) {
                    log.d { "🔇 BLOCKED: Strict matching - agency: $agencyMatch, location: $locationMatch (need BOTH)" }
                } else {
                    log.d { "✅ ALLOWED: Strict matching - agency: $agencyMatch, location: $locationMatch (both match)" }
                }
                result
            }
        } else {
            // Flexible mode: Use OR logic when both filters active, otherwise use the active filter
            val result = if (hasAgencyFilter && hasLocationFilter) {
                // Both filters active: EITHER must match
                agencyMatch || locationMatch
            } else if (hasAgencyFilter) {
                // Only agency filter active: must match agency
                agencyMatch
            } else {
                // Only location filter active: must match location
                locationMatch
            }

            if (!result) {
                log.d { "🔇 BLOCKED: Flexible matching - agency: $agencyMatch (${if (hasAgencyFilter) "filtered" else "any"}), location: $locationMatch (${if (hasLocationFilter) "filtered" else "any"})" }
            } else {
                log.d { "✅ ALLOWED: Flexible matching - agency: $agencyMatch (${if (hasAgencyFilter) "filtered" else "any"}), location: $locationMatch (${if (hasLocationFilter) "filtered" else "any"})" }
            }
            result
        }

        log.i { "NotificationFilter result: $shouldShow" }
        return shouldShow
    }

    /**
     * Check if the notification type (timing) is enabled by user
     */
    private fun isNotificationTypeEnabled(type: String, state: NotificationState): Boolean {
        return when (type) {
            "netstampChanged" -> state.isTopicEnabled(NotificationTopic.NETSTAMP_CHANGED)
            "twentyFourHour" -> state.isTopicEnabled(NotificationTopic.TWENTY_FOUR_HOUR)
            "oneHour" -> state.isTopicEnabled(NotificationTopic.ONE_HOUR)
            "tenMinutes" -> state.isTopicEnabled(NotificationTopic.TEN_MINUTES)
            "oneMinute" -> state.isTopicEnabled(NotificationTopic.ONE_MINUTE)
            "inFlight" -> state.isTopicEnabled(NotificationTopic.IN_FLIGHT)
            "success" -> state.isTopicEnabled(NotificationTopic.SUCCESS)
            "event" -> state.isTopicEnabled(NotificationTopic.EVENTS)
            else -> {
                log.w { "⚠️ Unknown notification type: $type, allowing by default" }
                true // Unknown types are allowed by default
            }
        }
    }
}
