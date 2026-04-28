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
import app.lexilabs.basic.ads.composable.rememberInterstitialAd
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature
import org.koin.compose.koinInject

private val log by lazy { SpaceLogger.getLogger("InterstitialAdHandler") }

/**
 * iOS implementation of InterstitialAdHandler using BasicAds library.
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

    val subscriptionRepo = koinInject<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsStateWithLifecycle()

    log.v { "🎯 InterstitialAdHandler: hasAdFree=$hasAdFree, isLoading=${subscriptionState.isLoading}, isMobile=${getPlatform().type.isMobile}, hasContext=${contextFactory != null}" }

    if (subscriptionState.isLoading ||
        hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null
    ) {
        log.v { "⚠️ InterstitialAdHandler: Not showing ad due to conditions" }
        return
    }

    val globalAdManager: GlobalAdManager = koinInject()

    // Side effect: increments visit counter. Gated behind early-returns so premium
    // users don't bump it.
    val shouldShowAd = remember {
        globalAdManager.shouldShowInterstitialOnDetailView()
    }

    if (!shouldShowAd) {
        log.v { "⏭️ InterstitialAdHandler: Not time to show ad yet" }
        return
    }

    var adShownThisSession by remember { mutableStateOf(false) }

    // 🚀 ON-DEMAND LOAD: Only request the ad now that the gate has opened.
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
        log.v { "⏭️ InterstitialAdHandler: Ad already shown in this session" }
        return
    }

    LaunchedEffect(interstitialAd.state) {
        when (interstitialAd.state) {
            AdState.READY -> log.d { "🎯 InterstitialAd: Ad loaded successfully and ready to show" }
            AdState.SHOWING -> {
                log.d { "✅ InterstitialAd: Ad is showing!" }
                onAdShown?.invoke()
            }

            AdState.DISMISSED -> {
                log.d { "🎯 InterstitialAd: Ad dismissed by user" }
                adShownThisSession = true
            }

            AdState.FAILING -> {
                log.w { "❌ InterstitialAd: Ad failed to load" }
                onAdFailed?.invoke("Failed to load")
                adShownThisSession = true
            }

            AdState.LOADING -> log.v { "⏳ InterstitialAd: Ad is loading..." }
            AdState.SHOWN -> {
                log.d { "✅ InterstitialAd: Ad has finished showing" }
                adShownThisSession = true
            }

            AdState.NONE -> log.v { "⚪ InterstitialAd: Initial state" }
            else -> log.v { "🔄 InterstitialAd: State: ${interstitialAd.state}" }
        }
    }

    // FINAL GATE — re-check premium state at display time.
    if (!adShownThisSession &&
        interstitialAd.state == AdState.READY &&
        !hasAdFree &&
        !subscriptionState.isLoading &&
        !subscriptionState.isSubscribed
    ) {
        log.d { "🎯 InterstitialAd: FINAL GATE - Showing ad to free user" }
        InterstitialAd(loadedAd = interstitialAd)
    } else if (!adShownThisSession && interstitialAd.state == AdState.READY) {
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
