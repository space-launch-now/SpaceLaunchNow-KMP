package me.calebjones.spacelaunchnow.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentResolutionTest {

    @Test
    fun resolverFiresOnlyOnce() {
        var calls = 0
        val resolver = ConsentResolver { calls++ }
        assertTrue(resolver.resolve())
        assertFalse(resolver.resolve())
        assertFalse(resolver.resolve())
        assertEquals(1, calls)
        assertTrue(resolver.isResolved)
    }

    @Test
    fun resolverWithNullCallbackStillTracksState() {
        val resolver = ConsentResolver(null)
        assertFalse(resolver.isResolved)
        assertTrue(resolver.resolve())
        assertTrue(resolver.isResolved)
    }

    @Test
    fun pollReturnsImmediatelyWhenAlreadyAllowed() = runTest {
        val outcome = awaitCanRequestAds(canRequestAds = { true })
        assertEquals(ConsentPollOutcome.CAN_REQUEST_ADS, outcome)
        assertEquals(0, currentTime)
    }

    @Test
    fun pollReturnsWhenGetterFlipsLater() = runTest {
        var reads = 0
        val outcome = awaitCanRequestAds(canRequestAds = { ++reads > 4 })
        assertEquals(ConsentPollOutcome.CAN_REQUEST_ADS, outcome)
        assertEquals(4 * 250L, currentTime)
    }

    @Test
    fun pollTimesOutWhenNeverAllowed() = runTest {
        val outcome = awaitCanRequestAds(canRequestAds = { false })
        assertEquals(ConsentPollOutcome.TIMED_OUT, outcome)
        assertEquals(15_000L, currentTime)
    }

    @Test
    fun pollNeverTimesOutWhileHeld() = runTest {
        // A consent form is on screen: 100 polls (25 s) is well past the 15 s timeout.
        var polls = 0
        val outcome = awaitCanRequestAds(
            canRequestAds = { ++polls > 100 },
            holdTimeoutWhile = { true },
        )
        assertEquals(ConsentPollOutcome.CAN_REQUEST_ADS, outcome)
        assertEquals(100 * 250L, currentTime)
    }

    @Test
    fun pollTimesOutOnlyAfterHoldReleases() = runTest {
        var polls = 0
        val outcome = awaitCanRequestAds(
            canRequestAds = { polls++; false },
            holdTimeoutWhile = { polls <= 20 },
        )
        assertEquals(ConsentPollOutcome.TIMED_OUT, outcome)
        assertEquals(20 * 250L + 15_000L, currentTime)
    }

    @Test
    fun pollHonoursCustomTimeout() = runTest {
        val outcome = awaitCanRequestAds(
            canRequestAds = { false },
            intervalMs = 100,
            timeoutMs = 1_000,
        )
        assertEquals(ConsentPollOutcome.TIMED_OUT, outcome)
        assertEquals(1_000L, currentTime)
    }
}
