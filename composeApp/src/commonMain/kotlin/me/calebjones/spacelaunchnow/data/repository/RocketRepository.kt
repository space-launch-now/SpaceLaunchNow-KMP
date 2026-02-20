package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList

/**
 * Repository interface for rocket-related data operations.
 *
 * This repository follows the Result<T> pattern to encapsulate success/failure states
 * and provides a clean abstraction layer over the Launch Library API.
 */
interface RocketRepository {
    
    /**
     * Fetch a paginated list of rockets from the API.
     *
     * @param limit Maximum number of results per page (default: 20)
     * @param offset Number of results to skip for pagination
     * @param ordering Optional field to sort results by (e.g., "name", "-name")
     * @param search Optional search query to filter rockets by name
     * @param manufacturerIds Optional list of manufacturer IDs to filter rockets
     *
     * @return Result wrapping PaginatedLauncherConfigNormalList on success,
     *         or an exception on failure (network error, API error, etc.)
     */
    suspend fun getRockets(
        limit: Int = 20,
        offset: Int = 0,
        ordering: String? = null,
        search: String? = null,

        programIds: List<Int>? = null,
        familyIds: List<Int>? = null,
        active: Boolean? = null,
        reusable: Boolean? = null
    ): Result<PaginatedLauncherConfigNormalList>
    
    /**
     * Fetch detailed information about a specific rocket configuration.
     *
     * @param id The unique identifier of the rocket configuration
     *
     * @return Result wrapping LauncherConfigDetailed on success,
     *         or an exception on failure (404 if rocket not found)
     */
    suspend fun getRocketDetails(id: Int): Result<LauncherConfigDetailed>
}
