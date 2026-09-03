package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.ll.getSpaceStationDetailed
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepository
import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ExpeditionsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepository
import me.calebjones.spacelaunchnow.database.SpaceStationLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainDetail
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock

/**
 * Space station repository, backed directly by the Launch Library 2 (LL) API - the pre-Trantor-
 * migration implementation, resurrected as a standalone class so the app can fall back to LL in
 * production via the `DataBackend` revert lever (see Koin wiring). Implements the CURRENT
 * [SpaceStationRepository] interface (domain-typed throughout).
 *
 * LL has no embedded expedition data on the station detail endpoint (unlike Trantor's
 * `space_stations`, which inlines `expeditions[]`), so [getExpeditionDetails] restores the
 * original N+1 fan-out over the standalone `ExpeditionsApi` - genuinely required by LL, not a
 * regression introduced here.
 *
 * Every network result is mapped LL -> domain via the existing `domain/mapper` extension
 * functions declared for LL's `api.launchlibrary.models` types in `SpaceStationMappers.kt`,
 * untouched by the Trantor migration, before being cached through [SpaceStationLocalDataSource],
 * which - per the phase5 migration - only ever stores the domain [SpaceStationDetail] /
 * [ExpeditionDetailItem] types directly.
 */
class LLSpaceStationRepositoryImpl(
    private val spaceStationsApi: SpaceStationsApi,
    private val expeditionsApi: ExpeditionsApi,
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

            val staleCached = localDataSource?.getSpaceStationStale(stationId)
            val staleTimestamp = localDataSource?.getStationCacheTimestamp(stationId)

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

            log.d { "📡 Fetching station details from API - stationId: $stationId" }
            val response = spaceStationsApi.getSpaceStationDetailed(stationId)
            val station = response.body()
            log.i { "Successfully fetched station: ${station.name} from API" }

            val domainStation = station.toDomain()

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

            val staleCached = localDataSource?.getExpeditionsByStationIdStale(stationId)

            if (!forceRefresh && localDataSource != null) {
                val cached = localDataSource.getExpeditionsByStationId(stationId)
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

            // LL has no standalone bulk/embedded expedition source - fan out one request per
            // expedition id, same as the pre-migration behavior.
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

            val domainExpeditions = expeditions.map { it.toDomainDetail() }

            localDataSource?.cacheExpeditions(domainExpeditions, stationId)

            Result.success(
                DataResult(
                    data = domainExpeditions,
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

            val staleCached = localDataSource?.getTleStale(noradId)

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

            log.d { "📡 Fetching TLE from wheretheiss.at API" }
            val tleResult = issTrackingRepository.getTleData()

            tleResult.fold(
                onSuccess = { tle ->
                    log.i { "Successfully fetched TLE for ${tle.name} from API" }

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
            val stationResult = getSpaceStationDetails(
                stationId = SpaceStationRepository.ISS_STATION_ID,
                forceRefresh = false
            )

            stationResult.onSuccess { result ->
                log.d { "Pre-warmed station: ${result.data.name} (source: ${result.source})" }

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
