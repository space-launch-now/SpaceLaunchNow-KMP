package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AgencyList
import me.calebjones.spacelaunchnow.api.trantor.models.StationDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * D4 (E12(P5)): `StationDetail.owners[]` (the agencies list row) is preferred; the
 * `owner_ids`/`owner_names` parallel lists are a fallback kept for one release
 * (`phase5-owners-cleanup`).
 */
class SpaceStationMappersTest {

    private fun testStationDetail(
        owners: List<AgencyList>? = null,
        ownerIds: List<Int>? = null,
        ownerNames: List<String>? = null
    ) = StationDetail(
        id = 4,
        name = "ISS",
        owners = owners,
        ownerIds = ownerIds,
        ownerNames = ownerNames
    )

    @Test
    fun ownersRowsAreUsedWhenPresent() {
        val owners = listOf(
            AgencyList(
                abbrev = "NASA",
                id = 44,
                name = "National Aeronautics and Space Administration",
                logoUrl = "https://example.com/nasa-logo.png",
                socialLogoUrl = "https://example.com/nasa-social.png"
            )
        )
        val domain = testStationDetail(
            owners = owners,
            ownerIds = listOf(99),
            ownerNames = listOf("Stale Fallback Agency")
        ).toDomain()

        assertEquals(1, domain.owners.size)
        assertEquals(44, domain.owners.first().id)
        assertEquals("https://example.com/nasa-social.png", domain.owners.first().socialLogoUrl)
    }

    @Test
    fun ownerIdsAndNamesAreUsedWhenOwnersIsAbsent() {
        val domain = testStationDetail(
            owners = null,
            ownerIds = listOf(44, 27),
            ownerNames = listOf("NASA", "Roscosmos")
        ).toDomain()

        assertEquals(2, domain.owners.size)
        assertEquals(44, domain.owners[0].id)
        assertEquals("NASA", domain.owners[0].name)
        assertNull(domain.owners[0].logoUrl)
    }

    @Test
    fun ownerIdsAndNamesAreUsedWhenOwnersIsEmpty() {
        val domain = testStationDetail(
            owners = emptyList(),
            ownerIds = listOf(44),
            ownerNames = listOf("NASA")
        ).toDomain()

        assertTrue(domain.owners.isNotEmpty())
        assertEquals(44, domain.owners.first().id)
    }
}
