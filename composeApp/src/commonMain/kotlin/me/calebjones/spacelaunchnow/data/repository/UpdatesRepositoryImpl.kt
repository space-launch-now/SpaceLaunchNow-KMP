package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getLatestUpdates
import me.calebjones.spacelaunchnow.api.extensions.getUpdates
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.UpdateLocalDataSource
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Clock.System

class UpdatesRepositoryImpl(
    private val updatesApi: UpdatesApi,
    private val localDataSource: UpdateLocalDataSource? = null
) : UpdatesRepository {

    private val log = logger()

    override suspend fun getLatestUpdates(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedUpdateEndpointList>> {
        return try {
            log.d { "getLatestUpdates called - limit: $limit, forceRefresh: $forceRefresh, cacheAvailable: ${localDataSource != null}" }

            val now = Clock.System.now().toEpochMilliseconds()
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")

            // Try cache first if available and not forcing refresh
            if (!forceRefresh) {
                val cachedUpdates = localDataSource?.getRecentUpdates(limit)
                log.v { "Cache query result: ${'$'}{cachedUpdates?.size ?: 0} updates found" }
                if (cachedUpdates != null && cachedUpdates.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${'$'}{cachedUpdates.size} cached updates" }
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
                    log.d { "Cache miss - No cached data available, fetching from API" }
                }
            } else {
                log.d { "Force refresh - Bypassing cache, fetching fresh data from API" }
            }

            log.d { "Fetching updates from API" }
            val response = updatesApi.getLatestUpdates(limit = limit)
            val updates = response.body()

            // Cache the results for future use
            localDataSource?.cacheUpdates(updates.results)
            log.i { "Successfully fetched and cached ${'$'}{updates.results.size} updates" }

            Result.success(
                DataResult(
                    data = updates,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching updates" }
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentUpdates(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${'$'}{staleCached.size} stale cached updates due to API error" }
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
            log.e(e) { "Network error while fetching updates" }
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentUpdates(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${'$'}{staleCached.size} stale cached updates due to network error" }
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
            log.e(e) { "Unexpected error while fetching updates" }
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
