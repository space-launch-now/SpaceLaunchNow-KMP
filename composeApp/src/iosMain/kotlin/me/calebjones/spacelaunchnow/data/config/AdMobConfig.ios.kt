package me.calebjones.spacelaunchnow.data.config

import me.calebjones.spacelaunchnow.util.AppSecrets

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

    // For iOS, we'll use a simple debug check
    // You can enhance this based on your build configuration
    actual val isDebug: Boolean = true // TODO: Set based on your iOS build configuration
}
