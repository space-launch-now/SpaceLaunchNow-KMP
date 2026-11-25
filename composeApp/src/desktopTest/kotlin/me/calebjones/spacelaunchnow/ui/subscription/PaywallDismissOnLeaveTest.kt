package me.calebjones.spacelaunchnow.ui.subscription

import me.calebjones.spacelaunchnow.test.TestMain
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import me.calebjones.spacelaunchnow.analytics.FakeAnalyticsProvider
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.data.billing.MockBillingManager
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.MockSubscriptionRepository
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The iOS swipe-back pops the screen out of composition; the desktop window close disposes the
 * whole composition. Both end in the same disposal, which [DismissOnLeaveComposition] reports.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalComposeUiApi::class, ExperimentalCoroutinesApi::class)
class PaywallDismissOnLeaveTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: MockSubscriptionRepository
    private lateinit var fake: FakeAnalyticsProvider
    private lateinit var viewModel: SubscriptionViewModel

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
        TestMain.install(dispatcher)
        repository = MockSubscriptionRepository()
        fake = FakeAnalyticsProvider()
        val analytics = AnalyticsManagerImpl(listOf(fake), scope = CoroutineScope(SupervisorJob() + dispatcher))
        viewModel = SubscriptionViewModel(repository, MockBillingManager(), analytics)
    }

    @AfterTest
    fun tearDown() {
        TestMain.reset()
    }

    private fun dismissed() = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PaywallDismissed>()

    @Test
    fun `swipe-back removing the screen fires paywall_dismissed once`() = runComposeUiTest {
        val tracker = PaywallDismissTracker(viewModel, "support_us")
        var shown by mutableStateOf(true)
        setContent { if (shown) DismissOnLeaveComposition(tracker) }

        assertTrue(dismissed().isEmpty())
        shown = false
        waitForIdle()

        val event = dismissed().single()
        assertEquals("support_us", event.source)
    }

    @Test
    fun `window close disposing the composition fires paywall_dismissed once`() {
        val tracker = PaywallDismissTracker(viewModel, "settings")
        val scene = ImageComposeScene(width = 100, height = 100)
        scene.setContent { DismissOnLeaveComposition(tracker) }
        scene.render()
        assertTrue(dismissed().isEmpty())

        // Closing the window ends the application, which disposes its composition.
        scene.close()

        assertEquals(1, dismissed().size)
    }

    @Test
    fun `back arrow then swipe-back does not double count`() = runComposeUiTest {
        val tracker = PaywallDismissTracker(viewModel, "support_us")
        var shown by mutableStateOf(true)
        setContent { if (shown) DismissOnLeaveComposition(tracker) }

        tracker.dismiss()
        shown = false
        waitForIdle()

        assertEquals(1, dismissed().size)
    }

    @Test
    fun `leaving while subscribed does not fire paywall_dismissed`() = runComposeUiTest {
        repository.mockSubscriptionState = repository.mockSubscriptionState.copy(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM
        )
        val tracker = PaywallDismissTracker(viewModel, "support_us")
        var shown by mutableStateOf(true)
        setContent { if (shown) DismissOnLeaveComposition(tracker) }

        shown = false
        waitForIdle()

        assertTrue(dismissed().isEmpty())
    }
}
