package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ParseISOTest {
    
    @Test
    fun testParseIsoDuration_hoursMinutesSeconds() {
        val result = parseIsoDurationToHumanReadable("PT2H30M15S")
        assertEquals("2h 30m 15s", result)
    }
    
    @Test
    fun testParseIsoDuration_hoursOnly() {
        val result = parseIsoDurationToHumanReadable("PT3H")
        assertEquals("3h", result)
    }
    
    @Test
    fun testParseIsoDuration_minutesOnly() {
        val result = parseIsoDurationToHumanReadable("PT45M")
        assertEquals("45m", result)
    }
    
    @Test
    fun testParseIsoDuration_secondsOnly() {
        val result = parseIsoDurationToHumanReadable("PT30S")
        assertEquals("30s", result)
    }
    
    @Test
    fun testParseIsoDuration_daysHoursMinutes() {
        val result = parseIsoDurationToHumanReadable("P2DT5H30M")
        assertEquals("2d 5h 30m", result)
    }
    
    @Test
    fun testParseIsoDuration_fullFormat() {
        val result = parseIsoDurationToHumanReadable("P1Y2M3DT4H5M6S")
        assertEquals("1y 2mo 3d 4h 5m 6s", result)
    }
    
    @Test
    fun testParseIsoDuration_yearsOnly() {
        val result = parseIsoDurationToHumanReadable("P1Y")
        assertEquals("1y", result)
    }
    
    @Test
    fun testParseIsoDuration_monthsOnly() {
        val result = parseIsoDurationToHumanReadable("P6M")
        assertEquals("6mo", result)
    }
    
    @Test
    fun testParseIsoDuration_daysOnly() {
        val result = parseIsoDurationToHumanReadable("P10D")
        assertEquals("10d", result)
    }
    
    @Test
    fun testParseIsoDuration_zeroDuration() {
        val result = parseIsoDurationToHumanReadable("PT0S")
        assertEquals("0", result)
    }
    
    @Test
    fun testParseIsoDuration_emptyPeriod() {
        val result = parseIsoDurationToHumanReadable("P")
        assertEquals("0", result)
    }
    
    @Test
    fun testParseIsoDuration_invalidFormat() {
        val result = parseIsoDurationToHumanReadable("invalid")
        assertEquals("invalid", result)
    }
    
    @Test
    fun testParseIsoDuration_hoursAndMinutes() {
        val result = parseIsoDurationToHumanReadable("PT1H30M")
        assertEquals("1h 30m", result)
    }
    
    @Test
    fun testParseIsoDuration_minutesAndSeconds() {
        val result = parseIsoDurationToHumanReadable("PT15M30S")
        assertEquals("15m 30s", result)
    }
    
    @Test
    fun testParseIsoDuration_commonEventDuration() {
        // Test a realistic event duration like a 2-hour spacewalk
        val result = parseIsoDurationToHumanReadable("PT2H")
        assertEquals("2h", result)
    }
    
    @Test
    fun testParseIsoDuration_spacecraftMissionDuration() {
        // Test a spacecraft mission that lasts 59 days, 12 hours, 59 minutes (like ISS mission)
        val result = parseIsoDurationToHumanReadable("P59DT12H59M")
        assertEquals("59d 12h 59m", result)
    }
}
