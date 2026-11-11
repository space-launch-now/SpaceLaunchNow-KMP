package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.UpdatesApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Update
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Updates repository for the Terminus backend. Both [UpdatesRepository] methods are served by
 * `GET /api/v1/updates/` (newest first, `-created_on`), so nothing delegates to LL. `launch`
 * maps to a LaunchRef and `created_by` stays text.
 *
 * Reads use [terminusCachedFetch] with a [TerminusMemoryCache] (30 s fresh, stale kept for a 5xx
 * or a network failure); see [TerminusEventsRepositoryImpl] for why the cache is in memory.
 */
class TerminusUpdatesRepositoryImpl internal constructor(
    private val updatesApi: UpdatesApi,
    private val cache: TerminusMemoryCache
) : UpdatesRepository {

    constructor(updatesApi: UpdatesApi) : this(updatesApi, TerminusMemoryCache())

    private val log = logger()

    override suspend fun getLatestUpdates(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Update>>> =
        fetchPage(limit = limit, allProgram = null, forceRefresh = forceRefresh)

    override suspend fun getUpdatesByProgram(
        allProgram: Int,
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Update>>> =
        fetchPage(limit = limit, allProgram = allProgram, forceRefresh = forceRefresh)

    private suspend fun fetchPage(
        limit: Int,
        allProgram: Int?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Update>>> {
        val ordering = "-created_on"
        val key = terminusCacheKey(
            "updates",
            "limit" to limit,
            "ordering" to LaunchListCacheKey.encode(ordering),
            "all_program" to allProgram
        )
        log.d { "terminus read updates limit=$limit allProgram=$allProgram forceRefresh=$forceRefresh" }
        return terminusCachedFetch(
            what = "updates limit=$limit allProgram=$allProgram",
            forceRefresh = forceRefresh,
            readFresh = { cache.fresh(key) },
            readStale = { cache.stale(key) },
            fetch = { updatesApi.updatesList(limit = limit, ordering = ordering, allProgram = allProgram) },
            store = { cache.put(key, it) }
        ).mapCatching { DataResult(it.value.toDomain(), it.source, it.timestamp) }
    }
}
