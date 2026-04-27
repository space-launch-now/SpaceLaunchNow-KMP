package me.calebjones.spacelaunchnow.ui.ads

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.Consent
import app.lexilabs.basic.ads.ConsentDebugSettings
import app.lexilabs.basic.ads.ConsentRequestParameters
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.DependsOnGoogleUserMessagingPlatform
import app.lexilabs.basic.ads.composable.rememberBannerAd
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
import co.touchlab.kermit.Logger
import androidx.compose.runtime.remember


/**
 * Android implementation of AdConsentPopup using Google UMP.
 *
 * NOTE: The consent form will only appear if a GDPR message has been configured
 * in the AdMob console (Privacy & messaging) for the app's AdMob App ID.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class, DependsOnGoogleMobileAds::class)
@Composable
actual fun AdConsentPopup(
    onFailure: ((Throwable) -> Unit)?,
    onConsentResolved: (() -> Unit)?
) {
    val log = Logger.withTag("AdConsentPopup")
    val contextFactory = LocalContextFactory.current
    val activity = contextFactory?.getActivity() as? Activity

    // Only show consent popup if we have a valid Activity
    if (activity == null) {
        log.w { "Activity is null, resolving consent immediately" }
        LaunchedEffect(Unit) { onConsentResolved?.invoke() }
        return
    }

    // Debug: uncomment to simulate EEA geography for consent testing.
    // Get your device hash from logcat: "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId(...)"
    // val debugSettings = ConsentDebugSettings.builder(
    //     debugGeography = ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA,
    //     hashedId = "0BF9377651BCA3F62260F25FFC54F6A8",
    //     forceTesting = true
    // )
    // val params = remember(debugSettings) {
    //     ConsentRequestParameters.Builder()
    //         .setConsentDebugSettings(debugSettings)
    //         .build()
    // }

    // Production: bare consent request params (no debug override)
    val params = remember { ConsentRequestParameters.Builder().build() }

    // Create consent object once per activity
    val consent = remember(activity) { Consent(activity) }

    // Single LaunchedEffect — fires ONE requestConsentInfoUpdate with debug settings.
    // We avoid ConsentPopup because it fires a SECOND request WITHOUT debug settings,
    // which overwrites gdprApplies back to 0.
    LaunchedEffect(consent, params) {
        consent.requestConsentInfoUpdate(
            params = params,
            onCompletion = {
                if (consent.canRequestAds) {
                    log.d { "Consent already granted — ads can be requested" }
                    onConsentResolved?.invoke()
                } else {
                    consent.loadAndShowConsentForm(
                        onLoaded = { log.d { "Consent form loaded" } },
                        onShown = {
                            log.d { "Consent form shown" }
                            if (consent.canRequestAds) {
                                onConsentResolved?.invoke()
                            }
                        },
                        onError = { throwable ->
                            log.w(throwable) { "Consent form unavailable: ${throwable.message}" }
                            onFailure?.invoke(throwable)
                            onConsentResolved?.invoke()
                        }
                    )
                }
            },
            onError = { exception ->
                log.w(exception) { "Consent info update failed: ${exception.message}" }
                onFailure?.invoke(exception)
                onConsentResolved?.invoke()
            }
        )
    }
}

/**
 * Android implementation of WithPreloadedAds.
 *
 * Preloads only the banner sizes used by the vast majority of (phone) sessions and
 * one dedicated banner for the persistent bottom navigation. Larger/rare sizes
 * (LEADERBOARD, FULL_BANNER, FLUID) and tablet-specific variants are aliased to
 * the closest preloaded size; SmartBannerAd has fallback logic that picks the
 * best available preload at render time.
 *
 * Interstitial and rewarded ads are NOT preloaded here — they are loaded
 * on-demand inside their respective handlers only when the gating logic decides
 * to show one. This keeps AdMob "show rate" healthy by not requesting an ad we
 * are unlikely to display.
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

    // Primary banner ad (most common — used in content areas, phone CONTENT/NAVIGATION)
    val preloadedBannerAd by rememberBannerAd(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.BANNER
    )

    // Large banner for FEED placements and tablet NAVIGATION
    val preloadedLargeBannerAd by rememberBannerAd(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.LARGE_BANNER
    )

    // Medium rectangle for tablet FEED/CONTENT and phone INTERSTITIAL placement type
    val preloadedMediumRectangleAd by rememberBannerAd(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.MEDIUM_RECTANGLE
    )

    // Dedicated navigation banner — separate request to avoid recomposition fights
    // with the content banner when transitioning between screens.
    val preloadedNavigationBannerAd by rememberBannerAd(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.BANNER
    )

    // Aliased fallbacks for sizes/placements that produced ~$0 in production.
    // SmartBannerAd's fallback picker still resolves to a usable banner.
    val preloadedNavigationLargeBannerAd = preloadedLargeBannerAd
    val preloadedNavigationLeaderboardAd = preloadedLargeBannerAd
    val preloadedLeaderboardAd = preloadedLargeBannerAd
    val preloadedFullBannerAd = preloadedLargeBannerAd
    val preloadedFluidAd = preloadedBannerAd

    CompositionLocalProvider(
        LocalPreloadedBannerAd provides preloadedBannerAd,
        LocalPreloadedLargeBannerAd provides preloadedLargeBannerAd,
        LocalPreloadedMediumRectangleAd provides preloadedMediumRectangleAd,
        LocalPreloadedNavigationBannerAd provides preloadedNavigationBannerAd,
        LocalPreloadedNavigationLargeBannerAd provides preloadedNavigationLargeBannerAd,
        LocalPreloadedNavigationLeaderboardAd provides preloadedNavigationLeaderboardAd,
        LocalPreloadedLeaderboardAd provides preloadedLeaderboardAd,
        LocalPreloadedFullBannerAd provides preloadedFullBannerAd,
        LocalPreloadedFluidAd provides preloadedFluidAd
        // Interstitial + rewarded intentionally omitted — loaded on-demand by their handlers.
    ) {
        content()
    }
}

/**
 * Android implementation — shows the UMP privacy options form via BasicAds
 * so the user can revoke or change their ad consent choices.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class)
actual fun showPrivacyOptionsForm(
    activity: Any?,
    onDismiss: () -> Unit,
    onFailure: (Throwable) -> Unit
) {
    val log = Logger.withTag("PrivacyOptions")
    if (activity == null) {
        log.w { "Activity is null, cannot show privacy options form" }
        onFailure(IllegalStateException("Activity is null"))
        return
    }

    val consent = Consent(activity)
    consent.showPrivacyOptionsForm(
        onDismissed = { onDismiss() },
        onError = { exception ->
            log.w { "Privacy options form error: ${exception.message}" }
            onFailure(exception)
        }
    )
}

/**
 * Android implementation — checks whether the UMP privacy options entry
 * point should be visible via BasicAds.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class)
@Composable
actual fun rememberPrivacyOptionsRequired(): Boolean {
    val contextFactory = LocalContextFactory.current
    val activity = contextFactory?.getActivity() ?: return false

    val consent = remember(activity) { Consent(activity) }
    return consent.isPrivacyOptionsRequired()
}
