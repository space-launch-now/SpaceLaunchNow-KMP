package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlin.time.Clock.System
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
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

class LaunchRepositoryImpl(
    private val launchesApi: LaunchesApi,
    private val agenciesApi: AgenciesApi,
    private val appPreferences: AppPreferences,
    private val localDataSource: LaunchLocalDataSource? = null
) : LaunchRepository {

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
            val response = launchesApi.launchesMiniList(
                limit = limit,
                upcoming = true,
                ordering = "net" // Order by launch time
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
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
            println("=== LaunchRepository.getUpcomingLaunchesNormal ===")
            println("Parameters: limit=$limit, forceRefresh=$forceRefresh, agencyIds=$agencyIds, locationIds=$locationIds")
            
            val now = System.now().toEpochMilliseconds()
            
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
                    println("✓ CACHE HIT: Returning ${cachedLaunches.size} fresh cached launches")
                    return Result.success(DataResult(
                        data = PaginatedLaunchNormalList(
                            count = cachedLaunches.size,
                            next = null,
                            previous = null,
                            results = cachedLaunches
                        ),
                        source = DataSource.CACHE,
                        timestamp = staleTimestamp ?: now
                    ))
                } else if (hasStaleData) {
                    // Filter stale cache by user preferences (agency and location)
                    val filteredStale = filterLaunchesByPreferences(staleCached!!, agencyIds, locationIds)
                    println("⏳ STALE CACHE: Returning ${filteredStale.size}/${staleCached.size} filtered stale launches (filters: agencies=$agencyIds, locations=$locationIds)")
                    // Return stale data immediately - UI shows this while fetch happens in background
                    return Result.success(DataResult(
                        data = PaginatedLaunchNormalList(
                            count = filteredStale.size,
                            next = null,
                            previous = null,
                            results = filteredStale
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp ?: now
                    ))
                }
            }
            
            // Fetch from API (either no cache, expired, or force refresh)
            val hideTbd = withContext(Dispatchers.Default) { appPreferences.getHideTbdLaunches() }
            
            // NOTE: Current implementation uses AND logic (strict matching) when both filters are present.
            // Flexible mode (OR logic) would require two API calls and merge, which is deferred for MVP.
            // The API parameters use: (lsp__id IN agencies) AND (location__ids IN locations)
            // TODO: Implement flexible mode - see LAUNCH_FILTERS_HOME_INTEGRATION.md Phase 7
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net",
                lspId = agencyIds,
                locationIds = locationIds
            )
            
            val launches = response.body()
            val filtered = if (hideTbd) {
                launches.copy(results = launches.results.filterNot { it.status?.id == 8 })
            } else {
                launches
            }
            
            // Cache the results for future use
            localDataSource?.cacheNormalLaunches(filtered.results)
            println("✓ API SUCCESS: Fetched and cached ${filtered.results.size} upcoming launches (filters: agencies=$agencyIds, locations=$locationIds)")
            
            Result.success(DataResult(
                data = filtered,
                source = DataSource.NETWORK,
                timestamp = now
            ))
        } catch (e: ResponseException) {
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("upcoming_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Filter stale cache by user preferences
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                println("⚠️ API ERROR: Returning ${filteredStale.size}/${staleCached.size} filtered stale cached launches as fallback")
                return Result.success(DataResult(
                    data = PaginatedLaunchNormalList(
                        count = filteredStale.size,
                        next = null,
                        previous = null,
                        results = filteredStale
                    ),
                    source = DataSource.STALE_CACHE,
                    timestamp = staleTimestamp
                ))
            }
            Result.failure(e)
        } catch (e: IOException) {
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("upcoming_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Filter stale cache by user preferences
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                println("⚠️ NETWORK ERROR: Returning ${filteredStale.size}/${staleCached.size} filtered stale cached launches as fallback")
                return Result.success(DataResult(
                    data = PaginatedLaunchNormalList(
                        count = filteredStale.size,
                        next = null,
                        previous = null,
                        results = filteredStale
                    ),
                    source = DataSource.STALE_CACHE,
                    timestamp = staleTimestamp
                ))
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingLaunchesList(limit: Int, netGt: Instant?, netLt: Instant?): Result<PaginatedLaunchBasicList> {
        return try {
            val response = launchesApi.getLaunchMiniList(
                limit = limit,
                netGt = netGt,
                netLt = netLt,
                ordering = "net"
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
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
            println("=== LaunchRepository.getPreviousLaunchesNormal ===")
            println("Parameters: limit=$limit, forceRefresh=$forceRefresh, agencyIds=$agencyIds, locationIds=$locationIds")
            
            val now = System.now().toEpochMilliseconds()
            
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
                    println("✓ CACHE HIT: Returning ${cachedLaunches.size} fresh cached previous launches")
                    return Result.success(DataResult(
                        data = PaginatedLaunchNormalList(
                            count = cachedLaunches.size,
                            next = null,
                            previous = null,
                            results = cachedLaunches
                        ),
                        source = DataSource.CACHE,
                        timestamp = staleTimestamp ?: now
                    ))
                } else if (hasStaleData) {
                    // Apply user preference filters to stale cached data
                    val filteredStale = filterLaunchesByPreferences(staleCached!!, agencyIds, locationIds)
                    println("⏳ STALE CACHE: Returning ${filteredStale.size} filtered stale previous launches")
                    return Result.success(DataResult(
                        data = PaginatedLaunchNormalList(
                            count = filteredStale.size,
                            next = null,
                            previous = null,
                            results = filteredStale
                        ),
                        source = DataSource.STALE_CACHE,
                        timestamp = staleTimestamp ?: now
                    ))
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
            println("✓ API SUCCESS: Fetched and cached ${launches.results.size} previous launches (filters: agencies=$agencyIds, locations=$locationIds)")
            
            Result.success(DataResult(
                data = launches,
                source = DataSource.NETWORK,
                timestamp = now
            ))
        } catch (e: ResponseException) {
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getPreviousNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("previous_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Apply user preference filters to stale cached data
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                println("⚠️ API ERROR: Returning ${filteredStale.size} filtered stale cached previous launches as fallback")
                return Result.success(DataResult(
                    data = PaginatedLaunchNormalList(
                        count = filteredStale.size,
                        next = null,
                        previous = null,
                        results = filteredStale
                    ),
                    source = DataSource.STALE_CACHE,
                    timestamp = staleTimestamp
                ))
            }
            Result.failure(e)
        } catch (e: IOException) {
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getPreviousNormalLaunchesStale(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("previous_launches")
            if (staleCached != null && staleCached.isNotEmpty()) {
                // Filter stale cache by user preferences
                val filteredStale = filterLaunchesByPreferences(staleCached, agencyIds, locationIds)
                println("⚠️ NETWORK ERROR: Returning ${filteredStale.size}/${staleCached.size} filtered stale cached previous launches as fallback")
                return Result.success(DataResult(
                    data = PaginatedLaunchNormalList(
                        count = filteredStale.size,
                        next = null,
                        previous = null,
                        results = filteredStale
                    ),
                    source = DataSource.STALE_CACHE,
                    timestamp = staleTimestamp
                ))
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLaunchesByDayAndMonth(day: Int, month: Int, limit: Int): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                netDay = listOf(day.toDouble()),
                netMonth = listOf(month.toDouble()),
                ordering = "-net" // Most recent first
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLaunchDetails(id: String, forceRefresh: Boolean): Result<LaunchDetailed> {
        return try {
            println("=== LaunchRepository.getLaunchDetails ===")
            println("Parameters: id=$id, forceRefresh=$forceRefresh")
            
            // STALE-WHILE-REVALIDATE: Check for stale data
            val staleCached = localDataSource?.getDetailedLaunchStale(id)
            
            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedLaunch = localDataSource?.getDetailedLaunch(id)
                if (cachedLaunch != null) {
                    println("✓ CACHE HIT: Returning fresh cached detailed launch: ${cachedLaunch.name}")
                    return Result.success(cachedLaunch)
                } else if (staleCached != null) {
                    println("⏳ STALE CACHE: Returning stale detailed launch: ${staleCached.name}")
                }
            }
            
            // Fetch from API
            val response = launchesApi.launchesRetrieve(id)
            
            // Check if it's an error response
            if (response.status >= 400) {
                val rawResponse = response.response.bodyAsText()
                println("HTTP Error ${response.status}: $rawResponse")
                return Result.failure(Exception("API Error ${response.status}: $rawResponse"))
            }

            val body = response.body()
            
            // Cache the detailed launch for future use
            localDataSource?.cacheDetailedLaunch(body)
            println("✓ API SUCCESS: Fetched and cached detailed launch: ${body.name}")
            
            Result.success(body)
        } catch (e: ResponseException) {
            println("ResponseException in getLaunchDetails for ID $id: ${e.message}")
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                println("Error response body: $errorBody")
                
                // On error, try to return stale cache if available
                val staleCached = localDataSource?.getDetailedLaunchStale(id)
                if (staleCached != null) {
                    println("⚠️ API ERROR: Returning stale cached detailed launch as fallback: ${staleCached.name}")
                    return Result.success(staleCached)
                }
                
                Result.failure(Exception("API Error: $errorBody"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            println("IOException in getLaunchDetails for ID $id: ${e.message}")
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getDetailedLaunchStale(id)
            if (staleCached != null) {
                println("⚠️ NETWORK ERROR: Returning stale cached detailed launch as fallback: ${staleCached.name}")
                return Result.success(staleCached)
            }
            Result.failure(e)
        } catch (e: Exception) {
            println("Exception in getLaunchDetails for ID $id: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed> {
        return try {
            val response = agenciesApi.agenciesRetrieve(id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNextLaunch(
        limit: Int,
        forceRefresh: Boolean
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net" // Order by launch time
            )

            // Print raw response for debugging
            val rawResponse = response.response.bodyAsText()
            println("=== FULL API RESPONSE DEBUG ===")
            println("Request URL: ${response.response.request.url}")
            println("Request headers: ${response.response.request.headers}")
            println("Response status: ${response.status}")
            println("Response headers: ${response.response.headers}")
            println("Response length: ${rawResponse.length} characters")
            println("=== FULL RESPONSE BODY ===")
            println(rawResponse)
            println("=== END FULL RESPONSE ===")

            // Check if it's an error response
            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                println("HTTP Error ${response.status}: $errorMessage")
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            // Check if response looks like an error (contains "detail" field)
            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                println("API returned error response: $errorMessage")
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            // Try to deserialize the response body
            val body = response.body()
            Result.success(body)
        } catch (e: ResponseException) {
            println("ResponseException in getNextLaunch: ${e.message}")
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                val errorMessage = parseApiError(errorBody)
                println("Error response body: $errorMessage")
                Result.failure(Exception("API Error: $errorMessage"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            println("IOException in getNextLaunch: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Exception in getNextLaunch: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
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

            // Print raw response for debugging
            val rawResponse = response.response.bodyAsText()
            println("=== FULL API RESPONSE DEBUG ===")
            println("Request URL: ${response.response.request.url}")
            println("Request headers: ${response.response.request.headers}")
            println("Response status: ${response.status}")
            println("Response headers: ${response.response.headers}")
            println("Response length: ${rawResponse.length} characters")
            println("=== FULL RESPONSE BODY ===")
            println(rawResponse)
            println("=== END FULL RESPONSE ===")

            // Check if it's an error response
            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                println("HTTP Error ${response.status}: $errorMessage")
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            // Check if response looks like an error (contains "detail" field)
            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                println("API returned error response: $errorMessage")
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            // Try to deserialize the response body
            val body = response.body()
            Result.success(body)
        } catch (e: ResponseException) {
            println("ResponseException in getNextLaunch: ${e.message}")
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                val errorMessage = parseApiError(errorBody)
                println("Error response body: $errorMessage")
                Result.failure(Exception("API Error: $errorMessage"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            println("IOException in getNextLaunch: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Exception in getNextLaunch: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
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
            val response = launchesApi.getLaunchList(
                search = query,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLaunchesByCompany(
        lspIds: List<Int>,
        limit: Int = 20,
        upcoming: Boolean = true
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                lspId = lspIds,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCrewedLaunches(
        limit: Int = 20,
        upcoming: Boolean = true
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                isCrewed = true,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Build a cache key that includes filter parameters to ensure
     * different filter combinations don't return stale cached data
     */
    private fun buildCacheKey(base: String, agencyIds: List<Int>?, locationIds: List<Int>?): String {
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
