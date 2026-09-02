package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseProgramList

/**
 * Extension functions for the Trantor ProgramsApi to provide cleaner, named-parameter
 * call sites without exposing the generated method's full parameter list.
 */

/**
 * Get a list of programs with clean named parameters.
 */
suspend fun ProgramsApi.getProgramList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null
): HttpResponse<PaginatedResponseProgramList> {
    return listProgramsApiV1ProgramsGet(
        search = search,
        ordering = ordering,
        limit = limit,
        offset = offset
    )
}
