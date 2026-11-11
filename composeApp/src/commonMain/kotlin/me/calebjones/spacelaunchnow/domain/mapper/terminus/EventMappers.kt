package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.encodeToString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed as LLEventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList as LLPaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventTypeList as LLPaginatedEventTypeList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList as LLPaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.api.terminus.models.EventEndpointDetailed as TerminusEventEndpointDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedEventEndpointNormalList as TerminusPaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedEventTypeList as TerminusPaginatedEventTypeList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedUpdateEndpointList as TerminusPaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.EventType
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Update

// Terminus serves the LL2 shapes for events, event types and updates (it adds `phase` to events,
// which the LL model does not read and the bridge drops). Re-reading the Terminus model as the LL
// model reuses the one tested LL -> domain mapping, as the launch mappers do. The one domain
// change is on Update: `launch` becomes a LaunchRef (id and name), done in the shared mapper.

/** Terminus `GET /events/` page as the LL page the [me.calebjones.spacelaunchnow.data.repository.EventsRepository] interface returns. */
fun TerminusPaginatedEventEndpointNormalList.toLL(): LLPaginatedEventEndpointNormalList =
    bridgeJson.decodeFromString<LLPaginatedEventEndpointNormalList>(bridgeJson.encodeToString(this))

fun TerminusPaginatedEventEndpointNormalList.toDomain(): PaginatedResult<Event> = toLL().toDomain()

/** Terminus `GET /events/{id}/` to the domain event. */
fun TerminusEventEndpointDetailed.toDomain(): Event =
    bridgeJson
        .decodeFromString<LLEventEndpointDetailed>(bridgeJson.encodeToString(this))
        .toDomain()

/** Terminus `GET /config/event-types/` page to the domain event types. */
fun TerminusPaginatedEventTypeList.toDomain(): List<EventType> =
    bridgeJson
        .decodeFromString<LLPaginatedEventTypeList>(bridgeJson.encodeToString(this))
        .results.map { it.toDomain() }

/** Terminus `GET /updates/` page to the domain page. `launch` becomes a LaunchRef, `created_by` stays text. */
fun TerminusPaginatedUpdateEndpointList.toDomain(): PaginatedResult<Update> {
    val page = bridgeJson.decodeFromString<LLPaginatedUpdateEndpointList>(bridgeJson.encodeToString(this))
    return PaginatedResult(
        count = page.count,
        next = page.next,
        previous = page.previous,
        results = page.results.map { it.toDomain() }
    )
}
