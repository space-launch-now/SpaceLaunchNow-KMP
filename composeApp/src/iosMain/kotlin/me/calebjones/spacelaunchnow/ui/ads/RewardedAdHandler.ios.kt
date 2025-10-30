package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.RewardedAd
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.LocalPreloadedRewardedAd
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature

/**
 * iOS implementation of RewardedAdHandler using BasicAds library.
 * 
 * Rewarded ad handler that shows preloaded rewarded ads.
 * - Checks if the user has ad-free premium
 * - Uses the preloaded rewarded ad for instant showing
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
actual fun RewardedAdHandler(
    shouldShow: Boolean,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)?,
    onAdShown: (() -> Unit)?,
    onAdFailed: ((String) -> Unit)?
) {
    val contextFactory = LocalContextFactory.current
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    // 🚀 USE PRELOADED AD: Get preloaded rewarded ad from CompositionLocal
    val preloadedRewardedAd = LocalPreloadedRewardedAd.current

    // Don't show ads if:
    // 1. User has ad-free premium feature
    // 2. Not on a mobile platform (Android/iOS)
    // 3. No context factory available
    // 4. No preloaded rewarded ad available
    if (hasAdFree ||
        !getPlatform().type.isMobile ||
        contextFactory == null ||
        preloadedRewardedAd == null ||
        !shouldShow
    ) {
        if (shouldShow) {
            println("⚠️ RewardedAdHandler: Not showing ad due to conditions.")
        }
        return
    }

    // Use the preloaded rewarded ad
    val rewardedAd = preloadedRewardedAd
    
    // Track if reward has been granted to avoid duplicate grants
    var rewardGranted by remember { mutableStateOf(false) }

    // Handle ad state changes
    LaunchedEffect(rewardedAd.state, shouldShow) {
        when (rewardedAd.state) {
            AdState.READY -> {
                if (shouldShow) {
                    println("🎯 RewardedAd: Ad loaded successfully and ready to show")
                    // Reset reward granted flag when showing new ad
                    rewardGranted = false
                }
            }

            AdState.SHOWING -> {
                println("✅ RewardedAd: Ad is showing!")
                onAdShown?.invoke()
            }

            AdState.DISMISSED -> {
                println("🎯 RewardedAd: Ad dismissed by user")
            }

            AdState.FAILING -> {
                println("❌ RewardedAd: Ad failed to load")
                onAdFailed?.invoke("Failed to load")
            }

            AdState.LOADING -> {
                println("⏳ RewardedAd: Ad is loading...")
            }

            AdState.SHOWN -> {
                println("✅ RewardedAd: Ad has finished showing")
                // Grant reward when ad completes (SHOWN state)
                if (!rewardGranted) {
                    println("🎉 RewardedAd: User earned reward!")
                    onRewardEarned?.invoke(1, "reward")
                    rewardGranted = true
                }
            }

            AdState.NONE -> {
                println("⚪ RewardedAd: Initial state")
            }

            else -> {
                println("🔄 RewardedAd: State: ${rewardedAd.state}")
            }
        }
    }

    // Show the rewarded ad using the Composable pattern (required by basic-ads)
    if (shouldShow && rewardedAd.state == AdState.READY) {
        RewardedAd(
            loadedAd = rewardedAd,
            onRewardEarned = {
                println("🎉 RewardedAd: User earned reward!")
                onRewardEarned?.invoke(1, "reward") // Default reward values
            }
        )
    }
}

/**
 * Helper function to manually trigger a rewarded ad.
 * Useful for premium trial features or rewards.
 */
@Composable
fun TriggerRewardedAdIfReady(
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)? = null,
    onAdShown: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null
) {
    RewardedAdHandler(
        shouldShow = true,
        onRewardEarned = onRewardEarned,
        onAdShown = onAdShown,
        onAdFailed = onAdFailed
    )
}