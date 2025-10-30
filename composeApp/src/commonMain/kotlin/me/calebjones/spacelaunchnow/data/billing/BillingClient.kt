package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.Flow
import me.calebjones.spacelaunchnow.data.model.PlatformPurchase
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Platform-agnostic billing interface
 *
 * Now uses RevenueCat for unified billing across all platforms
 *
 * CRITICAL SECURITY:
 * - This is the ONLY source of truth for subscription status
 * - Never rely on cached values for access control
 * - Always verify purchases server-side for sensitive operations
 */
class BillingClient(
    private val revenueCatClient: RevenueCatBillingClient
) {

    /**
     * Initialize the billing client
     * Must be called before any other operations
     */
    suspend fun initialize(): Result<Unit> = revenueCatClient.initialize()

    /**
     * Query current active purchases
     * This directly queries RevenueCat which syncs with Google Play or App Store
     *
     * @return List of active purchases from platform
     */
    suspend fun queryPurchases(): Result<List<PlatformPurchase>> = revenueCatClient.queryPurchases()

    /**
     * Start purchase flow for a subscription
     *
     * @param productId The product ID to purchase
     * @param basePlanId The base plan ID (e.g., "base-plan" for monthly, "yearly" for yearly)
     * @return Result with purchase token on success
     */
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String? = null): Result<String> = 
        revenueCatClient.launchPurchaseFlow(productId, basePlanId)

    /**
     * Acknowledge a purchase (handled automatically by RevenueCat)
     *
     * @param purchaseToken The purchase token to acknowledge
     */
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> = 
        revenueCatClient.acknowledgePurchase(purchaseToken)

    /**
     * Get available products for purchase
     *
     * @return List of product IDs that can be purchased
     */
    suspend fun getAvailableProducts(): Result<List<String>> = revenueCatClient.getAvailableProducts()

    /**
     * Get product pricing details
     *
     * @param productId The product ID to query
     * @return List of pricing information for all base plans of this product
     */
    suspend fun getProductPricing(productId: String): Result<List<me.calebjones.spacelaunchnow.data.model.ProductPricing>> = 
        revenueCatClient.getProductPricing(productId)

    /**
     * Flow of purchase updates
     * Emits whenever a purchase is completed or updated
     */
    val purchaseUpdates: Flow<PlatformPurchase> = revenueCatClient.purchaseUpdates

    /**
     * Disconnect and cleanup resources
     */
    fun disconnect() = revenueCatClient.disconnect()
}

/**
 * Product IDs for subscriptions and one-time purchases
 * These must match the product IDs configured in Google Play Console and App Store Connect
 *
 * Google Play Console Configuration:
 * - Subscription ID: sln_production_yearly
 *   - Base Plans: base-plan (monthly), yearly (yearly)
 * - One-Time Purchase ID: spacelaunchnow_pro
 */
object SubscriptionProducts {
    // Google Play subscription product ID (single product with multiple base plans)
    const val PRODUCT_ID = "sln_production_yearly"

    // One-time purchase product ID (buy once, own forever)
    const val PRO_LIFETIME = "spacelaunchnow_pro"

    // Base plan IDs (configured in Google Play Console)
    const val BASE_PLAN_MONTHLY = "base-plan"  // Monthly subscription
    const val BASE_PLAN_YEARLY = "yearly"      // Yearly subscription (better value)

    // Legacy/Founder SKUs (for migration and compatibility)
    const val FOUNDER_2018 = "2018_founder"     // 2018 Founder lifetime purchase

    // RevenueCat Package Identifiers (from offering configured in dashboard)
    // These are the identifiers used in RevenueCat's Offering.availablePackages
    const val RC_PACKAGE_LIFETIME = "\$rc_lifetime"  // Maps to spacelaunchnow_pro
    const val RC_PACKAGE_MONTHLY = "\$rc_monthly"    // Maps to sln_production_yearly:base-plan
    const val RC_PACKAGE_ANNUAL = "\$rc_annual"      // Maps to sln_production_yearly:yearly

    // RevenueCat entitlement identifier (configured in dashboard)
    const val RC_ENTITLEMENT_PREMIUM = "premium"     // All features unlocked

    // NOTE: We don't explicitly list all legacy products here as there are hundreds of them.
    // Any product ID that doesn't match current products or debug products is treated as legacy.
    // Legacy products get ad-free access only (BASIC subscription type).
    const val DEBUG_EXPIRED = "expired_premium" // Debug/test product for expired state

    /**
     * All available subscription products
     * For Google Play Billing Library 6.x, we use the product ID
     */
    val ALL_SUBSCRIPTION_PRODUCTS = listOf(
        PRODUCT_ID,
        BASE_PLAN_MONTHLY,
        BASE_PLAN_YEARLY
    )

    /**
     * All available one-time purchase products
     */
    val ALL_INAPP_PRODUCTS = listOf(
        PRO_LIFETIME
    )

    /**
     * Known debug/test products
     */
    val DEBUG_PRODUCTS = listOf(
        DEBUG_EXPIRED
    )

    /**
     * All products combined (current products only)
     */
    val ALL_PRODUCTS = ALL_SUBSCRIPTION_PRODUCTS + ALL_INAPP_PRODUCTS

    /**
     * Map product ID to subscription type
     *
     * Logic:
     * - Current products (PRODUCT_ID, PRO_LIFETIME) -> PREMIUM (all features)
     * - Debug products -> FREE (no features, for testing)
     * - Everything else -> LEGACY (legacy products, ad-free only)
     */
    fun getSubscriptionType(productId: String): SubscriptionType {
        return SubscriptionType.fromProductId(productId)
    }

    /**
     * Check if product grants ad-free access
     *
     * Logic:
     * - Current products -> true
     * - Debug products -> false (for testing expired/free states)
     * - Everything else -> true (legacy products get ad-free)
     */
    fun grantsAdFreeAccess(productId: String): Boolean {
        return when {
            productId in ALL_PRODUCTS -> true
            productId.startsWith("debug_") || productId in DEBUG_PRODUCTS -> false
            else -> true // All unrecognized products are legacy and get ad-free
        }
    }

    /**
     * Check if product grants full premium access (only current products)
     */
    fun grantsFullPremiumAccess(productId: String): Boolean {
        return productId in ALL_PRODUCTS
    }

    /**
     * Get features available for a specific product ID
     *
     * Logic:
     * - Current products -> all premium features
     * - Debug products -> no features (for testing)
     * - Everything else -> ad-free only (legacy products)
     */
    fun getFeaturesForProduct(productId: String): Set<PremiumFeature> {
        return when {
            productId in ALL_PRODUCTS -> {
                // Current products get all premium features
                PremiumFeature.getPremiumFeatures()
            }

            productId.startsWith("debug_") || productId in DEBUG_PRODUCTS -> {
                // Debug products get no features (for testing)
                emptySet()
            }

            else -> {
                // All unrecognized products are legacy (ad-free only)
                PremiumFeature.getBasicFeatures()
            }
        }
    }

    /**
     * Check if product ID is a one-time purchase
     */
    fun isOneTimePurchase(productId: String): Boolean {
        return productId == PRO_LIFETIME
    }

    /**
     * Check if a base plan is yearly
     */
    fun isYearly(basePlanId: String): Boolean {
        return basePlanId == BASE_PLAN_YEARLY
    }

    /**
     * Check if product ID is a legacy SKU
     * Any product that's not current or debug is considered legacy
     */
    fun isLegacyProduct(productId: String): Boolean {
        return productId !in ALL_PRODUCTS &&
                !productId.startsWith("debug_") &&
                productId !in DEBUG_PRODUCTS
    }

    /**
     * Check if product ID is unknown/unrecognized (same as legacy)
     */
    fun isUnknownProduct(productId: String): Boolean {
        return isLegacyProduct(productId)
    }

    /**
     * Get user-friendly display name for any product ID
     */
    fun getProductDisplayName(productId: String): String {
        return when (productId) {
            PRODUCT_ID -> "Space Launch Now Premium"
            PRO_LIFETIME -> "Space Launch Now Pro (Lifetime)"
            DEBUG_EXPIRED -> "Premium (Expired)"
            else -> {
                when {
                    productId.startsWith("debug_") -> "Debug Product"
                    else -> "Legacy Premium" // All unrecognized products are legacy
                }
            }
        }
    }

    /**
     * Check if product should be treated as premium (including legacy)
     * Only debug products are considered non-premium
     */
    fun grantsPremiumAccess(productId: String): Boolean {
        return getSubscriptionType(productId) != SubscriptionType.FREE
    }
}


