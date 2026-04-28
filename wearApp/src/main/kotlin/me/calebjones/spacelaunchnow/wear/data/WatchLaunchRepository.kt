package me.calebjones.spacelaunchnow.wear.data

import kotlinx.coroutines.flow.Flow
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import me.calebjones.spacelaunchnow.wear.data.model.DataSource

interface WatchLaunchRepository {
    val launches: Flow<List<CachedLaunch>>
    val dataSource: Flow<DataSource>
    suspend fun refreshLaunches(limit: Int = 20): Result<List<CachedLaunch>>
    suspend fun getLaunchById(launchId: String): CachedLaunch?
    suspend fun getNextLaunch(): CachedLaunch?
}
