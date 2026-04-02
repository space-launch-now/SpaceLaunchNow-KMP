package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.data.model.DataResult

/**
 * Repository for space station data including ISS details, expeditions, and TLE data.
 * Provides cache-first access with stale-while-revalidate for offline support.
 */
interface SpaceStationRepository {

    companion object {
        const val ISS_STATION_ID = 4
    }

    /**
     * Get detailed information for a space station.
     * Uses cache-first with stale-while-revalidate pattern.
     * 
     * @param stationId Space station ID (e.g., 4 for ISS)
     * @param forceRefresh Bypass cache and fetch fresh data
     * @return DataResult containing SpaceStationDetailedEndpoint with source info
     */
    suspend fun getSpaceStationDetails(
        stationId: Int,
        forceRefresh: Boolean = false
    ): Result<DataResult<SpaceStationDetailedEndpoint>>

    /**
     * Get detailed expedition data including crew information.
     * Fetches full expedition details for each expedition ID.
     * 
     * @param expeditionIds List of expedition IDs to fetch
     * @param stationId The parent station ID for caching purposes
     * @param forceRefresh Bypass cache and fetch fresh data
     * @return DataResult containing list of ExpeditionDetailed
     */
    suspend fun getExpeditionDetails(
        expeditionIds: List<Int>,
        stationId: Int,
        forceRefresh: Boolean = false
    ): Result<DataResult<List<ExpeditionDetailed>>>

    /**
     * Get TLE (Two-Line Element) data for ISS orbit calculations.
     * TLE data is cached with shorter TTL (1 hour) since it updates multiple times daily.
     * 
     * @param noradId NORAD catalog ID (default: 25544 for ISS)
     * @param forceRefresh Bypass cache and fetch fresh data
     * @return DataResult containing IssTle
     */
    suspend fun getIssTle(
        noradId: String = "25544",
        forceRefresh: Boolean = false
    ): Result<DataResult<IssTle>>

    /**
     * Pre-warm the ISS cache by fetching all required data.
     * This is called from PreloadViewModel during app startup for instant ISS page loads.
     * 
     * Fetches:
     * - ISS station details
     * - Active expedition details with crew
     * - Current TLE data
     */
    suspend fun prewarmIssCache()
}
