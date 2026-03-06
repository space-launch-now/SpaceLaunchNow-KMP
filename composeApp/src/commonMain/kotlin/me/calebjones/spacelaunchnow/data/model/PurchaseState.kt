package me.calebjones.spacelaunchnow.data.model

/**
 * Comprehensive purchase state from billing system
 * Platform-agnostic representation of subscription status from RevenueCat or other billing providers
 * 
 * This is different from SubscriptionState - PurchaseState represents raw billing data,
 * while SubscriptionState represents cached/processed subscription info for UI.
 */
data class PurchaseState(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val activeEntitlements: Set<String> = emptySet(),
    val activeProductIds: Set<String> = emptySet(),
    val features: Set<PremiumFeature> = emptySet(),
    val lastRefreshed: Long = 0L,
    val userId: String? = null,
    val isInTrialPeriod: Boolean = false,
    val trialExpiresAt: Long? = null,
    val subscriptionExpiryMs: Long? = null // Unix ms when subscription expires; null for lifetime
) {
    /**
     * Convert to SubscriptionState for UI/caching
     */
    fun toSubscriptionState(): SubscriptionState {
        return SubscriptionState(
            isSubscribed = isSubscribed,
            subscriptionType = subscriptionType,
            productId = activeProductIds.firstOrNull(),
            lastVerified = lastRefreshed,
            features = features,
            needsVerification = false,
            isInTrialPeriod = isInTrialPeriod,
            trialExpiresAt = trialExpiresAt
        )
    }
}
