package me.calebjones.spacelaunchnow.platform.billing

/**
 * iOS implementation of DirectBillingClient (not supported).
 * iOS uses RevenueCat exclusively.
 */
actual class DirectBillingClient {
    actual suspend fun initialize(): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Direct billing is not supported on iOS. Please use RevenueCat."))
    }
    
    actual suspend fun launchPurchaseFlow(
        productId: String,
        productType: String,
        basePlanId: String?
    ): Result<String> {
        return Result.failure(UnsupportedOperationException("Direct billing is not supported on iOS. Please use RevenueCat."))
    }
    
    actual fun disconnect() {
        // No-op
    }
}

/**
 * Factory function for iOS (throws error).
 */
actual fun createDirectBillingClient(context: Any?): DirectBillingClient {
    return DirectBillingClient()
}

/**
 * Direct billing is NOT supported on iOS.
 */
actual fun isDirectBillingSupported(): Boolean = false
