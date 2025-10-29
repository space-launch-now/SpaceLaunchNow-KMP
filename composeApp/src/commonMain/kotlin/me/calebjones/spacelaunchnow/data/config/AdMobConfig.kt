package me.calebjones.spacelaunchnow.data.config

/**
 * AdMob configuration that provides platform-specific ad unit IDs
 */
expect object AdMobConfig {
    /**
     * Get the platform-specific banner ad unit ID
     */
    val bannerAdUnitId: String

    /**
     * Get the platform-specific interstitial ad unit ID
     */
    val interstitialAdUnitId: String

    /**
     * Get the platform-specific rewarded ad unit ID
     */
    val rewardedAdUnitId: String

    /**
     * Get the platform name for debugging
     */
    val platform: String

    /**
     * Whether we're in debug mode
     */
    val isDebug: Boolean
}
