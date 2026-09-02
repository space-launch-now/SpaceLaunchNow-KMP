package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getAgencyDetail
import me.calebjones.spacelaunchnow.api.extensions.getLaunchDetail
import me.calebjones.spacelaunchnow.api.extensions.getLaunchList
import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchesApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.StatsLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Launch repository, backed by the Trantor API (`/api/v1/launches`, `/api/v1/agencies/{id}`).
 *
 * Per the phase5-launch migration decision: every path here fetches straight from Trantor and
 * maps to domain - there is no stale-while-revalidate read/write against [LaunchLocalDataSource]
 * anymore (that cache serializes the old LL-shaped models and doesn't apply to Trantor's flat
 * rows; see the unit report for the full rationale). [localDataSource] is kept only for
 * [getStaleDetailedLaunch], which reads a previously-cached LL-era detail via its
 * already-domain-typed accessor and doesn't call Trantor at all. [statsLocalDataSource] keeps
 * its existing cache-a-plain-Int behavior unchanged, since that was never LL-model-shaped.
 */
class LaunchRepositoryImpl(
    private val launchesApi: LaunchesApi,
    private val agenciesApi: AgenciesApi,
    private val appPreferences: AppPreferences,
    private val localDataSource: LaunchLocalDataSource? = null,
    private val statsLocalDataSource: StatsLocalDataSource? = null
) : LaunchRepository {

    private val log = logger()

    /** Status ids to exclude when the user has enabled "hide TBD launches" (TBD=2, TBC=8). */
    private suspend fun hideTbdStatusIds(): List<Int>? {
        val hideTbd = withContext(Dispatchers.Default) { appPreferences.getHideTbdLaunches() }
        return if (hideTbd) listOf(1, 3, 4, 5, 6, 7, 9) else null
    }

    private suspend fun <T> apiCall(label: String, block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: ResponseException) {
        log.e(e) { "API error in $label: ${e.message}" }
        Result.failure(e)
    } catch (e: IOException) {
        log.e(e) { "Network error in $label: ${e.message}" }
        Result.failure(e)
    } catch (e: Exception) {
        log.e(e) { "Unexpected error in $label: ${e::class.simpleName}: ${e.message}" }
        Result.failure(e)
    }

    // ── Domain-returning method implementations ───────────────────────────

    override suspend fun getUpcomingLaunchesDomain(
        limit: Int,
        offset: Int,
        netGt: Instant?,
        netLt: Instant?
    ): Result<PaginatedResult<Launch>> = apiCall("getUpcomingLaunchesDomain") {
        launchesApi.getLaunchList(
            limit = limit,
            offset = offset,
            upcoming = if (netGt == null && netLt == null) true else null,
            netAfter = netGt,
            netBefore = netLt,
            ordering = "net"
        ).body().toDomain()
    }

    override suspend fun getPreviousLaunchesDomain(
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<Launch>> = apiCall("getPreviousLaunchesDomain") {
        launchesApi.getLaunchList(
            limit = limit,
            offset = offset,
            previous = true,
            ordering = "-net"
        ).body().toDomain()
    }

    override suspend fun getFeaturedLaunchDomain(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> = apiCall("getFeaturedLaunchDomain") {
        val oneHourAgo = Clock.System.now() - 1.hours
        val launches = launchesApi.getLaunchList(
            limit = 4,
            netAfter = oneHourAgo,
            ordering = "net",
            providerIds = agencyIds,
            locationIds = locationIds,
            statusIds = hideTbdStatusIds()
        ).body().toDomain()
        DataResult(data = launches, source = DataSource.NETWORK, timestamp = Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun getInFlightLaunchesDomain(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> = apiCall("getInFlightLaunchesDomain") {
        val launches = launchesApi.getLaunchList(
            statusIds = listOf(6), // In Flight
            providerIds = agencyIds,
            locationIds = locationIds,
            limit = 5,
            ordering = "net"
        ).body().toDomain()
        DataResult(data = launches, source = DataSource.NETWORK, timestamp = Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun getUpcomingLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> = apiCall("getUpcomingLaunchesNormalDomain") {
        val launches = launchesApi.getLaunchList(
            limit = limit,
            upcoming = true,
            ordering = "net",
            providerIds = agencyIds,
            locationIds = locationIds,
            statusIds = hideTbdStatusIds()
        ).body().toDomain()
        DataResult(data = launches, source = DataSource.NETWORK, timestamp = Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun getPreviousLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> = apiCall("getPreviousLaunchesNormalDomain") {
        val launches = launchesApi.getLaunchList(
            limit = limit,
            previous = true,
            ordering = "-net",
            providerIds = agencyIds,
            locationIds = locationIds
        ).body().toDomain()
        DataResult(data = launches, source = DataSource.NETWORK, timestamp = Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun getLaunchDetailDomain(
        id: String,
        forceRefresh: Boolean
    ): Result<Launch> = apiCall("getLaunchDetailDomain") {
        launchesApi.getLaunchDetail(id).body().toDomain()
    }

    override suspend fun getStarshipLaunchesDomain(
        limit: Int,
        forceRefresh: Boolean,
        programId: List<Int>?
    ): Result<PaginatedResult<Launch>> = apiCall("getStarshipLaunchesDomain") {
        launchesApi.getLaunchList(
            limit = limit,
            upcoming = true,
            ordering = "net",
            programIds = programId ?: listOf(1)
        ).body().toDomain()
    }

    override suspend fun getStarshipHistoryDomain(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Launch>>> = apiCall("getStarshipHistoryDomain") {
        val launches = launchesApi.getLaunchList(
            limit = limit,
            previous = true,
            ordering = "-net",
            programIds = listOf(1) // Starship program
        ).body().toDomain()
        DataResult(data = launches, source = DataSource.NETWORK, timestamp = Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun getLaunchByIdDomain(id: String): Result<Launch?> = apiCall("getLaunchByIdDomain") {
        launchesApi.getLaunchDetail(id).body().toDomain()
    }

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
    ): Result<PaginatedResult<Launch>> = apiCall("getFilteredLaunchesDomain") {
        launchesApi.getLaunchList(
            limit = limit,
            offset = offset,
            upcoming = upcoming,
            previous = previous,
            ordering = ordering,
            search = search,
            providerIds = lspIds,
            locationIds = locationIds,
            programIds = programIds,
            rocketConfigId = rocketConfigurationId,
            isCrewed = isCrewed,
            includeSuborbital = includeSuborbital,
            statusIds = statusIds,
            orbitIds = orbitIds,
            missionTypeIds = missionTypeIds,
            familyIds = launcherConfigFamilyIds
        ).body().toDomain()
    }

    // ── Non-deprecated legacy passthroughs (now domain-typed; see LaunchRepository) ──────

    override suspend fun getLaunchesByDayAndMonth(
        day: Int,
        month: Int,
        limit: Int
    ): Result<PaginatedResult<Launch>> = apiCall("getLaunchesByDayAndMonth") {
        launchesApi.getLaunchList(
            limit = limit,
            previous = true,
            netDay = day,
            netMonth = month,
            ordering = "-net"
        ).body().toDomain()
    }

    /**
     * Get stale detailed launch data from local cache without checking TTL, for the
     * stale-while-revalidate UI pattern. Reads the already-domain-typed sibling accessor
     * (no Trantor call, no LL model import).
     */
    override suspend fun getStaleDetailedLaunch(id: String): Launch? =
        localDataSource?.getDetailedLaunchStale(id)

    override suspend fun getAgencyDetails(id: Int): Result<Agency> = apiCall("getAgencyDetails") {
        agenciesApi.getAgencyDetail(id).body().toDomainAgency()
    }

    override suspend fun getNextDetailedLaunch(limit: Int): Result<PaginatedResult<Launch>> =
        apiCall("getNextDetailedLaunch") {
            launchesApi.getLaunchList(limit = limit, upcoming = true, ordering = "net").body().toDomain()
        }

    override suspend fun getNextNormalLaunch(limit: Int): Result<PaginatedResult<Launch>> =
        apiCall("getNextNormalLaunch") {
            launchesApi.getLaunchList(limit = limit, upcoming = true, ordering = "net").body().toDomain()
        }

    override suspend fun getStatsCount(
        key: String,
        netGt: Instant,
        netLt: Instant,
        forceRefresh: Boolean
    ): Result<DataResult<Int>> {
        return try {
            val now = Clock.System.now().toEpochMilliseconds()

            val staleResult = statsLocalDataSource?.getStatCountStale(key)

            if (!forceRefresh) {
                val cached = statsLocalDataSource?.getStatCount(key)
                if (cached != null) {
                    return Result.success(DataResult(data = cached, source = DataSource.CACHE, timestamp = now))
                }
                if (staleResult != null) {
                    return Result.success(
                        DataResult(data = staleResult.first, source = DataSource.STALE_CACHE, timestamp = staleResult.second)
                    )
                }
            }

            val count = launchesApi.getLaunchList(
                limit = 1,
                upcoming = true,
                netAfter = netGt,
                netBefore = netLt
            ).body().count

            statsLocalDataSource?.cacheStat(key, count)
            log.i { "Stats API SUCCESS for '$key': count=$count" }

            Result.success(DataResult(data = count, source = DataSource.NETWORK, timestamp = now))
        } catch (e: Exception) {
            log.e(e) { "Error in getStatsCount for key='$key': ${e.message}" }
            val staleResult = statsLocalDataSource?.getStatCountStale(key)
            if (staleResult != null) {
                Result.success(DataResult(data = staleResult.first, source = DataSource.STALE_CACHE, timestamp = staleResult.second))
            } else {
                Result.failure(e)
            }
        }
    }
}
