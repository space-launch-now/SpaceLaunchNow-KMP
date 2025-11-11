package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingManager

/**
 * Handles syncing subscription data between local storage and billing system
 * This runs in the background and updates local storage when purchase state changes
 */
class SubscriptionSyncer(
    private val localStorage: LocalSubscriptionStorage,
    private val billingManager: BillingManager
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
        
        // Listen for billing manager purchase state changes
        syncScope.launch {
            billingManager.purchaseState.collect { purchaseState ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastSyncTime > syncCooldownMs) {
                    println("SubscriptionSyncer: Purchase state updated, syncing...")
                    lastSyncTime = currentTime
                    
                    // Update local storage with new purchase state
                    localStorage.update(
                        isSubscribed = purchaseState.isSubscribed,
                        subscriptionType = purchaseState.subscriptionType,
                        productIds = purchaseState.activeProductIds,
                        availableFeatures = purchaseState.features,
                        needsSync = false
                    )
                    
                    println("SubscriptionSyncer: ✅ Sync complete")
                } else {
                    println("SubscriptionSyncer: Skipping sync (cooldown period active)")
                }
            }
        }
    }
    
    /**
     * Manually trigger a sync with billing system
     * Call this when user restores purchases or after a purchase
     */
    suspend fun syncNow(): Boolean {
        println("SubscriptionSyncer: Manual sync requested")
        return billingManager.refreshPurchaseState()
    }
}