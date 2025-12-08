package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.BannerAdHandler
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.BannerAd
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.LocalPreloadedBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedFluidAd
import me.calebjones.spacelaunchnow.LocalPreloadedFullBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedLargeBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedLeaderboardAd
import me.calebjones.spacelaunchnow.LocalPreloadedMediumRectangleAd
import me.calebjones.spacelaunchnow.LocalPreloadedNavigationBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedNavigationLargeBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedNavigationLeaderboardAd
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getOrientation
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature

/**
 * Android implementation of SmartBannerAd using BasicAds library.
 * 
 * Smart banner ad component that automatically handles:
 * - Premium subscription checking (no ads if user has AD_FREE)
 * - Material 3 styling
 * - Proper sizing for different ad formats based on placement type
 * - Optional "Remove Ads" button for premium conversion
 * - Performance tracking and optimization via GlobalAdManager
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
actual fun SmartBannerAd(
    modifier: Modifier,
    placementType: AdPlacementType,
    showRemoveAdsButton: Boolean,
    showCard: Boolean,
    onRemoveAdsClick: (() -> Unit)?,
    onSizeChanged: ((widthDp: Dp, heightPx: Int) -> Unit)?
) {
    val log = SpaceLogger.getLogger("SmartBannerAd")
    // Convert placement type to AdSize for Android implementation
    val adSize = getAdSizeForPlacement(placementType)
    val contextFactory = LocalContextFactory.current
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    // 🚀 USE PRELOADED ADS: Get preloaded ads from CompositionLocal for instant rendering
    val preloadedBannerAd = LocalPreloadedBannerAd.current
    val preloadedLargeBannerAd = LocalPreloadedLargeBannerAd.current
    val preloadedMediumRectangleAd = LocalPreloadedMediumRectangleAd.current
    val preloadedNavigationBannerAd = LocalPreloadedNavigationBannerAd.current
    val preloadedNavigationLargeBannerAd = LocalPreloadedNavigationLargeBannerAd.current
    val preloadedNavigationLeaderboardAd = LocalPreloadedNavigationLeaderboardAd.current
    val preloadedLeaderboardAd = LocalPreloadedLeaderboardAd.current
    val preloadedFullBannerAd = LocalPreloadedFullBannerAd.current
    val preloadedFluidAd = LocalPreloadedFluidAd.current

    // Get the actual ad size based on placement type
    val actualAdSize = getAdSizeForPlacement(placementType)
    
    log.d { "🎯 SmartBannerAd (iOS): Using AdSize ${actualAdSize.width}x${actualAdSize.height} for placement $placementType" }

    // Don't show ads if:
    // 1. User has ad-free premium feature
    // 2. Not on a mobile platform (Android/iOS)
    // 3. No context factory available
    // 4. No preloaded ads available
    if (hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null ||
        (preloadedBannerAd == null && preloadedLargeBannerAd == null && preloadedMediumRectangleAd == null && 
         preloadedNavigationBannerAd == null && preloadedNavigationLargeBannerAd == null && preloadedNavigationLeaderboardAd == null &&
         preloadedLeaderboardAd == null && preloadedFullBannerAd == null && preloadedFluidAd == null)
    ) {
        log.v { "SmartBannerAd: Not showing ad due to conditions." }
        return
    }

    // Map AdSize dimensions directly to the appropriate preloaded ad
    // This avoids iOS issues with AdSize constant comparisons
    val bannerAd = when {
        // NAVIGATION placement uses dedicated navigation ads
        placementType == AdPlacementType.NAVIGATION -> {
            when {
                actualAdSize.width == 320 && actualAdSize.height == 50 -> preloadedNavigationBannerAd // BANNER
                actualAdSize.width == 468 && actualAdSize.height == 60 -> preloadedNavigationLargeBannerAd // FULL_BANNER
                actualAdSize.width == 728 && actualAdSize.height == 90 -> preloadedNavigationLeaderboardAd // LEADERBOARD
                else -> preloadedNavigationBannerAd // Default fallback
            }
        }
        // All other placements use regular content ads
        else -> {
            when {
                actualAdSize.width == 320 && actualAdSize.height == 50 -> preloadedBannerAd // BANNER
                actualAdSize.width == 320 && actualAdSize.height == 100 -> preloadedLargeBannerAd // LARGE_BANNER
                actualAdSize.width == 300 && actualAdSize.height == 250 -> preloadedMediumRectangleAd // MEDIUM_RECTANGLE
                actualAdSize.width == 468 && actualAdSize.height == 60 -> preloadedFullBannerAd // FULL_BANNER
                actualAdSize.width == 728 && actualAdSize.height == 90 -> preloadedLeaderboardAd // LEADERBOARD
                actualAdSize.height == -2 -> preloadedFluidAd // FLUID (WRAP_CONTENT)
                else -> preloadedBannerAd // Default fallback
            }
        }
    }

    // If the selected ad is not available, try to find any available ad as fallback
    val availableAd = bannerAd ?: run {
        log.w { "SmartBannerAd: Primary ad ($actualAdSize) not available, trying fallbacks" }
        if (placementType == AdPlacementType.NAVIGATION) {
            // For navigation, prefer navigation ads or basic banner ads
            preloadedNavigationBannerAd ?: preloadedNavigationLargeBannerAd ?: preloadedNavigationLeaderboardAd ?: 
            preloadedBannerAd ?: preloadedLargeBannerAd
        } else {
            // For content, try other content ads
            preloadedBannerAd ?: preloadedLargeBannerAd ?: preloadedMediumRectangleAd ?: 
            preloadedLeaderboardAd ?: preloadedFullBannerAd ?: preloadedFluidAd
        }
    }

    // Safety check: ensure we have a banner ad to show
    if (availableAd == null) {
        log.w { "SmartBannerAd: No preloaded ad available for size $actualAdSize - skipping ad display" }
        return
    }

    // 🚀 PERFORMANCE: Fast-path return for failing ads to avoid layout delays
    if (availableAd.state == AdState.FAILING || availableAd.state == AdState.NONE) {
        log.v { "SmartBannerAd: Ad state is ${availableAd.state} - skipping to avoid layout delays" }
        return
    }

    // Debug logging for ad state
    log.v { "SmartBannerAd: Ad state is ${availableAd.state} for placement $placementType" }

    // IMPORTANT: Always render BannerAd Composable to trigger load on iOS
    // Show layout when ad is ready, showing, or loading
    when (availableAd.state) {
        AdState.READY, AdState.SHOWING, AdState.LOADING -> {
            // Show the banner ad with optional remove ads button
            // Note: We render even in LOADING state because iOS needs the Composable rendered to trigger load
            if (showCard) {
                // Content area: wrapped in card
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        SmartBannerAdContent(
                            modifier = modifier.padding(8.dp),
                            adSize = actualAdSize,
                            showRemoveAdsButton = showRemoveAdsButton,
                            onSizeChanged = onSizeChanged,
                            bannerAd = availableAd
                        )
                    }


                    // Optional "Remove Ads" button section
                    if (showRemoveAdsButton && onRemoveAdsClick != null) {
                        OutlinedButton(
                            onClick = onRemoveAdsClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Remove Ads",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                // Navigation area: no card wrapper
                SmartBannerAdContent(
                    modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    adSize = actualAdSize,
                    showRemoveAdsButton = showRemoveAdsButton,
                    onSizeChanged = onSizeChanged,
                    bannerAd = availableAd
                )
            }
            
            // Log loading state for debugging
            if (availableAd.state == AdState.LOADING) {
                log.d { "🔄 SmartBannerAd: Ad is loading for placement $placementType - BannerAd Composable rendered to trigger load" }
            }
        }

        AdState.FAILING, AdState.NONE, AdState.DISMISSED -> {
            // Ad failed to load or was dismissed - don't show anything (no placeholder)
            // This prevents white gaps and invisible barriers when ads fail to load
            log.v { "SmartBannerAd: Ad state is ${availableAd.state} for placement $placementType - hiding ad space" }
            // Don't render anything - let the layout collapse
        }

        else -> {
            // Unknown state or SHOWN (already displayed)
            log.w { "SmartBannerAd: Unknown ad state ${availableAd.state} for placement $placementType" }
        }
    }
}

@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun SmartBannerAdContent(
    modifier: Modifier = Modifier,
    adSize: AdSize,
    showRemoveAdsButton: Boolean,
    onSizeChanged: ((Dp, Int) -> Unit)?,
    bannerAd: BannerAdHandler,
) {
    val log = SpaceLogger.getLogger("SmartBannerAdContent")
    // Log AdSize dimensions (can't rely on == comparison on iOS since AdSize constants aren't singletons)
    log.v { "SmartBannerAdContent: adSize dimensions = ${adSize.width}x${adSize.height}" }
    
    BoxWithConstraints {
        val availableWidthDp = maxWidth
        val availableHeightDp = maxHeight

        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            // Banner Ad Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = if (showRemoveAdsButton) 0.dp else 4.dp, // Only elevate if no card wrapper
                shape = MaterialTheme.shapes.small
            ) {
                // Use AdSize height directly - simple and works on all platforms
                val bannerHeight = when {
                    adSize.height > 0 -> adSize.height.dp // Use actual height if positive
                    adSize.height == -2 -> 250.dp // FLUID (WRAP_CONTENT) - use medium rectangle height
                    else -> 50.dp // Fallback to standard banner size
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                        .onSizeChanged { size ->
                            // Optional: Log the actual size for debugging
                            log.v { "SmartBannerAd: Available width: ${availableWidthDp}, Height: ${size.height}px, Banner height: ${bannerHeight}" }
                            // Call custom callback if provided
                            onSizeChanged?.invoke(availableWidthDp, size.height)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Constrain BannerAd to exact size to prevent expansion
                    BannerAd(
                        ad = bannerAd
                    )
                }
            }
        }
    }
}

/**
 * Get the appropriate ad size based on placement type and device characteristics
 * Returns the actual AdSize constant to use (no comparisons needed)
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun getAdSizeForPlacement(placementType: AdPlacementType): AdSize {
    val log = SpaceLogger.getLogger("getAdSizeForPlacement")
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val widthSizeClass = windowSizeClass.windowWidthSizeClass

    log.v { "SmartBannerAd: Placement=$placementType, WidthClass=$widthSizeClass" }

    return when (placementType) {
        AdPlacementType.NAVIGATION -> {
            when (widthSizeClass) {
                WindowWidthSizeClass.COMPACT -> AdSize.BANNER // 320x50 for phones
                WindowWidthSizeClass.MEDIUM -> AdSize.FULL_BANNER // 468x60 for medium tablets
                WindowWidthSizeClass.EXPANDED -> AdSize.LEADERBOARD // 728x90 for large tablets/desktop
                else -> AdSize.BANNER
            }
        }

        AdPlacementType.CONTENT -> {
            when (widthSizeClass) {
                WindowWidthSizeClass.COMPACT -> AdSize.BANNER // 320x50 for phones
                WindowWidthSizeClass.MEDIUM -> AdSize.MEDIUM_RECTANGLE // 300x250 for tablets
                WindowWidthSizeClass.EXPANDED -> AdSize.MEDIUM_RECTANGLE // 300x250 for large tablets
                else -> AdSize.BANNER
            }
        }

        AdPlacementType.FEED -> {
            when (widthSizeClass) {
                WindowWidthSizeClass.COMPACT -> AdSize.LARGE_BANNER // 320x100 for phones
                WindowWidthSizeClass.MEDIUM -> AdSize.MEDIUM_RECTANGLE // 300x250 for tablets
                WindowWidthSizeClass.EXPANDED -> AdSize.MEDIUM_RECTANGLE // 300x250 for large tablets
                else -> AdSize.LARGE_BANNER
            }
        }

        AdPlacementType.INTERSTITIAL -> {
            when (widthSizeClass) {
                WindowWidthSizeClass.COMPACT -> AdSize.MEDIUM_RECTANGLE // 300x250 for phones
                WindowWidthSizeClass.MEDIUM -> AdSize.MEDIUM_RECTANGLE // 300x250 for tablets
                WindowWidthSizeClass.EXPANDED -> AdSize.LEADERBOARD // 728x90 for large tablets
                else -> AdSize.MEDIUM_RECTANGLE
            }
        }
    }
}

/**
 * Helper function to choose appropriate ad size based on device characteristics (legacy)
 * @deprecated Use getAdSizeForPlacement with AdPlacementType instead
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
@Deprecated("Use getAdSizeForPlacement with AdPlacementType.FEED instead")
fun getRecommendedAdSize(): AdSize = getAdSizeForPlacement(AdPlacementType.FEED)

/**
 * Content-aware ad size selection for content areas (legacy)
 * @deprecated Use getAdSizeForPlacement with AdPlacementType.CONTENT instead
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
@Deprecated("Use getAdSizeForPlacement with AdPlacementType.CONTENT instead")
fun getContentAdSize(): AdSize = getAdSizeForPlacement(AdPlacementType.CONTENT)

/**
 * Navigation-specific ad size selection (legacy)
 * @deprecated Use getAdSizeForPlacement with AdPlacementType.NAVIGATION instead
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
@Deprecated("Use getAdSizeForPlacement with AdPlacementType.NAVIGATION instead")
fun getNavigationAdSize(): AdSize = getAdSizeForPlacement(AdPlacementType.NAVIGATION)