package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList
import me.calebjones.spacelaunchnow.data.model.RocketFilters

interface RocketRepository {
    suspend fun getRockets(limit: Int, offset: Int = 0): Result<PaginatedLauncherConfigNormalList>
    suspend fun getRocketDetails(id: Int): Result<LauncherConfigDetailed>
    suspend fun searchRockets(searchQuery: String, limit: Int = 20): Result<PaginatedLauncherConfigNormalList>
    
    /**
     * Get rockets with comprehensive filtering, searching, and sorting.
     * 
     * @param filters RocketFilters containing all query parameters
     * @return Result wrapping PaginatedLauncherConfigNormalList on success, exception on failure
     */
    suspend fun getRocketsFiltered(filters: RocketFilters): Result<PaginatedLauncherConfigNormalList>
}
