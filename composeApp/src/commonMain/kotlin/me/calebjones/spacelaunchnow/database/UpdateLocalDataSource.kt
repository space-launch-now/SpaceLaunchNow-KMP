package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class UpdateLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.updateQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    private val cacheDuration = 1.hours
    private val debugCacheDuration = 2.minutes

    private val log = logger()
    
    private suspend fun getEffectiveCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            log.w { "⚠️ DEBUG MODE: Using short cache TTL (2 minutes) instead of ${cacheDuration.inWholeHours} hours" }
            debugCacheDuration
        } else {
            cacheDuration
        }
    }
    
    suspend fun cacheUpdate(update: UpdateEndpoint) {
        val now = Clock.System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        queries.insertOrReplaceUpdate(
            id = update.id.toLong(),
            profile_image = update.profileImage,
            comment = update.comment ?: "",
            info_url = update.infoUrl,
            created_on = update.createdOn?.toEpochMilliseconds(),
            json_data = json.encodeToString(update),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    suspend fun cacheUpdates(updates: List<UpdateEndpoint>) {
        updates.forEach { cacheUpdate(it) }
    }
    
    suspend fun getUpdate(id: Int): UpdateEndpoint? {
        val now = Clock.System.now().toEpochMilliseconds()
        val cached = queries.getUpdateById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<UpdateEndpoint>(it.json_data) }
    }
    
    suspend fun getRecentUpdates(limit: Int): List<UpdateEndpoint> {
        val now = Clock.System.now().toEpochMilliseconds()
        val results = queries.getRecentUpdates(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    val ageMinutes = (now - cached.cached_at) / 60000
                    log.v { "Cache entry age: ${ageMinutes} minutes (cached at ${cached.cached_at}, expires at ${cached.expires_at})" }
                    json.decodeFromString<UpdateEndpoint>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding UpdateEndpoint from cache: ${e.message}" }
                    null
                }
            }
        return results
    }
    
    suspend fun deleteExpiredUpdates() {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.deleteExpiredUpdates(now)
    }
    
    /**
     * Gets the timestamp of when updates were last cached.
     * Returns the most recent cached_at timestamp.
     */
    suspend fun getCacheTimestamp(key: String): Long? {
        return when (key) {
            "updates" -> queries.getRecentUpdates(Long.MAX_VALUE, 1).executeAsOneOrNull()?.cached_at
            else -> null
        }
    }
    
    suspend fun clearAllUpdates() {
        queries.clearAllUpdates()
    }
}
