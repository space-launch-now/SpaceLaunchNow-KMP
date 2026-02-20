package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing all filter, search, and sort parameters for rocket queries.
 * Immutable by default to ensure predictable state management.
 * 
 * @property searchQuery Text search query for rocket name/manufacturer (null = no search)
 * @property sortField Field to sort results by
 * @property activeOnly Filter to show only active rockets (null = show all)
 * @property limit Number of results per page (default 20, max 100)
 * @property offset Pagination offset (default 0)
 */
@Serializable
data class RocketFilters(
    val searchQuery: String? = null,
    val sortField: SortField = SortField.TOTAL_LAUNCHES_DESC,
    val activeOnly: Boolean? = null,
    val limit: Int = 20,
    val offset: Int = 0
) {
    /**
     * Returns true if no filters are active (search, active status).
     * Used for UI to show "clear filters" button state.
     */
    fun hasActiveFilters(): Boolean = 
        !searchQuery.isNullOrEmpty() || 
        activeOnly != null
    
    /**
     * Returns count of active filters for badge display.
     */
    fun activeFilterCount(): Int {
        var count = 0
        if (!searchQuery.isNullOrEmpty()) count++
        if (activeOnly != null) count++
        return count
    }
    
    /**
     * Resets to default state (no filters).
     */
    fun reset(): RocketFilters = RocketFilters(
        sortField = sortField, // Keep sort preference
        limit = limit
    )
    
    /**
     * Validates filter parameters before API call.
     * Throws IllegalArgumentException if validation fails.
     */
    fun validate() {
        require(limit in 1..100) {
            "Limit must be between 1 and 100, got: $limit" 
        }
        require(offset >= 0) { 
            "Offset must be non-negative, got: $offset" 
        }
        searchQuery?.let { query ->
            require(query.trim().isNotEmpty()) { 
                "Search query cannot be empty or whitespace" 
            }
        }
    }
    
    companion object {
        /**
         * Default filter state for initial screen load.
         */
        val DEFAULT = RocketFilters()
    }
}
