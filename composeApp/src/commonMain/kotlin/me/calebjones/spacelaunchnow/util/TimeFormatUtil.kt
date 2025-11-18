package me.calebjones.spacelaunchnow.util

import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.milliseconds

/**
 * Formats a timestamp into a human-readable "time ago" string
 * 
 * @param timestamp Epoch milliseconds
 * @return Formatted string like "2 hours ago", "3 days ago", etc.
 */
fun formatTimeAgo(timestamp: Long): String {
    val now = System.now().toEpochMilliseconds()
    val diff = (now - timestamp).milliseconds
    
    return when {
        diff.inWholeMinutes < 1 -> "just now"
        diff.inWholeMinutes < 60 -> "${diff.inWholeMinutes} min ago"
        diff.inWholeHours < 24 -> {
            val hours = diff.inWholeHours
            if (hours == 1L) "1 hour ago" else "$hours hours ago"
        }
        diff.inWholeDays < 7 -> {
            val days = diff.inWholeDays
            if (days == 1L) "1 day ago" else "$days days ago"
        }
        diff.inWholeDays < 30 -> {
            val weeks = diff.inWholeDays / 7
            if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
        }
        diff.inWholeDays < 365 -> {
            val months = diff.inWholeDays / 30
            if (months == 1L) "1 month ago" else "$months months ago"
        }
        else -> {
            val years = diff.inWholeDays / 365
            if (years == 1L) "1 year ago" else "$years years ago"
        }
    }
}
