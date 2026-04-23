package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftEndpointDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig
import me.calebjones.spacelaunchnow.domain.model.SpacecraftStatus

fun SpacecraftConfigDetailed.toDomain(): SpacecraftConfig = SpacecraftConfig(
    id = id,
    name = name,
    type = type.name,
    agency = agency.toDomain(),
    imageUrl = image?.imageUrl,
    inUse = inUse,
    capability = capability,
    history = history,
    details = details,
    maidenFlight = maidenFlight,
    humanRated = humanRated,
    crewCapacity = crewCapacity,
    payloadCapacity = payloadCapacity
)

fun SpacecraftEndpointDetailed.toDomain(): Spacecraft = Spacecraft(
    id = id,
    name = name,
    serialNumber = serialNumber,
    status = status?.let { SpacecraftStatus(id = it.id, name = it.name) },
    description = description,
    imageUrl = image?.imageUrl,
    config = spacecraftConfig.toDomain()
)

fun PaginatedSpacecraftEndpointDetailedList.toDomain(): PaginatedResult<Spacecraft> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

fun PaginatedSpacecraftConfigDetailedList.toDomain(): PaginatedResult<SpacecraftConfig> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)
