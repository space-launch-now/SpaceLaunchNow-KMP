package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

/**
 * iOS implementation using NSNumberFormatter with current locale.
 */
internal actual fun platformFormatNumber(value: Int): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    return formatter.stringFromNumber(NSNumber(value)) ?: value.toString()
}
