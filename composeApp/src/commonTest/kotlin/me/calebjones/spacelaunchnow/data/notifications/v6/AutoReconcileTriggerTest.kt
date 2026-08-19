package me.calebjones.spacelaunchnow.data.notifications.v6

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The trigger is what makes filter toggles reach FCM without an explicit save step —
 * if it under-fires, a user's change silently waits for the next app start (the bug
 * this replaces); if it fires per tick, a class switch storms FCM with ~20 topic
 * rewrites per toggle.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AutoReconcileTriggerTest {

    @Test
    fun `a burst of requests collapses into a single reconcile`() = runTest {
        var reconciles = 0
        val trigger = AutoReconcileTrigger(backgroundScope, debounceMs = 750) { reconciles++ }

        repeat(20) { trigger.request() }
        advanceTimeBy(751)
        runCurrent()

        assertEquals(1, reconciles)
    }

    @Test
    fun `requests inside the debounce window keep deferring the pass`() = runTest {
        var reconciles = 0
        val trigger = AutoReconcileTrigger(backgroundScope, debounceMs = 750) { reconciles++ }

        trigger.request()
        advanceTimeBy(500)
        runCurrent()
        assertEquals(0, reconciles, "still inside the window")

        trigger.request()
        advanceTimeBy(500)
        runCurrent()
        assertEquals(0, reconciles, "second request restarted the window")

        advanceTimeBy(251)
        runCurrent()
        assertEquals(1, reconciles)
    }

    @Test
    fun `separate editing sessions each get their own reconcile`() = runTest {
        var reconciles = 0
        val trigger = AutoReconcileTrigger(backgroundScope, debounceMs = 750) { reconciles++ }

        trigger.request()
        advanceTimeBy(751)
        runCurrent()
        assertEquals(1, reconciles)

        trigger.request()
        advanceTimeBy(751)
        runCurrent()
        assertEquals(2, reconciles)
    }

    @Test
    fun `a throwing reconcile does not kill the trigger`() = runTest {
        var attempts = 0
        val trigger = AutoReconcileTrigger(backgroundScope, debounceMs = 750) {
            attempts++
            if (attempts == 1) throw IllegalStateException("FCM exploded")
        }

        trigger.request()
        advanceTimeBy(751)
        runCurrent()
        assertEquals(1, attempts)

        // The trigger must survive the failure and serve the next request.
        trigger.request()
        advanceTimeBy(751)
        runCurrent()
        assertEquals(2, attempts)
    }
}
