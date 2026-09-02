package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.FamiliesApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseFamilyList

/**
 * Extension functions for the Trantor FamiliesApi to provide cleaner, named-parameter
 * call sites without exposing the generated method's full parameter list.
 */

/**
 * Get a list of launcher-config families with clean named parameters.
 */
suspend fun FamiliesApi.getFamilyList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null
): HttpResponse<PaginatedResponseFamilyList> {
    return listFamiliesApiV1FamiliesGet(
        search = search,
        ordering = ordering,
        limit = limit,
        offset = offset
    )
}
