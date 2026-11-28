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
     * True when the user can get a monthly intro price: an eligible intro offer with a price,
     * billed per one-month period for one or more cycles ([introOfferCycles] months). For
     * example $0.99/month for the first 3 months. [hasIntroOffer] already carries the store
     * eligibility check, so this is also false for a user who cannot get the offer. It is false
     * too when a free trial exists: the store's default purchase option would then be the trial,
     * not the paid intro price.
     */
    val isMonthlyIntroOffer: Boolean
        get() = hasIntroOffer &&
            !hasFreeTrial &&
            introOfferPrice != null &&
            introOfferPeriodValue == 1 &&
            introOfferPeriodUnit == "MONTH" &&
            introOfferCycles >= 1

    /** True for a [isMonthlyIntroOffer] that lasts exactly one month. */
    val isFirstMonthIntroOffer: Boolean
        get() = isMonthlyIntroOffer && introOfferCycles == 1
}
