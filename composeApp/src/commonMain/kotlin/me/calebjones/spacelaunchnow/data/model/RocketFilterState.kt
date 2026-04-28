package me.calebjones.spacelaunchnow.data.model

import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * UI state for Rockets screen combining data, filters, and loading states.
 */
data class RocketFilterState(
    val rockets: List<VehicleConfig> = emptyList(),
    val filters: RocketFilters = RocketFilters.DEFAULT,
    val manufacturers: List<ManufacturerFilter> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = false,
    val totalCount: Int = 0
) {
    /**
     * Returns true if screen is showing filtered results.
     */
    val isFiltered: Boolean
        get() = filters.hasActiveFilters()
    
    /**
     * Returns true if empty state should be shown (no results, not loading).
     */
    val showEmptyState: Boolean
        get() = rockets.isEmpty() && !isLoading && error == null
}
