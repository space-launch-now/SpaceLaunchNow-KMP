package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList

interface RocketRepository {
    suspend fun getRockets(limit: Int, offset: Int = 0): Result<PaginatedLauncherConfigNormalList>
    suspend fun getRocketDetails(id: Int): Result<LauncherConfigDetailed>
    suspend fun searchRockets(searchQuery: String, limit: Int = 20): Result<PaginatedLauncherConfigNormalList>
}
