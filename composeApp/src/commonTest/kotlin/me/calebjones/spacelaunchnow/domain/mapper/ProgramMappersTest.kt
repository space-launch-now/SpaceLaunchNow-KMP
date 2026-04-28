package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ProgramMappersTest {

    @Test
    fun programNormalToDomainProgramMapsAllFields() {
        val api = ProgramNormal(
            id = 42,
            url = "https://example.com/program/42/",
            name = "Artemis",
            image = testImage(),
            infoUrl = "https://nasa.gov/artemis",
            wikiUrl = "https://wikipedia.org/wiki/Artemis_program",
            description = "Return to the Moon",
            agencies = listOf(testAgencyMini(id = 44, name = "NASA", abbrev = "NASA")),
            startDate = Instant.parse("2017-12-11T00:00:00Z"),
            endDate = null,
            missionPatches = emptyList(),
            type = ProgramType(id = 1, name = "Crewed"),
            vidUrls = emptyList()
        )
        val domain = api.toDomainProgram()
        assertEquals(42, domain.id)
        assertEquals("Artemis", domain.name)
        assertEquals("Return to the Moon", domain.description)
        assertEquals("Crewed", domain.type)
        assertEquals(Instant.parse("2017-12-11T00:00:00Z"), domain.startDate)
        assertEquals(null, domain.endDate)
        assertEquals(1, domain.agencies.size)
        assertEquals("NASA", domain.agencies.first().name)
    }
}
