package me.calebjones.spacelaunchnow.ui.ads

/**
 * AdMob policy "Modified ad code: Resizing Ad Frames": a banner AdView must show at the exact
 * size it was loaded at. A slot never clips, squeezes or stretches it; a slot too narrow for
 * the ad shows no ad.
 */
internal object BannerFrame {

    /** Horizontal space SmartBannerAd's own chrome takes, in dp, around the AdView. */
    fun chromeWidthDp(showCard: Boolean): Float =
        if (showCard) CARD_CHROME_DP else NAV_CHROME_DP

    /**
     * True when an ad loaded at [adWidthDp] x [adHeightDp] fits at full size in a slot that is
     * [slotWidthDp] wide, after the [chromeDp] of padding around the AdView.
     */
    fun fits(adWidthDp: Int, adHeightDp: Int, slotWidthDp: Float, chromeDp: Float): Boolean =
        adWidthDp > 0 && adHeightDp > 0 && slotWidthDp - chromeDp >= adWidthDp

    // Card: 8 dp outer padding + 8 dp inner padding, on each side.
    private const val CARD_CHROME_DP = 32f

    // No card: 8 dp padding on each side.
    private const val NAV_CHROME_DP = 16f
}
