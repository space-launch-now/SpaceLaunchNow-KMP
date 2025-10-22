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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import me.calebjones.spacelaunchnow.getScreenWidth
import me.calebjones.spacelaunchnow.isTablet
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature

/**
 * Ad placement types for context-aware ad sizing
 */
enum class AdPlacementType {
    NAVIGATION,     // Bottom nav, navigation rail - compact ads
    CONTENT,        // Article content, detail pages - larger content ads
    FEED,           // Home page, lists - medium visibility ads
    INTERSTITIAL    // Between content sections - attention-grabbing ads
}

/**
 * Smart banner ad component that automatically handles:
 * - Premium subscription checking (no ads if user has AD_FREE)
 * - Platform detection (shows on Android/iOS, hidden on Desktop)
 * - Context factory validation
 * - Material 3 styling
 * - Proper sizing for different ad formats
 * - Optional "Remove Ads" button for premium conversion
 * - Performance tracking and optimization via GlobalAdManager
 *
 * This component uses GlobalAdManager for performance tracking and optimization
 * to provide the fastest possible ad loading experience.
 *
 * Supported ad sizes:
 * - BANNER (320x50) - Standard banner for phones & tablets
 * - LARGE_BANNER (320x100) - Large banner for phones & tablets
 * - MEDIUM_RECTANGLE (300x250) - IAB medium rectangle for phones & tablets
 * - FULL_BANNER (468x60) - IAB full-size banner for tablets
 * - LEADERBOARD (728x90) - IAB leaderboard for tablets
 * - WIDE_SKYSCRAPER (160x600) - IAB wide skyscraper
 * - FLUID - Dynamic size that adapts to content
 *
 * Usage patterns:
 * - Navigation/Bottom areas: SmartBannerAd(showCard = false) (no card wrapper)
 * - Content areas: SmartBannerAd(showRemoveAdsButton = true, onRemoveAdsClick = {...}) (with card)
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun SmartBannerAd(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    placementType: AdPlacementType? = null, // If provided, overrides adSize
    showRemoveAdsButton: Boolean = false, // Default: no button (e.g., BottomNavigationBar)
    showCard: Boolean = true, // Default: show card wrapper (disable for navigation areas)
    onRemoveAdsClick: (() -> Unit)? = null,
    onSizeChanged: ((widthDp: Dp, heightPx: Int) -> Unit)? = null
) {
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

    // Determine the actual ad size to use - placementType overrides adSize
    val actualAdSize = placementType?.let { getAdSizeForPlacement(it) } ?: adSize

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
        println("⚠️ SmartBannerAd: Not showing ad due to conditions.")
        return
    }

    // Select the appropriate preloaded ad based on actual ad size and placement type
    val bannerAd = if (placementType == AdPlacementType.NAVIGATION) {
        // Use dedicated navigation ads to avoid conflicts with content ads
        when (actualAdSize) {
            AdSize.BANNER -> preloadedNavigationBannerAd
            AdSize.LARGE_BANNER -> preloadedNavigationLargeBannerAd
            AdSize.LEADERBOARD -> preloadedNavigationLeaderboardAd
            AdSize.FULL_BANNER -> preloadedNavigationLargeBannerAd // Fallback to large banner
            else -> preloadedNavigationBannerAd // Default to standard navigation banner
        }
    } else {
        // Use regular ads for content areas
        when (actualAdSize) {
            AdSize.BANNER -> preloadedBannerAd
            AdSize.LARGE_BANNER -> preloadedLargeBannerAd
            AdSize.MEDIUM_RECTANGLE -> preloadedMediumRectangleAd
            AdSize.LEADERBOARD -> preloadedLeaderboardAd
            AdSize.FULL_BANNER -> preloadedFullBannerAd
            AdSize.FLUID -> preloadedFluidAd
            else -> preloadedBannerAd // Fallback to standard banner
        }
    }

    // If the selected ad is not available, try to find any available ad as fallback
    val availableAd = bannerAd ?: run {
        println("⚠️ SmartBannerAd: Primary ad ($actualAdSize) not available, trying fallbacks")
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
        println("⚠️ SmartBannerAd: No preloaded ad available for size $actualAdSize")
        return
    }

    // Debug logging for ad state
    println("🎯 SmartBannerAd: Ad state is ${availableAd.state} for placement $placementType")

    // Show layout when ad is ready, showing, or loading
    when (availableAd.state) {
        AdState.READY, AdState.SHOWING -> {
            // Show the banner ad with optional remove ads button
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
        }

        AdState.LOADING -> {
            // Ad is loading, show a placeholder or try to reload
            println("🔄 SmartBannerAd: Ad is loading for placement $placementType")
            // For navigation ads, we can show a minimal placeholder or just wait
            if (!showCard) {
                // Navigation area: show minimal space to avoid layout jumps
                Box(
                    modifier = modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(60.dp) // Standard banner height
                )
            }
        }

        AdState.FAILING, AdState.NONE, AdState.DISMISSED -> {
            // Ad failed to load or was dismissed
            println("⚠️ SmartBannerAd: Ad state is ${availableAd.state} for placement $placementType - ad may need reloading at app level")
            
            // For navigation ads, reserve some space to avoid layout jumps
            if (!showCard) {
                Box(
                    modifier = modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(60.dp) // Standard banner height
                )
            }
        }

        else -> {
            // Unknown state or SHOWN (already displayed)
            println("❓ SmartBannerAd: Unknown ad state ${availableAd.state} for placement $placementType")
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
    println(
        "SmartBannerAdContent: adSize = " + when (adSize) {
            AdSize.BANNER -> "BANNER"
            AdSize.LARGE_BANNER -> "LARGE_BANNER"
            AdSize.MEDIUM_RECTANGLE -> "MEDIUM_RECTANGLE"
            AdSize.FULL_BANNER -> "FULL_BANNER"
            AdSize.LEADERBOARD -> "LEADERBOARD"
            AdSize.WIDE_SKYSCRAPER -> "WIDE_SKYSCRAPER"
            AdSize.FLUID -> "FLUID"
            else -> "ADAPTIVE_OR_CUSTOM: $adSize"
        }
    )
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            when (adSize) {
                                AdSize.BANNER -> 50.dp // 320x50 - Standard banner (phones & tablets)
                                AdSize.LARGE_BANNER -> 100.dp // 320x100 - Large banner (phones & tablets)
                                AdSize.MEDIUM_RECTANGLE -> 250.dp // 300x250 - IAB medium rectangle (phones & tablets)
                                AdSize.FULL_BANNER -> 60.dp // 468x60 - IAB full-size banner (tablets)
                                AdSize.LEADERBOARD -> 90.dp // 728x90 - IAB leaderboard (tablets)
                                AdSize.WIDE_SKYSCRAPER -> 600.dp // 160x600 - IAB wide skyscraper
                                AdSize.FLUID -> availableHeightDp // Dynamic size - start with reasonable default height
                                else -> availableHeightDp // Default to standard banner size
                            }
                        )
                        .onSizeChanged { size ->
                            // Optional: Log the actual size for debugging
                            println("SmartBannerAd: Available width: ${availableWidthDp}, Height: ${size.height}px")
                            // Call custom callback if provided
                            onSizeChanged?.invoke(availableWidthDp, size.height)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BannerAd(ad = bannerAd)
                }
            }
        }
    }
}

/**
 * Get the appropriate ad size based on placement type and device characteristics
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun getAdSizeForPlacement(placementType: AdPlacementType): AdSize {
    val isTabletDevice = isTablet()
    val screenWidthDp = getScreenWidth()
    // 1 is portrait, 2 is landscape
    val orientation = getOrientation()

    println("SmartBannerAd: isTabletDevice: $isTabletDevice, screenWidthDp: $screenWidthDp, orientation: $orientation")

    // Cap the calculated max width to sensible ad breakpoints considering orientation
    val maxWidth = if (isTabletDevice) {
        if (orientation == 1) {
            // Portrait mode: use more conservative ad sizes even on wide tablets
            when {
                screenWidthDp > 468.dp -> 468.dp // Max full banner size in portrait
                screenWidthDp > 320.dp -> 320.dp // Standard banner size
                else -> screenWidthDp
            }
        } else {
            // Landscape mode: can use larger ad sizes
            when {
                screenWidthDp > 728.dp -> 728.dp // Leaderboard size
                else -> screenWidthDp
            }
        }
    } else {
        // Phones: keep existing logic
        if (screenWidthDp > 320.dp) 320.dp else screenWidthDp
    }

    return when (placementType) {
        AdPlacementType.NAVIGATION -> {
            // Navigation areas: compact, non-intrusive ads
            when {
                isTabletDevice -> {
                    if (maxWidth >= 728.dp) {
                        println("SmartBannerAdSize: Leaderboard")
                        AdSize.LEADERBOARD // Use leaderboard if enough width
                    } else if (maxWidth >= 468.dp) {
                        println("SmartBannerAdSize: Full Banner")
                        AdSize.FULL_BANNER // Use full banner if enough width
                    } else {
                        println("SmartBannerAdSize: Banner")
                        AdSize.BANNER // Fallback to standard banner
                    }
                }

                else -> {
                    if (maxWidth >= 468.dp) {
                        AdSize.FULL_BANNER // Use full banner if enough width
                    } else {
                        AdSize.BANNER // Fallback to standard banner
                    }
                }
            }
        }

        AdPlacementType.CONTENT -> {
            // Content areas: larger, more prominent ads
            when {
                isTabletDevice -> {
                    if (maxWidth >= 728.dp) {
                        AdSize.MEDIUM_RECTANGLE // Use fluid ad size if enough width
                    } else {
                        AdSize.MEDIUM_RECTANGLE // Fallback to standard banner
                    }
                }

                else -> {
                    // Phones: medium rectangle for good content visibility
                    AdSize.MEDIUM_RECTANGLE // 300x250 - ideal for content
                }
            }
        }

        AdPlacementType.FEED -> {
            // Feed areas: balanced visibility without being overwhelming
            when {
                isTabletDevice -> {
                    // Tablets: adaptive leaderboard for feeds
                    AdSize.MEDIUM_RECTANGLE
                }

                else -> {
                    // Phones: large banner for good feed integration
                    AdSize.LARGE_BANNER // 320x100 - visible but not overwhelming
                }
            }
        }

        AdPlacementType.INTERSTITIAL -> {
            // Between content sections: attention-grabbing but not too large
            when {
                isTabletDevice -> {
                    if (maxWidth >= 728.dp) {
                        AdSize.LEADERBOARD // Use leaderboard if enough width
                    } else {
                        AdSize.MEDIUM_RECTANGLE // Fallback to standard banner
                    }
                }

                else -> {
                    // Phones: medium rectangle for interstitial visibility
                    AdSize.MEDIUM_RECTANGLE // 300x250 - good for breaking content
                }
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

/**
 * Helper function to choose ad size based on available width using adaptive banners
 * @param availableWidthDp The available width in dp
 * @param context Optional context for adaptive banner creation
 * @return The most appropriate AdSize for the given width
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun getAdSizeForWidth(availableWidthDp: Dp): AdSize {
    val contextFactory = LocalContextFactory.current
    val widthPx = with(LocalDensity.current) { availableWidthDp.roundToPx() }

    return when {
        availableWidthDp >= 728.dp -> {
            // Very wide screens: use adaptive leaderboard
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context = contextFactory?.getActivity(),
                width = 728
            )
        }

        availableWidthDp >= 468.dp -> {
            // Medium-wide screens: use adaptive full banner
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context = contextFactory?.getActivity(),
                width = 468
            )
        }

        availableWidthDp >= 320.dp -> {
            // Standard screens: use adaptive banner
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context = contextFactory?.getActivity(),
                width = 320
            )
        }

        availableWidthDp >= 300.dp -> {
            // Narrow but tall areas: use medium rectangle (non-adaptive)
            AdSize.MEDIUM_RECTANGLE // 300x250 for content areas
        }

        else -> {
            // Very narrow: fallback to basic banner
            AdSize.BANNER // 320x50 default fallback
        }
    }
}