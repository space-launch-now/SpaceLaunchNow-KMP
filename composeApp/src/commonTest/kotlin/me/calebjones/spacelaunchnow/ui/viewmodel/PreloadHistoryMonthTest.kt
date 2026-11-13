package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class PreloadHistoryMonthTest {

    @Test fun `january warms month 1, not 0`() {
        assertEquals(15 to 1, historyDayAndMonth(LocalDateTime(2026, 1, 15, 12, 0)))
    }

    @Test fun `december warms month 12`() {
        assertEquals(31 to 12, historyDayAndMonth(LocalDateTime(2026, 12, 31, 0, 0)))
    }
}
