package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getLatestUpdates
import me.calebjones.spacelaunchnow.api.extensions.getUpdates
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.database.UpdateLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Update
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock

/**
 * Updates repository, backed directly by the Launch Library 2 (LL) API - the pre-Trantor-migration
 * implementation, resurrected as a standalone class so the app can fall back to LL in production
 * via the `DataBackend` revert lever (see Koin wiring). Implements the CURRENT [UpdatesRepository]
 * interface (domain-typed throughout).
 *
 * LL's `UpdateEndpoint` (`api.launchlibrary.models`, returned by `UpdatesApi.updatesList`) nests a
 * real launch/event/program on every row, so its existing `toDomain()` mapper in
 * `domain/mapper/CommonMappers.kt` - untouched by the Trantor migration - already produces the
 * current domain [Update] shape (`launch: LaunchRef?`, `event: UpdateEventRef?`) without any
 * bridging needed here; contrast with Trantor's standalone updates feed, which denormalizes to
 * launch_id/launch_name and needs its own mapper for the same domain shape.
 *
 * Mirrors the caching behavior of the Trantor-backed
 * [me.calebjones.spacelaunchnow.data.repository.UpdatesRepositoryImpl]: every network result is
 * mapped LL -> domain before being cached through [UpdateLocalDataSource], which - since the
 * events/updates unit - stores and reads back only the domain [Update] model. This class never
 * constructs an [UpdateLocalDataSource] cache write from a raw LL wire model.
 */
class LLUpdatesRepositoryImpl(
    private val updatesApi: UpdatesApi,
    private val localDataSource: UpdateLocalDataSource? = null
) : UpdatesRepository {

    private val log = logger()

    override suspend fun getLatestUpdates(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Update>>> {
        return try {
            log.d { "getLatestUpdates - limit: $limit, forceRefresh: $forceRefresh, cacheAvailable: ${localDataSource != null}" }

            val now = Clock.System.now().toEpochMilliseconds()
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")

            if (!forceRefresh) {
                val cachedUpdates = localDataSource?.getRecentUpdates(limit)
                if (!cachedUpdates.isNullOrEmpty()) {
                    log.i { "Cache hit - Returning ${cachedUpdates.size} cached updates" }
                    return Result.success(
                        DataResult(
                            data = PaginatedResult(
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
            val body = response.body()
            val updates = body.results.map { it.toDomain() }

            // Cache the domain-mapped results (not the LL wire model) for future use.
            localDataSource?.cacheUpdates(updates)
            log.i { "API SUCCESS: Fetched and cached ${updates.size} updates" }

            Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = body.count,
                        next = body.next,
                        previous = body.previous,
                        results = updates
                    ),
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching updates: ${e.message}" }
            val staleCached = localDataSource?.getRecentUpdates(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")
            if (!staleCached.isNullOrEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached updates due to API error" }
                return Result.success(
                    DataResult(
                        data = PaginatedResult(
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
            log.e(e) { "Network error while fetching updates: ${e.message}" }
            val staleCached = localDataSource?.getRecentUpdates(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("updates")
            if (!staleCached.isNullOrEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached updates due to network error" }
                return Result.success(
                    DataResult(
                        data = PaginatedResult(
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
            log.e(e) { "Unexpected error while fetching updates: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getUpdatesByProgram(
        allProgram: Int,
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Update>>> {
        return try {
            log.d { "getUpdatesByProgram - allProgram: $allProgram, limit: $limit, forceRefresh: $forceRefresh" }

            val now = Clock.System.now().toEpochMilliseconds()

            // Matches the pre-migration behavior: program-specific queries skip caching.
            val response = updatesApi.getUpdates(
                limit = limit,
                allProgram = allProgram,
                ordering = "-created_on"
            )
            val body = response.body()
            val updates = body.results.map { it.toDomain() }

            log.i { "API SUCCESS: Fetched ${updates.size} updates for program $allProgram" }

            Result.success(
                DataResult(
                    data = PaginatedResult(
                        count = body.count,
                        next = body.next,
                        previous = body.previous,
                        results = updates
                    ),
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "API error for program $allProgram: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error for program $allProgram: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error for program $allProgram: ${e.message}" }
            Result.failure(e)
        }
    }
}
