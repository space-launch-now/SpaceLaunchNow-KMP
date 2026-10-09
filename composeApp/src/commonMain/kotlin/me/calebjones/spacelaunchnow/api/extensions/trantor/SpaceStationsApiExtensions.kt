package me.calebjones.spacelaunchnow.api.extensions.trantor

import me.calebjones.spacelaunchnow.api.trantor.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseStationList
import me.calebjones.spacelaunchnow.api.trantor.models.StationDetail

/**
 * Extension functions for the Trantor [SpaceStationsApi]. There is no standalone
 * `/expeditions` endpoint on Trantor — station detail embeds `expeditions[]` with crew.
 */

/**
 * Get detailed information for a specific space station, including embedded expeditions.
 */
suspend fun SpaceStationsApi.getSpaceStationDetailed(
    id: Int
): HttpResponse<StationDetail> = getStationApiV1SpaceStationsStationIdGet(stationId = id)

/**
 * Get list of space stations with filtering.
 */
suspend fun SpaceStationsApi.getSpaceStationDetailedList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    statusId: Int? = null,
    ordering: String? = null
): HttpResponse<PaginatedResponseStationList> = listStationsApiV1SpaceStationsGet(
    statusId = statusId,
    search = search,
    ordering = ordering,
    limit = limit,
    offset = offset
)
