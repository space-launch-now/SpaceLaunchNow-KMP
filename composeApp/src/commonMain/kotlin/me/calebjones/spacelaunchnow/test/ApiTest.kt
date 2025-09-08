package me.calebjones.spacelaunchnow.test

import kotlinx.coroutines.runBlocking
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

object ApiTest {
    fun runTest() = runBlocking {
        // Create a simple HTTP client to test the API
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
        }
        
        try {
            println("Testing Launch Library API v2.3.1...")
            println("=".repeat(50))
            
            // Test the upcoming launches endpoint with different modes
            val baseUrl = "https://spacelaunchnow.app/api/ll/2.4.0"
            
            // Test with mode=list
            println("\n1. Testing with mode=list:")
            val listResponse = client.get("$baseUrl/launches/upcoming/?limit=3&mode=list")
            println("Status: ${listResponse.status}")
            val listBody = listResponse.bodyAsText()
            println("Response length: ${listBody.length} characters")
            println("First 500 characters:")
            println(listBody.take(500))
            
            // Test with mode=normal  
            println("\n2. Testing with mode=normal:")
            val normalResponse = client.get("$baseUrl/launches/upcoming/?limit=3&mode=normal")
            println("Status: ${normalResponse.status}")
            val normalBody = normalResponse.bodyAsText()
            println("Response length: ${normalBody.length} characters")
            println("First 500 characters:")
            println(normalBody.take(500))
            
            // Parse JSON to see the structure
            println("\n3. Analyzing JSON structure:")
            val json = Json { ignoreUnknownKeys = true }
            val jsonElement = json.parseToJsonElement(listBody)
            
            if (jsonElement is JsonObject) {
                println("Top-level keys: ${jsonElement.keys}")
                
                val results = jsonElement["results"]
                if (results is JsonArray && results.isNotEmpty()) {
                    val firstLaunch = results[0]
                    if (firstLaunch is JsonObject) {
                        println("First launch keys: ${firstLaunch.keys}")
                        println("Response mode: ${firstLaunch["response_mode"]}")
                        
                        // Show some key fields
                        println("Launch ID: ${firstLaunch["id"]}")
                        println("Launch name: ${firstLaunch["name"]}")
                        println("Launch slug: ${firstLaunch["slug"]}")
                    }
                }
            }
            
        } catch (e: Exception) {
            println("Error: ${e.message}")
            e.printStackTrace()
        } finally {
            client.close()
        }
    }
}
