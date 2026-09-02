package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLocationList

/**
 * Extension functions for the Trantor LocationsApi to provide cleaner, named-parameter
 * call sites without exposing the generated method's full parameter list.
 */

/**
 * Get a list of locations with clean named parameters.
 */
suspend fun LocationsApi.getLocationList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null,
    active: Boolean? = null
): HttpResponse<PaginatedResponseLocationList> {
    return listLocationsApiV1LocationsGet(
        active = active,
        search = search,
        ordering = ordering,
        limit = limit,
        offset = offset
    )
}
