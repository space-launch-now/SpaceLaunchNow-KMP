package me.calebjones.spacelaunchnow.data.repository.trantor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import me.calebjones.spacelaunchnow.api.extensions.trantor.getLookups
import me.calebjones.spacelaunchnow.api.trantor.apis.LookupsApi
import me.calebjones.spacelaunchnow.api.trantor.models.LookupItem
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Trantor launch rows carry only `status_id` and the status name ("Success"); the short label
 * ("SUCCESS" style `abbrev`) lives in `GET /api/v1/lookups` -> `launch_statuses`. This resolves
 * `abbrev` by status id from that one payload.
 *
 * The lookups payload is fetched once and kept in memory (no per-launch call). A launch list
 * never waits long for it: the first fetch is bounded by [fetchTimeout], and a failed or slow
 * fetch leaves `abbrev` null (the UI then shows the status name) and is retried no sooner than
 * [retryAfterFailure] later.
 */
class LaunchStatusAbbrevResolver(
    private val fetchStatuses: suspend () -> List<LookupItem>,
    private val fetchTimeout: Duration = 1500.milliseconds,
    private val retryAfterFailure: Duration = 1.minutes
) {
    constructor(lookupsApi: LookupsApi) : this({ lookupsApi.getLookups().body().launchStatuses })

    private val log = logger()
    private val mutex = Mutex()
    private var abbrevById: Map<Int, String>? = null
    private var lastFailure: kotlin.time.Instant? = null

    /** status id -> abbrev; empty when lookups are unavailable. Never throws (except cancellation). */
    suspend fun abbrevs(): Map<Int, String> {
        abbrevById?.let { return it }
        return mutex.withLock {
            abbrevById?.let { return@withLock it }
            val failedAt = lastFailure
            if (failedAt != null && Clock.System.now() - failedAt < retryAfterFailure) {
                return@withLock emptyMap()
            }
            try {
                val items = withTimeoutOrNull(fetchTimeout) { fetchStatuses() }
                if (items == null) {
                    lastFailure = Clock.System.now()
                    log.w { "Lookups fetch timed out; launch status shows names" }
                    emptyMap()
                } else {
                    items.mapNotNull { item -> item.abbrev?.takeIf { it.isNotBlank() }?.let { item.id to it } }
                        .toMap()
                        .also { abbrevById = it }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                lastFailure = Clock.System.now()
                log.w(e) { "Lookups fetch failed; launch status shows names" }
                emptyMap()
            }
        }
    }

    suspend fun resolve(launch: Launch): Launch = launch.withStatusAbbrev(abbrevs())

    suspend fun resolve(page: PaginatedResult<Launch>): PaginatedResult<Launch> {
        val map = abbrevs()
        if (map.isEmpty()) return page
        return page.copy(results = page.results.map { it.withStatusAbbrev(map) })
    }
}

/** Fill `status.abbrev` from [abbrevs] by status id; leaves the launch untouched when absent. */
internal fun Launch.withStatusAbbrev(abbrevs: Map<Int, String>): Launch {
    val current = status ?: return this
    if (current.abbrev != null) return this
    val abbrev = abbrevs[current.id] ?: return this
    return copy(status = current.copy(abbrev = abbrev))
}
