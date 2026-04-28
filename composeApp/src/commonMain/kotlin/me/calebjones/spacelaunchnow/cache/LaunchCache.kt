package me.calebjones.spacelaunchnow.cache

import me.calebjones.spacelaunchnow.domain.model.Launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Cache service for storing launch data to avoid unnecessary API calls
 * when navigating between screens with partial launch data.
 * Uses unified domain Launch type for all cache entries.
 */
class LaunchCache {
    private val cache = mutableMapOf<String, Launch>()
    private val cacheTimestamps = mutableMapOf<String, Instant>()

    /**
     * Store a Launch object in cache
     */
    fun cacheLaunch(launch: Launch) {
        cache[launch.id] = launch
        cacheTimestamps[launch.id] = Clock.System.now()
    }

    /**
     * Get cached Launch by ID
     * Returns null if cache is stale (older than cacheDuration)
     */
    fun getCachedLaunch(
        launchId: String,
        cacheDuration: Duration = 15.minutes
    ): Launch? {
        val cached = cache[launchId] ?: return null
        val timestamp = cacheTimestamps[launchId] ?: return null

        return if (isCacheValid(timestamp, cacheDuration)) {
            cached
        } else {
            cache.remove(launchId)
            cacheTimestamps.remove(launchId)
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
     * Check if we have any cached data for a launch ID
     */
    fun hasCachedData(launchId: String): Boolean {
        return cache.containsKey(launchId)
    }

    /**
     * Clear all cached data
     */
    fun clearCache() {
        cache.clear()
        cacheTimestamps.clear()
    }

    /**
     * Remove specific launch from cache
     */
    fun removeLaunch(launchId: String) {
        cache.remove(launchId)
        cacheTimestamps.remove(launchId)
    }
}
