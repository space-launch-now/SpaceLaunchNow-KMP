package me.calebjones.spacelaunchnow.util

/**
 * Utility for getting the user's locale information in a platform-specific way
 */
object LocaleUtil {
    /**
     * Gets the user's current locale as a language tag (e.g., "en-US", "fr-FR", "ja-JP")
     * This follows BCP 47 language tag format when possible
     */
    fun getLocaleTag(): String = getPlatformLocaleTag()
    
    /**
     * Gets just the language code (e.g., "en", "fr", "ja")
     */
    fun getLanguageCode(): String = getPlatformLanguageCode()
    
    /**
     * Gets the country/region code if available (e.g., "US", "FR", "JP")
     * Returns empty string if not available
     */
    fun getCountryCode(): String = getPlatformCountryCode()
}

/**
 * Platform-specific implementation to get the full locale tag
 */
expect fun getPlatformLocaleTag(): String

/**
 * Platform-specific implementation to get the language code
 */
expect fun getPlatformLanguageCode(): String

/**
 * Platform-specific implementation to get the country code
 */
expect fun getPlatformCountryCode(): String
