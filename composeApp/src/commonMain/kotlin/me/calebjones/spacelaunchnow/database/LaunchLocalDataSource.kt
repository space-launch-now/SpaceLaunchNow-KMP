package me.calebjones.spacelaunchnow.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import kotlin.time.Duration.Companion.hours
import kotlin.time.Clock.System

/**
 * Local data source for launch data using SQLDelight
 * Provides caching with automatic expiration
 */
class LaunchLocalDataSource(
    database: SpaceLaunchDatabase
) {
    private val queries = database.launchQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    // Default cache durations
    private val basicCacheDuration = 24.hours
    private val normalCacheDuration = 12.hours
    private val detailedCacheDuration = 6.hours
    
    // LaunchBasic operations
    suspend fun cacheBasicLaunch(launch: LaunchBasic) {
        val now = System.now().toEpochMilliseconds()
        val expiresAt = now + basicCacheDuration.inWholeMilliseconds
        
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
    
    suspend fun getBasicLaunch(id: String): LaunchBasic? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getBasicById(id, now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<LaunchBasic>(it.json_data) }
    }
    
    suspend fun getUpcomingBasicLaunches(limit: Int): List<LaunchBasic> {
        val now = System.now().toEpochMilliseconds()
        return queries.getUpcomingBasic(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchBasic>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    suspend fun getPreviousBasicLaunches(limit: Int): List<LaunchBasic> {
        val now = System.now().toEpochMilliseconds()
        return queries.getPreviousBasic(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchBasic>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    // LaunchNormal operations
    suspend fun cacheNormalLaunch(launch: LaunchNormal) {
        val now = System.now().toEpochMilliseconds()
        val expiresAt = now + normalCacheDuration.inWholeMilliseconds
        
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
    
    suspend fun getNormalLaunch(id: String): LaunchNormal? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getNormalById(id, now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<LaunchNormal>(it.json_data) }
    }
    
    suspend fun getUpcomingNormalLaunches(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getUpcomingNormal(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    suspend fun getPreviousNormalLaunches(limit: Int): List<LaunchNormal> {
        val now = System.now().toEpochMilliseconds()
        return queries.getPreviousNormal(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<LaunchNormal>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    // LaunchDetailed operations
    suspend fun cacheDetailedLaunch(launch: LaunchDetailed) {
        val now = System.now().toEpochMilliseconds()
        val expiresAt = now + detailedCacheDuration.inWholeMilliseconds
        
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
    
    suspend fun getDetailedLaunch(id: String): LaunchDetailed? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getDetailedById(id, now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<LaunchDetailed>(it.json_data) }
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
