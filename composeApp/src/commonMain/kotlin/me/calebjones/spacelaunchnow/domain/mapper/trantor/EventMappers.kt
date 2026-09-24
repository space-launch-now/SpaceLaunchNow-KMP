package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AstronautList
import me.calebjones.spacelaunchnow.api.trantor.models.EventDetail
import me.calebjones.spacelaunchnow.api.trantor.models.EventInfoUrl
import me.calebjones.spacelaunchnow.api.trantor.models.EventList
import me.calebjones.spacelaunchnow.api.trantor.models.EventVidUrl
import me.calebjones.spacelaunchnow.api.trantor.models.Expedition
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseEventList
import me.calebjones.spacelaunchnow.api.trantor.models.ProgramList
import me.calebjones.spacelaunchnow.api.trantor.models.StationList
import me.calebjones.spacelaunchnow.domain.model.AstronautSummary
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.ExpeditionSummary
import me.calebjones.spacelaunchnow.domain.model.InfoLink
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.ProgramSummary
import me.calebjones.spacelaunchnow.domain.model.SpaceStationSummary
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.domain.model.EventType as DomainEventType

// --- Trantor mappers ---
//
// Trantor's event list row only ever carries launch_ids/program_ids (no nested objects,
// matching LL's EventEndpointNormal). The detail embeds one level of each related resource's
// own list row (ruling E7(P5)): launches[], programs[], space_stations[], agencies[],
// astronauts[], and expeditions[] (the space-station detail's own expedition shape). The
// agencies/astronauts/expeditions junctions are a Phase-1 sync watermark target, so they may
// still read back empty on an environment that hasn't been backfilled yet — that is a sync
// state, not a mapping gap, and is never faked here.

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
    updates = emptyList(),
    agencies = agencies?.map { it.toDomain() } ?: emptyList(),
    launches = launches?.map { it.toDomain() } ?: emptyList(),
    expeditions = expeditions?.map { it.toDomainSummary() } ?: emptyList(),
    spaceStations = spaceStations?.map { it.toDomainSummary() } ?: emptyList(),
    programs = programs?.map { it.toDomainSummary() } ?: emptyList(),
    astronauts = astronauts?.map { it.toDomainSummary() } ?: emptyList()
)

fun StationList.toDomainSummary(): SpaceStationSummary = SpaceStationSummary(
    id = id,
    name = name,
    imageUrl = imageUrl,
    orbit = orbit
)

fun ProgramList.toDomainSummary(): ProgramSummary = ProgramSummary(
    id = id,
    name = name,
    imageUrl = imageUrl,
    description = null,
    infoUrl = null,
    wikiUrl = null,
    type = null
)

fun AstronautList.toDomainSummary(): AstronautSummary = AstronautSummary(
    id = id,
    name = name,
    nationality = nationality?.firstOrNull(),
    profileImageUrl = imageUrl,
    status = status
)

fun Expedition.toDomainSummary(): ExpeditionSummary = ExpeditionSummary(
    id = id,
    name = name,
    start = start,
    end = end,
    imageUrl = null
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
