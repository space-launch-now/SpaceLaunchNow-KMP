package me.calebjones.spacelaunchnow.ui.compose

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Tasks#1144: a final launch status hides the countdown digits. */
class CountdownVisibilityTest {

    private val secondPrecision = 0

    @Test
    fun finalStatusesHideCountdown() {
        // 3 = Success, 4 = Failure, 7 = Partial Failure
        listOf(3, 4, 7).forEach { statusId ->
            assertFalse(
                isCountdownShown(days = 0, precisionId = secondPrecision, statusId = statusId),
                "status $statusId should hide the countdown"
            )
        }
    }

    @Test
    fun openStatusesShowCountdown() {
        // 1 = Go, 2 = TBD, 5 = Hold, 6 = In Flight, 8 = TBC, null = unknown
        listOf(1, 2, 5, 6, 8, null).forEach { statusId ->
            assertTrue(
                isCountdownShown(days = 0, precisionId = secondPrecision, statusId = statusId),
                "status $statusId should show the countdown"
            )
        }
    }

    @Test
    fun farOutOrImpreciseHidesCountdown() {
        assertFalse(isCountdownShown(days = 30, precisionId = secondPrecision, statusId = 1))
        assertFalse(isCountdownShown(days = 0, precisionId = 5, statusId = 1))
        assertFalse(isCountdownShown(days = 0, precisionId = null, statusId = 1))
    }
}
