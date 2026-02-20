package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

/**
 * Enum representing sort options for rocket lists.
 * Each enum value maps to a specific API ordering parameter.
 * 
 * @property apiParam The parameter value sent to Launch Library API
 * @property displayName Human-readable name for UI display
 */
@Serializable
enum class SortField(
    val apiParam: String,
    val displayName: String
) {
    NAME_ASC(
        apiParam = "name",
        displayName = "Name (A-Z)"
    ),
    NAME_DESC(
        apiParam = "-name",
        displayName = "Name (Z-A)"
    ),
    TOTAL_LAUNCHES_DESC(
        apiParam = "-total_launch_count",
        displayName = "Most Launches"
    ),
    TOTAL_LAUNCHES_ASC(
        apiParam = "total_launch_count",
        displayName = "Fewest Launches"
    ),
    FIRST_FLIGHT_DESC(
        apiParam = "-maiden_flight",
        displayName = "Newest First"
    ),
    FIRST_FLIGHT_ASC(
        apiParam = "maiden_flight",
        displayName = "Oldest First"
    );
    
    companion object {
        /**
         * Default sort order for initial load.
         */
        val DEFAULT = TOTAL_LAUNCHES_DESC
        
        /**
         * Find sort field by API parameter value.
         */
        fun fromApiParam(param: String): SortField? = 
            entries.firstOrNull { it.apiParam == param }
    }
}
