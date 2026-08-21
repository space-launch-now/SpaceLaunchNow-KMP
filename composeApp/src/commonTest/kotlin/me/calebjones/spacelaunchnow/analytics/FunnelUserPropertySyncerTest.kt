package me.calebjones.spacelaunchnow.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.billing.MockBillingManager
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FunnelUserPropertySyncerTest {

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
    }

    // AnalyticsManagerImpl dispatches setUserProperty on its own scope — hand it
    // the test scheduler so advanceUntilIdle() flushes the writes.
    private fun TestScope.analyticsWith(fake: FakeAnalyticsProvider) =
        AnalyticsManagerImpl(
            listOf(fake),
            scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        )

    @Test
    fun `syncs the four funnel dimensions as user properties`() = runTest {
        val fake = FakeAnalyticsProvider()
        val billingManager = MockBillingManager()
        val stateFlow = MutableStateFlow(SubscriptionState())
        val syncer = FunnelUserPropertySyncer(analyticsWith(fake), billingManager)

        syncer.start(this, stateFlow)
        advanceUntilIdle()

        assertEquals("free", fake.userProperties["subscription_type"])
        assertEquals("false", fake.userProperties["is_trial"])
        assertNotNull(fake.userProperties["active_entitlements"])
        assertEquals(getPlatform().type.name.lowercase(), fake.userProperties["platform"])

        // The syncer's collector is infinite by design; cancel it so runTest completes.
        coroutineContext.cancelChildren()
    }

    @Test
    fun `state changes update the properties`() = runTest {
        val fake = FakeAnalyticsProvider()
        val billingManager = MockBillingManager()
        val stateFlow = MutableStateFlow(SubscriptionState())
        val syncer = FunnelUserPropertySyncer(analyticsWith(fake), billingManager)

        syncer.start(this, stateFlow)
        advanceUntilIdle()

        stateFlow.value = SubscriptionState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            isInTrialPeriod = true
        )
        advanceUntilIdle()

        assertEquals("premium", fake.userProperties["subscription_type"])
        assertEquals("true", fake.userProperties["is_trial"])

        // The syncer's collector is infinite by design; cancel it so runTest completes.
        coroutineContext.cancelChildren()
    }
}
