package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationNormal
import me.calebjones.spacelaunchnow.api.trantor.models.EventDetail
import me.calebjones.spacelaunchnow.api.trantor.models.EventInfoUrl
import me.calebjones.spacelaunchnow.api.trantor.models.EventList
import me.calebjones.spacelaunchnow.api.trantor.models.EventVidUrl
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseEventList
import me.calebjones.spacelaunchnow.domain.model.AstronautSummary
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.ExpeditionSummary
import me.calebjones.spacelaunchnow.domain.model.InfoLink
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpaceStationSummary
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.domain.model.EventType as DomainEventType

fun EventEndpointNormal.toDomain(): Event = Event(
    id = id,
    name = name,
    slug = slug,
    type = type.toDomain(),
    description = description,
    date = date,
    location = location,
    imageUrl = image?.imageUrl,
    webcastLive = webcastLive ?: false,
    lastUpdated = lastUpdated,
    duration = duration,
    datePrecision = datePrecision?.toDomain(),
    infoUrls = infoUrls.map { it.toDomain() },
    vidUrls = vidUrls.map { it.toDomain() },
    updates = updates.map { it.toDomain() }
)

fun EventEndpointDetailed.toDomain(): Event = Event(
    id = id,
    name = name,
    slug = slug,
    type = type.toDomain(),
    description = description,
    date = date,
    location = location,
    imageUrl = image?.imageUrl,
    webcastLive = webcastLive ?: false,
    lastUpdated = lastUpdated,
    duration = duration,
    datePrecision = datePrecision?.toDomain(),
    infoUrls = infoUrls.map { it.toDomain() },
    vidUrls = vidUrls.map { it.toDomain() },
    updates = updates.map { it.toDomain() },
    agencies = agencies.map { it.toDomain() },
    launches = launches.map { it.toDomain() },
    expeditions = expeditions.map { it.toDomain() },
    spaceStations = spacestations.map { it.toDomain() },
    programs = program?.map { it.toDomain() } ?: emptyList(),
    astronauts = astronauts?.map { it.toDomain() } ?: emptyList()
)

fun me.calebjones.spacelaunchnow.api.launchlibrary.models.EventType.toDomain(): DomainEventType =
    DomainEventType(
        id = id,
        name = name
    )

fun ExpeditionNormal.toDomain(): ExpeditionSummary = ExpeditionSummary(
    id = id,
    name = name,
    start = start,
    end = end,
    imageUrl = spacestation.image?.imageUrl
)

fun SpaceStationNormal.toDomain(): SpaceStationSummary = SpaceStationSummary(
    id = id,
    name = name,
    imageUrl = image?.imageUrl,
    orbit = orbit
)

fun AstronautNormal.toDomain(): AstronautSummary = AstronautSummary(
    id = id,
    name = name ?: "",
    nationality = nationality.firstOrNull()?.nationalityName,
    profileImageUrl = image?.imageUrl,
    status = status?.name
)

fun PaginatedEventEndpointNormalList.toDomain(): PaginatedResult<Event> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

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
