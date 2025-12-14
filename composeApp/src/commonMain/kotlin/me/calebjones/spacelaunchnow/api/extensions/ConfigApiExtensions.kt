package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
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
