package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherDetailedList

/**
 * Extension functions for LaunchersApi to provide clean, named-parameter interfaces
 * instead of dealing with many generated parameters
 */

/**
 * Get launchers (boosters/first stages) with basic filters and pagination
 * 
 * For Starship dashboard, use search = "Super Heavy" or "Booster" to filter for boosters
 */
suspend fun LaunchersApi.getLaunchers(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    ordering: String? = null,
    launcherConfigIds: List<Int>? = null,
    flightProven: Boolean? = null,
    isPlaceholder: Boolean? = null
): HttpResponse<PaginatedLauncherDetailedList> = launchersDetailedList(
    limit = limit,
    offset = offset,
    search = search,
    ordering = ordering,
    launcherConfigIds = launcherConfigIds,
    flightProven = flightProven,
    isPlaceholder = isPlaceholder,
    // All the other parameters we don't need right now
    attemptedLandings = null,
    attemptedLandingsGt = null,
    attemptedLandingsGte = null,
    attemptedLandingsLt = null,
    attemptedLandingsLte = null,
    firstLaunchDate = null,
    firstLaunchDateDay = null,
    firstLaunchDateMonth = null,
    firstLaunchDateYear = null,
    flights = null,
    flightsGt = null,
    flightsGte = null,
    flightsLt = null,
    flightsLte = null,
    id = null,
    idContains = null,
    lastLaunchDate = null,
    launcherConfigManufacturerName = null,
    launcherConfigManufacturerNameContains = null,
    serialNumber = null,
    serialNumberContains = null,
    status = null,
    successfulLandings = null,
    successfulLandingsGt = null,
    successfulLandingsGte = null,
    successfulLandingsLt = null,
    successfulLandingsLte = null
)
