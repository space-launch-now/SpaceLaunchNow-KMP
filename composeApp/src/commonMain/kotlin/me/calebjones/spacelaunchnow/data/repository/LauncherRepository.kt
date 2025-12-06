package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherDetailedList

/**
 * Repository interface for launcher (booster/first stage) data
 */
interface LauncherRepository {
    
    /**
     * Get launchers with optional search/filter and pagination
     */
    suspend fun getLaunchers(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        ordering: String? = null,
        launcherConfigId: Int? = null
    ): Result<PaginatedLauncherDetailedList>

    /**
     * Get launchers by config ID with pagination
     */
    suspend fun getLaunchersByConfig(
        configId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedLauncherDetailedList>

    /**
     * Get launcher details by ID
     */
    suspend fun getLauncherDetails(launcherId: Int): Result<LauncherDetailed>
}
