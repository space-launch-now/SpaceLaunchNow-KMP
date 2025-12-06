package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed

/**
 * Extension functions for SpacecraftConfigurationsApi to provide clean, named-parameter interfaces.
 * 
 * NOTE: SpacecraftConfigurationsApi does NOT have a `program` filter like LauncherConfigurationsApi.
 * We can filter by `agency` (SpaceX = 121) instead, or filter client-side after fetching.
 */

/**
 * Get spacecraft configurations (spacecraft types) with agency filter and pagination.
 * 
 * SpacecraftConfig represents a spacecraft type/model (e.g., "Starship", "Dragon 2").
 * This is the "category" level - individual spacecraft belong to a configuration.
 * 
 * @param agencyId Filter by agency ID (e.g., 121 for SpaceX)
 * @param limit Number of results per page (max 100)
 * @param offset Pagination offset
 * @param search Search term for name
 * @param humanRated Filter by human-rated status
 * @param inUse Filter by in-use status
 * @param ordering Field to order by (e.g., "name")
 */
suspend fun SpacecraftConfigurationsApi.getConfigurationsByAgency(
    agencyId: Int? = null,
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    humanRated: Boolean? = null,
    inUse: Boolean? = null,
    ordering: String? = null
): HttpResponse<PaginatedSpacecraftConfigDetailedList> = spacecraftConfigurationsDetailedList(
    agency = agencyId,
    limit = limit,
    offset = offset,
    search = search,
    humanRated = humanRated,
    inUse = inUse,
    ordering = ordering,
    name = null
)

/**
 * Get all spacecraft configurations with basic filters.
 */
suspend fun SpacecraftConfigurationsApi.getConfigurations(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    humanRated: Boolean? = null,
    inUse: Boolean? = null,
    ordering: String? = null
): HttpResponse<PaginatedSpacecraftConfigDetailedList> = getConfigurationsByAgency(
    agencyId = null,
    limit = limit,
    offset = offset,
    search = search,
    humanRated = humanRated,
    inUse = inUse,
    ordering = ordering
)

/**
 * Get detailed spacecraft configuration by ID
 */
suspend fun SpacecraftConfigurationsApi.getConfigurationDetails(
    id: Int
): HttpResponse<SpacecraftConfigDetailed> = spacecraftConfigurationsRetrieve(id = id)
