package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.ProductPricing
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Mock implementation of SubscriptionRepository for testing
 */
class MockSubscriptionRepository : SubscriptionRepository {
    
    private val _state = MutableStateFlow(SubscriptionState())
    override val state: StateFlow<SubscriptionState> = _state.asStateFlow()
    
    var mockSubscriptionState = SubscriptionState()
        set(value) {
            field = value
            _state.value = value
        }
    
    // Test tracking
    var initializeCalled = false
    var verifySubscriptionCalled = false
    var getProductPricingCalled = false
    var launchPurchaseFlowCalled = false
    var restorePurchasesCalled = false
    
    var lastForceRefresh = false
    var lastProductId: String? = null
    var lastBasePlanId: String? = null
    
    // Configurable behavior
    var shouldVerifyFail = false
    var shouldGetPricingFail = false
    var shouldLaunchPurchaseFail = false
    var shouldRestoreFail = false
    
    override suspend fun initialize() {
        initializeCalled = true
        _state.value = mockSubscriptionState
    }
    
    override suspend fun verifySubscription(forceRefresh: Boolean): Result<SubscriptionState> {
        verifySubscriptionCalled = true
        lastForceRefresh = forceRefresh
        
        return if (shouldVerifyFail) {
            Result.failure(Exception("Mock verification failed"))
        } else {
            _state.value = mockSubscriptionState
            Result.success(mockSubscriptionState)
        }
    }
    
    override suspend fun getProductPricing(productId: String): Result<List<ProductPricing>> {
        getProductPricingCalled = true
        lastProductId = productId
        
        return if (shouldGetPricingFail) {
            Result.failure(Exception("Mock pricing failed"))
        } else {
            Result.success(
                listOf(
                    ProductPricing(
                        productId = productId,
                        basePlanId = "base-plan",
                        formattedPrice = "$4.99",
                        priceCurrencyCode = "USD",
                        priceAmountMicros = 4990000L,
                        billingPeriod = "P1M",
                        title = "Test Product",
                        description = "Test Description"
                    )
                )
            )
        }
    }
    
    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<String> {
        launchPurchaseFlowCalled = true
        lastProductId = productId
        lastBasePlanId = basePlanId
        
        return if (shouldLaunchPurchaseFail) {
            Result.failure(Exception("Mock purchase failed"))
        } else {
            // Simulate successful purchase
            mockSubscriptionState = mockSubscriptionState.copy(
                isSubscribed = true,
                subscriptionType = SubscriptionType.PREMIUM
            )
            _state.value = mockSubscriptionState
            Result.success("mock_purchase_token")
        }
    }
    
    override suspend fun restorePurchases(): Result<SubscriptionState> {
        restorePurchasesCalled = true
        
        return if (shouldRestoreFail) {
            Result.failure(Exception("Mock restore failed"))
        } else {
            _state.value = mockSubscriptionState
            Result.success(mockSubscriptionState)
        }
    }
    
    override suspend fun hasFeature(feature: PremiumFeature): Boolean {
        return mockSubscriptionState.hasFeature(feature)
    }
    
    override suspend fun getAvailableFeatures(): Set<PremiumFeature> {
        return mockSubscriptionState.features
    }
    
    override suspend fun cancelSubscription(): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun clearSubscriptionCache() {
        // No-op for mock
    }
    
    override suspend fun forceRefreshWidgetAccess(): Boolean {
        return mockSubscriptionState.isSubscribed
    }
    
    // Test helper
    fun reset() {
        initializeCalled = false
        verifySubscriptionCalled = false
        getProductPricingCalled = false
        launchPurchaseFlowCalled = false
        restorePurchasesCalled = false
        lastForceRefresh = false
        lastProductId = null
        lastBasePlanId = null
        shouldVerifyFail = false
        shouldGetPricingFail = false
        shouldLaunchPurchaseFail = false
        shouldRestoreFail = false
        mockSubscriptionState = SubscriptionState()
        _state.value = mockSubscriptionState
    }
}
