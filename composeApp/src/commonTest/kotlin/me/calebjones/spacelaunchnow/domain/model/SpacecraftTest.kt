package me.calebjones.spacelaunchnow.domain.model

import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.domain.model.SpacecraftStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SpacecraftTest {

    @Test
    fun minimalSpacecraft() {
        val spacecraft = Spacecraft(
            id = 1,
            name = "Crew Dragon Endeavour",
            serialNumber = "C206"
        )
        assertEquals("Crew Dragon Endeavour", spacecraft.name)
        assertEquals("C206", spacecraft.serialNumber)
        assertNull(spacecraft.config)
        assertNull(spacecraft.status)
    }

    @Test
    fun spacecraftWithConfig() {
        val provider = Provider(
            id = 121,
            name = "SpaceX",
            abbrev = "SpX",
            type = "Commercial",
            countryCode = "US",
            logoUrl = null,
            socialLogo = null,
            imageUrl = null
        )
        val config = SpacecraftConfig(
            id = 5,
            name = "Crew Dragon",
            type = "Capsule",
            agency = provider,
            imageUrl = "https://example.com/dragon.png",
            inUse = true,
            capability = "Crewed ISS transport",
            history = "First crewed flight in 2020",
            details = "Reusable capsule",
            maidenFlight = LocalDate(2019, 3, 2),
            humanRated = true,
            crewCapacity = 7,
            payloadCapacity = 6000
        )
        val spacecraft = Spacecraft(
            id = 2,
            name = "Crew Dragon Resilience",
            serialNumber = "C207",
            status = SpacecraftStatus(id = 1, name = "active"),
            description = "Crew Dragon operational vehicle",
            imageUrl = "https://example.com/resilience.png",
            config = config
        )
        assertEquals("Crew Dragon", spacecraft.config?.name)
        assertEquals("SpaceX", spacecraft.config?.agency?.name)
        assertEquals(true, spacecraft.config?.humanRated)
        assertNotNull(spacecraft.config?.maidenFlight)
    }

    @Test
    fun spacecraftConfigDefaults() {
        val config = SpacecraftConfig(id = 1, name = "Starliner")
        assertNull(config.agency)
        assertNull(config.inUse)
        assertNull(config.crewCapacity)
    }
}
