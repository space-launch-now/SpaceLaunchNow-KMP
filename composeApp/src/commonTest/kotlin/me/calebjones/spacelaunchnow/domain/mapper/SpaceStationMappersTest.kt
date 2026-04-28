package me.calebjones.spacelaunchnow.domain.mapper

import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class SpaceStationMappersTest {

    @Test
    fun spaceStationDetailedEndpointToDomainMapsAllFields() {
        val expedition = ExpeditionMini(
            id = 70,
            url = "https://example.com/expedition/70/",
            name = "Expedition 70",
            start = Instant.parse("2023-09-27T00:00:00Z"),
            end = null
        )
        val api = SpaceStationDetailedEndpoint(
            id = 1,
            url = "https://example.com/station/1/",
            name = "ISS",
            image = testImage(),
            status = SpaceStationStatus(id = 1, name = "Active"),
            founded = LocalDate(1998, 11, 20),
            deorbited = false,
            description = "International Space Station",
            orbit = "LEO",
            type = SpaceStationType(id = 1, name = "Space Station"),
            owners = listOf(testAgencyNormal()),
            activeExpeditions = listOf(expedition),
            dockingLocation = emptyList(),
            activeDockingEvents = emptyList(),
            height = 109.0,
            width = 73.0,
            mass = 420000.0,
            volume = 916,
            onboardCrew = 7,
            dockedVehicles = 3
        )
        val domain = api.toDomain()
        assertEquals(1, domain.id)
        assertEquals("ISS", domain.name)
        assertEquals("Active", domain.statusName)
        assertEquals("LEO", domain.orbit)
        assertEquals(1, domain.owners.size)
        assertEquals("SpaceX", domain.owners.first().name)
        assertEquals(1, domain.activeExpeditions.size)
        assertEquals("Expedition 70", domain.activeExpeditions.first().name)
        assertEquals(916.0, domain.volume)
        assertEquals(7, domain.onboardCrew)
        assertTrue(domain.dockingLocations.isEmpty())
    }
}
