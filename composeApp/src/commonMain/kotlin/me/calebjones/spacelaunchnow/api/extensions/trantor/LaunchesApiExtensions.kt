package me.calebjones.spacelaunchnow.api.extensions.trantor

import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.trantor.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchDetail
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLaunchList
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Extension functions for the Trantor LaunchesApi, providing named-parameter ergonomics
 * over the generated multi-parameter methods, matching the pattern established for the
 * LL client in LaunchesApiExtensions.kt.
 */

private fun List<Int>?.toCommaParam(): String? =
    this?.takeIf { it.isNotEmpty() }?.joinToString(",")

/**
 * List launches.
 *
 * Wire renames vs. the old LL client (see the Contracts `trantor-api-v1.md` mapping table):
 * `lsp__id` -> providerIds, `status__ids` -> statusIds, `location__ids` -> locationIds,
 * `program` -> programIds, `orbit__ids` -> orbitIds, `mission_type__ids` -> missionTypeIds,
 * `launcher_config_family_ids`/`families` -> familyIds, `rocket__configuration__id` ->
 * rocketConfigId, `net__gt`/`net__lt` -> netAfter/netBefore, `net__day`/`net__month` ->
 * netDay/netMonth (single ints, not lists - Trantor takes one value at a time).
 *
 * There is no server-side `previous` param anymore: `previous = true` is translated to
 * `upcoming = false` here so call sites don't need to know about the rename. A call site
 * that already passes `upcoming` directly is left alone.
 */
@OptIn(ExperimentalTime::class)
suspend fun LaunchesApi.getLaunchList(
    limit: Int? = null,
    offset: Int? = null,
    upcoming: Boolean? = null,
    previous: Boolean? = null,
    ordering: String? = null,
    search: String? = null,
    providerIds: List<Int>? = null,
    locationIds: List<Int>? = null,
    programIds: List<Int>? = null,
    orbitIds: List<Int>? = null,
    missionTypeIds: List<Int>? = null,
    familyIds: List<Int>? = null,
    rocketConfigId: Int? = null,
    padId: Int? = null,
    statusIds: List<Int>? = null,
    isCrewed: Boolean? = null,
    includeSuborbital: Boolean? = null,
    netAfter: Instant? = null,
    netBefore: Instant? = null,
    netDay: Int? = null,
    netMonth: Int? = null
): HttpResponse<PaginatedResponseLaunchList> = listLaunchesApiV1LaunchesGet(
    upcoming = upcoming ?: previous?.let { !it },
    netAfter = netAfter,
    netBefore = netBefore,
    netDay = netDay,
    netMonth = netMonth,
    statusIds = statusIds.toCommaParam(),
    providerIds = providerIds.toCommaParam(),
    locationIds = locationIds.toCommaParam(),
    programIds = programIds.toCommaParam(),
    orbitIds = orbitIds.toCommaParam(),
    missionTypeIds = missionTypeIds.toCommaParam(),
    familyIds = familyIds.toCommaParam(),
    rocketConfigId = rocketConfigId,
    padId = padId,
    isCrewed = isCrewed,
    includeSuborbital = includeSuborbital,
    search = search,
    ordering = ordering,
    limit = limit,
    offset = offset
)

/**
 * Get a single launch's full detail. Replaces the old LL list-as-detail pattern
 * (`GET /launches?id=<uuid>`) with a real `GET /launches/{id}` call.
 */
suspend fun LaunchesApi.getLaunchDetail(id: String): HttpResponse<LaunchDetail> =
    getLaunchApiV1LaunchesLaunchIdGet(launchId = id)
