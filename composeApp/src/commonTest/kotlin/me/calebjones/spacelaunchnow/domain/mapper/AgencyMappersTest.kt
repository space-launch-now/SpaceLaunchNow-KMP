package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AgencyMappersTest {

    private fun testAgencyEndpointDetailed() = AgencyEndpointDetailed(
        id = 121,
        url = "https://example.com/agency/121/",
        name = "SpaceX",
        type = testAgencyType(id = 2, name = "Commercial"),
        country = listOf(testCountry()),
        parent = null,
        image = testImage(),
        logo = testImage(id = 2, name = "logo", imageUrl = "https://example.com/logo.png"),
        socialLogo = null,
        totalLaunchCount = 300,
        consecutiveSuccessfulLaunches = 250,
        successfulLaunches = 295,
        failedLaunches = 5,
        pendingLaunches = 10,
        consecutiveSuccessfulLandings = 200,
        successfulLandings = 250,
        failedLandings = 10,
        attemptedLandings = 260,
        successfulLandingsSpacecraft = 0,
        failedLandingsSpacecraft = 0,
        attemptedLandingsSpacecraft = 0,
        successfulLandingsPayload = 0,
        failedLandingsPayload = 0,
        attemptedLandingsPayload = 0,
        infoUrl = "https://spacex.com",
        wikiUrl = "https://en.wikipedia.org/wiki/SpaceX",
        socialMediaLinks = emptyList(),
        launcherList = emptyList(),
        spacecraftList = emptyList(),
        abbrev = "SpX",
        featured = true,
        description = "Private launch provider",
        administrator = "Elon Musk",
        foundingYear = 2002
    )

    @Test
    fun agencyEndpointDetailedMapsAllFields() {
        val domain = testAgencyEndpointDetailed().toDomainAgency()
        assertEquals(121, domain.id)
        assertEquals("SpaceX", domain.name)
        assertEquals("SpX", domain.abbrev)
        assertEquals("Commercial", domain.typeName)
        assertEquals(1, domain.countries.size)
        assertEquals("US", domain.countries.first().alpha2Code)
        assertEquals("https://example.com/logo.png", domain.logoUrl)
        assertEquals("Private launch provider", domain.description)
        assertEquals("Elon Musk", domain.administrator)
        assertEquals(2002, domain.foundingYear)
    }

    @Test
    fun paginatedAgencyNormalListMapsResults() {
        val paginated = PaginatedAgencyNormalList(
            count = 1,
            next = null,
            previous = null,
            results = listOf(testAgencyNormal())
        )
        val domain = paginated.toDomain()
        assertEquals(1, domain.count)
        assertEquals(1, domain.results.size)
        val agency = domain.results.first()
        assertEquals("SpaceX", agency.name)
        assertNotNull(agency.countries)
    }
}
