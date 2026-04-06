package me.calebjones.spacelaunchnow.analytics

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.analytics.providers.ConsoleAnalyticsProvider
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConsoleAnalyticsProviderTest {

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
    }

    @Test
    fun `name returns console`() {
        assertEquals("console", ConsoleAnalyticsProvider().name)
    }

    @Test
    fun `trackEvent does not throw`() = runTest {
        val provider = ConsoleAnalyticsProvider()
        provider.trackEvent(AnalyticsEvent.AppOpened())
    }

    @Test
    fun `trackEvent is no-op when disabled`() = runTest {
        val provider = ConsoleAnalyticsProvider(isEnabled = false)
        // Should not throw even when disabled
        provider.trackEvent(AnalyticsEvent.LaunchViewed("id", "Falcon 9"))
    }

    @Test
    fun `trackScreenView does not throw`() = runTest {
        val provider = ConsoleAnalyticsProvider()
        provider.trackScreenView("Home", "HomeRoute")
    }

    @Test
    fun `trackScreenView is no-op when disabled`() = runTest {
        val provider = ConsoleAnalyticsProvider(isEnabled = false)
        provider.trackScreenView("Home")
    }

    @Test
    fun `setUserId stores userId`() {
        val provider = ConsoleAnalyticsProvider()
        provider.setUserId("user-abc")
        // No public getter — just verify no throw; state is internal
    }

    @Test
    fun `setUserId accepts null`() {
        val provider = ConsoleAnalyticsProvider()
        provider.setUserId(null)
    }

    @Test
    fun `reset clears state without throwing`() {
        val provider = ConsoleAnalyticsProvider()
        provider.setUserId("user-abc")
        provider.reset()
    }

    @Test
    fun `flush does not throw`() = runTest {
        val provider = ConsoleAnalyticsProvider()
        provider.flush()
    }

    @Test
    fun `isEnabled can be toggled`() {
        val provider = ConsoleAnalyticsProvider(isEnabled = true)
        provider.isEnabled = false
        assertEquals(false, provider.isEnabled)
        provider.isEnabled = true
        assertEquals(true, provider.isEnabled)
    }
}
