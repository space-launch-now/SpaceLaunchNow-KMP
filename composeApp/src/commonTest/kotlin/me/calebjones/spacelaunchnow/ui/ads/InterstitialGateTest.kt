package me.calebjones.spacelaunchnow.ui.ads

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InterstitialGateTest {

    private class MemoryStore(var state: InterstitialGateState = InterstitialGateState()) : InterstitialGateStore {
        override suspend fun load(): InterstitialGateState = state
        override suspend fun save(state: InterstitialGateState) {
            this.state = state
        }
    }

    private var clock = 1_000_000L

    private fun gate(
        store: InterstitialGateStore = MemoryStore(),
        visits: Int = 4,
        minIntervalMs: Long = 120_000L,
    ) = InterstitialGate(
        visitsBetween = { visits },
        minIntervalMs = { minIntervalMs },
        now = { clock },
        store = store,
        scope = CoroutineScope(Dispatchers.Unconfined),
    )

    @Test
    fun firstEverVisitNeverShows() {
        assertFalse(gate().shouldShow())
    }

    @Test
    fun showsOnEveryNthVisit() {
        val g = gate(visits = 4)
        val results = (1..8).map { clock += 200_000L; g.shouldShow() }
        assertEquals(listOf(false, false, false, true, false, false, false, true), results)
    }

    @Test
    fun minIntervalBlocksShowAndRecordsNothing() {
        val g = gate(visits = 2, minIntervalMs = 120_000L)
        clock += 200_000L
        g.shouldShow()
        assertTrue(g.shouldShow()) // visit 2, shown
        g.shouldShow() // visit 3
        clock += 10_000L
        assertFalse(g.shouldShow()) // visit 4, only 10 s later
        assertEquals(4, g.visitCount)
    }

    @Test
    fun nonPositiveVisitsDisablesGate() {
        assertFalse(gate(visits = 0).let { g -> (1..10).any { clock += 500_000L; g.shouldShow() } })
        assertFalse(gate(visits = -3).let { g -> (1..10).any { clock += 500_000L; g.shouldShow() } })
    }

    @Test
    fun countSurvivesRestartAfterRestore() = runTest {
        val store = MemoryStore()
        val first = gate(store, visits = 4)
        first.restore()
        repeat(3) { first.shouldShow() }
        val second = gate(store, visits = 4)
        second.restore()
        assertEquals(3, second.visitCount)
        clock += 200_000L
        assertTrue(second.shouldShow())
    }

    @Test
    fun restoreMergesVisitsMadeBeforeLoad() = runTest {
        val store = MemoryStore(InterstitialGateState(visitCount = 2, lastShownAtMs = 500L))
        val g = gate(store, visits = 10)
        g.shouldShow()
        assertEquals(2, store.state.visitCount) // not overwritten before restore
        g.restore()
        assertEquals(3, g.visitCount)
        assertEquals(3, store.state.visitCount)
    }

    @Test
    fun lastShownPersistsAcrossRestart() = runTest {
        val store = MemoryStore()
        val first = gate(store, visits = 1, minIntervalMs = 120_000L)
        first.restore()
        clock += 200_000L
        assertTrue(first.shouldShow())
        val second = gate(store, visits = 1, minIntervalMs = 120_000L)
        second.restore()
        clock += 30_000L
        assertFalse(second.shouldShow())
        assertEquals(0L, second.minutesSinceLastShown())
    }

    @Test
    fun resetClearsStateAndStore() {
        val store = MemoryStore()
        val g = gate(store, visits = 1)
        clock += 200_000L
        g.shouldShow()
        g.reset()
        assertEquals(0, g.visitCount)
        assertEquals(999L, g.minutesSinceLastShown())
        assertEquals(InterstitialGateState(), store.state)
    }

    @Test
    fun minutesSinceLastShownIsSentinelBeforeFirstShow() {
        assertEquals(999L, gate().minutesSinceLastShown())
    }
}
