package me.calebjones.spacelaunchnow.api.extensions.trantor

import me.calebjones.spacelaunchnow.api.trantor.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseSpacecraftSummary
import me.calebjones.spacelaunchnow.api.trantor.models.SpacecraftFull

/**
 * Extension functions for the Trantor [SpacecraftApi]. Filter names follow the phase5
 * contract rename: `spacecraft_config` (LL) -> `config_id` (Trantor).
 */

/**
 * Get spacecraft filtered by configuration ID.
 */
suspend fun SpacecraftApi.getSpacecraftByConfig(
    configId: Int,
    limit: Int? = null,
    offset: Int? = null,
    inSpace: Boolean? = null,
    isPlaceholder: Boolean? = null,
    search: String? = null
): HttpResponse<PaginatedResponseSpacecraftSummary> = listSpacecraftApiV1SpacecraftGet(
    configId = configId,
    inSpace = inSpace,
    isPlaceholder = isPlaceholder,
    search = search,
    limit = limit,
    offset = offset
)

/**
 * Get all spacecraft with basic filters and pagination.
 */
suspend fun SpacecraftApi.getSpacecraft(
    limit: Int? = null,
    offset: Int? = null,
    inSpace: Boolean? = null,
    isPlaceholder: Boolean? = null,
    search: String? = null
): HttpResponse<PaginatedResponseSpacecraftSummary> = listSpacecraftApiV1SpacecraftGet(
    configId = null,
    inSpace = inSpace,
    isPlaceholder = isPlaceholder,
    search = search,
    limit = limit,
    offset = offset
)

/**
 * Get detailed spacecraft information by ID.
 */
suspend fun SpacecraftApi.getSpacecraftDetail(
    id: Int
): HttpResponse<SpacecraftFull> = getSpacecraftApiV1SpacecraftSpacecraftIdGet(spacecraftId = id)
