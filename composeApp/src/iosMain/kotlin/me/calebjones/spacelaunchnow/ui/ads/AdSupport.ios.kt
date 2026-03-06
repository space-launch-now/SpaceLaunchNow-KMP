package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.lexilabs.basic.ads.AdSize
import kotlinx.coroutines.delay
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

private val log by lazy { SpaceLogger.getLogger("AdSupport") }

/**
 * iOS implementation of AdConsentPopup using Google UMP.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class, ExperimentalBasicAds::class)
@Composable
actual fun AdConsentPopup(
    onFailure: ((Throwable) -> Unit)?,
    onConsentResolved: (() -> Unit)?
) {
    val contextFactory = LocalContextFactory.current
    var viewController by remember { mutableStateOf<Any?>(null) }

    // Retry up to 5 times with 500ms gaps to handle the startup race where the
    // UIWindowScene is not yet foreground-active when this first composes.
    LaunchedEffect(Unit) {
        repeat(5) { attempt ->
            val vc = contextFactory?.getActivity()
            if (vc != null && vc != "") {
                viewController = vc
                return@LaunchedEffect
            }
            if (attempt < 4) delay(500)
        }
        if (viewController == null) {
            log.w { "⚠️ AdConsentPopup: ViewController is null after retries, consent popup will not be shown" }
            // Resolve to avoid blocking ad loading indefinitely
            onConsentResolved?.invoke()
        }
    }

    val vc = viewController ?: return

    val consent by rememberConsent(activity = vc)

    // CR-2: Call onConsentResolved when UMP signals ads can be requested
    LaunchedEffect(consent.canRequestAds) {
        if (consent.canRequestAds) {
            onConsentResolved?.invoke()
        }
    }

    ConsentPopup(
        consent = consent,
        onFailure = { throwable ->
            log.e(throwable) { "❌ Consent popup failure" }
            onFailure?.invoke(throwable)
            // Also resolve on failure to avoid blocking ad loading indefinitely
            onConsentResolved?.invoke()
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
    shouldPreloadAds: Boolean,
    content: @Composable () -> Unit
) {
    if (!shouldPreloadAds) {
        content()
        return
    }

    // 🚀 PERFORMANCE OPTIMIZATION: Only preload the 3 most commonly used banner sizes
    // This reduces memory usage and loading time by 66% compared to loading 9 sizes
    // Other sizes will load on-demand when needed
    
    // Primary banner ad (most common - used in content areas)
    val preloadedBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.BANNER
    )
    
    // Large banner for detail pages and featured content
    val preloadedLargeBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.LARGE_BANNER
    )
    
    // Medium rectangle for inline list ads
    val preloadedMediumRectangleAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.MEDIUM_RECTANGLE
    )
    
    // 🔧 FIX: Create DEDICATED navigation banner ad (doesn't share with content ads)
    // This prevents conflicts when navigating between screens with inline ads
    val preloadedNavigationBannerAd by rememberBannerAd(
        activity = context,
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
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.INTERSTITIAL)
    )
    val preloadedRewardedAd by rememberRewardedAd(
        activity = context,
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
