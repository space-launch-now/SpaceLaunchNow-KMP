package me.calebjones.spacelaunchnow.data.billing

import android.content.Context
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.CustomerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.calebjones.spacelaunchnow.BuildConfig
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.analytics.DatadogRUM
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import kotlin.coroutines.resume

/**
 * Android implementation of BillingManager using RevenueCat
 *
 * This class handles all RevenueCat operations for Android platform:
 * - Initialization with Android API key
 * - Purchase flows
 * - Subscription state management
 * - Entitlement checking
 */
class AndroidBillingManager(
    private val context: Context
) : BillingManager {

    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _purchaseState = MutableStateFlow(PurchaseState())
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val purchases: Purchases
        get() = Purchases.sharedInstance

    override suspend fun initialize(appUserId: String?): Result<Unit> {
        return try {
            println("AndroidBillingManager: Initializing RevenueCat for Android (debug=${BuildConfig.IS_DEBUG})")

            // Configure RevenueCat
            Purchases.logLevel = if (BuildConfig.IS_DEBUG) LogLevel.DEBUG else LogLevel.WARN

            Purchases.configure(apiKey = BuildConfig.REVENUECAT_ANDROID_KEY) {
                this.appUserId = appUserId
            }

            println("AndroidBillingManager: RevenueCat configured")
            _isInitialized.value = true

            // Initial sync with store (silent, no UI)
            syncPurchases()

            // Load initial purchase state
            val success = refreshPurchaseState()
            if (success) {
                println("AndroidBillingManager: ✅ Initialization successful")
            } else {
                println("AndroidBillingManager: ⚠️  Initialization completed but failed to load customer info")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("AndroidBillingManager: ❌ Failed to initialize - ${e.message}")
            e.printStackTrace()
            
            DatadogLogger.error(
                "Failed to initialize AndroidBillingManager",
                e,
                mapOf(
                    "error_type" to (e::class.simpleName ?: "Unknown"),
                    "error_message" to (e.message ?: "No message"),
                    "error_cause" to (e.cause?.message ?: "No cause"),
                    "stack_trace" to (e.stackTraceToString().take(500)),
                    "is_debug" to BuildConfig.IS_DEBUG,
                    "api_key_configured" to BuildConfig.REVENUECAT_ANDROID_KEY.isNotEmpty(),
                    "app_user_id" to (appUserId ?: "null")
                )
            )
            Result.failure(e)
        }
    }

    override suspend fun refreshPurchaseState(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (!_isInitialized.value) {
                    println("AndroidBillingManager: ❌ Cannot refresh - not initialized")
                    continuation.resume(false)
                    return@suspendCancellableCoroutine
                }

                println("AndroidBillingManager: 🔄 Refreshing purchase state...")

                purchases.getCustomerInfo(
                    onError = { error ->
                        println("AndroidBillingManager: ❌ Failed to get customer info - ${error.message}")
                        DatadogLogger.error(
                            "Failed to get customer info",
                            null,
                            mapOf(
                                "error_message" to (error.message ?: "unknown"),
                                "error_code" to error.code.name
                            )
                        )
                        continuation.resume(false)
                    },
                    onSuccess = { customerInfo ->
                        updatePurchaseState(customerInfo)
                        println("AndroidBillingManager: ✅ Purchase state refreshed")
                        continuation.resume(true)
                    }
                )
            } catch (e: Exception) {
                println("AndroidBillingManager: ❌ Exception refreshing purchase state - ${e.message}")
                DatadogLogger.error("Exception refreshing purchase state", e)
                continuation.resume(false)
            }
        }
    }

    override suspend fun getAvailableProducts(): Result<List<ProductInfo>> {
        return try {
            if (!_isInitialized.value) {
                return Result.failure(IllegalStateException("BillingManager not initialized"))
            }

            println("AndroidBillingManager: 🔄 Loading available products...")
            val offerings = purchases.awaitOfferings()

            val products = offerings.current?.availablePackages?.map { pkg ->
                ProductInfo(
                    productId = pkg.storeProduct.id,
                    basePlanId = pkg.identifier,
                    title = pkg.storeProduct.title,
                    description = "${pkg.packageType} - ${pkg.storeProduct.period?.unit?.name ?: "One-time"}",
                    formattedPrice = pkg.storeProduct.price.formatted,
                    priceAmountMicros = pkg.storeProduct.price.amountMicros.toLong(),
                    currencyCode = pkg.storeProduct.price.currencyCode
                )
            } ?: emptyList()

            println("AndroidBillingManager: ✅ Found ${products.size} products")
            Result.success(products)
        } catch (e: Exception) {
            println("AndroidBillingManager: ❌ Failed to get products - ${e.message}")
            DatadogLogger.error("Failed to get available products", e)
            Result.failure(e)
        }
    }

    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        return try {
            if (!_isInitialized.value) {
                return Result.failure(IllegalStateException("BillingManager not initialized"))
            }

            println("AndroidBillingManager: 🛒 Starting purchase flow for $productId")

            val offerings = purchases.awaitOfferings()
            val pkg = offerings.current?.availablePackages?.find {
                it.storeProduct.id == productId &&
                        (basePlanId == null || it.identifier == basePlanId)
            } ?: return Result.failure(IllegalArgumentException("Product not found: $productId"))

            val result = purchases.awaitPurchase(pkg)
            updatePurchaseState(result.customerInfo)

            println("AndroidBillingManager: ✅ Purchase successful")
            DatadogLogger.info(
                "Purchase completed",
                mapOf(
                    "product_id" to productId,
                    "base_plan_id" to (basePlanId ?: "default")
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            println("AndroidBillingManager: ❌ Purchase failed - ${e.message}")
            DatadogLogger.error(
                "Purchase failed",
                e,
                mapOf("product_id" to productId)
            )
            Result.failure(e)
        }
    }

    override suspend fun restorePurchases(): Result<PurchaseState> {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (!_isInitialized.value) {
                    continuation.resume(Result.failure(IllegalStateException("BillingManager not initialized")))
                    return@suspendCancellableCoroutine
                }

                println("AndroidBillingManager: 🔄 Restoring purchases...")

                purchases.restorePurchases(
                    onError = { error ->
                        println("AndroidBillingManager: ❌ Restore failed - ${error.message}")
                        DatadogLogger.error(
                            "Restore purchases failed",
                            null,
                            mapOf("error" to error.message)
                        )
                        continuation.resume(Result.failure(Exception(error.message)))
                    },
                    onSuccess = { customerInfo ->
                        updatePurchaseState(customerInfo)
                        println("AndroidBillingManager: ✅ Purchases restored")
                        DatadogLogger.info("Purchases restored successfully")
                        continuation.resume(Result.success(_purchaseState.value))
                    }
                )
            } catch (e: Exception) {
                println("AndroidBillingManager: ❌ Exception restoring purchases - ${e.message}")
                DatadogLogger.error("Exception restoring purchases", e)
                continuation.resume(Result.failure(e))
            }
        }
    }

    override suspend fun syncPurchases() {
        suspendCancellableCoroutine { continuation ->
            try {
                if (!_isInitialized.value) {
                    continuation.resume(Unit)
                    return@suspendCancellableCoroutine
                }

                println("AndroidBillingManager: 🔄 Syncing purchases with store...")

                purchases.syncPurchases(
                    onError = { error ->
                        println("AndroidBillingManager: ⚠️  Sync failed - ${error.message}")
                        continuation.resume(Unit)
                    },
                    onSuccess = { customerInfo ->
                        updatePurchaseState(customerInfo)
                        println("AndroidBillingManager: ✅ Purchases synced")
                        continuation.resume(Unit)
                    }
                )
            } catch (e: Exception) {
                println("AndroidBillingManager: ⚠️  Exception syncing purchases - ${e.message}")
                continuation.resume(Unit)
            }
        }
    }

    override fun hasEntitlement(entitlementId: String): Boolean {
        return _purchaseState.value.activeEntitlements.contains(entitlementId)
    }

    override fun getActiveEntitlements(): Set<String> {
        return _purchaseState.value.activeEntitlements
    }

    /**
     * Update purchase state from RevenueCat CustomerInfo
     */
    private fun updatePurchaseState(customerInfo: CustomerInfo) {
        println("AndroidBillingManager: 📊 Updating purchase state...")

        val activeEntitlements = customerInfo.entitlements.active.keys

        // Collect all product IDs from various sources
        val productIds = buildSet {
            // From entitlements
            customerInfo.entitlements.active.values.forEach {
                it.productIdentifier?.let { productId -> add(productId) }
            }
            // From active subscriptions
            addAll(customerInfo.activeSubscriptions)
            // From non-subscription purchases
            customerInfo.nonSubscriptionTransactions.forEach {
                add(it.productIdentifier)
            }
        }

        val subscriptionType = determineSubscriptionType(activeEntitlements, productIds)
        val features = determineFeatures(subscriptionType)

        println("AndroidBillingManager: 📊 Purchase state:")
        println("  • Subscription Type: $subscriptionType")
        println("  • Active Entitlements: $activeEntitlements")
        println("  • Product IDs: $productIds")
        println("  • Features: ${features.size} features")

        _purchaseState.value = PurchaseState(
            isSubscribed = subscriptionType != SubscriptionType.FREE,
            subscriptionType = subscriptionType,
            activeEntitlements = activeEntitlements,
            activeProductIds = productIds,
            features = features,
            lastRefreshed = System.currentTimeMillis(),
            userId = customerInfo.originalAppUserId
        )

        // Update Datadog RUM with user subscription info
        DatadogRUM.setUser(
            id = customerInfo.originalAppUserId,
            extraInfo = mapOf(
                "platform" to "Android",
                "active_entitlements" to activeEntitlements.joinToString(","),
                "active_subscriptions" to customerInfo.activeSubscriptions.joinToString(","),
                "subscription_type" to subscriptionType.name,
                "has_premium" to (subscriptionType != SubscriptionType.FREE)
            )
        )
    }

    /**
     * Determine subscription type from entitlements and product IDs
     */
    private fun determineSubscriptionType(
        entitlements: Set<String>,
        productIds: Set<String>
    ): SubscriptionType {
        println("AndroidBillingManager: 🔍 Determining subscription type...")
        println(
            "AndroidBillingManager  📋 Input Entitlements: ${
                entitlements.joinToString(", ") { "\"$it\"" }.ifEmpty { "(none)" }
            }"
        )
        println(
            "AndroidBillingManager  📦 Input Product IDs: ${
                productIds.joinToString(", ") { "\"$it\"" }.ifEmpty { "(none)" }
            }"
        )

        // Check entitlements first (most reliable)
        val result = when {
            // Check for premium entitlement (all variations)
            entitlements.contains("premium") -> {
                println("AndroidBillingManager  ✅ Match: Found 'premium' entitlement → PREMIUM")
                SubscriptionType.PREMIUM
            }

            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_PRO) -> {
                println("AndroidBillingManager  ✅ Match: Found '${SubscriptionProducts.RC_ENTITLEMENT_PRO}' entitlement → PREMIUM")
                SubscriptionType.PREMIUM
            }

            // Check for lifetime/founder entitlements (all variations)
            entitlements.contains("founder") -> {
                println("AndroidBillingManager  ✅ Match: Found 'founder' entitlement → LIFETIME")
                SubscriptionType.LIFETIME
            }

            entitlements.contains("lifetime") -> {
                println("AndroidBillingManager  ✅ Match: Found 'lifetime' entitlement → LIFETIME")
                SubscriptionType.LIFETIME
            }

            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LIFETIME) -> {
                println("AndroidBillingManager  ✅ Match: Found '${SubscriptionProducts.RC_ENTITLEMENT_LIFETIME}' entitlement → LIFETIME")
                SubscriptionType.LIFETIME
            }

            // Check for legacy entitlement
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LEGACY) -> {
                println("AndroidBillingManager  ✅ Match: Found '${SubscriptionProducts.RC_ENTITLEMENT_LEGACY}' entitlement → LEGACY")
                SubscriptionType.LEGACY
            }

            // Fallback to product ID matching for legacy purchases
            productIds.any { it.contains("lifetime", ignoreCase = true) } -> {
                val matchedProduct = productIds.first { it.contains("lifetime", ignoreCase = true) }
                println("AndroidBillingManager  ✅ Match: Found lifetime product ID '$matchedProduct' → LIFETIME")
                SubscriptionType.LIFETIME
            }

            productIds.any {
                it == "spacelaunchnow_pro" ||
                        it.contains("yearly", ignoreCase = true) ||
                        it.contains("monthly", ignoreCase = true)
            } -> {
                val matchedProduct = productIds.first {
                    it == "spacelaunchnow_pro" ||
                            it.contains("yearly", ignoreCase = true) ||
                            it.contains("monthly", ignoreCase = true)
                }
                println("AndroidBillingManager  ✅ Match: Found premium product ID '$matchedProduct' → PREMIUM")
                SubscriptionType.PREMIUM
            }

            // Check for any active subscription or purchase (legacy)
            productIds.isNotEmpty() -> {
                println("AndroidBillingManager  ⚠️  Fallback: Unknown product IDs present → LEGACY")
                println("AndroidBillingManager      Products: ${productIds.joinToString(", ")}")
                SubscriptionType.LEGACY
            }

            else -> {
                println("AndroidBillingManager  ❌ No entitlements or products found → FREE")
                SubscriptionType.FREE
            }
        }

        println("AndroidBillingManager  🎯 Final Determination: $result")

        // Log to Datadog for monitoring
        DatadogLogger.info(
            "AndroidBillingManager - subscription type determined",
            mapOf(
                "subscription_type" to result.name,
                "entitlements_count" to entitlements.size,
                "product_ids_count" to productIds.size,
                "entitlements" to entitlements.joinToString(","),
                "product_ids" to productIds.joinToString(",")
            )
        )

        return result
    }

    /**
     * Determine available features based on subscription type
     */
    private fun determineFeatures(type: SubscriptionType): Set<PremiumFeature> {
        return PremiumFeature.getFeaturesForType(type)
    }
}

/**
 * Android-specific factory with context
 */
fun createBillingManager(context: Context): BillingManager {
    return AndroidBillingManager(context)
}
