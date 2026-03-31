package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.time.Instant

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

        // Test durations with days (beyond 24 hours)
        assertEquals("T - 1d 14h", DateTimeUtil.formatTimelineRelativeTime("-P1DT14H"))
        assertEquals("T + 2d", DateTimeUtil.formatTimelineRelativeTime("P2D"))
        assertEquals("T - 3d 2h 30m", DateTimeUtil.formatTimelineRelativeTime("-P3DT2H30M"))
        assertEquals("T + 1d 6h 15m 10s", DateTimeUtil.formatTimelineRelativeTime("P1DT6H15M10S"))
        assertEquals("T + 1d", DateTimeUtil.formatTimelineRelativeTime("P1D"))
        assertEquals("T - 7d", DateTimeUtil.formatTimelineRelativeTime("-P7D"))
        assertEquals("T + 0s", DateTimeUtil.formatTimelineRelativeTime("P"))
    }

    @Test
    fun testParseDurationToSeconds() {
        // Positive durations
        assertEquals(2280L, DateTimeUtil.parseDurationToSeconds("PT38M"))
        assertEquals(45L, DateTimeUtil.parseDurationToSeconds("PT45S"))
        assertEquals(7200L, DateTimeUtil.parseDurationToSeconds("PT2H"))
        assertEquals(9000L, DateTimeUtil.parseDurationToSeconds("PT2H30M"))
        assertEquals(8130L, DateTimeUtil.parseDurationToSeconds("PT2H15M30S"))
        assertEquals(172800L, DateTimeUtil.parseDurationToSeconds("P2D"))
        assertEquals(136800L, DateTimeUtil.parseDurationToSeconds("P1DT14H"))
        assertEquals(0L, DateTimeUtil.parseDurationToSeconds("PT0S"))

        // Negative durations
        assertEquals(-2280L, DateTimeUtil.parseDurationToSeconds("-PT38M"))
        assertEquals(-7200L, DateTimeUtil.parseDurationToSeconds("-PT2H"))
        assertEquals(-136800L, DateTimeUtil.parseDurationToSeconds("-P1DT14H"))
        assertEquals(-604800L, DateTimeUtil.parseDurationToSeconds("-P7D"))

        // Edge cases
        assertNull(DateTimeUtil.parseDurationToSeconds(null))
        assertNull(DateTimeUtil.parseDurationToSeconds(""))
        assertNull(DateTimeUtil.parseDurationToSeconds("invalid"))
        assertNull(DateTimeUtil.parseDurationToSeconds("P"))
    }

    @Test
    fun testIsTimelineEventPassed() {
        // NET at epoch + 1 hour (3600s)
        val net = Instant.fromEpochMilliseconds(3600_000L)

        // Event at T-38m = NET - 38min = epoch + 1h - 38m = epoch + 22min
        // This event is always in the past (epoch + 22m is far in the past)
        assertTrue(DateTimeUtil.isTimelineEventPassed("-PT38M", net))

        // Event at T+2h = NET + 2h = epoch + 3h — also far in the past
        assertTrue(DateTimeUtil.isTimelineEventPassed("PT2H", net))

        // NET far in the future — event should NOT have passed
        val futureNet = Instant.fromEpochMilliseconds(32503680000000L) // year 3000
        assertFalse(DateTimeUtil.isTimelineEventPassed("-PT38M", futureNet))
        assertFalse(DateTimeUtil.isTimelineEventPassed("PT2H", futureNet))

        // Null/invalid relativeTime returns false
        assertFalse(DateTimeUtil.isTimelineEventPassed(null, net))
        assertFalse(DateTimeUtil.isTimelineEventPassed("", net))
        assertFalse(DateTimeUtil.isTimelineEventPassed("invalid", net))
    }
}
