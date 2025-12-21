package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpaceStationDetailedEndpointList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint

/**
 * Extension functions for SpaceStationsApi to provide cleaner interfaces
 * following the pattern established in LaunchesApiExtensions.kt
 */

/**
 * Get detailed information for a specific space station
 * 
 * @param id Space station ID (e.g., 4 for ISS)
 * @return HttpResponse containing SpaceStationDetailedEndpoint
 */
suspend fun SpaceStationsApi.getSpaceStationDetailed(
    id: Int
): HttpResponse<SpaceStationDetailedEndpoint> = spaceStationsRetrieve(
    id = id
)

/**
 * Get list of space stations with filtering
 * 
 * @param limit Number of results per page
 * @param offset Pagination offset
 * @param search Search term for name
 * @param status Filter by status ID
 * @param name Filter by exact name
 * @param ordering Sort field (e.g., "name", "-name")
 * @return HttpResponse containing PaginatedSpaceStationDetailedEndpointList
 */
suspend fun SpaceStationsApi.getSpaceStationDetailedList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    status: Int? = null,
    name: String? = null,
    ordering: String? = null
): HttpResponse<PaginatedSpaceStationDetailedEndpointList> = spaceStationsDetailedList(
    limit = limit,
    offset = offset,
    search = search,
    status = status,
    name = name,
    ordering = ordering
)
