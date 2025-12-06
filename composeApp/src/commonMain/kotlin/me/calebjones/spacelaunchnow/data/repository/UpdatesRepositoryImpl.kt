package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.extensions.getLatestUpdates
import me.calebjones.spacelaunchnow.api.extensions.getUpdates
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.data.model.ApiError
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.UpdateLocalDataSource
import kotlin.time.Clock.System

class UpdatesRepositoryImpl(
    private val updatesApi: UpdatesApi,
    private val localDataSource: UpdateLocalDataSource? = null
) : UpdatesRepository {

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

    override suspend fun getLatestUpdates(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedUpdateEndpointList>> {
        return try {
            println("=== UpdatesRepository.getLatestUpdates ===")
            println("Parameters: limit=$limit, forceRefresh=$forceRefresh")
            println("Cache available: ${localDataSource != null}")

            val now = System.now().toEpochMilliseconds()
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")

            // Try cache first if available and not forcing refresh
            if (!forceRefresh) {
                val cachedUpdates = localDataSource?.getRecentUpdates(limit)
                println("Cache query result: ${cachedUpdates?.size ?: 0} updates found")
                if (cachedUpdates != null && cachedUpdates.isNotEmpty()) {
                    println("✓ CACHE HIT: Returning ${cachedUpdates.size} cached updates")
                    return Result.success(
                        DataResult(
                            data = PaginatedUpdateEndpointList(
                                count = cachedUpdates.size,
                                next = null,
                                previous = null,
                                results = cachedUpdates
                            ),
                            source = DataSource.CACHE,
                            timestamp = staleTimestamp ?: now
                        )
                    )
                } else {
                    println("✗ CACHE MISS: No cached data available, fetching from API")
                }
            } else {
                println("⟳ FORCE REFRESH: Bypassing cache, fetching fresh data from API")
            }

            println("UpdatesRepository: Fetching updates from API (forceRefresh: $forceRefresh)")
            val response = updatesApi.getLatestUpdates(limit = limit)
            val updates = response.body()

            // Cache the results for future use
            localDataSource?.cacheUpdates(updates.results)
            println("✓ API SUCCESS: Fetched and cached ${updates.results.size} updates")

            Result.success(
                DataResult(
                    data = updates,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentUpdates(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("UpdatesRepository: Returning ${staleCached.size} stale cached updates due to API error")
                return Result.success(
                    DataResult(
                        data = PaginatedUpdateEndpointList(
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
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentUpdates(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("UpdatesRepository: Returning ${staleCached.size} stale cached updates due to network error")
                return Result.success(
                    DataResult(
                        data = PaginatedUpdateEndpointList(
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
            Result.failure(e)
        }
    }

    override suspend fun getUpdatesByProgram(
        allProgram: Int,
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedUpdateEndpointList>> {
        return try {
            println("=== UpdatesRepository.getUpdatesByProgram ===")
            println("Parameters: allProgram=$allProgram, limit=$limit, forceRefresh=$forceRefresh")

            val now = System.now().toEpochMilliseconds()

            // For now, skip caching for program-specific queries
            // TODO: Add program-specific caching if needed

            println("UpdatesRepository: Fetching program $allProgram updates from API")
            val response = updatesApi.getUpdates(
                limit = limit,
                allProgram = allProgram,
                ordering = "-created_on"
            )
            val updates = response.body()

            println("✓ API SUCCESS: Fetched ${updates.results.size} updates for program $allProgram")

            Result.success(
                DataResult(
                    data = updates,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            println("UpdatesRepository: API error for program $allProgram: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("UpdatesRepository: Network error for program $allProgram: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("UpdatesRepository: Unexpected error for program $allProgram: ${e.message}")
            Result.failure(e)
        }
    }
}
