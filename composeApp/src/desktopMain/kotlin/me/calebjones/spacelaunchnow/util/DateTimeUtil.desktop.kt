package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Desktop/JVM-specific implementations using Java's DateTimeFormatter with locale support
 */
actual fun formatLocalDateTime(localDateTime: LocalDateTime, useUtc: Boolean): String {
    return try {
        val javaDateTime = java.time.LocalDateTime.of(
            localDateTime.year,
            localDateTime.monthNumber,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute,
            localDateTime.second
        )
        
        // Use JVM's locale-aware formatting
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(Locale.getDefault())

        val formatted = javaDateTime.format(formatter)
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        // Fallback to simple format
        val formatted =
            "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year} ${localDateTime.hour}:${
                localDateTime.minute.toString().padStart(2, '0')
            }"
        if (useUtc) "$formatted UTC" else formatted
    }
}

actual fun formatLocalDate(localDateTime: LocalDateTime, useUtc: Boolean): String {
    return try {
        val javaDate = java.time.LocalDate.of(
            localDateTime.year,
            localDateTime.monthNumber,
            localDateTime.dayOfMonth
        )
        
        // Use JVM's locale-aware date formatting
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())

        val formatted = javaDate.format(formatter)
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        // Fallback to simple format
        val formatted =
            "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
        if (useUtc) "$formatted UTC" else formatted
    }
}

actual fun formatLocalTime(localDateTime: LocalDateTime, useUtc: Boolean): String {
    return try {
        val javaTime = java.time.LocalTime.of(
            localDateTime.hour,
            localDateTime.minute
        )
        
        // Use JVM's locale-aware time formatting
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())

        val formatted = javaTime.format(formatter)
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        // Fallback to 24-hour format
        val formatted = "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        if (useUtc) "$formatted UTC" else formatted
    }
}
