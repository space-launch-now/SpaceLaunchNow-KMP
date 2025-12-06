package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

/**
 * Local data source for program data using SQLDelight.
 * Provides caching with automatic expiration for Starship and other program dashboards.
 */
class ProgramLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.programQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    // Program data changes infrequently - 1 hour cache
    private val cacheDuration = 1.hours
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
     * Cache a program with automatic expiration.
     */
    suspend fun cacheProgram(program: ProgramNormal) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        queries.insertOrReplaceProgram(
            id = program.id.toLong(),
            name = program.name ?: "",
            description = program.description,
            image_url = program.image?.imageUrl,
            info_url = program.infoUrl,
            wiki_url = program.wikiUrl,
            start_date = program.startDate?.toEpochMilliseconds(),
            end_date = program.endDate?.toEpochMilliseconds(),
            json_data = json.encodeToString(program),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    /**
     * Get a program from fresh cache (within TTL).
     * Returns null if expired or not found.
     */
    suspend fun getProgram(id: Int): ProgramNormal? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getProgramById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let { 
            try {
                val ageMinutes = (now - it.cached_at) / 60000
                println("  [PROGRAM] Cache entry age: ${ageMinutes} minutes (cached at ${it.cached_at}, expires at ${it.expires_at})")
                json.decodeFromString<ProgramNormal>(it.json_data)
            } catch (e: Exception) {
                println("  [PROGRAM] Failed to deserialize cached program: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Get a program from stale cache (ignores expiration).
     * Used for stale-while-revalidate pattern when API fails.
     */
    suspend fun getProgramStale(id: Int): ProgramNormal? {
        val cached = queries.getProgramByIdStale(id.toLong()).executeAsOneOrNull()
        return cached?.let { 
            try {
                val now = System.now().toEpochMilliseconds()
                val ageMinutes = (now - it.cached_at) / 60000
                println("  [PROGRAM] Stale cache entry age: ${ageMinutes} minutes")
                json.decodeFromString<ProgramNormal>(it.json_data)
            } catch (e: Exception) {
                println("  [PROGRAM] Failed to deserialize stale program: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Get the cache timestamp for a program.
     * Returns epoch milliseconds when data was cached, or null if not found.
     */
    suspend fun getCacheTimestamp(id: Int): Long? {
        return queries.getProgramByIdStale(id.toLong()).executeAsOneOrNull()?.cached_at
    }
    
    /**
     * Delete all expired program entries.
     */
    suspend fun deleteExpiredPrograms() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredPrograms(now)
    }
    
    /**
     * Clear all cached programs.
     */
    suspend fun clearAllPrograms() {
        queries.clearAllPrograms()
    }
}
