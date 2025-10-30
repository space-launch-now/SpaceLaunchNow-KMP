package me.calebjones.spacelaunchnow.platform.billing

/**
 * Direct billing client for testing custom SKU purchases.
 * This bypasses RevenueCat and uses the platform's native billing library.
 * 
 * Android: Uses Google Play Billing Library
 * iOS: Not supported (returns error)
 * Desktop: Not supported (returns error)
 */
expect class DirectBillingClient() {
    /**
     * Initialize the billing client.
     * @return Result success or failure with error
     */
    suspend fun initialize(): Result<Unit>
    
    /**
     * Launch a purchase flow for a specific product.
     * 
     * @param productId The product ID/SKU to purchase
     * @param productType Type of product: "inapp" for one-time purchases, "subs" for subscriptions
     * @param basePlanId Optional base plan ID for subscription products
     * @return Result with purchase token on success, or error on failure
     */
    suspend fun launchPurchaseFlow(
        productId: String,
        productType: String = "inapp",
        basePlanId: String? = null
    ): Result<String>
    
    /**
     * Disconnect from the billing service.
     */
    fun disconnect()
}

/**
 * Factory function to create DirectBillingClient with platform-specific context.
 * 
 * @param context Platform-specific context (Activity on Android, null on other platforms)
 * @return DirectBillingClient instance
 */
expect fun createDirectBillingClient(context: Any?): DirectBillingClient

/**
 * Check if direct billing is supported on this platform.
 */
expect fun isDirectBillingSupported(): Boolean
