package me.calebjones.spacelaunchnow.api.extensions

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse as KtorHttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse

/**
 * A non-2xx LL2 response, reported without the request URL.
 *
 * Ktor's own [ResponseException] builds its message as
 * `"Bad response: $response. Text: \"$cachedResponseText\""`, and `HttpResponse.toString()`
 * expands to `"HttpResponse[$url, $status]"` — so the default message carries the full request
 * URL, including the user's agency/location filter IDs and date windows. Error-level logs are
 * forwarded to Crashlytics as non-fatals (see `FirebaseCrashlyticsLogWriter`), so that URL would
 * become telemetry. Overriding [message] keeps the operation and status (which is what triages
 * the crash) and drops the query string. [response] is still available to any handler that
 * genuinely needs it.
 */
public class LL2ResponseException(
    response: KtorHttpResponse,
    operation: String,
    status: Int,
) : ResponseException(response, "LL2 $operation failed: HTTP $status") {
    override val message: String = "LL2 $operation failed: HTTP $status"
}

/**
 * Ktor is configured with the default `expectSuccess = false`, so a non-2xx LL2 response never
 * throws on its own — deserializing its `text/html` error page fails with
 * `NoTransformationFoundException`, which lands in the catch-all arm and skips the stale-cache
 * fallback. Raise [LL2ResponseException] (a [ResponseException]) up front so server errors take
 * the same recovery path as any other API error. Mirrors the SNAPI fix from #190.
 */
public suspend fun <T : Any> HttpResponse<T>.bodyOrThrow(operation: String): T {
    if (!success) {
        throw LL2ResponseException(response, operation, status)
    }
    return body()
}
