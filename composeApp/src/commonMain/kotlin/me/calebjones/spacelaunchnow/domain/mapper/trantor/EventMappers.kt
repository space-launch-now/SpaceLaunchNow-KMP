package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.EventDetail
import me.calebjones.spacelaunchnow.api.trantor.models.EventInfoUrl
import me.calebjones.spacelaunchnow.api.trantor.models.EventList
import me.calebjones.spacelaunchnow.api.trantor.models.EventVidUrl
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseEventList
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.InfoLink
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.domain.model.EventType as DomainEventType

// --- Trantor mappers ---
//
// Trantor's event list/detail rows only ever carry launch_ids/program_ids (not the nested
// LaunchNormal/ProgramNormal objects LL's EventEndpointDetailed embedded), and detail carries
// no agencies/astronauts/spacestations/expeditions at all. Rather than fabricate those
// objects from bare ids, the detail-only fields below stay at their empty-list defaults —
// same as when mapping from EventEndpointNormal today. See Phase 5 events-updates unit report
// for the escalation on restoring this data (either a Trantor contract addition or client-side
// composition with LaunchesApi/ProgramsApi).

fun EventList.toDomain(): Event = Event(
    id = id,
    name = name,
    slug = slug ?: "",
    type = DomainEventType(id = typeId ?: 0, name = type),
    description = null,
    date = date,
    location = location,
    imageUrl = imageUrl,
    webcastLive = webcastLive ?: false,
    lastUpdated = null,
    duration = null,
    datePrecision = null,
    infoUrls = emptyList(),
    vidUrls = emptyList(),
    updates = emptyList()
)

fun EventDetail.toDomain(): Event = Event(
    id = id,
    name = name,
    slug = slug ?: "",
    type = DomainEventType(id = typeId ?: 0, name = type),
    description = description,
    date = date,
    location = location,
    imageUrl = imageUrl,
    webcastLive = webcastLive ?: false,
    lastUpdated = null,
    duration = null,
    datePrecision = null,
    infoUrls = infoUrls?.map { it.toDomain() } ?: emptyList(),
    vidUrls = vidUrls?.map { it.toDomain() } ?: emptyList(),
    updates = emptyList()
)

fun EventInfoUrl.toDomain(): InfoLink = InfoLink(
    url = url,
    title = title,
    source = source,
    description = description,
    featureImage = featureImage,
    type = type,
    priority = priority
)

fun EventVidUrl.toDomain(): VideoLink = VideoLink(
    url = url,
    title = title,
    source = source,
    publisher = publisher,
    description = description,
    featureImage = featureImage,
    live = live,
    priority = priority
)

fun PaginatedResponseEventList.toDomain(): PaginatedResult<Event> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)
