package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for the UserAgentUtil
 */
class UserAgentUtilTest {

    @Test
    fun testUserAgentGeneration() {
        val userAgent = UserAgentUtil.getUserAgent()

        // Should contain the app name and version
        assertTrue(userAgent.contains("Space"))
        assertTrue(userAgent.contains("v"))

        // Should contain platform information
        assertTrue(userAgent.contains("(") && userAgent.contains(")"))

        println("Generated User Agent: $userAgent")
    }
}
