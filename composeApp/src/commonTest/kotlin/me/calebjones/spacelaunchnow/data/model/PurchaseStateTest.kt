package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PurchaseStateTest {
    
    @Test
    fun `default PurchaseState should be free tier`() {
        val state = PurchaseState()
        
        assertFalse(state.isSubscribed)
        assertEquals(SubscriptionType.FREE, state.subscriptionType)
        assertTrue(state.activeEntitlements.isEmpty())
        assertTrue(state.activeProductIds.isEmpty())
        assertTrue(state.features.isEmpty())
        assertEquals(0L, state.lastRefreshed)
        assertEquals(null, state.userId)
    }
    
    @Test
    fun `PurchaseState with premium subscription should have correct state`() {
        val currentTime = System.currentTimeMillis()
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium"),
            activeProductIds = setOf("monthly_sub"),
            features = PremiumFeature.getPremiumFeatures(),
            lastRefreshed = currentTime,
            userId = "user123"
        )
        
        assertTrue(state.isSubscribed)
        assertEquals(SubscriptionType.PREMIUM, state.subscriptionType)
        assertEquals(setOf("premium"), state.activeEntitlements)
        assertEquals(setOf("monthly_sub"), state.activeProductIds)
        assertEquals(PremiumFeature.entries.toSet(), state.features)
        assertEquals(currentTime, state.lastRefreshed)
        assertEquals("user123", state.userId)
    }
    
    @Test
    fun `PurchaseState with lifetime subscription should have all features`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.LIFETIME,
            activeEntitlements = setOf("premium", "lifetime"),
            activeProductIds = setOf("lifetime_purchase"),
            features = PremiumFeature.getPremiumFeatures()
        )
        
        assertTrue(state.isSubscribed)
        assertEquals(SubscriptionType.LIFETIME, state.subscriptionType)
        assertTrue(state.activeEntitlements.contains("lifetime"))
        assertEquals(PremiumFeature.entries.size, state.features.size)
    }
    
    @Test
    fun `toSubscriptionState should convert PurchaseState to SubscriptionState`() {
        val currentTime = System.currentTimeMillis()
        val purchaseState = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium"),
            activeProductIds = setOf("monthly_sub", "addon_1"),
            features = PremiumFeature.getPremiumFeatures(),
            lastRefreshed = currentTime,
            userId = "user123"
        )
        
        val subscriptionState = purchaseState.toSubscriptionState()
        
        assertEquals(true, subscriptionState.isSubscribed)
        assertEquals(SubscriptionType.PREMIUM, subscriptionState.subscriptionType)
        assertEquals("monthly_sub", subscriptionState.productId) // First product ID
        assertEquals(currentTime, subscriptionState.lastVerified)
        assertEquals(PremiumFeature.entries.toSet(), subscriptionState.features)
        assertEquals(false, subscriptionState.needsVerification)
    }
    
    @Test
    fun `toSubscriptionState with no products should handle null productId`() {
        val purchaseState = PurchaseState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            activeProductIds = emptySet()
        )
        
        val subscriptionState = purchaseState.toSubscriptionState()
        
        assertEquals(null, subscriptionState.productId)
        assertEquals(SubscriptionType.FREE, subscriptionState.subscriptionType)
    }
    
    @Test
    fun `PurchaseState with multiple entitlements should track all`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium", "ad_free", "widgets", "cal_sync"),
            activeProductIds = setOf("premium_yearly"),
            features = PremiumFeature.getPremiumFeatures()
        )
        
        assertEquals(4, state.activeEntitlements.size)
        assertTrue(state.activeEntitlements.contains("premium"))
        assertTrue(state.activeEntitlements.contains("ad_free"))
        assertTrue(state.activeEntitlements.contains("widgets"))
        assertTrue(state.activeEntitlements.contains("cal_sync"))
    }
    
    @Test
    fun `PurchaseState with multiple products should track all`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeProductIds = setOf("premium_yearly", "addon_themes", "addon_notifications")
        )
        
        assertEquals(3, state.activeProductIds.size)
        assertTrue(state.activeProductIds.contains("premium_yearly"))
        assertTrue(state.activeProductIds.contains("addon_themes"))
        assertTrue(state.activeProductIds.contains("addon_notifications"))
    }
    
    @Test
    fun `PurchaseState copy should create independent instance`() {
        val original = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium"),
            userId = "user1"
        )
        
        val copy = original.copy(userId = "user2")
        
        assertEquals("user1", original.userId)
        assertEquals("user2", copy.userId)
        assertEquals(original.isSubscribed, copy.isSubscribed)
        assertEquals(original.subscriptionType, copy.subscriptionType)
    }
}
