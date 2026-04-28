package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationNormal
import me.calebjones.spacelaunchnow.domain.model.AstronautSummary
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.ExpeditionSummary
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpaceStationSummary
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
