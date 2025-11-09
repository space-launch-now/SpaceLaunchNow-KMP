package me.calebjones.spacelaunchnow.util

import java.util.Locale

/**
 * Desktop (JVM) implementation using Java's Locale class
 */
actual fun getPlatformLocaleTag(): String {
    return try {
        Locale.getDefault().toLanguageTag()
    } catch (e: Exception) {
        "en-US" // Fallback
    }
}

actual fun getPlatformLanguageCode(): String {
    return try {
        Locale.getDefault().language
    } catch (e: Exception) {
        "en" // Fallback
    }
}

actual fun getPlatformCountryCode(): String {
    return try {
        Locale.getDefault().country
    } catch (e: Exception) {
        "" // Return empty string if not available
    }
}
