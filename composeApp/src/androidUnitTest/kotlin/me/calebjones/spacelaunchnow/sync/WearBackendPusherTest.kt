package me.calebjones.spacelaunchnow.sync

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.TerminusHost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WearBackendPusherTest {

    private class FakeSync(private val failure: Exception? = null) : PhoneDataLayerSync {
        val backends = mutableListOf<Pair<DataBackend, String>>()
        override suspend fun syncToWatch() = Unit
        override suspend fun syncEntitlementToWatch(active: Boolean, expiresAt: Instant?) = Unit
        override suspend fun syncBackendToWatch(backend: DataBackend, terminusBaseUrl: String) {
            failure?.let { throw it }
            backends += backend to terminusBaseUrl
        }
    }

    @Test
    fun startSendsTheResolvedTerminusBackendAndHost() = runTest {
        val sync = FakeSync()
        val scope = TestScope(UnconfinedTestDispatcher(testScheduler))

        WearBackendPusher(DataBackend.TERMINUS, TerminusHost.STAGING.baseUrl, sync, scope).start()

        assertEquals(listOf(DataBackend.TERMINUS to TerminusHost.STAGING.baseUrl), sync.backends)
    }

    @Test
    fun pushNowSendsLlWhenThePhoneResolvedLl() = runTest {
        val sync = FakeSync()

        WearBackendPusher(DataBackend.LL, TerminusHost.PROD.baseUrl, sync, TestScope()).pushNow()

        assertEquals(listOf(DataBackend.LL to TerminusHost.PROD.baseUrl), sync.backends)
    }

    @Test
    fun pushNowSwallowsASyncFailure() = runTest {
        val sync = FakeSync(failure = IllegalStateException("no watch"))

        WearBackendPusher(DataBackend.TERMINUS, TerminusHost.PROD.baseUrl, sync, TestScope()).pushNow()

        assertTrue(sync.backends.isEmpty())
    }
}
