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
    val introOfferPeriodDisplay: String? = null
)
