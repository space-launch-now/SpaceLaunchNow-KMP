package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.FilterOption

/**
 * Repository for fetching and caching filter options for the Astronaut Screen
 */
interface AstronautFilterRepository {
    /**
     * Get all available astronaut statuses for filtering
     * @param forceRefresh Force fetch from API ignoring cache
     * @return List of status filter options
     */
    suspend fun getStatuses(forceRefresh: Boolean = false): Result<List<FilterOption>>
}
