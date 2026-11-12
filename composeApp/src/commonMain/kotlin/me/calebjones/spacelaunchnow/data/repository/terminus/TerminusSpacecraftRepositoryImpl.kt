package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.repository.SpacecraftRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Spacecraft repository for the Terminus backend (Tasks#1012). The lists use the detailed variant,
 * as the LL repository does: `GET /api/v1/spacecraft/detailed/` for the per-config list and the
 * paged list, and `GET /api/v1/spacecraft/{id}/` for one spacecraft. Nothing delegates to LL.
 *
 * Reads go through the 30 s [TerminusMemoryCache]; a 5xx or a network failure falls back to the
 * stale entry. The LL SQLDelight spacecraft table is not used on this backend.
 */
class TerminusSpacecraftRepositoryImpl internal constructor(
    private val spacecraftApi: SpacecraftApi,
    private val cache: TerminusMemoryCache
) : SpacecraftRepository {

    constructor(spacecraftApi: SpacecraftApi) : this(spacecraftApi, TerminusMemoryCache())

    private val log = logger()

    override suspend fun getSpacecraftByConfigDomain(
        configId: Int,
        limit: Int,
        forceRefresh: Boolean,
        isPlaceholder: Boolean?
    ): Result<DataResult<List<Spacecraft>>> {
        val key = "spacecraft?config=$configId&limit=$limit&placeholder=$isPlaceholder"
        log.d { "terminus $key forceRefresh=$forceRefresh" }
        return terminusMemoryCachedFetch(cache, key, "spacecraft for config $configId", forceRefresh) {
            spacecraftApi.spacecraftDetailedList(
                spacecraftConfig = configId,
                limit = limit,
                ordering = "-id",
                isPlaceholder = isPlaceholder
            )
        }.mapCatching {
            DataResult(data = it.value.toDomain().results, source = it.source, timestamp = it.timestamp)
        }
    }

    override suspend fun getSpacecraftDetailsDomain(spacecraftId: Int): Result<Spacecraft> {
        log.d { "terminus spacecraft detail $spacecraftId" }
        return terminusMemoryCachedFetch(cache, "spacecraft?id=$spacecraftId", "spacecraft $spacecraftId", forceRefresh = false) {
            spacecraftApi.spacecraftRetrieve(spacecraftId)
        }.mapCatching { it.value.toDomain() }
    }

    override suspend fun getSpacecraftDomain(
        limit: Int,
        offset: Int,
        inSpace: Boolean?,
        search: String?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResult<Spacecraft>> {
        val key = "spacecraft?limit=$limit&offset=$offset&inSpace=$inSpace&search=$search&placeholder=$isPlaceholder"
        log.d { "terminus $key" }
        return terminusMemoryCachedFetch(cache, key, "spacecraft list", forceRefresh = false) {
            spacecraftApi.spacecraftDetailedList(
                limit = limit,
                offset = offset,
                inSpace = inSpace,
                search = search,
                ordering = "-flights_count",
                isPlaceholder = isPlaceholder
            )
        }.mapCatching { it.value.toDomain() }
    }
}
