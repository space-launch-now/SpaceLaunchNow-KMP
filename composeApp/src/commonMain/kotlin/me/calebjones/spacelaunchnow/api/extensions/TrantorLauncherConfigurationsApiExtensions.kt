package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigFull
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLauncherConfigSummary

/**
 * Extension functions for the Trantor LauncherConfigurationsApi to provide clean,
 * named-parameter access to GET /configurations and /configurations/{id}.
 */

suspend fun LauncherConfigurationsApi.listConfigurations(
    search: String? = null,
    active: Boolean? = null,
    manufacturerId: Int? = null,
    reusable: Boolean? = null,
    familyIds: List<Int>? = null,
    programIds: List<Int>? = null,
    isPlaceholder: Boolean? = null,
    ordering: String? = "name",
    limit: Int? = 25,
    offset: Int? = 0
): HttpResponse<PaginatedResponseLauncherConfigSummary> = listConfigurationsApiV1ConfigurationsGet(
    search = search,
    active = active,
    manufacturerId = manufacturerId,
    reusable = reusable,
    familyIds = familyIds?.joinToString(","),
    programIds = programIds?.joinToString(","),
    isPlaceholder = isPlaceholder,
    ordering = ordering,
    limit = limit,
    offset = offset
)

suspend fun LauncherConfigurationsApi.getConfiguration(
    configId: Int,
    expand: String? = null
): HttpResponse<LauncherConfigFull> = getConfigurationApiV1ConfigurationsConfigIdGet(
    configId = configId,
    expand = expand
)

/**
 * Get a list of launcher (rocket) configurations with clean named parameters.
 */
suspend fun LauncherConfigurationsApi.getConfigurationList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null,
    active: Boolean? = null,
    manufacturerId: Int? = null,
    reusable: Boolean? = null,
    familyIds: String? = null,
    programIds: String? = null,
    isPlaceholder: Boolean? = null
): HttpResponse<PaginatedResponseLauncherConfigSummary> {
    return listConfigurationsApiV1ConfigurationsGet(
        search = search,
        active = active,
        manufacturerId = manufacturerId,
        reusable = reusable,
        familyIds = familyIds,
        programIds = programIds,
        isPlaceholder = isPlaceholder,
        ordering = ordering,
        limit = limit,
        offset = offset
    )
}
