package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.platform.AppEnvironmentInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RevenueCatAttributesSyncerTest {

    private class FakeAttributes : RevenueCatAttributes {
        val calls = mutableListOf<Map<String, String?>>()
        var lastPushToken: String? = null
        override fun set(attributes: Map<String, String?>) {
            calls += attributes
        }
        override fun setPushToken(token: String) {
            lastPushToken = token
        }
    }

    @Test
    fun `pushSnapshot includes app and device attributes`() = runTest {
        val fake = FakeAttributes()
        val syncer = RevenueCatAttributesSyncer(
            attributes = fake,
            envInfo = AppEnvironmentInfo(),
            subscriptionStateProvider = { "free" },
            themeModeProvider = { "system" },
            hasCustomThemeProvider = { false },
            grantsTotalProvider = { 0L },
            adsShownTotalProvider = { 0L },
            tempAccessActiveProvider = { false }
        )

        syncer.pushSnapshot()

        assertEquals(1, fake.calls.size)
        val snapshot = fake.calls.single()
        assertTrue(snapshot.containsKey("app_version"))
        assertTrue(snapshot.containsKey("platform"))
        assertTrue(snapshot.containsKey("os_version"))
        assertTrue(snapshot.containsKey("subscription_state"))
        assertEquals("free", snapshot["subscription_state"])
        assertEquals("0", snapshot["temporary_access_grants_total"])
    }

    @Test
    fun `pushSnapshot reflects updated providers on each call`() = runTest {
        val fake = FakeAttributes()
        var subState = "free"
        val syncer = RevenueCatAttributesSyncer(
            attributes = fake,
            envInfo = AppEnvironmentInfo(),
            subscriptionStateProvider = { subState },
            themeModeProvider = { "system" },
            hasCustomThemeProvider = { false },
            grantsTotalProvider = { 0L },
            adsShownTotalProvider = { 0L },
            tempAccessActiveProvider = { false }
        )

        syncer.pushSnapshot()
        subState = "premium"
        syncer.pushSnapshot()

        assertEquals(2, fake.calls.size)
        assertEquals("free", fake.calls[0]["subscription_state"])
        assertEquals("premium", fake.calls[1]["subscription_state"])
    }
}
