package me.calebjones.spacelaunchnow.api.extensions.trantor

import me.calebjones.spacelaunchnow.api.trantor.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseProgramList
import me.calebjones.spacelaunchnow.api.trantor.models.ProgramDetail

/**
 * Extension functions for the Trantor [ProgramsApi].
 */

/**
 * Get a list of programs with clean named parameters.
 */
suspend fun ProgramsApi.getProgramList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null
): HttpResponse<PaginatedResponseProgramList> = listProgramsApiV1ProgramsGet(
    search = search,
    ordering = ordering,
    limit = limit,
    offset = offset
)

/**
 * Get detailed program information by ID.
 */
suspend fun ProgramsApi.getProgramDetails(
    id: Int
): HttpResponse<ProgramDetail> = getProgramApiV1ProgramsProgramIdGet(programId = id)
