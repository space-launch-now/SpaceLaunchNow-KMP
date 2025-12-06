package me.calebjones.spacelaunchnow.test

import kotlinx.coroutines.runBlocking

import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.util.EnvironmentManager
import kotlin.test.Test

/**
 * Test runner for SimpleApiTest - converts the main function into a test
 */
class SimpleApiTestRunner {
    private val log = logger()

    @Test
    fun runSimpleApiTest() = runBlocking {
        log.i { "Testing Launch Library API v2.4.0..." }
        log.i { "=".repeat(50) }

        val baseUrl = "https://spacelaunchnow.app/"
        val apiKey = EnvironmentManager.getEnv("API_KEY", "")

        try {
            // Test 1: Try to create the API client
            log.i { "\n1. Creating LaunchesApi client:" }
            val launchesApi = LaunchesApi(baseUrl).apply {
                // Configure API authentication
                setApiKey(apiKey, "Authorization")
                setApiKeyPrefix("Token", "Authorization")
            }
            log.i { "✓ LaunchesApi created successfully" }
            log.i { "✓ API Key configured: ${if (apiKey.isNotEmpty()) "Yes" else "No (using default)"}" }

            // Test 2: Try different response modes
            log.i { "\n2. Testing different response modes:" }

            // Test list mode
            log.i { "\n  Testing list:" }
            try {
                val listResponse = launchesApi.launchesMiniList(
                    limit = 1
                )
                log.i { "  ✓ List mode request successful: ${listResponse.status}" }

                // Try to deserialize
                val listBody = listResponse.body()
                log.i { "  ✓ List mode deserialization successful" }
                log.i { "  Result count: ${listBody.count}" }
                log.i { "  Results size: ${listBody.results.size}" }
                if (listBody.results.isNotEmpty()) {
                    log.i { "  First result response_mode: ${listBody.results.first().responseMode}" }
                }
            } catch (e: Exception) {
                log.e(e) { "  ✗ List mode failed" }
            }

            // Test normal mode
            log.i { "\n  Testing normal:" }
            try {
                val normalResponse = launchesApi.launchesList(
                    limit = 1
                )
                log.i { "  ✓ Normal mode request successful: ${normalResponse.status}" }

                val normalBody = normalResponse.body()
                log.i { "  ✓ Normal mode deserialization successful" }
                log.i { "  Result count: ${normalBody.count}" }
                if (normalBody.results.isNotEmpty()) {
                    log.i { "  First result response_mode: ${normalBody.results.first().responseMode}" }
                }
            } catch (e: Exception) {
                log.e(e) { "  ✗ Normal mode failed" }
            }

        } catch (e: Exception) {
            log.e(e) { "✗ Test failed" }
        }
    }
}
