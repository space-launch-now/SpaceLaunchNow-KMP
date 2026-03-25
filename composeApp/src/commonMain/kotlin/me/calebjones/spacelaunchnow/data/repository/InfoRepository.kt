package me.calebjones.spacelaunchnow.data.repository

/**
 * Repository for accessing SpaceNews API info including available news sites
 */
interface InfoRepository {
    /**
     * Get the list of available news sites for filtering
     * @return List of news site names
     */
    suspend fun getNewsSites(): Result<List<String>>
}
