package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.flow.StateFlow
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Repository for managing subscription state
 * 
 * ARCHITECTURE:
 * - RevenueCat entitlements as authoritative source of truth
 * - Cache in DataStore for instant UI feedback
 * - Server-side receipt validation via RevenueCat
 * - Automatic cross-platform sync
 * - Falls back to cached state for offline scenarios
 */
interface SubscriptionRepository {
    
    /**
     * Single source of truth for UI
     * Emits cached state immediately, then updates after verification
     */
    val state: StateFlow<SubscriptionState>
    
    /**
     * Initialize the repository
     * - Load cached state
     * - Initialize billing client
     * - Verify subscription status
     */
    suspend fun initialize()
    
    /**
     * Verify subscription status with RevenueCat
     * 
     * Queries RevenueCat for active purchases and entitlements.
     * Updates cached state with verified information.
     * 
     * @param forceRefresh Force verification even if recently verified
     * @return Result with verified subscription state
     */
    suspend fun verifySubscription(forceRefresh: Boolean = false): Result<SubscriptionState>
    
    /**
     * Launch purchase flow for a subscription product
     * 
     * @param productId The product to purchase
     * @param basePlanId The base plan (e.g., "base-plan" for monthly, "yearly" for yearly)
     * @return Result with purchase token on success
     */
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String? = null): Result<String>
    
    /**
     * Restore purchases (useful for iOS or device changes)
     * Queries platform for all purchases and updates state
     */
    suspend fun restorePurchases(): Result<SubscriptionState>
    
    /**
     * Get product pricing from platform
     * 
     * @param productId The product ID to query
     * @return List of pricing for all base plans
     */
    suspend fun getProductPricing(productId: String): Result<List<me.calebjones.spacelaunchnow.data.model.ProductPricing>>
    
    /**
     * Check if user has access to a specific feature
     * 
     * Uses RevenueCat entitlements as the authoritative source of truth.
     * Falls back to cached state for offline/debug scenarios.
     * 
     * @param feature The feature to check
     * @return True if user has access to the feature
     */
    suspend fun hasFeature(feature: PremiumFeature): Boolean
    
    /**
     * Get all features available to current subscription
     */
    suspend fun getAvailableFeatures(): Set<PremiumFeature>
    
    /**
     * Cancel subscription (platform-specific)
     * Note: On mobile, this typically opens the platform's subscription management
     */
    suspend fun cancelSubscription(): Result<Unit>
    
    /**
     * Clear cached subscription data
     * Use when user logs out
     */
    suspend fun clearSubscriptionCache()
    
    /**
     * Force refresh widget access for debugging
     * Call this method to manually check and update widget access
     */
    suspend fun forceRefreshWidgetAccess(): Boolean
}
