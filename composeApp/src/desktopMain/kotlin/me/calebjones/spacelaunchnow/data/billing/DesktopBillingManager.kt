package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Desktop implementation of BillingManager - No billing support
 * Desktop apps don't have in-app purchases, so this is a no-op implementation
 */
class DesktopBillingManager : BillingManager {
    private val log = logger()
    
    private val _isInitialized = MutableStateFlow(true) // Always initialized
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Always FREE on desktop
    private val _purchaseState = MutableStateFlow(
        PurchaseState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            activeEntitlements = emptySet(),
            activeProductIds = emptySet(),
            features = emptySet(),
            lastRefreshed = System.currentTimeMillis()
        )
    )
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    override suspend fun initialize(appUserId: String?): Result<Unit> {
        log.d { "DesktopBillingManager: ℹ️ No billing support on desktop platform" }
        return Result.success(Unit)
    }
    
    override suspend fun refreshPurchaseState(): Boolean {
        log.d { "DesktopBillingManager: ℹ️ No purchase state to refresh on desktop" }
        return true
    }
    
    override suspend fun getAvailableProducts(): Result<List<ProductInfo>> {
        log.d { "DesktopBillingManager: ℹ️ No products available on desktop platform" }
        return Result.success(emptyList())
    }
    
    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        log.w { "DesktopBillingManager: ⚠️ In-app purchases not supported on desktop platform" }
        return Result.failure(UnsupportedOperationException("In-app purchases not supported on desktop"))
    }
    
    override suspend fun restorePurchases(): Result<PurchaseState> {
        log.d { "DesktopBillingManager: ℹ️ No purchases to restore on desktop" }
        return Result.success(_purchaseState.value)
    }
    
    override suspend fun syncPurchases() {
        log.d { "DesktopBillingManager: ℹ️ No purchases to sync on desktop" }
        // No-op
    }
    
    override fun hasEntitlement(entitlementId: String): Boolean {
        log.d { "DesktopBillingManager: ℹ️ No entitlements on desktop platform" }
        return false
    }
    
    override fun getActiveEntitlements(): Set<String> {
        log.d { "DesktopBillingManager: ℹ️ No active entitlements on desktop" }
        return emptySet()
    }
}
