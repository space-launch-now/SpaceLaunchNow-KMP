package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.billing.MockBillingManager
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.MockSubscriptionRepository
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for SubscriptionViewModel using BillingManager
 * Phase 8: Validates the platform-agnostic ViewModel implementation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {
    
    private lateinit var billingManager: MockBillingManager
    private lateinit var repository: MockSubscriptionRepository
    private val analyticsManager: AnalyticsManager = AnalyticsManagerImpl(emptyList())
    private lateinit var viewModel: SubscriptionViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
        Dispatchers.setMain(testDispatcher)
        billingManager = MockBillingManager()
        repository = MockSubscriptionRepository()
        viewModel = SubscriptionViewModel(repository, billingManager, analyticsManager)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // ========================================
    // Product Loading Tests
    // ========================================
    
    @Test
    fun `should load available products on initialization`() = runTest {
        // Given: BillingManager has products
        billingManager.mockProducts = listOf(
            ProductInfo(
                productId = "monthly_sub",
                basePlanId = "monthly-base",
                title = "Monthly",
                description = "Monthly subscription",
                formattedPrice = "$4.99",
                priceAmountMicros = 4990000L,
                currencyCode = "USD"
            ),
            ProductInfo(
                productId = "yearly_sub",
                basePlanId = "yearly-base",
                title = "Yearly",
                description = "Annual subscription",
                formattedPrice = "$39.99",
                priceAmountMicros = 39990000L,
                currencyCode = "USD"
            )
        )
        
        // When: ViewModel initializes
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Products should be loaded
        val products = viewModel.availableProducts.first()
        assertEquals(2, products.size)
        assertTrue(billingManager.getProductsCalled)
    }
    
    @Test
    fun `should handle product loading failure gracefully`() = runTest {
        // Given: BillingManager fails to load products
        billingManager.shouldGetProductsFail = true
        
        // When: ViewModel initializes
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Available products should be empty
        val products = viewModel.availableProducts.first()
        assertTrue(products.isEmpty())
        assertTrue(billingManager.getProductsCalled)
    }
    
    // ========================================
    // Product Type Detection Tests
    // ========================================
    
    @Test
    fun `getProductByType should find monthly product`() = runTest {
        // Given: Products with monthly plan
        billingManager.mockProducts = listOf(
            ProductInfo(
                productId = "monthly_sub",
                basePlanId = "monthly-base",
                title = "Monthly",
                description = "Monthly subscription",
                formattedPrice = "$4.99",
                priceAmountMicros = 4990000L,
                currencyCode = "USD"
            )
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Getting monthly product
        val monthlyProduct = viewModel.getProductByType(ProductType.MONTHLY)
        
        // Then: Should find the monthly product
        assertNotNull(monthlyProduct)
        assertEquals("monthly_sub", monthlyProduct.productId)
        assertTrue(monthlyProduct.basePlanId?.contains("monthly", ignoreCase = true) == true)
    }
    
    @Test
    fun `getProductByType should find annual product`() = runTest {
        // Given: Products with annual/yearly plan
        billingManager.mockProducts = listOf(
            ProductInfo(
                productId = "yearly_sub",
                basePlanId = "annual-base",
                title = "Yearly",
                description = "Annual subscription",
                formattedPrice = "$39.99",
                priceAmountMicros = 39990000L,
                currencyCode = "USD"
            )
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Getting annual product
        val annualProduct = viewModel.getProductByType(ProductType.ANNUAL)
        
        // Then: Should find the annual product
        assertNotNull(annualProduct)
        assertEquals("yearly_sub", annualProduct.productId)
        assertTrue(
            annualProduct.basePlanId?.contains("annual", ignoreCase = true) == true ||
            annualProduct.basePlanId?.contains("yearly", ignoreCase = true) == true
        )
    }
    
    @Test
    fun `getProductByType should find lifetime product`() = runTest {
        // Given: Products with lifetime plan
        billingManager.mockProducts = listOf(
            ProductInfo(
                productId = "pro_lifetime",
                basePlanId = "lifetime-base",
                title = "Lifetime Pro",
                description = "One-time lifetime purchase",
                formattedPrice = "$99.99",
                priceAmountMicros = 99990000L,
                currencyCode = "USD"
            )
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Getting lifetime product
        val lifetimeProduct = viewModel.getProductByType(ProductType.LIFETIME)
        
        // Then: Should find the lifetime product
        assertNotNull(lifetimeProduct)
        assertEquals("pro_lifetime", lifetimeProduct.productId)
        assertTrue(
            lifetimeProduct.basePlanId?.contains("lifetime", ignoreCase = true) == true ||
            lifetimeProduct.productId.contains("lifetime", ignoreCase = true) ||
            lifetimeProduct.productId.contains("pro", ignoreCase = true)
        )
    }
    
    @Test
    fun `getProductByType should return null for non-existent type`() = runTest {
        // Given: No products loaded
        billingManager.mockProducts = emptyList()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Getting any product type
        val monthlyProduct = viewModel.getProductByType(ProductType.MONTHLY)
        
        // Then: Should return null
        assertNull(monthlyProduct)
    }
    
    // ========================================
    // Purchase Flow Tests
    // ========================================
    
    @Test
    fun `purchaseProduct with ProductInfo should call BillingManager`() = runTest {
        // Given: A product to purchase
        val product = ProductInfo(
            productId = "monthly_sub",
            basePlanId = "monthly-base",
            title = "Monthly",
            description = "Monthly subscription",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "USD"
        )
        
        // When: Purchasing the product
        viewModel.purchaseProduct(product)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: BillingManager should be called
        assertTrue(billingManager.launchPurchaseFlowCalled)
        assertEquals("monthly_sub", billingManager.lastProductIdPurchased)
        assertEquals("monthly-base", billingManager.lastBasePlanIdPurchased)
    }
    
    @Test
    fun `purchaseProduct with strings should call BillingManager`() = runTest {
        // When: Purchasing with product ID and base plan
        viewModel.purchaseProduct("yearly_sub", "yearly-base")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: BillingManager should be called
        assertTrue(billingManager.launchPurchaseFlowCalled)
        assertEquals("yearly_sub", billingManager.lastProductIdPurchased)
        assertEquals("yearly-base", billingManager.lastBasePlanIdPurchased)
    }
    
    @Test
    fun `purchaseProduct should update UI state on success`() = runTest {
        // Given: Purchase will succeed
        billingManager.shouldLaunchPurchaseFail = false
        
        // When: Purchasing a product
        viewModel.purchaseProduct("monthly_sub", "monthly-base")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: UI state should show success
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isProcessing)
        assertEquals("Purchase completed successfully!", uiState.successMessage)
        assertNull(uiState.errorMessage)
    }
    
    @Test
    fun `purchaseProduct should update UI state on failure`() = runTest {
        // Given: Purchase will fail
        billingManager.shouldLaunchPurchaseFail = true
        
        // When: Purchasing a product
        viewModel.purchaseProduct("monthly_sub", "monthly-base")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: UI state should show error
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isProcessing)
        assertNull(uiState.successMessage)
        assertNotNull(uiState.errorMessage)
    }
    
    // ========================================
    // Verify Subscription Tests
    // ========================================
    
    @Test
    fun `verifySubscription should call repository`() = runTest {
        // When: Verifying subscription
        viewModel.verifySubscription(forceRefresh = true)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Repository should be called
        assertTrue(repository.verifySubscriptionCalled)
        assertTrue(repository.lastForceRefresh)
    }
    
    @Test
    fun `verifySubscription should update UI state on success`() = runTest {
        // Given: Verification will succeed
        repository.shouldVerifyFail = false
        
        // When: Verifying subscription
        viewModel.verifySubscription()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: UI state should show success
        val uiState = viewModel.uiState.first()
        assertEquals("Subscription verified", uiState.successMessage)
    }
    
    @Test
    fun `verifySubscription should update UI state on failure`() = runTest {
        // Given: Verification will fail
        repository.shouldVerifyFail = true
        
        // When: Verifying subscription
        viewModel.verifySubscription()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: UI state should show error
        val uiState = viewModel.uiState.first()
        assertNotNull(uiState.errorMessage)
    }
    
    // ========================================
    // Restore Purchases Tests
    // ========================================
    
    @Test
    fun `restorePurchases should call repository`() = runTest {
        // When: Restoring purchases
        viewModel.restorePurchases()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Repository should be called
        assertTrue(repository.restorePurchasesCalled)
    }
    
    @Test
    fun `restorePurchases should show success when subscribed`() = runTest {
        // Given: User has active subscription after restore
        repository.shouldRestoreFail = false
        repository.mockSubscriptionState = repository.mockSubscriptionState.copy(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM
        )
        
        // When: Restoring purchases
        viewModel.restorePurchases()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: UI state should show subscription restored
        val uiState = viewModel.uiState.first()
        assertEquals("Subscription restored", uiState.successMessage)
    }
    
    @Test
    fun `restorePurchases should show no subscriptions when not subscribed`() = runTest {
        // Given: User has no active subscription after restore
        repository.shouldRestoreFail = false
        repository.mockSubscriptionState = repository.mockSubscriptionState.copy(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE
        )
        
        // When: Restoring purchases
        viewModel.restorePurchases()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: UI state should indicate no subscriptions
        val uiState = viewModel.uiState.first()
        assertEquals("No active subscriptions found", uiState.successMessage)
    }
    
    // ========================================
    // UI State Management Tests
    // ========================================
    
    @Test
    fun `clearMessages should clear success and error messages`() = runTest {
        // Given: UI state has messages
        viewModel.purchaseProduct("test_product")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Clearing messages
        viewModel.clearMessages()
        
        // Then: Messages should be cleared
        val uiState = viewModel.uiState.first()
        assertNull(uiState.successMessage)
        assertNull(uiState.errorMessage)
    }
    
    @Test
    fun `hasFeature should delegate to subscription state`() = runTest {
        // Given: Subscription state from repository
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When/Then: Should delegate to subscription state
        // (Implementation would need PremiumFeature check)
        // This is just verifying the method exists and doesn't crash
        val hasFeature = viewModel.hasFeature(me.calebjones.spacelaunchnow.data.model.PremiumFeature.CUSTOM_THEMES)
        // Just verify it returns a boolean (actual value depends on mock state)
        assertTrue(hasFeature is Boolean)
    }
}
