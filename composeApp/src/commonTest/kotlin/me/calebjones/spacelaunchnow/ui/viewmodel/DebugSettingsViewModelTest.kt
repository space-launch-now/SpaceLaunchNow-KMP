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
import me.calebjones.spacelaunchnow.data.repository.FakeNotificationRepository
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
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
    fun `setUseDebugTopics reconciles V6 subscriptions so the env swap takes effect immediately`() = runTest {
        // The env ("prod"/"debug") is baked into every v6_* topic name, so
        // flipping the toggle is a full resubscribe -- and it must happen at
        // toggle time, not at next app start: this switch is how a staging
        // device reaches the v6_debug_* topics at all.
        val prefs = DebugPreferences(InMemoryPreferencesDataStore())
        val notificationRepository = FakeNotificationRepository()
        val vm = DebugSettingsViewModel(
            debugPreferences = prefs,
            billingManager = billingManager,
            launchRepository = null,
            notificationRepository = notificationRepository
        )

        vm.setUseDebugTopics(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.getDebugSettings().useDebugTopics)
        assertEquals(1, notificationRepository.reconcileCalls)
        val message = vm.statusMessage.first()
        assertNotNull(message)
        assertTrue(message!!.contains("v6_debug"), "status should name the V6 env, was: $message")
    }

    @Test
    fun `setUseDebugTopics reports partial reconcile failure instead of claiming success`() = runTest {
        val prefs = DebugPreferences(InMemoryPreferencesDataStore())
        val notificationRepository = FakeNotificationRepository()
        notificationRepository.nextReconcileResult =
            me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult(attempted = 10, failed = 2)
        val vm = DebugSettingsViewModel(
            debugPreferences = prefs,
            billingManager = billingManager,
            launchRepository = null,
            notificationRepository = notificationRepository
        )

        vm.setUseDebugTopics(false)
        testDispatcher.scheduler.advanceUntilIdle()

        val message = vm.statusMessage.first()
        assertNotNull(message)
        assertTrue(message!!.contains("v6_prod"), "status should name the V6 env, was: $message")
        assertTrue(message.contains("failed"), "status must surface the failure, was: $message")
    }

    // ========================================
    // Trantor target (staging / prod)
    // ========================================

    @Test
    fun `switchToTrantorProdUrl points Trantor at prod and leaves the LL URL alone`() = runTest {
        val prefs = DebugPreferences(InMemoryPreferencesDataStore())
        val vm = DebugSettingsViewModel(
            debugPreferences = prefs,
            billingManager = billingManager,
            launchRepository = null,
            notificationRepository = null
        )

        vm.switchToTrantorProdUrl()
        testDispatcher.scheduler.advanceUntilIdle()

        val settings = prefs.getDebugSettings()
        assertEquals("https://api.spacelaunchnow.app", settings.trantorApiBaseUrl)
        assertEquals("https://api.spacelaunchnow.app", prefs.getEffectiveTrantorBaseUrl())
        // The Trantor preference is independent of the LL/SNAPI one.
        assertFalse(settings.useCustomApiUrl)
        assertEquals(DebugPreferences.PROD_API_URL, settings.customApiBaseUrl)
        val message = vm.statusMessage.first()
        assertNotNull(message)
        assertTrue(message!!.contains("production"), "status should name the target, was: $message")
    }

    @Test
    fun `switchToTrantorUrl returns Trantor to staging after a prod switch`() = runTest {
        val prefs = DebugPreferences(InMemoryPreferencesDataStore())
        val vm = DebugSettingsViewModel(
            debugPreferences = prefs,
            billingManager = billingManager,
            launchRepository = null,
            notificationRepository = null
        )

        vm.switchToTrantorProdUrl()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.switchToTrantorUrl()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DebugPreferences.TRANTOR_API_URL, prefs.getEffectiveTrantorBaseUrl())
    }

    // ========================================
    // Clear All Caches (Trantor migration lever)
    // ========================================

    private class FakeCacheWiper : me.calebjones.spacelaunchnow.database.CacheWiper {
        var clearCalls = 0
        var failWith: Exception? = null
        override suspend fun clearAll(imageLoader: coil3.ImageLoader?) {
            failWith?.let { throw it }
            clearCalls++
        }
    }

    @Test
    fun `clearAllCaches wipes the caches and tells the user to restart`() = runTest {
        val wiper = FakeCacheWiper()
        val vm = DebugSettingsViewModel(
            debugPreferences = null,
            billingManager = billingManager,
            launchRepository = null,
            notificationRepository = null,
            cacheWiper = wiper
        )

        vm.clearAllCaches()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, wiper.clearCalls)
        val message = vm.statusMessage.first()
        assertNotNull(message)
        assertTrue(message!!.contains("restart", ignoreCase = true), "status should ask for a restart, was: $message")
        assertFalse(vm.isLoading.first())
    }

    @Test
    fun `clearAllCaches surfaces a wipe failure instead of claiming success`() = runTest {
        val wiper = FakeCacheWiper().apply { failWith = IllegalStateException("disk locked") }
        val vm = DebugSettingsViewModel(
            debugPreferences = null,
            billingManager = billingManager,
            launchRepository = null,
            notificationRepository = null,
            cacheWiper = wiper
        )

        vm.clearAllCaches()
        testDispatcher.scheduler.advanceUntilIdle()

        val message = vm.statusMessage.first()
        assertNotNull(message)
        assertTrue(message!!.contains("disk locked"), "status must surface the failure, was: $message")
        assertFalse(vm.isLoading.first())
    }

    @Test
    fun `clearAllCaches reports when no wiper is wired`() = runTest {
        viewModel.clearAllCaches()
        testDispatcher.scheduler.advanceUntilIdle()

        val message = viewModel.statusMessage.first()
        assertNotNull(message)
        assertTrue(message!!.contains("not available"), "was: $message")
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
