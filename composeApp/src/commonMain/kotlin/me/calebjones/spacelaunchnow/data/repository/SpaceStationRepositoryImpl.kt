package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getSpaceStationDetailed
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepository
import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ExpeditionsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.SpaceStationLocalDataSource
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock

/**
 * Implementation of SpaceStationRepository with cache-first pattern.
 * Provides offline support through stale-while-revalidate for space station data.
 */
class SpaceStationRepositoryImpl(
    private val spaceStationsApi: SpaceStationsApi,
    private val expeditionsApi: ExpeditionsApi,
    private val issTrackingRepository: IssTrackingRepository,
    private val localDataSource: SpaceStationLocalDataSource? = null
) : SpaceStationRepository {

    private val log = logger()

    override suspend fun getSpaceStationDetails(
        stationId: Int,
        forceRefresh: Boolean
    ): Result<DataResult<SpaceStationDetailedEndpoint>> {
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

            // Cache the result
            localDataSource?.cacheSpaceStation(station)

            Result.success(
                DataResult(
                    data = station,
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
    ): Result<DataResult<SpaceStationDetailedEndpoint>> {
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
    ): Result<DataResult<List<ExpeditionDetailed>>> {
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

            // Fetch from API in parallel
            log.d { "📡 Fetching ${expeditionIds.size} expedition details from API" }
            val expeditions = coroutineScope {
                expeditionIds.map { expeditionId ->
                    async {
                        try {
                            val response = expeditionsApi.expeditionsRetrieve(expeditionId)
                            response.body()
                        } catch (e: Exception) {
                            log.e(e) { "Error fetching expedition $expeditionId" }
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            log.i { "Successfully fetched ${expeditions.size}/${expeditionIds.size} expeditions from API" }

            // Cache the results
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
    ): Result<DataResult<List<ExpeditionDetailed>>> {
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
