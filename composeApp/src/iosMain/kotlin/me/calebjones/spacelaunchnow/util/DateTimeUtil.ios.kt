package me.calebjones.spacelaunchnow.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithName
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone

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
        
        // Set timezone for formatting output
        if (useUtc) {
            dateFormatter.timeZone = NSTimeZone.timeZoneWithName("UTC") 
                ?: NSTimeZone.timeZoneWithName("GMT")
                ?: NSTimeZone.localTimeZone
        } else {
            dateFormatter.timeZone = NSTimeZone.localTimeZone
        }
        
        // Convert LocalDateTime to NSDate - LocalDateTime represents the time in the timezone used to create it
        // We need to convert it back to an Instant using the same timezone context
        val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
        val instant = localDateTime.toInstant(timeZone)
        val nsDate = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        
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
        
        // Set timezone for formatting output
        if (useUtc) {
            dateFormatter.timeZone = NSTimeZone.timeZoneWithName("UTC") 
                ?: NSTimeZone.timeZoneWithName("GMT")
                ?: NSTimeZone.localTimeZone
        } else {
            dateFormatter.timeZone = NSTimeZone.localTimeZone
        }
        
        // Convert LocalDateTime to NSDate using proper timezone context
        val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
        val instant = localDateTime.toInstant(timeZone)
        val nsDate = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        
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
        
        // Set timezone for formatting output
        if (useUtc) {
            dateFormatter.timeZone = NSTimeZone.timeZoneWithName("UTC") 
                ?: NSTimeZone.timeZoneWithName("GMT")
                ?: NSTimeZone.localTimeZone
        } else {
            dateFormatter.timeZone = NSTimeZone.localTimeZone
        }
        
        // Convert LocalDateTime to NSDate using proper timezone context
        val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
        val instant = localDateTime.toInstant(timeZone)
        val nsDate = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        
        val formatted = dateFormatter.stringFromDate(nsDate)
        if (useUtc) "$formatted UTC" else formatted
    } catch (e: Exception) {
        // Fallback to 24-hour format
        val formatted = "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        if (useUtc) "$formatted UTC" else formatted
    }
}
