package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.AgencyFull

/**
 * Extension functions for the Trantor AgenciesApi, providing named-parameter ergonomics
 * over the generated methods, matching the pattern established for the LL client in
 * AgenciesApiExtensions.kt.
 */

/**
 * Get a single agency's full detail (`GET /agencies/{id}`).
 */
suspend fun AgenciesApi.getAgencyDetail(
    id: Int,
    expand: String? = null
): HttpResponse<AgencyFull> = getAgencyApiV1AgenciesAgencyIdGet(agencyId = id, expand = expand)
