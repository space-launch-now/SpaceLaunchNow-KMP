package me.calebjones.spacelaunchnow.offer

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HolidayOfferSessionTest {
    @BeforeTest
    @AfterTest
    fun reset() = HolidayOfferSession.reset()

    @Test
    fun `starts unchecked and not shown`() {
        assertFalse(HolidayOfferSession.checked.value)
        assertFalse(HolidayOfferSession.shown.value)
    }

    @Test
    fun `eligible decision sets shown no later than checked`() {
        // A host that wakes on checked reads shown at once: it must already be true.
        var shownWhenChecked: Boolean? = null
        val before = HolidayOfferSession.checked.value
        assertFalse(before)
        HolidayOfferSession.recordDecision(eligible = true)
        if (HolidayOfferSession.checked.value) shownWhenChecked = HolidayOfferSession.shown.value
        assertTrue(shownWhenChecked == true)
    }

    @Test
    fun `ineligible decision checks without showing`() {
        HolidayOfferSession.recordDecision(eligible = false)
        assertTrue(HolidayOfferSession.checked.value)
        assertFalse(HolidayOfferSession.shown.value)
    }

    @Test
    fun `reset clears both flags`() {
        HolidayOfferSession.recordDecision(eligible = true)
        HolidayOfferSession.reset()
        assertFalse(HolidayOfferSession.checked.value)
        assertFalse(HolidayOfferSession.shown.value)
    }
}
