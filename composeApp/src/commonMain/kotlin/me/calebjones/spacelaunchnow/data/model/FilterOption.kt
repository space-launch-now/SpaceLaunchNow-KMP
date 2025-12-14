package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a filter option for Schedule Screen filtering
 */
@Serializable
data class FilterOption(
    val id: Int,
    val name: String,
    val abbreviation: String? = null
) {
    /**
     * Display name for the filter option
     * Uses full name by default, with abbreviation in parentheses if available
     */
    val displayName: String
        get() = if (abbreviation != null && abbreviation != name) {
            "$name ($abbreviation)"
        } else {
            name
        }
}
