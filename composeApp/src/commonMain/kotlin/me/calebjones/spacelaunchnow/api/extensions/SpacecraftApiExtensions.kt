package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftEndpointDetailedList

/**
 * Extension functions for SpacecraftApi to provide clean, named-parameter interfaces
 * instead of dealing with many generated parameters
 */

/**
 * Get spacecraft filtered by configuration ID
 */
suspend fun SpacecraftApi.getSpacecraftByConfig(
    configId: Int,
    limit: Int? = null,
    inSpace: Boolean? = null,
    ordering: String? = null
): HttpResponse<PaginatedSpacecraftEndpointDetailedList> = spacecraftDetailedList(
    spacecraftConfig = configId,
    limit = limit,
    inSpace = inSpace,
    ordering = ordering,
    isPlaceholder = null,
    name = null,
    offset = null,
    search = null,
    status = null
)

/**
 * Get all spacecraft with basic filters and pagination
 */
suspend fun SpacecraftApi.getSpacecraft(
    limit: Int? = null,
    offset: Int? = null,
    inSpace: Boolean? = null,
    isPlaceholder: Boolean? = null,
    ordering: String? = null,
    search: String? = null
): HttpResponse<PaginatedSpacecraftEndpointDetailedList> = spacecraftDetailedList(
    limit = limit,
    offset = offset,
    inSpace = inSpace,
    isPlaceholder = isPlaceholder,
    ordering = ordering,
    search = search,
    spacecraftConfig = null,
    name = null,
    status = null
)

