package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig

/**
 * Repository interface for spacecraft configuration (spacecraft type) data.
 *
 * SpacecraftConfig represents a spacecraft type/model (e.g., "Starship", "Dragon 2").
 * This is the "category" level - individual spacecraft belong to a configuration.
 *
 * NOTE: Unlike LauncherConfigRepository, the API does NOT support filtering by program.
 * We filter by agency instead (SpaceX = 121) or use search.
 */
interface SpacecraftConfigRepository {

    suspend fun getConfigurationsByAgencyDomain(
        agencyId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedResult<SpacecraftConfig>>

    suspend fun getConfigurationsDomain(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null
    ): Result<PaginatedResult<SpacecraftConfig>>

    suspend fun getConfigurationDetailsDomain(configId: Int): Result<SpacecraftConfig>
}