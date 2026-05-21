package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PurchaseStateTest {

    @Test
    fun `default PurchaseState is free tier with no loaded flag`() {
        val state = PurchaseState()
        assertFalse(state.isSubscribed)
        assertEquals(SubscriptionType.FREE, state.subscriptionType)
        assertTrue(state.activeEntitlements.isEmpty())
        assertTrue(state.activeProductIds.isEmpty())
        assertTrue(state.features.isEmpty())
        assertEquals(0L, state.lastRefreshed)
        assertFalse(state.hasLoaded)
    }

    @Test
    fun `PurchaseState with premium subscription has correct features`() {
        val currentTime = System.currentTimeMillis()
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium"),
            activeProductIds = setOf("monthly_sub"),
            lastRefreshed = currentTime
        )
        assertTrue(state.isSubscribed)
        assertEquals(SubscriptionType.PREMIUM, state.subscriptionType)
        assertEquals(PremiumFeature.entries.toSet(), state.features)
        assertTrue(state.hasLoaded)
    }

    @Test
    fun `PurchaseState with lifetime subscription has all features`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.LIFETIME,
            activeEntitlements = setOf("premium", "lifetime"),
            activeProductIds = setOf("lifetime_purchase")
        )
        assertEquals(PremiumFeature.entries.size, state.features.size)
    }

    @Test
    fun `LEGACY subscription has basic features`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.LEGACY,
            lastRefreshed = 1000L
        )
        assertEquals(PremiumFeature.getBasicFeatures(), state.features)
    }

    @Test
    fun `features is derived from subscriptionType on copy`() {
        val original = PurchaseState(isSubscribed = true, subscriptionType = SubscriptionType.PREMIUM)
        val downgraded = original.copy(subscriptionType = SubscriptionType.FREE)
        assertEquals(PremiumFeature.entries.toSet(), original.features)
        assertTrue(downgraded.features.isEmpty())
    }

    @Test
    fun `toSubscriptionState converts correctly`() {
        val currentTime = System.currentTimeMillis()
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium"),
            activeProductIds = setOf("monthly_sub", "addon_1"),
            lastRefreshed = currentTime
        )
        val result = state.toSubscriptionState()
        assertTrue(result.isSubscribed)
        assertEquals(SubscriptionType.PREMIUM, result.subscriptionType)
        assertEquals("monthly_sub", result.productId)
        assertEquals(currentTime, result.lastVerified)
        assertEquals(PremiumFeature.entries.toSet(), result.features)
        assertFalse(result.needsVerification)
    }

    @Test
    fun `toSubscriptionState with no products returns null productId`() {
        val state = PurchaseState(isSubscribed = false, subscriptionType = SubscriptionType.FREE)
        val result = state.toSubscriptionState()
        assertEquals(null, result.productId)
    }

    @Test
    fun `PurchaseState tracks all entitlements`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium", "ad_free", "widgets", "cal_sync"),
            activeProductIds = setOf("premium_yearly")
        )
        assertEquals(4, state.activeEntitlements.size)
        assertTrue(state.activeEntitlements.containsAll(listOf("premium", "ad_free", "widgets", "cal_sync")))
    }
}
