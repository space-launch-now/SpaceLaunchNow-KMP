package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautFlight
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacewalkList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacewalkNormal
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.CrewMember
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SocialMediaLink
import me.calebjones.spacelaunchnow.domain.model.SpacecraftFlightSummary
import me.calebjones.spacelaunchnow.domain.model.SpacewalkSummary

fun AstronautEndpointNormal.toDomainListItem(): AstronautListItem = AstronautListItem(
    id = id,
    name = name,
    statusName = status?.name,
    statusId = status?.id,
    agencyName = agency?.name,
    agencyAbbrev = agency?.abbrev,
    agencyId = agency?.id,
    imageUrl = image?.imageUrl,
    thumbnailUrl = image?.thumbnailUrl,
    age = age,
    bio = bio,
    typeName = type.name,
    nationality = nationality.map { it.toDomain() }
)

fun AstronautNormal.toDomainListItem(): AstronautListItem = AstronautListItem(
    id = id,
    name = name,
    statusName = status?.name,
    statusId = status?.id,
    agencyName = agency?.name,
    agencyAbbrev = agency?.abbrev,
    agencyId = agency?.id,
    imageUrl = image?.imageUrl,
    thumbnailUrl = image?.thumbnailUrl,
    age = age,
    bio = bio,
    typeName = type.name,
    nationality = nationality.map { it.toDomain() }
)

fun AstronautEndpointDetailed.toDomainDetail(): AstronautDetail = AstronautDetail(
    id = id,
    name = name,
    statusName = status?.name,
    statusId = status?.id,
    agencyName = agency?.name,
    agencyAbbrev = agency?.abbrev,
    agencyId = agency?.id,
    imageUrl = image?.imageUrl,
    thumbnailUrl = image?.thumbnailUrl,
    age = age,
    bio = bio,
    typeName = type.name,
    nationality = nationality.map { it.toDomain() },
    inSpace = inSpace,
    timeInSpace = timeInSpace,
    evaTime = evaTime,
    dateOfBirth = dateOfBirth,
    dateOfDeath = dateOfDeath,
    wikiUrl = wiki,
    lastFlight = lastFlight,
    firstFlight = firstFlight,
    socialMediaLinks = socialMediaLinks?.map { link ->
        SocialMediaLink(
            id = link.id,
            url = link.url,
            platformName = link.socialMedia.name,
            platformLogoUrl = link.socialMedia.logo?.thumbnailUrl ?: link.socialMedia.logo?.imageUrl
        )
    } ?: emptyList(),
    flightsCount = flightsCount,
    landingsCount = landingsCount,
    spacewalksCount = spacewalksCount,
    flights = flights.map { it.toDomain() },
    landings = landings.map { it.toDomainSummary() },
    spacewalks = spacewalks.map { it.toDomainSummary() }
)

fun AstronautFlight.toDomainCrewMember(): CrewMember = CrewMember(
    id = id,
    role = role?.role,
    astronaut = astronaut.toDomainListItem()
)

fun SpacecraftFlightNormal.toDomainSummary(): SpacecraftFlightSummary = SpacecraftFlightSummary(
    id = id,
    serialNumber = spacecraft.serialNumber,
    spacecraftName = spacecraft.name,
    destination = destination,
    missionEnd = missionEnd
)

fun SpacewalkNormal.toDomainSummary(): SpacewalkSummary = SpacewalkSummary(
    id = id,
    name = name,
    start = start,
    end = end,
    duration = duration
)

fun SpacewalkList.toDomainSummary(): SpacewalkSummary = SpacewalkSummary(
    id = id,
    name = name,
    start = start,
    end = end,
    duration = duration
)

fun PaginatedAstronautEndpointNormalList.toDomain(): PaginatedResult<AstronautListItem> =
    PaginatedResult(
        count = count,
        next = next,
        previous = previous,
        results = results.map { it.toDomainListItem() }
    )
