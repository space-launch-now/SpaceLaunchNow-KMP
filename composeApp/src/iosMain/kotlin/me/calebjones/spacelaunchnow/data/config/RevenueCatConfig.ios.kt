package me.calebjones.spacelaunchnow.data.config

import me.calebjones.spacelaunchnow.util.AppSecrets
import me.calebjones.spacelaunchnow.util.BuildConfig

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
            return key
        }

    actual val platform: String = "iOS"

    // iOS debug mode is read from Secrets.plist at app initialization
    // Unlike Android (automatic via build type), iOS requires explicit DEBUG configuration
    actual val isDebug: Boolean
        get() = BuildConfig.IS_DEBUG
}