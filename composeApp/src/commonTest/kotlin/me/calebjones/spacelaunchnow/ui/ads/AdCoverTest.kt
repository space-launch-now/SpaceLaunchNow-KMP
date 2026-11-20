package me.calebjones.spacelaunchnow.ui.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdCoverTest {

    @Test
    fun notCoveredAtZero() {
        assertFalse(AdCoverMath.isCovered(0))
    }

    @Test
    fun coveredUntilEveryOverlayLeaves() {
        var c = 0
        c = AdCoverMath.up(c)
        c = AdCoverMath.up(c)
        assertTrue(AdCoverMath.isCovered(c))
        c = AdCoverMath.down(c)
        assertTrue(AdCoverMath.isCovered(c))
        c = AdCoverMath.down(c)
        assertFalse(AdCoverMath.isCovered(c))
    }

    @Test
    fun extraLowerNeverGoesNegative() {
        assertEquals(0, AdCoverMath.down(0))
        assertTrue(AdCoverMath.isCovered(AdCoverMath.up(AdCoverMath.down(0))))
    }

    @Test
    fun stateFollowsRaiseAndLower() {
        val state = AdCoverState()
        assertFalse(state.isCovered)
        state.raise()
        assertTrue(state.isCovered)
        state.lower()
        assertFalse(state.isCovered)
    }
}
