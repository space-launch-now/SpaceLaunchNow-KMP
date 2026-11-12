package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log get() = SpaceLogger.getLogger("TerminusRead")

/**
 * One cached Terminus read for the vehicle repositories: logs [what], then serves [key] from this
 * cache, falling back to [fetch] and to stale data on a failure (see [terminusMemoryCachedFetch]).
 * Never forces a refresh.
 */
internal suspend fun <T : Any> TerminusMemoryCache.terminusRead(
    key: String,
    what: String,
    fetch: suspend () -> HttpResponse<T>
): Result<TerminusFetched<T>> {
    log.d { "terminus read $what" }
    return terminusMemoryCachedFetch(this, key, what, forceRefresh = false, fetch = fetch)
}
