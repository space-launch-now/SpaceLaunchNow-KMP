package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList

/**
 * Repository interface for astronaut-related data operations.
 *
 * This repository follows the Result<T> pattern to encapsulate success/failure states
 * and provides a clean abstraction layer over the Launch Library API.
 */
interface AstronautRepository {

    /**
     * Fetch a paginated list of astronauts from the API.
     *
     * @param limit Maximum number of results per page (default: 20)
     * @param offset Number of results to skip for pagination
     * @param search Optional search query to filter astronauts by name
     * @param statusIds Optional list of status IDs to filter astronauts (e.g., active, retired)
     * @param agencyIds Optional list of agency IDs to filter astronauts by their agencies
     * @param ordering Optional field to sort results by (e.g., "name", "-name")
     * @param hasFlown Optional filter by whether astronaut has flown to space
     * @param inSpace Optional filter by whether astronaut is currently in space
     * @param isHuman Optional filter by whether entry is for a human or non-human
     *
     * @return Result wrapping PaginatedAstronautEndpointNormalList on success,
     *         or an exception on failure (network error, API error, etc.)
     */
    suspend fun getAstronauts(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        statusIds: List<Int>? = null,
        agencyIds: List<Int>? = null,
        ordering: String? = null,
        hasFlown: Boolean? = null,
        inSpace: Boolean? = null,
        isHuman: Boolean? = null
    ): Result<PaginatedAstronautEndpointNormalList>

    /**
     * Fetch detailed information about a specific astronaut.
     *
     * @param id The unique identifier of the astronaut
     *
     * @return Result wrapping AstronautEndpointDetailed on success,
     *         or an exception on failure (404 if astronaut not found)
     */
    suspend fun getAstronautDetail(id: Int): Result<AstronautEndpointDetailed>
}
