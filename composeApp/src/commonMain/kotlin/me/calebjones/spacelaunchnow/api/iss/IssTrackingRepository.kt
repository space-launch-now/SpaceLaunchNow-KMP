package me.calebjones.spacelaunchnow.api.iss

/**
 * Repository interface for ISS tracking data from wheretheiss.at API
 */
interface IssTrackingRepository {
    /**
     * Get current ISS position
     * @return Result containing IssPosition or error
     */
    suspend fun getCurrentPosition(): Result<IssPosition>

    /**
     * Get TLE data for ISS orbit propagation
     * @return Result containing IssTle or error
     */
    suspend fun getTleData(): Result<IssTle>

    /**
     * Get ISS positions at multiple timestamps
     * @param timestamps Unix timestamps in seconds
     * @return Result containing list of IssPosition or error
     */
    suspend fun getPositionsAtTimestamps(timestamps: List<Long>): Result<List<IssPosition>>
}
