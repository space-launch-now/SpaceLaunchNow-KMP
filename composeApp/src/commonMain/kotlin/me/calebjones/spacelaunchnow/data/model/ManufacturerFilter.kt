package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a manufacturer option in the filter sheet.
 * 
 * @property id Manufacturer ID (LSP ID from API)
 * @property name Full manufacturer name (e.g., "SpaceX")
 * @property abbreviation Short name/abbreviation (e.g., "SpX")
 * @property rocketCount Number of rockets by this manufacturer (for display)
 */
@Serializable
data class ManufacturerFilter(
    val id: Int,
    val name: String,
    val abbreviation: String?,
    val rocketCount: Int = 0
) {
    /**
     * Display label for filter UI.
     * Format: "SpaceX (12 rockets)" or "NASA"
     */
    val displayLabel: String
        get() = if (rocketCount > 0) {
            "$name ($rocketCount ${if (rocketCount == 1) "rocket" else "rockets"})"
        } else {
            name
        }
    
    /**
     * Search text for filtering manufacturer list.
     */
    val searchText: String
        get() = "$name ${abbreviation ?: ""}".lowercase()
}
