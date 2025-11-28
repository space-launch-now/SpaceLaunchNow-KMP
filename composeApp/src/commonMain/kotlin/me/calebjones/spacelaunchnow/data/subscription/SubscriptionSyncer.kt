package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import kotlin.time.Clock

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
                val currentTime = Clock.System.now().toEpochMilliseconds()

                // Check if we're in debug/simulation mode
                val currentData = localStorage.get()
                if (currentData.isDebugMode) {
                    println("SubscriptionSyncer: 🎭 Debug mode active (isDebugMode=true), skipping automatic sync")
                    return@collect
                }

                if (currentTime - lastSyncTime > syncCooldownMs) {
                    println("SubscriptionSyncer: Purchase state updated, syncing...")
                    println("  isSubscribed=${purchaseState.isSubscribed}, type=${purchaseState.subscriptionType}")
                    println("  products=${purchaseState.activeProductIds}")
                    lastSyncTime = currentTime

                    // Update local storage with new purchase state
                    val newData = LocalSubscriptionData(
                        isSubscribed = purchaseState.isSubscribed,
                        subscriptionType = purchaseState.subscriptionType,
                        productIds = purchaseState.activeProductIds,
                        entitlements = purchaseState.activeEntitlements,
                        lastSynced = currentTime,
                        needsSync = false,
                        isDebugMode = false // Real sync, not debug mode
                    )
                    
                    println("SubscriptionSyncer: ✅ Sync complete - isSubscribed=${purchaseState.isSubscribed}")

                    val success = localStorage.update(newData)

                    if (success) {
                        println("SubscriptionSyncer: ✅ Sync complete - subscription state persisted successfully")
                    } else {
                        println("SubscriptionSyncer: ❌ CRITICAL: Failed to persist subscription state!")
                        println("  User will lose premium access on next app restart")

                        me.calebjones.spacelaunchnow.analytics.DatadogLogger.error(
                            "Failed to persist subscription state during sync",
                            null,
                            mapOf(
                                "subscription_type" to purchaseState.subscriptionType.name,
                                "is_subscribed" to purchaseState.isSubscribed,
                                "entitlements" to purchaseState.activeEntitlements.joinToString(","),
                                "product_ids" to purchaseState.activeProductIds.joinToString(",")
                            )
                        )

                        // Mark as needing sync so we retry on next app start
                        localStorage.markNeedsSync()
                    }
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