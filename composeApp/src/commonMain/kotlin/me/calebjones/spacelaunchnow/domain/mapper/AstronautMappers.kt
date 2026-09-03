package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautFlight as LLCrewFlight
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacewalkList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacewalkNormal
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautDetail as TrantorAstronautDetail
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautFlight as TrantorAstronautFlight
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautList as TrantorAstronautList
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAstronautList as TrantorPaginatedAstronautList
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautFlight
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.CrewMember
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SocialMediaLink
import me.calebjones.spacelaunchnow.domain.model.SpacecraftFlightSummary
import me.calebjones.spacelaunchnow.domain.model.SpacewalkSummary

fun AstronautEndpointNormal.toDomainListItem(): AstronautListItem = AstronautListItem(
    id = id,
    name = name ?: "Unknown",
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
    name = name ?: "Unknown",
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
    flights = flights.map { it.toDomainAstronautFlight() },
    landings = landings.map { it.toDomainSummary() },
    spacewalks = spacewalks.map { it.toDomainSummary() }
)

fun LLCrewFlight.toDomainCrewMember(): CrewMember = CrewMember(
    id = id,
    role = role?.role,
    astronaut = astronaut.toDomainListItem()
)

fun LaunchBasic.toDomainAstronautFlight(): AstronautFlight = AstronautFlight(
    launchId = id,
    launchName = name ?: "",
    net = net
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

// ==================== Trantor overloads (phase5-browse-space migration) ====================
//
// Escalations (fields the Trantor `/api/v1/astronauts` contract does not expose, left
// absent rather than fabricated per the fail-closed rule for this unit):
// - `agencyAbbrev`, `age` (list row), `typeName` (list row): not present on Trantor's
//   `AstronautList` row (only on `AstronautDetail`, and even there there's no abbrev).
// - `thumbnailUrl`: Trantor exposes a single `image_url`; reused for both fields since
//   there is no separate thumbnail variant.
// - `nationality`: Trantor gives plain country-name strings; the domain `Country` type
//   requires a real non-null `id` we don't have, so this maps to an empty list rather
//   than inventing ids.
// - `socialMediaLinks`, `landings`, `spacewalks`: not present on Trantor astronaut detail.
// - `flights`: now maps to the narrow domain `AstronautFlight` ref type (launch id/name/net
//   only), which is exactly what Trantor's embedded `AstronautFlight` carries — see
//   `toDomainFlight()` below.

fun TrantorAstronautList.toDomainListItem(): AstronautListItem = AstronautListItem(
    id = id,
    name = name,
    statusName = status,
    statusId = statusId,
    agencyName = agencyName,
    agencyAbbrev = null,
    agencyId = agencyId,
    imageUrl = imageUrl,
    thumbnailUrl = imageUrl,
    age = null,
    bio = null,
    typeName = null,
    nationality = emptyList()
)

fun TrantorPaginatedAstronautList.toDomain(): PaginatedResult<AstronautListItem> =
    PaginatedResult(
        count = count,
        next = next,
        previous = previous,
        results = results.map { it.toDomainListItem() }
    )

fun TrantorAstronautDetail.toDomainDetail(): AstronautDetail = AstronautDetail(
    id = id,
    name = name,
    statusName = status,
    statusId = statusId,
    agencyName = agencyName,
    agencyAbbrev = null,
    agencyId = agencyId,
    imageUrl = imageUrl,
    thumbnailUrl = imageUrl,
    age = age,
    bio = bio,
    typeName = type,
    nationality = emptyList(),
    inSpace = inSpace,
    timeInSpace = timeInSpace,
    evaTime = evaTime,
    dateOfBirth = dateOfBirth,
    dateOfDeath = dateOfDeath,
    wikiUrl = wiki,
    lastFlight = lastFlight,
    firstFlight = firstFlight,
    socialMediaLinks = emptyList(),
    flightsCount = flightsCount,
    landingsCount = landingsCount,
    spacewalksCount = spacewalksCount,
    flights = flights.orEmpty().map { it.toDomainFlight() },
    landings = emptyList(),
    spacewalks = emptyList()
)

fun TrantorAstronautFlight.toDomainFlight(): AstronautFlight = AstronautFlight(
    launchId = launchId,
    launchName = launchName,
    net = net
)
