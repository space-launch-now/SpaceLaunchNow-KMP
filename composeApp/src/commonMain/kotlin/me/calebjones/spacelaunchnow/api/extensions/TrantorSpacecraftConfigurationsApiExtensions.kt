package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseSpacecraftConfigSummary
import me.calebjones.spacelaunchnow.api.trantor.models.SpacecraftConfigFull

/**
 * Extension functions for the Trantor [SpacecraftConfigurationsApi]. Filter rename per the
 * phase5 contract: `agency` (LL) -> `agency_id` (Trantor). `human_rated` / `in_use` filters
 * are deferred on the Trantor side and intentionally not exposed here.
 */

/**
 * Get spacecraft configurations (spacecraft types) filtered by manufacturer agency.
 */
suspend fun SpacecraftConfigurationsApi.getConfigurationsByAgency(
    agencyId: Int? = null,
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null
): HttpResponse<PaginatedResponseSpacecraftConfigSummary> = listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet(
    agencyId = agencyId,
    search = search,
    limit = limit,
    offset = offset
)

/**
 * Get all spacecraft configurations with basic filters.
 */
suspend fun SpacecraftConfigurationsApi.getConfigurations(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null
): HttpResponse<PaginatedResponseSpacecraftConfigSummary> = getConfigurationsByAgency(
    agencyId = null,
    limit = limit,
    offset = offset,
    search = search
)

/**
 * Get detailed spacecraft configuration by ID.
 */
suspend fun SpacecraftConfigurationsApi.getConfigurationDetails(
    id: Int
): HttpResponse<SpacecraftConfigFull> = getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet(configId = id)
