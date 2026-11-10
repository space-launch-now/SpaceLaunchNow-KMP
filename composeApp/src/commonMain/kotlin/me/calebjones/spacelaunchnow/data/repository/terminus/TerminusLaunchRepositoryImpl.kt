package me.calebjones.spacelaunchnow.data.repository.terminus

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.terminus.getLaunchDetail
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Launch repository for the Terminus backend. Launch detail (`GET /api/v1/launches/{id}/`)
 * comes from Terminus; every other call goes to [delegate], the LL repository, until its
 * own port lands.
 *
 * The detail path keeps the stale-while-revalidate behavior of the LL repository: fresh cache,
 * then network, then the stale cache when the network fails. A Terminus 404 fails the same way
 * an LL 404 does.
 */
class TerminusLaunchRepositoryImpl(
    private val delegate: LaunchRepository,
    private val launchesApi: LaunchesApi,
    private val localDataSource: LaunchLocalDataSource? = null
) : LaunchRepository by delegate {

    private val log = logger()

    override suspend fun getStaleDetailedLaunch(id: String): Launch? =
        localDataSource?.getTerminusDetailedLaunchStale(id)?.toDomain()

    override suspend fun getLaunchDetailDomain(id: String, forceRefresh: Boolean): Result<Launch> =
        getLaunchDetails(id, forceRefresh).mapCatching { it.toDomain() }

    /** A launch Terminus does not know is `success(null)`, as the LL id-filter lookup gives. */
    override suspend fun getLaunchByIdDomain(id: String): Result<Launch?> {
        val result = getLaunchDetails(id, forceRefresh = false)
        if (result.exceptionOrNull() is LaunchNotFoundException) return Result.success(null)
        return result.mapCatching { it.toDomain() }
    }

    private class LaunchNotFoundException(message: String) : Exception(message)

    private suspend fun getLaunchDetails(id: String, forceRefresh: Boolean): Result<LaunchDetailed> {
        return try {
            log.d { "getLaunchDetails (terminus) - id: $id, forceRefresh: $forceRefresh" }

            if (!forceRefresh) {
                localDataSource?.getTerminusDetailedLaunch(id)?.let { cached ->
                    log.i { "Cache hit: fresh cached Terminus launch detail: ${cached.name}" }
                    return Result.success(cached)
                }
            }

            val response = launchesApi.getLaunchDetail(id)
            if (response.status >= 400) {
                val rawResponse = response.response.bodyAsText()
                log.e { "HTTP Error ${response.status}: $rawResponse" }
                // A server error is like a network failure: serve the stale entry when there is one.
                if (response.status >= 500) {
                    localDataSource?.getTerminusDetailedLaunchStale(id)?.let { stale ->
                        log.w { "Returning stale cached Terminus launch detail after HTTP ${response.status}" }
                        return Result.success(stale)
                    }
                }
                val message = "API Error ${response.status}: $rawResponse"
                return Result.failure(
                    if (response.status == 404) LaunchNotFoundException(message) else Exception(message)
                )
            }

            val body = response.body()
            localDataSource?.cacheTerminusDetailedLaunch(body)
            log.i { "API success: fetched and cached Terminus launch detail: ${body.name} (ID: ${body.id})" }
            Result.success(body)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            log.e(e) { "ResponseException in getLaunchDetails (terminus) for ID $id" }
            staleOrFailure(id, e)
        } catch (e: IOException) {
            log.e(e) { "IOException in getLaunchDetails (terminus) for ID $id" }
            staleOrFailure(id, e)
        } catch (e: Exception) {
            log.e(e) { "Exception in getLaunchDetails (terminus) for ID $id: ${e::class.simpleName}" }
            Result.failure(e)
        }
    }

    private suspend fun staleOrFailure(id: String, cause: Exception): Result<LaunchDetailed> {
        val stale = localDataSource?.getTerminusDetailedLaunchStale(id)
        if (stale != null) {
            log.w { "Returning stale cached Terminus launch detail as fallback: ${stale.name}" }
            return Result.success(stale)
        }
        return Result.failure(cause)
    }
}
