package me.calebjones.spacelaunchnow.data.config

/**
 * Desktop implementation of AdMob configuration
 * Desktop doesn't support ads, so we use placeholder IDs
 */
actual object AdMobConfig {
    // Desktop placeholder ad unit IDs (not used since ads aren't supported)
    actual val bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111"
    actual val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712"
    actual val rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917"

    actual val platform: String = "Desktop"
    actual val isDebug: Boolean = true
}
