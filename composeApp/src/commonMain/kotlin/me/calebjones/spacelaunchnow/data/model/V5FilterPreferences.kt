package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

/**
 * V5 Filter Preferences
 *
 * @deprecated This class is no longer used by V5 notification filtering.
 * V5 now reuses NotificationState (String-based IDs) instead of maintaining
 * a separate Int-based filter system. This class is kept for backward
 * compatibility only and will be removed in a future version.
 *
 * See V5NotificationFilter.kt for current implementation.
 *
 * User preferences for V5 notification filtering.
 * These preferences are evaluated client-side to determine
 * whether a V5 notification should be displayed.
 *
 * Filter Logic:
 * - null = follow all (no filtering for that category)
 * - empty set = block all (filter enabled but nothing selected)
 * - non-empty set = filter to only matching items
 *
 * Strict vs Flexible Matching:
 * - Strict (AND): ALL active filters must match
 * - Flexible (OR): ANY active filter can match (default)
 */
@Deprecated(
    message = "V5 now uses NotificationState directly instead of V5FilterPreferences. " +
        "This class is kept for backward compatibility only.",
    replaceWith = ReplaceWith("NotificationState"),
    level = DeprecationLevel.WARNING
)
@Serializable
data class V5FilterPreferences(
    // Master enable/disable
    val enableNotifications: Boolean = true,

    // Notification type toggles (which notification timings are enabled)
    val enabledNotificationTypes: Set<String> = setOf(
        "tenMinutes",
        "oneHour",
        "twentyFourHour",
        "netstampChanged",
        "inFlight",
        "success",
        "failure",
        "webcastLive"
    ),

    // V5 Filter Categories
    // null = follow all (no filtering), empty = block all, non-empty = filter
    val subscribedLspIds: Set<Int>? = null,           // null = all LSPs
    val subscribedLocationIds: Set<Int>? = null,      // null = all locations
    val subscribedProgramIds: Set<Int>? = null,       // null = all programs
    val subscribedOrbitIds: Set<Int>? = null,         // null = all orbits
    val subscribedMissionTypeIds: Set<Int>? = null,   // null = all mission types
    val subscribedLauncherFamilyIds: Set<Int>? = null,// null = all launcher families

    // Filter mode
    val useStrictMatching: Boolean = false,           // AND vs OR logic

    // Special filters
    val webcastOnly: Boolean = false,                 // Only notifications with webcast

    // Migration tracking
    val hasV5Migration: Boolean = false               // Has migrated from V4
) {
    companion object {
        /**
         * Default preferences - all notifications enabled, no filtering
         */
        val DEFAULT = V5FilterPreferences()

        /**
         * Create preferences that block all notifications
         */
        fun blockedAll() = V5FilterPreferences(
            enableNotifications = false
        )

        /**
         * Create preferences with specific LSP filtering
         */
        fun withLspFilter(lspIds: Set<Int>) = V5FilterPreferences(
            subscribedLspIds = lspIds
        )
    }

    /**
     * Check if any category-based filtering is active
     */
    fun hasActiveFilters(): Boolean {
        return subscribedLspIds != null ||
            subscribedLocationIds != null ||
            subscribedProgramIds != null ||
            subscribedOrbitIds != null ||
            subscribedMissionTypeIds != null ||
            subscribedLauncherFamilyIds != null
    }

    /**
     * Check if a specific notification type is enabled
     */
    fun isNotificationTypeEnabled(type: String): Boolean {
        return type in enabledNotificationTypes
    }

    /**
     * Create a copy with updated LSP subscriptions
     */
    fun withLspIds(lspIds: Set<Int>?): V5FilterPreferences {
        return copy(subscribedLspIds = lspIds)
    }

    /**
     * Create a copy with updated location subscriptions
     */
    fun withLocationIds(locationIds: Set<Int>?): V5FilterPreferences {
        return copy(subscribedLocationIds = locationIds)
    }

    /**
     * Create a copy with updated program subscriptions
     */
    fun withProgramIds(programIds: Set<Int>?): V5FilterPreferences {
        return copy(subscribedProgramIds = programIds)
    }

    /**
     * Create a copy with migration complete flag
     */
    fun withMigrationComplete(): V5FilterPreferences {
        return copy(hasV5Migration = true)
    }

    /**
     * Create a copy with an updated notification type enabled state
     */
    fun withNotificationTypeEnabled(type: String, enabled: Boolean): V5FilterPreferences {
        val updatedTypes = if (enabled) {
            enabledNotificationTypes + type
        } else {
            enabledNotificationTypes - type
        }
        return copy(enabledNotificationTypes = updatedTypes)
    }

    /**
     * Convert to a debug-friendly string
     */
    fun toDebugString(): String {
        return buildString {
            append("V5Prefs(")
            append("enabled=$enableNotifications, ")
            append("strict=$useStrictMatching, ")
            append("webcastOnly=$webcastOnly, ")
            if (subscribedLspIds != null) append("lsps=${subscribedLspIds.size}, ")
            if (subscribedLocationIds != null) append("locations=${subscribedLocationIds.size}, ")
            if (subscribedProgramIds != null) append("programs=${subscribedProgramIds.size}, ")
            append("migrated=$hasV5Migration)")
        }
    }
}
