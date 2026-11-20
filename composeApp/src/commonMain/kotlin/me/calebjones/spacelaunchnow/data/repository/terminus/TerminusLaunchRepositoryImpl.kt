package me.calebjones.spacelaunchnow.data.repository.terminus

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.extensions.TimeFilterRounding
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource.CachedValue
import me.calebjones.spacelaunchnow.database.StatsLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.terminus.TerminusLaunchDetail
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Launch repository for the Terminus backend. It serves every [LaunchRepository] method and
 * does not call LL: launch detail (`GET /api/v1/launches/{id}/`, `.../rocket/` and `.../timeline/`), the launch lists
 * (`GET /api/v1/launches/mini/` and `GET /api/v1/launches/`), and the reads built on them
 * (featured, in flight, Starship, by day and the stats count).
 *
 * Every Terminus read goes through [terminusCachedFetch]: fresh cache, then network, then the
 * stale cache when the network fails. A Terminus 404 fails the same way an LL 404 does.
 *
 * The generated Terminus client does not round time filters. [LaunchListQuery] does, so equal
 * requests build equal URLs: lower bounds round down and upper bounds round up (see
 * [TimeFilterRounding]).
 */
class TerminusLaunchRepositoryImpl(
    private val launchesApi: LaunchesApi,
    private val localDataSource: LaunchLocalDataSource? = null,
    private val statsLocalDataSource: StatsLocalDataSource? = null,
    private val appPreferences: AppPreferences? = null
) : LaunchRepository {

    private val log = logger()

    // -- Launch detail --------------------------------------------------------

    override suspend fun getStaleDetailedLaunch(id: String): Launch? =
        localDataSource?.getTerminusDetailedLaunchStale(id)?.toDomain()

    override suspend fun getLaunchDetailDomain(id: String, forceRefresh: Boolean): Result<Launch> =
        getLaunchDetails(id, forceRefresh).mapCatching { it.toDomain() }

    /** A launch Terminus does not know is `success(null)`, as the LL id-filter lookup gives. */
    override suspend fun getLaunchByIdDomain(id: String): Result<Launch?> {
        val result = getLaunchDetails(id, forceRefresh = false)
        if (result.exceptionOrNull() is TerminusNotFoundException) return Result.success(null)
        return result.mapCatching { it.toDomain() }
    }

    /**
     * The launch, its full rocket and its timeline: three reads made at the same time and cached
     * as one entry (Tasks#1078). A read that fails fails the detail, which then falls back to the
     * stale entry. The rocket read result is ignored when the launch has no rocket.
     */
    private suspend fun getLaunchDetails(id: String, forceRefresh: Boolean): Result<TerminusLaunchDetail> {
        log.d { "getLaunchDetails (terminus) - id: $id, forceRefresh: $forceRefresh" }
        return terminusCachedFetchBody(
            what = "launch detail $id",
            forceRefresh = forceRefresh,
            readFresh = { localDataSource?.getTerminusDetailedLaunch(id)?.let { CachedValue(it) } },
            readStale = { localDataSource?.getTerminusDetailedLaunchStale(id)?.let { CachedValue(it) } },
            fetch = { fetchLaunchDetail(id) },
            store = { localDataSource?.cacheTerminusDetailedLaunch(it) }
        ).map { it.value }
    }

    private suspend fun fetchLaunchDetail(id: String): TerminusLaunchDetail = coroutineScope {
        val launch = async { settle { launchesApi.launchesRetrieve(id).bodyOrThrow() } }
        val rocket = async { settle { launchesApi.launchesRocketRetrieve(id).bodyOrThrow() } }
        val timeline = async { settle { launchesApi.launchesTimelineRetrieve(id).bodyOrThrow() } }
        // The launch decides first: a 404 there is a 404 for the detail, whatever the other reads say.
        val launchBody = launch.await().getOrThrow()
        TerminusLaunchDetail(
            launch = launchBody,
            rocket = if (launchBody.rocket == null) null else rocket.await().getOrElse { throw sectionFailure("rocket", it) },
            timeline = timeline.await().getOrElse { throw sectionFailure("timeline", it) }.timeline
        )
    }

    /** A 404 on a section of a launch that exists is a failure, not "launch not found". */
    private fun sectionFailure(section: String, cause: Throwable): Throwable =
        if (cause is TerminusHttpStatusException && cause.status == 404) {
            IllegalStateException("The $section read answered 404 for a launch that exists", cause)
        } else {
            cause
        }

    /** The result of [block], holding every failure but cancellation, so one failed read does not cancel its siblings. */
    private suspend fun <T> settle(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    // -- Launch lists ---------------------------------------------------------

    override suspend fun getUpcomingLaunchesDomain(
        limit: Int,
        offset: Int,
        netGt: Instant?,
        netLt: Instant?,
        forceRefresh: Boolean
    ): Result<PaginatedResult<Launch>> {
        // Same arguments as the LL repository: a time window replaces the `upcoming` flag.
        val query = if (netGt != null || netLt != null) {
            LaunchListQuery.of(limit = limit, offset = offset, netGt = netGt, netLt = netLt, ordering = "net")
        } else {
            LaunchListQuery.of(limit = limit, offset = offset, upcoming = true, ordering = "net")
        }
        return miniList(query, forceRefresh).map { it.value }
    }

    override suspend fun getPreviousLaunchesDomain(
        limit: Int,
        offset: Int,
        forceRefresh: Boolean
    ): Result<PaginatedResult<Launch>> =
        miniList(
            LaunchListQuery.of(limit = limit, offset = offset, previous = true, ordering = "-net"),
            forceRefresh
        ).map { it.value }

    override suspend fun getFilteredLaunchesDomain(
        limit: Int,
        offset: Int,
        upcoming: Boolean?,
        previous: Boolean?,
        ordering: String?,
        search: String?,
        lspIds: List<Int>?,
        locationIds: List<Int>?,
        programIds: List<Int>?,
        rocketConfigurationId: Int?,
        isCrewed: Boolean?,
        includeSuborbital: Boolean?,
        statusIds: List<Int>?,
        orbitIds: List<Int>?,
        missionTypeIds: List<Int>?,
        launcherConfigFamilyIds: List<Int>?,
        forceRefresh: Boolean
    ): Result<PaginatedResult<Launch>> =
        miniList(
            LaunchListQuery.of(
                limit = limit,
                offset = offset,
                upcoming = upcoming,
                previous = previous,
                ordering = ordering,
                search = search,
                lspId = lspIds,
                locationIds = locationIds,
                program = programIds,
                rocketConfigurationId = rocketConfigurationId,
                isCrewed = isCrewed,
                includeSuborbital = includeSuborbital,
                statusIds = statusIds,
                orbitIds = orbitIds,
                missionTypeIds = missionTypeIds,
                launcherConfigFamilyIds = launcherConfigFamilyIds
            ),
            forceRefresh
        ).map { it.value }

    override suspend fun getUpcomingLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val query = LaunchListQuery.of(
            limit = limit,
            upcoming = true,
            ordering = "net",
            lspId = agencyIds,
            locationIds = locationIds,
            statusIds = hideTbdStatusIds()
        )
        return normalList(query, forceRefresh)
    }

    /**
     * The widget list: `/launches/mini/` (15 KB for 10 rows, against 87 KB for the normal list)
     * with the filters of the home list. The widgets read these fields only: name, NET, status,
     * provider and location name.
     */
    override suspend fun getUpcomingLaunchesForWidget(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> =
        miniList(widgetListQuery(limit, agencyIds, locationIds, hideTbdStatusIds()), forceRefresh)
            .map { DataResult(it.value, it.source, it.timestamp) }

    /**
     * The newest of the lists that hold the widget's rows, read with no network call and no
     * expiry check: the widget's own mini list, and the normal lists the home screen stores for
     * the same filters (Home reads [limit] rows, the preload reads [PRELOAD_LIMIT]). A list with
     * fewer rows than [limit] is drawn as it is. The longer list wins a tie in time.
     */
    override suspend fun getCachedUpcomingLaunchesForWidget(
        limit: Int,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): DataResult<PaginatedResult<Launch>>? {
        val local = localDataSource ?: return null
        val statusIds = hideTbdStatusIds()
        fun query(rows: Int) = widgetListQuery(rows, agencyIds, locationIds, statusIds)

        // An entry that cannot be mapped is a miss, as for every other cache read.
        fun <T> CachedValue<T>?.stored(toDomain: (T) -> PaginatedResult<Launch>): DataResult<PaginatedResult<Launch>>? =
            this?.let { cached ->
                runCatching { toDomain(cached.value) }.getOrNull()
                    ?.let { DataResult(it, DataSource.STALE_CACHE, cached.cachedAt ?: 0L) }
            }

        val candidates = buildList {
            add(local.getTerminusMiniListStale(query(limit).key).stored { it.toDomain() })
            for (rows in listOf(limit, PRELOAD_LIMIT).distinct()) {
                add(local.getTerminusNormalListStale(query(rows).key).stored { it.toDomain() })
            }
        }.filterNotNull()
        return candidates.maxWithOrNull(compareBy({ it.timestamp }, { it.data.results.size }))
    }

    private fun widgetListQuery(
        limit: Int,
        agencyIds: List<Int>?,
        locationIds: List<Int>?,
        statusIds: List<Int>?
    ): LaunchListQuery = LaunchListQuery.of(
        limit = limit,
        upcoming = true,
        ordering = "net",
        lspId = agencyIds,
        locationIds = locationIds,
        statusIds = statusIds
    )

    /** Same filter as the LL repository: hide TBD (2) and TBC (8) when the user asks. */
    private suspend fun hideTbdStatusIds(): List<Int>? {
        val hideTbd = appPreferences?.let { withContext(Dispatchers.Default) { it.getHideTbdLaunches() } } ?: false
        return if (hideTbd) listOf(1, 3, 4, 5, 6, 7, 9) else null
    }

    override suspend fun getPreviousLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> =
        normalList(
            LaunchListQuery.of(
                limit = limit,
                previous = true,
                ordering = "-net",
                lspId = agencyIds,
                locationIds = locationIds
            ),
            forceRefresh
        )

    // -- Reads built on the lists ---------------------------------------------

    /** Same request as the LL repository: the next 4 launches from one hour ago, hide-TBD applied. */
    override suspend fun getFeaturedLaunchDomain(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> =
        normalList(
            LaunchListQuery.of(
                limit = FEATURED_LIMIT,
                netGt = Clock.System.now() - 1.hours,
                ordering = "net",
                lspId = agencyIds,
                locationIds = locationIds,
                statusIds = hideTbdStatusIds()
            ),
            forceRefresh
        )

    override suspend fun getInFlightLaunchesDomain(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> =
        normalList(
            LaunchListQuery.of(
                limit = IN_FLIGHT_LIMIT,
                ordering = "net",
                lspId = agencyIds,
                locationIds = locationIds,
                statusIds = listOf(IN_FLIGHT_STATUS_ID)
            ),
            forceRefresh
        )

    override suspend fun getStarshipLaunchesDomain(
        limit: Int,
        forceRefresh: Boolean,
        programId: List<Int>?
    ): Result<PaginatedResult<Launch>> =
        normalList(
            LaunchListQuery.of(limit = limit, upcoming = true, ordering = "net", program = programId),
            forceRefresh
        ).map { it.data }

    /** Every previous launch of the Starship program, newest first; read 25 rows at a time. */
    override suspend fun getStarshipHistoryDomain(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Launch>>> =
        terminusPagedList(limit) { pageLimit, offset ->
            normalList(
                LaunchListQuery.of(
                    limit = pageLimit,
                    offset = offset,
                    previous = true,
                    ordering = "-net",
                    program = listOf(STARSHIP_PROGRAM_ID)
                ),
                forceRefresh
            )
        }

    /** Past launches on one day of one month, newest first; read 25 rows at a time. */
    override suspend fun getLaunchesByDayAndMonth(
        day: Int,
        month: Int,
        limit: Int,
        forceRefresh: Boolean
    ): Result<PaginatedResult<Launch>> =
        terminusPagedList(limit) { pageLimit, offset ->
            normalList(
                LaunchListQuery.of(
                    limit = pageLimit,
                    offset = offset,
                    previous = true,
                    netDay = listOf(day),
                    netMonth = listOf(month),
                    ordering = "-net"
                ),
                forceRefresh
            )
        }.map { it.data }

    /**
     * The `count` of a one-row page of upcoming launches in the window. The stats cache keeps the
     * count only, so a cache hit is a page that holds that count and no rows.
     */
    override suspend fun getStatsCount(
        key: String,
        netGt: Instant,
        netLt: Instant,
        forceRefresh: Boolean
    ): Result<DataResult<Int>> {
        val query = LaunchListQuery.of(limit = 1, upcoming = true, netGt = netGt, netLt = netLt)
        fun countOnly(count: Int) = PaginatedLaunchBasicList(count = count, results = emptyList())
        return terminusCachedFetch(
            what = "stats count $key",
            forceRefresh = forceRefresh,
            readFresh = { statsLocalDataSource?.getStatCount(key)?.let { CachedValue(countOnly(it)) } },
            readStale = {
                statsLocalDataSource?.getStatCountStale(key)
                    ?.let { (count, cachedAt) -> CachedValue(countOnly(count), cachedAt) }
            },
            fetch = { query.fetchMini(launchesApi) },
            store = { statsLocalDataSource?.cacheStat(key, it.count) }
        ).map { DataResult(it.value.count, it.source, it.timestamp) }
    }

    private suspend fun miniList(
        query: LaunchListQuery,
        forceRefresh: Boolean
    ): Result<TerminusFetched<PaginatedResult<Launch>>> =
        terminusCachedFetch(
            what = "launch mini list ${query.logLabel}",
            forceRefresh = forceRefresh,
            readFresh = { localDataSource?.getTerminusMiniList(query.key) },
            readStale = { localDataSource?.getTerminusMiniListStale(query.key) },
            fetch = { query.fetchMini(launchesApi) },
            store = { localDataSource?.cacheTerminusMiniList(query.key, it) }
        ).mapCatching { TerminusFetched(it.value.toDomain(), it.source, it.timestamp) }

    private suspend fun normalList(
        query: LaunchListQuery,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Launch>>> =
        terminusCachedFetch(
            what = "launch list ${query.logLabel}",
            forceRefresh = forceRefresh,
            readFresh = { localDataSource?.getTerminusNormalList(query.key) },
            readStale = { localDataSource?.getTerminusNormalListStale(query.key) },
            fetch = { query.fetchNormal(launchesApi) },
            store = { localDataSource?.cacheTerminusNormalList(query.key, it) }
        ).mapCatching { DataResult(it.value.toDomain(), it.source, it.timestamp) }

    private companion object {
        const val FEATURED_LIMIT = 4
        /** Rows the preload reads for the home list. */
        const val PRELOAD_LIMIT = 8
        const val IN_FLIGHT_LIMIT = 5
        const val IN_FLIGHT_STATUS_ID = 6
        const val STARSHIP_PROGRAM_ID = 1
    }
}
