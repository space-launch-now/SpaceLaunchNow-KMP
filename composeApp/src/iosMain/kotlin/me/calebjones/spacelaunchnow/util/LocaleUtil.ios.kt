package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.countryCode
import platform.Foundation.localeIdentifier

/**
 * iOS implementation using NSLocale
 */
actual fun getPlatformLocaleTag(): String {
    return try {
        val locale = NSLocale.currentLocale
        // Get the locale identifier (e.g., "en_US") and convert to language tag format (e.g., "en-US")
        (locale.localeIdentifier as? String)?.replace('_', '-') ?: "en-US"
    } catch (e: Exception) {
        "en-US" // Fallback
    }
}

actual fun getPlatformLanguageCode(): String {
    return try {
        val locale = NSLocale.currentLocale
        (locale.languageCode as? String) ?: "en"
    } catch (e: Exception) {
        "en" // Fallback
    }
}

actual fun getPlatformCountryCode(): String {
    return try {
        val locale = NSLocale.currentLocale
        (locale.countryCode as? String) ?: ""
    } catch (e: Exception) {
        "" // Return empty string if not available
    }
}
