package me.calebjones.spacelaunchnow.cache

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed

/**
 * Cache service for storing launch data to avoid unnecessary API calls
 * when navigating between screens with partial launch data.
 */
class LaunchCache {
    private val normalCache = mutableMapOf<String, LaunchNormal>()
    private val detailedCache = mutableMapOf<String, LaunchDetailed>()
    
    /**
     * Store a LaunchNormal object in cache
     */
    fun cacheLaunchNormal(launch: LaunchNormal) {
        normalCache[launch.id] = launch
    }
    
    /**
     * Store a LaunchDetailed object in cache
     */
    fun cacheLaunchDetailed(launch: LaunchDetailed) {
        detailedCache[launch.id] = launch
    }
    
    /**
     * Get cached LaunchNormal by ID
     */
    fun getCachedLaunchNormal(launchId: String): LaunchNormal? {
        return normalCache[launchId]
    }
    
    /**
     * Get cached LaunchDetailed by ID
     */
    fun getCachedLaunchDetailed(launchId: String): LaunchDetailed? {
        return detailedCache[launchId]
    }
    
    /**
     * Check if we have any cached data for a launch ID (either normal or detailed)
     */
    fun hasCachedData(launchId: String): Boolean {
        return normalCache.containsKey(launchId) || detailedCache.containsKey(launchId)
    }
    
    /**
     * Clear all cached data
     */
    fun clearCache() {
        normalCache.clear()
        detailedCache.clear()
    }
    
    /**
     * Remove specific launch from cache
     */
    fun removeLaunch(launchId: String) {
        normalCache.remove(launchId)
        detailedCache.remove(launchId)
    }
}
