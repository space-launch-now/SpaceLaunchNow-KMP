package me.calebjones.spacelaunchnow.api.extensions

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Checks the URLs that the launch extensions build. A time filter sent to the microsecond
 * makes every request a new URL, so no cache can serve it (2026-10-01 incident).
 */
@OptIn(ExperimentalTime::class)
class LaunchesApiExtensionsTest {

    private val requested = mutableListOf<Url>()

    private val api = LaunchesApi(
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

    // The shape the app sent during the incident.
    private val sent = Instant.parse("2026-10-01T14:14:12.895063Z")

    @Test
    fun `getLaunchList rounds netGt down and netLt up`() = runTest {
        api.getLaunchList(limit = 4, netGt = sent, netLt = sent + 1.days, ordering = "net")

        val url = requested.single()
        assertEquals("2026-10-01T14:14:00Z", url.parameters["net__gt"])
        assertEquals("2026-10-02T14:15:00Z", url.parameters["net__lt"])
    }

    @Test
    fun `getLaunchMiniList rounds netGt down and netLt up`() = runTest {
        api.getLaunchMiniList(limit = 1, upcoming = true, netGt = sent, netLt = sent + 1.days)

        val url = requested.single()
        assertEquals("2026-10-01T14:14:00Z", url.parameters["net__gt"])
        assertEquals("2026-10-02T14:15:00Z", url.parameters["net__lt"])
    }

    @Test
    fun `featured launch calls 30 seconds apart in one bucket build the same URL`() = runTest {
        val first = Instant.parse("2026-10-01T14:11:00.123456Z")

        listOf(first, first + 30.seconds).forEach { now ->
            api.getLaunchList(
                limit = 4,
                netGt = now,
                ordering = "net",
                lspId = listOf(121),
                locationIds = listOf(11, 27, 12),
                statusIds = listOf(1, 3, 4, 5, 6, 7, 9)
            )
        }

        assertEquals(requested[0], requested[1])
    }

    @Test
    fun `count calls 30 seconds apart in one bucket build the same URL`() = runTest {
        val first = Instant.parse("2026-10-01T14:11:00.123456Z")

        listOf(first, first + 30.seconds).forEach { now ->
            api.getLaunchMiniList(limit = 1, upcoming = true, netGt = now, netLt = now + 1.days)
        }

        assertEquals(requested[0], requested[1])
    }

    @Test
    fun `getLaunchList sorts and de-duplicates id filters`() = runTest {
        api.getLaunchList(
            lspId = listOf(121, 44, 121),
            locationIds = listOf(143, 11, 27, 12, 11),
            statusIds = listOf(9, 1, 3)
        )

        val url = requested.single()
        assertEquals("44,121", url.parameters["lsp__id"])
        assertEquals("11,12,27,143", url.parameters["location__ids"])
        assertEquals("1,3,9", url.parameters["status__ids"])
    }

    @Test
    fun `equal filter sets in any order build the same URL`() = runTest {
        api.getLaunchList(
            lspId = listOf(44, 121),
            relatedLspId = listOf(1, 2),
            statusIds = listOf(1, 3),
            locationIds = listOf(11, 12, 27, 143),
            program = listOf(1, 25),
            orbitIds = listOf(8, 9),
            missionTypeIds = listOf(3, 7),
            launcherConfigFamilyIds = listOf(1, 5)
        )
        api.getLaunchList(
            lspId = listOf(121, 44),
            relatedLspId = listOf(2, 1, 2),
            statusIds = listOf(3, 1),
            locationIds = listOf(143, 27, 12, 11),
            program = listOf(25, 1),
            orbitIds = listOf(9, 8),
            missionTypeIds = listOf(7, 3),
            launcherConfigFamilyIds = listOf(5, 1, 5)
        )
        api.getLaunchMiniList(
            lspId = listOf(44, 121),
            locationIds = listOf(11, 12, 27, 143),
            statusIds = listOf(1, 3),
            program = listOf(1, 25)
        )
        api.getLaunchMiniList(
            lspId = listOf(121, 44, 44),
            locationIds = listOf(27, 143, 11, 12),
            statusIds = listOf(3, 1),
            program = listOf(25, 1)
        )

        assertEquals(requested[0], requested[1])
        assertEquals(requested[2], requested[3])
    }

    @Test
    fun `null filters add no parameters`() = runTest {
        api.getLaunchList(limit = 4)

        val names = requested.single().parameters.names()
        assertEquals(setOf("limit"), names, "unexpected parameters: $names")
    }

    @Test
    fun `starship and by-id helpers still send their own filters`() = runTest {
        api.getStarshipLaunches()
        api.getLaunchById("f059c3d4-1f8a-4d6c-9a9e-1d6b0c3c1b2a")

        assertEquals(listOf("1"), requested[0].parameters.getAll("program"))
        assertTrue(requested[1].parameters["id"]!!.contains("f059c3d4"))
    }
}
