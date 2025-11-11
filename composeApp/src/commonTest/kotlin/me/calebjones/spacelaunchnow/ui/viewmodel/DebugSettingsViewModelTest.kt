package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.data.billing.MockBillingManager
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for DebugSettingsViewModel using BillingManager
 * Phase 8: Validates platform-agnostic debug tools
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DebugSettingsViewModelTest {
    
    private lateinit var billingManager: MockBillingManager
    private lateinit var viewModel: DebugSettingsViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        billingManager = MockBillingManager()
        viewModel = DebugSettingsViewModel(
            debugPreferences = null,  // Not testing debug preferences here
            billingManager = billingManager,
            launchRepository = null,
            notificationRepository = null
        )
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // ========================================
    // Billing Initialization Tests
    // ========================================
    
    @Test
    fun `checkBillingInitialization should show initialized status`() = runTest {
        // Given: Billing is initialized
        billingManager.initialize()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Checking initialization
        viewModel.checkBillingInitialization()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show successful status
        val statusMessage = viewModel.statusMessage.first()
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertEquals("Billing Status Check", statusMessage)
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("Initialized: true"))
    }
    
    @Test
    fun `checkBillingInitialization should show subscription state`() = runTest {
        // Given: User is subscribed
        billingManager.initialize()
        billingManager.launchPurchaseFlow("test_monthly")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Checking initialization
        viewModel.checkBillingInitialization()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show subscription details
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("Is Subscribed: true"))
        assertTrue(detailedMessage.contains("Subscription Type: PREMIUM"))
    }
    
    @Test
    fun `checkBillingInitialization should handle null billing manager`() = runTest {
        // Given: ViewModel with no billing manager
        val viewModelNoBilling = DebugSettingsViewModel(
            debugPreferences = null,
            billingManager = null,
            launchRepository = null,
            notificationRepository = null
        )
        
        // When: Checking initialization
        viewModelNoBilling.checkBillingInitialization()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show not available message
        val statusMessage = viewModelNoBilling.statusMessage.first()
        assertEquals("❌ BillingManager not available (not injected)", statusMessage)
    }
    
    // ========================================
    // Product Query Tests
    // ========================================
    
    @Test
    fun `queryBillingProducts should list available products`() = runTest {
        // Given: Multiple products available
        billingManager.mockProducts = listOf(
            ProductInfo(
                productId = "monthly_sub",
                basePlanId = "monthly-base",
                title = "Monthly Subscription",
                description = "MONTHLY - Month",
                formattedPrice = "$4.99",
                priceAmountMicros = 4990000L,
                currencyCode = "USD"
            ),
            ProductInfo(
                productId = "yearly_sub",
                basePlanId = "yearly-base",
                title = "Yearly Subscription",
                description = "ANNUAL - Year",
                formattedPrice = "$39.99",
                priceAmountMicros = 39990000L,
                currencyCode = "USD"
            ),
            ProductInfo(
                productId = "pro_lifetime",
                basePlanId = "lifetime",
                title = "Pro Lifetime",
                description = "One-time purchase",
                formattedPrice = "$99.99",
                priceAmountMicros = 99990000L,
                currencyCode = "USD"
            )
        )
        
        // When: Querying products
        viewModel.queryBillingProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show all products
        val statusMessage = viewModel.statusMessage.first()
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertEquals("Products Query Result", statusMessage)
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("Available Products (3)"))
        assertTrue(detailedMessage.contains("monthly_sub"))
        assertTrue(detailedMessage.contains("yearly_sub"))
        assertTrue(detailedMessage.contains("pro_lifetime"))
        assertTrue(detailedMessage.contains("$4.99"))
        assertTrue(detailedMessage.contains("$39.99"))
        assertTrue(detailedMessage.contains("$99.99"))
    }
    
    @Test
    fun `queryBillingProducts should handle empty product list`() = runTest {
        // Given: No products available
        billingManager.mockProducts = emptyList()
        
        // When: Querying products
        viewModel.queryBillingProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show no products message
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("No products available"))
    }
    
    @Test
    fun `queryBillingProducts should handle query failure`() = runTest {
        // Given: Product query will fail
        billingManager.shouldGetProductsFail = true
        
        // When: Querying products
        viewModel.queryBillingProducts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show error message
        val statusMessage = viewModel.statusMessage.first()
        assertTrue(statusMessage!!.contains("Error querying products"))
    }
    
    // ========================================
    // Entitlement Check Tests
    // ========================================
    
    @Test
    fun `checkBillingEntitlements should show active entitlements`() = runTest {
        // Given: User has active entitlements
        billingManager.initialize()
        billingManager.launchPurchaseFlow("test_monthly")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Checking entitlements
        viewModel.checkBillingEntitlements()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should list entitlements
        val statusMessage = viewModel.statusMessage.first()
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertEquals("Entitlements Check Result", statusMessage)
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("Active Entitlements"))
        assertTrue(detailedMessage.contains("premium"))
    }
    
    @Test
    fun `checkBillingEntitlements should show no entitlements for free user`() = runTest {
        // Given: Free user with no entitlements
        billingManager.initialize()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Checking entitlements
        viewModel.checkBillingEntitlements()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show free user message
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("None (Free user)"))
    }
    
    // ========================================
    // Restore Purchases Tests
    // ========================================
    
    @Test
    fun `testBillingRestore should restore purchases successfully`() = runTest {
        // Given: User has purchases to restore
        billingManager.initialize()
        billingManager.launchPurchaseFlow("test_monthly")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Testing restore
        viewModel.testBillingRestore()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show restore success
        val statusMessage = viewModel.statusMessage.first()
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertEquals("Restore Purchases Result", statusMessage)
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("Restore successful"))
        assertTrue(detailedMessage.contains("Is Subscribed: true"))
    }
    
    @Test
    fun `testBillingRestore should handle restore failure`() = runTest {
        // Given: Restore will fail
        billingManager.shouldRestorePurchasesFail = true
        
        // When: Testing restore
        viewModel.testBillingRestore()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show error message
        val statusMessage = viewModel.statusMessage.first()
        assertTrue(statusMessage!!.contains("Error restoring purchases"))
    }
    
    // ========================================
    // Product Details Tests
    // ========================================
    
    @Test
    fun `viewBillingProductDetails should categorize products by type`() = runTest {
        // Given: Products of different types
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
                basePlanId = "annual-base",
                title = "Yearly",
                description = "Annual subscription",
                formattedPrice = "$39.99",
                priceAmountMicros = 39990000L,
                currencyCode = "USD"
            ),
            ProductInfo(
                productId = "pro_lifetime",
                basePlanId = "lifetime",
                title = "Lifetime Pro",
                description = "One-time purchase",
                formattedPrice = "$99.99",
                priceAmountMicros = 99990000L,
                currencyCode = "USD"
            )
        )
        
        // When: Viewing product details
        viewModel.viewBillingProductDetails()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should categorize by type
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("Lifetime Product"))
        assertTrue(detailedMessage.contains("Annual Product"))
        assertTrue(detailedMessage.contains("Monthly Product"))
        assertTrue(detailedMessage.contains("$99.99"))
        assertTrue(detailedMessage.contains("$39.99"))
        assertTrue(detailedMessage.contains("$4.99"))
    }
    
    @Test
    fun `viewBillingProductDetails should handle no products`() = runTest {
        // Given: No products available
        billingManager.mockProducts = emptyList()
        
        // When: Viewing product details
        viewModel.viewBillingProductDetails()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Should show no products message
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertNotNull(detailedMessage)
        assertTrue(detailedMessage!!.contains("No products loaded"))
    }
    
    // ========================================
    // UI State Tests
    // ========================================
    
    @Test
    fun `clearStatusMessage should clear status and detailed messages`() = runTest {
        // Given: ViewModel with status messages
        viewModel.checkBillingInitialization()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Clearing messages
        viewModel.clearStatusMessage()
        
        // Then: Messages should be cleared
        val statusMessage = viewModel.statusMessage.first()
        val detailedMessage = viewModel.detailedMessage.first()
        
        assertNull(statusMessage)
        assertNull(detailedMessage)
    }
    
    @Test
    fun `isLoading should be false after operations complete`() = runTest {
        // Given: Starting an operation
        viewModel.checkBillingInitialization()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Loading should be false when complete
        val isLoading = viewModel.isLoading.first()
        assertFalse(isLoading)
    }
}
