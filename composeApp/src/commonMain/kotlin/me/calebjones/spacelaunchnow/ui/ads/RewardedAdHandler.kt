package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.LocalPreloadedRewardedAd
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature

/**
 * Rewarded ad handler that shows preloaded rewarded ads.
 * 
 * Usage: Call this composable when you want to show a rewarded ad
 * It will automatically:
 * - Check if the user has ad-free premium
 * - Use the preloaded rewarded ad for instant showing
 * - Only show on mobile platforms (Android/iOS)
 * 
 * @param shouldShow Whether to trigger showing the rewarded ad
 * @param onRewardEarned Called when the user earns a reward (with reward amount and type)
 * @param onAdShown Called when a rewarded ad is successfully shown
 * @param onAdFailed Called when a rewarded ad fails to load or show
 */
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun RewardedAdHandler(
    shouldShow: Boolean = false,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)? = null,
    onAdShown: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null
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

    // Handle ad state changes
    LaunchedEffect(rewardedAd.state, shouldShow) {
        when (rewardedAd.state) {
            AdState.READY -> {
                if (shouldShow) {
                    println("🎯 RewardedAd: Ad loaded successfully, showing now...")
                    try {
                        rewardedAd.show(onRewardEarned = { })
                    } catch (e: Exception) {
                        println("❌ RewardedAd: Failed to show ad: ${e.message}")
                        onAdFailed?.invoke("Failed to show: ${e.message}")
                    }
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
                onRewardEarned?.invoke(1, "reward") // Replace with actual values if available
            }
            AdState.NONE -> {
                println("⚪ RewardedAd: Initial state")
            }
            else -> {
                println("🔄 RewardedAd: State: ${rewardedAd.state}")
            }
        }
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