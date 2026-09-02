package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.AstronautsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautDetail
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAstronautList

/**
 * Extension functions for the Trantor [AstronautsApi], mirroring the naming of the
 * retired Launch Library `AstronautsApiExtensions.kt` so repository call sites read the same.
 */

/**
 * Get a paginated list of astronauts with the filters Trantor's `/api/v1/astronauts` supports.
 */
suspend fun AstronautsApi.getAstronautList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    statusIds: List<Int>? = null,
    agencyIds: List<Int>? = null,
    ordering: String? = null,
    hasFlown: Boolean? = null,
    inSpace: Boolean? = null,
    isHuman: Boolean? = null
): HttpResponse<PaginatedResponseAstronautList> = listAstronautsApiV1AstronautsGet(
    statusIds = statusIds?.joinToString(","),
    agencyIds = agencyIds?.joinToString(","),
    hasFlown = hasFlown,
    inSpace = inSpace,
    isHuman = isHuman,
    search = search,
    ordering = ordering,
    limit = limit,
    offset = offset
)

/**
 * Get detailed information about a specific astronaut by their ID.
 */
suspend fun AstronautsApi.getAstronautDetail(
    id: Int
): HttpResponse<AstronautDetail> = getAstronautApiV1AstronautsAstronautIdGet(astronautId = id)
