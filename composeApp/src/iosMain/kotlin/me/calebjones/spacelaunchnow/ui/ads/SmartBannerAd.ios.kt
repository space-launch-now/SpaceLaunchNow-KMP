package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.window.core.layout.WindowWidthSizeClass
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.BannerAdHandler
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.BannerAd
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
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
import kotlin.time.Clock
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
    onSizeChanged: ((widthDp: Dp, heightPx: Int) -> Unit)?,
    refreshKey: Any?
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

    // Size-aware fallback — never substitute a shorter ad into a taller slot
    // (e.g. a 320x50 banner inside a 300x250 medium-rectangle box would render
    // empty space around the creative). If a height-compatible ad isn't ready,
    // let the shimmer ride until the correct preload fills.
    val availableAd = bannerAd ?: run {
        log.w { "SmartBannerAd: Primary ad ($actualAdSize) not available, trying size-compatible fallbacks" }
        when {
            placementType == AdPlacementType.NAVIGATION -> {
                preloadedNavigationBannerAd ?: preloadedNavigationLargeBannerAd
                    ?: preloadedNavigationLeaderboardAd
                    ?: preloadedBannerAd ?: preloadedLargeBannerAd
            }
            actualAdSize.height >= 250 -> preloadedMediumRectangleAd
            actualAdSize.height >= 90 -> preloadedLeaderboardAd ?: preloadedLargeBannerAd ?: preloadedFullBannerAd
            actualAdSize.height >= 100 -> preloadedLargeBannerAd ?: preloadedFullBannerAd
            else -> preloadedBannerAd ?: preloadedFluidAd
        }
    }

    // Safety check: ensure we have a banner ad to show
    if (availableAd == null) {
        log.w { "SmartBannerAd: No preloaded ad available for size $actualAdSize - skipping ad display" }
        return
    }

    // A new screen visit (e.g. a different launch) loads a fresh creative instead of
    // re-showing the one this shared banner already displayed. BannerAd stays out of
    // composition until the check runs, so it first mounts the reloaded view.
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshChecked by remember(availableAd, refreshKey, lifecycleOwner) {
        mutableStateOf(refreshKey == null)
    }
    LaunchedEffect(availableAd, refreshKey, lifecycleOwner) {
        if (refreshKey != null &&
            BannerRefreshTracker.shouldReload(
                availableAd,
                refreshKey to lifecycleOwner,
                Clock.System.now().toEpochMilliseconds()
            )
        ) {
            log.d { "SmartBannerAd: New screen for placement $placementType - loading a fresh banner" }
            availableAd.reloadBanner(placementType.name)
        }
        refreshChecked = true
    }
    if (!refreshChecked) return

    // RETRY LOGIC: backoff state lives per handler in BannerRetryPolicy (1s, 3s, 10s, 30s, 60s,
    // then every 120s, never giving up), so one failed preload no longer hides the handler for
    // the process. NONE means the library has not started loading: kick one load after the delay.
    val retryPolicy = BannerRetryPolicy.shared
    var retryRound by remember(availableAd) { mutableIntStateOf(0) }
    val adState = availableAd.state
    LaunchedEffect(adState, retryRound) {
        when (adState) {
            AdState.READY, AdState.SHOWING, AdState.SHOWN -> {
                retryPolicy.reset(availableAd)
                retryPolicy.markShown(availableAd)
            }

            AdState.FAILING, AdState.NONE -> {
                val delayMs = retryPolicy.nextDelayMs(availableAd)
                log.d { "SmartBannerAd: Ad state $adState, retrying in ${delayMs}ms for placement $placementType" }
                delay(delayMs)
                // The preloader or another instance may have moved on while we waited
                if (availableAd.state == adState) {
                    retryPolicy.recordAttempt(availableAd)
                    availableAd.reloadBanner(placementType.name)
                }
                retryRound++
            }

            else -> Unit
        }
    }

    // Debug logging for ad state
    log.v { "SmartBannerAd: Ad state is $adState for placement $placementType" }

    // The library reports FAILING for any failed load, including an auto-refresh no-fill while
    // an earlier creative is still on screen. Keep rendering that creative during the retry.
    val keepShownCreative = adState == AdState.FAILING && retryPolicy.hasShown(availableAd)
    val bannerHeight = when {
        actualAdSize.height > 0 -> actualAdSize.height.dp
        actualAdSize.height == -2 -> 250.dp // FLUID
        else -> 50.dp
    }

    // IMPORTANT: Always render BannerAd Composable to trigger load on iOS
    // Show layout when ad is ready, showing, or loading
    when {
        adState == AdState.READY || adState == AdState.SHOWING || adState == AdState.SHOWN ||
            adState == AdState.LOADING || keepShownCreative -> {
            // Show the banner ad with optional remove ads button
            // SHOWN keeps the banner mounted after a tap instead of swapping to the shimmer
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
            if (adState == AdState.LOADING) {
                log.d { "🔄 SmartBannerAd: Ad is loading for placement $placementType - BannerAd Composable rendered to trigger load" }
            }
        }

        adState == AdState.FAILING || adState == AdState.NONE -> {
            // Shimmer while waiting for a retry: the slot is never left empty
            AdShimmerPlaceholder(height = bannerHeight, showCard = showCard, modifier = modifier)
        }

        adState == AdState.DISMISSED -> {
            // Ad was dismissed - don't show anything
            log.v { "SmartBannerAd: Ad dismissed for placement $placementType - hiding ad space" }
        }

        else -> {
            // Unknown state or SHOWN (already displayed)
            log.w { "SmartBannerAd: Unknown ad state $adState for placement $placementType" }
        }
    }
}

/**
 * Reloads with this app's ad unit and the handler's own size. A bare load() falls back to
 * the library defaults: Google's test ad unit at FULL_BANNER size.
 */
@OptIn(DependsOnGoogleMobileAds::class)
private fun BannerAdHandler.reloadBanner(placement: String) {
    val size = AdTelemetry.sizeLabel(adSize.width, adSize.height)
    load(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = adSize,
        onLoad = { AdTelemetry.loaded("banner", size, placement) },
        onFailure = { AdTelemetry.failed("banner", it, size, placement) },
        onDismissed = {},
        onShown = {},
        onImpression = { AdTelemetry.impression("banner", size, placement) },
        onClick = { AdTelemetry.clicked("banner", size, placement) }
    )
}

/** Shimmer placeholder shown while ads are loading or waiting for a retry. */
@Composable
private fun AdShimmerPlaceholder(height: Dp, showCard: Boolean, modifier: Modifier = Modifier) {
    val placeholderContent = @Composable {
        Box(
            modifier = Modifier
                .shimmer()
                .fillMaxWidth()
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
        )
    }

    if (showCard) {
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
            Box(modifier = Modifier.padding(8.dp)) { placeholderContent() }
        }
    } else {
        Box(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) { placeholderContent() }
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