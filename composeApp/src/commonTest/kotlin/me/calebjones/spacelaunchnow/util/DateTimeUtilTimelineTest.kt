package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeUtilTimelineTest {

    @Test
    fun testFormatTimelineRelativeTime() {
        // Test negative durations (before NET)
        assertEquals("T - 38m", DateTimeUtil.formatTimelineRelativeTime("-PT38M"))
        assertEquals("T - 45s", DateTimeUtil.formatTimelineRelativeTime("-PT45S"))
        assertEquals("T - 2h", DateTimeUtil.formatTimelineRelativeTime("-PT2H"))
        assertEquals("T - 1h 30m", DateTimeUtil.formatTimelineRelativeTime("-PT1H30M"))
        assertEquals("T - 2h 15m 30s", DateTimeUtil.formatTimelineRelativeTime("-PT2H15M30S"))
        
        // Test positive durations (after NET)
        assertEquals("T + 38m", DateTimeUtil.formatTimelineRelativeTime("PT38M"))
        assertEquals("T + 45s", DateTimeUtil.formatTimelineRelativeTime("PT45S"))
        assertEquals("T + 2h", DateTimeUtil.formatTimelineRelativeTime("PT2H"))
        assertEquals("T + 1h 30m", DateTimeUtil.formatTimelineRelativeTime("PT1H30M"))
        assertEquals("T + 2h 15m 30s", DateTimeUtil.formatTimelineRelativeTime("PT2H15M30S"))
        
        // Test edge cases
        assertEquals("T + 0s", DateTimeUtil.formatTimelineRelativeTime(""))
        assertEquals("T + 0s", DateTimeUtil.formatTimelineRelativeTime(null))
        assertEquals("T + 0s", DateTimeUtil.formatTimelineRelativeTime("PT"))
        assertEquals("T + 0s", DateTimeUtil.formatTimelineRelativeTime("invalid"))
        
        // Test single components
        assertEquals("T - 1h", DateTimeUtil.formatTimelineRelativeTime("-PT1H"))
        assertEquals("T + 30m", DateTimeUtil.formatTimelineRelativeTime("PT30M"))
        assertEquals("T - 15s", DateTimeUtil.formatTimelineRelativeTime("-PT15S"))
    }
}
