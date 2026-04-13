package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.extensions.getLaunchById
import me.calebjones.spacelaunchnow.api.extensions.getLaunchList
import me.calebjones.spacelaunchnow.api.extensions.getLaunchMiniList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.ApiError
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class LaunchRepositoryImpl(
    private val launchesApi: LaunchesApi,
    private val agenciesApi: AgenciesApi,
    private val appPreferences: AppPreferences,
    private val localDataSource: LaunchLocalDataSource? = null,
    private val statsLocalDataSource: me.calebjones.spacelaunchnow.database.StatsLocalDataSource? = null
) : LaunchRepository {

    private val log = logger()
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun parseApiError(rawResponse: String): String {
        return try {
            val apiError = json.decodeFromString<ApiError>(rawResponse)
            apiError.getErrorMessage()
        } catch (e: Exception) {
            // If we can't parse as ApiError, return the raw response
            rawResponse
        }
    }

    override suspend fun getUpcomingLaunchesList(limit: Int): Result<PaginatedLaunchBasicList> {
        return try {
            log.d { "getUpcomingLaunchesList - limit: $limit" }
            val response = launchesApi.launchesMiniList(
                limit = limit,
                upcoming = true,
                ordering = "net" // Order by launch time
            )
            val launches = response.body()
            log.i { "✅ API SUCCESS: Fetched ${launches.results.size} upcoming launches (mini list)" }
            Result.success(launches)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getUpcomingLaunchesList: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getUpcomingLaunchesList: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getUpcomingLaunchesList: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getFeaturedLaunch(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedLaunchNormalList>> {
        return try {
            log.d { "getFeaturedLaunch called - forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }

            val now = Clock.System.now().toEpochMilliseconds()

            // Create cache key for featured launch
            val cacheKey = buildCacheKey("featured_launch", agencyIds, locationIds)

            // STALE-WHILE-REVALIDATE: Check for stale data (up to 4 featured launches)
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(4)
            val staleTimestamp = localDataSource?.getCacheTimestamp(cacheKey)
            val hasStaleData = staleCached != null && staleCached.isNotEmpty()

            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedLaunches = localDataSource?.getUpcomingNormalLaunches(4)
                if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
                    // Apply user filter preferences to fresh cache (prevents returning unfiltered data on cold start)
                    val filteredCache = filterLaunchesByPreferences(cachedLaunches, agencyIds, locationIds)
                    if (filteredCache.isNotEmpty()) {
                        log.i { "Cache hit - Returning ${filteredCache.size}/${cachedLaunches.size} filtered fresh cached featured launches" }
                        return Result.success(
                            DataResult(
                                data = PaginatedLaunchNormalList(
                                    count = filteredCache.size,
                                    next = null,
                                    previous = null,
                                    results = filteredCache
                                ),
                                source = DataSource.CACHE,
                                timestamp = staleTimestamp ?: now
                            )
                        )
                    }
                    log.d { "Fresh cache filtered to zero results (had ${cachedLaunches.size}), falling through to stale/API" }
                } else if (hasStaleData) {
                    // Filter stale cache by user preferences
                    val filteredStale =
                        filterLaunchesByPreferences(staleCached!!, agencyIds, locationIds)
                    log.v { "Stale cache - Found ${filteredStale.size}/${staleCached.size} filtered stale launches" }

                    if (filteredStale.isEmpty()) {
                        log.d { "Stale cache filtered to zero, fetching from API" }
                        // Continue to API call
                    } else {
                        log.i { "Returning ${filteredStale.size} stale featured launches" }
                        return Result.success(
                            DataResult(
                                data = PaginatedLaunchNormalList(
                                    count = filteredStale.size,
                                    next = null,
                                    previous = null,
                                    results = filteredStale
                                ),
                                source = DataSource.STALE_CACHE,
                                timestamp = staleTimestamp ?: now
                            )
                        )
                    }
                }
            }

            // Fetch from API using custom time window (1 hour before now)
            log.d { "Fetching featured launch from API with netGt=(now - 1 hour)" }
            val hideTbd = withContext(Dispatchers.Default) { appPreferences.getHideTbdLaunches() }
            log.v { "HideTBD preference: $hideTbd" }

            // Build status filter: exclude TBD (id=2) and TBC (id=8) if hideTbd is enabled
            // Include all other statuses: Go(1), Success(3), Failure(4), Hold(5), In Flight(6), Partial Failure(7), Deployed(9)
            val statusIds = if (hideTbd) {
                listOf(1, 3, 4, 5, 6, 7, 9) // Exclude TBD(2) and TBC(8)
            } else {
                null // No filtering, include all statuses
            }

            // Calculate time window: 1 hour before now
            val oneHourAgo = Clock.System.now() - 1.hours
            log.d { "Making API call - limit: 4, netGt: $oneHourAgo, agencyIds: $agencyIds, locationIds: $locationIds, statusIds: $statusIds" }

            val response = launchesApi.getLaunchList(
                limit = 4,
                netGt = oneHourAgo,
                ordering = "net",
                lspId = agencyIds,
                locationIds = locationIds,
                statusIds = statusIds
            )
            log.d { "API response received - status: ${response.status}, url: ${response.response.request.url}" }

            val launches = response.body()
            log.i { "API returned ${launches.results.size} launches (filtered by status at API level)" }
            log.v { "Full API response - count: ${launches.count}, next: ${launches.next}, previous: ${launches.previous}" }

            // Log each launch returned
            launches.results.forEachIndexed { index, launch ->
                log.v { "Launch[$index]: name='${launch.name}', id=${launch.id}, net=${launch.net}, status=${launch.status?.name}" }
            }

            if (launches.results.isEmpty()) {
                log.w { "API returned NO launches!" }
            } else {
                // Cache all 4 featured launches
                localDataSource?.cacheNormalLaunches(launches.results.take(4))
                log.i { "Successfully fetched and cached ${launches.results.size} featured launches" }
            }

            Result.success(
                DataResult(
                    data = launches,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching featured launches" }
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(4)
            if (staleCached != null && staleCached.isNotEmpty()) {
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                if (filteredStale.isNotEmpty()) {
                    log.w { "Returning ${filteredStale.size} filtered stale featured launch as fallback" }
                    return Result.success(
                        DataResult(
                            data = PaginatedLaunchNormalList(
                                count = filteredStale.size,
                                next = null,
                                previous = null,
                                results = filteredStale
                            ),
                            source = DataSource.STALE_CACHE,
                            timestamp = localDataSource?.getCacheTimestamp("featured_launch")
                        )
                    )
                }
            }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error while fetching featured launch" }
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(1)
            if (staleCached != null && staleCached.isNotEmpty()) {
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                if (filteredStale.isNotEmpty()) {
                    log.w { "Returning ${filteredStale.size} filtered stale featured launch as fallback (network error)" }
                    return Result.success(
                        DataResult(
                            data = PaginatedLaunchNormalList(
                                count = filteredStale.size,
                                next = null,
                                previous = null,
                                results = filteredStale
                            ),
                            source = DataSource.STALE_CACHE,
                            timestamp = localDataSource?.getCacheTimestamp("featured_launch")
                        )
                    )
                }
            }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while fetching featured launch" }
            Result.failure(e)
        }
    }

    override suspend fun getInFlightLaunches(
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedLaunchNormalList>> {
        return try {
            log.d { "getInFlightLaunches - forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }

            val now = Clock.System.now().toEpochMilliseconds()

            // STALE-WHILE-REVALIDATE: check stale first for fallback
            val staleCached = localDataSource?.getInFlightNormalLaunchesStale(5)
            val hasStaleData = staleCached != null && staleCached.isNotEmpty()

            // Try fresh cache if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getInFlightNormalLaunches(5)
                if (cached != null && cached.isNotEmpty()) {
                    log.d { "In-flight cache HIT: ${cached.size} launches" }
                    return Result.success(DataResult(
                        data = PaginatedLaunchNormalList(count = cached.size, results = cached, next = null, previous = null),
                        source = DataSource.CACHE,
                        timestamp = now
                    ))
                }
                if (hasStaleData) {
                    log.d { "In-flight stale cache HIT: ${staleCached!!.size} launches" }
                    return Result.success(DataResult(
                        data = PaginatedLaunchNormalList(count = staleCached!!.size, results = staleCached, next = null, previous = null),
                        source = DataSource.STALE_CACHE,
                        timestamp = now
                    ))
                }
            }

            // Fetch from API - status_ids=6 means "In Flight"
            log.d { "Fetching in-flight launches from API with statusIds=[6]" }
            val response = launchesApi.getLaunchList(
                statusIds = listOf(6),  // In Flight status
                lspId = agencyIds,
                locationIds = locationIds,
                limit = 5,
                ordering = "net"
            )

            val launches = response.body()
            log.i { "\u2705 API SUCCESS: Fetched ${launches.results.size} in-flight launches" }

            // Log each in-flight launch
            launches.results.forEachIndexed { index, launch ->
                log.v { "InFlight[$index]: name='${launch.name}', id=${launch.id}, status=${launch.status?.name}" }
            }

            // Cache the results
            if (launches.results.isNotEmpty()) {
                localDataSource?.cacheNormalLaunches(launches.results)
                log.d { "Cached ${launches.results.size} in-flight launches" }
            }

            Result.success(
                DataResult(
                    data = launches,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "\u274C API ERROR in getInFlightLaunches: ${e.message}" }
            val staleCached = localDataSource?.getInFlightNormalLaunchesStale(5)
            if (staleCached != null && staleCached.isNotEmpty()) {
                val now = Clock.System.now().toEpochMilliseconds()
                Result.success(DataResult(
                    data = PaginatedLaunchNormalList(count = staleCached.size, results = staleCached, next = null, previous = null),
                    source = DataSource.STALE_CACHE,
                    timestamp = now
                ))
            } else {
                Result.failure(e)
            }
        } catch (e: IOException) {
            log.e(e) { "\u274C NETWORK ERROR in getInFlightLaunches: ${e.message}" }
            val staleCached = localDataSource?.getInFlightNormalLaunchesStale(5)
            if (staleCached != null && staleCached.isNotEmpty()) {
                val now = Clock.System.now().toEpochMilliseconds()
                Result.success(DataResult(
                    data = PaginatedLaunchNormalList(count = staleCached.size, results = staleCached, next = null, previous = null),
                    source = DataSource.STALE_CACHE,
                    timestamp = now
                ))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            log.e(e) { "\u274C UNEXPECTED ERROR in getInFlightLaunches: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingLaunchesNormal(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedLaunchNormalList>> {
        return try {
            log.d { "getUpcomingLaunchesNormal - limit: $limit, forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }

            val now = Clock.System.now().toEpochMilliseconds()

            // Create cache key that includes filter parameters
            val cacheKey = buildCacheKey("upcoming_launches", agencyIds, locationIds)

            // STALE-WHILE-REVALIDATE: Always check for stale data first
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp(cacheKey)
            val hasStaleData = staleCached != null && staleCached.isNotEmpty()

            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedLaunches = localDataSource?.getUpcomingNormalLaunches(limit)
                if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
                    // Apply user filter preferences to fresh cache (prevents returning unfiltered data on cold start)
                    val filteredCache = filterLaunchesByPreferences(cachedLaunches, agencyIds, locationIds)
                    if (filteredCache.isNotEmpty()) {
                        log.i { "✅ CACHE HIT: Returning ${filteredCache.size}/${cachedLaunches.size} filtered fresh cached launches" }
                        return Result.success(
                            DataResult(
                                data = PaginatedLaunchNormalList(
                                    count = filteredCache.size,
                                    next = null,
                                    previous = null,
                                    results = filteredCache
                                ),
                                source = DataSource.CACHE,
                                timestamp = staleTimestamp ?: now
                            )
                        )
                    }
                    log.d { "Fresh cache filtered to zero results (had ${cachedLaunches.size}), falling through to stale/API" }
                } else if (hasStaleData) {
                    // Filter stale cache by user preferences (agency and location)
                    val filteredStale =
                        filterLaunchesByPreferences(staleCached!!, agencyIds, locationIds)
                    log.d { "⏳ STALE CACHE: Found ${filteredStale.size}/${staleCached.size} filtered stale launches (filters: agencies=$agencyIds, locations=$locationIds)" }
                    if (filteredStale.isEmpty()) {
                        log.w {
                            "⚠️ WARNING: Stale cache was filtered to zero results! All ${staleCached.size} cached launches were filtered out. Filter criteria - agencyIds: $agencyIds, locationIds: $locationIds. Sample cached launches: ${
                                staleCached.take(
                                    3
                                ).map { "${it.name} (LSP: ${it.launchServiceProvider?.id})" }
                            }. Will fetch fresh data from API."
                        }
                        // DON'T return here - continue to API call to get filtered results
                    } else {
                        // Only return early if we have stale data that passed filters
                        log.d { "Returning stale data immediately, will revalidate in background" }
                        return Result.success(
                            DataResult(
                                data = PaginatedLaunchNormalList(
                                    count = filteredStale.size,
                                    next = null,
                                    previous = null,
                                    results = filteredStale
                                ),
                                source = DataSource.STALE_CACHE,
                                timestamp = staleTimestamp ?: now
                            )
                        )
                    }
                }
            }

            // Fetch from API (either no cache, expired, or force refresh)
            log.d { "📡 Fetching from API (no fresh cache available)..." }
            val hideTbd = withContext(Dispatchers.Default) { appPreferences.getHideTbdLaunches() }
            log.v { "HideTBD preference: $hideTbd" }

            // Build status filter: exclude TBD (id=2) and TBC (id=8) if hideTbd is enabled
            // Include all other statuses: Go(1), Success(3), Failure(4), Hold(5), In Flight(6), Partial Failure(7), Deployed(9)
            val statusIds = if (hideTbd) {
                listOf(1, 3, 4, 5, 6, 7, 9) // Exclude TBD(2) and TBC(8)
            } else {
                null // No filtering, include all statuses
            }

            // NOTE: Current implementation uses AND logic (strict matching) when both filters are present.
            // Flexible mode (OR logic) would require two API calls and merge, which is deferred for MVP.
            // The API parameters use: (lsp__id IN agencies) AND (location__ids IN locations)
            // TODO: Implement flexible mode - see LAUNCH_FILTERS_HOME_INTEGRATION.md Phase 7
            log.d { "Making API call with: limit=$limit, agencyIds=$agencyIds, locationIds=$locationIds, statusIds=$statusIds" }
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net",
                lspId = agencyIds,
                locationIds = locationIds,
                statusIds = statusIds
            )
            log.d { "✅ API response received with status: ${response.status}" }

            val launches = response.body()
            log.i { "API returned ${launches.results.size} launches (filtered by status at API level)" }

            if (launches.results.isEmpty()) {
                log.w { "⚠️ WARNING: API returned NO launches!" }
            }

            // Cache the results for future use
            localDataSource?.cacheNormalLaunches(launches.results)
            log.i { "✅ API SUCCESS: Fetched and cached ${launches.results.size} upcoming launches (filters: agencies=$agencyIds, locations=$locationIds, statusIds=$statusIds)" }

            Result.success(
                DataResult(
                    data = launches,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("upcoming_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Filter stale cache by user preferences
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                log.w { "⚠️ API ERROR: Returning ${filteredStale.size}/${staleCached.size} filtered stale cached launches as fallback" }
                return Result.success(
                    DataResult(
                        data = PaginatedLaunchNormalList(
                            count = filteredStale.size,
                            next = null,
                            previous = null,
                            results = filteredStale
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp
                    )
                )
            }
            Result.failure(e)
        } catch (e: IOException) {
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("upcoming_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Filter stale cache by user preferences
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                log.w { "⚠️ NETWORK ERROR: Returning ${filteredStale.size}/${staleCached.size} filtered stale cached launches as fallback" }
                return Result.success(
                    DataResult(
                        data = PaginatedLaunchNormalList(
                            count = filteredStale.size,
                            next = null,
                            previous = null,
                            results = filteredStale
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp
                    )
                )
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreviousLaunchesList(limit: Int): Result<PaginatedLaunchBasicList> {
        return try {
            log.d { "getPreviousLaunchesList - limit: $limit" }
            val response = launchesApi.getLaunchMiniList(
                limit = limit,
                previous = true,
                ordering = "-net"
            )
            val launches = response.body()
            log.i { "✅ API SUCCESS: Fetched ${launches.results.size} previous launches (mini list)" }
            Result.success(launches)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getPreviousLaunchesList: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getPreviousLaunchesList: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getPreviousLaunchesList: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingLaunchesList(
        limit: Int,
        netGt: Instant?,
        netLt: Instant?
    ): Result<PaginatedLaunchBasicList> {
        return try {
            log.d { "getUpcomingLaunchesList - limit: $limit, netGt: $netGt, netLt: $netLt" }
            val response = launchesApi.getLaunchMiniList(
                limit = limit,
                netGt = netGt,
                netLt = netLt,
                ordering = "net"
            )
            val launches = response.body()
            log.i { "✅ API SUCCESS: Fetched ${launches.results.size} launches with time window filter" }
            Result.success(launches)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getUpcomingLaunchesList (time window): ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getUpcomingLaunchesList (time window): ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getUpcomingLaunchesList (time window): ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getPreviousLaunchesNormal(
        limit: Int,
        forceRefresh: Boolean,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): Result<DataResult<PaginatedLaunchNormalList>> {
        return try {
            log.d { "getPreviousLaunchesNormal - limit: $limit, forceRefresh: $forceRefresh, agencyIds: $agencyIds, locationIds: $locationIds" }

            val now = Clock.System.now().toEpochMilliseconds()

            // Create cache key that includes filter parameters
            val cacheKey = buildCacheKey("previous_launches", agencyIds, locationIds)

            // STALE-WHILE-REVALIDATE: Check for stale data
            val staleCached = localDataSource?.getPreviousNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp(cacheKey)
            val hasStaleData = staleCached != null && staleCached.isNotEmpty()

            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedLaunches = localDataSource?.getPreviousNormalLaunches(limit)
                if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
                    // Apply user filter preferences to fresh cache (prevents returning unfiltered data on cold start)
                    val filteredCache = filterLaunchesByPreferences(cachedLaunches, agencyIds, locationIds)
                    if (filteredCache.isNotEmpty()) {
                        log.i { "✅ CACHE HIT: Returning ${filteredCache.size}/${cachedLaunches.size} filtered fresh cached previous launches" }
                        return Result.success(
                            DataResult(
                                data = PaginatedLaunchNormalList(
                                    count = filteredCache.size,
                                    next = null,
                                    previous = null,
                                    results = filteredCache
                                ),
                                source = DataSource.CACHE,
                                timestamp = staleTimestamp ?: now
                            )
                        )
                    }
                    log.d { "Fresh cache filtered to zero results (had ${cachedLaunches.size}), falling through to stale/API" }
                }

                if (hasStaleData) {
                    // Apply user preference filters to stale cached data
                    val filteredStale =
                        filterLaunchesByPreferences(staleCached!!, agencyIds, locationIds)
                    log.d { "⏳ STALE CACHE: Returning ${filteredStale.size} filtered stale previous launches" }
                    return Result.success(
                        DataResult(
                            data = PaginatedLaunchNormalList(
                                count = filteredStale.size,
                                next = null,
                                previous = null,
                                results = filteredStale
                            ),
                            source = DataSource.STALE_CACHE,
                            timestamp = staleTimestamp ?: now
                        )
                    )
                }
            }

            // Fetch from API with filter parameters
            // NOTE: Current implementation uses AND logic (strict matching) when both filters are present.
            // Flexible mode (OR logic) would require two API calls and merge, which is deferred for MVP.
            // The API parameters use: (lsp__id IN agencies) AND (location__ids IN locations)
            // TODO: Implement flexible mode - see LAUNCH_FILTERS_HOME_INTEGRATION.md Phase 7
            val response = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                ordering = "-net",
                lspId = agencyIds,
                locationIds = locationIds
            )
            val launches = response.body()

            // Cache the results for future use
            localDataSource?.cacheNormalLaunches(launches.results)
            log.i { "✅ API SUCCESS: Fetched and cached ${launches.results.size} previous launches (filters: agencies=$agencyIds, locations=$locationIds)" }

            Result.success(
                DataResult(
                    data = launches,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getPreviousNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("previous_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Apply user preference filters to stale cached data
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                log.w { "⚠️ API ERROR: Returning ${filteredStale.size} filtered stale cached previous launches as fallback" }
                return Result.success(
                    DataResult(
                        data = PaginatedLaunchNormalList(
                            count = filteredStale.size,
                            next = null,
                            previous = null,
                            results = filteredStale
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp
                    )
                )
            }
            Result.failure(e)
        } catch (e: IOException) {
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getPreviousNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("previous_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Filter stale cache by user preferences
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                log.w { "⚠️ NETWORK ERROR: Returning ${filteredStale.size}/${staleCached.size} filtered stale cached previous launches as fallback" }
                return Result.success(
                    DataResult(
                        data = PaginatedLaunchNormalList(
                            count = filteredStale.size,
                            next = null,
                            previous = null,
                            results = filteredStale
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp
                    )
                )
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLaunchesByDayAndMonth(
        day: Int,
        month: Int,
        limit: Int
    ): Result<PaginatedLaunchNormalList> {
        return try {
            log.d { "getLaunchesByDayAndMonth - day: $day, month: $month, limit: $limit" }
            val response = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                netDay = listOf(day.toDouble()),
                netMonth = listOf(month.toDouble()),
                ordering = "-net" // Most recent first
            )
            val launches = response.body()
            log.i { "✅ API SUCCESS: Fetched ${launches.results.size} launches for day $day, month $month" }
            Result.success(launches)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getLaunchesByDayAndMonth: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getLaunchesByDayAndMonth: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getLaunchesByDayAndMonth: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getLaunchDetails(
        id: String,
        forceRefresh: Boolean
    ): Result<LaunchDetailed> {
        return try {
            log.d { "getLaunchDetails - id: $id, forceRefresh: $forceRefresh" }

            // STALE-WHILE-REVALIDATE: Check for stale data
            val staleCached = localDataSource?.getDetailedLaunchStale(id)

            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedLaunch = localDataSource?.getDetailedLaunch(id)
                if (cachedLaunch != null) {
                    log.i { "✅ CACHE HIT: Returning fresh cached detailed launch: ${cachedLaunch.name}" }
                    return Result.success(cachedLaunch)
                } else if (staleCached != null) {
                    log.d { "⏳ STALE CACHE: Returning stale detailed launch: ${staleCached.name}" }
                }
            }

            // Fetch from API
            val response = launchesApi.launchesRetrieve(id)

            // Check if it's an error response
            if (response.status >= 400) {
                val rawResponse = response.response.bodyAsText()
                log.e { "HTTP Error ${response.status}: $rawResponse" }
                return Result.failure(Exception("API Error ${response.status}: $rawResponse"))
            }

            val body = response.body()

            // Cache the detailed launch for future use
            localDataSource?.cacheDetailedLaunch(body)
            log.i { "✅ API SUCCESS: Fetched and cached detailed launch: ${body.name} (ID: ${body.id})" }

            Result.success(body)
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getLaunchDetails for ID $id" }
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                log.e { "Error response body: $errorBody" }

                // On error, try to return stale cache if available
                val staleCached = localDataSource?.getDetailedLaunchStale(id)
                if (staleCached != null) {
                    log.w { "⚠️ API ERROR: Returning stale cached detailed launch as fallback: ${staleCached.name}" }
                    return Result.success(staleCached)
                }

                Result.failure(Exception("API Error: $errorBody"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            log.e(e) { "IOException in getLaunchDetails for ID $id" }
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getDetailedLaunchStale(id)
            if (staleCached != null) {
                log.w { "⚠️ NETWORK ERROR: Returning stale cached detailed launch as fallback: ${staleCached.name}" }
                return Result.success(staleCached)
            }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getLaunchDetails for ID $id: ${e::class.simpleName}" }
            Result.failure(e)
        }
    }

    /**
     * Get stale detailed launch data from local cache without checking TTL.
     * Used for stale-while-revalidate pattern to show data immediately while refreshing.
     */
    override suspend fun getStaleDetailedLaunch(id: String): LaunchDetailed? {
        return localDataSource?.getDetailedLaunchStale(id)
    }

    override suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed> {
        return try {
            log.d { "getAgencyDetails - id: $id" }
            val response = agenciesApi.agenciesRetrieve(id)
            val agency = response.body()
            log.i { "✅ API SUCCESS: Fetched agency details: ${agency.name} (ID: $id)" }
            Result.success(agency)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getAgencyDetails for ID $id: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getAgencyDetails for ID $id: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getAgencyDetails for ID $id: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getNextStarshipLaunch(
        limit: Int,
        forceRefresh: Boolean,
        programId: List<Int>?
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net",
                program = programId
            )

            // Verbose debug logging for API response
            val rawResponse = response.response.bodyAsText()
            log.v { "API Response Debug - URL: ${response.response.request.url}, Status: ${response.status}, Length: ${rawResponse.length} chars" }
            log.v { "Full Response Body: $rawResponse" }

            // Check if it's an error response
            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "HTTP Error ${response.status}: $errorMessage" }
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            // Check if response looks like an error (contains "detail" field)
            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "API returned error response: $errorMessage" }
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            // Try to deserialize the response body
            val body = response.body()
            Result.success(body)
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getNextLaunch: ${e.message}" }
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                val errorMessage = parseApiError(errorBody)
                log.e { "Error response body: $errorMessage" }
                Result.failure(Exception("API Error: $errorMessage"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            log.e(e) { "IOException in getNextLaunch: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getNextLaunch: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getStarshipHistoryLaunches(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedLaunchNormalList>> {
        return try {
            log.d { "getStarshipHistoryLaunches - limit: $limit, forceRefresh: $forceRefresh" }

            val now = Clock.System.now().toEpochMilliseconds()
            val cacheKey = "starship_history"

            // STALE-WHILE-REVALIDATE: Check for stale data
            val staleCached = localDataSource?.getStarshipHistoryStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp(cacheKey)
            val hasStaleData = staleCached != null && staleCached.isNotEmpty()

            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedLaunches = localDataSource?.getStarshipHistory(limit)
                if (cachedLaunches != null && cachedLaunches.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cachedLaunches.size} fresh cached Starship history launches" }
                    return Result.success(
                        DataResult(
                            data = PaginatedLaunchNormalList(
                                count = cachedLaunches.size,
                                next = null,
                                previous = null,
                                results = cachedLaunches
                            ),
                            source = DataSource.CACHE,
                            timestamp = staleTimestamp ?: now
                        )
                    )
                } else if (hasStaleData) {
                    log.i { "Returning ${staleCached!!.size} stale Starship history launches" }
                    return Result.success(
                        DataResult(
                            data = PaginatedLaunchNormalList(
                                count = staleCached.size,
                                next = null,
                                previous = null,
                                results = staleCached
                            ),
                            source = DataSource.STALE_CACHE,
                            timestamp = staleTimestamp ?: now
                        )
                    )
                }
            }

            // Fetch from API - all previous Starship launches (program=1)
            log.d { "Fetching Starship history from API with program=1, previous=true" }
            val response = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                ordering = "-net", // Newest first
                program = listOf(1) // Starship program
            )

            val launches = response.body()
            log.i { "✅ API SUCCESS: Fetched ${launches.results.size} Starship history launches" }

            // Cache the results with 1-month TTL
            localDataSource?.cacheStarshipHistory(launches.results)

            Result.success(
                DataResult(
                    data = launches,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getStarshipHistoryLaunches: ${e.message}" }
            // On API error, try to return stale cache if available
            val staleCached = localDataSource?.getStarshipHistoryStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("starship_history")
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "⚠️ API ERROR: Returning ${staleCached.size} stale Starship history launches as fallback" }
                return Result.success(
                    DataResult(
                        data = PaginatedLaunchNormalList(
                            count = staleCached.size,
                            next = null,
                            previous = null,
                            results = staleCached
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp
                    )
                )
            }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getStarshipHistoryLaunches: ${e.message}" }
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getStarshipHistoryStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("starship_history")
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "⚠️ NETWORK ERROR: Returning ${staleCached.size} stale Starship history launches as fallback" }
                return Result.success(
                    DataResult(
                        data = PaginatedLaunchNormalList(
                            count = staleCached.size,
                            next = null,
                            previous = null,
                            results = staleCached
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp
                    )
                )
            }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getStarshipHistoryLaunches: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getNextDetailedLaunch(
        limit: Int
    ): Result<PaginatedLaunchDetailedList> {
        return try {
            val response = launchesApi.launchesDetailedList(
                limit = limit,
                upcoming = true,
                ordering = "net"
            )

            // Verbose debug logging for API response
            val rawResponse = response.response.bodyAsText()
            log.v { "API Response Debug - URL: ${response.response.request.url}, Status: ${response.status}, Length: ${rawResponse.length} chars" }
            log.v { "Full Response Body: $rawResponse" }

            // Check if it's an error response
            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "HTTP Error ${response.status}: $errorMessage" }
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            // Check if response looks like an error (contains "detail" field)
            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                log.e { "API returned error response: $errorMessage" }
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            // Try to deserialize the response body
            val body = response.body()
            Result.success(body)
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getNextDetailedLaunch: ${e.message}" }
            // Try to get the error response body
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

    override suspend fun getNextNormalLaunch(
        limit: Int
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net"
            )
            val body = response.body()
            Result.success(body)
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

    override suspend fun getLaunchById(id: String): Result<LaunchNormal?> {
        return try {
            log.d { "getLaunchById - id: $id" }
            val response = launchesApi.getLaunchById(launchId = id)
            val body = response.body()
            val launch = body.results?.firstOrNull()
            Result.success(launch)
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getLaunchById: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "IOException in getLaunchById: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getLaunchById: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    // Additional utility methods using the clean extension functions

    suspend fun searchLaunches(
        query: String,
        limit: Int = 20,
        upcoming: Boolean? = null
    ): Result<PaginatedLaunchNormalList> {
        return try {
            log.d { "searchLaunches - query: '$query', limit: $limit, upcoming: $upcoming" }
            val response = launchesApi.getLaunchList(
                search = query,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            val launches = response.body()
            log.i { "✅ API SUCCESS: Search returned ${launches.results.size} launches for query '$query'" }
            Result.success(launches)
        } catch (e: Exception) {
            log.e(e) { "❌ ERROR in searchLaunches for query '$query': ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun getLaunchesByCompany(
        lspIds: List<Int>,
        limit: Int = 20,
        upcoming: Boolean = true
    ): Result<PaginatedLaunchNormalList> {
        return try {
            log.d { "getLaunchesByCompany - lspIds: $lspIds, limit: $limit, upcoming: $upcoming" }
            val response = launchesApi.getLaunchList(
                lspId = lspIds,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            val launches = response.body()
            log.i { "✅ API SUCCESS: Fetched ${launches.results.size} launches for companies $lspIds" }
            Result.success(launches)
        } catch (e: Exception) {
            log.e(e) { "❌ ERROR in getLaunchesByCompany for lspIds $lspIds: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun getCrewedLaunches(
        limit: Int = 20,
        upcoming: Boolean = true
    ): Result<PaginatedLaunchNormalList> {
        return try {
            log.d { "getCrewedLaunches - limit: $limit, upcoming: $upcoming" }
            val response = launchesApi.getLaunchList(
                isCrewed = true,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            val launches = response.body()
            log.i { "✅ API SUCCESS: Fetched ${launches.results.size} crewed launches" }
            Result.success(launches)
        } catch (e: Exception) {
            log.e(e) { "❌ ERROR in getCrewedLaunches: ${e::class.simpleName}: ${e.message}" }
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

            // Check stale data first for fallback
            val staleResult = statsLocalDataSource?.getStatCountStale(key)

            // Try fresh cache if not forcing refresh
            if (!forceRefresh) {
                val cached = statsLocalDataSource?.getStatCount(key)
                if (cached != null) {
                    log.d { "Stats cache HIT for '$key': $cached" }
                    return Result.success(DataResult(data = cached, source = DataSource.CACHE, timestamp = now))
                }
                // Try stale
                if (staleResult != null) {
                    log.d { "Stats stale cache HIT for '$key': ${staleResult.first}" }
                    return Result.success(DataResult(data = staleResult.first, source = DataSource.STALE_CACHE, timestamp = staleResult.second))
                }
            }

            // Fetch from API
            log.d { "Fetching stats count from API for key='$key', netGt=$netGt, netLt=$netLt" }
            val response = launchesApi.getLaunchMiniList(
                limit = 1,
                upcoming = true,
                netGt = netGt,
                netLt = netLt
            )
            val count = response.body().count

            // Cache the count
            statsLocalDataSource?.cacheStat(key, count)
            log.i { "Stats API SUCCESS for '$key': count=$count" }

            Result.success(DataResult(data = count, source = DataSource.NETWORK, timestamp = now))
        } catch (e: Exception) {
            log.e(e) { "Error in getStatsCount for key='$key': ${e.message}" }
            // Fallback to stale on error
            val staleResult = statsLocalDataSource?.getStatCountStale(key)
            if (staleResult != null) {
                Result.success(DataResult(data = staleResult.first, source = DataSource.STALE_CACHE, timestamp = staleResult.second))
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Build a cache key that includes filter parameters to ensure
     * different filter combinations don't return stale cached data
     */
    private fun buildCacheKey(
        base: String,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): String {
        return buildString {
            append(base)
            if (agencyIds != null && agencyIds.isNotEmpty()) {
                append("_agencies_${agencyIds.sorted().joinToString(",")}")
            }
            if (locationIds != null && locationIds.isNotEmpty()) {
                append("_locations_${locationIds.sorted().joinToString(",")}")
            }
        }
    }

    /**
     * Filter launches by user's agency and location preferences.
     * Matches the API's AND logic: (lsp__id IN agencies) AND (location__ids IN locations)
     */
    private fun filterLaunchesByPreferences(
        launches: List<LaunchNormal>,
        agencyIds: List<Int>?,
        locationIds: List<Int>?
    ): List<LaunchNormal> {
        var filtered = launches

        // Filter by agency (LSP) if specified
        if (agencyIds != null && agencyIds.isNotEmpty()) {
            filtered = filtered.filter { launch ->
                launch.launchServiceProvider?.id?.let { it in agencyIds } ?: false
            }
        }

        // Filter by location if specified
        if (locationIds != null && locationIds.isNotEmpty()) {
            filtered = filtered.filter { launch ->
                launch.pad?.location?.id?.let { it in locationIds } ?: false
            }
        }

        return filtered
    }
}
