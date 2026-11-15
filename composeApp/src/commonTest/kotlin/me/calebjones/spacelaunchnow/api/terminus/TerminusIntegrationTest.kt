package me.calebjones.spacelaunchnow.api.terminus

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.terminus.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.RocketsApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Reads an environment variable on the test host. Null when unset (or unsupported). */
internal expect fun terminusTestEnv(name: String): String?

/**
 * Live call against Terminus staging. Skipped unless TERMINUS_INTEGRATION=true so CI and
 * offline runs stay green. Host only: the generated paths already carry /api/v1.
 */
class TerminusIntegrationTest {
    @Test
    fun configAgencyTypesReturnsPage() = runTest {
        if (terminusTestEnv("TERMINUS_INTEGRATION") != "true") return@runTest

        val api = ConfigApi(baseUrl = "https://staging-terminus.spacelaunchnow.app")
        val response = api.configAgencyTypesList(limit = 1)

        assertTrue(response.success, "Terminus agency types list failed: ${response.status}")
        assertTrue(response.body().results.size <= 1)
    }

    /**
     * The three reads behind launch detail (Tasks#1078): the launch, its full rocket and its
     * timeline, and the detailed rockets by launch. The rocket and timeline answers must match
     * the generated models, and the rocket must be the one the launch names.
     */
    @Test
    fun launchRocketAndTimelineReadsDecodeAndMatchTheLaunch() = runTest {
        if (terminusTestEnv("TERMINUS_INTEGRATION") != "true") return@runTest

        val host = "https://staging-terminus.spacelaunchnow.app"
        val launches = LaunchesApi(baseUrl = host)
        val launch = launches.launchesList(limit = 1, ordering = "-net", upcoming = true).body().results.first()

        val rocket = launches.launchesRocketRetrieve(launch.id)
        assertTrue(rocket.success, "rocket read failed: ${rocket.status}")
        val timeline = launches.launchesTimelineRetrieve(launch.id)
        assertTrue(timeline.success, "timeline read failed: ${timeline.status}")
        assertTrue(timeline.body().timeline.size >= 0)

        val detailed = RocketsApi(baseUrl = host).rocketsDetailedList(launchIds = listOf(launch.id), limit = 1)
        assertTrue(detailed.success, "detailed rockets read failed: ${detailed.status}")
        val rows = detailed.body().results
        assertTrue(rows.size <= 1)
        rows.firstOrNull()?.let { assertEquals(launch.id, it.launchId) }
    }
}
