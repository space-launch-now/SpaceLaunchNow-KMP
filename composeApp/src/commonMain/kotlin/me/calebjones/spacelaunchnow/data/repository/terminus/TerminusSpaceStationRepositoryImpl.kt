package me.calebjones.spacelaunchnow.data.repository.terminus

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.terminus.apis.ExpeditionsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomainDetail
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Space station repository for the Terminus backend (Tasks#1012). Station and expedition details
 * come from Terminus (`GET /api/v1/space_stations/{id}/`, `GET /api/v1/expeditions/{id}/`).
 *
 * The ISS TLE is not a Launch Library read: it comes from wheretheiss.at. [getIssTle] therefore
 * stays on [tle], the LL implementation, which owns the TLE cache. [prewarmIssCache] is rebuilt
 * here so it warms the Terminus reads, not the LL ones.
 *
 * Reads go through [terminusMemoryCachedFetch] (30 s fresh, stale kept for a 5xx or a network
 * failure). The station and expedition tables in SQLDelight hold LL rows, so Terminus keeps its
 * own in-memory cache; a restart starts empty.
 */
class TerminusSpaceStationRepositoryImpl internal constructor(
    private val spaceStationsApi: SpaceStationsApi,
    private val expeditionsApi: ExpeditionsApi,
    private val tle: SpaceStationRepository,
    private val cache: TerminusMemoryCache
) : SpaceStationRepository {

    constructor(
        spaceStationsApi: SpaceStationsApi,
        expeditionsApi: ExpeditionsApi,
        tle: SpaceStationRepository
    ) : this(spaceStationsApi, expeditionsApi, tle, TerminusMemoryCache())

    private val log = logger()

    override suspend fun getSpaceStationDetails(
        stationId: Int,
        forceRefresh: Boolean
    ): Result<DataResult<SpaceStationDetail>> {
        log.d { "terminus station $stationId, forceRefresh: $forceRefresh" }
        return terminusMemoryCachedFetch(
            cache = cache,
            key = "space-station?id=$stationId",
            what = "space station $stationId",
            forceRefresh = forceRefresh
        ) { spaceStationsApi.spaceStationsRetrieve(stationId) }
            .mapCatching { DataResult(it.value.toDomain(), it.source, it.timestamp) }
    }

    override suspend fun getExpeditionDetails(
        expeditionIds: List<Int>,
        stationId: Int,
        forceRefresh: Boolean
    ): Result<DataResult<List<ExpeditionDetailItem>>> {
        log.d { "terminus ${expeditionIds.size} expeditions of station $stationId, forceRefresh: $forceRefresh" }
        // Like the LL path, an expedition that fails is dropped; the others still show.
        val fetched = coroutineScope {
            expeditionIds.map { id ->
                async {
                    terminusMemoryCachedFetch(
                        cache = cache,
                        key = "expedition?id=$id",
                        what = "expedition $id",
                        forceRefresh = forceRefresh
                    ) { expeditionsApi.expeditionsRetrieve(id) }
                        .mapCatching { TerminusFetched(it.value.toDomainDetail(), it.source, it.timestamp) }
                        .onFailure { log.e(it) { "Error fetching expedition $id" } }
                        .getOrNull()
                }
            }.awaitAll().filterNotNull()
        }
        val source = when {
            fetched.any { it.source == DataSource.NETWORK } -> DataSource.NETWORK
            fetched.any { it.source == DataSource.STALE_CACHE } -> DataSource.STALE_CACHE
            fetched.isNotEmpty() -> DataSource.CACHE
            else -> DataSource.NETWORK
        }
        return Result.success(
            DataResult(fetched.map { it.value }, source, fetched.minOfOrNull { it.timestamp })
        )
    }

    override suspend fun getIssTle(noradId: String, forceRefresh: Boolean): Result<DataResult<IssTle>> =
        tle.getIssTle(noradId, forceRefresh)

    override suspend fun prewarmIssCache() {
        log.i { "Pre-warming ISS cache (terminus)..." }
        getSpaceStationDetails(SpaceStationRepository.ISS_STATION_ID, forceRefresh = false)
            .onSuccess { station ->
                val expeditionIds = station.data.activeExpeditions.map { it.id }
                if (expeditionIds.isNotEmpty()) {
                    getExpeditionDetails(expeditionIds, SpaceStationRepository.ISS_STATION_ID, forceRefresh = false)
                }
            }
            .onFailure { log.w(it as? Exception) { "Failed to pre-warm station details" } }
        getIssTle(forceRefresh = false)
            .onFailure { log.w(it as? Exception) { "Failed to pre-warm TLE data" } }
    }
}
