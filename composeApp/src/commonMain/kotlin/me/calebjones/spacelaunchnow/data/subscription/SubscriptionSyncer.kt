package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Handles syncing subscription data between local storage and RevenueCat
 * This runs in the background and updates local storage when RevenueCat data changes
 */
class SubscriptionSyncer(
    private val localStorage: LocalSubscriptionStorage,
    private val revenueCatManager: RevenueCatManager
) {
    
    private val syncScope = CoroutineScope(SupervisorJob())
    private var lastSyncTime = 0L
    private val syncCooldownMs = 1000L // 1 second cooldown between syncs
    
    /**
     * Start background syncing
     * Call this once during app initialization
     */
    fun startSyncing() {
        println("SubscriptionSyncer: Starting background sync")
        
        // Sync immediately if needed
        syncScope.launch {
            val current = localStorage.get()
            if (current.needsSync) {
                println("SubscriptionSyncer: Initial sync needed")
                syncWithRevenueCat(skipRefresh = false) // Initial sync should refresh
            }
        }
        
        // Listen for RevenueCat customer info changes and sync
        // TEMPORARILY DISABLED to fix infinite loop
        // TODO: Re-enable with proper feedback loop prevention
        /*
        syncScope.launch {
            revenueCatManager.customerInfo.collect { customerInfo ->
                if (customerInfo != null) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastSyncTime > syncCooldownMs) {
                        println("SubscriptionSyncer: RevenueCat customer info updated, syncing...")
                        lastSyncTime = currentTime
                        syncWithRevenueCat(skipRefresh = true) // Don't refresh since we're reacting to a change
                    } else {
                        println("SubscriptionSyncer: Skipping sync (cooldown period active)")
                    }
                }
            }
        }
        */
    }
    
    /**
     * Manually trigger a sync with RevenueCat
     * Call this when user restores purchases or after a purchase
     */
    suspend fun syncNow(): Boolean {
        println("SubscriptionSyncer: Manual sync requested")
        return syncWithRevenueCat(skipRefresh = false) // Manual calls always refresh
    }
    
    /**
     * Sync local data with RevenueCat entitlements
     */
    private suspend fun syncWithRevenueCat(skipRefresh: Boolean = false): Boolean {
        return try {
            println("SubscriptionSyncer: Syncing with RevenueCat...")
            
            // Refresh RevenueCat customer info only if not skipping
            if (!skipRefresh) {
                println("SubscriptionSyncer: Refreshing customer info...")
                val refreshSuccess = revenueCatManager.refreshCustomerInfo()
                if (!refreshSuccess) {
                    println("SubscriptionSyncer: Failed to refresh RevenueCat data")
                    return false
                }
            } else {
                println("SubscriptionSyncer: Skipping refresh (reacting to existing customer info change)")
            }
            
            // Get current entitlements
            val activeEntitlements = revenueCatManager.getActiveEntitlements()
            println("SubscriptionSyncer: Active entitlements: $activeEntitlements")
            
            // Determine subscription type from entitlements
            val subscriptionType = determineSubscriptionType(activeEntitlements)
            val isSubscribed = subscriptionType != SubscriptionType.FREE
            
            // Get active product IDs from multiple sources
            val customerInfo = revenueCatManager.customerInfo.value
            val activeSubscriptions = customerInfo?.activeSubscriptions?.toSet() ?: emptySet()
            
            // Also try to get product IDs from entitlements
            val entitlementProductIds = customerInfo?.entitlements?.active?.values?.mapNotNull { entitlement ->
                entitlement.productIdentifier
            }?.toSet() ?: emptySet()
            
            // Combine both sources
            val productIds = activeSubscriptions + entitlementProductIds
            
            println("SubscriptionSyncer: Customer info: $customerInfo")
            println("SubscriptionSyncer: Active subscriptions: ${customerInfo?.activeSubscriptions}")
            println("SubscriptionSyncer: Entitlement product IDs: $entitlementProductIds")
            println("SubscriptionSyncer: Combined product IDs: $productIds")
            println("SubscriptionSyncer: Determined subscription: $subscriptionType (subscribed: $isSubscribed)")
            
            // Update local storage
            localStorage.updateSubscription(
                isSubscribed = isSubscribed,
                subscriptionType = subscriptionType,
                entitlements = activeEntitlements,
                productIds = productIds
            )
            
            println("SubscriptionSyncer: ✅ Sync complete")
            true
            
        } catch (e: Exception) {
            println("SubscriptionSyncer: ❌ Sync failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Determine subscription type from active entitlements
     */
    private fun determineSubscriptionType(entitlements: Set<String>): SubscriptionType {
        return when {
            // Check for LIFETIME entitlement (ID or name)
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LIFETIME) ||
            entitlements.contains("Lifetime") -> {
                println("SubscriptionSyncer: User has LIFETIME entitlement")
                SubscriptionType.LIFETIME
            }
            // Check for PRO entitlement (ID or name)
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_PRO) ||
            entitlements.contains("Pro") ||
            entitlements.contains("Premium") -> {
                println("SubscriptionSyncer: User has PRO entitlement")
                SubscriptionType.PREMIUM
            }
            // Check for LEGACY entitlement (ID or name)
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LEGACY) ||
            entitlements.contains("Legacy") -> {
                println("SubscriptionSyncer: User has LEGACY entitlement")
                SubscriptionType.LEGACY
            }
            else -> {
                println("SubscriptionSyncer: No premium entitlements found (entitlements: $entitlements)")
                SubscriptionType.FREE
            }
        }
    }
}