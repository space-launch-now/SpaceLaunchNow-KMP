package me.calebjones.spacelaunchnow.api.terminus

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import kotlin.test.Test
import kotlin.test.assertTrue

/** Reads an environment variable on the test host. Null when unset (or unsupported). */
internal expect fun terminusTestEnv(name: String): String?

/**
 * Live call against Terminus staging. Skipped unless TERMINUS_INTEGRATION=true so CI and
 * offline runs stay green. Host only: the generated paths already carry /api/v1.
 */
class TerminusIntegrationTest {
    @Test
    fun launchesListReturnsPage() = runTest {
        if (terminusTestEnv("TERMINUS_INTEGRATION") != "true") return@runTest

        val api = LaunchesApi(baseUrl = "https://staging-terminus.spacelaunchnow.app")
        val response = api.launchesList(limit = 1)

        assertTrue(response.success, "Terminus launches list failed: ${response.status}")
        assertTrue(response.body().results.size <= 1)
    }
}
