package me.calebjones.spacelaunchnow.tests

import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.api.trantor.apis.EventsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LookupsApi
import me.calebjones.spacelaunchnow.util.EnvironmentManager
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the generated Trantor (SpaceLaunchNow-API) OpenAPI client, run
 * against the live staging deployment.
 *
 * Unlike [GeneratedApiClientIntegrationTest] (the LL sibling this was modeled on, which
 * hits the network unconditionally with a hardcoded URL), the base URL here is overridable
 * via the TRANTOR_BASE_URL env var, and the whole class is @Ignore'd by default so a normal
 * `desktopTest`/CI run never depends on network access or staging availability.
 *
 * To run manually: remove/comment the @Ignore below (or run with your test runner's
 * "include ignored" option) and execute, e.g.:
 *   ./gradlew.bat :composeApp:desktopTest --tests "*.TrantorApiClientIntegrationTest"
 */
@Ignore
class TrantorApiClientIntegrationTest {

    private val baseUrl = EnvironmentManager.getEnv(
        "TRANTOR_BASE_URL",
        "https://staging-api.spacelaunchnow.app"
    )

    @Test
    fun testLaunchesListDecodes() = runBlocking {
        val launchesApi = LaunchesApi(baseUrl)

        val response = launchesApi.listLaunchesApiV1LaunchesGet(limit = 5, offset = 0)
        val responseBody = response.body()

        assertNotNull(responseBody, "Response should not be null")
        assertTrue(responseBody.count > 0, "Launches count should be greater than 0")
        assertTrue(responseBody.results.isNotEmpty(), "Should have at least one launch")
    }

    @Test
    fun testLookupsDecodes() = runBlocking {
        val lookupsApi = LookupsApi(baseUrl)

        val response = lookupsApi.getLookupsApiV1LookupsGet()
        val lookups = response.body()

        assertNotNull(lookups, "Lookups response should not be null")
        assertTrue(lookups.launchStatuses.isNotEmpty(), "launch_statuses should not be empty")
        assertTrue(lookups.eventTypes.isNotEmpty(), "event_types should not be empty")
        assertTrue(lookups.orbits.isNotEmpty(), "orbits should not be empty")
        assertTrue(lookups.missionTypes.isNotEmpty(), "mission_types should not be empty")
        assertTrue(lookups.astronautStatuses.isNotEmpty(), "astronaut_statuses should not be empty")
        assertTrue(lookups.agencyTypes.isNotEmpty(), "agency_types should not be empty")
    }

    @Test
    fun testEventsListDecodes() = runBlocking {
        val eventsApi = EventsApi(baseUrl)

        val response = eventsApi.listEventsApiV1EventsGet(limit = 5, offset = 0)
        val responseBody = response.body()

        assertNotNull(responseBody, "Response should not be null")
        assertNotNull(responseBody.results, "Results should not be null")
    }
}
