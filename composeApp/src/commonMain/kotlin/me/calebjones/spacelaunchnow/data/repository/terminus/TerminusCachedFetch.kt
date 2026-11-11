package me.calebjones.spacelaunchnow.data.repository.terminus

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.time.Clock

/** The Terminus backend answered 404. A caller may turn this into an empty result. */
internal class TerminusNotFoundException(message: String) : Exception(message)

/** A value plus where it came from. [TerminusCacheHit.cachedAt] is null when the cache does not keep it. */
internal class TerminusFetched<T>(val value: T, val source: DataSource, val timestamp: Long)

/** A cached value and the time it was stored, in epoch milliseconds (null when unknown). */
internal class TerminusCacheHit<T>(val value: T, val cachedAt: Long? = null)

private const val MAX_ERROR_BODY_CHARS = 200
private val log get() = SpaceLogger.getLogger("TerminusCachedFetch")

/**
 * Stale-while-revalidate for one Terminus read, shared by launch detail and the launch lists:
 * a fresh cache entry, else the network, else the stale entry when the network fails.
 *
 * A 5xx answer, a transport failure and an I/O failure all fall back to the stale entry. Any
 * other 4xx fails; a 404 fails with [TerminusNotFoundException]. [forceRefresh] skips the fresh
 * read only. Cancellation is never swallowed.
 */
internal suspend fun <T : Any> terminusCachedFetch(
    what: String,
    forceRefresh: Boolean,
    readFresh: suspend () -> TerminusCacheHit<T>?,
    readStale: suspend () -> TerminusCacheHit<T>?,
    fetch: suspend () -> HttpResponse<T>,
    store: suspend (T) -> Unit
): Result<TerminusFetched<T>> {
    suspend fun staleOr(cause: Exception): Result<TerminusFetched<T>> {
        val stale = readStale()
        if (stale != null) {
            log.w { "Returning stale cached $what as fallback: ${cause.message?.take(MAX_ERROR_BODY_CHARS)}" }
            return Result.success(
                TerminusFetched(stale.value, DataSource.STALE_CACHE, stale.cachedAt ?: Clock.System.now().toEpochMilliseconds())
            )
        }
        return Result.failure(cause)
    }

    return try {
        if (!forceRefresh) {
            readFresh()?.let { hit ->
                log.i { "Cache hit: fresh cached $what" }
                return Result.success(
                    TerminusFetched(hit.value, DataSource.CACHE, hit.cachedAt ?: Clock.System.now().toEpochMilliseconds())
                )
            }
        }

        val response = fetch()
        if (response.status >= 400) {
            // Cap the body: an error page can be large and must not fill the log or the message.
            val rawResponse = response.response.bodyAsText().take(MAX_ERROR_BODY_CHARS)
            log.e { "HTTP Error ${response.status} for $what: $rawResponse" }
            val message = "API Error ${response.status}: $rawResponse"
            return when {
                response.status >= 500 -> staleOr(Exception(message))
                response.status == 404 -> Result.failure(TerminusNotFoundException(message))
                else -> Result.failure(Exception(message))
            }
        }

        val body = response.body()
        store(body)
        Result.success(TerminusFetched(body, DataSource.NETWORK, Clock.System.now().toEpochMilliseconds()))
    } catch (e: CancellationException) {
        throw e
    } catch (e: ResponseException) {
        log.e(e) { "ResponseException for $what" }
        staleOr(e)
    } catch (e: IOException) {
        log.e(e) { "IOException for $what" }
        staleOr(e)
    } catch (e: Exception) {
        log.e(e) { "Exception for $what: ${e::class.simpleName}" }
        Result.failure(e)
    }
}
