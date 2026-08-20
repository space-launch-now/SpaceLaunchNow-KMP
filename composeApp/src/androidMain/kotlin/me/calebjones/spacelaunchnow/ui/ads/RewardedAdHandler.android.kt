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
import app.lexilabs.basic.ads.composable.RewardedAd
import app.lexilabs.basic.ads.composable.rememberRewardedAd
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.koinInject

private val log by lazy { SpaceLogger.getLogger("RewardedAdHandler") }

/**
 * Android implementation of RewardedAdHandler using BasicAds library.
 *
 * Loads the rewarded ad **on demand** when [shouldShow] becomes true, mirroring the
 * on-demand InterstitialAdHandler pattern. The old preloaded-CompositionLocal path was
 * never provided after preloading was removed, which left this handler permanently
 * early-returning and the "Watch Ad for 24h Premium Access" flow dead (spec 018 US2).
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
actual fun RewardedAdHandler(
    shouldShow: Boolean,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)?,
    onAdShown: (() -> Unit)?,
    onAdFailed: ((String) -> Unit)?,
    onAdDismissed: (() -> Unit)?
) {
    if (!shouldShow) return

    val contextFactory = LocalContextFactory.current
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    val subscriptionRepo =
        koinInject<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsStateWithLifecycle()

    if (subscriptionState.isLoading ||
        hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null
    ) {
        log.w { "Not showing rewarded ad due to conditions" }
        return
    }

    // Terminal-outcome guards: rememberRewardedAd auto-reloads after DISMISSED/FAILING,
    // so without these the ad would re-show (or callbacks would re-fire) in a loop.
    var rewardGranted by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    // 🚀 ON-DEMAND LOAD: only requested once the user has tapped "Watch Ad".
    val rewardedAd by rememberRewardedAd(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.REWARDED),
        onFailure = { e ->
            log.e { "Rewarded ad failed to load: ${e.message}" }
            if (!finished) {
                finished = true
                onAdFailed?.invoke(e.message ?: "Failed to load")
            }
        }
    )

    LaunchedEffect(rewardedAd.state) {
        when (rewardedAd.state) {
            AdState.READY -> log.d { "Rewarded ad loaded and ready to show" }
            AdState.LOADING -> log.d { "Rewarded ad is loading..." }
            AdState.FAILING -> {
                log.e { "Rewarded ad failed" }
                if (!finished) {
                    finished = true
                    onAdFailed?.invoke("Failed to load")
                }
            }
            else -> log.v { "Rewarded ad state: ${rewardedAd.state}" }
        }
    }

    if (!finished && rewardedAd.state == AdState.READY) {
        RewardedAd(
            loadedAd = rewardedAd,
            onRewardEarned = {
                if (!rewardGranted) {
                    log.d { "User earned reward" }
                    rewardGranted = true
                    onRewardEarned?.invoke(1, "reward")
                }
            },
            onShown = { onAdShown?.invoke() },
            onDismissed = {
                log.d { "Rewarded ad dismissed" }
                if (!finished) {
                    finished = true
                    // Reward callbacks can arrive before dismissal; only report an
                    // unrewarded dismissal if no reward was granted.
                    if (!rewardGranted) onAdDismissed?.invoke()
                }
            },
            onFailure = { e ->
                log.e { "Rewarded ad failed to show: ${e.message}" }
                if (!finished) {
                    finished = true
                    onAdFailed?.invoke(e.message ?: "Failed to show")
                }
            }
        )
    }
}
