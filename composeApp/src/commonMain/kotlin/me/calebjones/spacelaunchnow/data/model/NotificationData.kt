package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

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
    val agencyId: String,                 // e.g., "121" for SpaceX
    val locationId: String                // e.g., "143" for Texas
) {
    companion object {
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
                    agencyId = data["agency_id"] ?: return null,
                    locationId = data["location_id"] ?: return null
                )
            } catch (e: Exception) {
                println("❌ Failed to parse notification data: ${e.message}")
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
 * val settings = notificationPreferences.getNotificationSettings()
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
    
    /**
     * Convenience method for filtering from raw data map
     * Useful for iOS Swift interop
     */
    fun shouldShowFromMap(
        dataMap: Map<String, String>,
        state: NotificationState
    ): Boolean {
        val data = NotificationData.fromMap(dataMap) ?: run {
            println("⚠️ Failed to parse notification data, suppressing notification")
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
        println("=== NotificationFilter: Evaluating notification ===")
        println("Type: ${data.notificationType}, Agency: ${data.agencyId}, Location: ${data.locationId}")
        println("Launch: ${data.launchName}, Webcast: ${data.webcast}")
        
        // 1. Check if notifications are globally enabled
        if (!state.enableNotifications) {
            println("🔇 BLOCKED: Notifications disabled globally")
            return false
        }

        // 2. Check webcast-only filter
        val webcastOnly = state.isTopicEnabled(NotificationTopic.WEBCAST_ONLY)
        if (webcastOnly && !data.hasWebcast()) {
            println("🔇 BLOCKED: Webcast-only filter enabled, launch has no webcast")
            return false
        }

        // 3. Check notification type (timing) settings
        val notificationTypeEnabled = isNotificationTypeEnabled(data.notificationType, state)
        if (!notificationTypeEnabled) {
            println("🔇 BLOCKED: Notification type '${data.notificationType}' is disabled")
            return false
        }

        // 4. Check if following all launches
        // When followAllLaunches is enabled, show ALL notifications (skip agency/location filtering)
        // Note: useStrictMatching is ignored when followAllLaunches is enabled
        if (state.followAllLaunches) {
            println("✅ ALLOWED: Following all launches (agency/location filtering skipped, strict matching ignored)")
            return true
        }

        // 5. Check agency filter
        // Empty set means "don't filter by agency" (match all agencies)
        val agencyMatch = state.subscribedAgencies.isEmpty() || 
                         state.subscribedAgencies.contains(data.agencyId)
        
        // 6. Check location filter
        // Empty set means "don't filter by location" (match all locations)
        // Special case: locationId="0" (Other) acts as a wildcard - matches ANY location
        val locationMatch = state.subscribedLocations.isEmpty() ||
                            state.subscribedLocations.contains(data.locationId) || 
                            state.subscribedLocations.contains("0")

        // 7. Apply strict matching logic
        // Strict: BOTH agency AND location must match (agencyMatch && locationMatch)
        //         BUT if either set is empty, that criterion is ignored
        // Flexible: EITHER agency OR location must match (agencyMatch || locationMatch)
        //           If both sets are empty, show nothing (need at least one filter)
        val shouldShow = if (state.useStrictMatching) {
            // Strict mode with empty sets:
            // - If both empty: block (need at least one filter)
            // - If one empty: only check the non-empty one
            // - If both non-empty: need both to match
            if (state.subscribedAgencies.isEmpty() && state.subscribedLocations.isEmpty()) {
                println("🔇 BLOCKED: Strict matching with no filters (need at least one agency or location)")
                false
            } else {
                val result = agencyMatch && locationMatch
                if (!result) {
                    println("🔇 BLOCKED: Strict matching - agency: $agencyMatch (${if (state.subscribedAgencies.isEmpty()) "empty" else "filtered"}), location: $locationMatch (${if (state.subscribedLocations.isEmpty()) "empty" else "filtered"}) (need BOTH)")
                } else {
                    println("✅ ALLOWED: Strict matching - agency: $agencyMatch, location: $locationMatch (both match)")
                }
                result
            }
        } else {
            // Flexible mode with empty sets:
            // - If both empty: block (need at least one filter)
            // - Otherwise: at least one must match
            if (state.subscribedAgencies.isEmpty() && state.subscribedLocations.isEmpty()) {
                println("🔇 BLOCKED: Flexible matching with no filters (need at least one agency or location)")
                false
            } else {
                val result = agencyMatch || locationMatch
                if (!result) {
                    println("🔇 BLOCKED: Flexible matching - agency: $agencyMatch, location: $locationMatch (need at least ONE)")
                } else {
                    println("✅ ALLOWED: Flexible matching - agency: $agencyMatch (${if (state.subscribedAgencies.isEmpty()) "empty" else "filtered"}), location: $locationMatch (${if (state.subscribedLocations.isEmpty()) "empty" else "filtered"}) (at least one matches)")
                }
                result
            }
        }

        println("=== NotificationFilter: Result = $shouldShow ===")
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
                println("⚠️ Unknown notification type: $type, allowing by default")
                true // Unknown types are allowed by default
            }
        }
    }
}
