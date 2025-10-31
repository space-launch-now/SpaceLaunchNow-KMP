package me.calebjones.spacelaunchnow.util

import kotlin.math.roundToInt

/**
 * Utility for formatting numbers with locale-appropriate thousand separators.
 * Uses expect/actual pattern for platform-specific implementations.
 */
object NumberFormatUtil {
    /**
     * Formats an integer with thousand separators based on current locale.
     * Examples: 1234567 -> "1,234,567" (US) or "1.234.567" (DE)
     */
    fun formatNumber(value: Int): String = platformFormatNumber(value)

    /**
     * Formats a double as an integer with thousand separators based on current locale.
     */
    fun formatNumber(value: Double): String = platformFormatNumber(value.roundToInt())

    /**
     * Formats a number with optional suffix (like "kg", "km", etc.)
     */
    fun formatNumberWithUnit(value: Int, unit: String): String {
        return "${formatNumber(value)} $unit"
    }

    /**
     * Formats a number with optional suffix (like "kg", "km", etc.)
     */
    fun formatNumberWithUnit(value: Double, unit: String): String {
        return "${formatNumber(value)} $unit"
    }
}

/**
 * Platform-specific number formatting implementation.
 */
internal expect fun platformFormatNumber(value: Int): String
