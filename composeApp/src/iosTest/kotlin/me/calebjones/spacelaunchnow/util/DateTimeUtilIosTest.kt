package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * iOS-specific tests for DateTimeUtil to verify correct date formatting
 * Particularly testing the toEpochSeconds conversion for proper month handling
 */
class DateTimeUtilIosTest {
    
    @Test
    fun testFormatLocalDate_November() {
        // Test November 15, 2025 - this was showing as October in the bug
        val novemberDate = LocalDateTime(2025, Month.NOVEMBER, 15, 12, 0, 0)
        val formatted = formatLocalDate(novemberDate, useUtc = false)
        
        // The formatted string should contain "November" or "Nov" or "11" (depending on locale)
        // It should NOT contain "October" or "Oct" or show month 10
        assertTrue(
            formatted.contains("November", ignoreCase = true) || 
            formatted.contains("Nov", ignoreCase = true) ||
            formatted.contains("11") ||
            formatted.matches(Regex(".*\\b11\\b.*")),
            "Date should represent November, but got: $formatted"
        )
        
        // Additional check: should contain 2025
        assertTrue(formatted.contains("2025"), "Date should contain year 2025, but got: $formatted")
        
        // Should contain 15
        assertTrue(formatted.contains("15"), "Date should contain day 15, but got: $formatted")
    }
    
    @Test
    fun testFormatLocalDate_December() {
        // Test December to verify month 12 works correctly
        val decemberDate = LocalDateTime(2025, Month.DECEMBER, 25, 12, 0, 0)
        val formatted = formatLocalDate(decemberDate, useUtc = false)
        
        assertTrue(
            formatted.contains("December", ignoreCase = true) || 
            formatted.contains("Dec", ignoreCase = true) ||
            formatted.contains("12") ||
            formatted.matches(Regex(".*\\b12\\b.*")),
            "Date should represent December, but got: $formatted"
        )
        
        assertTrue(formatted.contains("2025"), "Date should contain year 2025, but got: $formatted")
        assertTrue(formatted.contains("25"), "Date should contain day 25, but got: $formatted")
    }
    
    @Test
    fun testFormatLocalDate_January() {
        // Test January to verify first month of year works correctly
        val januaryDate = LocalDateTime(2025, Month.JANUARY, 1, 0, 0, 0)
        val formatted = formatLocalDate(januaryDate, useUtc = false)
        
        assertTrue(
            formatted.contains("January", ignoreCase = true) || 
            formatted.contains("Jan", ignoreCase = true) ||
            formatted.startsWith("1/") || // Some locales might use numeric format
            formatted.matches(Regex(".*\\b1\\b.*")),
            "Date should represent January, but got: $formatted"
        )
        
        assertTrue(formatted.contains("2025"), "Date should contain year 2025, but got: $formatted")
    }
    
    @Test
    fun testFormatLaunchDate_VariousMonths() {
        // Test that each month is correctly represented (no off-by-one errors)
        val months = listOf(
            Month.JANUARY to "January",
            Month.FEBRUARY to "February",
            Month.MARCH to "March",
            Month.APRIL to "April",
            Month.MAY to "May",
            Month.JUNE to "June",
            Month.JULY to "July",
            Month.AUGUST to "August",
            Month.SEPTEMBER to "September",
            Month.OCTOBER to "October",
            Month.NOVEMBER to "November",
            Month.DECEMBER to "December"
        )
        
        months.forEach { (month, monthName) ->
            val testDate = LocalDateTime(2025, month, 15, 12, 0, 0)
            val instant = Instant.parse("2025-${month.number.toString().padStart(2, '0')}-15T12:00:00Z")
            val formatted = DateTimeUtil.formatLaunchDate(instant, useUtc = true)
            
            // The formatted date should contain the correct month number or name
            val monthNum = month.number.toString()
            assertTrue(
                formatted.contains(monthName, ignoreCase = true) || 
                formatted.contains(monthNum) ||
                formatted.contains(monthName.substring(0, 3), ignoreCase = true),
                "Date for $monthName should be correctly formatted, but got: $formatted"
            )
        }
    }
}
