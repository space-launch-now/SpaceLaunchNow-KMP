package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.trantor.getSpaceStationDetailed
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepository
import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.trantor.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.SpaceStationLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainDetail
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock

/**
 * Implementation of SpaceStationRepository with cache-first pattern, backed by Trantor's
 * `/api/v1/space_stations` endpoint.
 *
 * Trantor's station detail embeds `expeditions[]` with crew directly — there is no
 * standalone `/expeditions` endpoint — so [getExpeditionDetails] fetches the station once
 * and slices the requested expeditions out of the embedded array instead of the old
 * per-expedition Launch Library fan-out (`ExpeditionsApi.expeditionsRetrieve` x N).
 *
 * [SpaceStationLocalDataSource] caches the domain [SpaceStationDetail] / [ExpeditionDetailItem]
 * types directly, so the Trantor-mapped payloads fetched here are written straight to disk.
 */
class SpaceStationRepositoryImpl(
    private val spaceStationsApi: SpaceStationsApi,
    private val issTrackingRepository: IssTrackingRepository,
    private val localDataSource: SpaceStationLocalDataSource? = null
) : SpaceStationRepository {

    private val log = logger()

    override suspend fun getSpaceStationDetails(
        stationId: Int,
        forceRefresh: Boolean
    ): Result<DataResult<SpaceStationDetail>> {
        return try {
            log.d { "getSpaceStationDetails called - stationId: $stationId, forceRefresh: $forceRefresh" }

            val now = Clock.System.now().toEpochMilliseconds()

            // STALE-WHILE-REVALIDATE: Always check for stale data first (for fallback)
            val staleCached = localDataSource?.getSpaceStationStale(stationId)
            val staleTimestamp = localDataSource?.getStationCacheTimestamp(stationId)

            // Try fresh cache first if available and not forcing refresh
            if (!forceRefresh && localDataSource != null) {
                val cached = localDataSource.getSpaceStation(stationId)
                if (cached != null) {
                    val timestamp = localDataSource.getStationCacheTimestamp(stationId)
                    log.i { "✅ CACHE HIT: Returning fresh cached station: ${cached.name}" }
                    return Result.success(
                        DataResult(
                            data = cached,
                            source = DataSource.CACHE,
                            timestamp = timestamp ?: now
                        )
                    )
                }

                // No fresh cache, but have stale data - return it immediately
                if (staleCached != null) {
                    log.d { "⏳ STALE CACHE: Returning stale data immediately for station: ${staleCached.name}" }
                    return Result.success(
                        DataResult(
                            data = staleCached,
                            source = DataSource.STALE_CACHE,
                            timestamp = staleTimestamp
                        )
                    )
                }

                log.d { "Cache MISS - No cached data, fetching from API" }
            }

            // Fetch from API (cache miss or force refresh)
            log.d { "📡 Fetching station details from API - stationId: $stationId" }
            val response = spaceStationsApi.getSpaceStationDetailed(stationId)
            val station = response.body()
            log.i { "Successfully fetched station: ${station.name} from API" }

            val domainStation = station.toDomain()

            // Cache the domain-mapped payload (SpaceStationLocalDataSource stores/reads SpaceStationDetail directly)
            localDataSource?.cacheSpaceStation(domainStation)

            Result.success(
                DataResult(
                    data = domainStation,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )

        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching station $stationId (status: ${e.response.status})" }
            returnStaleStationOrFailure(stationId, e)
        } catch (e: IOException) {
            log.e(e) { "Network error while fetching station $stationId" }
            returnStaleStationOrFailure(stationId, e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while fetching station $stationId" }
            returnStaleStationOrFailure(stationId, e)
        }
    }

    private suspend fun returnStaleStationOrFailure(
        stationId: Int,
        error: Exception
    ): Result<DataResult<SpaceStationDetail>> {
        val stale = localDataSource?.getSpaceStationStale(stationId)
        return if (stale != null) {
            val timestamp = localDataSource?.getStationCacheTimestamp(stationId)
            log.w { "Returning stale cached station: ${stale.name} due to error" }
            Result.success(
                DataResult(
                    data = stale,
                    source = DataSource.STALE_CACHE,
                    timestamp = timestamp
                )
            )
        } else {
            Result.failure(error)
        }
    }

    override suspend fun getExpeditionDetails(
        expeditionIds: List<Int>,
        stationId: Int,
        forceRefresh: Boolean
    ): Result<DataResult<List<ExpeditionDetailItem>>> {
        return try {
            log.d { "getExpeditionDetails called - ${expeditionIds.size} expeditions, stationId: $stationId" }

            val now = Clock.System.now().toEpochMilliseconds()

            // STALE-WHILE-REVALIDATE: Always check for stale data first (for fallback)
            val staleCached = localDataSource?.getExpeditionsByStationIdStale(stationId)

            // Try fresh cache first if available and not forcing refresh
            if (!forceRefresh && localDataSource != null) {
                val cached = localDataSource.getExpeditionsByStationId(stationId)
                // Only use cache if we have all requested expeditions
                if (cached.isNotEmpty() && cached.map { it.id }.containsAll(expeditionIds)) {
                    log.i { "✅ CACHE HIT: Returning ${cached.size} fresh cached expeditions" }
                    return Result.success(
                        DataResult(
                            data = cached,
                            source = DataSource.CACHE,
                            timestamp = now
                        )
                    )
                }

                // No fresh cache, but have stale data - return it immediately
                if (staleCached != null && staleCached.isNotEmpty()) {
                    log.d { "⏳ STALE CACHE: Returning ${staleCached.size} stale expeditions immediately" }
                    return Result.success(
                        DataResult(
                            data = staleCached,
                            source = DataSource.STALE_CACHE,
                            timestamp = null
                        )
                    )
                }

                log.d { "Cache MISS - Cached ${cached.size} but need ${expeditionIds.size}, fetching from API" }
            }

            // Trantor has no standalone /expeditions endpoint: fetch the station once and
            // slice the requested expeditions out of its embedded `expeditions[]` (with
            // crew) instead of fanning out one call per expedition id.
            log.d { "📡 Fetching station detail to extract embedded expeditions (station $stationId)" }
            val response = spaceStationsApi.getSpaceStationDetailed(stationId)
            val rawStation = response.body()
            val allExpeditions = rawStation.expeditions.orEmpty().map { it.toDomainDetail() }
            val expeditions = if (expeditionIds.isEmpty()) {
                allExpeditions
            } else {
                allExpeditions.filter { it.id in expeditionIds }
            }

            log.i { "Successfully extracted ${expeditions.size}/${expeditionIds.size} expeditions from station detail" }

            // Cache the domain-mapped payload (SpaceStationLocalDataSource stores/reads ExpeditionDetailItem directly)
            localDataSource?.cacheExpeditions(expeditions, stationId)

            Result.success(
                DataResult(
                    data = expeditions,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )

        } catch (e: Exception) {
            log.e(e) { "Error fetching expedition details" }
            returnStaleExpeditionsOrFailure(stationId, e)
        }
    }

    private suspend fun returnStaleExpeditionsOrFailure(
        stationId: Int,
        error: Exception
    ): Result<DataResult<List<ExpeditionDetailItem>>> {
        val stale = localDataSource?.getExpeditionsByStationIdStale(stationId)
        return if (stale != null && stale.isNotEmpty()) {
            log.w { "Returning ${stale.size} stale cached expeditions due to error" }
            Result.success(
                DataResult(
                    data = stale,
                    source = DataSource.STALE_CACHE,
                    timestamp = null
                )
            )
        } else {
            Result.failure(error)
        }
    }

    override suspend fun getIssTle(
        noradId: String,
        forceRefresh: Boolean
    ): Result<DataResult<IssTle>> {
        return try {
            log.d { "getIssTle called - noradId: $noradId, forceRefresh: $forceRefresh" }

            val now = Clock.System.now().toEpochMilliseconds()

            // STALE-WHILE-REVALIDATE: Always check for stale data first (for fallback)
            val staleCached = localDataSource?.getTleStale(noradId)

            // Try fresh cache first if available and not forcing refresh
            if (!forceRefresh && localDataSource != null) {
                val cached = localDataSource.getTle(noradId)
                if (cached != null) {
                    log.i { "✅ CACHE HIT: Returning fresh cached TLE for $noradId" }
                    return Result.success(
                        DataResult(
                            data = cached,
                            source = DataSource.CACHE,
                            timestamp = now
                        )
                    )
                }

                // No fresh cache, but have stale data - return it immediately
                if (staleCached != null) {
                    log.d { "⏳ STALE CACHE: Returning stale TLE for $noradId immediately" }
                    return Result.success(
                        DataResult(
                            data = staleCached,
                            source = DataSource.STALE_CACHE,
                            timestamp = null
                        )
                    )
                }

                log.d { "Cache MISS - No cached TLE, fetching from API" }
            }

            // Fetch from API
            log.d { "📡 Fetching TLE from wheretheiss.at API" }
            val tleResult = issTrackingRepository.getTleData()

            tleResult.fold(
                onSuccess = { tle ->
                    log.i { "Successfully fetched TLE for ${tle.name} from API" }

                    // Cache the result
                    localDataSource?.cacheTle(tle)

                    Result.success(
                        DataResult(
                            data = tle,
                            source = DataSource.NETWORK,
                            timestamp = now
                        )
                    )
                },
                onFailure = { error ->
                    log.e(error as? Exception) { "Error fetching TLE from API" }
                    returnStaleTleOrFailure(noradId, error as? Exception ?: Exception(error.message))
                }
            )

        } catch (e: Exception) {
            log.e(e) { "Error fetching TLE" }
            returnStaleTleOrFailure(noradId, e)
        }
    }

    private suspend fun returnStaleTleOrFailure(
        noradId: String,
        error: Exception
    ): Result<DataResult<IssTle>> {
        val stale = localDataSource?.getTleStale(noradId)
        return if (stale != null) {
            log.w { "Returning stale cached TLE for $noradId due to error" }
            Result.success(
                DataResult(
                    data = stale,
                    source = DataSource.STALE_CACHE,
                    timestamp = null
                )
            )
        } else {
            Result.failure(error)
        }
    }

    override suspend fun prewarmIssCache() {
        log.i { "Pre-warming ISS cache..." }

        try {
            // Fetch ISS station details
            val stationResult = getSpaceStationDetails(
                stationId = SpaceStationRepository.ISS_STATION_ID,
                forceRefresh = false
            )

            stationResult.onSuccess { result ->
                log.d { "Pre-warmed station: ${result.data.name} (source: ${result.source})" }

                // Fetch expedition details for active expeditions
                val expeditionIds = result.data.activeExpeditions.map { it.id }
                if (expeditionIds.isNotEmpty()) {
                    val expeditionResult = getExpeditionDetails(
                        expeditionIds = expeditionIds,
                        stationId = SpaceStationRepository.ISS_STATION_ID,
                        forceRefresh = false
                    )
                    expeditionResult.onSuccess { expResult ->
                        log.d { "Pre-warmed ${expResult.data.size} expeditions (source: ${expResult.source})" }
                    }.onFailure { e ->
                        log.w(e as? Exception) { "Failed to pre-warm expeditions" }
                    }
                }
            }.onFailure { e ->
                log.w(e as? Exception) { "Failed to pre-warm station details" }
            }

            // Fetch TLE data
            val tleResult = getIssTle(forceRefresh = false)
            tleResult.onSuccess { result ->
                log.d { "Pre-warmed TLE for ${result.data.name} (source: ${result.source})" }
            }.onFailure { e ->
                log.w(e as? Exception) { "Failed to pre-warm TLE data" }
            }

            log.i { "ISS cache pre-warm complete" }

        } catch (e: Exception) {
            log.e(e) { "Error during ISS cache pre-warm" }
        }
    }
}
