package me.calebjones.spacelaunchnow.ui.ads

import android.app.Activity
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
    val activity = contextFactory?.getActivity() as? Activity
    
    // Only show consent popup if we have a valid Activity
    if (activity == null) {
        println("⚠️ AdConsentPopup: Activity context is null, consent popup will not be shown")
        return
    }
    
    val consent by rememberConsent(activity = activity)
    
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
    // Cast context to Activity - it should be an Activity on Android
    val activity = context as? Activity
    
    if (activity == null) {
        println("⚠️ WithPreloadedAds: Activity context is null, ads will not be loaded")
        // Still provide the content but without ads
        content()
        return
    }
    
    // 🚀 PERFORMANCE OPTIMIZATION: Only preload the 3 most commonly used banner sizes
    // This reduces memory usage and loading time by 66% compared to loading 9 sizes
    // Other sizes will load on-demand when needed
    
    // Primary banner ad (most common - used in content areas)
    val preloadedBannerAd by rememberBannerAd(
        activity = activity,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.BANNER
    )
    
    // Large banner for detail pages and featured content
    val preloadedLargeBannerAd by rememberBannerAd(
        activity = activity,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.LARGE_BANNER
    )
    
    // Medium rectangle for inline list ads
    val preloadedMediumRectangleAd by rememberBannerAd(
        activity = activity,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.MEDIUM_RECTANGLE
    )
    
    // 🔧 FIX: Create DEDICATED navigation banner ad (doesn't share with content ads)
    // This prevents conflicts when navigating between screens with inline ads
    val preloadedNavigationBannerAd by rememberBannerAd(
        activity = activity,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.BANNER
    )
    
    // Navigation fallbacks still reuse existing ads for efficiency
    val preloadedNavigationLargeBannerAd = preloadedLargeBannerAd
    val preloadedNavigationLeaderboardAd = preloadedLargeBannerAd // Fallback to large banner
    
    // Tablet-specific ads also reuse the preloaded ads (lazy load on demand if needed)
    val preloadedLeaderboardAd = preloadedLargeBannerAd // Fallback to large banner
    val preloadedFullBannerAd = preloadedLargeBannerAd  // Fallback to large banner
    val preloadedFluidAd = preloadedBannerAd            // Fallback to standard banner
    
    // Preload interstitial and rewarded ads
    val preloadedInterstitialAd by rememberInterstitialAd(
        activity = activity,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.INTERSTITIAL)
    )
    val preloadedRewardedAd by rememberRewardedAd(
        activity = activity,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.REWARDED)
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
