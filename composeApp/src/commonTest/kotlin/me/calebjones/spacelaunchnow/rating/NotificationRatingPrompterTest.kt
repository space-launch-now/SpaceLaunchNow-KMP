package me.calebjones.spacelaunchnow.rating

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationRatingPrompterTest {

    private class RecordingAnalytics : AnalyticsManager {
        val events = mutableListOf<AnalyticsEvent>()
        override fun track(event: AnalyticsEvent) { events += event }
        override fun trackScreenView(screenName: String, screenClass: String?) = Unit
        override fun setUserId(userId: String?) = Unit
        override fun setUserProperty(key: String, value: String) = Unit
        override suspend fun flush() = Unit
        override fun reset() = Unit
        override fun enableProvider(name: String, enabled: Boolean) = Unit
    }

    private class Fixture(
        var launchCount: Long = 5,
        var lastPrompt: Long? = null,
        var rated: Boolean = false,
        val now: Long = 1_800_000_000_000L,
    ) {
        val config = MockRemoteConfigRepository().apply { notificationRatingPromptEnabled = true }
        val analytics = RecordingAnalytics()
        val prompter = NotificationRatingPrompter(
            launchCount = { launchCount },
            lastPromptAtMs = { lastPrompt },
            hasRated = { rated },
            saveLastPromptAtMs = { lastPrompt = it },
            flagEnabled = { config.getNotificationRatingPromptEnabled() },
            nowMs = { now },
            analytics = analytics,
        )
    }

    @Test
    fun `flag off gives no prompt and no event`() = runTest {
        val f = Fixture()
        f.config.notificationRatingPromptEnabled = false
        f.prompter.onOpenedFromNotification("L1")
        f.prompter.onLaunchLoaded("L1", 1)
        assertFalse(f.prompter.promptRequest.value)
        assertTrue(f.analytics.events.isEmpty())
        assertNull(f.lastPrompt)
    }

    @Test
    fun `allow gives one event, one request and saves cooldown`() = runTest {
        val f = Fixture()
        f.prompter.onOpenedFromNotification("L1")
        f.prompter.onLaunchLoaded("L1", 3)
        assertTrue(f.prompter.promptRequest.value)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.RatingPromptRequested("notification", "L1")), f.analytics.events)
        assertEquals(f.now, f.lastPrompt)
        f.prompter.consume()
        assertFalse(f.prompter.promptRequest.value)
    }

    @Test
    fun `non matching launch id gives nothing and keeps the pending id`() = runTest {
        val f = Fixture()
        f.prompter.onOpenedFromNotification("L1")
        f.prompter.onLaunchLoaded("OTHER", 1)
        assertFalse(f.prompter.promptRequest.value)
        assertTrue(f.analytics.events.isEmpty())
        f.prompter.onLaunchLoaded("L1", 1)
        assertTrue(f.prompter.promptRequest.value)
    }

    @Test
    fun `no notification opened gives nothing`() = runTest {
        val f = Fixture()
        f.prompter.onLaunchLoaded("L1", 1)
        assertFalse(f.prompter.promptRequest.value)
        assertTrue(f.analytics.events.isEmpty())
    }

    @Test
    fun `failure status gives nothing`() = runTest {
        val f = Fixture()
        f.prompter.onOpenedFromNotification("L1")
        f.prompter.onLaunchLoaded("L1", 4)
        assertFalse(f.prompter.promptRequest.value)
        assertTrue(f.analytics.events.isEmpty())
        assertNull(f.lastPrompt)
    }

    @Test
    fun `second load gives nothing`() = runTest {
        val f = Fixture()
        f.prompter.onOpenedFromNotification("L1")
        f.prompter.onLaunchLoaded("L1", 1)
        f.prompter.consume()
        f.prompter.onLaunchLoaded("L1", 1)
        assertFalse(f.prompter.promptRequest.value)
        assertEquals(1, f.analytics.events.size)
    }

    @Test
    fun `blocked first evaluation clears pending so a later load does not prompt`() = runTest {
        val f = Fixture()
        f.prompter.onOpenedFromNotification("L1")
        f.prompter.onLaunchLoaded("L1", 4)
        f.prompter.onLaunchLoaded("L1", 1)
        assertFalse(f.prompter.promptRequest.value)
        assertTrue(f.analytics.events.isEmpty())
    }

    @Test
    fun `first session gives nothing`() = runTest {
        val f = Fixture(launchCount = 1)
        f.prompter.onOpenedFromNotification("L1")
        f.prompter.onLaunchLoaded("L1", 1)
        assertFalse(f.prompter.promptRequest.value)
        assertTrue(f.analytics.events.isEmpty())
    }

    @Test
    fun `recent prompt and already rated give nothing`() = runTest {
        val recent = Fixture(lastPrompt = 1_800_000_000_000L - 10L * 24 * 60 * 60 * 1000)
        recent.prompter.onOpenedFromNotification("L1")
        recent.prompter.onLaunchLoaded("L1", 1)
        assertFalse(recent.prompter.promptRequest.value)

        val rated = Fixture(rated = true)
        rated.prompter.onOpenedFromNotification("L1")
        rated.prompter.onLaunchLoaded("L1", 1)
        assertFalse(rated.prompter.promptRequest.value)
    }
}
