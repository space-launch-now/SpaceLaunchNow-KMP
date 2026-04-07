package me.calebjones.spacelaunchnow.analytics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.CoroutineScope
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsManagerImplTest {

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
    }

    @Test
    fun `track dispatches event to all enabled providers`() = runTest {
        val p1 = FakeAnalyticsProvider("p1")
        val p2 = FakeAnalyticsProvider("p2")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AnalyticsManagerImpl(listOf(p1, p2), scope = CoroutineScope(SupervisorJob() + dispatcher))

        val event = AnalyticsEvent.LaunchViewed("id-1", "Falcon 9")
        manager.track(event)
        advanceUntilIdle()

        assertEquals(1, p1.trackedEvents.size)
        assertEquals(event, p1.trackedEvents[0])
        assertEquals(1, p2.trackedEvents.size)
        assertEquals(event, p2.trackedEvents[0])
    }

    @Test
    fun `track skips disabled providers`() = runTest {
        val active = FakeAnalyticsProvider("active", isEnabled = true)
        val inactive = FakeAnalyticsProvider("inactive", isEnabled = false)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AnalyticsManagerImpl(listOf(active, inactive), scope = CoroutineScope(SupervisorJob() + dispatcher))

        manager.track(AnalyticsEvent.AppOpened())
        advanceUntilIdle()

        assertEquals(1, active.trackedEvents.size)
        assertEquals(0, inactive.trackedEvents.size)
    }

    @Test
    fun `track survives a throwing provider and still delivers to others`() = runTest {
        val throwing = FakeAnalyticsProvider("throwing", throwOnTrack = true)
        val good = FakeAnalyticsProvider("good")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AnalyticsManagerImpl(listOf(throwing, good), scope = CoroutineScope(SupervisorJob() + dispatcher))

        manager.track(AnalyticsEvent.AppOpened())
        advanceUntilIdle()

        // good provider still received the event
        assertEquals(1, good.trackedEvents.size)
    }

    @Test
    fun `setUserId propagates to all providers`() = runTest {
        val p1 = FakeAnalyticsProvider("p1")
        val p2 = FakeAnalyticsProvider("p2")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AnalyticsManagerImpl(listOf(p1, p2), scope = CoroutineScope(SupervisorJob() + dispatcher))

        manager.setUserId("user-123")
        advanceUntilIdle()

        assertEquals("user-123", p1.lastUserId)
        assertEquals("user-123", p2.lastUserId)
    }

    @Test
    fun `reset propagates to all providers`() = runTest {
        val p1 = FakeAnalyticsProvider("p1")
        val p2 = FakeAnalyticsProvider("p2")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AnalyticsManagerImpl(listOf(p1, p2), scope = CoroutineScope(SupervisorJob() + dispatcher))

        manager.reset()
        advanceUntilIdle()

        assertTrue(p1.reset)
        assertTrue(p2.reset)
    }

    @Test
    fun `enableProvider toggles the correct provider`() = runTest {
        val p1 = FakeAnalyticsProvider("p1", isEnabled = true)
        val p2 = FakeAnalyticsProvider("p2", isEnabled = true)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AnalyticsManagerImpl(listOf(p1, p2), scope = CoroutineScope(SupervisorJob() + dispatcher))

        manager.enableProvider("p1", false)

        assertFalse(p1.isEnabled)
        assertTrue(p2.isEnabled)

        manager.enableProvider("p1", true)
        assertTrue(p1.isEnabled)
    }

    @Test
    fun `trackScreenView dispatches to all enabled providers`() = runTest {
        val p1 = FakeAnalyticsProvider("p1")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = AnalyticsManagerImpl(listOf(p1), scope = CoroutineScope(SupervisorJob() + dispatcher))

        manager.trackScreenView("Home", "HomeRoute")
        advanceUntilIdle()

        assertEquals(1, p1.trackedScreens.size)
        assertEquals("Home" to "HomeRoute", p1.trackedScreens[0])
    }
}

