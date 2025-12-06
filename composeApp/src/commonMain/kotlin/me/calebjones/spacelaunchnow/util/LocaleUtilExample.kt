package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("LocaleUtilExample") }

/**
 * Example usage of LocaleUtil
 * 
 * This demonstrates how to get the user's locale information
 * across different platforms (Android, iOS, Desktop)
 */
fun exampleLocaleUsage() {
    // Get the full locale tag (e.g., "en-US", "fr-FR", "ja-JP")
    val localeTag = LocaleUtil.getLocaleTag()
    log.d { "User's locale: $localeTag" }
    
    // Get just the language code (e.g., "en", "fr", "ja")
    val language = LocaleUtil.getLanguageCode()
    log.d { "User's language: $language" }
    
    // Get the country code (e.g., "US", "FR", "JP")
    val country = LocaleUtil.getCountryCode()
    log.d { "User's country: $country" }
    
    // Example: Customize content based on language
    val greeting = when (language) {
        "en" -> "Hello"
        "es" -> "Hola"
        "fr" -> "Bonjour"
        "de" -> "Hallo"
        "ja" -> "こんにちは"
        "zh" -> "你好"
        else -> "Hello"
    }
    log.d { "Greeting: $greeting" }
    
    // Example: Use locale tag for API calls or analytics
    log.d { "Analytics locale: $localeTag" }
}
