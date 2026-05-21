package me.calebjones.spacelaunchnow.data.subscription

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
}
