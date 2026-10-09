package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.api.trantor.models.AgencyList
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toDomainAgency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CountryNamesTest {

    @Test
    fun knownCodesUseLlStyleNames() {
        assertEquals("United States of America", CountryNames.nameFor("US"))
        assertEquals("Russia", CountryNames.nameFor("RU"))
        assertEquals("France", CountryNames.nameFor("FR"))
        assertEquals("China", CountryNames.nameFor("CN"))
    }

    @Test
    fun lookupIgnoresCaseAndWhitespace() {
        assertEquals("Japan", CountryNames.nameFor(" jp "))
    }

    @Test
    fun unknownOrNullCodeReturnsNull() {
        assertNull(CountryNames.nameFor("ZZ"))
        assertNull(CountryNames.nameFor(null))
        assertEquals("ZZ", CountryNames.nameOrCode("ZZ"))
    }

    @Test
    fun tableCoversEveryIsoCodeShape() {
        assertTrue(CountryNames.NAMES.size >= 249, "expected the full ISO 3166-1 list, got ${CountryNames.NAMES.size}")
        assertTrue(CountryNames.NAMES.keys.all { it.length == 2 && it == it.uppercase() })
        assertTrue(CountryNames.NAMES.values.all { it.isNotBlank() })
    }

    @Test
    fun trantorAgencyMapperFillsCountryName() {
        val agency = AgencyList(
            abbrev = "CNES",
            id = 1,
            name = "Centre National d'Etudes Spatiales",
            countryCodes = listOf("FR", "ZZ")
        ).toDomainAgency()
        assertEquals("France", agency.countries[0].name)
        assertEquals("FR", agency.countries[0].alpha2Code)
        assertNull(agency.countries[1].name)
        assertEquals("ZZ", agency.countries[1].alpha2Code)
    }
}
