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
            "DATADOG_CLIENT_TOKEN" -> AppSecrets.dataDogClientToken
            "DATADOG_APPLICATION_ID" -> AppSecrets.dataDogApplicationId
            "DATADOG_ENVIRONMENT" -> AppSecrets.dataDogEnv
            "TOTP_SECRET" -> AppSecrets.totpSecret
            "MAPS_API_KEY" -> AppSecrets.mapsApiKey
            else -> defaultValue
        }
    }

    /**
     * Gets a boolean environment variable
     */
    fun getEnvBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return when (key) {
            "DATADOG_ENABLED" -> AppSecrets.datadogEnabled
            else -> defaultValue
        }
    }
}

