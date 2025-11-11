package me.calebjones.spacelaunchnow.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * Android implementation using Java NumberFormat with current locale.
 */
internal actual fun platformFormatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(value)
}

/**
 * Android implementation for decimal formatting with specified decimal places.
 */
internal actual fun platformFormatDecimal(value: Double, decimalPlaces: Int): String {
    val pattern = buildString {
        append("#")
        if (decimalPlaces > 0) {
            append(".")
            repeat(decimalPlaces) { append("#") }
        }
    }
    val formatter = DecimalFormat(pattern)
    return formatter.format(value)
}
