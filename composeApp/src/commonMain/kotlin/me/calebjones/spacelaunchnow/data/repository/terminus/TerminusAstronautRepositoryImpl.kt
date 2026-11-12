package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.AstronautsApi
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomainDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Astronaut repository for the Terminus backend (Tasks#1012). The list is `GET /api/v1/astronauts/`
 * (the normal variant, as the LL repository uses) and the detail is `GET /api/v1/astronauts/{id}/`,
 * whose flights, landings and spacewalks map through the same LL bridge. Nothing delegates to LL.
 *
 * The interface has no `forceRefresh`, so a read is served from the 30 s [TerminusMemoryCache]
 * when fresh; a 5xx or a network failure falls back to the stale entry.
 */
class TerminusAstronautRepositoryImpl internal constructor(
    private val astronautsApi: AstronautsApi,
    private val cache: TerminusMemoryCache
) : AstronautRepository {

    constructor(astronautsApi: AstronautsApi) : this(astronautsApi, TerminusMemoryCache())

    private val log = logger()

    override suspend fun getAstronauts(
        limit: Int,
        offset: Int,
        search: String?,
        statusIds: List<Int>?,
        agencyIds: List<Int>?,
        ordering: String?,
        hasFlown: Boolean?,
        inSpace: Boolean?,
        isHuman: Boolean?
    ): Result<PaginatedResult<AstronautListItem>> {
        val key = "astronauts?limit=$limit&offset=$offset&search=$search&status=${statusIds?.joinToString(",")}" +
            "&agency=${agencyIds?.joinToString(",")}&ordering=$ordering&hasFlown=$hasFlown" +
            "&inSpace=$inSpace&isHuman=$isHuman"
        log.d { "terminus $key" }
        return terminusMemoryCachedFetch(cache, key, "astronauts", forceRefresh = false) {
            astronautsApi.astronautsList(
                limit = limit,
                offset = offset,
                search = search,
                statusIds = statusIds?.map { it.toDouble() },
                agencyIds = agencyIds?.map { it.toDouble() },
                ordering = ordering,
                hasFlown = hasFlown,
                inSpace = inSpace,
                isHuman = isHuman
            )
        }.mapCatching { it.value.toDomain() }
    }

    override suspend fun getAstronautDetail(id: Int): Result<AstronautDetail> {
        log.d { "terminus astronaut detail $id" }
        return terminusMemoryCachedFetch(cache, "astronaut?id=$id", "astronaut $id", forceRefresh = false) {
            astronautsApi.astronautsRetrieve(id)
        }.mapCatching { it.value.toDomainDetail() }
    }
}
