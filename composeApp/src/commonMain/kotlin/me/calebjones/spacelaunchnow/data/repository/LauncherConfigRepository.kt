package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Repository interface for launcher configuration (rocket type) data.
 *
 * LauncherConfig represents a rocket type/variant (e.g., "Super Heavy", "Falcon 9 Block 5").
 * This is the "category" level - individual launchers (boosters) belong to a configuration.
 */
interface LauncherConfigRepository {

    suspend fun getConfigurationsByProgramDomain(
        programId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedResult<VehicleConfig>>

    suspend fun getConfigurationsDomain(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null
    ): Result<PaginatedResult<VehicleConfig>>

    suspend fun getConfigurationDetailsDomain(configId: Int): Result<VehicleConfig>
}