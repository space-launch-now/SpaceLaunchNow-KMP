package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLocationSerializerWithPadsList

/**
 * Extension functions for LocationsApi to provide cleaner, named-parameter interfaces
 * for common use cases without exposing all generated parameters.
 */

/**
 * Get a list of locations with clean named parameters
 */
suspend fun LocationsApi.getLocationList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null,
    active: Boolean? = null,
    countryCode: String? = null,
    name: String? = null,
    nameContains: String? = null
): HttpResponse<PaginatedLocationSerializerWithPadsList> = locationsDetailedList(
    active = active,
    countryCode = countryCode,
    id = null,
    limit = limit,
    name = name,
    nameContains = nameContains,
    offset = offset,
    ordering = ordering,
    search = search,
    totalLandingCount = null,
    totalLandingCountGt = null,
    totalLandingCountGte = null,
    totalLandingCountLt = null,
    totalLandingCountLte = null,
    totalLaunchCount = null,
    totalLaunchCountGt = null,
    totalLaunchCountGte = null,
    totalLaunchCountLt = null,
    totalLaunchCountLte = null
)
