package me.calebjones.spacelaunchnow.api.extensions.trantor

import me.calebjones.spacelaunchnow.api.trantor.apis.LookupsApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.LookupsResponse

/**
 * Extension functions for the Trantor LookupsApi to provide cleaner call sites.
 */

/**
 * Fetch every filter-picker lookup table (statuses, event types, orbits, mission types,
 * astronaut statuses, agency types) in one long-cached payload. Replaces the app's five
 * separate LL /config/_* picker calls.
 */
suspend fun LookupsApi.getLookups(): HttpResponse<LookupsResponse> {
    return getLookupsApiV1LookupsGet()
}
