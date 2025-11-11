package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.StateFlow
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.ProductInfo

/**
 * Platform-agnostic billing manager interface
 * Implementations handle RevenueCat on iOS/Android, no-op on Desktop
 */
interface BillingManager {
    
    /**
     * Initialization state
     */
    val isInitialized: StateFlow<Boolean>
    
    /**
     * Current purchase state
     * Emits whenever entitlements change
     */
    val purchaseState: StateFlow<PurchaseState>
    
    /**
     * Initialize the billing system
     * Must be called before any other operations
     */
    suspend fun initialize(appUserId: String? = null): Result<Unit>
    
    /**
     * Refresh purchase state from server
     * Returns true if successful
     */
    suspend fun refreshPurchaseState(): Boolean
    
    /**
     * Get available products for purchase
     */
    suspend fun getAvailableProducts(): Result<List<ProductInfo>>
    
    /**
     * Launch purchase flow for a product
     */
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String? = null): Result<Unit>
    
    /**
     * Restore previous purchases
     * Useful for users who reinstalled the app
     */
    suspend fun restorePurchases(): Result<PurchaseState>
    
    /**
     * Check if user has a specific entitlement
     */
    fun hasEntitlement(entitlementId: String): Boolean
    
    /**
     * Get all active entitlements
     */
    fun getActiveEntitlements(): Set<String>
    
    /**
     * Sync purchases from store (silent, no UI)
     */
    suspend fun syncPurchases()
}

/**
 * Factory function to create platform-specific BillingManager
 */
expect fun createBillingManager(): BillingManager
