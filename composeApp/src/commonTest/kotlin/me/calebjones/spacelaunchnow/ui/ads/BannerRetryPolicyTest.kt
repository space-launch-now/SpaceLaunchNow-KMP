package me.calebjones.spacelaunchnow.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BannerRetryPolicyTest {

    private var clock = 0L
    private val policy = BannerRetryPolicy(now = { clock })

    /** Waits out the full delay, then records the attempt, like the composable does. */
    private fun attempt(handler: Any): Long {
        val delay = policy.nextDelayMs(handler)
        clock += delay
        policy.recordAttempt(handler)
        return delay
    }

    @Test
    fun backoffGrowsThenCaps() {
        val handler = Any()
        val delays = List(8) { attempt(handler) }
        assertEquals(
            listOf(1_000L, 3_000L, 10_000L, 30_000L, 60_000L, 120_000L, 120_000L, 120_000L),
            delays
        )
    }

    @Test
    fun neverGivesUp() {
        val handler = Any()
        repeat(500) { attempt(handler) }
        assertEquals(120_000L, policy.nextDelayMs(handler))
    }

    @Test
    fun resetRestartsBackoff() {
        val handler = Any()
        repeat(4) { attempt(handler) }
        policy.reset(handler)
        clock += 1_000_000
        assertEquals(1_000L, policy.nextDelayMs(handler))
    }

    @Test
    fun handlersAreIndependent() {
        val a = Any()
        val b = Any()
        repeat(3) { attempt(a) }
        assertEquals(1_000L, policy.nextDelayMs(b))
    }

    @Test
    fun elapsedTimeShortensDelayButKeepsOneSecondFloor() {
        val handler = Any()
        attempt(handler)
        attempt(handler) // next backoff step is 10s
        clock += 9_500
        assertEquals(1_000L, policy.nextDelayMs(handler))
        clock += 100_000
        assertEquals(1_000L, policy.nextDelayMs(handler))
    }

    @Test
    fun shownSurvivesReset() {
        val handler = Any()
        assertFalse(policy.hasShown(handler))
        policy.markShown(handler)
        policy.reset(handler)
        assertTrue(policy.hasShown(handler))
        assertFalse(policy.hasShown(Any()))
    }
}
