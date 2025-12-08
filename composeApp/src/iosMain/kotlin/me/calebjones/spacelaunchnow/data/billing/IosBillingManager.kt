package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import me.calebjones.spacelaunchnow.util.logging.logger
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.util.AppSecrets
import kotlin.coroutines.resume

/**
 * iOS implementation of BillingManager using RevenueCat SDK
 */
class IosBillingManager : BillingManager {
    private val log = logger()
    
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _purchaseState = MutableStateFlow(PurchaseState())
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    private val purchases: Purchases
        get() = Purchases.sharedInstance
    
    override suspend fun initialize(appUserId: String?): Result<Unit> {
        return try {
            log.i { "🚀 Initializing IosBillingManager..." }
            
            // Configure RevenueCat with iOS API key
            Purchases.logLevel = LogLevel.DEBUG
            
            val apiKey = AppSecrets.revenueCatIosKey
            if (apiKey.isEmpty()) {
                log.e { "❌ RevenueCat iOS API key is empty!" }
                return Result.failure(IllegalStateException("RevenueCat iOS API key not configured"))
            }
            
            log.d { "🔑 Using API key: ${apiKey.take(15)}..." }
            
            Purchases.configure(apiKey = apiKey) {
                this.appUserId = appUserId
            }
            
            _isInitialized.value = true
            log.i { "✅ IosBillingManager initialized successfully" }
            
            // Initial sync
            syncPurchases()
            refreshPurchaseState()
            
            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "❌ IosBillingManager initialization failed" }
            Result.failure(e)
        }
    }
    
    override suspend fun refreshPurchaseState(): Boolean {
        return try {
            log.i { "🔄 Refreshing purchase state..." }
            val customerInfo = purchases.awaitCustomerInfo()
            updatePurchaseState(customerInfo)
            log.i { "✅ Purchase state refreshed" }
            true
        } catch (e: Exception) {
            log.e(e) { "❌ Failed to refresh purchase state" }
            false
        }
    }
    
    override suspend fun getAvailableProducts(): Result<List<ProductInfo>> {
        return try {
            log.i { "📦 Fetching available products..." }
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
            
            log.i { "✅ Found ${products.size} products" }
            Result.success(products)
        } catch (e: Exception) {
            log.e(e) { "❌ Failed to get products" }
            Result.failure(e)
        }
    }
    
    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        return try {
            log.i { "💳 Launching purchase flow for: $productId" }
            
            val offerings = purchases.awaitOfferings()
            val pkg = offerings.current?.availablePackages?.find { 
                it.storeProduct.id == productId && 
                (basePlanId == null || it.identifier == basePlanId)
            } ?: return Result.failure(IllegalArgumentException("Product not found: $productId"))
            
            val result = purchases.awaitPurchase(pkg)
            updatePurchaseState(result.customerInfo)
            
            log.i { "✅ Purchase completed successfully" }
            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "❌ Purchase failed" }
            Result.failure(e)
        }
    }
    
    override suspend fun restorePurchases(): Result<PurchaseState> {
        return suspendCancellableCoroutine { continuation ->
            log.i { "🔄 Restoring purchases..." }
            
            purchases.restorePurchases(
                onError = { error ->
                    log.e { "❌ Restore failed: ${error.message}" }
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { customerInfo ->
                    log.i { "✅ Purchases restored successfully" }
                    updatePurchaseState(customerInfo)
                    continuation.resume(Result.success(_purchaseState.value))
                }
            )
        }
    }
    
    override suspend fun syncPurchases() {
        suspendCancellableCoroutine { continuation ->
            log.i { "🔄 Syncing purchases..." }
            
            purchases.syncPurchases(
                onError = { 
                    log.w { "⚠️ Sync failed (non-critical)" }
                    continuation.resume(Unit) 
                },
                onSuccess = { customerInfo ->
                    log.i { "✅ Purchases synced" }
                    updatePurchaseState(customerInfo)
                    continuation.resume(Unit)
                }
            )
        }
    }
    
    override fun hasEntitlement(entitlementId: String): Boolean {
        val hasIt = _purchaseState.value.activeEntitlements.contains(entitlementId)
        log.d { "🔍 Has entitlement '$entitlementId': $hasIt" }
        return hasIt
    }
    
    override fun getActiveEntitlements(): Set<String> {
        val entitlements = _purchaseState.value.activeEntitlements
        log.d { "📋 Active entitlements: $entitlements" }
        return entitlements
    }
    
    private fun updatePurchaseState(customerInfo: com.revenuecat.purchases.kmp.models.CustomerInfo) {
        log.i { "📊 Updating purchase state..." }
        
        val activeEntitlements = customerInfo.entitlements.active.keys
        val productIds = buildSet {
            customerInfo.entitlements.active.values.forEach { 
                it.productIdentifier?.let { add(it) }
            }
            addAll(customerInfo.activeSubscriptions)
            customerInfo.nonSubscriptionTransactions.forEach { 
                add(it.productIdentifier) 
            }
        }
        
        val subscriptionType = determineSubscriptionType(activeEntitlements, productIds)
        val features = determineFeatures(subscriptionType)
        
        log.d { "📊 Purchase state: Type=$subscriptionType, Entitlements=$activeEntitlements, Products=$productIds, Features=${features.size}" }
        
        _purchaseState.value = PurchaseState(
            isSubscribed = subscriptionType != SubscriptionType.FREE,
            subscriptionType = subscriptionType,
            activeEntitlements = activeEntitlements,
            activeProductIds = productIds,
            features = features,
            lastRefreshed = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            userId = customerInfo.originalAppUserId
        )
    }
    
    private fun determineSubscriptionType(entitlements: Set<String>, productIds: Set<String>): SubscriptionType {
        // Check entitlements first (most reliable)
        return when {
            // Check for premium entitlement (all variations)
            entitlements.contains("premium") -> SubscriptionType.PREMIUM
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_PRO) -> SubscriptionType.PREMIUM
            
            // Check for lifetime/founder entitlements (all variations)
            entitlements.contains("founder") -> SubscriptionType.LIFETIME
            entitlements.contains("lifetime") -> SubscriptionType.LIFETIME
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LIFETIME) -> SubscriptionType.LIFETIME
            
            // Check for legacy entitlement
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LEGACY) -> SubscriptionType.LEGACY
            
            // Fallback to product ID matching for legacy purchases
            productIds.any { it.contains("lifetime", ignoreCase = true) } -> SubscriptionType.LIFETIME
            productIds.any { 
                it == "spacelaunchnow_pro" || 
                it.contains("yearly", ignoreCase = true) || 
                it.contains("monthly", ignoreCase = true)
            } -> SubscriptionType.PREMIUM
            
            // Check for any active subscription or purchase (legacy)
            productIds.isNotEmpty() -> SubscriptionType.LEGACY
            
            else -> SubscriptionType.FREE
        }
    }
    
    private fun determineFeatures(type: SubscriptionType): Set<PremiumFeature> {
        return PremiumFeature.getFeaturesForType(type)
    }
}
