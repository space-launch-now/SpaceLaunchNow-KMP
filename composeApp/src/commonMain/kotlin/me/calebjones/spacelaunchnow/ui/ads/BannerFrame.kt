package me.calebjones.spacelaunchnow.ui.ads

/**
 * AdMob policy "Modified ad code: Resizing Ad Frames": a banner AdView must show at the exact
 * size it was loaded at. A slot never clips, squeezes or stretches it. When the slot is too
 * narrow for SmartBannerAd's card, the ad drops its chrome; when the slot is narrower than the
 * ad itself, the slot shows no ad.
 */
internal object BannerFrame {

    /** Card outer padding, each side, in dp. */
    const val CARD_OUTER_PAD_DP = 8

    /** Padding between the card edge and the ad, each side, in dp. */
    const val CARD_INNER_PAD_DP = 8

    /** Padding around a banner with no card, each side, in dp. */
    const val PADDED_PAD_DP = 8

    /** How SmartBannerAd frames a banner, from most chrome to none. */
    enum class Chrome(val widthDp: Int) {
        CARD(2 * (CARD_OUTER_PAD_DP + CARD_INNER_PAD_DP)),
        PADDED(2 * PADDED_PAD_DP),
        BARE(0),

        /** The slot is narrower than the ad: show no ad. */
        NONE(0)
    }

    /**
     * The most chrome that still lets an ad loaded at [adWidthDp] x [adHeightDp] show at full
     * size in a slot [slotWidthDp] wide. [preferCard] is false for slots that never use a card.
     */
    fun chromeFor(adWidthDp: Int, adHeightDp: Int, slotWidthDp: Float, preferCard: Boolean): Chrome {
        if (adWidthDp <= 0 || adHeightDp <= 0) return Chrome.NONE
        val candidates = if (preferCard) {
            listOf(Chrome.CARD, Chrome.PADDED, Chrome.BARE)
        } else {
            listOf(Chrome.PADDED, Chrome.BARE)
        }
        return candidates.firstOrNull { slotWidthDp - it.widthDp >= adWidthDp } ?: Chrome.NONE
    }
}
