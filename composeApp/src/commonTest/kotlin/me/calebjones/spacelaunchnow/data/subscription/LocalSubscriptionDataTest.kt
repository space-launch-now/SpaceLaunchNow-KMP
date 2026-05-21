package me.calebjones.spacelaunchnow.data.subscription

import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalSubscriptionDataTest {

    @Test
    fun `default LocalSubscriptionData has no trial`() {
        val data = LocalSubscriptionData()
        assertFalse(data.isInTrialPeriod)
        assertEquals(null, data.trialExpiresAt)
    }

    @Test
    fun `trial fields survive copy`() {
        val trialExpiry = 9_999_999_999L
        val data = LocalSubscriptionData(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            isInTrialPeriod = true,
            trialExpiresAt = trialExpiry
        )
        val copy = data.copy(isSubscribed = false)
        assertTrue(copy.isInTrialPeriod)
        assertEquals(trialExpiry, copy.trialExpiresAt)
    }

    @Test
    fun `FREE companion has no trial`() {
        assertFalse(LocalSubscriptionData.FREE.isInTrialPeriod)
        assertEquals(null, LocalSubscriptionData.FREE.trialExpiresAt)
    }

    @Test
    fun `fromPurchaseState copies subscription fields from purchase`() {
        val existing = LocalSubscriptionData(wasEverPremium = false)
        val purchase = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeProductIds = setOf("sln_monthly"),
            subscriptionExpiryMs = 9_999_999_999L,
            isInTrialPeriod = false,
            trialExpiresAt = null,
            lastRefreshed = 1_000L
        )
        val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)

        assertTrue(result.isSubscribed)
        assertEquals(SubscriptionType.PREMIUM, result.subscriptionType)
        assertEquals(setOf("sln_monthly"), result.productIds)
        assertEquals(9_999_999_999L, result.subscriptionExpiryMs)
        assertFalse(result.isDebugMode)
        assertFalse(result.needsSync)
    }

    @Test
    fun `fromPurchaseState wasEverPremium sticky from existing`() {
        val existing = LocalSubscriptionData(wasEverPremium = true)
        val purchase = PurchaseState(isSubscribed = false, subscriptionType = SubscriptionType.FREE)
        val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)
        assertTrue(result.wasEverPremium)
    }

    @Test
    fun `fromPurchaseState wasEverPremium sticky from purchase`() {
        val existing = LocalSubscriptionData(wasEverPremium = false)
        val purchase = PurchaseState(isSubscribed = true, subscriptionType = SubscriptionType.PREMIUM)
        val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)
        assertTrue(result.wasEverPremium)
    }

    @Test
    fun `fromPurchaseState sets lastSynced to current time`() {
        val existing = LocalSubscriptionData(lastSynced = 0L)
        val purchase = PurchaseState(isSubscribed = false, subscriptionType = SubscriptionType.FREE)
        val before = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)
        val after = kotlin.time.Clock.System.now().toEpochMilliseconds()
        assertTrue(result.lastSynced in before..after)
    }
}
