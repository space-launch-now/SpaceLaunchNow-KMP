package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.analytics.DatadogRUM
import me.calebjones.spacelaunchnow.data.config.RevenueCatConfig

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
                    DatadogLogger.error("Failed to get RevenueCat customer info", null, mapOf(
                        "error_message" to (error.message ?: "unknown"),
                        "error_code" to error.code.name
                    ))
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
                    
                    DatadogLogger.info("Customer info refreshed", mapOf(
                        "user_id" to userId,
                        "active_entitlements" to activeEntitlements,
                        "active_subscriptions" to activeSubscriptions
                    ))
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
        val customerInfo = _customerInfo.value ?: return emptySet()
        val productIds = mutableSetOf<String>()
        
        // Add all products from active entitlements
        customerInfo.entitlements.active.values.forEach { entitlementInfo ->
            entitlementInfo.productIdentifier?.let { productIds.add(it) }
        }
        
        // Add all non-subscription purchases (lifetime purchases)
        customerInfo.nonSubscriptionTransactions.forEach { transaction ->
            productIds.add(transaction.productIdentifier)
        }
        
        // Add all active subscription product identifiers
        customerInfo.activeSubscriptions.forEach { productId ->
            productIds.add(productId)
        }
        
        println("RevenueCat: Active product identifiers: $productIds")
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
     */
    suspend fun restorePurchases() {
        try {
            if (!_isInitialized.value) {
                println("RevenueCat: Cannot restore - not initialized")
                DatadogLogger.warn("Restore purchases called but SDK not initialized")
                return
            }

            println("RevenueCat: Starting restore purchases...")
            DatadogLogger.info("Restore purchases started", mapOf(
                "user_id" to (_customerInfo.value?.originalAppUserId ?: "unknown")
            ))
            
            Purchases.sharedInstance.restorePurchases(
                onError = { error ->
                    println("RevenueCat: ❌ Restore purchases failed - ${error.message}")
                    println("  Error code: ${error.code}")
                    println("  Underlying error: ${error.underlyingErrorMessage}")
                    
                    DatadogLogger.error("Restore purchases failed", null, mapOf(
                        "error_message" to (error.message ?: "unknown"),
                        "error_code" to error.code.name,
                        "underlying_error" to (error.underlyingErrorMessage ?: "none"),
                        "user_id" to (_customerInfo.value?.originalAppUserId ?: "unknown")
                    ))
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    println("RevenueCat: ✅ Purchases restored successfully")
                    println("  Active entitlements: ${customerInfo.entitlements.active.keys}")
                    println("  Active subscriptions: ${customerInfo.activeSubscriptions}")
                    println("  Non-subscription transactions: ${customerInfo.nonSubscriptionTransactions.map { it.productIdentifier }}")
                    
                    // Log all product identifiers found
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
                    println("  Total active products found: ${allProducts.size} - $allProducts")
                    
                    // Detailed Datadog logging for debugging user issues
                    val entitlementDetails = customerInfo.entitlements.active.entries.associate { (key, value) ->
                        key to mapOf(
                            "product_id" to (value.productIdentifier ?: "unknown"),
                            "is_active" to value.isActive,
                            "will_renew" to value.willRenew,
                            "period_type" to value.periodType.name,
                            "expires_date" to (value.expirationDate?.toString() ?: "never")
                        )
                    }
                    
                    val nonSubTransactions = customerInfo.nonSubscriptionTransactions.map { transaction ->
                        mapOf(
                            "product_id" to transaction.productIdentifier,
                            "purchase_date" to transaction.purchaseDate.toString(),
                            "store_transaction_id" to transaction.transactionIdentifier
                        )
                    }
                    
                    DatadogLogger.info("Restore purchases successful", mapOf(
                        "user_id" to customerInfo.originalAppUserId,
                        "total_products_found" to allProducts.size,
                        "all_products" to allProducts.joinToString(","),
                        "active_entitlements" to customerInfo.entitlements.active.keys.joinToString(","),
                        "active_subscriptions" to customerInfo.activeSubscriptions.joinToString(","),
                        "non_subscription_count" to customerInfo.nonSubscriptionTransactions.size,
                        "entitlement_details" to entitlementDetails.toString(),
                        "non_subscription_details" to nonSubTransactions.toString(),
                        "management_url" to (customerInfo.managementUrlString),
                        "original_purchase_date" to (customerInfo.originalPurchaseDate?.toString() ?: "unknown"),
                        "first_seen" to customerInfo.firstSeen.toString()
                    ))
                    
                    // Update Datadog user info with restored purchase data
                    DatadogRUM.setUser(
                        id = customerInfo.originalAppUserId,
                        extraInfo = mapOf(
                            "platform" to RevenueCatConfig.platform,
                            "active_entitlements" to customerInfo.entitlements.active.keys.joinToString(","),
                            "active_subscriptions" to customerInfo.activeSubscriptions.joinToString(","),
                            "total_products" to allProducts.size,
                            "has_premium" to customerInfo.entitlements.active.containsKey("premium"),
                            "has_legacy_purchase" to (customerInfo.nonSubscriptionTransactions.isNotEmpty())
                        )
                    )
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: ❌ Failed to restore purchases - ${e.message}")
            e.printStackTrace()
            
            DatadogLogger.error("Exception during restore purchases", e, mapOf(
                "user_id" to (_customerInfo.value?.originalAppUserId ?: "unknown")
            ))
        }
    }

    /**
     * Log in a user (useful when you have your own user authentication)
     */
    suspend fun loginUser(appUserId: String) {
        try {
            if (!_isInitialized.value) return

            Purchases.sharedInstance.logIn(
                appUserId,
                onError = { error ->
                    println("RevenueCat: Login failed - ${error.message}")
                },
                onSuccess = { customerInfo, _ ->
                    _customerInfo.value = customerInfo
                    println("RevenueCat: User logged in successfully")
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: Failed to login user - ${e.message}")
        }
    }

    /**
     * Log out the current user
     */
    suspend fun logoutUser() {
        try {
            if (!_isInitialized.value) return

            Purchases.sharedInstance.logOut(
                onError = { error ->
                    println("RevenueCat: Logout failed - ${error.message}")
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    println("RevenueCat: User logged out successfully")
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: Failed to logout user - ${e.message}")
        }
    }
}