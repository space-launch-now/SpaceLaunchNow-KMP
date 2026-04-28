package me.calebjones.spacelaunchnow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgencyTest {

    @Test
    fun minimalAgencyConstruction() {
        val agency = Agency(
            id = 1,
            name = "NASA",
            abbrev = "NASA",
            typeName = "Government",
            countries = emptyList(),
            imageUrl = null,
            logoUrl = null,
            socialLogoUrl = null,
            description = null,
            administrator = null,
            foundingYear = null
        )
        assertEquals(1, agency.id)
        assertEquals("NASA", agency.name)
        assertTrue(agency.countries.isEmpty())
        assertNull(agency.description)
        assertNull(agency.foundingYear)
    }

    @Test
    fun fullyPopulatedAgency() {
        val country = Country(
            id = 1,
            name = "United States",
            alpha2Code = "US",
            alpha3Code = "USA",
            nationalityName = "American",
            nationalityNameComposed = "American"
        )
        val agency = Agency(
            id = 121,
            name = "SpaceX",
            abbrev = "SpX",
            typeName = "Commercial",
            countries = listOf(country),
            imageUrl = "https://example.com/image.png",
            logoUrl = "https://example.com/logo.png",
            socialLogoUrl = "https://example.com/social.png",
            description = "Private launch provider",
            administrator = "Elon Musk",
            foundingYear = 2002
        )
        assertEquals("SpaceX", agency.name)
        assertEquals(2002, agency.foundingYear)
        assertEquals(1, agency.countries.size)
        assertEquals("US", agency.countries.first().alpha2Code)
    }
}
