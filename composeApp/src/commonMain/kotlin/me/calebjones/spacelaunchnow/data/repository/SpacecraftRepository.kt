package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftEndpointDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.data.model.DataResult

interface SpacecraftRepository {
    /**
     * Get spacecraft by configuration ID with caching support.
     * Returns DataResult to indicate data freshness.
     */
    suspend fun getSpacecraftByConfig(
        configId: Int,
        limit: Int = 20,
        forceRefresh: Boolean = false,
        isPlaceholder: Boolean? = null
    ): Result<DataResult<List<SpacecraftEndpointDetailed>>>

    /**
     * Get spacecraft details by ID
     */
    suspend fun getSpacecraftDetails(spacecraftId: Int): Result<SpacecraftEndpointDetailed>

    /**
     * Get all spacecraft with basic filters and pagination
     */
    suspend fun getSpacecraft(
        limit: Int = 20,
        offset: Int = 0,
        inSpace: Boolean? = null,
        search: String? = null,
        isPlaceholder: Boolean?
    ): Result<PaginatedSpacecraftEndpointDetailedList>
}

