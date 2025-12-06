package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.calebjones.spacelaunchnow.data.model.PlatformPurchase
import me.calebjones.spacelaunchnow.logger

/**
 * Desktop implementation - No billing support
 * 
 * Desktop apps typically don't have in-app purchases.
 * If needed, implement server-side licensing instead.
 */
actual class BillingClient {
    private val log = logger()
    
    actual val purchaseUpdates: Flow<PlatformPurchase> = flowOf()

    actual suspend fun initialize(): Result<Unit> {
        log.d { "DesktopBillingClient: No billing support on desktop" }
        return Result.success(Unit)
    }

    actual suspend fun queryPurchases(): Result<List<PlatformPurchase>> {
        // Desktop - no purchases
        return Result.success(emptyList())
    }

    actual suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<String> {
        // Desktop - no purchase flow
        return Result.failure(UnsupportedOperationException("In-app purchases not supported on desktop"))
    }

    actual suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> {
        return Result.success(Unit)
    }

    actual suspend fun getAvailableProducts(): Result<List<String>> {
        return Result.success(emptyList())
    }

    actual suspend fun getProductPricing(productId: String): Result<List<me.calebjones.spacelaunchnow.data.model.ProductPricing>> {
        return Result.success(emptyList())
    }

    actual fun disconnect() {
        // No-op
    }
}

/**
 * Factory function to create Desktop BillingClient
 * No parameters needed for desktop
 */
actual fun createBillingClient(): BillingClient {
    return BillingClient()
}
