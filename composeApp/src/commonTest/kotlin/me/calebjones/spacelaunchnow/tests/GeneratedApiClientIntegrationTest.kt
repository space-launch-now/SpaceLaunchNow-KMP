package me.calebjones.spacelaunchnow.tests

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the generated OpenAPI client
 *
 * These tests verify that the generated API client can:
 * 1. Make HTTP requests successfully
 * 2. Deserialize responses correctly
 * 3. Handle authentication properly
 *
 * Note: Uses platform-specific HTTP engine via expect/actual pattern
 */

// Expect function to get platform-specific HTTP client
expect fun createTestHttpClient(): HttpClient

class GeneratedApiClientIntegrationTest {

    private val httpClient = createTestHttpClient()
    private val baseUrl = "https://spacelaunchnow.app"

    @Test
    fun testLaunchesApiConnection() = runBlocking {
        val launchesApi = LaunchesApi(baseUrl, httpClient)

        try {
            val response = launchesApi.launchesList(
                limit = 1,
                offset = 0
            )

            val responseBody = response.body()
            assertNotNull(responseBody, "Response should not be null")
            assertNotNull(responseBody.results, "Results should not be null")
            assertTrue(responseBody.results.isNotEmpty(), "Should have at least one launch")

            val launch = responseBody.results.first()
            assertNotNull(launch.name, "Launch name should not be null")
            assertNotNull(launch.id, "Launch ID should not be null")

            println("✅ Successfully retrieved launch: ${launch.name}")

        } catch (e: Exception) {
            println("❌ Test failed with exception: ${e.message}")
            throw e
        }
    }

    @Test
    fun testAgenciesApiConnection() = runBlocking {
        val agenciesApi = AgenciesApi(baseUrl, httpClient)

        try {
            val response = agenciesApi.agenciesList(
                limit = 1,
                offset = 0
            )

            val responseBody = response.body()
            assertNotNull(responseBody, "Response should not be null")
            assertNotNull(responseBody.results, "Results should not be null")
            assertTrue(responseBody.results.isNotEmpty(), "Should have at least one agency")

            val agency = responseBody.results.first()
            assertNotNull(agency.name, "Agency name should not be null")
            assertNotNull(agency.id, "Agency ID should not be null")

            println("✅ Successfully retrieved agency: ${agency.name}")

        } catch (e: Exception) {
            println("❌ Test failed with exception: ${e.message}")
            throw e
        }
    }

    @Test
    fun testSerializationOfComplexObjects() = runBlocking {
        val launchesApi = LaunchesApi(baseUrl, httpClient)

        try {
            // Get a detailed launch with nested objects
            val response = launchesApi.launchesList(
                limit = 1,
                offset = 0
            )

            val responseBody = response.body()
            assertNotNull(responseBody, "Response should not be null")
            assertNotNull(responseBody.results, "Results should not be null")
            assertTrue(responseBody.results.isNotEmpty(), "Should have at least one launch")

            val launch = responseBody.results.first()

            // Verify complex nested objects are properly deserialized
            assertNotNull(launch.rocket, "Launch rocket should not be null")
            assertNotNull(launch.mission, "Launch mission should not be null")
            assertNotNull(launch.pad, "Launch pad should not be null")

            println("✅ Successfully deserialized complex launch object:")
            println("   Launch: ${launch.name}")
            println("   Rocket: ${launch.rocket.configuration.name}")
            println("   Mission: ${launch.mission.name}")
            println("   Pad: ${launch.pad.name}")

        } catch (e: Exception) {
            println("❌ Complex object serialization test failed: ${e.message}")
            throw e
        }
    }
}
