package me.calebjones.spacelaunchnow.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Android implementation using Java NumberFormat with current locale.
 */
internal actual fun platformFormatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(value)
}
