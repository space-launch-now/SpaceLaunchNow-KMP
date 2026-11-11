package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.extensions.TimeFilterRounding
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchNormalList
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The filters of one Terminus launch-list request, in normal form: time filters rounded with
 * [TimeFilterRounding] (the raw generated client does not round), id filters sorted and
 * de-duplicated, an `offset` of 0 left out. Equal requests build equal URLs, and [key] names the
 * request in the list cache.
 *
 * Generated name to wire name: lspId to `lsp__id`, locationIds to `location__ids`, statusIds to
 * `status__ids`, orbitIds to `orbit__ids`, missionTypeIds to `mission_type__ids`,
 * launcherConfigFamilyIds to `launcher_config_family_ids`, rocketConfigurationId to
 * `rocket__configuration__id`, isCrewed to `is_crewed`, includeSuborbital to `include_suborbital`,
 * netGt to `net__gt`, netLt to `net__lt`.
 */
@OptIn(ExperimentalTime::class)
internal class LaunchListQuery private constructor(
    val limit: Int?,
    val offset: Int?,
    val upcoming: Boolean?,
    val previous: Boolean?,
    val ordering: String?,
    val search: String?,
    val lspId: List<Int>?,
    val locationIds: List<Int>?,
    val program: List<Int>?,
    val rocketConfigurationId: Int?,
    val isCrewed: Boolean?,
    val includeSuborbital: Boolean?,
    val statusIds: List<Int>?,
    val orbitIds: List<Int>?,
    val missionTypeIds: List<Int>?,
    val launcherConfigFamilyIds: List<Int>?,
    val netGt: Instant?,
    val netLt: Instant?
) {
    /**
     * Stable name of this request, for the cache only (never log it: it holds the search text).
     * Every set filter appears, in a fixed order. Values are percent-encoded, so no value can
     * forge another filter, and a long search is replaced by its length and a hash.
     */
    val key: String = describe(searchPart = search?.let { LaunchListCacheKey.search(it) })

    /** Name of this request for logs: the same filters, but the search text is shown as `<set>`. */
    val logLabel: String = describe(searchPart = search?.let { "<set>" })

    private fun describe(searchPart: String?): String = listOf(
        "limit" to limit, "offset" to offset, "upcoming" to upcoming, "previous" to previous,
        "ordering" to ordering?.let { LaunchListCacheKey.encode(it) }, "search" to searchPart,
        "lsp__id" to lspId?.joinToString(","),
        "location__ids" to locationIds?.joinToString(","), "program" to program?.joinToString(","),
        "rocket__configuration__id" to rocketConfigurationId, "is_crewed" to isCrewed,
        "include_suborbital" to includeSuborbital, "status__ids" to statusIds?.joinToString(","),
        "orbit__ids" to orbitIds?.joinToString(","), "mission_type__ids" to missionTypeIds?.joinToString(","),
        "launcher_config_family_ids" to launcherConfigFamilyIds?.joinToString(","),
        "net__gt" to netGt, "net__lt" to netLt
    ).filter { it.second != null }.joinToString("&") { "${it.first}=${it.second}" }

    suspend fun fetchMini(api: LaunchesApi): HttpResponse<PaginatedLaunchBasicList> =
        api.launchesMiniList(
            limit = limit,
            offset = offset,
            upcoming = upcoming,
            previous = previous,
            ordering = ordering,
            search = search,
            lspId = lspId,
            locationIds = locationIds,
            program = program,
            rocketConfigurationId = rocketConfigurationId,
            isCrewed = isCrewed,
            includeSuborbital = includeSuborbital,
            statusIds = statusIds,
            orbitIds = orbitIds,
            missionTypeIds = missionTypeIds,
            launcherConfigFamilyIds = launcherConfigFamilyIds,
            netGt = netGt,
            netLt = netLt
        )

    suspend fun fetchNormal(api: LaunchesApi): HttpResponse<PaginatedLaunchNormalList> =
        api.launchesList(
            limit = limit,
            offset = offset,
            upcoming = upcoming,
            previous = previous,
            ordering = ordering,
            search = search,
            lspId = lspId,
            locationIds = locationIds,
            program = program,
            rocketConfigurationId = rocketConfigurationId,
            isCrewed = isCrewed,
            includeSuborbital = includeSuborbital,
            statusIds = statusIds,
            orbitIds = orbitIds,
            missionTypeIds = missionTypeIds,
            launcherConfigFamilyIds = launcherConfigFamilyIds,
            netGt = netGt,
            netLt = netLt
        )

    companion object {
        fun of(
            limit: Int? = null,
            offset: Int? = null,
            upcoming: Boolean? = null,
            previous: Boolean? = null,
            ordering: String? = null,
            search: String? = null,
            lspId: List<Int>? = null,
            locationIds: List<Int>? = null,
            program: List<Int>? = null,
            rocketConfigurationId: Int? = null,
            isCrewed: Boolean? = null,
            includeSuborbital: Boolean? = null,
            statusIds: List<Int>? = null,
            orbitIds: List<Int>? = null,
            missionTypeIds: List<Int>? = null,
            launcherConfigFamilyIds: List<Int>? = null,
            netGt: Instant? = null,
            netLt: Instant? = null
        ) = LaunchListQuery(
            limit = limit,
            offset = offset?.takeIf { it > 0 },
            upcoming = upcoming,
            previous = previous,
            ordering = ordering,
            search = search,
            lspId = lspId.normalizedIds(),
            locationIds = locationIds.normalizedIds(),
            program = program.normalizedIds(),
            rocketConfigurationId = rocketConfigurationId,
            isCrewed = isCrewed,
            includeSuborbital = includeSuborbital,
            statusIds = statusIds.normalizedIds(),
            orbitIds = orbitIds.normalizedIds(),
            missionTypeIds = missionTypeIds.normalizedIds(),
            launcherConfigFamilyIds = launcherConfigFamilyIds.normalizedIds(),
            netGt = netGt?.let { TimeFilterRounding.floor(it) },
            netLt = netLt?.let { TimeFilterRounding.ceil(it) }
        )
    }
}
