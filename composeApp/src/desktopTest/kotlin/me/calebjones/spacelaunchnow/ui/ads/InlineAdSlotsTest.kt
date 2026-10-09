package me.calebjones.spacelaunchnow.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class InlineAdSlotsTest {

    private fun rows(itemCount: Int, pageSize: Int = 25, max: Int = 4) =
        inlineAdSlotRows(itemCount, pageSize, max)

    @Test
    fun emptyListHasNoSlots() {
        assertEquals(emptyList(), rows(0))
    }

    @Test
    fun partFirstPageHasNoSlots() {
        assertEquals(emptyList(), rows(4))
        assertEquals(emptyList(), rows(24))
    }

    @Test
    fun fullFirstPageGetsAPageEndSlot() {
        assertEquals(listOf(25), rows(25))
    }

    @Test
    fun nextPageRowsGoAfterThePageEndSlot() {
        // 26 rows: the slot after row 25 is already there, row 26 comes after it
        assertEquals(listOf(25), rows(26))
    }

    @Test
    fun hundredRowsHaveFourPageEndSlots() {
        assertEquals(listOf(25, 50, 75, 100), rows(100))
    }

    @Test
    fun pageEndSlotsStopAtTheMax() {
        assertEquals(listOf(25, 50, 75, 100), rows(150))
    }

    @Test
    fun noSlotsWhenMaxIsZero() {
        assertEquals(emptyList(), rows(100, max = 0))
    }

    @Test
    fun slotKeepsItsHandlerUntilRelease() {
        val slots = InlineAdSlots()
        var created = 0
        val first = slots.slot("a").handler { created++; Any() }
        val again = slots.slot("a").handler { created++; Any() }
        val other = slots.slot("b").handler { created++; Any() }

        assertSame(first, again)
        assertFalse(first === other)
        assertEquals(2, created)
    }

    @Test
    fun releaseDropsHandlersAndTheirState() {
        val slots = InlineAdSlots()
        val handler = slots.slot("a").handler { Any() }
        BannerRetryPolicy.shared.markShown(handler)
        BannerRefreshTracker.shouldReload(handler, "visit-1", nowMs = 0)

        slots.release()

        assertFalse(BannerRetryPolicy.shared.hasShown(handler))
        // Forgotten, so the next use is a first use again and keeps its ad
        assertFalse(BannerRefreshTracker.shouldReload(handler, "visit-2", nowMs = 1_000_000))
        val fresh = slots.slot("a").handler { Any() }
        assertTrue(fresh !== handler)
    }
}
