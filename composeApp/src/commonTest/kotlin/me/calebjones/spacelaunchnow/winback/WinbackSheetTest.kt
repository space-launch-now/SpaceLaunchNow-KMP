package me.calebjones.spacelaunchnow.winback

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WinbackSheetTest {

    private val day = 24L * 60L * 60L * 1000L
    private val dismissedAt = 1_800_000_000_000L

    private fun input(
        flag: Boolean = true,
        dismissed: Long? = dismissedAt,
        shown: Boolean = false,
        subscribed: Boolean = false,
        now: Long = dismissedAt + 7 * day,
    ) = WinbackSheetRule.Input(flag, dismissed, shown, subscribed, now)

    @Test
    fun `shows exactly at day 7 and after`() {
        assertEquals(WinbackSheetRule.Decision.SHOW, WinbackSheetRule.evaluate(input(now = dismissedAt + 7 * day)))
        assertEquals(WinbackSheetRule.Decision.SHOW, WinbackSheetRule.evaluate(input(now = dismissedAt + 40 * day)))
    }

    @Test
    fun `blocked before day 7`() {
        assertEquals(
            WinbackSheetRule.Decision.BLOCKED_TOO_EARLY,
            WinbackSheetRule.evaluate(input(now = dismissedAt + 7 * day - 1))
        )
        assertEquals(WinbackSheetRule.Decision.BLOCKED_TOO_EARLY, WinbackSheetRule.evaluate(input(now = dismissedAt)))
    }

    @Test
    fun `blocked when the onboarding paywall was never dismissed`() {
        assertEquals(WinbackSheetRule.Decision.BLOCKED_NOT_DISMISSED, WinbackSheetRule.evaluate(input(dismissed = null)))
    }

    @Test
    fun `blocked once shown`() {
        assertEquals(WinbackSheetRule.Decision.BLOCKED_ALREADY_SHOWN, WinbackSheetRule.evaluate(input(shown = true)))
    }

    @Test
    fun `blocked for subscribers`() {
        assertEquals(WinbackSheetRule.Decision.BLOCKED_SUBSCRIBED, WinbackSheetRule.evaluate(input(subscribed = true)))
    }

    @Test
    fun `blocked when the flag is off`() {
        assertEquals(WinbackSheetRule.Decision.BLOCKED_FLAG_OFF, WinbackSheetRule.evaluate(input(flag = false)))
    }

    private class Fixture(var now: Long, var dismissed: Long? = 1_800_000_000_000L) {
        val config = MockRemoteConfigRepository().apply { winbackSheetEnabled = true }
        var shown = false
        val controller = WinbackSheetController(
            flagEnabled = { config.getWinbackSheetEnabled() },
            onboardingDismissedAtMs = { dismissed },
            alreadyShown = { shown },
            saveShown = { shown = true },
            nowMs = { now },
        )
    }

    @Test
    fun `controller is eligible at day 7 and stops after markShown`() = runTest {
        val f = Fixture(now = 1_800_000_000_000L + 7 * day)
        assertTrue(f.controller.isEligible(isSubscribed = false))
        f.controller.markShown()
        assertFalse(f.controller.isEligible(isSubscribed = false))
    }

    @Test
    fun `controller follows the remote flag`() = runTest {
        val f = Fixture(now = 1_800_000_000_000L + 8 * day)
        f.config.winbackSheetEnabled = false
        assertFalse(f.controller.isEligible(isSubscribed = false))
        f.config.winbackSheetEnabled = true
        assertTrue(f.controller.isEligible(isSubscribed = false))
    }

    @Test
    fun `controller never offers a subscriber`() = runTest {
        val f = Fixture(now = 1_800_000_000_000L + 8 * day)
        assertFalse(f.controller.isEligible(isSubscribed = true))
    }
}
