package me.calebjones.spacelaunchnow.tests

import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.api.extensions.getLaunchList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.util.EnvironmentManager
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the bidirectional launch carousel feature
 *
 * These tests verify that:
 * 1. Previous launches can be fetched from the API
 * 2. Upcoming launches can be fetched from the API
 * 3. The combined list logic works as expected
 */
class BidirectionalCarouselIntegrationTest {

    private val baseUrl = "https://spacelaunchnow.app"
    private val apiKey = EnvironmentManager.getEnv("API_KEY", "")

    @Test
    fun testFetchPreviousLaunches() = runBlocking {
        val launchesApi = LaunchesApi(baseUrl).apply {
            setApiKey(apiKey, "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }

        try {
            val response = launchesApi.getLaunchList(
                limit = 5,
                previous = true,
                ordering = "-net"
            )

            val responseBody = response.body()
            assertNotNull(responseBody, "Response should not be null")
            assertNotNull(responseBody.results, "Results should not be null")
            assertTrue(responseBody.results.isNotEmpty(), "Should have at least one previous launch")

            val firstLaunch = responseBody.results.first()
            assertNotNull(firstLaunch.name, "Launch name should not be null")
            assertNotNull(firstLaunch.id, "Launch ID should not be null")

            println("✅ Successfully retrieved ${responseBody.results.size} previous launches")
            println("   Most recent: ${firstLaunch.name}")

        } catch (e: Exception) {
            println("❌ Previous launches test failed: ${e.message}")
            throw e
        }
    }

    @Test
    fun testFetchUpcomingLaunches() = runBlocking {
        val launchesApi = LaunchesApi(baseUrl).apply {
            setApiKey(apiKey, "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }

        try {
            val response = launchesApi.getLaunchList(
                limit = 10,
                upcoming = true,
                ordering = "net"
            )

            val responseBody = response.body()
            assertNotNull(responseBody, "Response should not be null")
            assertNotNull(responseBody.results, "Results should not be null")
            assertTrue(responseBody.results.isNotEmpty(), "Should have at least one upcoming launch")

            val firstLaunch = responseBody.results.first()
            assertNotNull(firstLaunch.name, "Launch name should not be null")
            assertNotNull(firstLaunch.id, "Launch ID should not be null")

            println("✅ Successfully retrieved ${responseBody.results.size} upcoming launches")
            println("   Next upcoming: ${firstLaunch.name}")

        } catch (e: Exception) {
            println("❌ Upcoming launches test failed: ${e.message}")
            throw e
        }
    }

    @Test
    fun testCombinedLaunchesLogic() = runBlocking {
        val launchesApi = LaunchesApi(baseUrl).apply {
            setApiKey(apiKey, "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }

        try {
            // Fetch both previous and upcoming launches
            val previousResponse = launchesApi.getLaunchList(
                limit = 5,
                previous = true,
                ordering = "-net"
            )

            val upcomingResponse = launchesApi.getLaunchList(
                limit = 10,
                upcoming = true,
                ordering = "net"
            )

            val previousLaunches = previousResponse.body().results
            val upcomingLaunches = upcomingResponse.body().results

            // Simulate the combining logic from HomeViewModel
            val previousReversed = previousLaunches.reversed()
            val combinedLaunches = previousReversed + upcomingLaunches
            val upcomingStartIndex = previousReversed.size

            // Verify the combined list structure
            assertTrue(combinedLaunches.size == previousLaunches.size + upcomingLaunches.size,
                "Combined list should contain all launches")
            assertTrue(upcomingStartIndex == previousReversed.size,
                "Upcoming start index should equal previous launches count")
            assertTrue(upcomingStartIndex > 0, "Should have previous launches")
            assertTrue(combinedLaunches.size > upcomingStartIndex, "Should have upcoming launches")

            println("✅ Combined launches logic verified:")
            println("   Previous: ${previousLaunches.size}")
            println("   Upcoming: ${upcomingLaunches.size}")
            println("   Combined: ${combinedLaunches.size}")
            println("   Upcoming start index: $upcomingStartIndex")

        } catch (e: Exception) {
            println("❌ Combined launches test failed: ${e.message}")
            throw e
        }
    }
}
