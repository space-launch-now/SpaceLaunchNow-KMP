package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherDetail
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLauncherListItem

/**
 * Extension functions for the Trantor LaunchersApi to provide clean, named-parameter
 * access to GET /launchers and /launchers/{id}.
 */

suspend fun LaunchersApi.listLaunchers(
    configIds: List<Int>? = null,
    isPlaceholder: Boolean? = null,
    search: String? = null,
    ordering: String? = "-flights",
    limit: Int? = 25,
    offset: Int? = 0
): HttpResponse<PaginatedResponseLauncherListItem> = listLaunchersApiV1LaunchersGet(
    configIds = configIds?.joinToString(","),
    isPlaceholder = isPlaceholder,
    search = search,
    ordering = ordering,
    limit = limit,
    offset = offset
)

suspend fun LaunchersApi.getLauncher(
    launcherId: Int
): HttpResponse<LauncherDetail> = getLauncherApiV1LaunchersLauncherIdGet(launcherId = launcherId)
