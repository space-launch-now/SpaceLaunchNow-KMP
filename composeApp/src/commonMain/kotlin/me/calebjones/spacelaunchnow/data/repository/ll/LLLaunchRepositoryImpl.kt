package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.extensions.ll.getLaunchById
import me.calebjones.spacelaunchnow.api.extensions.ll.getLaunchList
import me.calebjones.spacelaunchnow.api.extensions.ll.getLaunchMiniList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.data.model.ApiError
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.StatsLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.ll.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.ll.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Launch repository, backed directly by the Launch Library 2 (LL) API - the pre-Trantor-migration
 * implementation, resurrected as a standalone class so the app can fall back to LL in production
 * via the `DataBackend` revert lever (see Koin wiring). Implements the CURRENT [LaunchRepository]
 * interface (domain-typed throughout), not the old LL-typed interface that shipped alongside the
 * original `LaunchRepositoryImpl` on `main`.
 *
 * Mirrors the structure and stale-while-revalidate caching behavior of the Trantor-backed
 * [me.calebjones.spacelaunchnow.data.repository.LaunchRepositoryImpl]: every network result is
 * mapped LL -> domain via the existing `domain/mapper` extension functions (declared for LL's
 * `api.launchlibrary.models` types in `LaunchMappers.kt`/`AgencyMappers.kt`, untouched by the
 * Trantor migration) before being cached through [LaunchLocalDataSource], which - since the
 * phase5-launch migration - only ever stores the domain [Launch] model (ADR-0004). This class
 * never constructs a [LaunchLocalDataSource] cache write from a raw LL wire model.
 */
class LLLaunchRepositoryImpl(
    private val launchesApi: LaunchesApi,
    private val agenciesApi: AgenciesApi,
    private val appPreferences: AppPreferences,
    private val localDataSource: LaunchLocalDataSource? = null,
    private val statsLocalDataSource: StatsLocalDataSource? = null
) : LaunchRepository {

    private val log = logger()
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun parseApiError(rawResponse: String): String {
        return try {
            val apiError = json.decodeFromString<ApiError>(rawResponse)
            apiError.getErrorMessage()
        } catch (e: Exception) {
            rawResponse
        }
    }

    /** Status ids to exclude when the user has enabled "hide TBD launches" (TBD=2, TBC=8). */
    private suspend fun hideTbdStatusIds(): List<Int>? {
        val hideTbd = withContext(Dispatchers.Default) { appPreferences.getHideTbdLaunches() }
        return if (hideTbd) listOf(1, 3, 4, 5, 6, 7, 9) else null
    }

    private fun paginatedOf(results: List<Launch>): PaginatedResult<Launch> =
        PaginatedResult(count = results.size, next = null, previous = null, results = results)

    /**
     * Filter cached launches by the user's agency/location preferences. The cache tables aren't
     * partitioned per filter combination, so a shared cache table can serve rows fetched under a
     * different agency/location filter than the one currently requested - this re-filters cache
     * reads only, matching the pre-migration `filterLaunchesByPreferences` behavior.
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
    ): Result<PaginatedResult<Launch>> {
        return try {
            log.d { "getUpcomingLaunchesDomain - limit: $limit, netGt: $netGt, netLt: $netLt" }
            val response = launchesApi.getLaunchMiniList(
                limit = limit,
                netGt = netGt,
                netLt = netLt,
                ordering = "net"
            )
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getUpcomingLaunchesDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getUpcomingLaunchesDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getUpcomingLaunchesDomain: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getPreviousLaunchesDomain(
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<Launch>> {
        return try {
            log.d { "getPreviousLaunchesDomain - limit: $limit" }
            val response = launchesApi.getLaunchMiniList(
                limit = limit,
                previous = true,
                ordering = "-net"
            )
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getPreviousLaunchesDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getPreviousLaunchesDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getPreviousLaunchesDomain: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getFeaturedLaunchDomain(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            log.d { "getFeaturedLaunchDomain - forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }
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
                netGt = oneHourAgo,
                ordering = "net",
                lspId = agencyIds,
                locationIds = locationIds,
                statusIds = hideTbdStatusIds()
            ).body().toDomain()

            if (launches.results.isNotEmpty()) {
                localDataSource?.cacheListLaunches(launches.results.take(4))
                log.i { "Successfully fetched and cached ${launches.results.size} featured launches" }
            } else {
                log.w { "API returned NO launches for featured!" }
            }

            return Result.success(DataResult(launches, DataSource.NETWORK, now))
        } catch (e: Exception) {
            if (e is ResponseException) {
                log.e(e) { "API error while fetching featured launches" }
            } else if (e is IOException) {
                log.e(e) { "Network error while fetching featured launch" }
            } else {
                log.e(e) { "Unexpected error while fetching featured launch" }
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
            log.d { "getInFlightLaunchesDomain - forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }

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
                statusIds = listOf(6), // In Flight status
                lspId = agencyIds,
                locationIds = locationIds,
                limit = 5,
                ordering = "net"
            ).body().toDomain()

            log.i { "API SUCCESS: Fetched ${launches.results.size} in-flight launches" }

            if (launches.results.isNotEmpty()) {
                localDataSource?.cacheListLaunches(launches.results)
                log.d { "Cached ${launches.results.size} in-flight launches" }
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
            log.d { "getUpcomingLaunchesNormalDomain - limit: $limit, forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }
            val staleTimestamp = localDataSource?.getCacheTimestamp("upcoming_launches")

            if (!forceRefresh) {
                val cached = localDataSource?.getUpcomingListLaunches(limit)
                if (!cached.isNullOrEmpty()) {
                    val filtered = filterLaunchesByPreferences(cached, agencyIds, locationIds)
                    if (filtered.isNotEmpty()) {
                        log.i { "CACHE HIT: Returning ${filtered.size}/${cached.size} filtered fresh cached launches" }
                        return Result.success(
                            DataResult(paginatedOf(filtered), DataSource.CACHE, staleTimestamp ?: now)
                        )
                    }
                } else {
                    val stale = localDataSource?.getUpcomingListLaunchesStale(limit)
                    if (!stale.isNullOrEmpty()) {
                        val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                        if (filteredStale.isNotEmpty()) {
                            log.d { "Returning stale data immediately, will revalidate in background" }
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
                lspId = agencyIds,
                locationIds = locationIds,
                statusIds = hideTbdStatusIds()
            ).body().toDomain()

            localDataSource?.cacheListLaunches(launches.results)
            log.i { "API SUCCESS: Fetched and cached ${launches.results.size} upcoming launches (filters: agencies=$agencyIds, locations=$locationIds)" }

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
                    log.w { "Returning ${filteredStale.size}/${stale.size} filtered stale cached launches as fallback" }
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
            log.d { "getPreviousLaunchesNormalDomain - limit: $limit, forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }
            val staleTimestamp = localDataSource?.getCacheTimestamp("previous_launches")

            if (!forceRefresh) {
                val cached = localDataSource?.getPreviousListLaunches(limit)
                if (!cached.isNullOrEmpty()) {
                    val filtered = filterLaunchesByPreferences(cached, agencyIds, locationIds)
                    if (filtered.isNotEmpty()) {
                        log.i { "CACHE HIT: Returning ${filtered.size}/${cached.size} filtered fresh cached previous launches" }
                        return Result.success(
                            DataResult(paginatedOf(filtered), DataSource.CACHE, staleTimestamp ?: now)
                        )
                    }
                } else {
                    val stale = localDataSource?.getPreviousListLaunchesStale(limit)
                    if (!stale.isNullOrEmpty()) {
                        val filteredStale = filterLaunchesByPreferences(stale, agencyIds, locationIds)
                        log.d { "STALE CACHE: Returning ${filteredStale.size} filtered stale previous launches" }
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
                lspId = agencyIds,
                locationIds = locationIds
            ).body().toDomain()

            localDataSource?.cacheListLaunches(launches.results)
            log.i { "API SUCCESS: Fetched and cached ${launches.results.size} previous launches (filters: agencies=$agencyIds, locations=$locationIds)" }

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
            log.d { "getLaunchDetailDomain - id: $id, forceRefresh: $forceRefresh" }

            if (!forceRefresh) {
                val cached = localDataSource?.getDetailedLaunch(id)
                if (cached != null) {
                    log.i { "CACHE HIT: Returning fresh cached detailed launch: ${cached.name}" }
                    return Result.success(cached)
                }
            }

            val response = launchesApi.launchesRetrieve(id)

            if (response.status >= 400) {
                val rawResponse = response.response.bodyAsText()
                log.e { "HTTP Error ${response.status}: $rawResponse" }
                return Result.failure(Exception("API Error ${response.status}: $rawResponse"))
            }

            val launch = response.body().toDomain()
            localDataSource?.cacheDetailedLaunch(launch)
            log.i { "API SUCCESS: Fetched and cached detailed launch: ${launch.name} (ID: ${launch.id})" }

            return Result.success(launch)
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getLaunchDetailDomain for ID $id" }
            try {
                val errorBody = e.response.bodyAsText()
                log.e { "Error response body: $errorBody" }
                val stale = localDataSource?.getDetailedLaunchStale(id)
                if (stale != null) {
                    log.w { "API ERROR: Returning stale cached detailed launch as fallback: ${stale.name}" }
                    return Result.success(stale)
                }
                return Result.failure(Exception("API Error: $errorBody"))
            } catch (bodyException: Exception) {
                return Result.failure(e)
            }
        } catch (e: IOException) {
            log.e(e) { "IOException in getLaunchDetailDomain for ID $id" }
            val stale = localDataSource?.getDetailedLaunchStale(id)
            if (stale != null) {
                log.w { "NETWORK ERROR: Returning stale cached detailed launch as fallback: ${stale.name}" }
                return Result.success(stale)
            }
            return Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getLaunchDetailDomain for ID $id: ${e::class.simpleName}" }
            return Result.failure(e)
        }
    }

    override suspend fun getStarshipLaunchesDomain(
        limit: Int,
        forceRefresh: Boolean,
        programId: List<Int>?
    ): Result<PaginatedResult<Launch>> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net",
                program = programId ?: listOf(1)
            )

            val rawResponse = response.response.bodyAsText()
            log.v { "API Response Debug - Status: ${response.status}, Length: ${rawResponse.length} chars" }

            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "HTTP Error ${response.status}: $errorMessage" }
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "API returned error response: $errorMessage" }
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getStarshipLaunchesDomain: ${e.message}" }
            try {
                val errorBody = e.response.bodyAsText()
                val errorMessage = parseApiError(errorBody)
                log.e { "Error response body: $errorMessage" }
                Result.failure(Exception("API Error: $errorMessage"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            log.e(e) { "IOException in getStarshipLaunchesDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getStarshipLaunchesDomain: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getStarshipHistoryDomain(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Launch>>> {
        val now = Clock.System.now().toEpochMilliseconds()
        try {
            log.d { "getStarshipHistoryDomain - limit: $limit, forceRefresh: $forceRefresh" }
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
                program = listOf(1) // Starship program
            ).body().toDomain()

            localDataSource?.cacheStarshipHistory(launches.results)
            log.i { "API SUCCESS: Fetched and cached ${launches.results.size} Starship history launches" }

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

    override suspend fun getLaunchByIdDomain(id: String): Result<Launch?> {
        return try {
            log.d { "getLaunchByIdDomain - id: $id" }
            val response = launchesApi.getLaunchById(launchId = id)
            val launch = response.body().results?.firstOrNull()?.toDomain()
            Result.success(launch)
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getLaunchByIdDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "IOException in getLaunchByIdDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getLaunchByIdDomain: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
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
    ): Result<PaginatedResult<Launch>> {
        return try {
            log.d { "getFilteredLaunchesDomain - limit: $limit, offset: $offset, upcoming: $upcoming, previous: $previous" }
            val response = launchesApi.getLaunchMiniList(
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
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getFilteredLaunchesDomain" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getFilteredLaunchesDomain" }
            Result.failure(e)
        }
    }

    // ── Non-deprecated legacy passthroughs (now domain-typed; see LaunchRepository) ──────

    override suspend fun getLaunchesByDayAndMonth(
        day: Int,
        month: Int,
        limit: Int
    ): Result<PaginatedResult<Launch>> {
        return try {
            log.d { "getLaunchesByDayAndMonth - day: $day, month: $month, limit: $limit" }
            val response = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                netDay = listOf(day.toDouble()),
                netMonth = listOf(month.toDouble()),
                ordering = "-net"
            )
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getLaunchesByDayAndMonth: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getLaunchesByDayAndMonth: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getLaunchesByDayAndMonth: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Get stale detailed launch data from local cache without checking TTL, for the
     * stale-while-revalidate UI pattern. Pure cache read, no network involved.
     */
    override suspend fun getStaleDetailedLaunch(id: String): Launch? =
        localDataSource?.getDetailedLaunchStale(id)

    override suspend fun getAgencyDetails(id: Int): Result<Agency> {
        return try {
            log.d { "getAgencyDetails - id: $id" }
            val response = agenciesApi.agenciesRetrieve(id)
            val agency = response.body().toDomainAgency()
            log.i { "API SUCCESS: Fetched agency details: ${agency.name} (ID: $id)" }
            Result.success(agency)
        } catch (e: ResponseException) {
            log.e(e) { "API error in getAgencyDetails for ID $id: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getAgencyDetails for ID $id: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getAgencyDetails for ID $id: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getNextDetailedLaunch(limit: Int): Result<PaginatedResult<Launch>> {
        return try {
            val response = launchesApi.launchesDetailedList(
                limit = limit,
                upcoming = true,
                ordering = "net"
            )

            val rawResponse = response.response.bodyAsText()
            log.v { "API Response Debug - Status: ${response.status}, Length: ${rawResponse.length} chars" }

            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "HTTP Error ${response.status}: $errorMessage" }
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "API returned error response: $errorMessage" }
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getNextDetailedLaunch: ${e.message}" }
            try {
                val errorBody = e.response.bodyAsText()
                val errorMessage = parseApiError(errorBody)
                log.e { "Error response body: $errorMessage" }
                Result.failure(Exception("API Error: $errorMessage"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            log.e(e) { "IOException in getNextDetailedLaunch: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getNextDetailedLaunch: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getNextNormalLaunch(limit: Int): Result<PaginatedResult<Launch>> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net"
            )
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getNextNormalLaunch: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "IOException in getNextNormalLaunch: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getNextNormalLaunch: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
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
                    log.d { "Stats cache HIT for '$key': $cached" }
                    return Result.success(DataResult(data = cached, source = DataSource.CACHE, timestamp = now))
                }
                if (staleResult != null) {
                    log.d { "Stats stale cache HIT for '$key': ${staleResult.first}" }
                    return Result.success(
                        DataResult(data = staleResult.first, source = DataSource.STALE_CACHE, timestamp = staleResult.second)
                    )
                }
            }

            log.d { "Fetching stats count from API for key='$key', netGt=$netGt, netLt=$netLt" }
            val response = launchesApi.getLaunchMiniList(
                limit = 1,
                upcoming = true,
                netGt = netGt,
                netLt = netLt
            )
            val count = response.body().count

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
