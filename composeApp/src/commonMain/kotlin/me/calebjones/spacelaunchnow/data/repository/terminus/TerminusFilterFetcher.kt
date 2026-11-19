package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource.CachedValue
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private val log get() = SpaceLogger.getLogger("TerminusFilterFetcher")

/** One page of a Terminus list, reduced to the filter options it holds. */
internal class FilterPage(val options: List<FilterOption>, val total: Int)

/**
 * Reads whole Terminus lists for the filter screens (Tasks#1010, Tasks#1207). Each list is one
 * unit read through [terminusCachedFetchBody]: a fresh copy, else the network, else a stale copy
 * when the network fails (5xx, transport or I/O failure). `forceRefresh` skips the fresh read.
 *
 * Two copies sit behind each other. The memory cache is fresh for an hour. Behind it, [store]
 * keeps the list on disk with its read time, fresh for [FILTER_DISK_TTL] (7 days, or
 * [DEBUG_DISK_TTL] when the debug short-cache switch is on), so a cold start reads none. A fetch
 * writes both. Rows live in their own table, apart from the LL filter tables.
 *
 * Terminus has no `/lookups` endpoint in `schema/terminus_v1.yaml`, so each filter reads its own
 * list or `/config/...` endpoint.
 */
internal class TerminusFilterFetcher(
    private val store: FilterOptionStore,
    private val diskTtl: suspend () -> Duration,
    private val cache: TerminusMemoryCache = TerminusMemoryCache(ttl = 1.hours, maxEntries = 128),
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    constructor(store: FilterOptionStore, appPreferences: AppPreferences) : this(
        store,
        { if (appPreferences.isDebugShortCacheTtlEnabled()) DEBUG_DISK_TTL else FILTER_DISK_TTL }
    )

    /**
     * Pages through one list ([PAGE_SIZE] rows at a time) until [FilterPage.total] rows are in,
     * or a page comes back empty. It also stops after [MAX_PAGES] pages and returns the rows read
     * so far, so a server that never ends its list cannot loop the app. [name] keys the cache and labels the log.
     */
    suspend fun <P : Any> options(
        name: String,
        forceRefresh: Boolean,
        page: suspend (limit: Int, offset: Int) -> HttpResponse<P>,
        toPage: (P) -> FilterPage
    ): Result<List<FilterOption>> {
        val key = "filter:$name"
        return terminusCachedFetchBody(
            what = "$name filter options",
            forceRefresh = forceRefresh,
            readFresh = { cache.fresh<List<FilterOption>>(key) ?: freshFromDisk(key) },
            readStale = { cache.stale<List<FilterOption>>(key) ?: store.read(key) },
            fetch = { readAll(name, page, toPage) },
            store = {
                cache.put(key, it)
                store.write(key, it, now())
            }
        ).map { it.value }
    }

    /** The disk copy when it is younger than the disk TTL; it also goes into memory. */
    private suspend fun freshFromDisk(key: String): CachedValue<List<FilterOption>>? {
        val stored = store.read(key) ?: return null
        val age = now() - (stored.cachedAt ?: return null)
        if (age >= diskTtl().inWholeMilliseconds) return null
        cache.put(key, stored.value)
        return stored
    }

    private suspend fun <P : Any> readAll(
        name: String,
        page: suspend (limit: Int, offset: Int) -> HttpResponse<P>,
        toPage: (P) -> FilterPage
    ): List<FilterOption> {
        val all = mutableListOf<FilterOption>()
        var offset = 0
        repeat(MAX_PAGES) {
            val current = toPage(page(PAGE_SIZE, offset).bodyOrThrow())
            all += current.options
            offset += PAGE_SIZE
            if (current.options.isEmpty() || all.size >= current.total) return all
        }
        log.w { "$name filter options hit the $MAX_PAGES page cap; using the ${all.size} rows read" }
        return all
    }

    companion object {
        const val PAGE_SIZE = 25

        /** Upper bound on pages per list; 50 pages of 25 is 1,250 rows, far above any real filter list (rockets, the largest, has 527). */
        const val MAX_PAGES = 50

        /** How long a disk copy stays fresh (Tasks#1207; the same 7 days as the LL filter tables). */
        val FILTER_DISK_TTL = 7.days

        /** The disk TTL when the debug short-cache switch is on. */
        val DEBUG_DISK_TTL = 1.hours
    }
}
