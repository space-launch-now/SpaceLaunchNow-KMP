@file:OptIn(ExperimentalTime::class)

package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

private val log by lazy { SpaceLogger.getLogger("SubscriptionState") }

/**
 * Subscription state model
 *
 * SECURITY NOTE: This is cached locally for UX optimization only.
 * NEVER trust this for access control - always verify with platform billing libraries.
 */
@Serializable
data class SubscriptionState(
    // Subscription status
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,

    // Subscription details
    val subscriptionId: String? = null,
    val productId: String? = null,
    val expiresAt: Long? = null, // Epoch milliseconds
    val purchasedAt: Long? = null, // Epoch milliseconds

    // Verification state
    val lastVerified: Long = 0L, // Epoch milliseconds
    val needsVerification: Boolean = false,
    val verificationError: String? = null,

    // Features
    val features: Set<PremiumFeature> = emptySet(),

    // UI state
    val isLoading: Boolean = false,
    val isCached: Boolean = false
) {
    
    /**
     * Check if subscription is expired based on expiresAt timestamp
     */
    fun isExpired(
        currentTimeMillis: Long = System.now().toEpochMilliseconds()
    ): Boolean {
        val expires = expiresAt ?: return false
        return currentTimeMillis > expires
    }

    /**
     * Check if verification is recent (within last hour)
     */
    fun isRecentlyVerified(
        currentTimeMillis: Long = System.now().toEpochMilliseconds()
    ): Boolean {
        val oneHourMillis = 60 * 60 * 1000L
        return (currentTimeMillis - lastVerified) < oneHourMillis
    }

    /**
     * Check if user has access to a specific feature
     * CRITICAL: Only use this for UI hints - always verify server-side before granting access
     */
    fun hasFeature(feature: PremiumFeature): Boolean {
        val hasAccess = isSubscribed && !isExpired() && features.contains(feature)
        log.d { "hasFeature(${feature.name}): isSubscribed=$isSubscribed, isExpired=${isExpired()}, features=$features, hasAccess=$hasAccess" }
        return hasAccess
    }

    companion object {
        val DEFAULT = SubscriptionState()

        /**
         * Create a free tier state
         */
        fun free() = SubscriptionState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            features = PremiumFeature.getFreeFeatures()
        )

        /**
         * Create an error state when verification fails
         */
        fun error(message: String) = SubscriptionState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            needsVerification = true,
            verificationError = message,
            features = PremiumFeature.getFreeFeatures()
        )
    }
}

/**
 * Subscription tiers
 */
@Serializable
enum class SubscriptionType(val isLegacy: Boolean = false) {
    FREE(isLegacy = false),
    LEGACY(isLegacy = true),  // Legacy subscriptions (ad-free + widget only)
    PREMIUM(isLegacy = false), // Current subscriptions (all features)
    LIFETIME(isLegacy = false); // Lifetime subscriptions (all features, no renewal)

    companion object {
        fun fromProductId(productId: String): SubscriptionType {
            val result = when {
                // Match lifetime product patterns first
                productId.contains("lifetime", ignoreCase = true) -> LIFETIME
                productId.contains("permanent", ignoreCase = true) -> LIFETIME
                productId.contains("onetime", ignoreCase = true) -> LIFETIME
                productId.contains("one_time", ignoreCase = true) -> LIFETIME
                // Match current/known premium product patterns
                productId == "spacelaunchnow_pro" -> PREMIUM
                productId == "sln_production_yearly" -> PREMIUM
                productId.contains("yearly", ignoreCase = true) -> PREMIUM
                productId.contains("monthly", ignoreCase = true) -> PREMIUM
                productId.contains("base-plan", ignoreCase = true) -> PREMIUM
                // Match "pro" as a word (not as part of "product", "profile", etc.)
                // Use regex to match "pro" with word boundaries or as suffix after underscore
                productId.matches(Regex(".*[_\\s]pro$", RegexOption.IGNORE_CASE)) -> PREMIUM
                productId.matches(Regex("^pro[_\\s].*", RegexOption.IGNORE_CASE)) -> PREMIUM
                productId.equals("pro", ignoreCase = true) -> PREMIUM
                // Everything else is legacy
                else -> LEGACY
            }
            log.d { "fromProductId: '$productId' -> $result" }
            return result
        }
    }
}

/**
 * Premium features that can be gated behind subscriptions
 */
@Serializable
enum class PremiumFeature {
    AD_FREE,
    CUSTOM_THEMES,
    CAL_SYNC,
    ADVANCED_WIDGETS,
    WIDGETS_CUSTOMIZATION,
    NOTIFICATION_CUSTOMIZATION;

    companion object {
        /**
         * Features available in the free tier
         */
        fun getFreeFeatures(): Set<PremiumFeature> {
            return emptySet() // Start with no features in free, add as needed
        }

        /**
         * Features available in basic subscription
         */
        fun getBasicFeatures(): Set<PremiumFeature> {
            return setOf(
                AD_FREE,
                ADVANCED_WIDGETS,
                WIDGETS_CUSTOMIZATION,
                CAL_SYNC,
                NOTIFICATION_CUSTOMIZATION
            )
        }

        /**
         * Features available in premium subscription (all features)
         */
        fun getPremiumFeatures(): Set<PremiumFeature> {
            return entries.toSet()
        }

        /**
         * Get features for a subscription type
         */
        fun getFeaturesForType(type: SubscriptionType): Set<PremiumFeature> {
            return when (type) {
                SubscriptionType.FREE -> getFreeFeatures()
                SubscriptionType.LEGACY -> getBasicFeatures()
                SubscriptionType.PREMIUM -> getPremiumFeatures()
                SubscriptionType.LIFETIME -> getPremiumFeatures() // Lifetime gets all features like Premium
            }
        }
    }
}

/**
 * Platform-specific purchase information
 * This is the raw data from Google Play / App Store
 */
data class PlatformPurchase(
    val purchaseToken: String,
    val productId: String,
    val purchaseTime: Long,
    val expiryTime: Long?,
    val isAcknowledged: Boolean,
    val orderId: String?,
    val platform: Platform
)

enum class Platform {
    GOOGLE_PLAY,
    APP_STORE,
    UNKNOWN
}
