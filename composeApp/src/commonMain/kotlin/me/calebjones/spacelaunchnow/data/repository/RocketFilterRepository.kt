package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.FilterOption

/**
 * Repository for fetching and caching filter options for the Rocket List Screen
 */
interface RocketFilterRepository {
    /**
     * Get all available programs for filtering rockets
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of program filter options
     */
    suspend fun getPrograms(forceRefresh: Boolean = false): Result<List<FilterOption>>

    /**
     * Get all available launcher configuration families for filtering rockets
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of family filter options
     */
    suspend fun getFamilies(forceRefresh: Boolean = false): Result<List<FilterOption>>
}
