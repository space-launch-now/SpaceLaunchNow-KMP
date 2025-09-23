package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Android-specific implementations using Java's DateTimeFormatter with locale support
 */
actual fun formatLocalDateTime(localDateTime: LocalDateTime): String {
    return try {
        val javaDateTime = java.time.LocalDateTime.of(
            localDateTime.year,
            localDateTime.monthNumber,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute,
            localDateTime.second
        )
        
        // Use Android's locale-aware formatting
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
        
        javaDateTime.format(formatter)
    } catch (e: Exception) {
        // Fallback to simple format
        "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    }
}

actual fun formatLocalDate(localDateTime: LocalDateTime): String {
    return try {
        val javaDate = java.time.LocalDate.of(
            localDateTime.year,
            localDateTime.monthNumber,
            localDateTime.dayOfMonth
        )
        
        // Use Android's locale-aware date formatting
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.getDefault())
        
        javaDate.format(formatter)
    } catch (e: Exception) {
        // Fallback to simple format
        "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    }
}

actual fun formatLocalTime(localDateTime: LocalDateTime): String {
    return try {
        val javaTime = java.time.LocalTime.of(
            localDateTime.hour,
            localDateTime.minute
        )
        
        // Use Android's locale-aware time formatting
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
        
        javaTime.format(formatter)
    } catch (e: Exception) {
        // Fallback to 24-hour format
        "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    }
}
