package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import me.calebjones.spacelaunchnow.logger

private val log by lazy { logger() }

/**
 * Desktop implementation of SmartBannerAd (no-op).
 * Desktop doesn't support ads, so this is an empty composable.
 */
@Composable
actual fun SmartBannerAd(
    modifier: Modifier,
    placementType: AdPlacementType,
    showRemoveAdsButton: Boolean,
    showCard: Boolean,
    onRemoveAdsClick: (() -> Unit)?,
    onSizeChanged: ((widthDp: Dp, heightPx: Int) -> Unit)?
) {
    // No-op: Desktop doesn't show ads
    log.d { "🎯 SmartBannerAd (Desktop): Ads not supported, skipping" }
}
