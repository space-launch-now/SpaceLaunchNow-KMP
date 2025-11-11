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
    val currencyCode: String
)
