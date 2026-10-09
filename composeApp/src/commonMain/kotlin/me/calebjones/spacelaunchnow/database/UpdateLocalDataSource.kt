package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.Update
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

    // Caches the domain Update (not the Trantor wire model) so index columns are computed from
    // domain fields and the blob decodes back into what the rest of the app already consumes.
    suspend fun cacheUpdate(update: Update) {
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

    suspend fun cacheUpdates(updates: List<Update>) {
        updates.forEach { cacheUpdate(it) }
    }

    suspend fun getUpdate(id: Int): Update? {
        val now = Clock.System.now().toEpochMilliseconds()
        val cached = queries.getUpdateById(id.toLong(), now).executeAsOneOrNull() ?: return null
        return try {
            json.decodeFromString<Update>(cached.json_data)
        } catch (e: Exception) {
            // A blob cached before this migration (or by a future incompatible shape) simply
            // misses rather than crashing or being coerced into a fabricated Update.
            log.e(e) { "Error decoding Update from cache: ${e.message}" }
            null
        }
    }

    suspend fun getRecentUpdates(limit: Int): List<Update> {
        val now = Clock.System.now().toEpochMilliseconds()
        val results = queries.getRecentUpdates(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    val ageMinutes = (now - cached.cached_at) / 60000
                    log.v { "Cache entry age: ${ageMinutes} minutes (cached at ${cached.cached_at}, expires at ${cached.expires_at})" }
                    json.decodeFromString<Update>(cached.json_data)
                } catch (e: Exception) {
                    log.e(e) { "Error decoding Update from cache: ${e.message}" }
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
