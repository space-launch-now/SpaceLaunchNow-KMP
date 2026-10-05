package me.calebjones.spacelaunchnow.api.extensions.trantor

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.trantor.apis.EventsApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class EventsApiExtensionsTest {

    private val requested = mutableListOf<Url>()

    private val api = EventsApi(
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

    private val sent = Instant.parse("2026-10-01T14:14:12.895063Z")

    @Test
    fun `getEventList rounds dateAfter down and dateBefore up`() = runTest {
        api.getEventList(dateAfter = sent, dateBefore = sent)

        val url = requested.single()
        assertEquals("2026-10-01T14:14:00Z", url.parameters["date_after"])
        assertEquals("2026-10-01T14:15:00Z", url.parameters["date_before"])
    }
}
