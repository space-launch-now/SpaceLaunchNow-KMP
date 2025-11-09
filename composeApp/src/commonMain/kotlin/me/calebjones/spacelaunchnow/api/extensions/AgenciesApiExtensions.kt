package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList

/**
 * Extension functions for AgenciesApi to provide cleaner, named-parameter interfaces
 * instead of the verbose generated API methods
 */

/**
 * Get agency list with commonly used parameters
 */
suspend fun AgenciesApi.getAgencyList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    ordering: String? = null,
    featured: Boolean? = null,
    typeId: Int? = null,
    countryCode: List<String>? = null,
    name: String? = null,
    nameContains: String? = null
): HttpResponse<PaginatedAgencyNormalList> = agenciesList(
    abbrev = null,
    abbrevContains = null,
    attemptedLandings = null,
    attemptedLandingsGt = null,
    attemptedLandingsGte = null,
    attemptedLandingsLt = null,
    attemptedLandingsLte = null,
    consecutiveSuccessfulLandings = null,
    consecutiveSuccessfulLandingsGt = null,
    consecutiveSuccessfulLandingsGte = null,
    consecutiveSuccessfulLandingsLt = null,
    consecutiveSuccessfulLandingsLte = null,
    consecutiveSuccessfulLaunches = null,
    consecutiveSuccessfulLaunchesGt = null,
    consecutiveSuccessfulLaunchesGte = null,
    consecutiveSuccessfulLaunchesLt = null,
    consecutiveSuccessfulLaunchesLte = null,
    countryCode = countryCode,
    description = null,
    descriptionContains = null,
    failedLandings = null,
    failedLandingsGt = null,
    failedLandingsGte = null,
    failedLandingsLt = null,
    failedLandingsLte = null,
    failedLaunches = null,
    failedLaunchesGt = null,
    failedLaunchesGte = null,
    failedLaunchesLt = null,
    failedLaunchesLte = null,
    featured = featured,
    foundingYear = null,
    foundingYearGt = null,
    foundingYearGte = null,
    foundingYearLt = null,
    foundingYearLte = null,
    id = null,
    limit = limit,
    name = name,
    nameContains = nameContains,
    offset = offset,
    ordering = ordering,
    parentId = null,
    pendingLaunches = null,
    pendingLaunchesGt = null,
    pendingLaunchesGte = null,
    pendingLaunchesLt = null,
    pendingLaunchesLte = null,
    search = search,
    spacecraft = null,
    successfulLandings = null,
    successfulLandingsGt = null,
    successfulLandingsGte = null,
    successfulLandingsLt = null,
    successfulLandingsLte = null,
    successfulLaunches = null,
    successfulLaunchesGt = null,
    successfulLaunchesGte = null,
    successfulLaunchesLt = null,
    successfulLaunchesLte = null,
    totalLaunchCount = null,
    totalLaunchCountGt = null,
    totalLaunchCountGte = null,
    totalLaunchCountLt = null,
    totalLaunchCountLte = null,
    typeId = typeId
)
