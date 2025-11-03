package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.analytics.DatadogRUM
import me.calebjones.spacelaunchnow.data.config.RevenueCatConfig
import kotlin.coroutines.resume

/**
 * Manager class for RevenueCat operations
 */
class RevenueCatManager {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    private val _currentOffering = MutableStateFlow<Offering?>(null)
    val currentOffering: StateFlow<Offering?> = _currentOffering.asStateFlow()

    /**
     * Initialize RevenueCat with platform-specific configuration
     */
    suspend fun initialize(appUserId: String? = null) {
        try {
            // Skip initialization for unsupported platforms
            if (RevenueCatConfig.apiKey == "desktop_not_supported") {
                println("RevenueCat: Skipping initialization - platform not supported (${RevenueCatConfig.platform})")
                return
            }

            println("RevenueCat: Initializing for ${RevenueCatConfig.platform} with debug=${RevenueCatConfig.isDebug}")

            // Set log level based on debug mode
            Purchases.logLevel = if (RevenueCatConfig.isDebug) LogLevel.DEBUG else LogLevel.WARN

            // Configure Purchases
            Purchases.configure(apiKey = RevenueCatConfig.apiKey) {
                this.appUserId = appUserId
            }

            println("RevenueCat: Configuration complete")

            // Mark as initialized BEFORE making API calls
            _isInitialized.value = true
            println("RevenueCat: SDK initialized, fetching data...")

            // Load initial customer info
            refreshCustomerInfo()

            // Load offerings
            refreshOfferings()

            println("RevenueCat: Initialization successful")

        } catch (e: Exception) {
            println("RevenueCat: Failed to initialize - ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Refresh customer info from RevenueCat
     */
    suspend fun refreshCustomerInfo() {
        try {
            if (!_isInitialized.value) return

            Purchases.sharedInstance.getCustomerInfo(
                onError = { error ->
                    println("RevenueCat: Failed to get customer info - ${error.message}")
                    DatadogLogger.error(
                        "Failed to get RevenueCat customer info", null, mapOf(
                            "error_message" to (error.message ?: "unknown"),
                            "error_code" to error.code.name
                        )
                    )
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    println("RevenueCat: Customer info refreshed")
                    println("  - Active entitlements: ${customerInfo.entitlements.active.keys}")

                    // Set user info in Datadog for tracking
                    val userId = customerInfo.originalAppUserId
                    val activeEntitlements = customerInfo.entitlements.active.keys.joinToString(",")
                    val activeSubscriptions = customerInfo.activeSubscriptions.joinToString(",")

                    DatadogRUM.setUser(
                        id = userId,
                        extraInfo = mapOf(
                            "platform" to RevenueCatConfig.platform,
                            "active_entitlements" to activeEntitlements,
                            "active_subscriptions" to activeSubscriptions,
                            "has_premium" to customerInfo.entitlements.active.containsKey("premium"),
                            "first_seen" to customerInfo.firstSeen.toString()
                        )
                    )

                    DatadogLogger.info(
                        "Customer info refreshed", mapOf(
                            "user_id" to userId,
                            "active_entitlements" to activeEntitlements,
                            "active_subscriptions" to activeSubscriptions
                        )
                    )
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: Failed to refresh customer info - ${e.message}")
            DatadogLogger.error("Exception refreshing customer info", e)
        }
    }

    /**
     * Refresh offerings from RevenueCat
     */
    suspend fun refreshOfferings() {
        try {
            if (!_isInitialized.value) {
                println("RevenueCat: ❌ Cannot refresh offerings - SDK not initialized")
                return
            }

            println("RevenueCat: 🔄 Requesting offerings from API...")
            Purchases.sharedInstance.getOfferings(
                onError = { error ->
                    println("RevenueCat: ❌ Failed to get offerings")
                    println("  - Error code: ${error.code}")
                    println("  - Error message: ${error.message}")
                    println("  - Underlying error: ${error.underlyingErrorMessage}")
                    println("  - 💡 Tip: Check RevenueCat dashboard for offering configuration")
                },
                onSuccess = { offerings ->
                    println("RevenueCat: ✅ Offerings API response received")
                    println("  - Total offerings available: ${offerings.all.size}")
                    println("  - All offering IDs: ${offerings.all.keys.joinToString()}")
                    println("  - Current offering ID: ${offerings.current?.identifier ?: "❌ NONE"}")

                    if (offerings.current == null) {
                        println("  - ⚠️ WARNING: No 'current' offering is set!")
                        println("  - 💡 Fix: Go to RevenueCat dashboard → Offerings → Set one as 'Current'")
                    } else {
                        val pkgCount = offerings.current?.availablePackages?.size ?: 0
                        println("  - Available packages: $pkgCount")
                        offerings.current?.availablePackages?.forEach { pkg ->
                            println("    • Package: ${pkg.identifier}")
                            println("      Product: ${pkg.storeProduct.id}")
                            println("      Price: ${pkg.storeProduct.price.formatted}")
                        }
                    }

                    _currentOffering.value = offerings.current
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: ❌ Exception in refreshOfferings")
            println("  - ${e.message}")
            println("  - ${e.stackTraceToString()}")
        }
    }

    /**
     * Check if user has a specific entitlement
     */
    fun hasEntitlement(entitlementId: String): Boolean {
        return _customerInfo.value?.entitlements?.get(entitlementId)?.isActive == true
    }

    /**
     * Get all active entitlements
     */
    fun getActiveEntitlements(): Set<String> {
        return _customerInfo.value?.entitlements?.active?.keys ?: emptySet()
    }

    /**
     * Get all active product identifiers (including non-subscription purchases)
     * This includes legacy purchases that may not have entitlements configured
     */
    fun getActiveProductIdentifiers(): Set<String> {
        val customerInfo = _customerInfo.value

        if (customerInfo == null) {
            println("RevenueCat: ⚠️ Cannot get active products - no customer info available")
            DatadogLogger.warn("getActiveProductIdentifiers called with null customer info")
            return emptySet()
        }

        val productIds = mutableSetOf<String>()

        println("RevenueCat: === Getting All Active Product Identifiers ===")

        // Add all products from active entitlements
        println("  Checking active entitlements (${customerInfo.entitlements.active.size})...")
        customerInfo.entitlements.active.values.forEach { entitlementInfo ->
            entitlementInfo.productIdentifier?.let {
                productIds.add(it)
                println("    ✅ From entitlement '${entitlementInfo.identifier}': $it")
            }
        }

        // Add all non-subscription purchases (lifetime purchases)
        println("  Checking non-subscription transactions (${customerInfo.nonSubscriptionTransactions.size})...")
        customerInfo.nonSubscriptionTransactions.forEach { transaction ->
            productIds.add(transaction.productIdentifier)
            println("    ✅ From transaction: ${transaction.productIdentifier}")
        }

        // Add all active subscription product identifiers
        println("  Checking active subscriptions (${customerInfo.activeSubscriptions.size})...")
        customerInfo.activeSubscriptions.forEach { productId ->
            productIds.add(productId)
            println("    ✅ From subscription: $productId")
        }

        println("  === TOTAL: ${productIds.size} unique active products ===")
        if (productIds.isNotEmpty()) {
            println("  Products: ${productIds.joinToString(", ")}")
        } else {
            println("  ⚠️ WARNING: No active products found!")
            println("  This means:")
            println("    - No active entitlements")
            println("    - No non-subscription transactions (lifetime purchases)")
            println("    - No active subscriptions")
            println("  User may be on wrong store account or purchases not imported")
        }

        DatadogLogger.info(
            "Active product identifiers retrieved", mapOf(
                "product_count" to productIds.size,
                "products" to productIds.joinToString(","),
                "from_entitlements" to customerInfo.entitlements.active.values.mapNotNull { it.productIdentifier }.size,
                "from_non_sub_transactions" to customerInfo.nonSubscriptionTransactions.size,
                "from_active_subs" to customerInfo.activeSubscriptions.size,
                "user_id" to customerInfo.originalAppUserId
            )
        )

        return productIds
    }

    /**
     * Check if user has any active purchase (including legacy products)
     * This is useful for detecting legacy purchases that may not have entitlements configured
     */
    fun hasAnyActivePurchase(): Boolean {
        return getActiveProductIdentifiers().isNotEmpty()
    }

    /**
     * Restore purchases (useful for users who reinstalled the app)
     * Returns the CustomerInfo with active entitlements, or null on error
     */
    suspend fun restorePurchases(): CustomerInfo? = suspendCancellableCoroutine { continuation ->
        try {
            if (!_isInitialized.value) {
                println("RevenueCat: Cannot restore - not initialized")
                DatadogLogger.warn("Restore purchases called but SDK not initialized")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            println("RevenueCat: Starting restore purchases...")
            DatadogLogger.info(
                "Restore purchases started", mapOf(
                    "user_id" to (_customerInfo.value?.originalAppUserId ?: "unknown"),
                    "app_user_id_type" to if ((_customerInfo.value?.originalAppUserId
                            ?: "").startsWith("\$RCAnonymousID:")
                    ) "anonymous" else "identified"
                )
            )

            Purchases.sharedInstance.restorePurchases(
                onError = { error ->
                    println("RevenueCat: ❌ Restore purchases failed - ${error.message}")
                    println("  Error code: ${error.code}")
                    println("  Underlying error: ${error.underlyingErrorMessage}")

                    DatadogLogger.error(
                        "Restore purchases failed", null, mapOf(
                            "error_message" to error.message,
                            "error_code" to error.code.name,
                            "error_code_value" to error.code.code,
                            "underlying_error" to (error.underlyingErrorMessage ?: "none"),
                            "user_id" to (_customerInfo.value?.originalAppUserId ?: "unknown"),
                            "platform" to RevenueCatConfig.platform
                        )
                    )

                    continuation.resume(null)
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo

                    // === CRITICAL LOGGING FOR LEGACY SKU DEBUGGING ===
                    println("RevenueCat: ✅ Purchases restored successfully")
                    println("  App User ID: ${customerInfo.originalAppUserId}")
                    println("  Request Date: ${customerInfo.requestDate}")
                    println("  Original Purchase Date: ${customerInfo.originalPurchaseDate?.toString() ?: "none"}")
                    println("  First Seen: ${customerInfo.firstSeen}")
                    println("")
                    println("  === ENTITLEMENTS (${customerInfo.entitlements.all.size} total, ${customerInfo.entitlements.active.size} active) ===")
                    if (customerInfo.entitlements.active.isEmpty()) {
                        println("  ⚠️ WARNING: NO ACTIVE ENTITLEMENTS FOUND!")
                        println("  This means RevenueCat has not granted any entitlements.")
                        println("  Possible reasons:")
                        println("    1. Legacy product IDs not mapped to entitlements in RevenueCat dashboard")
                        println("    2. No purchases found in store account")
                        println("    3. Store account doesn't match the one used to purchase")
                    } else {
                        customerInfo.entitlements.active.forEach { (key, value) ->
                            println("    ✅ $key:")
                            println("       Product ID: ${value.productIdentifier ?: "unknown"}")
                            println("       Is Active: ${value.isActive}")
                            println("       Will Renew: ${value.willRenew}")
                            println("       Period Type: ${value.periodType.name}")
                            println("       Expires: ${value.expirationDate?.toString() ?: "never"}")
                            println("       Purchase Date: ${value.originalPurchaseDate?.toString() ?: "unknown"}")
                            println("       Store: ${value.store.name}")
                        }
                    }
                    println("")
                    println("  === ALL ENTITLEMENTS (including inactive) ===")
                    customerInfo.entitlements.all.forEach { (key, value) ->
                        println("    ${if (value.isActive) "✅" else "❌"} $key:")
                        println("       Product ID: ${value.productIdentifier ?: "unknown"}")
                        println("       Is Active: ${value.isActive}")
                    }
                    println("")
                    println("  === ACTIVE SUBSCRIPTIONS (${customerInfo.activeSubscriptions.size}) ===")
                    if (customerInfo.activeSubscriptions.isEmpty()) {
                        println("  ℹ️ No active subscriptions")
                    } else {
                        customerInfo.activeSubscriptions.forEach { productId ->
                            println("    • $productId")
                        }
                    }
                    println("")
                    println("  === NON-SUBSCRIPTION TRANSACTIONS (${customerInfo.nonSubscriptionTransactions.size}) ===")
                    if (customerInfo.nonSubscriptionTransactions.isEmpty()) {
                        println("  ⚠️ WARNING: NO NON-SUBSCRIPTION TRANSACTIONS FOUND!")
                        println("  This means no lifetime/one-time purchases were detected.")
                        println("  For users with legacy lifetime purchases (2018_founder, etc.), this should NOT be empty.")
                    } else {
                        customerInfo.nonSubscriptionTransactions.forEach { transaction ->
                            println("    • Product: ${transaction.productIdentifier}")
                            println("      Purchase Date: ${transaction.purchaseDate}")
                            println("      Transaction ID: ${transaction.transactionIdentifier}")
                        }
                    }
                    println("")
                    println("  === ALL PURCHASES (including inactive) ===")
                    val allPurchases = customerInfo.allPurchaseDates
                    if (allPurchases.isEmpty()) {
                        println("  ⚠️ CRITICAL: NO PURCHASES FOUND AT ALL!")
                        println("  This indicates:")
                        println("    1. User is signed into a different store account than the one used to purchase")
                        println("    2. Or purchases haven't been imported to RevenueCat")
                        println("  ACTION REQUIRED: Check RevenueCat dashboard for this user's purchase history")
                    } else {
                        allPurchases.forEach { (productId, purchaseDate) ->
                            println("    • $productId: $purchaseDate")
                        }
                    }

                    // Collect all unique product identifiers
                    val allProducts = mutableSetOf<String>()
                    customerInfo.entitlements.active.values.forEach { entitlementInfo ->
                        entitlementInfo.productIdentifier?.let { allProducts.add(it) }
                    }
                    customerInfo.nonSubscriptionTransactions.forEach { transaction ->
                        allProducts.add(transaction.productIdentifier)
                    }
                    customerInfo.activeSubscriptions.forEach { productId ->
                        allProducts.add(productId)
                    }

                    println("")
                    println("  === SUMMARY ===")
                    println("  Total unique products: ${allProducts.size}")
                    if (allProducts.isNotEmpty()) {
                        println("  Products: ${allProducts.joinToString(", ")}")
                    }
                    println("  Has entitlements: ${customerInfo.entitlements.active.isNotEmpty()}")
                    println("  Has active subs: ${customerInfo.activeSubscriptions.isNotEmpty()}")
                    println("  Has lifetime purchases: ${customerInfo.nonSubscriptionTransactions.isNotEmpty()}")
                    println("  ==============================")

                    // Detailed Datadog logging for debugging user issues
                    val entitlementDetails =
                        customerInfo.entitlements.active.entries.associate { (key, value) ->
                            key to mapOf(
                                "product_id" to value.productIdentifier,
                                "is_active" to value.isActive,
                                "will_renew" to value.willRenew,
                                "period_type" to value.periodType.name,
                                "expires_date" to (value.expirationDate?.toString() ?: "never"),
                                "original_purchase_date" to (value.originalPurchaseDate?.toString()
                                    ?: "unknown"),
                                "store" to value.store.name
                            )
                        }

                    val allEntitlementDetails =
                        customerInfo.entitlements.all.entries.associate { (key, value) ->
                            key to mapOf(
                                "product_id" to value.productIdentifier,
                                "is_active" to value.isActive
                            )
                        }

                    val nonSubTransactions =
                        customerInfo.nonSubscriptionTransactions.map { transaction ->
                            mapOf(
                                "product_id" to transaction.productIdentifier,
                                "purchase_date" to transaction.purchaseDate.toString(),
                                "store_transaction_id" to transaction.transactionIdentifier
                            )
                        }

                    val allPurchasesMap = customerInfo.allPurchaseDates.map { (productId, date) ->
                        mapOf(
                            "product_id" to productId,
                            "purchase_date" to date.toString()
                        )
                    }

                    DatadogLogger.info(
                        "Restore purchases successful - DETAILED", mapOf(
                            "user_id" to customerInfo.originalAppUserId,
                            "is_anonymous" to customerInfo.originalAppUserId.startsWith("\$RCAnonymousID:"),
                            "request_date" to customerInfo.requestDate.toString(),
                            "original_purchase_date" to (customerInfo.originalPurchaseDate?.toString()
                                ?: "none"),
                            "first_seen" to customerInfo.firstSeen.toString(),
                            "total_products_found" to allProducts.size,
                            "all_products" to allProducts.joinToString(","),
                            "active_entitlements_count" to customerInfo.entitlements.active.size,
                            "all_entitlements_count" to customerInfo.entitlements.all.size,
                            "active_entitlements" to customerInfo.entitlements.active.keys.joinToString(
                                ","
                            ),
                            "active_subscriptions_count" to customerInfo.activeSubscriptions.size,
                            "active_subscriptions" to customerInfo.activeSubscriptions.joinToString(
                                ","
                            ),
                            "non_subscription_count" to customerInfo.nonSubscriptionTransactions.size,
                            "all_purchases_count" to customerInfo.allPurchaseDates.size,
                            "has_any_entitlements" to customerInfo.entitlements.active.isNotEmpty(),
                            "has_non_sub_transactions" to customerInfo.nonSubscriptionTransactions.isNotEmpty(),
                            "has_any_purchases" to customerInfo.allPurchaseDates.isNotEmpty(),
                            "entitlement_details" to entitlementDetails.toString(),
                            "all_entitlement_details" to allEntitlementDetails.toString(),
                            "non_subscription_details" to nonSubTransactions.toString(),
                            "all_purchases_details" to allPurchasesMap.toString(),
                            "management_url" to (customerInfo.managementUrlString ?: "none"),
                            "platform" to RevenueCatConfig.platform
                        )
                    )

                    // Update Datadog user info with restored purchase data
                    DatadogRUM.setUser(
                        id = customerInfo.originalAppUserId,
                        extraInfo = mapOf(
                            "platform" to RevenueCatConfig.platform,
                            "active_entitlements" to customerInfo.entitlements.active.keys.joinToString(
                                ","
                            ),
                            "active_subscriptions" to customerInfo.activeSubscriptions.joinToString(
                                ","
                            ),
                            "total_products" to allProducts.size,
                            "has_premium" to customerInfo.entitlements.active.containsKey("premium"),
                            "has_legacy_purchase" to (customerInfo.nonSubscriptionTransactions.isNotEmpty())
                        )
                    )
                    continuation.resume(customerInfo)
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: ❌ Failed to restore purchases - ${e.message}")
            e.printStackTrace()

            DatadogLogger.error(
                "Exception during restore purchases", e, mapOf(
                    "user_id" to (_customerInfo.value?.originalAppUserId ?: "unknown")
                )
            )

            continuation.resume(null)
        }
    }
}