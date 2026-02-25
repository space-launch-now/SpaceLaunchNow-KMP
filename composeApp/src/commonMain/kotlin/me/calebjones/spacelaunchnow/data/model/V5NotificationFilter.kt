package me.calebjones.spacelaunchnow.data.model

import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * V5 Notification Filter (Simplified)
 *
 * Simplified filter logic for V5 notifications that reuses NotificationState
 * from V4. This eliminates the complex Int-based filtering system and dual
 * state management, fixing the bug where users received all notifications.
 *
 * Key Changes from Previous Implementation:
 * - Uses NotificationState (String-based IDs) instead of V5FilterPreferences (Int-based)
 * - Simple String membership checks: `"121" in state.subscribedAgencies`
 * - No String→Int conversion overhead
 * - Matches V4 filtering pattern (proven to work)
 *
 * Filter Logic:
 * - Master enable check: If notifications disabled, block all
 * - Follow all bypass: If followAllLaunches enabled, allow all
 * - Empty filters: If both agency and location filters empty, block all
 * - Flexible mode (OR): Agency OR location must match
 * - Strict mode (AND): Agency AND location must match
 * - Webcast-only: Optionally filter to launches with webcasts
 *
 * Example:
 * ```kotlin
 * val payload = V5NotificationPayload.fromMap(fcmData)
 * val state = notificationStateStorage.getState()
 * val result = V5NotificationFilter.shouldShow(payload, state)
 * if (result is FilterResult.Allowed) {
 *     showNotification(payload)
 * }
 * ```
 */
object V5NotificationFilter {
    private val log by lazy { logger() }

    /**
     * Determine if V5 notification should be shown based on user state
     *
     * @param payload The V5 notification payload (with String IDs)
     * @param state Current notification state (from NotificationState)
     * @return FilterResult indicating whether to show or block, with reason
     */
    fun shouldShow(
        payload: V5NotificationPayload,
        state: NotificationState
    ): FilterResult {
        log.d { "🔍 V5Filter: Evaluating ${payload.toDebugString()}" }
        log.d { "🔍 V5Filter: Subscriptions - agencies=${state.subscribedAgencies}, locations=${state.subscribedLocations}" }

        // 1. Check if notifications are globally enabled
        if (!state.enableNotifications) {
            log.d { "🔇 V5 BLOCKED: ${FilterResult.Companion.Reasons.NOTIFICATIONS_DISABLED}" }
            return FilterResult.blocked(FilterResult.Companion.Reasons.NOTIFICATIONS_DISABLED)
        }

        // 2. Check webcast-only filter
        val webcastOnly = state.isTopicEnabled(NotificationTopic.WEBCAST_ONLY)
        if (webcastOnly && !payload.webcast) {
            log.d { "🔇 V5 BLOCKED: ${FilterResult.Companion.Reasons.WEBCAST_ONLY_NO_WEBCAST}" }
            return FilterResult.blocked(FilterResult.Companion.Reasons.WEBCAST_ONLY_NO_WEBCAST)
        }

        // 3. Check notification type (timing) settings
        // TODO: Implement notification type checking if needed
        // For now, assume all notification types are enabled (V4 doesn't check this for V5 either)

        // 4. Check if following all launches
        // When followAllLaunches is enabled, show ALL notifications (skip agency/location filtering)
        if (state.followAllLaunches) {
            log.d { "✅ V5 ALLOWED: Following all launches (filters bypassed)" }
            return FilterResult.Allowed
        }

        // 5. Check if both filters are empty (block everything)
        if (state.subscribedAgencies.isEmpty() && state.subscribedLocations.isEmpty()) {
            log.d { "🔇 V5 BLOCKED: ${FilterResult.Companion.Reasons.NO_AGENCIES_OR_LOCATIONS_SUBSCRIBED}" }
            return FilterResult.blocked(FilterResult.Companion.Reasons.NO_AGENCIES_OR_LOCATIONS_SUBSCRIBED)
        }

        // 6. Determine which filters are active
        val hasAgencyFilter = state.subscribedAgencies.isNotEmpty()
        val hasLocationFilter = state.subscribedLocations.isNotEmpty()

        log.v { "Agency filter active: $hasAgencyFilter, Location filter active: $hasLocationFilter" }

        // 7. Check agency/LSP filter (V5 uses lspId instead of agencyId)
        val agencyMatch = if (hasAgencyFilter) {
            val matched = payload.lspId != null && state.subscribedAgencies.contains(payload.lspId)
            log.d { "Agency/LSP match: $matched (payload=${payload.lspId}, subscribed=${state.subscribedAgencies.take(5)}...)" }
            matched
        } else {
            true // No agency filter, so don't block based on agency
        }

        // 8. Check location filter
        val locationMatch = if (hasLocationFilter) {
            // Direct match
            val matched = if (payload.locationId != null && state.subscribedLocations.contains(payload.locationId)) {
                true
            }
            // Wildcard match (locationId="0" means "Other" / any location)
            else if (state.subscribedLocations.contains("0")) {
                true
            }
            // Check if notification's location is in any subscribed location's additionalIds
            // (for grouped locations like FLORIDA which includes multiple location IDs)
            else {
                val allLocations = NotificationLocation.getAll()
                state.subscribedLocations.any { subscribedLocationId ->
                    val location = allLocations.find { it.id.toString() == subscribedLocationId }
                    location?.getAllIds()?.any { it.toString() == payload.locationId } ?: false
                }
            }
            log.d { "Location match: $matched (payload=${payload.locationId}, subscribed=${state.subscribedLocations.take(5)}...)" }
            matched
        } else {
            true // No location filter, so don't block based on location
        }

        // 9. Apply strict vs flexible matching logic
        val shouldShow = if (state.useStrictMatching) {
            // Strict mode: BOTH agency AND location must match
            // If either filter is inactive (empty), we can't satisfy "BOTH" requirement
            if (!hasAgencyFilter || !hasLocationFilter) {
                log.d { "🔇 V5 BLOCKED: Strict matching requires BOTH agency AND location filters to be active" }
                false
            } else {
                val result = agencyMatch && locationMatch
                if (!result) {
                    log.d { "🔇 V5 BLOCKED: Strict matching - agency: $agencyMatch, location: $locationMatch (need BOTH)" }
                } else {
                    log.d { "✅ V5 ALLOWED: Strict matching - agency: $agencyMatch, location: $locationMatch (both match)" }
                }
                result
            }
        } else {
            // Flexible mode: EITHER agency OR location can match
            val result = agencyMatch || locationMatch
            if (!result) {
                log.d { "🔇 V5 BLOCKED: Flexible matching - agency: $agencyMatch, location: $locationMatch (need at least ONE)" }
            } else {
                log.d { "✅ V5 ALLOWED: Flexible matching - agency: $agencyMatch, location: $locationMatch (at least one matches)" }
            }
            result
        }

        return if (shouldShow) {
            FilterResult.Allowed
        } else {
            FilterResult.blocked("${FilterResult.Companion.Reasons.FILTER_CRITERIA_NOT_MET} (strict=${state.useStrictMatching})")
        }
    }

    /**
     * Convenience method for filtering from raw data map
     * Returns FilterResult for detailed reason tracking
     */
    fun shouldShowFromMap(
        dataMap: Map<String, String>,
        state: NotificationState
    ): FilterResult {
        val payload = V5NotificationPayload.fromMap(dataMap) ?: run {
            log.w { "⚠️ Failed to parse V5 notification payload, blocking notification" }
            return FilterResult.blocked("Failed to parse V5 notification payload")
        }
        return shouldShow(payload, state)
    }

    /**
     * Simple boolean convenience method
     */
    fun shouldShowNotification(
        payload: V5NotificationPayload,
        state: NotificationState
    ): Boolean {
        return shouldShow(payload, state).shouldShow()
    }
}
