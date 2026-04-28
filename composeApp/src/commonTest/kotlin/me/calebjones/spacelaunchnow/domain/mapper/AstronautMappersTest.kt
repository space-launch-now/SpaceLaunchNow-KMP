package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AstronautMappersTest {

    @Test
    fun astronautEndpointNormalToDomainListItemMapsAllFields() {
        val api = AstronautEndpointNormal(
            id = 99,
            url = "https://example.com/astronaut/99/",
            name = "Jane Doe",
            status = AstronautStatus(id = 1, name = "Active"),
            agency = testAgencyMini(id = 44, name = "NASA", abbrev = "NASA"),
            image = testImage(),
            age = 45,
            bio = "Test astronaut",
            type = AstronautType(id = 1, name = "Government"),
            nationality = listOf(testCountry())
        )
        val domain = api.toDomainListItem()
        assertEquals(99, domain.id)
        assertEquals("Jane Doe", domain.name)
        assertEquals("Active", domain.statusName)
        assertEquals(1, domain.statusId)
        assertEquals("NASA", domain.agencyName)
        assertEquals(44, domain.agencyId)
        assertEquals(45, domain.age)
        assertEquals("Government", domain.typeName)
        assertEquals("https://example.com/image.jpg", domain.imageUrl)
        assertEquals(1, domain.nationality.size)
    }

    @Test
    fun astronautEndpointNormalToDomainListItemHandlesNullName() {
        val api = AstronautEndpointNormal(
            id = 100,
            url = "https://example.com/astronaut/100/",
            name = null,
            status = null,
            agency = null,
            image = null,
            age = null,
            bio = "",
            type = AstronautType(id = 2, name = "Private"),
            nationality = emptyList()
        )
        val domain = api.toDomainListItem()
        assertEquals("Unknown", domain.name)
        assertEquals(null, domain.statusName)
        assertEquals(null, domain.agencyName)
        assertTrue(domain.nationality.isEmpty())
    }
}
