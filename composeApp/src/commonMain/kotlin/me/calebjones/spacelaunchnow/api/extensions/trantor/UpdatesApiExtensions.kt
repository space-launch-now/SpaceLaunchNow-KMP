package me.calebjones.spacelaunchnow.api.extensions.trantor

import me.calebjones.spacelaunchnow.api.trantor.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseUpdateList

/**
 * Extension functions for the Trantor UpdatesApi to provide clean, named-parameter interfaces
 * mirroring the retired LL UpdatesApiExtensions' call-site ergonomics.
 */

suspend fun UpdatesApi.getUpdates(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    launchId: String? = null,
    programIds: List<Int>? = null
): HttpResponse<PaginatedResponseUpdateList> = listUpdatesApiV1UpdatesGet(
    programIds = programIds?.joinToString(","),
    launchId = launchId,
    ordering = ordering,
    limit = limit,
    offset = offset
)

/**
 * Get latest updates ordered by creation date
 */
suspend fun UpdatesApi.getLatestUpdates(
    limit: Int = 10
): HttpResponse<PaginatedResponseUpdateList> = getUpdates(
    limit = limit,
    ordering = "-created_on"
)

/**
 * Get updates reachable from a program (Trantor's program_ids already carries the LL
 * all__program reachability semantics through launch/event junctions — one param).
 */
suspend fun UpdatesApi.getUpdatesByProgram(
    programId: Int,
    limit: Int? = null
): HttpResponse<PaginatedResponseUpdateList> = getUpdates(
    limit = limit,
    programIds = listOf(programId),
    ordering = "-created_on"
)
