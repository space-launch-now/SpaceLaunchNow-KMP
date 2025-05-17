package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Tests for the EnvironmentManager
 */
class EnvironmentManagerTest {
    
    @Test
    fun testGetApiKey() {
        // This test verifies that the API key is properly loaded from .env
        val apiKey = EnvironmentManager.getEnv("API_KEY", "")
        
        // The API key should not be empty if .env is configured properly
        assertNotNull(apiKey)
        assertNotEquals("", apiKey, "API key should not be empty")
        
        // Print a message for informational purposes (will be shown in test logs)
        println("API Key loaded successfully: ${if (apiKey.isNotEmpty()) "YES" else "NO"}")
    }
}
