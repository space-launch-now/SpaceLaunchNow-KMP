package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.time.Duration.Companion.hours

private val log get() = SpaceLogger.getLogger("TerminusFilterFetcher")

/** One page of a Terminus list, reduced to the filter options it holds. */
internal class FilterPage(val options: List<FilterOption>, val total: Int)

/**
 * Reads whole Terminus lists for the filter screens (Tasks#1010). Each page goes through
 * [terminusMemoryCachedFetch], so a fresh page is served from memory, a 5xx or a network failure
 * falls back to the stale page, and `forceRefresh` skips the fresh read. Filter lists change
 * rarely, so a page stays fresh for an hour, not the 30 s of the event and update reads.
 *
 * Terminus has no `/lookups` endpoint in `schema/terminus_v1.yaml`, so each filter reads its own
 * list or `/config/...` endpoint. Nothing is persisted: the SQLDelight filter tables hold LL ids
 * and a restart starts with an empty cache.
 */
internal class TerminusFilterFetcher(
    private val cache: TerminusMemoryCache = TerminusMemoryCache(ttl = 1.hours, maxEntries = 128)
) {
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
        val all = mutableListOf<FilterOption>()
        var offset = 0
        repeat(MAX_PAGES) {
            val fetched = terminusMemoryCachedFetch(
                cache = cache,
                key = "filter:$name?offset=$offset",
                what = "$name filter options (offset $offset)",
                forceRefresh = forceRefresh
            ) { page(PAGE_SIZE, offset) }
            val current = fetched.getOrElse { return Result.failure(it) }.let { toPage(it.value) }
            all += current.options
            offset += PAGE_SIZE
            if (current.options.isEmpty() || all.size >= current.total) return Result.success(all)
        }
        log.w { "$name filter options hit the $MAX_PAGES page cap; using the ${all.size} rows read" }
        return Result.success(all)
    }

    companion object {
        const val PAGE_SIZE = 25

        /** Upper bound on pages per list; 50 pages of 25 is 1,250 rows, far above any real filter list (rockets, the largest, has 527). */
        const val MAX_PAGES = 50
    }
}
