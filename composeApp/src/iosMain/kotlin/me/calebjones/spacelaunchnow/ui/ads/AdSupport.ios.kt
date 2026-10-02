package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.Consent
import kotlinx.coroutines.delay
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.DependsOnGoogleUserMessagingPlatform
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
import me.calebjones.spacelaunchnow.LocalContextFactory

private val log by lazy { SpaceLogger.getLogger("AdSupport") }

/**
 * iOS implementation of AdConsentPopup using Google UMP.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class)
@Composable
actual fun AdConsentPopup(
    onFailure: ((Throwable) -> Unit)?,
    onConsentResolved: (() -> Unit)?
) {
    val contextFactory = LocalContextFactory.current
    var viewController by remember { mutableStateOf<Any?>(null) }
    // Resolve at most once, however many paths (poll, failure, no-VC) try to.
    val latestOnResolved by rememberUpdatedState(onConsentResolved)
    // Google's order: UMP form first, then ATT, then ads. ATT is bounded and never throws,
    // so it cannot block the gate.
    val scope = rememberCoroutineScope()
    val resolver = remember {
        ConsentResolver {
            scope.launch {
                AppTracking.requestIfNeeded()
                latestOnResolved?.invoke()
            }
        }
    }

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
            resolver.resolve()
        }
    }

    val vc = viewController ?: return

    val consent by rememberConsent(activity = vc)

    // canRequestAds is a plain getter, not Compose state: reading it at composition time never
    // sees UMP finish (the update is async and nothing recomposes), which left the ad gate
    // closed until relaunch on fresh installs. ConsentPopup owns the UMP calls, so poll the
    // getter once per Consent instance. A timeout resolves anyway (regions with no consent
    // form must never wait).
    LaunchedEffect(consent) {
        when (awaitCanRequestAds(canRequestAds = { resolver.isResolved || consent.canRequestAds })) {
            ConsentPollOutcome.CAN_REQUEST_ADS -> {
                if (resolver.resolve()) log.d { "Consent resolved via canRequestAds poll" }
            }
            ConsentPollOutcome.TIMED_OUT -> {
                if (resolver.resolve()) log.w { "Consent not resolved after ${CONSENT_TIMEOUT_MS}ms, allowing ad loading anyway" }
            }
        }
    }

    ConsentPopup(
        consent = consent,
        onFailure = { throwable ->
            log.e(throwable) { "❌ Consent popup failure" }
            onFailure?.invoke(throwable)
            // Also resolve on failure to avoid blocking ad loading indefinitely
            resolver.resolve()
        }
    )
}

/**
 * iOS implementation of WithPreloadedAds.
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

    // ContextFactory.getActivity() returns an empty-string sentinel (not null) while the
    // UIWindowScene has no rootViewController yet — the first moments of launch. BasicAds'
    // BannerAdHandler.load throws "Root ViewController is null" if that value reaches it,
    // which is fatal inside composition (issue #168). Skip preloading and let a later
    // recomposition retry once the root view controller exists, same as AdConsentPopup.
    if (context == null || context == "") {
        log.w { "⚠️ WithPreloadedAds: no root view controller yet, skipping ad preload this composition" }
        content()
        return
    }

    // Primary banner ad (most common - used in content areas)
    val preloadedBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.BANNER,
        onLoad = { AdTelemetry.loaded("banner", AdSize.BANNER.label(), "BANNER") },
        onFailure = { AdTelemetry.failed("banner", it, AdSize.BANNER.label(), "BANNER") },
        onDismissed = {},
        onShown = {},
        onImpression = { AdTelemetry.impression("banner", AdSize.BANNER.label(), "BANNER") },
        onClick = { AdTelemetry.clicked("banner", AdSize.BANNER.label(), "BANNER") }
    )

    // Large banner for detail pages and featured content
    val preloadedLargeBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.LARGE_BANNER,
        onLoad = { AdTelemetry.loaded("banner", AdSize.LARGE_BANNER.label(), "LARGE_BANNER") },
        onFailure = { AdTelemetry.failed("banner", it, AdSize.LARGE_BANNER.label(), "LARGE_BANNER") },
        onDismissed = {},
        onShown = {},
        onImpression = { AdTelemetry.impression("banner", AdSize.LARGE_BANNER.label(), "LARGE_BANNER") },
        onClick = { AdTelemetry.clicked("banner", AdSize.LARGE_BANNER.label(), "LARGE_BANNER") }
    )

    // Medium rectangle for inline list ads (also phone INTERSTITIAL placement)
    val preloadedMediumRectangleAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.MEDIUM_RECTANGLE,
        onLoad = { AdTelemetry.loaded("banner", AdSize.MEDIUM_RECTANGLE.label(), "MEDIUM_RECTANGLE") },
        onFailure = { AdTelemetry.failed("banner", it, AdSize.MEDIUM_RECTANGLE.label(), "MEDIUM_RECTANGLE") },
        onDismissed = {},
        onShown = {},
        onImpression = { AdTelemetry.impression("banner", AdSize.MEDIUM_RECTANGLE.label(), "MEDIUM_RECTANGLE") },
        onClick = { AdTelemetry.clicked("banner", AdSize.MEDIUM_RECTANGLE.label(), "MEDIUM_RECTANGLE") }
    )

    // Dedicated navigation banner — separate request to avoid recomposition fights
    // with the content banner when transitioning between screens.
    val preloadedNavigationBannerAd by rememberBannerAd(
        activity = context,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = AdSize.BANNER,
        onLoad = { AdTelemetry.loaded("banner", AdSize.BANNER.label(), "NAVIGATION") },
        onFailure = { AdTelemetry.failed("banner", it, AdSize.BANNER.label(), "NAVIGATION") },
        onDismissed = {},
        onShown = {},
        onImpression = { AdTelemetry.impression("banner", AdSize.BANNER.label(), "NAVIGATION") },
        onClick = { AdTelemetry.clicked("banner", AdSize.BANNER.label(), "NAVIGATION") }
    )

    // Aliased fallbacks for sizes/placements that produced ~$0 in production.
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
 * iOS implementation — shows the UMP privacy options form via BasicAds.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class)
actual fun showPrivacyOptionsForm(
    activity: Any?,
    onDismiss: () -> Unit,
    onFailure: (Throwable) -> Unit
) {
    val consent = Consent(activity)
    consent.showPrivacyOptionsForm(
        onDismissed = { onDismiss() },
        onError = { exception ->
            log.w(exception) { "Privacy options form error: ${exception.message}" }
            onFailure(exception)
        }
    )
}

/**
 * iOS implementation — checks whether the UMP privacy options entry
 * point should be visible via BasicAds.
 */
@OptIn(DependsOnGoogleUserMessagingPlatform::class)
@Composable
actual fun rememberPrivacyOptionsRequired(): Boolean {
    val contextFactory = LocalContextFactory.current
    val vc = contextFactory?.getActivity()
    val consent = remember(vc) { Consent(vc) }
    return consent.isPrivacyOptionsRequired()
}

private fun AdSize.label(): String = AdTelemetry.sizeLabel(width, height)
