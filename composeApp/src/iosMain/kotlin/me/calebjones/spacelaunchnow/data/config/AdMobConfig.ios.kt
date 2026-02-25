package me.calebjones.spacelaunchnow.data.config

import me.calebjones.spacelaunchnow.util.AppSecrets
import me.calebjones.spacelaunchnow.util.BuildConfig

/**
 * iOS implementation of AdMob configuration
 * Ad unit IDs are loaded from Secrets.plist via AppSecrets
 */
actual object AdMobConfig {
    /**
     * iOS banner ad unit ID from Secrets.plist
     * Set iosBannerAdUnitId in Secrets.plist (generated from .env)
     */
    actual val bannerAdUnitId: String
        get() = AppSecrets.iosBannerAdUnitId

    /**
     * iOS interstitial ad unit ID from Secrets.plist
     * Set iosInterstitialAdUnitId in Secrets.plist (generated from .env)
     */
    actual val interstitialAdUnitId: String
        get() = AppSecrets.iosInterstitialAdUnitId

    /**
     * iOS rewarded ad unit ID from Secrets.plist
     * Set iosRewardedAdUnitId in Secrets.plist (generated from .env)
     */
    actual val rewardedAdUnitId: String
        get() = AppSecrets.iosRewardedAdUnitId

    actual val platform: String = "iOS"

    // iOS debug mode is read from Secrets.plist at app initialization
    // Unlike Android (automatic via build type), iOS requires explicit DEBUG configuration
    actual val isDebug: Boolean
        get() = BuildConfig.IS_DEBUG
}
