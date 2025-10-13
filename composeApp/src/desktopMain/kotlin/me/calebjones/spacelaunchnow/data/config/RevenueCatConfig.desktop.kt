package me.calebjones.spacelaunchnow.data.config

/**
 * Desktop implementation of RevenueCat configuration
 * Note: RevenueCat doesn't support desktop platforms, so this provides placeholder values
 */
actual object RevenueCatConfig {
    /**
     * Desktop doesn't support RevenueCat, so this is a placeholder
     */
    actual val apiKey: String = "desktop_not_supported"

    actual val platform: String = "Desktop"

    actual val isDebug: Boolean = true
}