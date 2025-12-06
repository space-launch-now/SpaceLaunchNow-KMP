package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getEventList
import me.calebjones.spacelaunchnow.api.extensions.getUpcomingEvents
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.EventLocalDataSource
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Clock.System


class EventsRepositoryImpl(
    private val eventsApi: EventsApi,
    private val localDataSource: EventLocalDataSource? = null
) : EventsRepository {

    private val log = logger()

    override suspend fun getUpcomingEvents(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedEventEndpointNormalList>> {
        return try {
            log.d { "getUpcomingEvents called - limit: $limit, forceRefresh: $forceRefresh, cacheAvailable: ${localDataSource != null}" }

            val now = Clock.System.now().toEpochMilliseconds()

            val staleTimestamp = localDataSource?.getCacheTimestamp("events")

            // Try cache first if available and not forcing refresh
            if (!forceRefresh) {
                val cachedEvents = localDataSource?.getUpcomingEvents(limit)
                log.v { "Cache query result: ${cachedEvents?.size ?: 0} events found" }
                if (cachedEvents != null && cachedEvents.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cachedEvents.size} cached upcoming events" }
                    return Result.success(
                        DataResult(
                            data = PaginatedEventEndpointNormalList(
                                count = cachedEvents.size,
                                next = null,
                                previous = null,
                                results = cachedEvents
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

            log.d { "Fetching upcoming events from API - limit: $limit" }

            val response = eventsApi.getUpcomingEvents(
                limit = limit,
                ordering = "date"
            )

            val events = response.body()
            log.i { "Successfully fetched ${events.results.size} upcoming events from API (status: ${response.status})" }

            // Cache the results for future use
            localDataSource?.cacheEvents(events.results)
            log.d { "Cached ${events.results.size} upcoming events for future use" }

            Result.success(
                DataResult(
                    data = events,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )

        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching upcoming events (status: ${e.response.status})" }
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingEvents(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("events")
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached events due to API error" }
                return Result.success(
                    DataResult(
                        data = PaginatedEventEndpointNormalList(
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
            log.e(e) { "Network error while fetching upcoming events" }
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingEvents(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("events")
            if (staleCached != null && staleCached.isNotEmpty()) {

                log.w { "Returning ${staleCached.size} stale cached events due to network error" }
                return Result.success(
                    DataResult(
                        data = PaginatedEventEndpointNormalList(
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
            log.e(e) { "Unexpected error while fetching upcoming events" }
            Result.failure(e)
        }
    }

    override suspend fun getEventsByType(
        typeIds: List<Int>,
        limit: Int
    ): Result<PaginatedEventEndpointNormalList> {
        return try {
            log.d { "getEventsByType called - typeIds: $typeIds, limit: $limit" }

            val response = eventsApi.getEventList(
                limit = limit,
                typeIds = typeIds,
                ordering = "date"
            )

            val body = response.body()
            log.i { "Successfully fetched ${body.results.size} events by type (status: ${response.status})" }

            Result.success(body)

        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching events by type (status: ${e.response.status})" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while fetching events by type" }
            Result.failure(e)
        }
    }

    override suspend fun getEventsByProgram(
        programId: Int,
        limit: Int,
        upcoming: Boolean?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedEventEndpointNormalList>> {
        return try {
            println("=== EventsRepository.getEventsByProgram ===")
            println("Parameters: programId=$programId, limit=$limit, upcoming=$upcoming, forceRefresh=$forceRefresh")

            val now = System.now().toEpochMilliseconds()

            // For now, skip caching for program-specific queries
            // TODO: Add program-specific caching if needed

            println("EventsRepository: Fetching program $programId events from API")
            val response = eventsApi.getEventList(
                limit = limit,
                upcoming = upcoming,
                program = listOf(programId),
                ordering = "date"
            )

            val events = response.body()
            println("✓ API SUCCESS: Fetched ${events.results.size} events for program $programId")

            Result.success(
                DataResult(
                    data = events,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            println("EventsRepository: API error for program $programId: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("EventsRepository: Network error for program $programId: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("EventsRepository: Unexpected error for program $programId: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getEvents(
        limit: Int,
        upcoming: Boolean?,
        typeIds: List<Int>?
    ): Result<PaginatedEventEndpointNormalList> {
        return try {
            log.d { "getEvents called - limit: $limit, upcoming: $upcoming, typeIds: $typeIds" }

            val response = eventsApi.getEventList(
                limit = limit,
                upcoming = upcoming,
                typeIds = typeIds,
                ordering = "date"
            )
            val body = response.body()
            log.i { "Successfully fetched ${body.results.size} events (status: ${response.status})" }

            Result.success(body)

        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching events (status: ${e.response.status})" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while fetching events" }
            Result.failure(e)
        }
    }

    override suspend fun getEventDetails(eventId: Int): Result<EventEndpointDetailed> {
        return try {
            log.d { "Getting event details for $eventId" }
            val response = eventsApi.eventsRetrieve(eventId)
            log.i { "Successfully fetched event details (status: ${response.status})" }
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API Error: Status ${e.response.status}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Failed to get event details" }
            Result.failure(e)
        }
    }
}
