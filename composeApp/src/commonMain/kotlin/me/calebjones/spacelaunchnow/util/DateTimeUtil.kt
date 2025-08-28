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
}

/**
 * Platform-specific date/time formatting functions
 * These will be implemented differently for each platform to use native locale formatting
 */
expect fun formatLocalDateTime(localDateTime: LocalDateTime): String
expect fun formatLocalDate(localDateTime: LocalDateTime): String  
expect fun formatLocalTime(localDateTime: LocalDateTime): String
