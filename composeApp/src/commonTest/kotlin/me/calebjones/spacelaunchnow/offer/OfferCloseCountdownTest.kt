package me.calebjones.spacelaunchnow.offer

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfferCloseCountdownTest {

    @Test
    fun `zero delay is closable at once`() {
        val c = OfferCloseCountdown(0)
        assertTrue(c.canClose)
        assertEquals(1f, c.progress)
    }

    @Test
    fun `not closable before the delay ends`() {
        val c = OfferCloseCountdown(5)
        assertFalse(c.canClose)
        c.advance(4_999)
        assertFalse(c.canClose)
        assertTrue(c.progress < 1f)
    }

    @Test
    fun `closable once the delay ends and progress stops at one`() {
        val c = OfferCloseCountdown(5)
        c.advance(5_000)
        assertTrue(c.canClose)
        c.advance(10_000)
        assertEquals(1f, c.progress)
    }

    @Test
    fun `run completes the countdown in virtual time`() = runTest {
        val c = OfferCloseCountdown(3)
        c.run()
        assertTrue(c.canClose)
        assertEquals(3_000L, testScheduler.currentTime)
    }
}
