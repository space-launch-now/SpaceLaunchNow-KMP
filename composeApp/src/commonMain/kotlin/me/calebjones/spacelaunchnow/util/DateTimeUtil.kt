package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility object for formatting dates and times in a locale-appropriate way using platform-specific formatters
 */
object DateTimeUtil {
    
    /**
     * Formats an Instant to a human-readable local date and time string using platform-specific locale formatting
     * Android: Uses DateTimeFormatter with locale
     * Other platforms: Falls back to kotlinx-datetime formatting
     */
    fun formatLaunchDateTime(instant: Instant): String {
        return try {
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            formatLocalDateTime(localDateTime)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to a shorter date string using locale formatting
     */
    fun formatLaunchDate(instant: Instant): String {
        return try {
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            formatLocalDate(localDateTime)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to just the time using locale formatting
     */
    fun formatLaunchTime(instant: Instant): String {
        return try {
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            formatLocalTime(localDateTime)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to a relative format if it's soon, otherwise absolute
     * Examples: "Today at 14:30", "Tomorrow at 09:15", "Dec 25, 2024 at 14:30"
     */
    fun formatLaunchDateTimeRelative(instant: Instant): String {
        return try {
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            
            val daysDiff = localDateTime.date.toEpochDays() - now.date.toEpochDays()
            
            when (daysDiff) {
                0 -> "Today at ${formatLaunchTime(instant)}"
                1 -> "Tomorrow at ${formatLaunchTime(instant)}"
                -1 -> "Yesterday at ${formatLaunchTime(instant)}"
                in 2..6 -> {
                    val dayName = localDateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    "$dayName at ${formatLaunchTime(instant)}"
                }
                else -> formatLaunchDateTime(instant)
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
}

/**
 * Platform-specific date/time formatting functions
 * These will be implemented differently for each platform to use native locale formatting
 */
expect fun formatLocalDateTime(localDateTime: LocalDateTime): String
expect fun formatLocalDate(localDateTime: LocalDateTime): String  
expect fun formatLocalTime(localDateTime: LocalDateTime): String
