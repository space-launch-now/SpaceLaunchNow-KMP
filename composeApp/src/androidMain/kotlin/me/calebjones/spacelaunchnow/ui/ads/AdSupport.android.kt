package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.DependsOnGoogleUserMessagingPlatform
import app.lexilabs.basic.ads.ExperimentalBasicAds
import app.lexilabs.basic.ads.composable.ConsentPopup
import app.lexilabs.basic.ads.composable.rememberBannerAd
import app.lexilabs.basic.ads.composable.rememberConsent
import app.lexilabs.basic.ads.composable.rememberInterstitialAd
import app.lexilabs.basic.ads.composable.rememberRewardedAd
import me.calebjones.spacelaunchnow.LocalPreloadedBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedLargeBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedMediumRectangleAd
import me.calebjones.spacelaunchnow.LocalPreloadedNavigationBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedNavigationLargeBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedNavigationLeaderboardAd
import me.calebjones.spacelaunchnow.LocalPreloadedLeaderboardAd
import me.calebjones.spacelaunchnow.LocalPreloadedFullBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedFluidAd
import me.calebjones.spacelaunchnow.LocalPreloadedInterstitialAd
import me.calebjones.spacelaunchnow.LocalPreloadedRewardedAd
import me.calebjones.spacelaunchnow.LocalContextFactory

/**
 * Android implementation of AdConsentPopup using Google UMP.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class, ExperimentalBasicAds::class)
@Composable
actual fun AdConsentPopup(
    onFailure: ((Throwable) -> Unit)?
) {
    val contextFactory = LocalContextFactory.current
    val consent by rememberConsent(activity = contextFactory?.getActivity())
    
    // Show consent popup
    ConsentPopup(
        consent = consent,
        onFailure = { throwable ->
            println("❌ Consent popup failure: ${throwable.message}")
            onFailure?.invoke(throwable)
        }
    )
}

/**
 * Android implementation of WithPreloadedAds.
 * Preloads all ad types and provides them via CompositionLocal.
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
actual fun WithPreloadedAds(
    context: Any?,
    content: @Composable () -> Unit
) {
    // Preload banner ads
    val preloadedBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.BANNER
    )
    val preloadedLargeBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LARGE_BANNER
    )
    val preloadedMediumRectangleAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.MEDIUM_RECTANGLE
    )
    
    // Preload dedicated navigation ads
    val preloadedNavigationBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.BANNER
    )
    val preloadedNavigationLargeBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LARGE_BANNER
    )
    val preloadedNavigationLeaderboardAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LEADERBOARD
    )
    
    // Preload tablet-specific ads
    val preloadedLeaderboardAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LEADERBOARD
    )
    val preloadedFullBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.FULL_BANNER
    )
    val preloadedFluidAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.FLUID
    )
    
    // Preload interstitial and rewarded ads
    val preloadedInterstitialAd by rememberInterstitialAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.INTERSTITIAL)
    )
    val preloadedRewardedAd by rememberRewardedAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.REWARDED)
    )
    
    // Provide all preloaded ads via CompositionLocal
    CompositionLocalProvider(
        LocalPreloadedBannerAd provides preloadedBannerAd,
        LocalPreloadedLargeBannerAd provides preloadedLargeBannerAd,
        LocalPreloadedMediumRectangleAd provides preloadedMediumRectangleAd,
        LocalPreloadedNavigationBannerAd provides preloadedNavigationBannerAd,
        LocalPreloadedNavigationLargeBannerAd provides preloadedNavigationLargeBannerAd,
        LocalPreloadedNavigationLeaderboardAd provides preloadedNavigationLeaderboardAd,
        LocalPreloadedLeaderboardAd provides preloadedLeaderboardAd,
        LocalPreloadedFullBannerAd provides preloadedFullBannerAd,
        LocalPreloadedFluidAd provides preloadedFluidAd,
        LocalPreloadedInterstitialAd provides preloadedInterstitialAd,
        LocalPreloadedRewardedAd provides preloadedRewardedAd
    ) {
        content()
    }
}
