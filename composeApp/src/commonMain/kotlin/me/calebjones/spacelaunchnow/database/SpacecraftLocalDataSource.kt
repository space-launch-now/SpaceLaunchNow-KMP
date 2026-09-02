package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

/**
 * Local data source for spacecraft data using SQLDelight.
 * Provides caching with automatic expiration for Starship vehicles and other spacecraft.
 *
 * Both writes and reads use the domain [Spacecraft] type directly - the cached JSON is the
 * domain representation itself (kotlinx-serializable), so no API-shaped model is ever
 * persisted or referenced from this file. A legacy (pre-migration) blob serialized from the
 * retired Launch Library `SpacecraftEndpointDetailed` shape will fail to decode against
 * [Spacecraft] and is treated as a cache miss, not a crash.
 */
class SpacecraftLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.spacecraftQueries
    private val json = Json { ignoreUnknownKeys = true }

    // Spacecraft data changes infrequently - 2 hour cache
    private val cacheDuration = 2.hours
    private val debugCacheDuration = 2.minutes

    private suspend fun getEffectiveCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            println("⚠️ DEBUG MODE: Using short cache TTL (2 minutes) instead of ${cacheDuration.inWholeHours} hours")
            debugCacheDuration
        } else {
            cacheDuration
        }
    }

    /**
     * Cache a spacecraft with automatic expiration.
     */
    suspend fun cacheSpacecraft(spacecraft: Spacecraft) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceSpacecraft(
            id = spacecraft.id.toLong(),
            name = spacecraft.name,
            serial_number = spacecraft.serialNumber,
            status_id = spacecraft.status?.id?.toLong(),
            status_name = spacecraft.status?.name,
            description = spacecraft.description,
            spacecraft_config_id = spacecraft.config?.id?.toLong(),
            spacecraft_config_name = spacecraft.config?.name,
            json_data = json.encodeToString(spacecraft),
            cached_at = now,
            expires_at = expiresAt
        )
    }

    /**
     * Cache multiple spacecraft.
     */
    suspend fun cacheSpacecraftList(spacecraftList: List<Spacecraft>) {
        spacecraftList.forEach { cacheSpacecraft(it) }
    }

    private fun deserialize(jsonData: String): Spacecraft? = try {
        json.decodeFromString<Spacecraft>(jsonData)
    } catch (e: Exception) {
        println("  [SPACECRAFT] Failed to deserialize cached spacecraft: ${e.message}")
        null
    }

    /**
     * Get a spacecraft from fresh cache (within TTL).
     * Returns null if expired or not found.
     */
    suspend fun getSpacecraft(id: Int): Spacecraft? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getSpacecraftById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let {
            val ageMinutes = (now - it.cached_at) / 60000
            println("  [SPACECRAFT] Cache entry age: ${ageMinutes} minutes")
            deserialize(it.json_data)
        }
    }

    /**
     * Get a spacecraft from stale cache (ignores expiration).
     * Used for stale-while-revalidate pattern when API fails.
     */
    suspend fun getSpacecraftStale(id: Int): Spacecraft? {
        val cached = queries.getSpacecraftByIdStale(id.toLong()).executeAsOneOrNull()
        return cached?.let {
            val now = System.now().toEpochMilliseconds()
            val ageMinutes = (now - it.cached_at) / 60000
            println("  [SPACECRAFT] Stale cache entry age: ${ageMinutes} minutes")
            deserialize(it.json_data)
        }
    }

    /**
     * Get all spacecraft from fresh cache.
     */
    suspend fun getAllSpacecraft(limit: Int): List<Spacecraft> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllSpacecraft(now, limit.toLong())
            .executeAsList()
            .mapNotNull { deserialize(it.json_data) }
    }

    /**
     * Get all spacecraft from stale cache (ignores expiration).
     */
    suspend fun getAllSpacecraftStale(limit: Int): List<Spacecraft> {
        return queries.getAllSpacecraftStale(limit.toLong())
            .executeAsList()
            .mapNotNull { deserialize(it.json_data) }
    }

    /**
     * Get spacecraft by config ID (e.g., Starship config) from fresh cache.
     */
    suspend fun getSpacecraftByConfigId(configId: Int, limit: Int): List<Spacecraft> {
        val now = System.now().toEpochMilliseconds()
        return queries.getSpacecraftByConfigId(configId.toLong(), now, limit.toLong())
            .executeAsList()
            .mapNotNull { deserialize(it.json_data) }
    }

    /**
     * Get spacecraft by config ID from stale cache.
     */
    suspend fun getSpacecraftByConfigIdStale(configId: Int, limit: Int): List<Spacecraft> {
        return queries.getSpacecraftByConfigIdStale(configId.toLong(), limit.toLong())
            .executeAsList()
            .mapNotNull { deserialize(it.json_data) }
    }

    /**
     * Get the cache timestamp for spacecraft data.
     * Returns epoch milliseconds when data was cached, or null if no data.
     */
    suspend fun getCacheTimestamp(): Long? {
        return queries.getAllSpacecraftStale(1).executeAsOneOrNull()?.cached_at
    }

    /**
     * Delete all expired spacecraft entries.
     */
    suspend fun deleteExpiredSpacecraft() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredSpacecraft(now)
    }

    /**
     * Clear all cached spacecraft.
     */
    suspend fun clearAllSpacecraft() {
        queries.clearAllSpacecraft()
    }
}
