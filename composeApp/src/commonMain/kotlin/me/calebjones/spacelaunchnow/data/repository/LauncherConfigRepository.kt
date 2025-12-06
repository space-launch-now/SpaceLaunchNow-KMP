package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigDetailedList

/**
 * Repository interface for launcher configuration (rocket type) data.
 * 
 * LauncherConfig represents a rocket type/variant (e.g., "Super Heavy", "Falcon 9 Block 5").
 * This is the "category" level - individual launchers (boosters) belong to a configuration.
 */
interface LauncherConfigRepository {
    
    /**
     * Get launcher configurations filtered by program ID with pagination.
     * 
     * @param programId Program ID to filter by (e.g., 1 for Starship)
     * @param limit Number of results per page
     * @param offset Pagination offset
     */
    suspend fun getConfigurationsByProgram(
        programId: Int,
        limit: Int = 20,
        offset: Int = 0
    ): Result<PaginatedLauncherConfigDetailedList>

    /**
     * Get all launcher configurations with pagination.
     */
    suspend fun getConfigurations(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null
    ): Result<PaginatedLauncherConfigDetailedList>

    /**
     * Get launcher configuration details by ID.
     */
    suspend fun getConfigurationDetails(configId: Int): Result<LauncherConfigDetailed>
}
