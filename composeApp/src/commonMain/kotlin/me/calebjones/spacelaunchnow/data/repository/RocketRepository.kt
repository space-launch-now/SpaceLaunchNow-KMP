package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Repository interface for rocket-related data operations.
 */
interface RocketRepository {

    suspend fun getRocketsDomain(
        limit: Int = 20,
        offset: Int = 0,
        ordering: String? = null,
        search: String? = null,
        programIds: List<Int>? = null,
        familyIds: List<Int>? = null,
        active: Boolean? = null,
        reusable: Boolean? = null
    ): Result<PaginatedResult<VehicleConfig>>

    suspend fun getRocketDetailsDomain(id: Int): Result<VehicleConfig>
}