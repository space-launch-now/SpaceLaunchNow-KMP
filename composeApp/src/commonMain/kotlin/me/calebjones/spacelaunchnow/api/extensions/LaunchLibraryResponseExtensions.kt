package me.calebjones.spacelaunchnow.api.extensions

import io.ktor.client.plugins.ResponseException
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse

/**
 * Ktor is configured with the default `expectSuccess = false`, so a non-2xx LL2 response never
 * throws on its own — deserializing its `text/html` error page fails with
 * `NoTransformationFoundException`, which lands in the catch-all arm and skips the stale-cache
 * fallback. Raise `ResponseException` up front so server errors take the same recovery path as
 * any other API error. Mirrors the SNAPI fix from #190.
 */
public suspend fun <T : Any> HttpResponse<T>.bodyOrThrow(operation: String): T {
    if (!success) {
        throw ResponseException(response, "LL2 $operation failed: HTTP $status")
    }
    return body()
}
