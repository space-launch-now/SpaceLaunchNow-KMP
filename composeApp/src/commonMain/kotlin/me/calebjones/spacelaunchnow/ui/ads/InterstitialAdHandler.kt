package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * Interstitial ad handler that shows ads every 4th detail view visit.
 *
 * Usage: Call this composable when entering a detail view (launch detail, event detail, etc.)
 * It will automatically:
 * - Check if the user has ad-free premium
 * - Track visit count and show ads every 4th visit
 * - Respect minimum time intervals between ads (5 minutes)
 * - Only show on mobile platforms (Android/iOS)
 *
 * @param onAdShown Called when an interstitial ad is successfully shown
 * @param onAdFailed Called when an interstitial ad fails to load or show
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun InterstitialAdHandler(
    onAdShown: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null
) {
    val contextFactory = LocalContextFactory.current
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    // 🚀 USE PRELOADED AD: Get preloaded interstitial ad from CompositionLocal
    val preloadedInterstitialAd = LocalPreloadedInterstitialAd.current

    // Don't show ads if:
    // 1. User has ad-free premium feature
    // 2. Not on a mobile platform (Android/iOS)
    // 3. No context factory available
    // 4. No preloaded interstitial ad available
    if (hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null ||
        preloadedInterstitialAd == null
    ) {
        println("⚠️ InterstitialAdHandler: Not showing ad due to conditions.")
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

            println(debugMessage)
        }
    }

    if (!shouldShowAd) {
        println("⏭️ InterstitialAdHandler: Not time to show ad yet.")
        return
    }
    
    if (adShownThisSession) {
        println("⏭️ InterstitialAdHandler: Ad already shown in this session.")
        return
    }

    // Use the preloaded interstitial ad
    val interstitialAd = preloadedInterstitialAd

    // Handle ad state changes
    LaunchedEffect(interstitialAd.state) {
        when (interstitialAd.state) {
            AdState.READY -> {
                println("🎯 InterstitialAd: Ad loaded successfully and ready to show")
            }

            AdState.SHOWING -> {
                println("✅ InterstitialAd: Ad is showing!")
                onAdShown?.invoke()
            }

            AdState.DISMISSED -> {
                println("🎯 InterstitialAd: Ad dismissed by user")
                adShownThisSession = true // Mark as shown
            }

            AdState.FAILING -> {
                println("❌ InterstitialAd: Ad failed to load")
                onAdFailed?.invoke("Failed to load")
                adShownThisSession = true // Don't retry in this session
            }

            AdState.LOADING -> {
                println("⏳ InterstitialAd: Ad is loading...")
            }

            AdState.SHOWN -> {
                println("✅ InterstitialAd: Ad has finished showing")
                adShownThisSession = true // Mark as shown
            }

            AdState.NONE -> {
                println("⚪ InterstitialAd: Initial state")
            }

            else -> {
                println("🔄 InterstitialAd: State: ${interstitialAd.state}")
            }
        }
    }

    // Show the interstitial ad using the Composable pattern (required by basic-ads)
    // Only show if ad hasn't been shown yet in this session
    if (!adShownThisSession && interstitialAd.state == AdState.READY) {
        InterstitialAd(loadedAd = interstitialAd)
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
