package me.calebjones.spacelaunchnow.data.subscription

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
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
    val entitlements: Set<String> = emptySet(), // RevenueCat entitlement IDs
    val productIds: Set<String> = emptySet(),   // Product IDs user owns
    val lastSynced: Long = 0L,                  // Last time we synced with RevenueCat
    val needsSync: Boolean = true,              // Whether we need to sync with RevenueCat
    val isDebugMode: Boolean = false            // Whether we're in debug/simulation mode (blocks sync)
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

    private val store: KStore<LocalSubscriptionData> = storeOf(
        file = Path("${AppDirectories.getAppDataDir()}/subscription_data.json"),
        default = LocalSubscriptionData.DEFAULT
    )

    /**
     * Flow of current subscription data - UI observes this
     */
    val subscriptionData: Flow<LocalSubscriptionData> =
        store.updates.map { it ?: LocalSubscriptionData.DEFAULT }

    /**
     * Get current subscription data immediately (synchronous)
     */
    suspend fun get(): LocalSubscriptionData {
        return store.get() ?: LocalSubscriptionData.DEFAULT
    }

    /**
     * Update subscription data with error handling and verification
     * @return true if update was successful, false if it failed
     */
    suspend fun update(data: LocalSubscriptionData): Boolean {
        return try {
            log.d { "Saving subscription data - Type: ${data.subscriptionType}, Subscribed: ${data.isSubscribed}, Entitlements: ${data.entitlements}" }

            store.set(data)

            // Verify write succeeded by reading back
            val readBack = store.get()
            val success = readBack == data

            if (success) {
                log.i { "Subscription data saved and verified successfully" }
            } else {
                log.e { "❌ Verification failed - read-back mismatch! Expected: $data, Read back: $readBack" }
                me.calebjones.spacelaunchnow.analytics.DatadogLogger.error(
                    "Subscription state verification failed - read-back mismatch",
                    null,
                    mapOf(
                        "expected_type" to data.subscriptionType.name,
                        "read_back_type" to (readBack?.subscriptionType?.name ?: "null"),
                        "expected_subscribed" to data.isSubscribed,
                        "read_back_subscribed" to (readBack?.isSubscribed ?: false)
                    )
                )
            }

            success
        } catch (e: Exception) {
            log.e(e) { "❌ Error saving subscription data: ${e.message}" }
            me.calebjones.spacelaunchnow.analytics.DatadogLogger.error(
                "Failed to save subscription state to KStore",
                e,
                mapOf(
                    "subscription_type" to data.subscriptionType.name,
                    "is_subscribed" to data.isSubscribed,
                    "error_type" to (e::class.simpleName ?: "Unknown"),
                    "error_message" to (e.message ?: "No message")
                )
            )
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
        entitlements: Set<String> = emptySet(),
        productIds: Set<String> = emptySet()
    ): Boolean {
        val current = get()
        return update(
            current.copy(
                isSubscribed = isSubscribed,
                subscriptionType = subscriptionType,
                entitlements = entitlements,
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
        return update(LocalSubscriptionData.FREE)
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
        productId: String = "",
        entitlements: Set<String> = emptySet()
    ) {
        val productIds = if (productId.isNotEmpty()) setOf(productId) else emptySet()
        update(
            LocalSubscriptionData(
                isSubscribed = subscriptionType != SubscriptionType.FREE,
                subscriptionType = subscriptionType,
                entitlements = entitlements,
                productIds = productIds,
                lastSynced = System.now().toEpochMilliseconds(),
                needsSync = false // Don't sync when in debug mode
            )
        )
    }

    /**
     * Clear debug state and reset to free tier
     * Use this to exit debug mode and return to real state
     */
    suspend fun clearDebugState() {
        update(LocalSubscriptionData.FREE.copy(needsSync = true, isDebugMode = false)) // Force sync to get real state
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
