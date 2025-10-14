package me.calebjones.spacelaunchnow.data.model

/**
 * Product pricing information from platform billing
 */
data class ProductPricing(
    val productId: String,
    val basePlanId: String,
    val formattedPrice: String,        // e.g., "$4.99"
    val priceCurrencyCode: String,     // e.g., "USD"
    val priceAmountMicros: Long,       // Price in micros (e.g., 4990000 for $4.99)
    val billingPeriod: String,         // e.g., "P1M" (monthly), "P1Y" (yearly)
    val title: String,                  // Product title from store
    val description: String             // Product description from store
) {
    /**
     * Get human-readable billing period
     */
    fun getBillingPeriodText(): String {
        return when {
            billingPeriod == "LIFETIME" -> "lifetime"
            billingPeriod.contains("P1M") -> "month"
            billingPeriod.contains("P1Y") -> "year"
            billingPeriod.contains("P3M") -> "3 months"
            billingPeriod.contains("P6M") -> "6 months"
            else -> "period"
        }
    }
    
    /**
     * Check if this is a one-time purchase
     */
    fun isLifetime(): Boolean = billingPeriod == "LIFETIME"
    
    /**
     * Calculate savings compared to another plan
     */
    fun calculateSavingsPercent(comparedTo: ProductPricing): Int {
        if (comparedTo.priceAmountMicros == 0L) return 0
        
        // Normalize to same billing period
        val thisYearlyPrice = when {
            billingPeriod.contains("P1M") -> priceAmountMicros * 12
            billingPeriod.contains("P1Y") -> priceAmountMicros
            else -> priceAmountMicros
        }
        
        val comparedYearlyPrice = when {
            comparedTo.billingPeriod.contains("P1M") -> comparedTo.priceAmountMicros * 12
            comparedTo.billingPeriod.contains("P1Y") -> comparedTo.priceAmountMicros
            else -> comparedTo.priceAmountMicros
        }
        
        if (comparedYearlyPrice == 0L) return 0
        
        val savings = ((comparedYearlyPrice - thisYearlyPrice).toDouble() / comparedYearlyPrice.toDouble()) * 100
        return savings.toInt().coerceAtLeast(0)
    }
}
