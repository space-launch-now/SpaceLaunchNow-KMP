package me.calebjones.spacelaunchnow.cache

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Cache service for storing launch data to avoid unnecessary API calls
 * when navigating between screens with partial launch data.
 */
class LaunchCache {
    private val normalCache = mutableMapOf<String, LaunchNormal>()
    private val detailedCache = mutableMapOf<String, LaunchDetailed>()
    private val normalCacheTimestamps = mutableMapOf<String, Instant>()
    private val detailedCacheTimestamps = mutableMapOf<String, Instant>()

    /**
     * Store a LaunchNormal object in cache
     */
    fun cacheLaunchNormal(launch: LaunchNormal) {
        normalCache[launch.id] = launch
        normalCacheTimestamps[launch.id] = Clock.System.now()
    }

    /**
     * Store a LaunchDetailed object in cache
     */
    fun cacheLaunchDetailed(launch: LaunchDetailed) {
        detailedCache[launch.id] = launch
        detailedCacheTimestamps[launch.id] = Clock.System.now()
    }

    /**
     * Get cached LaunchNormal by ID
     * Returns null if cache is stale (older than cacheDuration)
     */
    fun getCachedLaunchNormal(
        launchId: String,
        cacheDuration: Duration = 15.minutes
    ): LaunchNormal? {
        val cached = normalCache[launchId] ?: return null
        val timestamp = normalCacheTimestamps[launchId] ?: return null

        return if (isCacheValid(timestamp, cacheDuration)) {
            cached
        } else {
            // Cache is stale, remove it
            normalCache.remove(launchId)
            normalCacheTimestamps.remove(launchId)
            null
        }
    }

    /**
     * Get cached LaunchDetailed by ID
     * Returns null if cache is stale (older than cacheDuration)
     */
    fun getCachedLaunchDetailed(
        launchId: String,
        cacheDuration: Duration = 5.minutes
    ): LaunchDetailed? {
        val cached = detailedCache[launchId] ?: return null
        val timestamp = detailedCacheTimestamps[launchId] ?: return null

        return if (isCacheValid(timestamp, cacheDuration)) {
            cached
        } else {
            // Cache is stale, remove it
            detailedCache.remove(launchId)
            detailedCacheTimestamps.remove(launchId)
            null
        }
    }

    /**
     * Check if cache is still valid based on timestamp
     */
    private fun isCacheValid(timestamp: Instant, cacheDuration: Duration): Boolean {
        val now = Clock.System.now()
        val age = now - timestamp
        return age < cacheDuration
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
        normalCacheTimestamps.clear()
        detailedCacheTimestamps.clear()
    }

    /**
     * Remove specific launch from cache
     */
    fun removeLaunch(launchId: String) {
        normalCache.remove(launchId)
        detailedCache.remove(launchId)
        normalCacheTimestamps.remove(launchId)
        detailedCacheTimestamps.remove(launchId)
    }
}
