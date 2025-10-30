package me.calebjones.spacelaunchnow.platform.billing

/**
 * Desktop implementation of DirectBillingClient (not supported).
 * Desktop builds don't have billing functionality.
 */
actual class DirectBillingClient {
    actual suspend fun initialize(): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Direct billing is not supported on Desktop."))
    }
    
    actual suspend fun launchPurchaseFlow(
        productId: String,
        productType: String,
        basePlanId: String?
    ): Result<String> {
        return Result.failure(UnsupportedOperationException("Direct billing is not supported on Desktop."))
    }
    
    actual fun disconnect() {
        // No-op
    }
}

/**
 * Factory function for Desktop (throws error).
 */
actual fun createDirectBillingClient(context: Any?): DirectBillingClient {
    return DirectBillingClient()
}

/**
 * Direct billing is NOT supported on Desktop.
 */
actual fun isDirectBillingSupported(): Boolean = false
