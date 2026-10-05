package me.calebjones.spacelaunchnow.api.extensions.ll

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class EventsApiExtensionsTest {

    private val requested = mutableListOf<Url>()

    private val api = EventsApi(
        baseUrl = "https://ll.test",
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
    fun `getEventList rounds lower date bounds down and upper bounds up`() = runTest {
        api.getEventList(dateGt = sent, dateGte = sent, dateLt = sent, dateLte = sent)

        val url = requested.single()
        assertEquals("2026-10-01T14:10:00Z", url.parameters["date__gt"])
        assertEquals("2026-10-01T14:10:00Z", url.parameters["date__gte"])
        assertEquals("2026-10-01T14:15:00Z", url.parameters["date__lt"])
        assertEquals("2026-10-01T14:15:00Z", url.parameters["date__lte"])
    }
}
