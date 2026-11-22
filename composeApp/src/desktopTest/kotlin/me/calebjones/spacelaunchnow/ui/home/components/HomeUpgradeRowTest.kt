package me.calebjones.spacelaunchnow.ui.home.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeUpgradeRowTest {

    @Test
    fun `row shows for a free user with the flag on and no close`() {
        assertTrue(shouldShowHomeUpgradeRow(flagEnabled = true, isPremium = false, dismissed = false))
    }

    @Test
    fun `row hides when the flag is off, the user subscribes, or the user closed it`() {
        assertFalse(shouldShowHomeUpgradeRow(flagEnabled = false, isPremium = false, dismissed = false))
        assertFalse(shouldShowHomeUpgradeRow(flagEnabled = true, isPremium = true, dismissed = false))
        assertFalse(shouldShowHomeUpgradeRow(flagEnabled = true, isPremium = false, dismissed = true))
    }
}
