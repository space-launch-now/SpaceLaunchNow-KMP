package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.AgencyFull
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAgencyList

/**
 * Extension functions for the Trantor AgenciesApi to provide clean, named-parameter
 * access to GET /agencies and /agencies/{id}.
 *
 * Note: Trantor's `country_code` filter takes a single ISO alpha-2 code, unlike the
 * legacy LL client which accepted a list. Callers with a multi-select country filter
 * should only send the first selected code; see AgencyRepositoryImpl.
 */

suspend fun AgenciesApi.listAgencies(
    search: String? = null,
    featured: Boolean? = null,
    typeIds: List<Int>? = null,
    countryCode: String? = null,
    ordering: String? = "name",
    limit: Int? = 25,
    offset: Int? = 0
): HttpResponse<PaginatedResponseAgencyList> = listAgenciesApiV1AgenciesGet(
    search = search,
    featured = featured,
    typeIds = typeIds?.joinToString(","),
    countryCode = countryCode,
    ordering = ordering,
    limit = limit,
    offset = offset
)

suspend fun AgenciesApi.getAgency(
    agencyId: Int,
    expand: String? = null
): HttpResponse<AgencyFull> = getAgencyApiV1AgenciesAgencyIdGet(agencyId = agencyId, expand = expand)

/**
 * Get a list of agencies with clean named parameters.
 */
suspend fun AgenciesApi.getAgencyList(
    limit: Int? = null,
    offset: Int? = null,
    ordering: String? = null,
    search: String? = null,
    featured: Boolean? = null,
    typeIds: String? = null,
    countryCode: String? = null
): HttpResponse<PaginatedResponseAgencyList> {
    return listAgenciesApiV1AgenciesGet(
        search = search,
        featured = featured,
        typeIds = typeIds,
        countryCode = countryCode,
        ordering = ordering,
        limit = limit,
        offset = offset
    )
}
