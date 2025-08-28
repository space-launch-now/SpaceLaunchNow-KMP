package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.LocalDateTime

/**
 * iOS-specific implementations 
 * Note: For true iOS locale formatting, you'd need to use NSDateFormatter through cinterop
 * For now, using a reasonable fallback format
 */
actual fun formatLocalDateTime(localDateTime: LocalDateTime): String {
    return try {
        // Format: "Dec 25, 2024 at 2:30 PM" style
        val month = getMonthName(localDateTime.monthNumber)
        val hour12 = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
        val amPm = if (localDateTime.hour < 12) "AM" else "PM"
        val minute = localDateTime.minute.toString().padStart(2, '0')
        
        "$month ${localDateTime.dayOfMonth}, ${localDateTime.year} at $hour12:$minute $amPm"
    } catch (e: Exception) {
        // Fallback to simple format
        "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    }
}

actual fun formatLocalDate(localDateTime: LocalDateTime): String {
    return try {
        val month = getMonthName(localDateTime.monthNumber)
        "$month ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    } catch (e: Exception) {
        "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    }
}

actual fun formatLocalTime(localDateTime: LocalDateTime): String {
    return try {
        // 12-hour format with AM/PM
        val hour12 = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
        val amPm = if (localDateTime.hour < 12) "AM" else "PM"
        val minute = localDateTime.minute.toString().padStart(2, '0')
        
        "$hour12:$minute $amPm"
    } catch (e: Exception) {
        // Fallback to 24-hour format
        "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
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
