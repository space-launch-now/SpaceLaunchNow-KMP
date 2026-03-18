package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("RewardedAdHandler") }

/**
 * Desktop implementation of RewardedAdHandler (no-op).
 * Desktop doesn't support ads, so this is an empty composable.
 */
@Composable
actual fun RewardedAdHandler(
    shouldShow: Boolean,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)?,
    onAdShown: (() -> Unit)?,
    onAdFailed: ((String) -> Unit)?
) {
    // No-op: Desktop doesn't show ads
    if (shouldShow) {
        log.d { "🎯 RewardedAdHandler (Desktop): Ads not supported, skipping" }
    }
}
