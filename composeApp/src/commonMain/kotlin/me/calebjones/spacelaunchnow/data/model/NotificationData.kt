package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Notification data structure for v4 notifications
 * Server sends this data structure and client filters based on user preferences
 * 
 * For V5 notifications, use V5NotificationPayload instead.
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
         * Check if payload is V5 format (has lsp_id field)
         * Use this to determine which parser to use.
         */
        fun isV5Payload(data: Map<String, String>): Boolean {
            return V5NotificationPayload.isV5Payload(data)
        }
        
        /**
         * Parse notification data from FCM data payload (V4 format)
         * For V5 payloads, use V5NotificationPayload.fromMap() instead.
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
        
        /**
         * Parse either V4 or V5 payload, returning a unified wrapper
         * Useful for backward compatibility during V4→V5 migration
         */
        fun parseAny(data: Map<String, String>): ParsedNotification? {
            return if (isV5Payload(data)) {
                V5NotificationPayload.fromMap(data)?.let { ParsedNotification.V5(it) }
            } else {
                fromMap(data)?.let { ParsedNotification.V4(it) }
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
        log.d("Agency match: $agencyMatch")

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
        log.d("Location match: $locationMatch")

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

/**
 * Unified wrapper for parsed notification payloads
 * Supports both V4 (NotificationData) and V5 (V5NotificationPayload) formats
 */
sealed class ParsedNotification {
    /**
     * V4 notification payload (legacy format)
     */
    data class V4(val data: NotificationData) : ParsedNotification() {
        override val notificationType: String get() = data.notificationType
        override val launchUuid: String get() = data.launchUuid
        override val launchName: String get() = data.launchName
        override val launchImage: String? get() = data.launchImage
        override val launchNet: String get() = data.launchNet
        override val hasWebcast: Boolean get() = data.hasWebcast()
    }

    /**
     * V5 notification payload (new format with extended filtering)
     */
    data class V5(val payload: V5NotificationPayload) : ParsedNotification() {
        override val notificationType: String get() = payload.notificationType
        override val launchUuid: String get() = payload.launchUuid
        override val launchName: String get() = payload.launchName
        override val launchImage: String? get() = payload.launchImage
        override val launchNet: String get() = payload.launchNet
        override val hasWebcast: Boolean get() = payload.webcast
    }

    /** Notification type (e.g., "tenMinutes", "oneHour") */
    abstract val notificationType: String
    /** Launch UUID for deep linking */
    abstract val launchUuid: String
    /** Launch display name */
    abstract val launchName: String
    /** Launch image URL (optional) */
    abstract val launchImage: String?
    /** Launch NET (ISO 8601) */
    abstract val launchNet: String
    /** Whether launch has webcast */
    abstract val hasWebcast: Boolean

    /** Check if this is a V5 payload */
    fun isV5(): Boolean = this is V5

    /** Get V5 payload if this is a V5 notification, null otherwise */
    fun asV5(): V5NotificationPayload? = (this as? V5)?.payload

    /** Get V4 data if this is a V4 notification, null otherwise */
    fun asV4(): NotificationData? = (this as? V4)?.data
}
