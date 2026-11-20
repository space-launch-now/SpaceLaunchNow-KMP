package me.calebjones.spacelaunchnow.data.config

import me.calebjones.spacelaunchnow.platform.AndroidBuildConfig

/**
 * Android implementation of AdMob configuration
 * Ad unit IDs are loaded from .env file via BuildConfig
 */
actual object AdMobConfig {
    /**
     * Android banner ad unit ID from .env file
     * Set ANDROID_BANNER_AD_UNIT_ID in .env file
     */
    actual val bannerAdUnitId: String = AndroidBuildConfig.ANDROID_BANNER_AD_UNIT_ID

    /**
     * Android interstitial ad unit ID from .env file
     * Set ANDROID_INTERSTITIAL_AD_UNIT_ID in .env file
     */
    actual val interstitialAdUnitId: String = AndroidBuildConfig.ANDROID_INTERSTITIAL_AD_UNIT_ID

    /**
     * Android rewarded ad unit ID from .env file
     * Set ANDROID_REWARDED_AD_UNIT_ID in .env file
     */
    actual val rewardedAdUnitId: String = AndroidBuildConfig.ANDROID_REWARDED_AD_UNIT_ID

    actual val platform: String = "Android"

    actual val isDebug: Boolean = AndroidBuildConfig.IS_DEBUG
}
