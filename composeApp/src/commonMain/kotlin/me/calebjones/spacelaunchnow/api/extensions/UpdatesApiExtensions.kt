package me.calebjones.spacelaunchnow.api.extensions

import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.models.PaginatedUpdateEndpointList

/**
 * Extension functions for UpdatesApi to provide cleaner, named-parameter interfaces
 * instead of the verbose generated API methods
 */

/**
 * Get updates with commonly used parameters
 */
suspend fun UpdatesApi.getUpdates(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    launch: String? = null,
    launchServiceProvider: Int? = null,
    program: Int? = null,
    search: String? = null,
    createdOn: Instant? = null
): HttpResponse<PaginatedUpdateEndpointList> = updatesList(
    createdOn = createdOn,
    launch = launch,
    launchLaunchServiceProvider = launchServiceProvider,
    limit = limit,
    offset = offset,
    ordering = ordering,
    program = program,
    search = search
)

/**
 * Get latest updates ordered by creation date
 */
suspend fun UpdatesApi.getLatestUpdates(
    limit: Int = 10
): HttpResponse<PaginatedUpdateEndpointList> = getUpdates(
    limit = limit,
    ordering = "-created_on"
)
