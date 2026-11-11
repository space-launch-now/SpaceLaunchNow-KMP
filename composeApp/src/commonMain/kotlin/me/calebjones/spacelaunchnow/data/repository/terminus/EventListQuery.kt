package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.EventsApi
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedEventEndpointNormalList

/**
 * The filters of one Terminus `GET /api/v1/events/` request, in normal form: id lists sorted and
 * de-duplicated, an `offset` of 0 left out. Equal requests build equal URLs, and [key] names the
 * request in the cache.
 *
 * Generated name to wire name: typeIds to `type__ids`, launchIds to `launch__ids`.
 */
internal class EventListQuery private constructor(
    val limit: Int?,
    val offset: Int?,
    val upcoming: Boolean?,
    val ordering: String?,
    val search: String?,
    val typeIds: List<Int>?,
    val launchIds: List<String>?,
    val program: List<Int>?
) {
    /** Cache name of this request. Never log it: it holds the search text. */
    val key: String = describe(search?.let { LaunchListCacheKey.search(it) })

    /** Name of this request for logs: the search text is shown as `<set>`. */
    val logLabel: String = describe(search?.let { "<set>" })

    private fun describe(searchPart: String?): String = terminusCacheKey(
        "events",
        "limit" to limit, "offset" to offset, "upcoming" to upcoming,
        "ordering" to ordering?.let { LaunchListCacheKey.encode(it) }, "search" to searchPart,
        "type__ids" to typeIds?.joinToString(","),
        "launch__ids" to launchIds?.joinToString(",") { LaunchListCacheKey.encode(it) },
        "program" to program?.joinToString(",")
    )

    suspend fun fetch(api: EventsApi): HttpResponse<PaginatedEventEndpointNormalList> =
        api.eventsList(
            limit = limit,
            offset = offset,
            upcoming = upcoming,
            ordering = ordering,
            search = search,
            typeIds = typeIds,
            launchIds = launchIds,
            program = program
        )

    companion object {
        fun of(
            limit: Int? = null,
            offset: Int? = null,
            upcoming: Boolean? = null,
            ordering: String? = null,
            search: String? = null,
            typeIds: List<Int>? = null,
            launchIds: List<String>? = null,
            program: List<Int>? = null
        ) = EventListQuery(
            limit = limit,
            offset = offset?.takeIf { it > 0 },
            upcoming = upcoming,
            ordering = ordering,
            search = search,
            typeIds = typeIds.normalizedIds(),
            launchIds = launchIds?.takeIf { it.isNotEmpty() }?.distinct()?.sorted(),
            program = program.normalizedIds()
        )
    }
}
