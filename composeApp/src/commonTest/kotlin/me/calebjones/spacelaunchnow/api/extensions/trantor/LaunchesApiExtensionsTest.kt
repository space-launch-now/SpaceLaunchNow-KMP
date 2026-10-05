package me.calebjones.spacelaunchnow.api.extensions.trantor

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Checks the URLs that the Trantor launch extensions build. A time filter sent to the
 * microsecond makes every request a new URL, so no cache can serve it (2026-10-01 incident).
 */
@OptIn(ExperimentalTime::class)
class LaunchesApiExtensionsTest {

    private val requested = mutableListOf<Url>()

    private val api = LaunchesApi(
        baseUrl = "https://trantor.test",
        httpClientEngine = MockEngine { request ->
            requested += request.url
            respond(
                content = """{"count":0,"next":null,"previous":null,"results":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
    )

    // The shape the app sent during the incident.
    private val sent = Instant.parse("2026-10-01T14:14:12.895063Z")

    @Test
    fun `getLaunchList rounds netAfter down and netBefore up`() = runTest {
        api.getLaunchList(limit = 4, netAfter = sent, netBefore = sent + 1.days, ordering = "net")

        val url = requested.single()
        assertEquals("2026-10-01T14:14:00Z", url.parameters["net_after"])
        assertEquals("2026-10-02T14:15:00Z", url.parameters["net_before"])
    }

    @Test
    fun `calls 30 seconds apart in one minute build the same URL`() = runTest {
        val first = Instant.parse("2026-10-01T14:11:00.123456Z")

        listOf(first, first + 30.seconds).forEach { now ->
            api.getLaunchList(limit = 4, netAfter = now, netBefore = now + 1.days, ordering = "net")
        }

        assertEquals(requested[0], requested[1])
    }

    @Test
    fun `null time filters add no time parameters`() = runTest {
        api.getLaunchList(limit = 4)

        val names = requested.single().parameters.names()
        assertFalse("net_after" in names, "unexpected parameters: $names")
        assertFalse("net_before" in names, "unexpected parameters: $names")
    }
}
