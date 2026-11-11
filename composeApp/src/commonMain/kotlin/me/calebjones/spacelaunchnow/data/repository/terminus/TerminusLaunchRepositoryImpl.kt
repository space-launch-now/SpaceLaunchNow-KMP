package me.calebjones.spacelaunchnow.data.repository.terminus

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.extensions.TimeFilterRounding
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Launch repository for the Terminus backend. Launch detail (`GET /api/v1/launches/{id}/`) and
 * the launch lists (`GET /api/v1/launches/mini/` and `GET /api/v1/launches/`) come from
 * Terminus; every other call (featured, in flight, Starship, stats, by day) goes to [delegate],
 * the LL repository, until its own port lands.
 *
 * Every Terminus read goes through [terminusCachedFetch]: fresh cache, then network, then the
 * stale cache when the network fails. A Terminus 404 fails the same way an LL 404 does.
 *
 * The generated Terminus client does not round time filters. [LaunchListQuery] does, so equal
 * requests build equal URLs: lower bounds round down and upper bounds round up (see
 * [TimeFilterRounding]).
 */
class TerminusLaunchRepositoryImpl(
    private val delegate: LaunchRepository,
    private val launchesApi: LaunchesApi,
    private val localDataSource: LaunchLocalDataSource? = null,
    private val appPreferences: AppPreferences? = null
) : LaunchRepository by delegate {

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

    private suspend fun getLaunchDetails(id: String, forceRefresh: Boolean): Result<LaunchDetailed> {
        log.d { "getLaunchDetails (terminus) - id: $id, forceRefresh: $forceRefresh" }
        return terminusCachedFetch(
            what = "launch detail $id",
            forceRefresh = forceRefresh,
            readFresh = { localDataSource?.getTerminusDetailedLaunch(id)?.let { TerminusCacheHit(it) } },
            readStale = { localDataSource?.getTerminusDetailedLaunchStale(id)?.let { TerminusCacheHit(it) } },
            fetch = { launchesApi.launchesRetrieve(id) },
            store = { localDataSource?.cacheTerminusDetailedLaunch(it) }
        ).map { it.value }
    }

    // -- Launch lists ---------------------------------------------------------

    override suspend fun getUpcomingLaunchesDomain(
        limit: Int,
        offset: Int,
        netGt: Instant?,
        netLt: Instant?
    ): Result<PaginatedResult<Launch>> {
        // Same arguments as the LL repository: a time window replaces the `upcoming` flag.
        val query = if (netGt != null || netLt != null) {
            LaunchListQuery.of(limit = limit, offset = offset, netGt = netGt, netLt = netLt, ordering = "net")
        } else {
            LaunchListQuery.of(limit = limit, offset = offset, upcoming = true, ordering = "net")
        }
        return miniList(query).map { it.value }
    }

    override suspend fun getPreviousLaunchesDomain(
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<Launch>> =
        miniList(LaunchListQuery.of(limit = limit, offset = offset, previous = true, ordering = "-net"))
            .map { it.value }

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
        launcherConfigFamilyIds: List<Int>?
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
            )
        ).map { it.value }

    override suspend fun getUpcomingLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        // Same arguments as the LL repository: hide TBD (2) and TBC (8) when the user asks.
        val hideTbd = appPreferences?.let { withContext(Dispatchers.Default) { it.getHideTbdLaunches() } } ?: false
        val query = LaunchListQuery.of(
            limit = limit,
            upcoming = true,
            ordering = "net",
            lspId = agencyIds,
            locationIds = locationIds,
            statusIds = if (hideTbd) listOf(1, 3, 4, 5, 6, 7, 9) else null
        )
        return normalList(query, forceRefresh)
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

    private suspend fun miniList(query: LaunchListQuery): Result<TerminusFetched<PaginatedResult<Launch>>> =
        terminusCachedFetch(
            what = "launch mini list ${query.logLabel}",
            forceRefresh = false,
            readFresh = {
                localDataSource?.getTerminusMiniList(query.key)?.let { TerminusCacheHit(it.value, it.cachedAt) }
            },
            readStale = {
                localDataSource?.getTerminusMiniListStale(query.key)?.let { TerminusCacheHit(it.value, it.cachedAt) }
            },
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
            readFresh = {
                localDataSource?.getTerminusNormalList(query.key)?.let { TerminusCacheHit(it.value, it.cachedAt) }
            },
            readStale = {
                localDataSource?.getTerminusNormalListStale(query.key)?.let { TerminusCacheHit(it.value, it.cachedAt) }
            },
            fetch = { query.fetchNormal(launchesApi) },
            store = { localDataSource?.cacheTerminusNormalList(query.key, it) }
        ).mapCatching { DataResult(it.value.toDomain(), it.source, it.timestamp) }
}
