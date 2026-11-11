package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList as LLPaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.api.terminus.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.terminus.apis.EventsApi
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toLL
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.EventType
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Events repository for the Terminus backend. Every [EventsRepository] method is served by
 * Terminus (`GET /api/v1/events/`, `GET /api/v1/events/{id}/`, `GET /api/v1/config/event_types/`),
 * so nothing delegates to LL. The list endpoint is the normal variant, as the LL repository
 * uses; the detail endpoint is the detailed variant.
 *
 * Reads go through [terminusCachedFetch] with a [TerminusMemoryCache] (30 s fresh, stale kept
 * for a 5xx or a network failure). The events table in SQLDelight holds LL rows by id and has no
 * place for a list keyed by request, and a schema change is out of scope, so the cache is in
 * memory.
 */
class TerminusEventsRepositoryImpl internal constructor(
    private val eventsApi: EventsApi,
    private val configApi: ConfigApi,
    private val cache: TerminusMemoryCache
) : EventsRepository {

    constructor(eventsApi: EventsApi, configApi: ConfigApi) : this(eventsApi, configApi, TerminusMemoryCache())

    private val log = logger()

    private suspend fun <T : Any> read(
        key: String,
        what: String,
        forceRefresh: Boolean,
        fetch: suspend () -> HttpResponse<T>
    ): Result<TerminusFetched<T>> {
        log.d { "terminus read $what, forceRefresh: $forceRefresh" }
        return terminusMemoryCachedFetch(cache, key, what, forceRefresh, fetch)
    }

    private suspend fun list(query: EventListQuery, forceRefresh: Boolean = false) =
        read(query.key, "events ${query.logLabel}", forceRefresh) { query.fetch(eventsApi) }

    // -- LL page methods (the interface still returns the LL page type) ---------------------

    override suspend fun getEventsByProgram(
        programId: Int,
        limit: Int,
        upcoming: Boolean?,
        forceRefresh: Boolean
    ): Result<DataResult<LLPaginatedEventEndpointNormalList>> =
        list(
            EventListQuery.of(limit = limit, upcoming = upcoming, program = listOf(programId), ordering = "date"),
            forceRefresh
        ).mapCatching { DataResult(it.value.toLL(), it.source, it.timestamp) }

    override suspend fun getEvents(
        limit: Int,
        upcoming: Boolean?,
        typeIds: List<Int>?
    ): Result<LLPaginatedEventEndpointNormalList> =
        list(EventListQuery.of(limit = limit, upcoming = upcoming, typeIds = typeIds, ordering = "date"))
            .mapCatching { it.value.toLL() }

    // -- Domain methods ---------------------------------------------------------------------

    override suspend fun getUpcomingEventsDomain(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> =
        list(EventListQuery.of(limit = limit, upcoming = true, ordering = "date"), forceRefresh)
            .mapCatching { DataResult(it.value.toDomain(), it.source, it.timestamp) }

    override suspend fun getEventDetailDomain(eventId: Int): Result<Event> =
        read("event-detail?id=$eventId", "event detail $eventId", forceRefresh = false) {
            eventsApi.eventsRetrieve(eventId)
        }.mapCatching { it.value.toDomain() }

    override suspend fun getEventsByTypeDomain(
        typeIds: List<Int>,
        limit: Int
    ): Result<PaginatedResult<Event>> =
        list(EventListQuery.of(limit = limit, typeIds = typeIds, ordering = "date"))
            .mapCatching { it.value.toDomain() }

    override suspend fun getEventsByLaunchIdDomain(
        launchId: String,
        limit: Int
    ): Result<PaginatedResult<Event>> =
        list(EventListQuery.of(limit = limit, launchIds = listOf(launchId), ordering = "-date"))
            .mapCatching { it.value.toDomain() }

    override suspend fun getEventsPaginatedDomain(
        limit: Int,
        offset: Int,
        search: String?,
        typeIds: List<Int>?,
        upcoming: Boolean?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> =
        list(
            EventListQuery.of(
                limit = limit,
                offset = offset,
                search = search,
                typeIds = typeIds,
                upcoming = upcoming,
                ordering = "date"
            ),
            forceRefresh
        ).mapCatching { DataResult(it.value.toDomain(), it.source, it.timestamp) }

    override suspend fun getEventTypesDomain(): Result<List<EventType>> =
        read("event-types", "event types", forceRefresh = false) { configApi.configEventTypesList() }
            .mapCatching { it.value.toDomain() }
}
