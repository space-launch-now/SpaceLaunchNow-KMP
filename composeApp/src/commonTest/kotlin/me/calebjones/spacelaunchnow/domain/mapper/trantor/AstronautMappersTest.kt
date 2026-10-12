package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AstronautDetail
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** E14(P5): astronauts gain `agency_abbrev` and `nationality_codes`. */
class AstronautMappersTest {

    @Test
    fun listRowMapsAgencyAbbrevAndNationalityCodes() {
        val domain = AstronautList(
            id = 1,
            name = "Jane Doe",
            agencyAbbrev = "NASA",
            nationalityCodes = listOf("US", "FR")
        ).toDomainListItem()
        assertEquals("NASA", domain.agencyAbbrev)
        assertEquals(listOf("US", "FR"), domain.nationality.map { it.alpha2Code })
        assertEquals("United States of America", domain.nationality.first().name)
    }

    @Test
    fun listRowIsEmptyWhenCodesAbsent() {
        val domain = AstronautList(id = 1, name = "Jane Doe").toDomainListItem()
        assertNull(domain.agencyAbbrev)
        assertTrue(domain.nationality.isEmpty())
    }

    @Test
    fun detailMapsAgencyAbbrevAndNationalityCodes() {
        val domain = AstronautDetail(
            id = 1,
            name = "Jane Doe",
            agencyAbbrev = "ESA",
            nationalityCodes = listOf("DE")
        ).toDomainDetail()
        assertEquals("ESA", domain.agencyAbbrev)
        assertEquals("Germany", domain.nationality.single().name)
    }

    @Test
    fun detailIsEmptyWhenCodesAbsent() {
        val domain = AstronautDetail(id = 1, name = "Jane Doe").toDomainDetail()
        assertNull(domain.agencyAbbrev)
        assertTrue(domain.nationality.isEmpty())
    }
}
