package me.calebjones.spacelaunchnow.util

import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Utility object for formatting dates and times in a locale-appropriate way using platform-specific formatters
 */
object DateTimeUtil {
    private val log by lazy { logger() }

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
     * - "-P1DT14H" -> "T - 1d 14h"
     * - "P2D" -> "T + 2d"
     */
    fun formatTimelineRelativeTime(relativeTime: String?): String {
        if (relativeTime.isNullOrBlank()) return "T + 0s"
        
        return try {
            // Parse ISO-8601 duration format (e.g., "-PT38M", "PT2H30M15S", "-P1DT14H")
            val trimmed = relativeTime.trim()
            val isNegative = trimmed.startsWith("-")
            val durationPart = if (isNegative) trimmed.substring(1) else trimmed
            
            // Must start with "P" for a valid ISO-8601 duration
            if (!durationPart.startsWith("P")) {
                return "T + 0s"
            }
            
            val afterP = durationPart.substring(1) // Remove "P"
            
            // Split into date part and time part at "T"
            val tIndex = afterP.indexOf('T')
            val datePart = if (tIndex >= 0) afterP.substring(0, tIndex) else afterP
            val timePart = if (tIndex >= 0) afterP.substring(tIndex + 1) else ""
            
            // Parse days (D) from the date part
            var days = 0
            val daysMatch = Regex("(\\d+)D").find(datePart)
            if (daysMatch != null) {
                days = daysMatch.groupValues[1].toInt()
            }
            
            // Parse hours, minutes, seconds from the time part
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
            
            if (days > 0) parts.add("${days}d")
            if (hours > 0) parts.add("${hours}h")
            if (minutes > 0) parts.add("${minutes}m")
            if (seconds > 0) parts.add("${seconds}s")
            
            // If no components found, default to 0s
            if (parts.isEmpty()) parts.add("0s")
            
            "T $sign ${parts.joinToString(" ")}"
        } catch (e: Exception) {
            "T + 0s"
        }
    }

    /**
     * Parses an ISO-8601 duration string into total seconds (signed).
     * Negative durations (before NET) return negative values.
     * Returns null if the string cannot be parsed.
     *
     * Examples:
     * - "-PT38M" -> -2280
     * - "PT2H30M" -> 9000
     * - "-P1DT14H" -> -136800
     * - "P2D" -> 172800
     */
    fun parseDurationToSeconds(relativeTime: String?): Long? {
        if (relativeTime.isNullOrBlank()) return null

        return try {
            val trimmed = relativeTime.trim()
            val isNegative = trimmed.startsWith("-")
            val durationPart = if (isNegative) trimmed.substring(1) else trimmed

            if (!durationPart.startsWith("P")) return null

            val afterP = durationPart.substring(1)
            val tIndex = afterP.indexOf('T')
            val datePart = if (tIndex >= 0) afterP.substring(0, tIndex) else afterP
            val timePart = if (tIndex >= 0) afterP.substring(tIndex + 1) else ""

            var days = 0L
            val daysMatch = Regex("(\\d+)D").find(datePart)
            if (daysMatch != null) {
                days = daysMatch.groupValues[1].toLong()
            }

            var hours = 0L
            var minutes = 0L
            var seconds = 0L

            val hoursMatch = Regex("(\\d+)H").find(timePart)
            if (hoursMatch != null) hours = hoursMatch.groupValues[1].toLong()

            val minutesMatch = Regex("(\\d+)M").find(timePart)
            if (minutesMatch != null) minutes = minutesMatch.groupValues[1].toLong()

            val secondsMatch = Regex("(\\d+)S").find(timePart)
            if (secondsMatch != null) seconds = secondsMatch.groupValues[1].toLong()

            val totalSeconds = days * 86400 + hours * 3600 + minutes * 60 + seconds

            if (totalSeconds == 0L && datePart.isEmpty() && timePart.isEmpty()) return null

            if (isNegative) -totalSeconds else totalSeconds
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Determines whether a timeline event has already occurred based on the launch NET and
     * the event's relative time offset.
     *
     * @param relativeTime ISO-8601 duration string relative to NET (e.g., "-PT38M", "PT2H30M")
     * @param net The launch NET (Network Event Time) as an Instant
     * @return true if the event time is in the past, false otherwise. Returns false if parsing fails.
     */
    fun isTimelineEventPassed(relativeTime: String?, net: Instant): Boolean {
        val offsetSeconds = parseDurationToSeconds(relativeTime) ?: return false
        val eventTime = net + offsetSeconds.seconds
        return eventTime <= System.now()
    }

    /**
     * Formats a domain Launch's NET date with custom precision fallback logic.
     */
    fun formatDateWithPrecisionFallback(launch: me.calebjones.spacelaunchnow.domain.model.Launch, useUtc: Boolean = false): String {
        val net = launch.net ?: return "TBD"
        val precisionId = launch.netPrecision?.id

        return formatDateWithPrecisionFallback(net, precisionId, useUtc)
    }

    private fun formatDateWithPrecisionFallback(net: kotlinx.datetime.Instant, precisionId: Int?, useUtc: Boolean): String {
        return when (precisionId) {
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
