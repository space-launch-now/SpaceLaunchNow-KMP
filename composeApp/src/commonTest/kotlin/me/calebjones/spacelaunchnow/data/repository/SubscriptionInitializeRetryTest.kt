package me.calebjones.spacelaunchnow.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.billing.BillingClient
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.billing.MockBillingManager
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionData
import me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionStorage
import me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// In-memory DataStore stub for tests
private class InMemoryDataStore : DataStore<Preferences> {
    private val _data = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = _data
    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val updated = transform(_data.value)
        _data.value = updated
        return updated
    }
}

// Minimal fake — stores one LocalSubscriptionData in memory; bypasses KStore file I/O
private class FakeLocalSubscriptionStorage(
    initial: LocalSubscriptionData
) : LocalSubscriptionStorage() {
    private var stored = initial
    private val _flow = MutableStateFlow(initial)
    override val subscriptionData: Flow<LocalSubscriptionData> = _flow.asStateFlow()
    override suspend fun get(): LocalSubscriptionData = stored
    override suspend fun update(data: LocalSubscriptionData): Boolean {
        stored = data
        _flow.value = data
        return true
    }
}

// Minimal fake — records whether syncNow() was called; no-ops startSyncing()
private class FakeSubscriptionSyncer(
    storage: LocalSubscriptionStorage,
    billing: BillingManager,
    private val onSyncNow: suspend () -> Boolean = { true }
) : SubscriptionSyncer(storage, billing) {
    override fun startSyncing() { /* no-op in tests */ }
    override suspend fun syncNow(): Boolean = onSyncNow()
}

class SubscriptionInitializeRetryTest {

    @Test
    fun `initialize calls syncNow when needsSync is true`() = runTest {
        var syncNowCalled = false
        val mockBilling = MockBillingManager()
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = true))
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, mockBilling) {
            syncNowCalled = true; true
        }
        val dataStore = InMemoryDataStore()

        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = BillingClient(mockBilling),
            widgetPreferences = WidgetPreferences(dataStore),
            temporaryPremiumAccess = TemporaryPremiumAccess(dataStore)
        )
        repo.initialize()

        assertTrue(syncNowCalled, "syncNow must be called when needsSync=true on cold start")
    }

    @Test
    fun `initialize does not call syncNow when needsSync is false`() = runTest {
        var syncNowCalled = false
        val mockBilling = MockBillingManager()
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = false))
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, mockBilling) {
            syncNowCalled = true; true
        }
        val dataStore = InMemoryDataStore()

        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = BillingClient(mockBilling),
            widgetPreferences = WidgetPreferences(dataStore),
            temporaryPremiumAccess = TemporaryPremiumAccess(dataStore)
        )
        repo.initialize()

        assertFalse(syncNowCalled, "syncNow must NOT be called when needsSync=false")
    }

    @Test
    fun `initialize completes silently when needsSync is true but syncNow fails`() = runTest {
        var syncNowCalled = false
        val mockBilling = MockBillingManager()
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = true))
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, mockBilling) {
            syncNowCalled = true; false // syncNow fails
        }
        val dataStore = InMemoryDataStore()

        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = BillingClient(mockBilling),
            widgetPreferences = WidgetPreferences(dataStore),
            temporaryPremiumAccess = TemporaryPremiumAccess(dataStore)
        )

        // Should complete without throwing
        repo.initialize()

        assertTrue(syncNowCalled, "syncNow must still be attempted when needsSync=true, even if it fails")
    }

    @Test
    fun `concurrent initialize calls run the body once`() = runTest {
        var syncNowCount = 0
        val gate = CompletableDeferred<Unit>()
        val mockBilling = MockBillingManager()
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = true))
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, mockBilling) {
            syncNowCount++
            gate.await() // hold the first caller inside the body so the second overlaps
            true
        }
        val dataStore = InMemoryDataStore()

        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = BillingClient(mockBilling),
            widgetPreferences = WidgetPreferences(dataStore),
            temporaryPremiumAccess = TemporaryPremiumAccess(dataStore)
        )

        val calls = listOf(async { repo.initialize() }, async { repo.initialize() })
        runCurrent()
        gate.complete(Unit)
        calls.awaitAll()

        assertEquals(1, mockBilling.initializeCallCount, "billingClient.initialize must run once")
        assertEquals(1, syncNowCount, "syncNow must run once")
    }

    @Test
    fun `second sequential initialize is a no-op`() = runTest {
        var syncNowCount = 0
        val mockBilling = MockBillingManager()
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = true))
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, mockBilling) {
            syncNowCount++; true
        }
        val dataStore = InMemoryDataStore()

        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = BillingClient(mockBilling),
            widgetPreferences = WidgetPreferences(dataStore),
            temporaryPremiumAccess = TemporaryPremiumAccess(dataStore)
        )
        repo.initialize()
        repo.initialize()

        assertEquals(1, mockBilling.initializeCallCount)
        assertEquals(1, syncNowCount)
    }

    @Test
    fun `initialize retries billing init after a failed result then becomes a no-op`() = runTest {
        var syncNowCount = 0
        val mockBilling = MockBillingManager().apply { shouldInitializeFail = true }
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = true))
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, mockBilling) {
            syncNowCount++; true
        }
        val dataStore = InMemoryDataStore()

        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = BillingClient(mockBilling),
            widgetPreferences = WidgetPreferences(dataStore),
            temporaryPremiumAccess = TemporaryPremiumAccess(dataStore)
        )

        repo.initialize()
        repo.initialize()
        assertEquals(2, mockBilling.initializeCallCount, "failed billing init must be retried")

        mockBilling.shouldInitializeFail = false
        repo.initialize()
        assertEquals(3, mockBilling.initializeCallCount)
        assertEquals(1, syncNowCount, "sync runs only once billing init succeeded")

        repo.initialize()
        assertEquals(3, mockBilling.initializeCallCount, "initialised repo must be a no-op")
        assertEquals(1, syncNowCount)
    }
}
