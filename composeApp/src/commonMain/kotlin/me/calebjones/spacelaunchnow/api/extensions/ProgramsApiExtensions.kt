package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedProgramNormalList

/**
 * Extension functions for ProgramsApi to provide cleaner, named-parameter interfaces
 * for common use cases without exposing the 70+ generated parameters.
 */

/**
 * Get a list of programs with clean named parameters
 */
suspend fun ProgramsApi.getProgramList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null
): HttpResponse<PaginatedProgramNormalList> {
    return programsList(
        limit = limit,
        offset = offset,
        ordering = ordering,
        search = search
    )
}
