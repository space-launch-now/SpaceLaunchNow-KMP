package me.calebjones.spacelaunchnow.domain.mapper

import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpacecraftMappersTest {

    private fun testSpacecraftConfig() = SpacecraftConfigDetailed(
        id = 10,
        url = "https://example.com/spacecraft/config/10/",
        name = "Crew Dragon",
        type = SpacecraftConfigType(id = 1, name = "Crewed"),
        agency = testAgencyNormal(),
        family = emptyList(),
        image = testImage(),
        fastestTurnaround = null,
        inUse = true,
        capability = "Low Earth Orbit crew transport",
        history = "First flight in 2020",
        details = "Operational NASA Commercial Crew vehicle",
        maidenFlight = LocalDate(2020, 5, 30),
        humanRated = true,
        crewCapacity = 7,
        payloadCapacity = 6000
    )

    @Test
    fun spacecraftConfigDetailedToDomainMapsAllFields() {
        val domain = testSpacecraftConfig().toDomain()
        assertEquals(10, domain.id)
        assertEquals("Crew Dragon", domain.name)
        assertEquals("Crewed", domain.type)
        assertEquals("SpaceX", domain.agency?.name)
        assertEquals(true, domain.humanRated)
        assertEquals(7, domain.crewCapacity)
        assertEquals(LocalDate(2020, 5, 30), domain.maidenFlight)
    }

    @Test
    fun spacecraftEndpointDetailedToDomainIncludesConfig() {
        val api = SpacecraftEndpointDetailed(
            id = 200,
            url = "https://example.com/spacecraft/200/",
            name = "Dragon Endeavour",
            serialNumber = "C206",
            image = null,
            timeInSpace = "P300D",
            timeDocked = "P250D",
            flightsCount = 5,
            missionEndsCount = 5,
            status = SpacecraftStatus(id = 1, name = "Active"),
            description = "Reused Crew Dragon capsule",
            spacecraftConfig = testSpacecraftConfig(),
            fastestTurnaround = "60 days",
            flights = emptyList(),
            isPlaceholder = false,
            inSpace = false
        )
        val domain = api.toDomain()
        assertEquals(200, domain.id)
        assertEquals("Dragon Endeavour", domain.name)
        assertEquals("C206", domain.serialNumber)
        assertEquals("Active", domain.status?.name)
        assertNotNull(domain.config)
        assertEquals("Crew Dragon", domain.config?.name)
        assertTrue(domain.config?.humanRated == true)
    }
}
