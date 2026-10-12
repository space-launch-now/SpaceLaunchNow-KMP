package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

/**
 * Schema version for the cached [Launch] JSON envelope (ADR-0004:
 * docs/architecture/adr/0004-cache-schema-versioning.md - "cache stores domain JSON with
 * explicit schema versions"). Bump this whenever a change to the domain `Launch` graph would
 * make previously-cached JSON undecodable or, worse, decodable-but-wrong (e.g. a field is
 * renamed so `ignoreUnknownKeys` would silently drop it instead of throwing). A row whose
 * embedded version doesn't match this constant - or that fails to decode at all - is always
 * treated as a cache miss, never partially trusted.
 *
 * ADR-0004 describes a `schema_version` SQL column per cache table. This cache instead embeds
 * the version inside the JSON blob itself (see [CachedLaunchEnvelope]) so invalidation doesn't
 * require an SQLDelight schema/version bump in `build.gradle.kts` (out of scope for the
 * phase5-launch unit that introduced this). Functionally equivalent for ADR-0004's purpose: a
 * version mismatch or decode failure both resolve to "cache miss, re-fetch from network."
 */
object CacheSchemaVersion {
    const val LAUNCH = 1
}

/**
 * What's actually stored in a launch cache row's `json_data` column: the domain [Launch] plus
 * the schema version it was written with. See [CacheSchemaVersion].
 */
@Serializable
internal data class CachedLaunchEnvelope(
    val schemaVersion: Int,
    val launch: Launch
)

/**
 * Local data source for launch data using SQLDelight.
 *
 * Caches the **domain** [Launch] model (see ADR-0004) rather than any LL/Trantor API model -
 * this is what lets one cache work across both API generations and survive future API
 * reshaping, per the phase5-launch migration decision (was previously LL-model JSON only;
 * see the unit report / git history for the full rationale).
 *
 * Three buckets, matching how launches actually enter the cache:
 *  - "Normal" (`LaunchNormalCache`): any launch that arrived via a *list* fetch (upcoming,
 *    previous, featured, in-flight, current Starship program launches). These are sparser -
 *    Trantor's flat list row has no window_start/window_end/pad-location/mission-description,
 *    so those domain fields are null for list-sourced entries; see TrantorLaunchMappers.
 *  - "Detailed" (`LaunchDetailedCache`): a launch fetched via the single-launch detail
 *    endpoint. Richer - all of the above are populated.
 *  - "StarshipHistory" (`StarshipHistoryCache`): previous Starship-program launches, kept in
 *    their own table with a 30-day TTL instead of the normal short TTL, same as before this
 *    migration.
 *
 * A fourth bucket, `LaunchBasicCache`, existed before this migration backing a "mini" LL list
 * tier that had zero callers (dead code) even before Trantor; its query wrappers were dropped
 * as part of this rewrite. The table itself is untouched on disk (removing it would need a
 * schema migration - out of scope here).
 *
 * Every read path treats a decode failure or schema-version mismatch as a cache miss (returns
 * null / filters the row out) rather than throwing - this is what makes it safe for an
 * on-device cache still holding pre-migration LL-shaped JSON: those rows simply fail to decode
 * as [CachedLaunchEnvelope] (different field names/shape entirely) and are ignored. See
 * [decodeLaunch] and `LaunchLocalDataSourceTest` for the invalidation contract.
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

    private fun encodeLaunch(launch: Launch): String =
        json.encodeToString(CachedLaunchEnvelope(schemaVersion = CacheSchemaVersion.LAUNCH, launch = launch))

    /**
     * Decode a cached row's `json_data` back into a domain [Launch]. Treats both a decode
     * failure (corrupt JSON, or a legacy pre-migration LL-model-shaped blob that simply
     * doesn't have this shape) and a schema-version mismatch as a cache miss - returns null,
     * never throws.
     */
    private fun decodeLaunch(jsonData: String, context: String): Launch? {
        return try {
            val envelope = json.decodeFromString<CachedLaunchEnvelope>(jsonData)
            if (envelope.schemaVersion != CacheSchemaVersion.LAUNCH) {
                log.w {
                    "Cache schema mismatch for $context (found version ${envelope.schemaVersion}, " +
                        "want ${CacheSchemaVersion.LAUNCH}); treating as cache miss"
                }
                null
            } else {
                envelope.launch
            }
        } catch (e: Exception) {
            log.w(e) { "Failed to decode cached launch for $context (legacy or corrupt blob?); treating as cache miss" }
            null
        }
    }

    // ── LaunchNormalCache: list-sourced launches ──────────────────────────

    suspend fun cacheListLaunch(launch: Launch) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceNormal(
            id = launch.id,
            name = launch.name,
            status_id = launch.status?.id?.toLong(),
            status_name = launch.status?.name,
            net = launch.net?.toEpochMilliseconds(),
            window_end = launch.windowEnd?.toEpochMilliseconds(),
            window_start = launch.windowStart?.toEpochMilliseconds(),
            launch_service_provider_id = launch.provider.id.toLong(),
            launch_service_provider_name = launch.provider.name,
            rocket_configuration_id = launch.rocket?.id?.toLong(),
            rocket_configuration_name = launch.rocket?.name,
            pad_name = launch.pad?.name,
            location_name = launch.pad?.location?.name,
            image_url = launch.imageUrl,
            mission_name = launch.mission?.name,
            mission_description = launch.mission?.description,
            json_data = encodeLaunch(launch),
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheListLaunches(launches: List<Launch>) {
        launches.forEach { cacheListLaunch(it) }
    }

    suspend fun getListLaunch(id: String): Launch? {
        val now = System.now().toEpochMilliseconds()
        return queries.getNormalById(id, now).executeAsOneOrNull()
            ?.let { decodeLaunch(it.json_data, "getListLaunch($id)") }
    }

    suspend fun getUpcomingListLaunches(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getUpcomingNormal(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getUpcomingListLaunches") }
    }

    suspend fun getUpcomingListLaunchesStale(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getUpcomingNormalStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getUpcomingListLaunchesStale") }
    }

    suspend fun getPreviousListLaunches(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getPreviousNormal(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getPreviousListLaunches") }
    }

    suspend fun getPreviousListLaunchesStale(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getPreviousNormalStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getPreviousListLaunchesStale") }
    }

    // In-flight launch cache methods (status_id = 6)
    suspend fun getInFlightListLaunches(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getInFlightNormal(now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getInFlightListLaunches") }
    }

    suspend fun getInFlightListLaunchesStale(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getInFlightNormalStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getInFlightListLaunchesStale") }
    }

    // ── LaunchDetailedCache: single-launch detail fetches ─────────────────

    suspend fun cacheDetailedLaunch(launch: Launch) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceDetailed(
            id = launch.id,
            name = launch.name,
            status_id = launch.status?.id?.toLong(),
            status_name = launch.status?.name,
            net = launch.net?.toEpochMilliseconds(),
            window_end = launch.windowEnd?.toEpochMilliseconds(),
            window_start = launch.windowStart?.toEpochMilliseconds(),
            launch_service_provider_id = launch.provider.id.toLong(),
            launch_service_provider_name = launch.provider.name,
            rocket_configuration_id = launch.rocket?.id?.toLong(),
            rocket_configuration_name = launch.rocket?.name,
            pad_name = launch.pad?.name,
            location_name = launch.pad?.location?.name,
            image_url = launch.imageUrl,
            mission_name = launch.mission?.name,
            mission_description = launch.mission?.description,
            json_data = encodeLaunch(launch),
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun getDetailedLaunch(id: String): Launch? {
        val now = System.now().toEpochMilliseconds()
        return queries.getDetailedById(id, now).executeAsOneOrNull()
            ?.let { decodeLaunch(it.json_data, "getDetailedLaunch($id)") }
    }

    /**
     * Get stale detailed launch data from local cache without checking TTL, for the
     * stale-while-revalidate UI pattern.
     */
    suspend fun getDetailedLaunchStale(id: String): Launch? {
        return queries.getDetailedByIdStale(id).executeAsOneOrNull()
            ?.let { decodeLaunch(it.json_data, "getDetailedLaunchStale($id)") }
    }

    // ── StarshipHistoryCache: previous Starship-program launches, 30-day TTL ──

    suspend fun cacheStarshipHistory(launches: List<Launch>) {
        val now = System.now().toEpochMilliseconds()
        val oneMonthMs = 2592000000L // 30 days in milliseconds
        val expiresAt = now + oneMonthMs

        launches.forEach { launch ->
            queries.insertOrReplaceStarshipHistory(
                id = launch.id,
                name = launch.name,
                status_id = launch.status?.id?.toLong(),
                status_name = launch.status?.name,
                net = launch.net?.toEpochMilliseconds(),
                window_end = launch.windowEnd?.toEpochMilliseconds(),
                window_start = launch.windowStart?.toEpochMilliseconds(),
                launch_service_provider_id = launch.provider.id.toLong(),
                launch_service_provider_name = launch.provider.name,
                rocket_configuration_id = launch.rocket?.id?.toLong(),
                rocket_configuration_name = launch.rocket?.name,
                pad_name = launch.pad?.name,
                location_name = launch.pad?.location?.name,
                image_url = launch.imageUrl,
                mission_name = launch.mission?.name,
                mission_description = launch.mission?.description,
                json_data = encodeLaunch(launch),
                cached_at = now,
                expires_at = expiresAt
            )
        }
    }

    suspend fun getStarshipHistory(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getStarshipHistory(now, now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getStarshipHistory") }
    }

    suspend fun getStarshipHistoryStale(limit: Int): List<Launch> {
        val now = System.now().toEpochMilliseconds()
        return queries.getStarshipHistoryStale(now, limit.toLong())
            .executeAsList()
            .mapNotNull { decodeLaunch(it.json_data, "getStarshipHistoryStale") }
    }

    // ── Cache metadata operations ──────────────────────────────────────────

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

    // ── Cleanup operations ──────────────────────────────────────────────────

    suspend fun deleteExpiredLaunches() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredNormal(now)
        queries.deleteExpiredDetailed(now)
    }

    suspend fun clearAllLaunches() {
        queries.clearAllNormal()
        queries.clearAllDetailed()
    }
}
