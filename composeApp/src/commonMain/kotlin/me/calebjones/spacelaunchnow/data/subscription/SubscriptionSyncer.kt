package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock

/**
 * Handles syncing subscription data between local storage and billing system
 * This runs in the background and updates local storage when purchase state changes
 */
class SubscriptionSyncer(
    private val localStorage: LocalSubscriptionStorage,
    private val billingManager: BillingManager
) {
    private val log = logger()

    private val syncScope = CoroutineScope(SupervisorJob())
    private var lastSyncTime = 0L
    private val syncCooldownMs = 1000L // 1 second cooldown between syncs

    /**
     * Start background syncing
     * Call this once during app initialization
     */
    fun startSyncing() {
        log.i { "Starting background sync" }

        // Listen for billing manager purchase state changes
        syncScope.launch {
            billingManager.purchaseState.collect { purchaseState ->
                val currentTime = Clock.System.now().toEpochMilliseconds()

                // Check if we're in debug/simulation mode
                val currentData = localStorage.get()
                if (currentData.isDebugMode) {
                    log.d { "Debug mode active (isDebugMode=true), skipping automatic sync" }
                    return@collect
                }

                if (currentTime - lastSyncTime > syncCooldownMs) {
                    log.d { "Purchase state updated, syncing - isSubscribed=${purchaseState.isSubscribed}, type=${purchaseState.subscriptionType}, products=${purchaseState.activeProductIds}" }
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
                    
                    val success = localStorage.update(newData)

                    if (success) {
                        log.i { "✅ Sync complete - subscription state persisted successfully" }
                    } else {
                        // Get current stored data to understand what's persisted
                        val currentStored = try {
                            localStorage.get()
                        } catch (e: Exception) {
                            log.e(e) { "Failed to read current stored subscription data for diagnostics" }
                            null
                        }
                        
                        log.e { 
                            buildString {
                                appendLine("❌ CRITICAL: Failed to persist subscription state!")
                                appendLine("User will lose premium access on next app restart")
                                appendLine("Purchase State:")
                                appendLine("  Subscription type: ${purchaseState.subscriptionType.name}")
                                appendLine("  Is subscribed: ${purchaseState.isSubscribed}")
                                appendLine("  Entitlements: ${purchaseState.activeEntitlements.joinToString(",")}")
                                appendLine("  Product IDs: ${purchaseState.activeProductIds.joinToString(",")}")
                                appendLine("Current Stored State:")
                                appendLine("  Subscription type: ${currentStored?.subscriptionType?.name ?: "null"}")
                                appendLine("  Is subscribed: ${currentStored?.isSubscribed ?: false}")
                                appendLine("  Debug mode: ${currentStored?.isDebugMode ?: false}")
                                appendLine("Sync Info:")
                                appendLine("  Sync timestamp: $currentTime")
                                appendLine("  Time since last sync: ${currentTime - lastSyncTime}ms")
                                appendLine("  Cooldown: ${syncCooldownMs}ms")
                                appendLine("Attempted new data: $newData")
                            }
                        }

                        // Mark as needing sync so we retry on next app start
                        localStorage.markNeedsSync()
                    }
                } else {
                    log.d { "Skipping sync (cooldown period active)" }
                }
            }
        }
    }

    /**
     * Manually trigger a sync with billing system
     * Call this when user restores purchases or after a purchase
     */
    suspend fun syncNow(): Boolean {
        log.i { "Manual sync requested" }
        return billingManager.refreshPurchaseState()
    }
}