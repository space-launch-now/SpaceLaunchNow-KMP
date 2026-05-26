package me.calebjones.spacelaunchnow.data.model

/**
 * Comprehensive purchase state from the billing system.
 * Platform-agnostic representation of subscription status from RevenueCat or other billing providers.
 *
 * This is different from SubscriptionState — PurchaseState represents raw billing data,
 * while SubscriptionState represents cached/processed subscription info for UI.
 */
data class PurchaseState(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val activeEntitlements: Set<String> = emptySet(),
    val activeProductIds: Set<String> = emptySet(),
    val lastRefreshed: Long = 0L,
    val isInTrialPeriod: Boolean = false,
    val trialExpiresAt: Long? = null,
    val subscriptionExpiryMs: Long? = null
) {
    val hasLoaded: Boolean get() = lastRefreshed != 0L
    val features: Set<PremiumFeature> get() = PremiumFeature.getFeaturesForType(subscriptionType)

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
