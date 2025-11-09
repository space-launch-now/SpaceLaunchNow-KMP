package me.calebjones.spacelaunchnow.util

/**
 * Example usage of LocaleUtil
 * 
 * This demonstrates how to get the user's locale information
 * across different platforms (Android, iOS, Desktop)
 */
fun exampleLocaleUsage() {
    // Get the full locale tag (e.g., "en-US", "fr-FR", "ja-JP")
    val localeTag = LocaleUtil.getLocaleTag()
    println("User's locale: $localeTag")
    
    // Get just the language code (e.g., "en", "fr", "ja")
    val language = LocaleUtil.getLanguageCode()
    println("User's language: $language")
    
    // Get the country code (e.g., "US", "FR", "JP")
    val country = LocaleUtil.getCountryCode()
    println("User's country: $country")
    
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
    println("Greeting: $greeting")
    
    // Example: Use locale tag for API calls or analytics
    println("Analytics locale: $localeTag")
}
