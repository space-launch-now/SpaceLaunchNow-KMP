package me.calebjones.spacelaunchnow.data.config

import me.calebjones.spacelaunchnow.BuildConfig

/**
 * Android implementation of RevenueCat configuration
 * API key is loaded from .env file via BuildConfig
 */
actual object RevenueCatConfig {
    /**
     * Android RevenueCat API key from .env file
     * Set REVENUECAT_ANDROID_KEY in .env file
     */
    actual val apiKey: String = BuildConfig.REVENUECAT_ANDROID_KEY

    actual val platform: String = "Android"

    actual val isDebug: Boolean = BuildConfig.IS_DEBUG
}