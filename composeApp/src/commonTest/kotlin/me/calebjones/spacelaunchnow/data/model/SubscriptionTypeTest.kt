package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SubscriptionTypeTest {

    @Test
    fun `fromProductId should return PREMIUM for premium product IDs`() {
        // Test various premium product ID formats
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("premium_monthly"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("premium_yearly"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("PREMIUM_SUBSCRIPTION"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("app.premium.plan"))
    }

    @Test
    fun `fromProductId should return LEGACY for legacy product IDs`() {
        // Test various legacy product ID formats
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("legacy_subscription"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("LEGACY_PLAN"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("basic_monthly"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("BASIC_YEARLY"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("app.legacy.plan"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("app.basic.plan"))
    }

    @Test
    fun `fromProductId should return FREE for unknown product IDs`() {
        // Test unknown or unrecognized product IDs
        assertEquals(SubscriptionType.FREE, SubscriptionType.fromProductId("unknown_product"))
        assertEquals(SubscriptionType.FREE, SubscriptionType.fromProductId(""))
        assertEquals(SubscriptionType.FREE, SubscriptionType.fromProductId("random_string"))
        assertEquals(SubscriptionType.FREE, SubscriptionType.fromProductId("app.free.plan"))
    }

    @Test
    fun `fromProductId should be case insensitive`() {
        // Test case insensitivity
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("Premium"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("PREMIUM"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("premium"))
        
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("Legacy"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("LEGACY"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("legacy"))
        
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("Basic"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("BASIC"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("basic"))
    }

    @Test
    fun `verify subscription type properties`() {
        // Test isLegacy property
        assertEquals(false, SubscriptionType.FREE.isLegacy)
        assertEquals(true, SubscriptionType.LEGACY.isLegacy)
        assertEquals(false, SubscriptionType.PREMIUM.isLegacy)
    }
}