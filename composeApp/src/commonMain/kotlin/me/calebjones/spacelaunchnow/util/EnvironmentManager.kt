package me.calebjones.spacelaunchnow.util

/**
 * Utility for managing environment variables across platforms
 * Note: For now, returns a hardcoded API key since .env loading is platform-specific
 */
object EnvironmentManager {
    /**
     * Gets an environment variable
     * Falls back to defaultValue if not found
     * TODO: Implement proper .env loading per platform
     */
    fun getEnv(key: String, defaultValue: String = ""): String {
        return when (key) {
            "API_KEY" -> AppSecrets.apiKey
            "REVENUECAT_ANDROID_KEY" -> AppSecrets.revenueCatAndroidKey
            "REVENUECAT_IOS_KEY" -> AppSecrets.revenueCatIosKey
            else -> defaultValue
        }
    }
}

