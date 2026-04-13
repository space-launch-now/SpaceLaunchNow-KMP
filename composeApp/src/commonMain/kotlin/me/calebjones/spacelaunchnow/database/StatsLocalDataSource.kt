package me.calebjones.spacelaunchnow.database

import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

class StatsLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.statsQueries
    private val log = logger()

    private val cacheDuration = 10.minutes
    private val debugCacheDuration = 1.minutes

    private suspend fun getEffectiveCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            log.w { "⚠️ DEBUG MODE: Using short cache TTL (1 minute) instead of ${cacheDuration.inWholeMinutes} minutes" }
            debugCacheDuration
        } else {
            cacheDuration
        }
    }

    /**
     * Get cached stat count. Returns null if cache miss or expired.
     */
    suspend fun getStatCount(key: String): Int? {
        val now = System.now().toEpochMilliseconds()
        return queries.getStatCount(key, now).executeAsOneOrNull()?.toInt()
    }

    /**
     * Get stale stat count (ignores TTL). Used for stale-while-revalidate.
     * Returns Pair(count, cachedAt) or null if never cached.
     */
    suspend fun getStatCountStale(key: String): Pair<Int, Long>? {
        val row = queries.getStatCountStale(key).executeAsOneOrNull() ?: return null
        return Pair(row.count.toInt(), row.cached_at)
    }

    /**
     * Cache a stat count value.
     */
    suspend fun cacheStat(key: String, count: Int) {
        val now = System.now().toEpochMilliseconds()
        val expiresAt = now + getEffectiveCacheDuration().inWholeMilliseconds
        queries.upsertStat(key, count.toLong(), now, expiresAt)
        log.d { "Cached stat '$key' = $count (expires in ${getEffectiveCacheDuration().inWholeMinutes} min)" }
    }

    /**
     * Delete expired stats entries. Called by CacheCleanupService.
     */
    suspend fun deleteExpiredStats() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredStats(now)
        log.d { "Deleted expired stats cache entries" }
    }

    /**
     * Delete all stats entries.
     */
    suspend fun deleteAllStats() {
        queries.deleteAllStats()
    }
}
