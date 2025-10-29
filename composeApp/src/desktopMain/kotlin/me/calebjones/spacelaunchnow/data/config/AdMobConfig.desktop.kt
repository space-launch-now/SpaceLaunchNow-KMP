package me.calebjones.spacelaunchnow.data.config

import app.lexilabs.basic.ads.AdUnitId

/**
 * Desktop implementation of AdMob configuration
 * Desktop doesn't support ads, so we use test IDs
 */
actual object AdMobConfig {
    actual val bannerAdUnitId: String = AdUnitId.BANNER_DEFAULT
    actual val interstitialAdUnitId: String = AdUnitId.INTERSTITIAL_DEFAULT
    actual val rewardedAdUnitId: String = AdUnitId.REWARDED_DEFAULT

    actual val platform: String = "Desktop"
    actual val isDebug: Boolean = true
}
