package me.calebjones.spacelaunchnow.data.config

/**
 * RevenueCat configuration that provides platform-specific API keys
 */
expect object RevenueCatConfig {
    /**
     * Get the platform-specific RevenueCat API key
     */
    val apiKey: String

    /**
     * Get the platform name for debugging
     */
    val platform: String

    /**
     * Whether we're in debug mode
     */
    val isDebug: Boolean
}