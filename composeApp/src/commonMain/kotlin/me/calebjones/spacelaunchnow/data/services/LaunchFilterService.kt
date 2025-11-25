package me.calebjones.spacelaunchnow.data.services

import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState

/**
 * Service for converting NotificationState filter settings into API query parameters
 * for filtering launches on the Home Screen.
 *
 * This service bridges the gap between the notification filter settings (which use
 * String IDs for flexibility) and the API parameters (which use Int IDs).
 */
class LaunchFilterService {

    /**
     * Get agency IDs for API filtering
     *
     * @param state Current notification/filter state
     * @return List of agency IDs as integers, or null if no filtering should be applied
     *
     * Returns null when:
     * - followAllLaunches is enabled (bypass all filters)
     * - No agencies are selected (empty set)
     */
    fun getAgencyIds(state: NotificationState): List<Int>? {
        if (state.followAllLaunches) return null
        if (state.subscribedAgencies.isEmpty()) return null

        return state.subscribedAgencies.mapNotNull { it.toIntOrNull() }
    }

    /**
     * Get location IDs for API filtering
     *
     * @param state Current notification/filter state
     * @return List of location IDs as integers, or null if no filtering should be applied
     *
     * Returns null when:
     * - followAllLaunches is enabled (bypass all filters)
     * - No locations are selected (empty set)
     *
     * Note:
     * - Filters out "0" (Other/Unknown) to avoid wildcard behavior
     * - Expands grouped locations to include all their associated IDs
     *   (e.g., Florida location includes both KSC and Cape Canaveral IDs)
     */
    fun getLocationIds(state: NotificationState): List<Int>? {
        if (state.followAllLaunches) return null
        if (state.subscribedLocations.isEmpty()) return null

        val allLocations = NotificationLocation.getAll()

        return state.subscribedLocations
            .filterNot { it == "0" } // Remove "Other" wildcard for explicit UI filtering
            .mapNotNull { it.toIntOrNull() }
            .flatMap { locationId ->
                // Find the location and get all its IDs (primary + additional)
                allLocations.find { it.id == locationId }?.getAllIds() ?: listOf(locationId)
            }
            .distinct()
    }

    /**
     * Check if user has any active filters configured
     *
     * @param state Current notification/filter state
     * @return true if filters are active (not following all and has selections)
     */
    fun hasActiveFilters(state: NotificationState): Boolean {
        if (state.followAllLaunches) return false
        return state.subscribedAgencies.isNotEmpty() || state.subscribedLocations.isNotEmpty()
    }

    /**
     * Check if current filter configuration will result in no data being shown
     * This happens when:
     * - followAllLaunches is false (filtering is enabled)
     * - Both agency and location filters are empty
     *
     * @param state Current notification/filter state
     * @return true if configuration will show no launches
     */
    fun willFilterEverything(state: NotificationState): Boolean {
        return !state.followAllLaunches &&
                state.subscribedAgencies.isEmpty() &&
                state.subscribedLocations.isEmpty()
    }

    /**
     * Get filter parameters for API calls
     *
     * @param state Current notification/filter state
     * @return FilterParams with agency IDs, location IDs, and merge strategy
     */
    fun getFilterParams(state: NotificationState): FilterParams {
        if (state.followAllLaunches) {
            return FilterParams(
                agencyIds = null,
                locationIds = null,
                requiresFlexibleMerge = false
            )
        }

        val agencies = getAgencyIds(state)
        val locations = getLocationIds(state)

        val bothActive = agencies != null && locations != null
        val flexibleMode = !state.useStrictMatching

        return FilterParams(
            agencyIds = agencies,
            locationIds = locations,
            requiresFlexibleMerge = bothActive && flexibleMode
        )
    }
}

/**
 * Data class representing filter parameters for API calls
 *
 * @property agencyIds List of agency IDs to filter by, or null for no agency filter
 * @property locationIds List of location IDs to filter by, or null for no location filter
 * @property requiresFlexibleMerge True if flexible mode with both filters active,
 *                                 requiring two API calls and merge (OR logic)
 */
data class FilterParams(
    val agencyIds: List<Int>?,
    val locationIds: List<Int>?,
    val requiresFlexibleMerge: Boolean
) {
    /**
     * Check if strict mode filtering is active
     * (both filters present, will be combined with AND logic)
     */
    val isStrictMode: Boolean
        get() = agencyIds != null && locationIds != null && !requiresFlexibleMerge

    /**
     * Check if any filtering is active
     */
    val hasFilters: Boolean
        get() = agencyIds != null || locationIds != null
}
