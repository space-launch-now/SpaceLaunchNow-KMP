package me.calebjones.spacelaunchnow.data.model

import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * V5 Notification Filter
 *
 * Filter logic for V5 notifications with extended filtering capabilities.
 * Evaluates V5 notification payloads against user preferences to determine
 * whether notifications should be displayed.
 *
 * Filter Categories:
 * - LSP (Launch Service Provider)
 * - Location
 * - Program
 * - Orbit
 * - Mission Type
 * - Launcher Family
 *
 * Filter Logic:
 * - null preference = no filtering (follow all)
 * - empty set = block all
 * - non-empty set = filter to matching items
 *
 * Matching Modes:
 * - Strict (AND): ALL active filter categories must match
 * - Flexible (OR): ANY active filter category can match (default)
 */
object V5NotificationFilter {
    private val log = logger()

    /**
     * Determine if V5 notification should be shown based on user preferences
     *
     * @param payload The V5 notification payload
     * @param preferences User filter preferences
     * @return FilterResult indicating whether to show or block
     */
    fun shouldShow(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): FilterResult {
        log.d { "🔍 V5Filter: Evaluating ${payload.toDebugString()}" }
        log.d { "🔍 V5Filter: Preferences ${preferences.toDebugString()}" }

        // 1. Check master enable
        if (!preferences.enableNotifications) {
            log.d { "🔇 V5 BLOCKED: ${FilterResult.Companion.Reasons.NOTIFICATIONS_DISABLED}" }
            return FilterResult.blocked(FilterResult.Companion.Reasons.NOTIFICATIONS_DISABLED)
        }

        // 2. Check notification type
        if (payload.notificationType !in preferences.enabledNotificationTypes) {
            val reason = "${FilterResult.Companion.Reasons.NOTIFICATION_TYPE_DISABLED}: '${payload.notificationType}'"
            log.d { "🔇 V5 BLOCKED: $reason" }
            return FilterResult.blocked(reason)
        }

        // 3. Check webcast-only filter
        if (preferences.webcastOnly && !payload.webcast) {
            log.d { "🔇 V5 BLOCKED: ${FilterResult.Companion.Reasons.WEBCAST_ONLY_NO_WEBCAST}" }
            return FilterResult.blocked(FilterResult.Companion.Reasons.WEBCAST_ONLY_NO_WEBCAST)
        }

        // 4. Check filter categories
        val filterResults = mutableListOf<CategoryFilterResult>()

        // LSP filter
        checkLspFilter(payload, preferences)?.let { filterResults.add(it) }

        // Location filter
        checkLocationFilter(payload, preferences)?.let { filterResults.add(it) }

        // Program filter
        checkProgramFilter(payload, preferences)?.let { filterResults.add(it) }

        // Orbit filter
        checkOrbitFilter(payload, preferences)?.let { filterResults.add(it) }

        // Mission type filter
        checkMissionTypeFilter(payload, preferences)?.let { filterResults.add(it) }

        // Launcher family filter
        checkLauncherFamilyFilter(payload, preferences)?.let { filterResults.add(it) }

        // 5. If no filters are active, allow
        if (filterResults.isEmpty()) {
            log.d { "✅ V5 ALLOWED: No category filters active" }
            return FilterResult.Allowed
        }

        // 6. Check for immediate blocks (empty subscription sets)
        val immediateBlock = filterResults.find { it.immediateBlock != null }
        if (immediateBlock != null) {
            log.d { "🔇 V5 BLOCKED: ${immediateBlock.immediateBlock}" }
            return FilterResult.blocked(immediateBlock.immediateBlock!!)
        }

        // 7. Apply filter logic (strict = AND, flexible = OR)
        val matches = filterResults.map { it.matches }
        val passed = if (preferences.useStrictMatching) {
            // Strict mode: ALL must match
            matches.all { it == true }
        } else {
            // Flexible mode: ANY can match (null counts as no preference, treat as pass)
            matches.any { it == true } || matches.all { it == null }
        }

        return if (passed) {
            log.d { "✅ V5 ALLOWED: Filter passed (strict=${preferences.useStrictMatching}, results=$filterResults)" }
            FilterResult.Allowed
        } else {
            val reason = "${FilterResult.Companion.Reasons.FILTER_CRITERIA_NOT_MET} (strict=${preferences.useStrictMatching})"
            log.d { "🔇 V5 BLOCKED: $reason" }
            FilterResult.blocked(reason)
        }
    }

    // MARK: - Category Filter Checks

    /**
     * Check LSP filter category
     * @return null if no filter active, CategoryFilterResult otherwise
     */
    private fun checkLspFilter(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): CategoryFilterResult? {
        val subscribedIds = preferences.subscribedLspIds ?: return null

        // Empty set = block all
        if (subscribedIds.isEmpty()) {
            return CategoryFilterResult(
                category = "LSP",
                matches = false,
                immediateBlock = FilterResult.Companion.Reasons.NO_LSPS_SUBSCRIBED
            )
        }

        // Check if payload LSP matches
        val payloadLspId = payload.lspId
        val matches = payloadLspId != null && payloadLspId in subscribedIds
        log.v { "  LSP filter: payload=$payloadLspId, subscribed=$subscribedIds, matches=$matches" }

        return CategoryFilterResult(category = "LSP", matches = matches)
    }

    /**
     * Check location filter category
     */
    private fun checkLocationFilter(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): CategoryFilterResult? {
        val subscribedIds = preferences.subscribedLocationIds ?: return null

        if (subscribedIds.isEmpty()) {
            return CategoryFilterResult(
                category = "Location",
                matches = false,
                immediateBlock = FilterResult.Companion.Reasons.NO_LOCATIONS_SUBSCRIBED
            )
        }

        val payloadLocationId = payload.locationId
        val matches = payloadLocationId != null && payloadLocationId in subscribedIds
        log.v { "  Location filter: payload=$payloadLocationId, subscribed=$subscribedIds, matches=$matches" }

        return CategoryFilterResult(category = "Location", matches = matches)
    }

    /**
     * Check program filter category
     * Programs use ANY match (if any program ID matches, it passes)
     */
    private fun checkProgramFilter(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): CategoryFilterResult? {
        val subscribedIds = preferences.subscribedProgramIds ?: return null

        if (subscribedIds.isEmpty()) {
            return CategoryFilterResult(
                category = "Program",
                matches = false,
                immediateBlock = FilterResult.Companion.Reasons.NO_PROGRAMS_SUBSCRIBED
            )
        }

        val payloadProgramIds = payload.programIds
        val matches = payloadProgramIds.any { it in subscribedIds }
        log.v { "  Program filter: payload=$payloadProgramIds, subscribed=$subscribedIds, matches=$matches" }

        return CategoryFilterResult(category = "Program", matches = matches)
    }

    /**
     * Check orbit filter category
     */
    private fun checkOrbitFilter(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): CategoryFilterResult? {
        val subscribedIds = preferences.subscribedOrbitIds ?: return null

        if (subscribedIds.isEmpty()) {
            return CategoryFilterResult(
                category = "Orbit",
                matches = false,
                immediateBlock = FilterResult.Companion.Reasons.NO_ORBITS_SUBSCRIBED
            )
        }

        val payloadOrbitId = payload.orbitId
        // If payload has no orbit ID, treat as null match (not failed)
        val matches = if (payloadOrbitId != null) payloadOrbitId in subscribedIds else null
        log.v { "  Orbit filter: payload=$payloadOrbitId, subscribed=$subscribedIds, matches=$matches" }

        return CategoryFilterResult(category = "Orbit", matches = matches)
    }

    /**
     * Check mission type filter category
     */
    private fun checkMissionTypeFilter(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): CategoryFilterResult? {
        val subscribedIds = preferences.subscribedMissionTypeIds ?: return null

        if (subscribedIds.isEmpty()) {
            return CategoryFilterResult(
                category = "MissionType",
                matches = false,
                immediateBlock = FilterResult.Companion.Reasons.NO_MISSION_TYPES_SUBSCRIBED
            )
        }

        val payloadMissionTypeId = payload.missionTypeId
        val matches = if (payloadMissionTypeId != null) payloadMissionTypeId in subscribedIds else null
        log.v { "  MissionType filter: payload=$payloadMissionTypeId, subscribed=$subscribedIds, matches=$matches" }

        return CategoryFilterResult(category = "MissionType", matches = matches)
    }

    /**
     * Check launcher family filter category
     */
    private fun checkLauncherFamilyFilter(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): CategoryFilterResult? {
        val subscribedIds = preferences.subscribedLauncherFamilyIds ?: return null

        if (subscribedIds.isEmpty()) {
            return CategoryFilterResult(
                category = "LauncherFamily",
                matches = false,
                immediateBlock = FilterResult.Companion.Reasons.NO_LAUNCHER_FAMILIES_SUBSCRIBED
            )
        }

        val payloadLauncherFamilyId = payload.launcherFamilyId
        val matches = if (payloadLauncherFamilyId != null) payloadLauncherFamilyId in subscribedIds else null
        log.v { "  LauncherFamily filter: payload=$payloadLauncherFamilyId, subscribed=$subscribedIds, matches=$matches" }

        return CategoryFilterResult(category = "LauncherFamily", matches = matches)
    }

    // MARK: - Convenience Methods

    /**
     * Convenience method for filtering from raw data map
     * Returns FilterResult for detailed reason tracking
     */
    fun shouldShowFromMap(
        dataMap: Map<String, String>,
        preferences: V5FilterPreferences
    ): FilterResult {
        val payload = V5NotificationPayload.fromMap(dataMap) ?: run {
            log.w { "⚠️ Failed to parse V5 notification payload, blocking notification" }
            return FilterResult.blocked("Failed to parse V5 notification payload")
        }
        return shouldShow(payload, preferences)
    }

    /**
     * Simple boolean convenience method
     */
    fun shouldShowNotification(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): Boolean {
        return shouldShow(payload, preferences).shouldShow()
    }
}

/**
 * Internal helper class for tracking category filter results
 */
private data class CategoryFilterResult(
    val category: String,
    val matches: Boolean?,           // true = matched, false = didn't match, null = payload missing this ID
    val immediateBlock: String? = null  // If non-null, block immediately with this reason
) {
    override fun toString(): String = "$category:$matches"
}
