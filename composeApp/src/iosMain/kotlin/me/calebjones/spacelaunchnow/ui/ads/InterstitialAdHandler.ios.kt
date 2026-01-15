package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.InterstitialAd
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.LocalPreloadedInterstitialAd
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature
import org.koin.compose.koinInject

private val log by lazy { SpaceLogger.getLogger("InterstitialAdHandler") }

/**
 * iOS implementation of InterstitialAdHandler using BasicAds library.
 * 
 * Interstitial ad handler that shows ads every 4th detail view visit.
 * - Checks if the user has ad-free premium
 * - Tracks visit count and shows ads every 4th visit
 * - Respects minimum time intervals between ads (5 minutes)
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
    val subscriptionRepo = koinInject<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsStateWithLifecycle()

    // 🚀 USE PRELOADED AD: Get preloaded interstitial ad from CompositionLocal
    val preloadedInterstitialAd = LocalPreloadedInterstitialAd.current

    log.v { "🎯 InterstitialAdHandler: hasAdFree=$hasAdFree, isLoading=${subscriptionState.isLoading}, isMobile=${getPlatform().type.isMobile}, hasContext=${contextFactory != null}, hasPreloadedAd=${preloadedInterstitialAd != null}" }

    // Don't show ads if:
    // 1. Subscription state is still loading (prevents race condition)
    // 2. User has ad-free premium feature
    // 3. Not on a mobile platform (Android/iOS)
    // 4. No context factory available
    // 5. No preloaded interstitial ad available
    if (subscriptionState.isLoading ||
        hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null ||
        preloadedInterstitialAd == null
    ) {
        log.v { "⚠️ InterstitialAdHandler: Not showing ad due to conditions" }
        return
    }

    // Get GlobalAdManager instance from Koin
    val globalAdManager: GlobalAdManager = koinInject()

    // Check if we should show an interstitial ad
    val shouldShowAd = remember {
        globalAdManager.shouldShowInterstitialOnDetailView()
    }
    
    // Track if ad has already been shown in this composition to prevent repeats
    var adShownThisSession by remember { mutableStateOf(false) }

    // Show debug notification in debug builds
    if (BuildConfig.IS_DEBUG) {
        LaunchedEffect(Unit) {
            val currentCount = globalAdManager.getDetailViewVisitCount()
            val timeSinceLastMinutes = globalAdManager.getMinutesSinceLastInterstitial()

            val debugMessage = buildString {
                appendLine("🎯 INTERSTITIAL AD DEBUG")
                appendLine("Visit Count: ${currentCount}/4")
                appendLine("Will Show: $shouldShowAd")
                if (timeSinceLastMinutes == 999L) {
                    appendLine("Time Since Last: Never")
                } else {
                    appendLine("Time Since Last: ${timeSinceLastMinutes}min")
                }
                appendLine("Min Interval: 5min")
                if (shouldShowAd) {
                    appendLine("🚀 Showing ad now!")
                } else {
                    val remaining = 4 - (currentCount % 4)
                    appendLine("Next in: $remaining visits")
                }
            }

            log.d { debugMessage }
        }
    }

    if (!shouldShowAd) {
        log.v { "⏭️ InterstitialAdHandler: Not time to show ad yet" }
        return
    }
    
    if (adShownThisSession) {
        log.v { "⏭️ InterstitialAdHandler: Ad already shown in this session" }
        return
    }

    // Use the preloaded interstitial ad
    val interstitialAd = preloadedInterstitialAd

    // Handle ad state changes
    LaunchedEffect(interstitialAd.state) {
        when (interstitialAd.state) {
            AdState.READY -> {
                log.d { "🎯 InterstitialAd: Ad loaded successfully and ready to show" }
            }

            AdState.SHOWING -> {
                log.d { "✅ InterstitialAd: Ad is showing!" }
                onAdShown?.invoke()
            }

            AdState.DISMISSED -> {
                log.d { "🎯 InterstitialAd: Ad dismissed by user" }
                adShownThisSession = true // Mark as shown
            }

            AdState.FAILING -> {
                log.w { "❌ InterstitialAd: Ad failed to load" }
                onAdFailed?.invoke("Failed to load")
                adShownThisSession = true // Don't retry in this session
            }

            AdState.LOADING -> {
                log.v { "⏳ InterstitialAd: Ad is loading..." }
            }

            AdState.SHOWN -> {
                log.d { "✅ InterstitialAd: Ad has finished showing" }
                adShownThisSession = true // Mark as shown
            }

            AdState.NONE -> {
                log.v { "⚪ InterstitialAd: Initial state" }
            }

            else -> {
                log.v { "🔄 InterstitialAd: State: ${interstitialAd.state}" }
            }
        }
    }

    // Show the interstitial ad using the Composable pattern (required by basic-ads)
    // Only show if ad hasn't been shown yet in this session
    // CRITICAL: Triple-check all conditions to prevent showing ads to premium users
    // This is the FINAL gate before the ad is displayed
    if (!adShownThisSession && 
        interstitialAd.state == AdState.READY && 
        !hasAdFree && 
        !subscriptionState.isLoading &&
        !subscriptionState.isSubscribed // ADDITIONAL CHECK: Verify subscription state directly
    ) {
        log.d { "🎯 InterstitialAd: FINAL GATE - Showing ad to free user" }
        InterstitialAd(loadedAd = interstitialAd)
    } else if (!adShownThisSession && interstitialAd.state == AdState.READY) {
        // Ad is ready but we're NOT showing it - log why
        val reason = when {
            hasAdFree -> "hasAdFree=true"
            subscriptionState.isLoading -> "isLoading=true"
            subscriptionState.isSubscribed -> "isSubscribed=true"
            else -> "unknown"
        }
        log.w { "⛔ InterstitialAd: BLOCKED - Not showing ad because: $reason" }
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
