package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

/**
 * Local data source for space station, expedition, and TLE data using SQLDelight.
 * Provides caching with automatic expiration.
 * 
 * TTLs:
 * - Space station details: 4 hours (data rarely changes)
 * - Expedition/crew data: 4 hours (only changes when astronauts arrive/depart)
 * - TLE data: 1 hour (updated several times daily by NORAD)
 */
class SpaceStationLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val spaceStationQueries = database.spaceStationQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    // Station and expedition data rarely changes
    private val stationCacheDuration = 4.hours
    // TLE data updates several times daily
    private val tleCacheDuration = 1.hours
    // Debug cache durations
    private val debugCacheDuration = 2.minutes

    private val log = logger()
    
    private suspend fun getStationCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            log.w { "⚠️ DEBUG MODE: Using short cache TTL (2 minutes) instead of ${stationCacheDuration.inWholeHours} hours" }
            debugCacheDuration
        } else {
            stationCacheDuration
        }
    }
    
    private suspend fun getTleCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            log.w { "⚠️ DEBUG MODE: Using short cache TTL (2 minutes) instead of ${tleCacheDuration.inWholeHours} hour" }
            debugCacheDuration
        } else {
            tleCacheDuration
        }
    }
    
    // ==================== Space Station Operations ====================
    
    suspend fun cacheSpaceStation(station: SpaceStationDetailedEndpoint) {
        val now = System.now().toEpochMilliseconds()
        val duration = getStationCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        spaceStationQueries.insertOrReplaceSpaceStation(
            id = station.id.toLong(),
            name = station.name,
            description = station.description,
            orbit = station.orbit,
            founded = station.founded?.toString(),
            image_url = station.image?.imageUrl,
            onboard_crew = station.onboardCrew?.toLong(),
            docked_vehicles = station.dockedVehicles?.toLong(),
            json_data = json.encodeToString(station),
            cached_at = now,
            expires_at = expiresAt
        )
        log.d { "Cached space station: ${station.name} (expires in ${duration.inWholeHours}h)" }
    }
    
    suspend fun getSpaceStation(id: Int): SpaceStationDetailedEndpoint? {
        val now = System.now().toEpochMilliseconds()
        val cached = spaceStationQueries.getSpaceStationById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let { 
            try {
                val station = json.decodeFromString<SpaceStationDetailedEndpoint>(it.json_data)
                val ageMinutes = (now - it.cached_at) / 60000
                log.d { "Cache HIT for space station $id (age: ${ageMinutes}min)" }
                station
            } catch (e: Exception) {
                log.e(e) { "Failed to parse cached space station: ${it.json_data}" }
                null
            }
        }
    }
    
    /**
     * Get stale (expired) space station data for offline support.
     * Use when fresh cache misses but we want to show something.
     */
    suspend fun getSpaceStationStale(id: Int): SpaceStationDetailedEndpoint? {
        val cached = spaceStationQueries.getSpaceStationByIdStale(id.toLong()).executeAsOneOrNull()
        return cached?.let { 
            try {
                val station = json.decodeFromString<SpaceStationDetailedEndpoint>(it.json_data)
                log.d { "Cache STALE HIT for space station $id" }
                station
            } catch (e: Exception) {
                log.e(e) { "Failed to parse stale cached space station" }
                null
            }
        }
    }
    
    suspend fun deleteExpiredSpaceStations() {
        val now = System.now().toEpochMilliseconds()
        spaceStationQueries.deleteExpiredSpaceStations(now)
        log.d { "Cleaned up expired space stations" }
    }
    
    // ==================== Expedition Operations ====================
    
    suspend fun cacheExpedition(expedition: ExpeditionDetailed, stationId: Int) {
        val now = System.now().toEpochMilliseconds()
        val duration = getStationCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        spaceStationQueries.insertOrReplaceExpedition(
            id = expedition.id.toLong(),
            station_id = stationId.toLong(),
            name = expedition.name,
            start_date = expedition.start?.toEpochMilliseconds(),
            end_date = expedition.end?.toEpochMilliseconds(),
            crew_count = expedition.crew.size.toLong(),
            json_data = json.encodeToString(expedition),
            cached_at = now,
            expires_at = expiresAt
        )
        log.d { "Cached expedition: ${expedition.name} with ${expedition.crew.size} crew" }
    }
    
    suspend fun cacheExpeditions(expeditions: List<ExpeditionDetailed>, stationId: Int) {
        expeditions.forEach { cacheExpedition(it, stationId) }
    }
    
    suspend fun getExpeditionsByStationId(stationId: Int): List<ExpeditionDetailed> {
        val now = System.now().toEpochMilliseconds()
        return spaceStationQueries.getExpeditionsByStationId(stationId.toLong(), now)
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<ExpeditionDetailed>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Failed to parse cached expedition" }
                    null
                }
            }.also { expeditions ->
                if (expeditions.isNotEmpty()) {
                    log.d { "Cache HIT for ${expeditions.size} expeditions (station $stationId)" }
                }
            }
    }
    
    /**
     * Get stale (expired) expedition data for offline support.
     */
    suspend fun getExpeditionsByStationIdStale(stationId: Int): List<ExpeditionDetailed> {
        return spaceStationQueries.getExpeditionsByStationIdStale(stationId.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<ExpeditionDetailed>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Failed to parse stale cached expedition" }
                    null
                }
            }.also { expeditions ->
                if (expeditions.isNotEmpty()) {
                    log.d { "Cache STALE HIT for ${expeditions.size} expeditions (station $stationId)" }
                }
            }
    }
    
    suspend fun deleteExpiredExpeditions() {
        val now = System.now().toEpochMilliseconds()
        spaceStationQueries.deleteExpiredExpeditions(now)
        log.d { "Cleaned up expired expeditions" }
    }
    
    // ==================== TLE Operations ====================
    
    companion object {
        const val ISS_NORAD_ID = "25544"
    }
    
    suspend fun cacheTle(tle: IssTle) {
        val now = System.now().toEpochMilliseconds()
        val duration = getTleCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        spaceStationQueries.insertOrReplaceTle(
            id = tle.id,
            name = tle.name,
            header_ = tle.header,
            line1 = tle.line1,
            line2 = tle.line2,
            tle_timestamp = tle.tleTimestamp,
            json_data = json.encodeToString(tle),
            cached_at = now,
            expires_at = expiresAt
        )
        log.d { "Cached TLE for ${tle.name} (expires in ${duration.inWholeHours}h)" }
    }
    
    suspend fun getTle(noradId: String = ISS_NORAD_ID): IssTle? {
        val now = System.now().toEpochMilliseconds()
        val cached = spaceStationQueries.getTleById(noradId, now).executeAsOneOrNull()
        return cached?.let { 
            try {
                val tle = json.decodeFromString<IssTle>(it.json_data)
                val ageMinutes = (now - it.cached_at) / 60000
                log.d { "Cache HIT for TLE $noradId (age: ${ageMinutes}min)" }
                tle
            } catch (e: Exception) {
                log.e(e) { "Failed to parse cached TLE" }
                null
            }
        }
    }
    
    /**
     * Get stale (expired) TLE data for offline support.
     */
    suspend fun getTleStale(noradId: String = ISS_NORAD_ID): IssTle? {
        val cached = spaceStationQueries.getTleByIdStale(noradId).executeAsOneOrNull()
        return cached?.let { 
            try {
                val tle = json.decodeFromString<IssTle>(it.json_data)
                log.d { "Cache STALE HIT for TLE $noradId" }
                tle
            } catch (e: Exception) {
                log.e(e) { "Failed to parse stale cached TLE" }
                null
            }
        }
    }
    
    suspend fun deleteExpiredTle() {
        val now = System.now().toEpochMilliseconds()
        spaceStationQueries.deleteExpiredTle(now)
        log.d { "Cleaned up expired TLE data" }
    }
    
    // ==================== Cache Timestamp Helpers ====================
    
    /**
     * Gets the timestamp of when a space station was last cached.
     */
    suspend fun getStationCacheTimestamp(stationId: Int): Long? {
        return spaceStationQueries.getSpaceStationByIdStale(stationId.toLong())
            .executeAsOneOrNull()?.cached_at
    }
}
