package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Mock implementation of BillingManager for testing
 * Allows full control over billing state and simulates various scenarios
 */
class MockBillingManager : BillingManager {
    
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _purchaseState = MutableStateFlow(PurchaseState())
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    // Test state tracking
    var initializeCalled = false
    var refreshCalled = false
    var getProductsCalled = false
    var launchPurchaseFlowCalled = false
    var restorePurchasesCalled = false
    var syncPurchasesCalled = false
    
    var lastProductIdPurchased: String? = null
    var lastBasePlanIdPurchased: String? = null
    var lastAppUserId: String? = null
    
    // Configurable behavior
    var shouldInitializeFail = false
    var shouldRefreshFail = false
    var shouldGetProductsFail = false
    var shouldLaunchPurchaseFail = false
    var shouldRestorePurchasesFail = false
    
    var mockProducts = listOf(
        ProductInfo(
            productId = "test_monthly",
            basePlanId = "monthly-base",
            title = "Monthly Subscription",
            description = "MONTHLY - Month",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "USD"
        ),
        ProductInfo(
            productId = "test_yearly",
            basePlanId = "yearly-base",
            title = "Yearly Subscription",
            description = "ANNUAL - Year",
            formattedPrice = "$39.99",
            priceAmountMicros = 39990000L,
            currencyCode = "USD"
        )
    )
    
    override suspend fun initialize(appUserId: String?): Result<Unit> {
        initializeCalled = true
        lastAppUserId = appUserId
        
        return if (shouldInitializeFail) {
            Result.failure(Exception("Mock initialization failed"))
        } else {
            _isInitialized.value = true
            Result.success(Unit)
        }
    }
    
    override suspend fun refreshPurchaseState(): Boolean {
        refreshCalled = true
        return !shouldRefreshFail
    }
    
    override suspend fun getAvailableProducts(): Result<List<ProductInfo>> {
        getProductsCalled = true
        
        return if (shouldGetProductsFail) {
            Result.failure(Exception("Mock get products failed"))
        } else {
            Result.success(mockProducts)
        }
    }
    
    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        launchPurchaseFlowCalled = true
        lastProductIdPurchased = productId
        lastBasePlanIdPurchased = basePlanId
        
        return if (shouldLaunchPurchaseFail) {
            Result.failure(Exception("Mock purchase failed"))
        } else {
            // Simulate successful purchase
            _purchaseState.value = PurchaseState(
                isSubscribed = true,
                subscriptionType = SubscriptionType.PREMIUM,
                activeEntitlements = setOf("premium"),
                activeProductIds = setOf(productId),
                features = PremiumFeature.getPremiumFeatures(),
                lastRefreshed = System.currentTimeMillis(),
                userId = lastAppUserId
            )
            Result.success(Unit)
        }
    }
    
    override suspend fun restorePurchases(): Result<PurchaseState> {
        restorePurchasesCalled = true
        
        return if (shouldRestorePurchasesFail) {
            Result.failure(Exception("Mock restore failed"))
        } else {
            Result.success(_purchaseState.value)
        }
    }
    
    override fun hasEntitlement(entitlementId: String): Boolean {
        return _purchaseState.value.activeEntitlements.contains(entitlementId)
    }
    
    override fun getActiveEntitlements(): Set<String> {
        return _purchaseState.value.activeEntitlements
    }
    
    override suspend fun syncPurchases() {
        syncPurchasesCalled = true
    }
    
    // Test helper methods
    fun reset() {
        initializeCalled = false
        refreshCalled = false
        getProductsCalled = false
        launchPurchaseFlowCalled = false
        restorePurchasesCalled = false
        syncPurchasesCalled = false
        lastProductIdPurchased = null
        lastBasePlanIdPurchased = null
        lastAppUserId = null
        shouldInitializeFail = false
        shouldRefreshFail = false
        shouldGetProductsFail = false
        shouldLaunchPurchaseFail = false
        shouldRestorePurchasesFail = false
        _isInitialized.value = false
        _purchaseState.value = PurchaseState()
    }
    
    fun setSubscribed(
        type: SubscriptionType = SubscriptionType.PREMIUM,
        productId: String = "test_product",
        entitlements: Set<String> = setOf("premium")
    ) {
        _purchaseState.value = PurchaseState(
            isSubscribed = true,
            subscriptionType = type,
            activeEntitlements = entitlements,
            activeProductIds = setOf(productId),
            features = PremiumFeature.getFeaturesForType(type),
            lastRefreshed = System.currentTimeMillis()
        )
    }
    
    fun setFree() {
        _purchaseState.value = PurchaseState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            activeEntitlements = emptySet(),
            activeProductIds = emptySet(),
            features = emptySet(),
            lastRefreshed = System.currentTimeMillis()
        )
    }
}
