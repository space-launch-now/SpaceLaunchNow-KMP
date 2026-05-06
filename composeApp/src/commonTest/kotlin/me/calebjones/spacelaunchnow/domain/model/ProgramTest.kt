package me.calebjones.spacelaunchnow.domain.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProgramTest {

    @Test
    fun minimalProgramConstruction() {
        val program = Program(
            id = 1,
            name = "Artemis"
        )
        assertEquals("Artemis", program.name)
        assertNull(program.description)
        assertNull(program.startDate)
        assertTrue(program.agencies.isEmpty())
        assertTrue(program.missionPatches.isEmpty())
    }

    @Test
    fun fullyPopulatedProgram() {
        val nasa = Provider(
            id = 44,
            name = "National Aeronautics and Space Administration",
            abbrev = "NASA",
            type = "Government",
            countryCode = "USA",
            logoUrl = null,
            socialLogo = null,
            imageUrl = null
        )
        val patch = MissionPatchSummary(
            id = 5,
            name = "Artemis I Patch",
            imageUrl = "https://example.com/patch.png",
            priority = 1
        )
        val program = Program(
            id = 22,
            name = "Artemis",
            description = "NASA's program to return humans to the Moon",
            imageUrl = "https://example.com/artemis.png",
            infoUrl = "https://nasa.gov/artemis",
            wikiUrl = "https://en.wikipedia.org/wiki/Artemis_program",
            type = "Exploration",
            startDate = Instant.parse("2017-12-11T00:00:00Z"),
            endDate = null,
            agencies = listOf(nasa),
            missionPatches = listOf(patch)
        )
        assertEquals("Exploration", program.type)
        assertEquals(1, program.agencies.size)
        assertEquals("NASA", program.agencies.first().abbrev)
        assertEquals(1, program.missionPatches.size)
        assertNull(program.endDate)
    }

    @Test
    fun programDefaultsForListsAreEmpty() {
        val program = Program(id = 1, name = "Apollo")
        assertTrue(program.agencies.isEmpty())
        assertTrue(program.missionPatches.isEmpty())
    }
}
