package me.calebjones.spacelaunchnow.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpaceStationTest {

    @Test
    fun minimalSpaceStationDetail() {
        val station = SpaceStationDetail(
            id = 1,
            name = "International Space Station",
            imageUrl = null,
            statusName = "Active",
            statusId = 1,
            founded = LocalDate(1998, 11, 20),
            deorbited = null,
            description = null,
            orbit = "Low Earth Orbit",
            typeName = "Modular",
            owners = emptyList(),
            activeExpeditions = emptyList(),
            dockingLocations = emptyList(),
            height = null,
            width = null,
            mass = null,
            volume = null,
            onboardCrew = null,
            dockedVehicles = null
        )
        assertEquals("International Space Station", station.name)
        assertTrue(station.owners.isEmpty())
        assertTrue(station.activeExpeditions.isEmpty())
        assertTrue(station.dockingLocations.isEmpty())
        assertNull(station.deorbited)
    }

    @Test
    fun expeditionMiniItemConstruction() {
        val expedition = ExpeditionMiniItem(
            id = 71,
            name = "Expedition 71",
            start = Instant.parse("2024-04-06T00:00:00Z"),
            end = null
        )
        assertEquals("Expedition 71", expedition.name)
        assertNull(expedition.end)
    }

    @Test
    fun expeditionDetailItemDefaults() {
        val expedition = ExpeditionDetailItem(
            id = 1,
            name = "Expedition 1",
            start = Instant.parse("2000-10-31T00:00:00Z"),
            end = Instant.parse("2001-03-21T00:00:00Z"),
            crew = emptyList(),
            missionPatches = emptyList(),
            spacewalks = emptyList()
        )
        assertTrue(expedition.crew.isEmpty())
        assertTrue(expedition.missionPatches.isEmpty())
        assertTrue(expedition.spacewalks.isEmpty())
    }

    @Test
    fun dockingLocationAndEvent() {
        val event = DockingEvent(
            id = 1,
            docking = Instant.parse("2024-03-04T00:00:00Z"),
            departure = null,
            vehicleName = "Crew Dragon Endeavour",
            vehicleConfigName = "Crew Dragon",
            vehicleImageUrl = null
        )
        val location = DockingLocation(
            id = 1,
            name = "Harmony Forward",
            currentlyDocked = event
        )
        assertEquals("Harmony Forward", location.name)
        assertEquals("Crew Dragon Endeavour", location.currentlyDocked?.vehicleName)
        assertNull(location.currentlyDocked?.departure)
    }
}
