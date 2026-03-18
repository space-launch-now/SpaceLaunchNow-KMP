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
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.coroutines.resume

/**
 * Manager class for RevenueCat operations
 */
class RevenueCatManager {

    private val log = logger()

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
                log.i { "Skipping initialization - platform not supported (${RevenueCatConfig.platform})" }
                return
            }

            log.i { "Initializing for ${RevenueCatConfig.platform} with debug=${RevenueCatConfig.isDebug}" }

            // Set log level based on debug mode
            Purchases.logLevel = if (RevenueCatConfig.isDebug) LogLevel.DEBUG else LogLevel.WARN

            // Configure Purchases
            Purchases.configure(apiKey = RevenueCatConfig.apiKey) {
                this.appUserId = appUserId
            }

            log.d { "Configuration complete" }

            // Mark as initialized BEFORE making API calls
            _isInitialized.value = true
            log.i { "SDK initialized, syncing purchases with store..." }

            // Sync purchases from the store at app start
            // This ensures we have the latest purchase state without showing restore UI
            syncPurchases()

            // Load initial customer info (which will include synced purchases)
            // Wait for this to complete to ensure entitlements are available immediately
            val customerInfoSuccess = refreshCustomerInfo()
            if (customerInfoSuccess) {
                log.i { "✅ Customer info loaded successfully" }
            } else {
                log.w { "⚠️  Customer info failed to load, but continuing..." }
            }

            // Load offerings (can run in parallel, don't wait)
            refreshOfferings()

            log.i { "Initialization successful" }

        } catch (e: Exception) {
            log.e(e) { "Failed to initialize - ${e.message}" }
        }
    }

    /**
     * Refresh customer info from RevenueCat (suspending)
     * This method properly waits for the RevenueCat API call to complete
     */
    suspend fun refreshCustomerInfo(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            if (!_isInitialized.value) {
                log.e { "❌ Cannot refresh customer info - SDK not initialized" }
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            log.d { "🔄 Refreshing customer info..." }

            Purchases.sharedInstance.getCustomerInfo(
                onError = { error ->
                    log.e { "Failed to get customer info - ${error.message}" }
                    DatadogLogger.error(
                        "Failed to get RevenueCat customer info", null, mapOf(
                            "error_message" to (error.message ?: "unknown"),
                            "error_code" to error.code.name
                        )
                    )
                    // Resume with false to indicate failure, but don't throw
                    continuation.resume(false)
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    log.i { "✅ Customer info refreshed successfully - Active entitlements: ${customerInfo.entitlements.active.keys}" }

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
                            "first_seen" to customerInfo.firstSeenMillis
                        )
                    )

                    DatadogLogger.info(
                        "Customer info refreshed", mapOf(
                            "user_id" to userId,
                            "active_entitlements" to activeEntitlements,
                            "active_subscriptions" to activeSubscriptions
                        )
                    )
                    
                    // Resume with true to indicate success
                    continuation.resume(true)
                }
            )

        } catch (e: Exception) {
            log.e(e) { "Failed to refresh customer info - ${e.message}" }
            DatadogLogger.error("Exception refreshing customer info", e)
            continuation.resume(false)
        }
    }

    /**
     * Refresh offerings from RevenueCat
     */
    suspend fun refreshOfferings() {
        try {
            if (!_isInitialized.value) {
                log.e { "❌ Cannot refresh offerings - SDK not initialized" }
                return
            }

            log.d { "🔄 Requesting offerings from API..." }
            Purchases.sharedInstance.getOfferings(
                onError = { error ->
                    log.e { "❌ Failed to get offerings - Code: ${error.code}, Message: ${error.message}, Underlying: ${error.underlyingErrorMessage}\n  💡 Tip: Check RevenueCat dashboard for offering configuration" }
                },
                onSuccess = { offerings ->
                    log.i { "✅ Offerings API response received - Total: ${offerings.all.size}, IDs: ${offerings.all.keys.joinToString()}, Current: ${offerings.current?.identifier ?: "❌ NONE"}" }

                    if (offerings.current == null) {
                        log.w { "⚠️ WARNING: No 'current' offering is set! Fix: Go to RevenueCat dashboard → Offerings → Set one as 'Current'" }
                    } else {
                        val pkgCount = offerings.current?.availablePackages?.size ?: 0
                        log.d { "Available packages: $pkgCount" }
                        offerings.current?.availablePackages?.forEach { pkg ->
                            log.d { "  • Package: ${pkg.identifier}, Product: ${pkg.storeProduct.id}, Price: ${pkg.storeProduct.price.formatted}" }
                        }
                    }

                    _currentOffering.value = offerings.current
                }
            )

        } catch (e: Exception) {
            log.e(e) { "❌ Exception in refreshOfferings - ${e.message}" }
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
            // Only log warning on null customer info - this is important
            log.w { "⚠️ Cannot get active products - no customer info available" }
            DatadogLogger.warn("getActiveProductIdentifiers called with null customer info")
            return emptySet()
        }

        val productIds = mutableSetOf<String>()

        // Collect products without verbose logging (this is called frequently from UI)
        customerInfo.entitlements.active.values.forEach { entitlementInfo ->
            entitlementInfo.productIdentifier?.let {
                productIds.add(it)
            }
        }

        customerInfo.nonSubscriptionTransactions.forEach { transaction ->
            productIds.add(transaction.productIdentifier)
        }

        customerInfo.activeSubscriptions.forEach { productId ->
            productIds.add(productId)
        }

        // Only log to Datadog (lightweight) - removed verbose console logging
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
     * Sync purchases from the store without triggering restore UI
     * This should be called at app start to ensure latest purchase state
     *
     * Unlike restorePurchases(), this doesn't trigger any user-facing restore flow
     * and silently syncs purchases in the background.
     */
    suspend fun syncPurchases() = suspendCancellableCoroutine { continuation ->
        try {
            if (!_isInitialized.value) {
                log.w { "Cannot sync - not initialized" }
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            log.i { "Syncing purchases from store..." }
            DatadogLogger.info("Syncing purchases at app start")

            Purchases.sharedInstance.syncPurchases(
                onError = { error ->
                    log.w { "⚠️ Sync purchases failed - ${error.message}" }
                    DatadogLogger.warn(
                        "Sync purchases failed", mapOf(
                            "error_message" to error.message,
                            "error_code" to error.code.name
                        )
                    )
                    // Don't fail initialization if sync fails - continue anyway
                    continuation.resume(Unit)
                },
                onSuccess = { customerInfo ->
                    log.i { "✅ Purchases synced successfully - Active entitlements: ${customerInfo.entitlements.active.keys.joinToString(", ")}" }

                    // Update cached customer info
                    _customerInfo.value = customerInfo

                    DatadogLogger.info(
                        "Purchases synced successfully", mapOf(
                            "user_id" to customerInfo.originalAppUserId,
                            "active_entitlements" to customerInfo.entitlements.active.keys.joinToString(
                                ","
                            ),
                            "all_purchases_count" to customerInfo.allPurchaseDateMillis.size
                        )
                    )

                    continuation.resume(Unit)
                }
            )
        } catch (e: Exception) {
            log.e(e) { "Exception during sync - ${e.message}" }
            DatadogLogger.error("Exception during syncPurchases", e)
            // Don't fail - continue initialization
            continuation.resume(Unit)
        }
    }

    /**
     * Restore purchases (useful for users who reinstalled the app)
     * Returns the CustomerInfo with active entitlements, or null on error
     */
    suspend fun restorePurchases(): CustomerInfo? = suspendCancellableCoroutine { continuation ->
        try {
            if (!_isInitialized.value) {
                log.w { "Cannot restore - not initialized" }
                DatadogLogger.warn("Restore purchases called but SDK not initialized")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            log.i { "Starting restore purchases..." }
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
                    log.e { "❌ Restore purchases failed - ${error.message}, Code: ${error.code}, Underlying: ${error.underlyingErrorMessage}" }

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
                    log.i { "✅ Purchases restored successfully - User: ${customerInfo.originalAppUserId}, Request Date: ${customerInfo.requestDateMillis}, Original Purchase: ${customerInfo.originalPurchaseDateMillis?.toString() ?: "none"}, First Seen: ${customerInfo.firstSeenMillis}" }
                    if (customerInfo.entitlements.active.isEmpty()) {
                        log.w { "⚠️ WARNING: NO ACTIVE ENTITLEMENTS FOUND (${customerInfo.entitlements.all.size} total)! Possible reasons: 1) Legacy product IDs not mapped to entitlements in RevenueCat dashboard, 2) No purchases found in store account, 3) Store account doesn't match the one used to purchase" }
                    } else {
                        customerInfo.entitlements.active.forEach { (key, value) ->
                            log.d { "  ✅ $key: Product=${value.productIdentifier ?: "unknown"}, Active=${value.isActive}, WillRenew=${value.willRenew}, Type=${value.periodType.name}, Expires=${value.expirationDateMillis?.toString() ?: "never"}, Purchased=${value.originalPurchaseDateMillis?.toString() ?: "unknown"}, Store=${value.store.name}" }
                        }
                    }
                    log.d { "All entitlements (including inactive): ${customerInfo.entitlements.all.entries.joinToString { (key, value) -> "${if (value.isActive) "✅" else "❌"} $key (${value.productIdentifier}, Active=${value.isActive})" }}" }
                    if (customerInfo.activeSubscriptions.isEmpty()) {
                        log.d { "Active subscriptions: None" }
                    } else {
                        log.d { "Active subscriptions (${customerInfo.activeSubscriptions.size}): ${customerInfo.activeSubscriptions.joinToString()}" }
                    }
                    if (customerInfo.nonSubscriptionTransactions.isEmpty()) {
                        log.w { "⚠️ WARNING: NO NON-SUBSCRIPTION TRANSACTIONS FOUND! No lifetime/one-time purchases detected. For users with legacy lifetime purchases (2018_founder, etc.), this should NOT be empty." }
                    } else {
                        log.d { "Non-subscription transactions (${customerInfo.nonSubscriptionTransactions.size}): ${customerInfo.nonSubscriptionTransactions.joinToString { "${it.productIdentifier} (${it.purchaseDateMillis}, ID: ${it.transactionIdentifier})" }}" }
                    }
                    val allPurchases = customerInfo.allPurchaseDateMillis
                    if (allPurchases.isEmpty()) {
                        log.e { "⚠️ CRITICAL: NO PURCHASES FOUND AT ALL! This indicates: 1) User is signed into a different store account than the one used to purchase, 2) Or purchases haven't been imported to RevenueCat. ACTION REQUIRED: Check RevenueCat dashboard for this user's purchase history" }
                    } else {
                        log.d { "All purchases (${allPurchases.size}): ${allPurchases.entries.joinToString { "${it.key}: ${it.value}" }}" }
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

                    log.i { "SUMMARY - Total unique products: ${allProducts.size}${if (allProducts.isNotEmpty()) ", Products: ${allProducts.joinToString(", ")}" else ""}, Has entitlements: ${customerInfo.entitlements.active.isNotEmpty()}, Has active subs: ${customerInfo.activeSubscriptions.isNotEmpty()}, Has lifetime purchases: ${customerInfo.nonSubscriptionTransactions.isNotEmpty()}" }

                    // Detailed Datadog logging for debugging user issues
                    val entitlementDetails =
                        customerInfo.entitlements.active.entries.associate { (key, value) ->
                            key to mapOf(
                                "product_id" to value.productIdentifier,
                                "is_active" to value.isActive,
                                "will_renew" to value.willRenew,
                                "period_type" to value.periodType.name,
                                "expires_date" to (value.expirationDateMillis?.toString()
                                    ?: "never"),
                                "original_purchase_date" to (value.originalPurchaseDateMillis?.toString()
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
                                "purchase_date" to transaction.purchaseDateMillis.toString(),
                                "store_transaction_id" to transaction.transactionIdentifier
                            )
                        }

                    val allPurchasesMap =
                        customerInfo.allPurchaseDateMillis.map { (productId, date) ->
                            mapOf(
                                "product_id" to productId,
                                "purchase_date" to date.toString()
                            )
                        }

                    DatadogLogger.info(
                        "Restore purchases successful - DETAILED", mapOf(
                            "user_id" to customerInfo.originalAppUserId,
                            "is_anonymous" to customerInfo.originalAppUserId.startsWith("\$RCAnonymousID:"),
                            "request_date" to customerInfo.requestDateMillis.toString(),
                            "original_purchase_date" to (customerInfo.originalPurchaseDateMillis?.toString()
                                ?: "none"),
                            "first_seen" to customerInfo.firstSeenMillis.toString(),
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
                            "all_purchases_count" to customerInfo.allPurchaseDateMillis.size,
                            "has_any_entitlements" to customerInfo.entitlements.active.isNotEmpty(),
                            "has_non_sub_transactions" to customerInfo.nonSubscriptionTransactions.isNotEmpty(),
                            "has_any_purchases" to customerInfo.allPurchaseDateMillis.isNotEmpty(),
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
            log.e(e) { "❌ Failed to restore purchases - ${e.message}" }

            DatadogLogger.error(
                "Exception during restore purchases", e, mapOf(
                    "user_id" to (_customerInfo.value?.originalAppUserId ?: "unknown")
                )
            )

            continuation.resume(null)
        }
    }
}