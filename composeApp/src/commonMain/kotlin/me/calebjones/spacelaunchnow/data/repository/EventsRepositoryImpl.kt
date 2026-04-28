package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getEventList
import me.calebjones.spacelaunchnow.api.extensions.getEventsByLaunchId
import me.calebjones.spacelaunchnow.api.extensions.getUpcomingEvents
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.EventLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.EventType
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Clock.System


class EventsRepositoryImpl(
    private val eventsApi: EventsApi,
    private val configApi: ConfigApi,
    private val localDataSource: EventLocalDataSource? = null
) : EventsRepository {

    private val log = logger()

    private suspend fun getUpcomingEvents(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedEventEndpointNormalList>> {
        return try {
            log.d { "getUpcomingEvents called - limit: $limit, forceRefresh: $forceRefresh, cacheAvailable: ${localDataSource != null}" }

            val now = Clock.System.now().toEpochMilliseconds()

            val staleTimestamp = localDataSource?.getCacheTimestamp("events")

            // Try cache first if available and not forcing refresh
            if (!forceRefresh) {
                val cachedEvents = localDataSource?.getUpcomingEventsApi(limit)
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
            val staleCached = localDataSource?.getUpcomingEventsApi(limit)
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
            val staleCached = localDataSource?.getUpcomingEventsApi(limit)
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

    private suspend fun getEventsByType(
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

    private suspend fun getEventDetails(eventId: Int): Result<EventEndpointDetailed> {
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

    private suspend fun getEventsByLaunchId(
        launchId: String,
        limit: Int
    ): Result<PaginatedEventEndpointNormalList> {
        return try {
            log.d { "getEventsByLaunchId called - launchId: $launchId, limit: $limit" }

            val response = eventsApi.getEventsByLaunchId(
                launchId = launchId,
                limit = limit,
                ordering = "-date"
            )

            val body = response.body()
            log.i { "Successfully fetched ${body.results.size} events for launch $launchId (status: ${response.status})" }

            Result.success(body)

        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching events for launch $launchId (status: ${e.response.status})" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while fetching events for launch $launchId" }
            Result.failure(e)
        }
    }

    private suspend fun getEventsPaginated(
        limit: Int,
        offset: Int,
        search: String?,
        typeIds: List<Int>?,
        upcoming: Boolean?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedEventEndpointNormalList>> {
        return try {
            log.d { "getEventsPaginated - limit: $limit, offset: $offset, search: $search, typeIds: $typeIds, upcoming: $upcoming" }
            
            val now = Clock.System.now().toEpochMilliseconds()
            
            val response = eventsApi.getEventList(
                limit = limit,
                offset = offset,
                search = search,
                typeIds = typeIds,
                upcoming = upcoming,
                ordering = "date"
            )
            
            val body = response.body()
            log.i { "Successfully fetched ${body.results.size} events (offset: $offset)" }
            
            Result.success(DataResult(
                data = body,
                source = DataSource.NETWORK,
                timestamp = now
            ))
        } catch (e: ResponseException) {
            log.e(e) { "API error in getEventsPaginated" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getEventsPaginated" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getEventsPaginated" }
            Result.failure(e)
        }
    }

    // ── Domain-returning method implementations ───────────────────────────

    override suspend fun getUpcomingEventsDomain(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> {
        return getUpcomingEvents(limit, forceRefresh).map { dataResult ->
            DataResult(
                data = dataResult.data.toDomain(),
                source = dataResult.source,
                timestamp = dataResult.timestamp
            )
        }
    }

    override suspend fun getEventDetailDomain(eventId: Int): Result<Event> {
        return getEventDetails(eventId).map { it.toDomain() }
    }

    override suspend fun getEventsByTypeDomain(
        typeIds: List<Int>,
        limit: Int
    ): Result<PaginatedResult<Event>> {
        return getEventsByType(typeIds, limit).map { it.toDomain() }
    }

    override suspend fun getEventsByLaunchIdDomain(
        launchId: String,
        limit: Int
    ): Result<PaginatedResult<Event>> {
        return getEventsByLaunchId(launchId, limit).map { it.toDomain() }
    }

    override suspend fun getEventsPaginatedDomain(
        limit: Int,
        offset: Int,
        search: String?,
        typeIds: List<Int>?,
        upcoming: Boolean?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> {
        return getEventsPaginated(limit, offset, search, typeIds, upcoming, forceRefresh).map { dataResult ->
            DataResult(
                data = dataResult.data.toDomain(),
                source = dataResult.source,
                timestamp = dataResult.timestamp
            )
        }
    }

    override suspend fun getEventTypesDomain(): Result<List<EventType>> {
        return try {
            log.d { "getEventTypesDomain called" }
            val response = configApi.configEventTypesList()
            val types = response.body().results.map { it.toDomain() }
            log.i { "Fetched ${types.size} event types" }
            Result.success(types)
        } catch (e: ResponseException) {
            log.e(e) { "API error fetching event types" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error fetching event types" }
            Result.failure(e)
        }
    }
}
