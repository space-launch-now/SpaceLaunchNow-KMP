package me.calebjones.spacelaunchnow.domain.mapper.trantor

import kotlin.time.Instant
import me.calebjones.spacelaunchnow.api.trantor.models.AgencyList
import me.calebjones.spacelaunchnow.api.trantor.models.AstronautList
import me.calebjones.spacelaunchnow.api.trantor.models.EventDetail
import me.calebjones.spacelaunchnow.api.trantor.models.Expedition
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchList
import me.calebjones.spacelaunchnow.api.trantor.models.ProgramList
import me.calebjones.spacelaunchnow.api.trantor.models.StationList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * E7(P5): `EventDetail` embeds one level of each related resource's own list row -
 * `launches[]`, `programs[]`, `space_stations[]`, `agencies[]`, `astronauts[]`, `expeditions[]`.
 * The junctions may still read back empty on an unbackfilled environment - that is a sync
 * state, never faked here (so the "absent" case is covered too).
 */
class EventMappersTest {

    private fun testEventDetail(
        agencies: List<AgencyList>? = null,
        astronauts: List<AstronautList>? = null,
        expeditions: List<Expedition>? = null,
        launches: List<LaunchList>? = null,
        programs: List<ProgramList>? = null,
        spaceStations: List<StationList>? = null
    ) = EventDetail(
        id = 1,
        name = "Static Fire Test",
        agencies = agencies,
        astronauts = astronauts,
        expeditions = expeditions,
        launches = launches,
        programs = programs,
        spaceStations = spaceStations
    )

    @Test
    fun embedsAreEmptyWhenTheJunctionsAreUnbackfilled() {
        val domain = testEventDetail().toDomain()
        assertTrue(domain.agencies.isEmpty())
        assertTrue(domain.astronauts.isEmpty())
        assertTrue(domain.expeditions.isEmpty())
        assertTrue(domain.launches.isEmpty())
        assertTrue(domain.programs.isEmpty())
        assertTrue(domain.spaceStations.isEmpty())
    }

    @Test
    fun embedsAreMappedWhenPresent() {
        val domain = testEventDetail(
            agencies = listOf(AgencyList(abbrev = "SpX", id = 44, name = "SpaceX")),
            astronauts = listOf(
                AstronautList(id = 5, name = "Jane Doe", nationality = listOf("USA"), status = "Active")
            ),
            expeditions = listOf(Expedition(id = 7, name = "Expedition 70", start = Instant.parse("2026-01-01T00:00:00Z"))),
            launches = listOf(
                LaunchList(
                    id = "launch-1",
                    name = "Falcon 9 | Starlink",
                    net = Instant.parse("2026-01-01T00:00:00Z"),
                    slug = "falcon-9-starlink",
                    status = "Go",
                    statusId = 1
                )
            ),
            programs = listOf(ProgramList(id = 2, name = "Starlink")),
            spaceStations = listOf(StationList(id = 4, name = "ISS", orbit = "LEO"))
        ).toDomain()

        assertEquals(1, domain.agencies.size)
        assertEquals("SpaceX", domain.agencies.first().name)
        assertEquals(1, domain.astronauts.size)
        assertEquals("Jane Doe", domain.astronauts.first().name)
        assertEquals("USA", domain.astronauts.first().nationality)
        assertEquals(1, domain.expeditions.size)
        assertEquals("Expedition 70", domain.expeditions.first().name)
        assertEquals(1, domain.launches.size)
        assertEquals("launch-1", domain.launches.first().id)
        assertEquals(1, domain.programs.size)
        assertEquals("Starlink", domain.programs.first().name)
        assertEquals(1, domain.spaceStations.size)
        assertEquals("ISS", domain.spaceStations.first().name)
        assertEquals("LEO", domain.spaceStations.first().orbit)
    }
}
