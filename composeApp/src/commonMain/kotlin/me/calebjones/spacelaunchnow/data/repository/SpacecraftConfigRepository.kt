package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed

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
    
    /**
     * Get spacecraft configurations filtered by agency ID with pagination.
     * 
     * @param agencyId Agency ID to filter by (e.g., 121 for SpaceX)
     * @param limit Number of results per page
     * @param offset Pagination offset
     */
    suspend fun getConfigurationsByAgency(
        agencyId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedSpacecraftConfigDetailedList>

    /**
     * Get all spacecraft configurations with pagination.
     */
    suspend fun getConfigurations(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null
    ): Result<PaginatedSpacecraftConfigDetailedList>

    /**
     * Get spacecraft configuration details by ID.
     */
    suspend fun getConfigurationDetails(configId: Int): Result<SpacecraftConfigDetailed>
}
