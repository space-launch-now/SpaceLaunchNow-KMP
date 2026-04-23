package me.calebjones.spacelaunchnow.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AstronautTest {

    @Test
    fun astronautListItemConstruction() {
        val item = AstronautListItem(
            id = 1,
            name = "Neil Armstrong",
            statusName = "Deceased",
            statusId = 2,
            agencyName = "NASA",
            agencyAbbrev = "NASA",
            agencyId = 44,
            imageUrl = null,
            thumbnailUrl = null,
            age = 82,
            bio = "First person on the Moon",
            typeName = "Government",
            nationality = emptyList()
        )
        assertEquals("Neil Armstrong", item.name)
        assertEquals(82, item.age)
        assertTrue(item.nationality.isEmpty())
    }

    @Test
    fun astronautDetailWithDefaults() {
        val detail = AstronautDetail(
            id = 2,
            name = "Chris Hadfield",
            statusName = "Retired",
            statusId = 3,
            agencyName = "CSA",
            agencyAbbrev = "CSA",
            agencyId = 10,
            imageUrl = null,
            thumbnailUrl = null,
            age = 64,
            bio = null,
            typeName = "Government",
            nationality = emptyList(),
            inSpace = false,
            timeInSpace = "P166D",
            evaTime = "PT14H54M",
            dateOfBirth = LocalDate(1959, 8, 29),
            dateOfDeath = null,
            wikiUrl = null,
            lastFlight = Instant.parse("2013-05-14T00:00:00Z"),
            firstFlight = Instant.parse("1995-11-12T00:00:00Z"),
            socialMediaLinks = emptyList(),
            flightsCount = 3,
            landingsCount = 3,
            spacewalksCount = 2,
            flights = emptyList(),
            landings = emptyList(),
            spacewalks = emptyList()
        )
        assertEquals(3, detail.flightsCount)
        assertNull(detail.dateOfDeath)
        assertTrue(detail.socialMediaLinks.isEmpty())
    }

    @Test
    fun socialMediaLinkConstruction() {
        val link = SocialMediaLink(
            id = 1,
            url = "https://twitter.com/cmdrhadfield",
            platformName = "Twitter",
            platformLogoUrl = null
        )
        assertEquals("Twitter", link.platformName)
    }

    @Test
    fun spacewalkSummaryConstruction() {
        val sw = SpacewalkSummary(
            id = 1,
            name = "EVA-1",
            start = Instant.parse("2020-01-15T12:00:00Z"),
            end = Instant.parse("2020-01-15T18:00:00Z"),
            duration = "PT6H"
        )
        assertEquals("EVA-1", sw.name)
        assertEquals("PT6H", sw.duration)
    }
}
