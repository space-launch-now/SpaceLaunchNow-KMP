package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for BillingManager interface using MockBillingManager
 * These tests validate the contract that all BillingManager implementations must follow
 */
class BillingManagerTest {
    
    private fun createManager() = MockBillingManager()
    
    @Test
    fun `initialize should set isInitialized to true on success`() = runTest {
        val manager = createManager()
        
        assertFalse(manager.isInitialized.value, "Should start uninitialized")
        
        val result = manager.initialize()
        
        assertTrue(result.isSuccess, "Initialize should succeed")
        assertTrue(manager.isInitialized.value, "Should be initialized after successful init")
        assertTrue(manager.initializeCalled, "Initialize should have been called")
    }
    
    @Test
    fun `initialize should handle failure gracefully`() = runTest {
        val manager = createManager()
        manager.shouldInitializeFail = true
        
        val result = manager.initialize()
        
        assertTrue(result.isFailure, "Initialize should fail when configured to fail")
        assertFalse(manager.isInitialized.value, "Should remain uninitialized on failure")
    }
    
    @Test
    fun `initialize should accept appUserId parameter`() = runTest {
        val manager = createManager()
        val testUserId = "test_user_123"
        
        manager.initialize(appUserId = testUserId)
        
        assertEquals(testUserId, manager.lastAppUserId, "Should store the provided app user ID")
    }
    
    @Test
    fun `purchaseState should start as free tier`() = runTest {
        val manager = createManager()
        
        val state = manager.purchaseState.first()
        
        assertFalse(state.isSubscribed, "Should start unsubscribed")
        assertEquals(SubscriptionType.FREE, state.subscriptionType, "Should start as FREE tier")
        assertTrue(state.activeEntitlements.isEmpty(), "Should have no entitlements")
        assertTrue(state.activeProductIds.isEmpty(), "Should have no products")
    }
    
    @Test
    fun `launchPurchaseFlow should update purchaseState on success`() = runTest {
        val manager = createManager()
        val productId = "test_monthly"
        
        val result = manager.launchPurchaseFlow(productId)
        
        assertTrue(result.isSuccess, "Purchase should succeed")
        assertTrue(manager.launchPurchaseFlowCalled, "Launch purchase flow should be called")
        assertEquals(productId, manager.lastProductIdPurchased, "Should track purchased product")
        
        val state = manager.purchaseState.first()
        assertTrue(state.isSubscribed, "Should be subscribed after purchase")
        assertTrue(state.activeProductIds.contains(productId), "Should have the purchased product")
    }
    
    @Test
    fun `launchPurchaseFlow should handle failure`() = runTest {
        val manager = createManager()
        manager.shouldLaunchPurchaseFail = true
        
        val result = manager.launchPurchaseFlow("test_product")
        
        assertTrue(result.isFailure, "Purchase should fail when configured to fail")
        
        val state = manager.purchaseState.first()
        assertFalse(state.isSubscribed, "Should remain unsubscribed on failed purchase")
    }
    
    @Test
    fun `launchPurchaseFlow should accept basePlanId parameter`() = runTest {
        val manager = createManager()
        val productId = "test_yearly"
        val basePlanId = "yearly-base"
        
        manager.launchPurchaseFlow(productId, basePlanId)
        
        assertEquals(productId, manager.lastProductIdPurchased)
        assertEquals(basePlanId, manager.lastBasePlanIdPurchased)
    }
    
    @Test
    fun `getAvailableProducts should return product list`() = runTest {
        val manager = createManager()
        
        val result = manager.getAvailableProducts()
        
        assertTrue(result.isSuccess, "Get products should succeed")
        assertTrue(manager.getProductsCalled, "Get products should be called")
        
        val products = result.getOrNull()
        assertEquals(2, products?.size, "Should return mock products")
        assertEquals("test_monthly", products?.get(0)?.productId)
        assertEquals("test_yearly", products?.get(1)?.productId)
    }
    
    @Test
    fun `getAvailableProducts should handle failure`() = runTest {
        val manager = createManager()
        manager.shouldGetProductsFail = true
        
        val result = manager.getAvailableProducts()
        
        assertTrue(result.isFailure, "Get products should fail when configured")
    }
    
    @Test
    fun `restorePurchases should return current purchaseState`() = runTest {
        val manager = createManager()
        manager.setSubscribed(productId = "restored_product")
        
        val result = manager.restorePurchases()
        
        assertTrue(result.isSuccess, "Restore should succeed")
        assertTrue(manager.restorePurchasesCalled, "Restore purchases should be called")
        
        val state = result.getOrNull()
        assertTrue(state?.isSubscribed == true, "Should restore subscription status")
        assertTrue(state?.activeProductIds?.contains("restored_product") == true)
    }
    
    @Test
    fun `restorePurchases should handle failure`() = runTest {
        val manager = createManager()
        manager.shouldRestorePurchasesFail = true
        
        val result = manager.restorePurchases()
        
        assertTrue(result.isFailure, "Restore should fail when configured")
    }
    
    @Test
    fun `hasEntitlement should check active entitlements`() = runTest {
        val manager = createManager()
        
        // Start with no entitlements
        assertFalse(manager.hasEntitlement("premium"), "Should not have premium initially")
        
        // Add entitlements
        manager.setSubscribed(entitlements = setOf("premium", "founder"))
        
        assertTrue(manager.hasEntitlement("premium"), "Should have premium entitlement")
        assertTrue(manager.hasEntitlement("founder"), "Should have founder entitlement")
        assertFalse(manager.hasEntitlement("nonexistent"), "Should not have nonexistent entitlement")
    }
    
    @Test
    fun `getActiveEntitlements should return all entitlements`() = runTest {
        val manager = createManager()
        manager.setSubscribed(entitlements = setOf("premium", "ad_free", "widgets"))
        
        val entitlements = manager.getActiveEntitlements()
        
        assertEquals(3, entitlements.size)
        assertTrue(entitlements.contains("premium"))
        assertTrue(entitlements.contains("ad_free"))
        assertTrue(entitlements.contains("widgets"))
    }
    
    @Test
    fun `refreshPurchaseState should return success status`() = runTest {
        val manager = createManager()
        
        val success = manager.refreshPurchaseState()
        
        assertTrue(success, "Refresh should succeed")
        assertTrue(manager.refreshCalled, "Refresh should be called")
    }
    
    @Test
    fun `refreshPurchaseState should handle failure`() = runTest {
        val manager = createManager()
        manager.shouldRefreshFail = true
        
        val success = manager.refreshPurchaseState()
        
        assertFalse(success, "Refresh should fail when configured")
    }
    
    @Test
    fun `syncPurchases should be callable`() = runTest {
        val manager = createManager()
        
        manager.syncPurchases()
        
        assertTrue(manager.syncPurchasesCalled, "Sync should be called")
    }
    
    @Test
    fun `setSubscribed test helper should update state correctly`() = runTest {
        val manager = createManager()
        
        manager.setSubscribed(
            type = SubscriptionType.LIFETIME,
            productId = "lifetime_product",
            entitlements = setOf("premium", "lifetime")
        )
        
        val state = manager.purchaseState.first()
        
        assertTrue(state.isSubscribed)
        assertEquals(SubscriptionType.LIFETIME, state.subscriptionType)
        assertEquals(setOf("lifetime_product"), state.activeProductIds)
        assertEquals(setOf("premium", "lifetime"), state.activeEntitlements)
    }
    
    @Test
    fun `setFree test helper should update state correctly`() = runTest {
        val manager = createManager()
        
        // First set as subscribed
        manager.setSubscribed()
        assertTrue(manager.purchaseState.first().isSubscribed)
        
        // Then set as free
        manager.setFree()
        
        val state = manager.purchaseState.first()
        
        assertFalse(state.isSubscribed)
        assertEquals(SubscriptionType.FREE, state.subscriptionType)
        assertTrue(state.activeEntitlements.isEmpty())
        assertTrue(state.activeProductIds.isEmpty())
    }
    
    @Test
    fun `reset test helper should clear all state`() = runTest {
        val manager = createManager()
        
        // Make some calls and set state
        manager.initialize("user123")
        manager.launchPurchaseFlow("product")
        manager.getAvailableProducts()
        
        assertTrue(manager.initializeCalled)
        assertTrue(manager.launchPurchaseFlowCalled)
        assertTrue(manager.getProductsCalled)
        
        // Reset
        manager.reset()
        
        // Verify all state cleared
        assertFalse(manager.initializeCalled)
        assertFalse(manager.launchPurchaseFlowCalled)
        assertFalse(manager.getProductsCalled)
        assertFalse(manager.isInitialized.value)
        assertFalse(manager.purchaseState.value.isSubscribed)
        assertEquals(null, manager.lastProductIdPurchased)
        assertEquals(null, manager.lastAppUserId)
    }
}
