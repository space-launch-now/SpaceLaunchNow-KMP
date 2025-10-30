package me.calebjones.spacelaunchnow

import androidx.compose.runtime.compositionLocalOf
import app.lexilabs.basic.ads.BannerAdHandler
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.InterstitialAdHandler
import app.lexilabs.basic.ads.RewardedAdHandler

/**
 * CompositionLocal to provide preloaded banner ads throughout the app (Android)
 */
@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedBannerAd = compositionLocalOf<BannerAdHandler?> { null }

@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedLargeBannerAd =
    compositionLocalOf<BannerAdHandler?> { null }

@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedMediumRectangleAd =
    compositionLocalOf<BannerAdHandler?> { null }

/**
 * CompositionLocal to provide dedicated navigation banner ad that doesn't conflict with content ads
 */
@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedNavigationBannerAd =
    compositionLocalOf<BannerAdHandler?> { null }

@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedNavigationLargeBannerAd =
    compositionLocalOf<BannerAdHandler?> { null }

@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedNavigationLeaderboardAd =
    compositionLocalOf<BannerAdHandler?> { null }

/**
 * CompositionLocal to provide preloaded tablet-specific ads throughout the app
 */
@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedLeaderboardAd =
    compositionLocalOf<BannerAdHandler?> { null }

@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedFullBannerAd =
    compositionLocalOf<BannerAdHandler?> { null }

@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedFluidAd =
    compositionLocalOf<BannerAdHandler?> { null }

/**
 * CompositionLocal to provide preloaded interstitial and rewarded ads throughout the app
 */
@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedInterstitialAd =
    compositionLocalOf<InterstitialAdHandler?> { null }

@OptIn(DependsOnGoogleMobileAds::class)
val LocalPreloadedRewardedAd =
    compositionLocalOf<RewardedAdHandler?> { null }
