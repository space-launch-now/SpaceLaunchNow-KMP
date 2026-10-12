package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals

class AgencyLogoUrlsTest {

    @Test
    fun socialLogoComesFirstThenLogoUrl() {
        assertEquals(listOf("s", "l"), agencyLogoCandidates("s", "l"))
    }

    @Test
    fun nullSocialLogoFallsBackToLogoUrl() {
        assertEquals(listOf("l"), agencyLogoCandidates(null, "l"))
    }

    @Test
    fun blankValuesAndDuplicatesAreDropped() {
        assertEquals(listOf("l"), agencyLogoCandidates("", "l"))
        assertEquals(listOf("s"), agencyLogoCandidates("s", "s"))
        assertEquals(emptyList(), agencyLogoCandidates(null, " "))
    }
}
