package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAgencyList

/**
 * Extension functions for the Trantor AgenciesApi to provide cleaner, named-parameter
 * call sites without exposing the generated method's full parameter list.
 */

/**
 * Get a list of agencies with clean named parameters.
 */
suspend fun AgenciesApi.getAgencyList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null,
    featured: Boolean? = null,
    typeIds: String? = null,
    countryCode: String? = null
): HttpResponse<PaginatedResponseAgencyList> {
    return listAgenciesApiV1AgenciesGet(
        search = search,
        featured = featured,
        typeIds = typeIds,
        countryCode = countryCode,
        ordering = ordering,
        limit = limit,
        offset = offset
    )
}
