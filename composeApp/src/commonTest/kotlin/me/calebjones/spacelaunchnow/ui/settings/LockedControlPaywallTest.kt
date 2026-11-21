package me.calebjones.spacelaunchnow.ui.settings

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepositoryImpl
import me.calebjones.spacelaunchnow.navigation.SupportUs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LockedControlPaywallTest {

    @Test fun `a free user tap opens the paywall while the flag is on`() {
        assertTrue(shouldOpenPaywallOnLockedTap(flagEnabled = true, hasCustomization = false))
    }

    @Test fun `the flag off turns the tap off`() {
        assertFalse(shouldOpenPaywallOnLockedTap(flagEnabled = false, hasCustomization = false))
    }

    @Test fun `a subscriber never sees the paywall from this tap`() {
        assertFalse(shouldOpenPaywallOnLockedTap(flagEnabled = true, hasCustomization = true))
    }

    @Test fun `the tap carries its own source value into the paywall route`() {
        assertEquals("locked_notification_control", PaywallSource.LOCKED_NOTIFICATION_CONTROL.value)
        assertEquals(
            "locked_notification_control",
            SupportUs(PaywallSource.LOCKED_NOTIFICATION_CONTROL.value).source,
        )
    }

    @Test fun `the flag defaults to off without Firebase`() = runTest {
        val repository = RemoteConfigRepositoryImpl(
            firebaseConfigured = { false },
            createRemoteConfig = { throw AssertionError("Firebase Remote Config must not be created") },
        )
        assertFalse(repository.getLockedNotificationTapPaywallEnabled())
    }

    @Test fun `the mock flag is off until a test turns it on`() = runTest {
        val config = MockRemoteConfigRepository()
        assertFalse(config.getLockedNotificationTapPaywallEnabled())
        config.lockedNotificationTapPaywallEnabled = true
        assertTrue(config.getLockedNotificationTapPaywallEnabled())
    }
}
