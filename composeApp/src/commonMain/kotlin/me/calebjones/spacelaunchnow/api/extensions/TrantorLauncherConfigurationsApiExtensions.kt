package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLauncherConfigSummary

/**
 * Extension functions for the Trantor LauncherConfigurationsApi to provide cleaner,
 * named-parameter call sites without exposing the generated method's full parameter list.
 */

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
