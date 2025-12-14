package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.FilterOption

/**
 * Repository for fetching and caching filter options for the Schedule Screen
 */
interface ScheduleFilterRepository {
    /**
     * Get all available agencies for filtering
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of agency filter options
     */
    suspend fun getAgencies(forceRefresh: Boolean = false): Result<List<FilterOption>>
    
    /**
     * Get all available programs for filtering
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of program filter options
     */
    suspend fun getPrograms(forceRefresh: Boolean = false): Result<List<FilterOption>>
    
    /**
     * Get all available rocket configurations for filtering
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of rocket filter options
     */
    suspend fun getRockets(forceRefresh: Boolean = false): Result<List<FilterOption>>
    
    /**
     * Get all available locations for filtering
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of location filter options
     */
    suspend fun getLocations(forceRefresh: Boolean = false): Result<List<FilterOption>>
    
    /**
     * Get all available launch statuses for filtering
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of status filter options
     */
    suspend fun getStatuses(forceRefresh: Boolean = false): Result<List<FilterOption>>
}
