package me.calebjones.spacelaunchnow.data.model

/**
 * Filter Result
 *
 * Represents the outcome of evaluating a notification against filter preferences.
 * Provides clear indication of whether notification should be shown and why.
 */
sealed class FilterResult {
    /**
     * Notification passed all filters and should be displayed
     */
    data object Allowed : FilterResult() {
        override fun toString(): String = "FilterResult.Allowed"
    }

    /**
     * Notification was blocked by a filter
     *
     * @param reason Human-readable explanation of why the notification was blocked
     */
    data class Blocked(val reason: String) : FilterResult() {
        override fun toString(): String = "FilterResult.Blocked(reason=$reason)"
    }

    /**
     * Check if the notification should be shown
     */
    fun shouldShow(): Boolean = this is Allowed

    /**
     * Get the block reason if blocked, null if allowed
     */
    fun getBlockReason(): String? = (this as? Blocked)?.reason

    companion object {
        /**
         * Create an Allowed result
         */
        fun allowed(): FilterResult = Allowed

        /**
         * Create a Blocked result with reason
         */
        fun blocked(reason: String): FilterResult = Blocked(reason)

        /**
         * Common block reasons
         */
        object Reasons {
            const val NOTIFICATIONS_DISABLED = "Notifications disabled globally"
            const val NOTIFICATION_TYPE_DISABLED = "Notification type disabled"
            const val NO_LSPS_SUBSCRIBED = "No LSPs subscribed"
            const val NO_LOCATIONS_SUBSCRIBED = "No locations subscribed"
            const val NO_PROGRAMS_SUBSCRIBED = "No programs subscribed"
            const val NO_ORBITS_SUBSCRIBED = "No orbits subscribed"
            const val NO_MISSION_TYPES_SUBSCRIBED = "No mission types subscribed"
            const val NO_LAUNCHER_FAMILIES_SUBSCRIBED = "No launcher families subscribed"
            const val LSP_NOT_SUBSCRIBED = "LSP not in subscribed list"
            const val LOCATION_NOT_SUBSCRIBED = "Location not in subscribed list"
            const val PROGRAM_NOT_SUBSCRIBED = "No matching program in subscribed list"
            const val ORBIT_NOT_SUBSCRIBED = "Orbit not in subscribed list"
            const val MISSION_TYPE_NOT_SUBSCRIBED = "Mission type not in subscribed list"
            const val LAUNCHER_FAMILY_NOT_SUBSCRIBED = "Launcher family not in subscribed list"
            const val FILTER_CRITERIA_NOT_MET = "Filter criteria not met"
            const val WEBCAST_ONLY_NO_WEBCAST = "Webcast-only filter enabled, launch has no webcast"
        }
    }
}
