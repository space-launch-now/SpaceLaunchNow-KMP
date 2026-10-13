package me.calebjones.spacelaunchnow.data.subscription

import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalSubscriptionStorageTest {

    private val written = LocalSubscriptionData(
        isSubscribed = false,
        subscriptionType = SubscriptionType.FREE,
        lastSynced = 1_000L,
        needsSync = false,
    )

    @Test
    fun readBackWithDifferentLastSyncedIsSuccess() {
        val readBack = written.copy(lastSynced = 1_003L)

        assertTrue(readBackMatches(written, readBack))
    }

    @Test
    fun identicalReadBackIsSuccess() {
        assertTrue(readBackMatches(written, written.copy()))
    }

    @Test
    fun readBackWithDifferentSubscriptionTypeIsFailure() {
        val readBack = written.copy(subscriptionType = SubscriptionType.PREMIUM)

        assertFalse(readBackMatches(written, readBack))
    }

    @Test
    fun differingLastSyncedDoesNotHideARealMismatch() {
        val readBack = written.copy(lastSynced = 1_003L, isSubscribed = true)

        assertFalse(readBackMatches(written, readBack))
    }

    @Test
    fun nullReadBackIsFailure() {
        assertFalse(readBackMatches(written, null))
    }
}
