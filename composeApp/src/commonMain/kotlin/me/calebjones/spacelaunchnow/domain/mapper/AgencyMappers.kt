package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyEndpointDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * Map [AgencyEndpointDetailed] to the unified [Agency] domain type, populating all available
 * detail fields. The simpler `AgencyNormal.toDomainAgency()` mapper lives in
 * [SpaceStationMappers].
 */
fun AgencyEndpointDetailed.toDomainAgency(): Agency = Agency(
    id = id,
    name = name,
    abbrev = abbrev,
    typeName = type?.name,
    countries = country.map { it.toDomain() },
    imageUrl = image?.imageUrl,
    logoUrl = logo?.imageUrl,
    socialLogoUrl = socialLogo?.imageUrl,
    description = description,
    administrator = administrator,
    foundingYear = foundingYear,
    featured = featured,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    launchersDescription = launchers,
    spacecraftDescription = spacecraft,
    totalLaunchCount = totalLaunchCount,
    consecutiveSuccessfulLaunches = consecutiveSuccessfulLaunches,
    successfulLaunches = successfulLaunches,
    failedLaunches = failedLaunches,
    pendingLaunches = pendingLaunches,
    attemptedLandings = attemptedLandings,
    successfulLandings = successfulLandings,
    failedLandings = failedLandings,
    consecutiveSuccessfulLandings = consecutiveSuccessfulLandings,
    attemptedLandingsSpacecraft = attemptedLandingsSpacecraft,
    successfulLandingsSpacecraft = successfulLandingsSpacecraft,
    failedLandingsSpacecraft = failedLandingsSpacecraft
)

fun PaginatedAgencyNormalList.toDomain(): PaginatedResult<Agency> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomainAgency() }
)

fun PaginatedAgencyEndpointDetailedList.toDomain(): PaginatedResult<Agency> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomainAgency() }
)
