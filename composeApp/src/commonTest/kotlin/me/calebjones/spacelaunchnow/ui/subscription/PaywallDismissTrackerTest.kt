package me.calebjones.spacelaunchnow.ui.subscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.analytics.FakeAnalyticsProvider
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
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

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallDismissTrackerTest {

    private lateinit var repository: MockSubscriptionRepository
    private lateinit var fake: FakeAnalyticsProvider
    private lateinit var viewModel: SubscriptionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
        Dispatchers.setMain(testDispatcher)
        repository = MockSubscriptionRepository()
        fake = FakeAnalyticsProvider()
        val analytics: AnalyticsManager = AnalyticsManagerImpl(listOf(fake), scope = CoroutineScope(SupervisorJob() + testDispatcher))
        viewModel = SubscriptionViewModel(repository, MockBillingManager(), analytics)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun dismissed() = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PaywallDismissed>()

    @Test
    fun `leaving without buying fires paywall_dismissed once with the source`() = runTest {
        val tracker = PaywallDismissTracker(viewModel, "home_heart")

        tracker.dismiss()
        tracker.dismiss() // back arrow then system back must not double count
        advanceUntilIdle()

        val event = dismissed().single()
        assertEquals("paywall_dismissed", event.name)
        assertEquals("home_heart", event.source)
        assertTrue(event.secondsOnScreen >= 0L)
    }

    @Test
    fun `leaving after a purchase does not fire paywall_dismissed`() = runTest {
        repository.mockSubscriptionState = repository.mockSubscriptionState.copy(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM
        )
        val tracker = PaywallDismissTracker(viewModel, "support_us")

        tracker.dismiss()
        advanceUntilIdle()

        assertTrue(dismissed().isEmpty())
    }
}
