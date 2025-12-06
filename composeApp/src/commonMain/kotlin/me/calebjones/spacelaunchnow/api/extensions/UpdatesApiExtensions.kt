package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList
import kotlin.time.Instant

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
    allProgram: Int? = null,
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
    search = search,
    allProgram = allProgram

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

/**
 * Get updates for Starship program (program ID = 1)
 */
suspend fun UpdatesApi.getStarshipUpdates(
    limit: Int = 20
): HttpResponse<PaginatedUpdateEndpointList> = getUpdates(
    limit = limit,
    program = 1,
    ordering = "-created_on"
)

