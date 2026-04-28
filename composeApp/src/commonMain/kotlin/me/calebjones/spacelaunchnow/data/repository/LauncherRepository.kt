package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * Repository interface for launcher (booster/first stage) data
 */
interface LauncherRepository {

    suspend fun getLaunchersDomain(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        ordering: String? = null,
        launcherConfigId: Int? = null,
        isPlaceholder: Boolean? = null
    ): Result<PaginatedResult<LauncherDetail>>

    suspend fun getLaunchersByConfigDomain(
        configId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedResult<LauncherDetail>>

    suspend fun getLauncherDetailsDomain(launcherId: Int): Result<LauncherDetail>
}