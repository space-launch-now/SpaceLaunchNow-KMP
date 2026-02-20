package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautStatusList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchStatusList

/**
 * Extension functions for ConfigApi to provide cleaner, named-parameter interfaces
 * for common use cases without exposing the 70+ generated parameters.
 */

/**
 * Get a list of launch statuses with clean named parameters
 */
suspend fun ConfigApi.getStatusList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null
): HttpResponse<PaginatedLaunchStatusList> {
    return configLaunchStatusesList(
        limit = limit,
        offset = offset,
        ordering = ordering,
        search = search
    )
}

/**
 * Get a paginated list of astronaut statuses.
 *
 * @param limit Maximum number of results to return per page (default: 100)
 * @param offset Number of results to skip (for pagination)
 * @param ordering Field to sort by
 * @param search Search query
 *
 * @return HttpResponse containing PaginatedAstronautStatusList
 *
 * Example usage:
 * ```kotlin
 * val response = configApi.getAstronautStatuses(limit = 100)
 * val statuses = response.body()
 * ```
 */
suspend fun ConfigApi.getAstronautStatuses(
    limit: Int? = 100,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null
): HttpResponse<PaginatedAstronautStatusList> = configAstronautStatusesList(
    limit = limit,
    offset = offset,
    ordering = ordering,
    search = search
)

