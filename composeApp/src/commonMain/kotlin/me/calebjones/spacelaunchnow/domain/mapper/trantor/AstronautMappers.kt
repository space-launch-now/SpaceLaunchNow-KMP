package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AstronautDetail as TrantorAstronautDetail
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautFlight as TrantorAstronautFlight
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautList as TrantorAstronautList
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAstronautList as TrantorPaginatedAstronautList
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautFlight
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

// ==================== Trantor overloads (phase5-browse-space migration) ====================
//
// Fields the Trantor `/api/v1/astronauts` contract does not expose, left absent rather
// than fabricated per the fail-closed rule for this unit:
// - `age` (list row), `typeName` (list row): not present on Trantor's `AstronautList` row
//   (only on `AstronautDetail`).
// - `thumbnailUrl`: Trantor exposes a single `image_url`; reused for both fields since
//   there is no separate thumbnail variant.
// - `nationality`: built from `nationality_codes` (ISO alpha-2) via `toDomainCountry()`,
//   which resolves the display name and flag. Empty when the server sends no codes.
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
    agencyAbbrev = agencyAbbrev,
    agencyId = agencyId,
    imageUrl = imageUrl,
    thumbnailUrl = imageUrl,
    age = null,
    bio = null,
    typeName = null,
    nationality = nationalityCodes?.map { it.toDomainCountry() } ?: emptyList()
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
    agencyAbbrev = agencyAbbrev,
    agencyId = agencyId,
    imageUrl = imageUrl,
    thumbnailUrl = imageUrl,
    age = age,
    bio = bio,
    typeName = type,
    nationality = nationalityCodes?.map { it.toDomainCountry() } ?: emptyList(),
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
