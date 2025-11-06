package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList

/**
 * Extension functions for LauncherConfigurationsApi to provide cleaner, named-parameter interfaces
 * instead of the verbose generated API methods
 */

/**
 * Get launcher configuration list with commonly used parameters
 */
suspend fun LauncherConfigurationsApi.getRocketList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    ordering: String? = null,
    active: Boolean? = null,
    reusable: Boolean? = null,
    manufacturerName: String? = null,
    manufacturerNameContains: String? = null
): HttpResponse<PaginatedLauncherConfigNormalList> = launcherConfigurationsList(
    active = active,
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
    families = null,
    familiesContains = null,
    fullName = null,
    fullNameContains = null,
    isPlaceholder = null,
    limit = limit,
    maidenFlight = null,
    maidenFlightDay = null,
    maidenFlightGt = null,
    maidenFlightGte = null,
    maidenFlightLt = null,
    maidenFlightLte = null,
    maidenFlightMonth = null,
    maidenFlightYear = null,
    manufacturerName = manufacturerName,
    manufacturerNameContains = manufacturerNameContains,
    name = null,
    nameContains = null,
    offset = offset,
    ordering = ordering,
    pendingLaunches = null,
    pendingLaunchesGt = null,
    pendingLaunchesGte = null,
    pendingLaunchesLt = null,
    pendingLaunchesLte = null,
    program = null,
    programContains = null,
    reusable = reusable,
    search = search,
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
    totalLaunchCountLte = null
)

/**
 * Get detailed rocket configuration by ID
 */
suspend fun LauncherConfigurationsApi.getRocketDetails(
    id: Int
): HttpResponse<LauncherConfigDetailed> = launcherConfigurationsRetrieve(id = id)
