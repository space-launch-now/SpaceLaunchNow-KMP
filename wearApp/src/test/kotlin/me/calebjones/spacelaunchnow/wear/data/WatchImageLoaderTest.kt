package me.calebjones.spacelaunchnow.wear.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Covers the HTTP client that Coil's Ktor fetcher uses. Coil's fetcher itself needs a real
 * Android Looper, so the wiring in [createWatchImageLoader] is checked on a device.
 */
class WatchImageLoaderTest {

    @Test
    fun imageRequestsCarryUserAgentAndNoJsonAccept() = runTest {
        val agent = "SpaceLaunchNow-v5.50.2-b113 (Android 34)"
        val engine = MockEngine { respondOk() }
        val client = createWatchImageHttpClient(engine, userAgent = agent)

        client.get("https://example.com/launch.png")

        val headers = engine.requestHistory.single().headers
        assertEquals(agent, headers[HttpHeaders.UserAgent])
        assertNotEquals("application/json", headers[HttpHeaders.Accept])
    }
}
