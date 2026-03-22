package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Desktop implementation of BillingManager - Premium granted by default
 * Desktop apps don't have in-app purchases, so premium is granted automatically
 */
class DesktopBillingManager : BillingManager {
    private val log = logger()
    
    private val _isInitialized = MutableStateFlow(true) // Always initialized
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Desktop users get premium for free
    private val _purchaseState = MutableStateFlow(
        PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.LIFETIME,
            activeEntitlements = setOf("pro"),
            activeProductIds = setOf("desktop_premium"),
            features = PremiumFeature.getPremiumFeatures(),
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
        // Desktop always has premium entitlements
        return true
    }
    
    override fun getActiveEntitlements(): Set<String> {
        return setOf("pro")
    }
}
