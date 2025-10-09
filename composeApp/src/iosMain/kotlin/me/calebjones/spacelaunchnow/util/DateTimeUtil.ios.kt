package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.LocalDateTime

/**
 * iOS-specific implementations 
 * Note: For true iOS locale formatting, you'd need to use NSDateFormatter through cinterop
 * For now, using a reasonable fallback format
 */
actual fun formatLocalDateTime(localDateTime: LocalDateTime, useUtc: Boolean): String {
    return try {
        // Format: "Dec 25, 2024 at 2:30 PM" style
        val month = getMonthName(localDateTime.monthNumber)
        val hour12 = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
        val amPm = if (localDateTime.hour < 12) "AM" else "PM"
        val minute = localDateTime.minute.toString().padStart(2, '0')

        val formatted =
            "$month ${localDateTime.dayOfMonth}, ${localDateTime.year} at $hour12:$minute $amPm"
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
        val month = getMonthName(localDateTime.monthNumber)
        val formatted = "$month ${localDateTime.dayOfMonth}, ${localDateTime.year}"
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        val formatted =
            "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
        if (useUtc) "$formatted UTC" else formatted
    }
}

actual fun formatLocalTime(localDateTime: LocalDateTime, useUtc: Boolean): String {
    return try {
        // 12-hour format with AM/PM
        val hour12 = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
        val amPm = if (localDateTime.hour < 12) "AM" else "PM"
        val minute = localDateTime.minute.toString().padStart(2, '0')

        val formatted = "$hour12:$minute $amPm"
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        // Fallback to 24-hour format
        val formatted = "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        if (useUtc) "$formatted UTC" else formatted
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb" 
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> month.toString()
    }
}
