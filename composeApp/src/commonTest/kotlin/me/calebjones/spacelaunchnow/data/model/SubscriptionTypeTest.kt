package me.calebjones.spacelaunchnow.data.model

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit

class SubscriptionTypeTest {

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
    }

    @Test
    fun `fromProductId should return PREMIUM for premium product IDs`() {
        // Test various premium product ID formats
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("pro"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("yearly"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("monthly"))
        assertEquals(SubscriptionType.PREMIUM, SubscriptionType.fromProductId("base-plan"))
    }

    @Test
    fun `fromProductId should return LEGACY for legacy product IDs`() {
        // Test various legacy product ID formats
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("2018_"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("2020_"))
        assertEquals(SubscriptionType.LEGACY, SubscriptionType.fromProductId("founder"))
    }

    @Test
    fun `verify subscription type properties`() {
        // Test isLegacy property
        assertEquals(false, SubscriptionType.FREE.isLegacy)
        assertEquals(true, SubscriptionType.LEGACY.isLegacy)
        assertEquals(false, SubscriptionType.PREMIUM.isLegacy)
    }
}