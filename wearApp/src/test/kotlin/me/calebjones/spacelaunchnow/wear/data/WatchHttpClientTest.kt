package me.calebjones.spacelaunchnow.wear.data

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WatchHttpClientTest {

    @Test
    fun userAgentMatchesPhoneFormat() {
        val ua = buildWatchUserAgent(versionName = "5.50.2-b113", sdkInt = 34)
        assertEquals("SpaceLaunchNow-v5.50.2-b113 (Android 34)", ua)
        assertTrue(Regex("""^SpaceLaunchNow-v\d+\.\d+\.\d+-b\d+ \(Android \d+\)$""").matches(ua), ua)
    }

    @Test
    fun defaultVersionNameCarriesBuildSuffix() {
        val ua = buildWatchUserAgent(sdkInt = 34)
        assertTrue(Regex("""^SpaceLaunchNow-v\d+\.\d+\.\d+-b\d+ \(Android 34\)$""").matches(ua), ua)
    }

    @Test
    fun requestsCarryUserAgentHeader() = runTest {
        val engine = MockEngine { respondOk() }
        val client = createWatchHttpClient(engine, userAgent = "SpaceLaunchNow-v5.50.2-b113 (Android 34)")
        client.get("https://terminus.spacelaunchnow.app/api/v1/launches/")
        assertEquals(
            "SpaceLaunchNow-v5.50.2-b113 (Android 34)",
            engine.requestHistory.single().headers[HttpHeaders.UserAgent],
        )
    }
}
