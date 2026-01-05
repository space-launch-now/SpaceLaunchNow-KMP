package me.calebjones.spacelaunchnow.ui.schedule

import kotlinx.serialization.Serializable

/**
 * State representing active filters for the Schedule Screen
 * Serialized to DataStore for persistence across app sessions
 */
@Serializable
data class ScheduleFilterState(
    val selectedAgencyIds: Set<Int> = emptySet(),
    val selectedLocationIds: Set<Int> = emptySet(),
    val selectedProgramIds: Set<Int> = emptySet(),
    val selectedRocketIds: Set<Int> = emptySet(),
    val selectedStatusIds: Set<Int> = emptySet(),
    val selectedOrbitIds: Set<Int> = emptySet(),
    val selectedMissionTypeIds: Set<Int> = emptySet(),
    val selectedLauncherConfigFamilyIds: Set<Int> = emptySet(),
    val isCrewed: Boolean? = null,
    val includeSuborbital: Boolean? = null,
    val dateStart: Long? = null,  // Epoch millis
    val dateEnd: Long? = null     // Epoch millis
) {
    /**
     * Returns true if any filters are currently active
     */
    fun hasActiveFilters(): Boolean =
        selectedAgencyIds.isNotEmpty() ||
                selectedLocationIds.isNotEmpty() ||
                selectedProgramIds.isNotEmpty() ||
                selectedRocketIds.isNotEmpty() ||
                selectedStatusIds.isNotEmpty() ||
                selectedOrbitIds.isNotEmpty() ||
                selectedMissionTypeIds.isNotEmpty() ||
                selectedLauncherConfigFamilyIds.isNotEmpty() ||
                isCrewed != null ||
                includeSuborbital != null ||
                dateStart != null ||
                dateEnd != null

    /**
     * Returns the total count of active filter criteria
     */
    fun activeFilterCount(): Int {
        var count = 0
        if (selectedAgencyIds.isNotEmpty()) count++
        if (selectedLocationIds.isNotEmpty()) count++
        if (selectedProgramIds.isNotEmpty()) count++
        if (selectedRocketIds.isNotEmpty()) count++
        if (selectedStatusIds.isNotEmpty()) count++
        if (selectedOrbitIds.isNotEmpty()) count++
        if (selectedMissionTypeIds.isNotEmpty()) count++
        if (selectedLauncherConfigFamilyIds.isNotEmpty()) count++
        if (isCrewed != null) count++
        if (includeSuborbital != null) count++
        if (dateStart != null || dateEnd != null) count++
        return count
    }
}
