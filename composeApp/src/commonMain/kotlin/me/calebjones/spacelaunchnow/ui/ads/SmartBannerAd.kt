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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.BannerAdHandler
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.BannerAd
import app.lexilabs.basic.ads.composable.rememberBannerAd
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.LocalPreloadedBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedLargeBannerAd
import me.calebjones.spacelaunchnow.LocalPreloadedMediumRectangleAd
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.isTablet
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature

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

    // Don't show ads if:
    // 1. User has ad-free premium feature
    // 2. Not on a mobile platform (Android/iOS)
    // 3. No context factory available
    // 4. No preloaded ads available
    if (hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null ||
        (preloadedBannerAd == null && preloadedLargeBannerAd == null && preloadedMediumRectangleAd == null)
    ) {
        println("⚠️ SmartBannerAd: Not showing ad due to conditions.")
        return
    }

    // Select the appropriate preloaded ad based on requested ad size
    val bannerAd = when (adSize) {
        AdSize.BANNER -> preloadedBannerAd
        AdSize.LARGE_BANNER -> preloadedLargeBannerAd  
        AdSize.MEDIUM_RECTANGLE -> preloadedMediumRectangleAd
        else -> preloadedBannerAd // Fallback to standard banner
    }

    // Safety check: ensure we have a banner ad to show
    if (bannerAd == null) {
        println("⚠️ SmartBannerAd: No preloaded ad available for size $adSize")
        return
    }

    // Show layout when ad is ready OR showing (loaded and displaying)
    when (bannerAd.state) {
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
                            modifier = Modifier.padding(8.dp),
                            adSize = adSize,
                            showRemoveAdsButton = showRemoveAdsButton,
                            onSizeChanged = onSizeChanged,
                            bannerAd = bannerAd
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
                    adSize = adSize,
                    showRemoveAdsButton = showRemoveAdsButton,
                    onSizeChanged = onSizeChanged,
                    bannerAd = bannerAd
                )
            }
        }

        else -> {
            // No ads to show
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

    BoxWithConstraints {
        val availableWidthDp = maxWidth

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
                                AdSize.FLUID -> 50.dp // Dynamic height, start with banner size
                                else -> 50.dp // Default to standard banner size
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
                    // Display the preloaded ad - instant rendering!
                    BannerAd(ad = bannerAd)
                }
            }
        }
    }
}

/**
 * Helper function to choose appropriate ad size based on device characteristics
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun getRecommendedAdSize(): AdSize {
    return when {
        isTablet() -> AdSize.LEADERBOARD // 728x90 for tablets
        else -> AdSize.BANNER // 320x50 for phones
    }
}

/**
 * Helper function to choose ad size based on available width
 * @param availableWidthDp The available width in dp
 * @return The most appropriate AdSize for the given width
 */
@OptIn(DependsOnGoogleMobileAds::class)
fun getAdSizeForWidth(availableWidthDp: Dp): AdSize {
    return when {
        availableWidthDp >= 728.dp -> AdSize.LEADERBOARD // 728x90 for wide screens
        availableWidthDp >= 468.dp -> AdSize.FULL_BANNER // 468x60 for medium screens  
        availableWidthDp >= 320.dp -> AdSize.BANNER // 320x50 for standard screens
        availableWidthDp >= 300.dp -> AdSize.MEDIUM_RECTANGLE // 300x250 for narrow but tall areas
        else -> AdSize.BANNER // Default fallback
    }
}