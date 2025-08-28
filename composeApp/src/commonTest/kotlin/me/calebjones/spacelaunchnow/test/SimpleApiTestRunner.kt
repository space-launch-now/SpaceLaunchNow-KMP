package me.calebjones.spacelaunchnow.test

import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.api.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.models.*
import me.calebjones.spacelaunchnow.api.extensions.getLaunchMiniList
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*
import kotlin.test.Test

/**
 * Test runner for SimpleApiTest - converts the main function into a test
 */
class SimpleApiTestRunner {
    
    @Test
    fun runSimpleApiTest() = runBlocking {
        println("Testing Launch Library API v2.4.0...")
        println("=".repeat(50))

        val baseUrl = "https://spacelaunchnow.app/"
        
        try {
            // Test 1: Try to create the API client
            println("\n1. Creating LaunchesApi client:")
            val launchesApi = LaunchesApi(baseUrl)
            println("✓ LaunchesApi created successfully")
            
            // Test 2: Try different response modes
            println("\n2. Testing different response modes:")
            
            // Test list mode
            println("\n  Testing list:")
            try {
                val listResponse = launchesApi.launchesMiniList(
                    limit = 1
                )
                println("  ✓ List mode request successful: ${listResponse.status}")
                
                // Try to deserialize
                val listBody = listResponse.body()
                println("  ✓ List mode deserialization successful")
                println("  Result count: ${listBody.count}")
                println("  Results size: ${listBody.results.size}")
                if (listBody.results.isNotEmpty()) {
                    println("  First result response_mode: ${listBody.results.first().responseMode}")
                }
            } catch (e: Exception) {
                println("  ✗ List mode failed: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
            }
            
            // Test normal mode
            println("\n  Testing normal:")
            try {
                val normalResponse = launchesApi.launchesList(
                    limit = 1
                )
                println("  ✓ Normal mode request successful: ${normalResponse.status}")
                
                val normalBody = normalResponse.body()
                println("  ✓ Normal mode deserialization successful")
                println("  Result count: ${normalBody.count}")
                if (normalBody.results.isNotEmpty()) {
                    println("  First result response_mode: ${normalBody.results.first().responseMode}")
                }
            } catch (e: Exception) {
                println("  ✗ Normal mode failed: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
            }

        } catch (e: Exception) {
            println("✗ Test failed: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }
    }
}
