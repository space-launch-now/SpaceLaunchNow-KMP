package me.calebjones.spacelaunchnow.data.config

import me.calebjones.spacelaunchnow.util.AppSecrets

/**
 * iOS implementation of RevenueCat configuration
 * API key is loaded from Secrets.plist via AppSecrets
 */
actual object RevenueCatConfig {
    /**
     * iOS RevenueCat API key from Secrets.plist
     * Set revenueCatIosKey in Secrets.plist (generated from .env)
     */
    actual val apiKey: String
        get() {
            val key = AppSecrets.revenueCatIosKey
            println("RevenueCatConfig.apiKey loaded: ${if (key.isNotEmpty()) "${key.take(10)}..." else "EMPTY"}")
            return key
        }

    actual val platform: String = "iOS"

    // For iOS, we'll use a simple debug check
    // You can enhance this based on your build configuration
    actual val isDebug: Boolean = true // TODO: Set based on your iOS build configuration
}