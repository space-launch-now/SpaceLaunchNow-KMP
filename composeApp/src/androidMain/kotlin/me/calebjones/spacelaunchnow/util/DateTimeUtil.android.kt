package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Android-specific implementations using Java's DateTimeFormatter with locale support
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

        // Use user's locale from LocaleUtil for consistent locale handling
        val userLocale = Locale.forLanguageTag(LocaleUtil.getLocaleTag())
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(userLocale)

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
            localDateTime.month.number,
            localDateTime.day
        )

        // Use user's locale from LocaleUtil for consistent locale handling
        val userLocale = Locale.forLanguageTag(LocaleUtil.getLocaleTag())
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(userLocale)

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

        // Use user's locale from LocaleUtil for consistent locale handling
        val userLocale = Locale.forLanguageTag(LocaleUtil.getLocaleTag())
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(userLocale)

        val formatted = javaTime.format(formatter)
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        // Fallback to 24-hour format
        val formatted = "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        if (useUtc) "$formatted UTC" else formatted
    }
}
