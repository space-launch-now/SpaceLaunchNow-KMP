package me.calebjones.spacelaunchnow.data.model

/**
 * Product information for in-app purchases
 * Platform-agnostic representation of a purchasable product
 */
data class ProductInfo(
    val productId: String,
    val basePlanId: String?,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val currencyCode: String,
    // Trial/intro offer fields — all have defaults for backward compatibility
    val hasFreeTrial: Boolean = false,
    val freeTrialPeriodDisplay: String? = null,
    val freeTrialPeriodValue: Int? = null,
    val freeTrialPeriodUnit: String? = null,
    val hasIntroOffer: Boolean = false,
    val introOfferPrice: String? = null,
    val introOfferPeriodDisplay: String? = null,
    /** Length of one intro billing period (e.g. 1 for "1 month"), or null when there is no intro offer. */
    val introOfferPeriodValue: Int? = null,
    /** Unit name of the intro period, e.g. "MONTH". */
    val introOfferPeriodUnit: String? = null,
    /** Number of billing cycles the intro price lasts. */
    val introOfferCycles: Int = 1
) {
    /**
     * True when the user can get a single-month intro price: an eligible intro offer with a
     * price, lasting exactly one cycle of one month. [hasIntroOffer] already carries the
     * store eligibility check, so this is also false for a user who cannot get the offer. It is
     * false too when a free trial exists: the store's default purchase option would then be the
     * trial, not the paid intro price.
     */
    val isFirstMonthIntroOffer: Boolean
        get() = hasIntroOffer &&
            !hasFreeTrial &&
            introOfferPrice != null &&
            introOfferPeriodValue == 1 &&
            introOfferPeriodUnit == "MONTH" &&
            introOfferCycles == 1
}
