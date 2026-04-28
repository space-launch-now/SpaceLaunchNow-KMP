package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.InterstitialAd
import app.lexilabs.basic.ads.composable.rememberInterstitialAd
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.koinInject

private val log by lazy { SpaceLogger.getLogger("InterstitialAdHandler") }

/**
 * Android implementation of InterstitialAdHandler using BasicAds library.
 *
 * Loads the interstitial **on demand**, only when [GlobalAdManager.shouldShowInterstitialOnDetailView]
 * decides to show one. This avoids requesting an ad on every detail view (which kept AdMob
 * "show rate" near 0% and risked unit health) and instead loads exactly when the gate opens.
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
actual fun InterstitialAdHandler(
    onAdShown: (() -> Unit)?,
    onAdFailed: ((String) -> Unit)?
) {
    val contextFactory = LocalContextFactory.current
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    // Get subscription state to check if it's still loading
    val subscriptionRepo =
        koinInject<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsStateWithLifecycle()

    // Don't show ads if:
    // 1. Subscription state is still loading (prevents race condition)
    // 2. User has ad-free premium feature
    // 3. Not on a mobile platform (Android/iOS)
    // 4. No context factory available
    if (subscriptionState.isLoading ||
        hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null
    ) {
        return
    }

    // Get GlobalAdManager instance from Koin
    val globalAdManager: GlobalAdManager = koinInject()

    // Check if we should show an interstitial ad. This call increments the visit
    // counter as a side effect — keep it gated behind the early-returns above so
    // premium users don't bump the counter.
    val shouldShowAd = remember {
        globalAdManager.shouldShowInterstitialOnDetailView()
    }

    if (!shouldShowAd) {
        return
    }

    // Track if ad has already been shown in this composition to prevent repeats
    var adShownThisSession by remember { mutableStateOf(false) }

    // 🚀 ON-DEMAND LOAD: Only request the ad now that the gate has opened.
    // Previously this was preloaded at app start which produced ~159K wasted matched
    // requests/month against ~422 actual impressions on Android.
    val interstitialAd by rememberInterstitialAd(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.INTERSTITIAL)
    )

    if (BuildConfig.IS_DEBUG) {
        LaunchedEffect(Unit) {
            val currentCount = globalAdManager.getDetailViewVisitCount()
            val timeSinceLastMinutes = globalAdManager.getMinutesSinceLastInterstitial()
            log.d {
                buildString {
                    appendLine("🎯 INTERSTITIAL AD DEBUG (on-demand)")
                    appendLine("Visit Count: $currentCount")
                    appendLine("Will Show: $shouldShowAd")
                    if (timeSinceLastMinutes == 999L) {
                        appendLine("Time Since Last: Never")
                    } else {
                        appendLine("Time Since Last: ${timeSinceLastMinutes}min")
                    }
                }
            }
        }
    }

    if (adShownThisSession) {
        return
    }

    // Handle ad state changes
    LaunchedEffect(interstitialAd.state) {
        when (interstitialAd.state) {
            AdState.READY -> log.d { "Ad loaded successfully and ready to show" }
            AdState.SHOWING -> {
                log.d { "Ad is showing!" }
                onAdShown?.invoke()
            }

            AdState.DISMISSED -> {
                log.d { "Ad dismissed by user" }
                adShownThisSession = true
            }

            AdState.FAILING -> {
                log.e { "Ad failed to load" }
                onAdFailed?.invoke("Failed to load")
                adShownThisSession = true
            }

            AdState.LOADING -> log.d { "Ad is loading..." }
            AdState.SHOWN -> {
                log.d { "Ad has finished showing" }
                adShownThisSession = true
            }

            AdState.NONE -> log.v { "Initial state" }
            else -> log.d { "State: ${interstitialAd.state}" }
        }
    }

    // FINAL GATE — re-check premium state at display time to prevent showing
    // ads to users who upgraded mid-session.
    if (!adShownThisSession &&
        interstitialAd.state == AdState.READY &&
        !hasAdFree &&
        !subscriptionState.isLoading &&
        !subscriptionState.isSubscribed
    ) {
        log.d { "FINAL GATE - Showing ad to free user" }
        InterstitialAd(loadedAd = interstitialAd)
    } else if (!adShownThisSession && interstitialAd.state == AdState.READY) {
        val reason = when {
            hasAdFree -> "hasAdFree=true"
            subscriptionState.isLoading -> "isLoading=true"
            subscriptionState.isSubscribed -> "isSubscribed=true"
            else -> "unknown"
        }
        log.d { "BLOCKED - Not showing ad because: $reason" }
    }
}

/**
 * Helper function to manually trigger an interstitial ad check.
 * Useful for testing or special cases.
 */
@Composable
fun TriggerInterstitialAdIfNeeded(
    onAdShown: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null
) {
    InterstitialAdHandler(
        onAdShown = onAdShown,
        onAdFailed = onAdFailed
    )
}
