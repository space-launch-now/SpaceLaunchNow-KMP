package me.calebjones.spacelaunchnow.data.repository.trantor

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.trantor.getAgencyDetail
import me.calebjones.spacelaunchnow.api.extensions.trantor.getLaunchDetail
import me.calebjones.spacelaunchnow.api.extensions.trantor.getLaunchList
import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchesApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.StatsLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Launch repository, backed by the Trantor API (`/api/v1/launches`, `/api/v1/agencies/{id}`).
 *
 * Restores stale-while-revalidate caching (fresh cache hit -> stale cache fallback -> network
 * fetch -> cache write -> stale-on-error fallback) for the six read paths that had it before
 * the Trantor migration - [getFeaturedLaunchDomain], [getInFlightLaunchesDomain],
 * [getUpcomingLaunchesNormalDomain], [getPreviousLaunchesNormalDomain],
 * [getLaunchDetailDomain], [getStarshipHistoryDomain] - now caching the **domain** [Launch]
 * model through [LaunchLocalDataSource] instead of the LL model it used to serialize (see
 * ADR-0004 and the phase5-launch unit report). Every other read here fetches straight from
 * Trantor with no cache involved, matching the pre-Trantor-migration behavior for those paths
 * (they never had caching - e.g. the mini/mini-list "basic" tier was already dead code).
 *
 * Filtering by agency/location is applied server-side via `providerIds`/`locationIds` query
 * params on every network fetch. [filterLaunchesByPreferences] is retained purely for **cache
 * reads**: the cache tables aren't partitioned per filter combination (never were - see the
 * pre-migration `buildCacheKey`, which was only ever used for cache-timestamp display, not
 * storage keys), so a shared cache table can otherwise serve rows fetched under a different
 * agency/location filter than the one currently requested. Re-filtering network results would
 * be redundant (server already filtered them) and is not done.
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

    private fun paginatedOf(results: List<Launch>): PaginatedResult<Launch> =
        PaginatedResult(count = results.size, next = null, previous = null, results = results)

    /**
     * Filter cached launches by the user's agency/location preferences. See the class kdoc
     * for why this only ever runs against cache reads, never network results.
     */
    private fun filterLaunchesByPreferences(
        launches: List<Launch>,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): List<Launch> {
        var filtered = launches
        if (!agencyIds.isNullOrEmpty()) {
            filtered = filtered.filter { it.provider.id in agencyIds }
        }
        if (!locationIds.isNullOrEmpty()) {
            filtered = filtered.filter { launch -> launch.pad?.location?.id?.let { it in locationIds } ?: false }
        }
        return filtered
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
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            val staleTimestamp = localDataSource?.getCacheTimestamp("upcoming_launches")

            if (!forceRefresh) {
                val cached = localDataSource?.getUpcomingListLaunches(4)
                if (!cached.isNullOrEmpty()) {
                    val filtered = filterLaunchesByPreferences(cached, agencyIds, locationIds)
                    if (filtered.isNotEmpty()) {
                        log.i { "Cache hit - returning ${filtered.size}/${cached.size} filtered fresh cached featured launches" }
                        return Result.success(
                            DataResult(paginatedOf(filtered), DataSource.CACHE, staleTimestamp ?: now)
                        )
                    }
                } else {
                    val stale = localDataSource?.getUpcomingListLaunchesStale(4)
                    if (!stale.isNullOrEmpty()) {
                        val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                        if (filteredStale.isNotEmpty()) {
                            log.i { "Returning ${filteredStale.size} stale featured launches" }
                            return Result.success(
                                DataResult(paginatedOf(filteredStale), DataSource.STALE_CACHE, staleTimestamp ?: now)
                            )
                        }
                    }
                }
            }

            val oneHourAgo = Clock.System.now() - 1.hours
            val launches = launchesApi.getLaunchList(
                limit = 4,
                netAfter = oneHourAgo,
                ordering = "net",
                providerIds = agencyIds,
                locationIds = locationIds,
                statusIds = hideTbdStatusIds()
            ).body().toDomain()

            if (launches.results.isNotEmpty()) {
                localDataSource?.cacheListLaunches(launches.results.take(4))
            }

            return Result.success(DataResult(launches, DataSource.NETWORK, now))
        } catch (e: Exception) {
            if (e is ResponseException) {
                log.e(e) { "API error in getFeaturedLaunchDomain: ${e.message}" }
            } else if (e is IOException) {
                log.e(e) { "Network error in getFeaturedLaunchDomain: ${e.message}" }
            } else {
                log.e(e) { "Unexpected error in getFeaturedLaunchDomain: ${e::class.simpleName}: ${e.message}" }
            }
            val stale = localDataSource?.getUpcomingListLaunchesStale(4)
            if (!stale.isNullOrEmpty()) {
                val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                if (filteredStale.isNotEmpty()) {
                    log.w { "Returning ${filteredStale.size} filtered stale featured launches as fallback" }
                    return Result.success(
                        DataResult(
                            paginatedOf(filteredStale),
                            DataSource.STALE_CACHE,
                            localDataSource?.getCacheTimestamp("upcoming_launches")
                        )
                    )
                }
            }
            return Result.failure(e)
        }
    }

    override suspend fun getInFlightLaunchesDomain(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            if (!forceRefresh) {
                val cached = localDataSource?.getInFlightListLaunches(5)
                if (!cached.isNullOrEmpty()) {
                    log.d { "In-flight cache HIT: ${cached.size} launches" }
                    return Result.success(DataResult(paginatedOf(cached), DataSource.CACHE, now))
                }
                val stale = localDataSource?.getInFlightListLaunchesStale(5)
                if (!stale.isNullOrEmpty()) {
                    log.d { "In-flight stale cache HIT: ${stale.size} launches" }
                    return Result.success(DataResult(paginatedOf(stale), DataSource.STALE_CACHE, now))
                }
            }

            val launches = launchesApi.getLaunchList(
                statusIds = listOf(6), // In Flight
                providerIds = agencyIds,
                locationIds = locationIds,
                limit = 5,
                ordering = "net"
            ).body().toDomain()

            if (launches.results.isNotEmpty()) {
                localDataSource?.cacheListLaunches(launches.results)
            }

            return Result.success(DataResult(launches, DataSource.NETWORK, now))
        } catch (e: Exception) {
            if (e is ResponseException) {
                log.e(e) { "API error in getInFlightLaunchesDomain: ${e.message}" }
            } else if (e is IOException) {
                log.e(e) { "Network error in getInFlightLaunchesDomain: ${e.message}" }
            } else {
                log.e(e) { "Unexpected error in getInFlightLaunchesDomain: ${e::class.simpleName}: ${e.message}" }
            }
            val stale = localDataSource?.getInFlightListLaunchesStale(5)
            if (!stale.isNullOrEmpty()) {
                return Result.success(DataResult(paginatedOf(stale), DataSource.STALE_CACHE, now))
            }
            return Result.failure(e)
        }
    }

    override suspend fun getUpcomingLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            val staleTimestamp = localDataSource?.getCacheTimestamp("upcoming_launches")

            if (!forceRefresh) {
                val cached = localDataSource?.getUpcomingListLaunches(limit)
                if (!cached.isNullOrEmpty()) {
                    val filtered = filterLaunchesByPreferences(cached, agencyIds, locationIds)
                    if (filtered.isNotEmpty()) {
                        log.i { "Cache hit - returning ${filtered.size}/${cached.size} filtered fresh cached upcoming launches" }
                        return Result.success(
                            DataResult(paginatedOf(filtered), DataSource.CACHE, staleTimestamp ?: now)
                        )
                    }
                } else {
                    val stale = localDataSource?.getUpcomingListLaunchesStale(limit)
                    if (!stale.isNullOrEmpty()) {
                        val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                        if (filteredStale.isNotEmpty()) {
                            log.d { "Returning ${filteredStale.size} filtered stale upcoming launches, will revalidate in background" }
                            return Result.success(
                                DataResult(paginatedOf(filteredStale), DataSource.STALE_CACHE, staleTimestamp ?: now)
                            )
                        }
                    }
                }
            }

            val launches = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net",
                providerIds = agencyIds,
                locationIds = locationIds,
                statusIds = hideTbdStatusIds()
            ).body().toDomain()

            localDataSource?.cacheListLaunches(launches.results)
            log.i { "Fetched and cached ${launches.results.size} upcoming launches (agencies=$agencyIds, locations=$locationIds)" }

            return Result.success(DataResult(launches, DataSource.NETWORK, now))
        } catch (e: Exception) {
            if (e is ResponseException) {
                log.e(e) { "API error in getUpcomingLaunchesNormalDomain: ${e.message}" }
            } else if (e is IOException) {
                log.e(e) { "Network error in getUpcomingLaunchesNormalDomain: ${e.message}" }
            } else {
                log.e(e) { "Unexpected error in getUpcomingLaunchesNormalDomain: ${e::class.simpleName}: ${e.message}" }
            }
            val stale = localDataSource?.getUpcomingListLaunchesStale(limit)
            if (!stale.isNullOrEmpty()) {
                val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                if (filteredStale.isNotEmpty()) {
                    log.w { "Returning ${filteredStale.size}/${stale.size} filtered stale cached upcoming launches as fallback" }
                    return Result.success(
                        DataResult(
                            paginatedOf(filteredStale),
                            DataSource.STALE_CACHE,
                            localDataSource?.getCacheTimestamp("upcoming_launches")
                        )
                    )
                }
            }
            return Result.failure(e)
        }
    }

    override suspend fun getPreviousLaunchesNormalDomain(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            val staleTimestamp = localDataSource?.getCacheTimestamp("previous_launches")

            if (!forceRefresh) {
                val cached = localDataSource?.getPreviousListLaunches(limit)
                if (!cached.isNullOrEmpty()) {
                    val filtered = filterLaunchesByPreferences(cached, agencyIds, locationIds)
                    if (filtered.isNotEmpty()) {
                        log.i { "Cache hit - returning ${filtered.size}/${cached.size} filtered fresh cached previous launches" }
                        return Result.success(
                            DataResult(paginatedOf(filtered), DataSource.CACHE, staleTimestamp ?: now)
                        )
                    }
                } else {
                    val stale = localDataSource?.getPreviousListLaunchesStale(limit)
                    if (!stale.isNullOrEmpty()) {
                        val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                        log.d { "Returning ${filteredStale.size} filtered stale previous launches" }
                        return Result.success(
                            DataResult(paginatedOf(filteredStale), DataSource.STALE_CACHE, staleTimestamp ?: now)
                        )
                    }
                }
            }

            val launches = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                ordering = "-net",
                providerIds = agencyIds,
                locationIds = locationIds
            ).body().toDomain()

            localDataSource?.cacheListLaunches(launches.results)
            log.i { "Fetched and cached ${launches.results.size} previous launches (agencies=$agencyIds, locations=$locationIds)" }

            return Result.success(DataResult(launches, DataSource.NETWORK, now))
        } catch (e: Exception) {
            if (e is ResponseException) {
                log.e(e) { "API error in getPreviousLaunchesNormalDomain: ${e.message}" }
            } else if (e is IOException) {
                log.e(e) { "Network error in getPreviousLaunchesNormalDomain: ${e.message}" }
            } else {
                log.e(e) { "Unexpected error in getPreviousLaunchesNormalDomain: ${e::class.simpleName}: ${e.message}" }
            }
            val stale = localDataSource?.getPreviousListLaunchesStale(limit)
            if (!stale.isNullOrEmpty()) {
                val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                log.w { "Returning ${filteredStale.size} filtered stale cached previous launches as fallback" }
                return Result.success(
                    DataResult(
                        paginatedOf(filteredStale),
                        DataSource.STALE_CACHE,
                        localDataSource?.getCacheTimestamp("previous_launches")
                    )
                )
            }
            return Result.failure(e)
        }
    }

    override suspend fun getLaunchDetailDomain(
        id: String,
        forceRefresh: Boolean
    ): Result<Launch> {
        try {
            if (!forceRefresh) {
                val cached = localDataSource?.getDetailedLaunch(id)
                if (cached != null) {
                    log.i { "Cache hit - returning fresh cached detailed launch: ${cached.name}" }
                    return Result.success(cached)
                }
            }

            val launch = launchesApi.getLaunchDetail(id).body().toDomain()
            localDataSource?.cacheDetailedLaunch(launch)
            log.i { "Fetched and cached detailed launch: ${launch.name} (ID: ${launch.id})" }
            return Result.success(launch)
        } catch (e: Exception) {
            if (e is ResponseException) {
                log.e(e) { "API error in getLaunchDetailDomain for ID $id: ${e.message}" }
            } else if (e is IOException) {
                log.e(e) { "Network error in getLaunchDetailDomain for ID $id: ${e.message}" }
            } else {
                log.e(e) { "Unexpected error in getLaunchDetailDomain for ID $id: ${e::class.simpleName}: ${e.message}" }
            }
            val stale = localDataSource?.getDetailedLaunchStale(id)
            if (stale != null) {
                log.w { "Returning stale cached detailed launch as fallback: ${stale.name}" }
                return Result.success(stale)
            }
            return Result.failure(e)
        }
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
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            val staleTimestamp = localDataSource?.getCacheTimestamp("starship_history")

            if (!forceRefresh) {
                val cached = localDataSource?.getStarshipHistory(limit)
                if (!cached.isNullOrEmpty()) {
                    log.i { "Cache hit - returning ${cached.size} fresh cached Starship history launches" }
                    return Result.success(DataResult(paginatedOf(cached), DataSource.CACHE, staleTimestamp ?: now))
                }
                val stale = localDataSource?.getStarshipHistoryStale(limit)
                if (!stale.isNullOrEmpty()) {
                    log.i { "Returning ${stale.size} stale Starship history launches" }
                    return Result.success(DataResult(paginatedOf(stale), DataSource.STALE_CACHE, staleTimestamp ?: now))
                }
            }

            val launches = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                ordering = "-net",
                programIds = listOf(1) // Starship program
            ).body().toDomain()

            localDataSource?.cacheStarshipHistory(launches.results)
            log.i { "Fetched and cached ${launches.results.size} Starship history launches" }

            return Result.success(DataResult(launches, DataSource.NETWORK, now))
        } catch (e: Exception) {
            if (e is ResponseException) {
                log.e(e) { "API error in getStarshipHistoryDomain: ${e.message}" }
            } else if (e is IOException) {
                log.e(e) { "Network error in getStarshipHistoryDomain: ${e.message}" }
            } else {
                log.e(e) { "Unexpected error in getStarshipHistoryDomain: ${e::class.simpleName}: ${e.message}" }
            }
            val stale = localDataSource?.getStarshipHistoryStale(limit)
            if (!stale.isNullOrEmpty()) {
                log.w { "Returning ${stale.size} stale Starship history launches as fallback" }
                return Result.success(
                    DataResult(paginatedOf(stale), DataSource.STALE_CACHE, localDataSource?.getCacheTimestamp("starship_history"))
                )
            }
            return Result.failure(e)
        }
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
     * stale-while-revalidate UI pattern. Pure cache read, no network involved.
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
