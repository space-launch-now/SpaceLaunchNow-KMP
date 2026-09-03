package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getEventList
import me.calebjones.spacelaunchnow.api.extensions.getEventsByLaunchId
import me.calebjones.spacelaunchnow.api.extensions.getUpcomingEvents
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.EventType
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock

/**
 * Events repository, backed directly by the Launch Library 2 (LL) API - the pre-Trantor-migration
 * implementation, resurrected as a standalone class so the app can fall back to LL in production
 * via the `DataBackend` revert lever (see Koin wiring). Implements the CURRENT [EventsRepository]
 * interface (domain-typed throughout), mapping LL's `api.launchlibrary.models` event types to
 * domain via the existing `domain/mapper` extension functions in `EventMappers.kt` (untouched by
 * the Trantor migration) before returning them.
 *
 * No local caching: [me.calebjones.spacelaunchnow.database.EventLocalDataSource]'s write path
 * (`cacheEvent`/`cacheEvents`) and stale-cache read path (`getUpcomingEventsApi`/`getAllEventsApi`)
 * were retyped during the Trantor migration from LL's `EventEndpointNormal` wire model to
 * Trantor's `EventList` wire model - not to the domain [Event] this class deals in, and not to
 * anything LL data can produce either. Wiring this fallback into that cache would mean either
 * fabricating a Trantor-shaped wire row from LL data, or silently serving another backend's stale
 * cache rows through the LL path; both were rejected, so every method here is network-only. This
 * mirrors the documented events/updates cache-migration gap (E5) called out in the "Trantor
 * mappers" section of `EventMappers.kt`. See the phase5-events-updates unit report for the
 * escalation on restoring event caching for this fallback path.
 */
class LLEventsRepositoryImpl(
    private val eventsApi: EventsApi,
    private val configApi: ConfigApi
) : EventsRepository {

    private val log = logger()

    override suspend fun getEventsByProgram(
        programId: Int,
        limit: Int,
        upcoming: Boolean?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> {
        return try {
            log.d { "getEventsByProgram - programId: $programId, limit: $limit, upcoming: $upcoming, forceRefresh: $forceRefresh" }
            val now = Clock.System.now().toEpochMilliseconds()

            val response = eventsApi.getEventList(
                limit = limit,
                upcoming = upcoming,
                program = listOf(programId),
                ordering = "date"
            )

            val events = response.body()
            log.i { "API SUCCESS: Fetched ${events.results.size} events for program $programId" }

            Result.success(
                DataResult(
                    data = events.toDomain(),
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "API error in getEventsByProgram for program $programId: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getEventsByProgram for program $programId: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getEventsByProgram for program $programId: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingEventsDomain(
        limit: Int,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> {
        return try {
            log.d { "getUpcomingEventsDomain - limit: $limit, forceRefresh: $forceRefresh" }
            val now = Clock.System.now().toEpochMilliseconds()

            val response = eventsApi.getUpcomingEvents(
                limit = limit,
                ordering = "date"
            )

            val events = response.body()
            log.i { "API SUCCESS: Fetched ${events.results.size} upcoming events" }

            Result.success(
                DataResult(
                    data = events.toDomain(),
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "API error in getUpcomingEventsDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getUpcomingEventsDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getUpcomingEventsDomain: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getEventDetailDomain(eventId: Int): Result<Event> {
        return try {
            log.d { "getEventDetailDomain - eventId: $eventId" }
            val response = eventsApi.eventsRetrieve(eventId)
            log.i { "API SUCCESS: Fetched event details (status: ${response.status})" }
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getEventDetailDomain for $eventId: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getEventDetailDomain for $eventId: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getEventsByTypeDomain(
        typeIds: List<Int>,
        limit: Int
    ): Result<PaginatedResult<Event>> {
        return try {
            log.d { "getEventsByTypeDomain - typeIds: $typeIds, limit: $limit" }
            val response = eventsApi.getEventList(
                limit = limit,
                typeIds = typeIds,
                ordering = "date"
            )
            val body = response.body()
            log.i { "API SUCCESS: Fetched ${body.results.size} events by type" }
            Result.success(body.toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getEventsByTypeDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getEventsByTypeDomain: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getEventsByLaunchIdDomain(
        launchId: String,
        limit: Int
    ): Result<PaginatedResult<Event>> {
        return try {
            log.d { "getEventsByLaunchIdDomain - launchId: $launchId, limit: $limit" }
            val response = eventsApi.getEventsByLaunchId(
                launchId = launchId,
                limit = limit,
                ordering = "-date"
            )
            val body = response.body()
            log.i { "API SUCCESS: Fetched ${body.results.size} events for launch $launchId" }
            Result.success(body.toDomain())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getEventsByLaunchIdDomain for $launchId: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getEventsByLaunchIdDomain for $launchId: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getEventsPaginatedDomain(
        limit: Int,
        offset: Int,
        search: String?,
        typeIds: List<Int>?,
        upcoming: Boolean?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Event>>> {
        return try {
            log.d { "getEventsPaginatedDomain - limit: $limit, offset: $offset, search: $search, typeIds: $typeIds, upcoming: $upcoming" }
            val now = Clock.System.now().toEpochMilliseconds()

            val response = eventsApi.getEventList(
                limit = limit,
                offset = offset,
                search = search,
                typeIds = typeIds,
                upcoming = upcoming,
                ordering = "date"
            )

            val events = response.body()
            log.i { "API SUCCESS: Fetched ${events.results.size} events (offset: $offset)" }

            Result.success(
                DataResult(
                    data = events.toDomain(),
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            log.e(e) { "API error in getEventsPaginatedDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getEventsPaginatedDomain: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getEventsPaginatedDomain: ${e.message}" }
            Result.failure(e)
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
            log.e(e) { "API error fetching event types: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error fetching event types: ${e.message}" }
            Result.failure(e)
        }
    }
}
