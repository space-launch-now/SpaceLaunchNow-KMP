package me.calebjones.spacelaunchnow.data.repository.terminus

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource.CachedValue
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.time.Clock

/** The Terminus backend answered 404. A caller may turn this into an empty result. */
internal class TerminusNotFoundException(message: String) : Exception(message)

/** A value plus where it came from. [CachedValue.cachedAt] is null when the cache does not keep it. */
internal class TerminusFetched<T>(val value: T, val source: DataSource, val timestamp: Long)

/** A Terminus read that answered 4xx or 5xx. [terminusCachedFetchBody] maps it like an HTTP error answer. */
internal class TerminusHttpStatusException(val status: Int, val body: String) : Exception("API Error $status: $body")

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
    readFresh: suspend () -> CachedValue<T>?,
    readStale: suspend () -> CachedValue<T>?,
    fetch: suspend () -> HttpResponse<T>,
    store: suspend (T) -> Unit
): Result<TerminusFetched<T>> = terminusCachedFetchBody(
    what = what,
    forceRefresh = forceRefresh,
    readFresh = readFresh,
    readStale = readStale,
    fetch = { fetch().bodyOrThrow() },
    store = store
)

/**
 * The body of this response, or a [TerminusHttpStatusException] for a 4xx or 5xx answer. The error
 * text is capped: an error page can be large and must not fill the log or the message.
 */
internal suspend fun <T : Any> HttpResponse<T>.bodyOrThrow(): T {
    if (status >= 400) throw TerminusHttpStatusException(status, response.bodyAsText().take(MAX_ERROR_BODY_CHARS))
    return body()
}

/**
 * [terminusCachedFetch] for a read that is more than one request. [fetch] returns the value or
 * throws; a [TerminusHttpStatusException] from it (see [bodyOrThrow]) is handled as an HTTP error,
 * so a read made of several requests fails and falls back to the stale entry like a single one.
 */
internal suspend fun <T : Any> terminusCachedFetchBody(
    what: String,
    forceRefresh: Boolean,
    readFresh: suspend () -> CachedValue<T>?,
    readStale: suspend () -> CachedValue<T>?,
    fetch: suspend () -> T,
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

        val body = fetch()
        store(body)
        Result.success(TerminusFetched(body, DataSource.NETWORK, Clock.System.now().toEpochMilliseconds()))
    } catch (e: CancellationException) {
        throw e
    } catch (e: TerminusHttpStatusException) {
        log.e { "HTTP Error ${e.status} for $what: ${e.body}" }
        when {
            e.status >= 500 -> staleOr(Exception(e.message))
            e.status == 404 -> Result.failure(TerminusNotFoundException(e.message ?: ""))
            else -> Result.failure(Exception(e.message))
        }
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

/** [terminusCachedFetch] over a [TerminusMemoryCache]: read fresh and stale by [key], store on success. */
internal suspend fun <T : Any> terminusMemoryCachedFetch(
    cache: TerminusMemoryCache,
    key: String,
    what: String,
    forceRefresh: Boolean,
    fetch: suspend () -> HttpResponse<T>
): Result<TerminusFetched<T>> = terminusCachedFetch(
    what = what,
    forceRefresh = forceRefresh,
    readFresh = { cache.fresh(key) },
    readStale = { cache.stale(key) },
    fetch = fetch,
    store = { cache.put(key, it) }
)
