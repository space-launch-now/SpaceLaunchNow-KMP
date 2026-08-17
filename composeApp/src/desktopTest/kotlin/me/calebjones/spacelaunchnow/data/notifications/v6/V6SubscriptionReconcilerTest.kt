package me.calebjones.spacelaunchnow.data.notifications.v6

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V6SubscriptionReconcilerTest {

    // Flexible matching, SpaceX + Florida, tenMinutes only -- 3 topics.
    private val flexState = NotificationState(
        enableNotifications = true,
        followAllLaunches = false,
        useStrictMatching = false,
        topicSettings = mapOf(
            "tenMinutes" to true,
            "twentyFourHour" to false, "oneHour" to false, "oneMinute" to false,
            "netstampChanged" to false, "webcastLive" to false, "inFlight" to false,
            "success" to false, "failure" to false, "partial_failure" to false,
            "webcastOnly" to false,
            "events" to false, "featured_news" to false, "announcements" to false,
        ),
        subscribedAgencies = setOf("121"),
        subscribedLocations = setOf("27"),
        hasCompletedV6Changeover = true,   // Task 5 adds this field
    )

    private class Harness(initialState: NotificationState) {
        val fake = FakeTopicMessaging()
        val store: TopicSubscriptionStore
        var state: NotificationState = initialState
        var now = 1_000L
        val reconciler: V6SubscriptionReconciler

        init {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            SpaceLaunchDatabase.Schema.create(driver)
            store = TopicSubscriptionStore(SpaceLaunchDatabase(driver))
            reconciler = V6SubscriptionReconciler(
                store = store,
                messaging = fake,
                platform = "ios",
                envProvider = { "prod" },
                stateProvider = { state },
                markChangeoverComplete = { state = state.copy(hasCompletedV6Changeover = true) },
                nowMillis = { now },
            )
        }
    }

    @Test
    fun firstReconcileSubscribesTheDerivedSet() = runTest {
        val h = Harness(flexState)
        val result = h.reconciler.reconcile()
        assertEquals(3, result.attempted)
        assertEquals(0, result.failed)
        assertEquals(
            setOf("v6_prod_ios_flex_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            h.store.confirmedTopics().toSet(),
        )
    }

    @Test
    fun classSwitchIssuesEveryUnsubscribeBeforeAnySubscribe() = runTest {
        // THE duplicate-delivery guard -- asserted on call order, not final state.
        val h = Harness(flexState)
        h.reconciler.reconcile()
        h.fake.calls.clear()

        h.state = h.state.copy(useStrictMatching = true)   // flex -> strict rewrite
        h.reconciler.reconcile()

        val firstSub = h.fake.calls.indexOfFirst { it.startsWith("sub:") }
        val lastUnsub = h.fake.calls.indexOfLast { it.startsWith("unsub:") }
        assertTrue(firstSub > lastUnsub, "unsubscribes must all precede subscribes: ${h.fake.calls}")
        assertTrue("unsub:v6_prod_ios_flex_tenMinutes" in h.fake.calls)
        assertTrue("sub:v6_prod_ios_strict_tenMinutes" in h.fake.calls)
    }

    @Test
    fun interruptedClassSwitchNeverHoldsTwoClasses() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()

        h.state = h.state.copy(useStrictMatching = true)
        h.fake.failSubscribes = setOf("v6_prod_ios_strict_tenMinutes")
        h.reconciler.reconcile()

        // Under-subscribed (a transient gap), never double-classed.
        val confirmedTypes = h.store.confirmedTopics().filter { it.contains("_flex_") || it.contains("_strict_") }
        assertEquals(emptyList(), confirmedTypes)
    }

    @Test
    fun failedSubscribeLeavesRowPendingAndRetriesNextReconcile() = runTest {
        val h = Harness(flexState)
        h.fake.failSubscribes = setOf("v6_prod_spacex")
        val first = h.reconciler.reconcile()
        assertEquals(1, first.failed)
        assertEquals(listOf("v6_prod_spacex"), h.store.pendingSubscribes())

        h.fake.failSubscribes = emptySet()
        val second = h.reconciler.reconcile()
        assertEquals(0, second.failed)
        assertTrue("v6_prod_spacex" in h.store.confirmedTopics())
    }

    @Test
    fun failedUnsubscribeStaysConfirmedAndRetries() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()

        h.state = h.state.copy(subscribedAgencies = setOf("121"), subscribedLocations = setOf("27", "143"))
        h.reconciler.reconcile()   // adds texas
        h.state = h.state.copy(subscribedLocations = setOf("27"))
        h.fake.failUnsubscribes = setOf("v6_prod_texas")
        h.reconciler.reconcile()
        assertTrue("v6_prod_texas" in h.store.pendingUnsubscribes())

        h.fake.failUnsubscribes = emptySet()
        h.reconciler.reconcile()
        assertTrue("v6_prod_texas" !in h.store.confirmedTopics())
    }

    @Test
    fun noChangeReconcileIsZeroFcmOperations() = runTest {
        // Repeated saves are free; also the token-refresh contract -- reconcile,
        // and the rows survive untouched.
        val h = Harness(flexState)
        h.reconciler.reconcile()
        h.fake.calls.clear()

        val result = h.reconciler.reconcile()
        assertEquals(0, result.attempted)
        assertEquals(emptyList(), h.fake.calls)
        assertEquals(3, h.store.confirmedTopics().size)
    }

    @Test
    fun killSwitchDrivesTheTableToEmpty() = runTest {
        val h = Harness(flexState)
        h.reconciler.reconcile()
        h.state = h.state.copy(enableNotifications = false)
        h.reconciler.reconcile()
        assertEquals(emptyList(), h.store.confirmedTopics())
        assertEquals(emptyList(), h.store.pendingUnsubscribes())
    }

    @Test
    fun desktopPlatformIsANoOp() = runTest {
        val h = Harness(flexState)
        val desktop = V6SubscriptionReconciler(
            store = h.store,
            messaging = h.fake,
            platform = null,
            envProvider = { "prod" },
            stateProvider = { h.state },
            markChangeoverComplete = { },
            nowMillis = { 0L },
        )
        val result = desktop.reconcile()
        assertTrue(result.skipped)
        assertEquals(emptyList(), h.fake.calls)
    }
}
