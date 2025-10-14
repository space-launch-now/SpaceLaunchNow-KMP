package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                onError = { error -> println("RevenueCat: Failed to get customer info - ${error.message}") },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    println("RevenueCat: Customer info refreshed")
                    println("  - Active entitlements: ${customerInfo.entitlements.active.keys}")
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: Failed to refresh customer info - ${e.message}")
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
     * Restore purchases (useful for users who reinstalled the app)
     */
    suspend fun restorePurchases() {
        try {
            if (!_isInitialized.value) return

            Purchases.sharedInstance.restorePurchases(
                onError = { error ->
                    println("RevenueCat: Restore purchases failed - ${error.message}")
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    println("RevenueCat: Purchases restored successfully")
                }
            )

        } catch (e: Exception) {
            println("RevenueCat: Failed to restore purchases - ${e.message}")
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