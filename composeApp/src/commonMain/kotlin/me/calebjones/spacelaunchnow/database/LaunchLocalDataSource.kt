package me.calebjones.spacelaunchnow.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

/**
 * Local data source for launch data using SQLDelight
 * Provides caching with automatic expiration
 */
class LaunchLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.launchQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    // All cache durations set to 10 minutes
    private val cacheDuration = 10.minutes
    
    // Debug cache duration (1 minutes for testing)
    private val debugCacheDuration = 1.minutes

    private val log = logger()
    
    private suspend fun getEffectiveCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            log.w { "⚠️ DEBUG MODE: Using short cache TTL (1 minutes) instead of ${cacheDuration.inWholeHours} hour" }
            debugCacheDuration
        } else {
            cacheDuration
        }
    }
    
    // LaunchBasic operations
    suspend fun cacheBasicLaunch(launch: LaunchBasic) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        queries.insertOrReplaceBasic(
            id = launch.id,
            name = launch.name ?: "",
            status_id = launch.status?.id?.toLong(),
            status_name = launch.status?.name,
            net = launch.net?.toEpochMilliseconds(),
            window_end = launch.windowEnd?.toEpochMilliseconds(),
            window_start = launch.windowStart?.toEpochMilliseconds(),
            launch_service_provider_id = launch.launchServiceProvider?.id?.toLong(),
            launch_service_provider_name = launch.launchServiceProvider?.name,
            rocket_configuration_id = null,
            rocket_configuration_name = null,
            pad_name = null,
            location_name = launch.locationName,
            image_url = launch.image?.imageUrl,
            json_data = json.encodeToString(launch),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    suspend fun cacheBasicLaunches(launches: List<LaunchBasic>) {
        launches.forEach { cacheBasicLaunch(it) }
    }
    
    suspend fun getBasicLaunch(id: String): Launch? {
        return getBasicLaunchApi(id)?.toDomain()
    }

    suspend fun getBasicLaunchApi(id: String): LaunchBasic? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getBasicById(id, now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<LaunchBasic>(it.json_data) }
    }
    
    suspend fun getUpcomingBasicLaunches(limit: Int): List<Launch> {
        return getUpcomingBasicLaunchesApi(limit).map { it.toDomain() }
    }

    suspend fun getUpcomingBasicLaunchesApi(limit: Int): List<LaunchBasic> {
        val now = System.now().toEpochMilliseconds()
        return queries.getUpcomingBasic(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchBasic>(cached.json_data)
                } catch (e: Exception) {
                    log.e (e) {
                        "Error decoding LaunchBasic from cache: ${e.message}"
                    }
                    null
                }
            }
    }
    
    suspend fun getPreviousBasicLaunches(limit: Int): List<Launch> {
        return getPreviousBasicLaunchesApi(limit).map { it.toDomain() }
    }

    suspend fun getPreviousBasicLaunchesApi(limit: Int): List<LaunchBasic> {
        val now = System.now().toEpochMilliseconds()
        return queries.getPreviousBasic(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchBasic>(cached.json_data)
                } catch (e: Exception) {
                    log.e (e) {
                        "Error decoding LaunchBasic from cache: ${e.message}"
                    }
                    null
                }
            }
    }
    
    // LaunchNormal operations
    suspend fun cacheNormalLaunch(launch: LaunchNormal) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        queries.insertOrReplaceNormal(
            id = launch.id,
            name = launch.name ?: "",
            status_id = launch.status?.id?.toLong(),
            status_name = launch.status?.name,
            net = launch.net?.toEpochMilliseconds(),
            window_end = launch.windowEnd?.toEpochMilliseconds(),
            window_start = launch.windowStart?.toEpochMilliseconds(),
            launch_service_provider_id = launch.launchServiceProvider?.id?.toLong(),
            launch_service_provider_name = launch.launchServiceProvider?.name,
            rocket_configuration_id = launch.rocket?.configuration?.id?.toLong(),
            rocket_configuration_name = launch.rocket?.configuration?.name,
            pad_name = launch.pad?.name,
            location_name = launch.pad?.location?.name,
            image_url = launch.image?.imageUrl,
            mission_name = launch.mission?.name,
            mission_description = launch.mission?.description,
            json_data = json.encodeToString(launch),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    suspend fun cacheNormalLaunches(launches: List<LaunchNormal>) {
        launches.forEach { cacheNormalLaunch(it) }
    }
    
    suspend fun getNormalLaunch(id: String): Launch? {
        return getNormalLaunchApi(id)?.toDomain()
    }

    suspend fun getNormalLaunchApi(id: String): LaunchNormal? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getNormalById(id, now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<LaunchNormal>(it.json_data) }
    }
    
    suspend fun getUpcomingNormalLaunches(limit: Int): List<Launch> {
        return getUpcomingNormalLaunchesApi(limit).map { it.toDomain() }
    }

    suspend fun getUpcomingNormalLaunchesApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        val results = queries.getUpcomingNormal(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    val ageMinutes = (now - cached.cached_at) / 60000
                    log.v("Cache entry age: ${ageMinutes} minutes (cached at ${cached.cached_at}, expires at ${cached.expires_at})")
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e (e) { "Error decoding LaunchNormal from cache: ${e.message}" }
                    null
                }
            }
        return results
    }
    
    suspend fun getPreviousNormalLaunches(limit: Int): List<Launch> {
        return getPreviousNormalLaunchesApi(limit).map { it.toDomain() }
    }

    suspend fun getPreviousNormalLaunchesApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        val results = queries.getPreviousNormal(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    val ageMinutes = (now - cached.cached_at) / 60000
                    log.v { "Cache entry age: ${ageMinutes} minutes (cached at ${cached.cached_at}, expires at ${cached.expires_at})" }
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding LaunchNormal from cache: ${e.message}" }
                    null
                }
            }
        return results
    }
    
    // LaunchDetailed operations
    suspend fun cacheDetailedLaunch(launch: LaunchDetailed) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        queries.insertOrReplaceDetailed(
            id = launch.id,
            name = launch.name ?: "",
            status_id = launch.status?.id?.toLong(),
            status_name = launch.status?.name,
            net = launch.net?.toEpochMilliseconds(),
            window_end = launch.windowEnd?.toEpochMilliseconds(),
            window_start = launch.windowStart?.toEpochMilliseconds(),
            launch_service_provider_id = launch.launchServiceProvider?.id?.toLong(),
            launch_service_provider_name = launch.launchServiceProvider?.name,
            rocket_configuration_id = launch.rocket?.configuration?.id?.toLong(),
            rocket_configuration_name = launch.rocket?.configuration?.name,
            pad_name = launch.pad?.name,
            location_name = launch.pad?.location?.name,
            image_url = launch.image?.imageUrl,
            mission_name = launch.mission?.name,
            mission_description = launch.mission?.description,
            json_data = json.encodeToString(launch),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    suspend fun getDetailedLaunch(id: String): Launch? {
        return getDetailedLaunchApi(id)?.toDomain()
    }

    suspend fun getDetailedLaunchApi(id: String): LaunchDetailed? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getDetailedById(id, now).executeAsOneOrNull()
        return cached?.let {
            val ageMinutes = (now - it.cached_at) / 60000
            log.v { "Cache entry age: ${ageMinutes} minutes (cached at ${it.cached_at}, expires at ${it.expires_at})" }
            json.decodeFromString<LaunchDetailed>(it.json_data)
        }
    }
    
    // In-flight launch cache methods (status_id = 6)
    suspend fun getInFlightNormalLaunches(limit: Int): List<Launch> {
        return getInFlightNormalLaunchesApi(limit).map { it.toDomain() }
    }

    suspend fun getInFlightNormalLaunchesApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getInFlightNormal(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding in-flight LaunchNormal from cache: ${e.message}" }
                    null
                }
            }
    }

    suspend fun getInFlightNormalLaunchesStale(limit: Int): List<Launch> {
        return getInFlightNormalLaunchesStaleApi(limit).map { it.toDomain() }
    }

    suspend fun getInFlightNormalLaunchesStaleApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getInFlightNormalStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding stale in-flight LaunchNormal from cache: ${e.message}" }
                    null
                }
            }
    }

    // Stale cache methods - return data regardless of expiration for stale-while-revalidate pattern
    suspend fun getUpcomingNormalLaunchesStale(limit: Int): List<Launch> {
        return getUpcomingNormalLaunchesStaleApi(limit).map { it.toDomain() }
    }

    suspend fun getUpcomingNormalLaunchesStaleApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getUpcomingNormalStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding LaunchNormal from cache: ${e.message}" }
                    null
                }
            }
    }
    
    suspend fun getPreviousNormalLaunchesStale(limit: Int): List<Launch> {
        return getPreviousNormalLaunchesStaleApi(limit).map { it.toDomain() }
    }

    suspend fun getPreviousNormalLaunchesStaleApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getPreviousNormalStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding LaunchNormal from cache: ${e.message}" }
                    null
                }
            }
    }
    
    suspend fun getDetailedLaunchStale(id: String): Launch? {
        return getDetailedLaunchStaleApi(id)?.toDomain()
    }

    suspend fun getDetailedLaunchStaleApi(id: String): LaunchDetailed? {
        return queries.getDetailedByIdStale(id).executeAsOneOrNull()?.let {
            try {
                json.decodeFromString<LaunchDetailed>(it.json_data)
            } catch (e: Exception) {
                log.e(e) { "Error decoding LaunchDetailed from cache: ${e.message}" }
                null
            }
        }
    }
    
    // Starship history operations with 1-month TTL (uses separate cache table)
    suspend fun cacheStarshipHistory(launches: List<LaunchNormal>) {
        val now = System.now().toEpochMilliseconds()
        val oneMonthMs = 2592000000L // 30 days in milliseconds
        val expiresAt = now + oneMonthMs
        
        launches.forEach { launch ->
            queries.insertOrReplaceStarshipHistory(
                id = launch.id,
                name = launch.name ?: "",
                status_id = launch.status?.id?.toLong(),
                status_name = launch.status?.name,
                net = launch.net?.toEpochMilliseconds(),
                window_end = launch.windowEnd?.toEpochMilliseconds(),
                window_start = launch.windowStart?.toEpochMilliseconds(),
                launch_service_provider_id = launch.launchServiceProvider?.id?.toLong(),
                launch_service_provider_name = launch.launchServiceProvider?.name,
                rocket_configuration_id = launch.rocket?.configuration?.id?.toLong(),
                rocket_configuration_name = launch.rocket?.configuration?.name,
                pad_name = launch.pad?.name,
                location_name = launch.pad?.location?.name,
                image_url = launch.image?.imageUrl,
                mission_name = launch.mission?.name,
                mission_description = launch.mission?.description,
                json_data = json.encodeToString(launch),
                cached_at = now,
                expires_at = expiresAt
            )
        }
    }
    
    suspend fun getStarshipHistory(limit: Int): List<Launch> {
        return getStarshipHistoryApi(limit).map { it.toDomain() }
    }

    suspend fun getStarshipHistoryApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getStarshipHistory(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding Starship history from cache: ${e.message}" }
                    null
                }
            }
    }
    
    suspend fun getStarshipHistoryStale(limit: Int): List<Launch> {
        return getStarshipHistoryStaleApi(limit).map { it.toDomain() }
    }

    suspend fun getStarshipHistoryStaleApi(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getStarshipHistoryStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding stale Starship history from cache: ${e.message}" }
                    null
                }
            }
    }
    
    // Cache metadata operations
    
    /**
     * Gets the timestamp of when data for a specific cache key was last cached.
     * Returns the most recent cached_at timestamp for launches in the specified category.
     * 
     * @param key Cache category: "upcoming_launches", "previous_launches", "starship_history", etc.
     * @return Timestamp in milliseconds since epoch, or null if no cached data exists
     */
    suspend fun getCacheTimestamp(key: String): Long? {
        // For upcoming/previous launches, get the most recent cached_at from normal launches
        // Since we cache all fetched launches together, they'll have the same timestamp
        return when (key) {
            "upcoming_launches" -> {
                val now = System.now().toEpochMilliseconds()
                queries.getUpcomingNormalStale(now, 1).executeAsOneOrNull()?.cached_at
            }
            "previous_launches" -> {
                val now = System.now().toEpochMilliseconds()
                queries.getPreviousNormalStale(now, 1).executeAsOneOrNull()?.cached_at
            }
            "starship_history" -> {
                val now = System.now().toEpochMilliseconds()
                queries.getPreviousNormalStale(now, 1).executeAsOneOrNull()?.cached_at
            }
            else -> null
        }
    }
    
    // Cleanup operations
    suspend fun deleteExpiredLaunches() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredBasic(now)
        queries.deleteExpiredNormal(now)
        queries.deleteExpiredDetailed(now)
    }
    
    suspend fun clearAllLaunches() {
        queries.clearAllBasic()
        queries.clearAllNormal()
        queries.clearAllDetailed()
    }
}
