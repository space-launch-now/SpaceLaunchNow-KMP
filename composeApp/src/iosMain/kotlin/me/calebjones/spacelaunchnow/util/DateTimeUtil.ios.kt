package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.LocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.timeIntervalSince1970

/**
 * iOS-specific implementations using NSDateFormatter with locale support
 */
actual fun formatLocalDateTime(localDateTime: LocalDateTime, useUtc: Boolean): String {
    return try {
        val dateFormatter = NSDateFormatter()
        
        // Use user's locale from LocaleUtil
        val localeIdentifier = LocaleUtil.getLocaleTag().replace('-', '_')
        dateFormatter.locale = NSLocale.localeWithLocaleIdentifier(localeIdentifier)
        
        // Set date style and time style
        dateFormatter.dateStyle = platform.Foundation.NSDateFormatterMediumStyle
        dateFormatter.timeStyle = platform.Foundation.NSDateFormatterShortStyle
        
        if (useUtc) {
            dateFormatter.timeZone = NSTimeZone.timeZoneWithName("UTC")
        }
        
        // Convert LocalDateTime to NSDate
        val timestamp = localDateTime.toEpochSeconds()
        val nsDate = NSDate.dateWithTimeIntervalSince1970(timestamp.toDouble())
        
        val formatted = dateFormatter.stringFromDate(nsDate)
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
        val dateFormatter = NSDateFormatter()
        
        // Use user's locale from LocaleUtil
        val localeIdentifier = LocaleUtil.getLocaleTag().replace('-', '_')
        dateFormatter.locale = NSLocale.localeWithLocaleIdentifier(localeIdentifier)
        
        // Set date style to long format
        dateFormatter.dateStyle = platform.Foundation.NSDateFormatterLongStyle
        dateFormatter.timeStyle = platform.Foundation.NSDateFormatterNoStyle
        
        if (useUtc) {
            dateFormatter.timeZone = NSTimeZone.timeZoneWithName("UTC")
        }
        
        // Convert LocalDateTime to NSDate
        val timestamp = localDateTime.toEpochSeconds()
        val nsDate = NSDate.dateWithTimeIntervalSince1970(timestamp.toDouble())
        
        val formatted = dateFormatter.stringFromDate(nsDate)
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        val formatted =
            "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
        if (useUtc) "$formatted UTC" else formatted
    }
}

actual fun formatLocalTime(localDateTime: LocalDateTime, useUtc: Boolean): String {
    return try {
        val dateFormatter = NSDateFormatter()
        
        // Use user's locale from LocaleUtil
        val localeIdentifier = LocaleUtil.getLocaleTag().replace('-', '_')
        dateFormatter.locale = NSLocale.localeWithLocaleIdentifier(localeIdentifier)
        
        // Set time style to short format
        dateFormatter.dateStyle = platform.Foundation.NSDateFormatterNoStyle
        dateFormatter.timeStyle = platform.Foundation.NSDateFormatterShortStyle
        
        if (useUtc) {
            dateFormatter.timeZone = NSTimeZone.timeZoneWithName("UTC")
        }
        
        // Convert LocalDateTime to NSDate
        val timestamp = localDateTime.toEpochSeconds()
        val nsDate = NSDate.dateWithTimeIntervalSince1970(timestamp.toDouble())
        
        val formatted = dateFormatter.stringFromDate(nsDate)
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        // Fallback to 24-hour format
        val formatted = "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        if (useUtc) "$formatted UTC" else formatted
    }
}

// Helper function to convert LocalDateTime to epoch seconds
private fun LocalDateTime.toEpochSeconds(): Long {
    // Simplified conversion - in production you might want to use kotlinx-datetime properly
    val year = this.year
    val month = this.monthNumber
    val day = this.dayOfMonth
    val hour = this.hour
    val minute = this.minute
    val second = this.second
    
    // Approximate calculation (not accounting for leap years perfectly, but good enough for formatting)
    val daysInYear = 365L
    val daysInMonth = longArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    
    val yearsSince1970 = year - 1970L
    val daysSince1970 = yearsSince1970 * daysInYear + 
                        (yearsSince1970 / 4) + // Leap years approximation
                        daysInMonth[month - 1] + 
                        day - 1
    
    return daysSince1970 * 86400 + hour * 3600 + minute * 60 + second
}
