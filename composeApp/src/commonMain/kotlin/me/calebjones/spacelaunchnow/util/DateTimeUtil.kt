package me.calebjones.spacelaunchnow.util

import kotlin.time.Clock.System
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic

/**
 * Utility object for formatting dates and times in a locale-appropriate way using platform-specific formatters
 */
object DateTimeUtil {
    
    /**
     * Formats an Instant to a human-readable local date and time string using platform-specific locale formatting
     * Android: Uses DateTimeFormatter with locale
     * Other platforms: Falls back to kotlinx-datetime formatting
     *
     * @param instant The instant to format
     * @param useUtc If true, displays time in UTC instead of local timezone
     */
    fun formatLaunchDateTime(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            formatLocalDateTime(localDateTime, useUtc)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to a shorter date string using locale formatting
     *
     * @param instant The instant to format
     * @param useUtc If true, displays date in UTC instead of local timezone
     */
    fun formatLaunchDate(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            formatLocalDate(localDateTime, useUtc)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to just the time using locale formatting
     *
     * @param instant The instant to format
     * @param useUtc If true, displays time in UTC instead of local timezone
     */
    fun formatLaunchTime(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            formatLocalTime(localDateTime, useUtc)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to a relative format if it's soon, otherwise absolute
     * Examples: "Today at 14:30", "Tomorrow at 09:15", "Dec 25, 2024 at 14:30"
     *
     * @param instant The instant to format
     * @param useUtc If true, uses UTC timezone for calculations and display
     */
    fun formatLaunchDateTimeRelative(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            val now = System.now().toLocalDateTime(timeZone)
            
            val daysDiff = localDateTime.date.toEpochDays() - now.date.toEpochDays()
            
            when (daysDiff) {
                0L -> "Today at ${formatLaunchTime(instant, useUtc)}"
                1L -> "Tomorrow at ${formatLaunchTime(instant, useUtc)}"
                (-1L) -> "Yesterday at ${formatLaunchTime(instant, useUtc)}"
                in 2..6 -> {
                    val dayName = localDateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    "$dayName at ${formatLaunchTime(instant, useUtc)}"
                }
                else -> formatLaunchDateTime(instant, useUtc)
            }
        } catch (e: Exception) {
            instant.toString()
        }
    }

    /**
     * Formats an ISO-8601 duration string to a relative time format for timeline events
     * Examples: 
     * - "-PT38M" -> "T - 38m"
     * - "PT2H30M" -> "T + 2h 30m"
     * - "-PT45S" -> "T - 45s"
     * - "PT1H" -> "T + 1h"
     */
    fun formatTimelineRelativeTime(relativeTime: String?): String {
        if (relativeTime.isNullOrBlank()) return "T + 0s"
        
        return try {
            // Parse ISO-8601 duration format (e.g., "-PT38M", "PT2H30M15S")
            val trimmed = relativeTime.trim()
            val isNegative = trimmed.startsWith("-")
            val durationPart = if (isNegative) trimmed.substring(1) else trimmed
            
            // Must start with "PT" for time duration
            if (!durationPart.startsWith("PT")) {
                return "T + 0s"
            }
            
            val timePart = durationPart.substring(2) // Remove "PT"
            
            // Parse hours, minutes, seconds
            var hours = 0
            var minutes = 0
            var seconds = 0
            
            var remaining = timePart
            
            // Parse hours (H)
            val hoursMatch = Regex("(\\d+)H").find(remaining)
            if (hoursMatch != null) {
                hours = hoursMatch.groupValues[1].toInt()
                remaining = remaining.replace(hoursMatch.value, "")
            }
            
            // Parse minutes (M)
            val minutesMatch = Regex("(\\d+)M").find(remaining)
            if (minutesMatch != null) {
                minutes = minutesMatch.groupValues[1].toInt()
                remaining = remaining.replace(minutesMatch.value, "")
            }
            
            // Parse seconds (S)
            val secondsMatch = Regex("(\\d+)S").find(remaining)
            if (secondsMatch != null) {
                seconds = secondsMatch.groupValues[1].toInt()
            }
            
            // Format the result
            val sign = if (isNegative) "-" else "+"
            val parts = mutableListOf<String>()
            
            if (hours > 0) parts.add("${hours}h")
            if (minutes > 0) parts.add("${minutes}m")
            if (seconds > 0) parts.add("${seconds}s")
            
            // If no time components found, default to 0s
            if (parts.isEmpty()) parts.add("0s")
            
            "T $sign ${parts.joinToString(" ")}"
        } catch (e: Exception) {
            "T + 0s"
        }
    }

    /**
     * Formats a LaunchBasic's NET date with custom precision fallback logic used in ScheduleScreen. Now shared for reuse.
     *
     * @param launch The launch to format
     * @param useUtc If true, displays time in UTC instead of local timezone
     */
    fun formatDateWithPrecisionFallback(launch: LaunchBasic, useUtc: Boolean = false): String {
        val net = launch.net ?: return "TBD"
        val precisionId = launch.netPrecision?.id

        return when (precisionId) {
            2 -> formatLaunchDate(net, useUtc)
            7 -> formatMonthYear(net, useUtc)
            8, 9, 10, 11 -> "${formatQuarter(net, useUtc)} ${formatYear(net, useUtc)}"
            12 -> "H1 ${formatYear(net, useUtc)}"
            13 -> "H2 ${formatYear(net, useUtc)}"
            14 -> "NET ${formatYear(net, useUtc)}"
            15 -> "FY ${formatYear(net, useUtc)}"
            16 -> "Decade ${formatYear(net, useUtc)}"
            else -> formatLaunchDate(net, useUtc)
        }
    }

    fun formatYear(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val ldt = instant.toLocalDateTime(timeZone)
            ldt.year.toString()
        } catch (e: Throwable) {
            ""
        }
    }

    fun formatMonthYear(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val ldt = instant.toLocalDateTime(timeZone)
            val monthYear = formatLaunchDate(instant, useUtc)
            val year = ldt.year.toString()
            val idx = monthYear.indexOf(year)
            if (idx > 0) {
                val beforeYear = monthYear.substring(0, idx).trim().trimEnd(',')
                "$beforeYear $year"
            } else monthYear
        } catch (_: Throwable) {
            formatLaunchDate(instant, useUtc)
        }
    }

    fun formatQuarter(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val ldt = instant.toLocalDateTime(timeZone)
            val month = ldt.monthNumber
            val quarter = when (month) {
                in 1..3 -> "Q1"
                in 4..6 -> "Q2"
                in 7..9 -> "Q3"
                else -> "Q4"
            }
            quarter
        } catch (_: Throwable) {
            "Q?"
        }
    }
}

// Top-level expect declarations for multiplatform date/time formatting
expect fun formatLocalDateTime(localDateTime: LocalDateTime, useUtc: Boolean = false): String
expect fun formatLocalDate(localDateTime: LocalDateTime, useUtc: Boolean = false): String
expect fun formatLocalTime(localDateTime: LocalDateTime, useUtc: Boolean = false): String
