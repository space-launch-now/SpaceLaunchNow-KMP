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
import kotlin.time.Clock.System

class EventsRepositoryImpl(
    private val eventsApi: EventsApi,
    private val localDataSource: EventLocalDataSource? = null
) : EventsRepository {

    override suspend fun getUpcomingEvents(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedEventEndpointNormalList>> {
        return try {
            println("=== EventsRepository.getUpcomingEvents ===")
            println("Parameters: limit=$limit, forceRefresh=$forceRefresh")
            println("Cache available: ${localDataSource != null}")

            val now = System.now().toEpochMilliseconds()
            val staleTimestamp = localDataSource?.getCacheTimestamp("events")

            // Try cache first if available and not forcing refresh
            if (!forceRefresh) {
                val cachedEvents = localDataSource?.getUpcomingEvents(limit)
                println("Cache query result: ${cachedEvents?.size ?: 0} events found")
                if (cachedEvents != null && cachedEvents.isNotEmpty()) {
                    println("✓ CACHE HIT: Returning ${cachedEvents.size} cached upcoming events")
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
                    println("✗ CACHE MISS: No cached data available, fetching from API")
                }
            } else {
                println("⟳ FORCE REFRESH: Bypassing cache, fetching fresh data from API")
            }

            println("=== EventsRepository: Getting upcoming events from API (forceRefresh: $forceRefresh) ===")
            println("Limit: $limit")

            val response = eventsApi.getUpcomingEvents(
                limit = limit,
                ordering = "date"
            )

            println("Response status: ${response.status}")
            val events = response.body()
            println("Events count: ${events.results.size}")

            // Cache the results for future use
            localDataSource?.cacheEvents(events.results)
            println("✓ API SUCCESS: Fetched and cached ${events.results.size} upcoming events")

            Result.success(
                DataResult(
                    data = events,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingEvents(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("events")
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("EventsRepository: Returning ${staleCached.size} stale cached events due to API error")
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
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getUpcomingEvents(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("events")
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("EventsRepository: Returning ${staleCached.size} stale cached events due to network error")
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
            println("Failed to get upcoming events: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getEventsByType(
        typeIds: List<Int>,
        limit: Int
    ): Result<PaginatedEventEndpointNormalList> {
        return try {
            println("=== EventsRepository: Getting events by type ===")
            println("Type IDs: $typeIds")
            println("Limit: $limit")

            val response = eventsApi.getEventList(
                limit = limit,
                typeIds = typeIds,
                ordering = "date"
            )

            println("Response status: ${response.status}")
            println("Events count: ${response.body()?.results?.size ?: 0}")

            Result.success(response.body())
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get events by type: ${e.message}")
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
            println("=== EventsRepository: Getting events ===")
            println("Limit: $limit, Upcoming: $upcoming, Type IDs: $typeIds")

            val response = eventsApi.getEventList(
                limit = limit,
                upcoming = upcoming,
                typeIds = typeIds,
                ordering = "date"
            )

            println("Response status: ${response.status}")
            println("Events count: ${response.body()?.results?.size ?: 0}")

            Result.success(response.body())
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get events: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getEventDetails(eventId: Int): Result<EventEndpointDetailed> {
        return try {
            println("=== EventsRepository: Getting event details for $eventId ===")
            val response = eventsApi.eventsRetrieve(eventId)
            println("Response status: ${response.status}")
            Result.success(response.body())
        } catch (e: ResponseException) {
            println("API Error: Status ${e.response.status}, Message: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get event details: ${e.message}")
            Result.failure(e)
        }
    }
}
