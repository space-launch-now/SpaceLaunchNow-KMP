package me.calebjones.spacelaunchnow.util

import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit

/**
 * Utility extensions for formatting RevenueCat [Period] values into
 * human-readable display strings.
 *
 * Used primarily for subscription free trial and intro offer durations.
 */

/**
 * Converts a [Period] to a hyphenated display string suitable for badges.
 *
 * Examples: "3-day", "1-week", "1-month", "1-year"
 */
fun Period.toDisplayString(): String = when (unit) {
    PeriodUnit.DAY -> if (value == 1) "1-day" else "$value-day"
    PeriodUnit.WEEK -> if (value == 1) "1-week" else "$value-week"
    PeriodUnit.MONTH -> if (value == 1) "1-month" else "$value-month"
    PeriodUnit.YEAR -> if (value == 1) "1-year" else "$value-year"
    PeriodUnit.UNKNOWN -> "$value"
}

/**
 * Converts a [Period] to a readable string with proper pluralization.
 *
 * Examples: "3 days", "1 week", "1 month", "1 year"
 */
fun Period.toReadableString(): String = when (unit) {
    PeriodUnit.DAY -> if (value == 1) "1 day" else "$value days"
    PeriodUnit.WEEK -> if (value == 1) "1 week" else "$value weeks"
    PeriodUnit.MONTH -> if (value == 1) "1 month" else "$value months"
    PeriodUnit.YEAR -> if (value == 1) "1 year" else "$value years"
    PeriodUnit.UNKNOWN -> "$value"
}
