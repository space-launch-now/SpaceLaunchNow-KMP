package me.calebjones.spacelaunchnow.data.subscription

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock.System

/**
 * Local subscription data stored on device
 * This is the single source of truth for subscription status
 */
@Serializable
data class LocalSubscriptionData(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val productIds: Set<String> = emptySet(),   // Product IDs user owns
    val lastSynced: Long = 0L,                  // Last time we synced with RevenueCat
    val needsSync: Boolean = true,              // Whether we need to sync with RevenueCat
    val isDebugMode: Boolean = false,           // Whether we're in debug/simulation mode (blocks sync)
    val subscriptionExpiryMs: Long? = null,     // Unix ms when subscription expires; null for lifetime
    val wasEverPremium: Boolean = false,        // Sticky flag: true once user has ever been premium
    val isInTrialPeriod: Boolean = false,       // Whether user is currently in a free trial
    val trialExpiresAt: Long? = null            // Unix ms when the trial expires; null if not in trial
) {
    /**
     * Check if user has a specific feature based on subscription type
     */
    fun hasFeature(feature: PremiumFeature): Boolean {
        return when (subscriptionType) {
            SubscriptionType.FREE -> PremiumFeature.getFreeFeatures().contains(feature)
            SubscriptionType.LEGACY -> PremiumFeature.getBasicFeatures().contains(feature)
            SubscriptionType.PREMIUM -> PremiumFeature.getPremiumFeatures().contains(feature)
            SubscriptionType.LIFETIME -> PremiumFeature.getPremiumFeatures().contains(feature)
        }
    }

    /**
     * Get all features available for this subscription
     */
    val availableFeatures: Set<PremiumFeature>
        get() = PremiumFeature.getFeaturesForType(subscriptionType)

    companion object {
        val DEFAULT = LocalSubscriptionData()

        val FREE = LocalSubscriptionData(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            needsSync = false,
            isDebugMode = false
        )
    }
}

/**
 * Local storage for subscription data using KStore
 * This provides immediate, synchronous access to subscription status
 */
class LocalSubscriptionStorage {
    private val log = logger()

    private val filePath = Path("${AppDirectories.getAppDataDir()}/subscription_data.json")

    private val store: KStore<LocalSubscriptionData> = storeOf(
        file = filePath,
        default = LocalSubscriptionData.DEFAULT
    )

    /**
     * Flow of current subscription data - UI observes this
     * Recovers gracefully from corrupted files by emitting default data
     */
    val subscriptionData: Flow<LocalSubscriptionData> =
        store.updates
            .map { it ?: LocalSubscriptionData.DEFAULT }
            .catch { e ->
                // CRITICAL: wasEverPremium cannot be recovered from a corrupt file.
                // needsSync=true (set in DEFAULT) ensures RC re-syncs on next session
                // and restores the subscription state for currently-active subscribers.
                // Formerly-premium-but-now-lapsed users will lose the wasEverPremium flag.
                log.e(e) {
                    "CRITICAL: Subscription data file corrupted — recovering with FREE defaults. " +
                    "wasEverPremium will be lost if user is no longer actively subscribed. " +
                    "File: $filePath"
                }
                tryDeleteCorruptedFile()
                emit(LocalSubscriptionData.DEFAULT)
            }

    /**
     * Get current subscription data immediately (synchronous)
     * Returns default data if file is corrupted or unreadable
     */
    suspend fun get(): LocalSubscriptionData {
        return try {
            store.get() ?: LocalSubscriptionData.DEFAULT
        } catch (e: Exception) {
            // CRITICAL: wasEverPremium cannot be recovered from a corrupt file.
            // needsSync=true (set in DEFAULT) ensures RC re-syncs and restores
            // active subscribers. Formerly-premium-but-lapsed users will lose the flag.
            log.e(e) {
                "CRITICAL: Subscription data file unreadable — recovering with FREE defaults. " +
                "wasEverPremium will be lost if user is no longer actively subscribed. " +
                "File: $filePath | Error: ${e.message}"
            }
            tryDeleteCorruptedFile()
            LocalSubscriptionData.DEFAULT
        }
    }

    /**
     * Attempt to delete corrupted subscription data file
     * Safe operation - logs but does not throw on failure
     */
    private fun tryDeleteCorruptedFile() {
        try {
            if (SystemFileSystem.exists(filePath)) {
                SystemFileSystem.delete(filePath)
                log.i { "Deleted corrupted subscription data file: $filePath" }
            }
        } catch (e: Exception) {
            log.w(e) { "Failed to delete corrupted file: $filePath" }
        }
    }

    /**
     * Update subscription data with error handling and verification
     * @return true if update was successful, false if it failed
     */
    suspend fun update(data: LocalSubscriptionData): Boolean {
        return try {
            log.d { "Saving subscription data - type=${data.subscriptionType}, subscribed=${data.isSubscribed}, products=${data.productIds}" }

            store.set(data)

            // Verify write succeeded by reading back
            val readBack = store.get()
            val success = readBack == data

            if (success) {
                log.i { "Subscription data saved and verified successfully" }
            } else {
  
                // Detailed field comparison for debugging
                val diagnostics = mutableMapOf<String, Any>(
                    "expected_type" to data.subscriptionType.name,
                    "read_back_type" to (readBack?.subscriptionType?.name ?: "null"),
                    "expected_subscribed" to data.isSubscribed,
                    "read_back_subscribed" to (readBack?.isSubscribed ?: false),
                    "types_match" to (data.subscriptionType == readBack?.subscriptionType),
                    "subscribed_match" to (data.isSubscribed == readBack?.isSubscribed),
                    "product_ids_match" to (data.productIds == readBack?.productIds),
                    "read_back_null" to (readBack == null),
                    "store_file_path" to "${AppDirectories.getAppDataDir()}/subscription_data.json"
                )

                // Add product ID comparison if they differ
                if (data.productIds != readBack?.productIds) {
                    diagnostics["expected_product_ids"] = data.productIds.joinToString(",")
                    diagnostics["read_back_product_ids"] = (readBack?.productIds?.joinToString(",") ?: "")
                }

                log.e { 
                    buildString {
                        appendLine("❌ Verification failed - read-back mismatch!")
                        appendLine("Expected: $data")
                        appendLine("Read back: $readBack")
                        appendLine("Diagnostics:")
                        diagnostics.forEach { (key, value) ->
                            appendLine("  $key: $value")
                        }
                    }
                }
            }

            success
        } catch (e: Exception) {
            log.e(e) { 
                buildString {
                    appendLine("❌ Error saving subscription data")
                    appendLine("Subscription type: ${data.subscriptionType.name}")
                    appendLine("Is subscribed: ${data.isSubscribed}")
                    appendLine("Error: ${e.message}")
                }
            }
            false
        }
    }

    /**
     * Update just the subscription type and features
     * @return true if update was successful, false if it failed
     */
    suspend fun updateSubscription(
        isSubscribed: Boolean,
        subscriptionType: SubscriptionType,
        productIds: Set<String> = emptySet()
    ): Boolean {
        val current = get()
        return update(
            current.copy(
                isSubscribed = isSubscribed,
                subscriptionType = subscriptionType,
                productIds = productIds,
                lastSynced = System.now().toEpochMilliseconds(),
                needsSync = false
            )
        )
    }

    /**
     * Mark that we need to sync with RevenueCat
     * @return true if update was successful, false if it failed
     */
    suspend fun markNeedsSync(): Boolean {
        val current = get()
        return update(current.copy(needsSync = true))
    }

    /**
     * Clear all subscription data (reset to free)
     * @return true if clear was successful, false if it failed
     */
    suspend fun clear(): Boolean {
        val current = get()
        // wasEverPremium is sticky — preserve it even when clearing so a
        // logout + re-login doesn't permanently erase the user's premium history.
        return update(LocalSubscriptionData.FREE.copy(wasEverPremium = current.wasEverPremium))
    }

    /**
     * Check if user has a specific feature (convenience method)
     */
    suspend fun hasFeature(feature: PremiumFeature): Boolean {
        return get().hasFeature(feature)
    }

    // Debug methods for testing different subscription states

    /**
     * Set debug subscription state for testing
     * This directly manipulates local storage for testing purposes
     */
    suspend fun setDebugSubscription(
        subscriptionType: SubscriptionType,
        productId: String = ""
    ) {
        val productIds = if (productId.isNotEmpty()) setOf(productId) else emptySet()
        update(
            LocalSubscriptionData(
                isSubscribed = subscriptionType != SubscriptionType.FREE,
                subscriptionType = subscriptionType,
                productIds = productIds,
                lastSynced = System.now().toEpochMilliseconds(),
                needsSync = false, // Don't sync when in debug mode
                isDebugMode = true // Protect debug state from being overwritten by SubscriptionSyncer
            )
        )
    }

    /**
     * Clear debug state and reset to free tier
     * Use this to exit debug mode and return to real state
     */
    suspend fun clearDebugState() {
        val current = get()
        // wasEverPremium is sticky — preserve it so exiting debug mode on a real premium
        // account doesn't permanently erase the user's premium history before RC re-syncs.
        update(LocalSubscriptionData.FREE.copy(needsSync = true, isDebugMode = false, wasEverPremium = current.wasEverPremium))
    }

    /**
     * Check if we're currently in a debug/simulated state
     * A debug state is indicated by isDebugMode = true (manually set by setDebugSubscription)
     * This makes it easy to detect simulation mode without time-based heuristics
     */
    suspend fun isInDebugMode(): Boolean {
        val current = get()
        // Debug mode is active when explicitly set by setDebugSubscription
        return current.isDebugMode
    }
}
