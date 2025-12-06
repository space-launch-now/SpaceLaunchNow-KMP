package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.logger
import me.calebjones.spacelaunchnow.platform.ContextFactory
import me.calebjones.spacelaunchnow.data.config.AdMobConfig
import me.calebjones.spacelaunchnow.util.BuildConfig

/**
 * Desktop implementation of GlobalAdManager (no-op stub).
 * Desktop doesn't support ads, so all operations are no-ops.
 */
actual class GlobalAdManager actual constructor(
    private val contextFactory: ContextFactory?
) {
    private val log = logger()
    
    actual fun initializeAndPreload() {
        log.d { "🎯 GlobalAdManager (Desktop): Ads not supported, skipping initialization" }
    }
    
    actual fun shouldShowInterstitialOnDetailView(): Boolean {
        return false // Never show ads on desktop
    }
    
    actual fun resetDetailViewCounter() {
        // No-op
    }
    
    actual fun getDetailViewVisitCount(): Int {
        return 0
    }
    
    actual fun getMinutesSinceLastInterstitial(): Long {
        return 999L // Never shown
    }
    
    actual companion object {
        actual fun getPlatformAdUnitId(adType: AdType): String {
            // Return placeholder ad unit IDs for Desktop (not actually used)
            return when (adType) {
                AdType.BANNER -> AdMobConfig.bannerAdUnitId
                AdType.INTERSTITIAL -> AdMobConfig.interstitialAdUnitId
                AdType.REWARDED -> AdMobConfig.rewardedAdUnitId
            }
        }
    }
}
