package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.PinnedContent
import me.calebjones.spacelaunchnow.data.model.RoadmapData

/**
 * Repository for Firebase Remote Config operations
 * 
 * Provides access to remotely-configured app values with built-in
 * caching and offline fallback support.
 */
interface RemoteConfigRepository {
    
    /**
     * Fetch remote config values from Firebase and activate them
     * 
     * Uses Firebase Remote Config's built-in caching:
     * - Default: 1-hour minimum fetch interval
     * - forceRefresh: Bypasses cache for immediate fetch
     * 
     * @param forceRefresh When true, ignores cache and fetches from network
     * @return Result.success if fetch and activate succeeded
     * @return Result.failure with exception if operation failed
     */
    suspend fun fetchAndActivate(forceRefresh: Boolean = false): Result<Unit>
    
    /**
     * Get roadmap data from remote config
     * 
     * Reads the 'roadmap_data' parameter and deserializes to RoadmapData.
     * Falls back to default values if:
     * - Remote value not yet fetched
     * - Remote value is invalid JSON
     * - Network unavailable and no cached value
     * 
     * @return Result.success with parsed RoadmapData
     * @return Result.failure with exception if parsing failed
     */
    suspend fun getRoadmapData(): Result<RoadmapData>
    
    /**
     * Get pinned/featured content from remote config
     * 
     * Reads the 'pinned_content' parameter and deserializes to PinnedContent.
     * Use this to feature specific launches or events at the top of the home screen.
     * 
     * @return Result.success with PinnedContent (null if not configured or disabled)
     * @return Result.failure with exception if parsing failed
     */
    suspend fun getPinnedContent(): Result<PinnedContent?>
    
    /**
     * Set default values for remote config parameters
     * 
     * Should be called during app initialization to ensure
     * the app functions before first remote fetch completes.
     * 
     * Defaults are embedded JSON matching the RoadmapData structure.
     */
    suspend fun setDefaults()
}
