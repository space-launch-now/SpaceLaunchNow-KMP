package me.calebjones.spacelaunchnow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommonTypesTest {

    @Test
    fun providerConstruction() {
        val p = Provider(id = 1, name = "NASA", abbrev = "NASA", type = "Government", countryCode = "US", logoUrl = null, imageUrl = null)
        assertEquals(1, p.id)
        assertEquals("NASA", p.name)
        assertEquals("Government", p.type)
    }

    @Test
    fun rocketConfigConstruction() {
        val rc = RocketConfig(id = 1, name = "Falcon 9", fullName = "Falcon 9 Block 5", family = "Falcon", variant = "Block 5", imageUrl = null, active = true, reusable = true)
        assertEquals("Falcon 9", rc.name)
        assertEquals(true, rc.active)
        assertEquals(true, rc.reusable)
    }

    @Test
    fun padWithNullLocation() {
        val pad = Pad(id = 1, name = "Pad A", latitude = null, longitude = null, mapUrl = null, mapImage = null, totalLaunchCount = null, location = null)
        assertNull(pad.location)
        assertNull(pad.latitude)
    }

    @Test
    fun missionWithOrbit() {
        val orbit = Orbit(id = 1, name = "LEO", abbrev = "LEO")
        val mission = Mission(id = 1, name = "Starlink", description = null, type = "Comm", orbit = orbit, imageUrl = null)
        assertEquals("LEO", mission.orbit?.abbrev)
    }

    @Test
    fun launchStatusFields() {
        val ls = LaunchStatus(id = 1, name = "Go for Launch", abbrev = "Go", description = "All systems go")
        assertEquals("Go for Launch", ls.name)
        assertEquals("Go", ls.abbrev)
    }

    @Test
    fun paginatedResultDefaultsToEmptyList() {
        val pr = PaginatedResult<String>(count = 0, next = null, previous = null)
        assertTrue(pr.results.isEmpty())
        assertEquals(0, pr.count)
    }

    @Test
    fun paginatedResultWithContent() {
        val pr = PaginatedResult(count = 2, next = "url", previous = null, results = listOf("a", "b"))
        assertEquals(2, pr.results.size)
        assertEquals("url", pr.next)
    }

    @Test
    fun videoLinkFields() {
        val vl = VideoLink(url = "https://yt.com/v", title = "T", source = "S", description = null, featureImage = null, live = true, priority = 1)
        assertEquals(true, vl.live)
        assertEquals(1, vl.priority)
    }

    @Test
    fun infoLinkFields() {
        val il = InfoLink(url = "https://info.com", title = "T", source = null, description = null, featureImage = null, type = "Article", priority = null)
        assertEquals("Article", il.type)
        assertNull(il.priority)
    }

    @Test
    fun timelineEntryFields() {
        val te = TimelineEntry(type = "LIFTOFF", relativeTime = "T+0s")
        assertEquals("LIFTOFF", te.type)
    }

    @Test
    fun updateFields() {
        val u = Update(id = 1, profileImage = null, comment = "test", infoUrl = null, createdBy = "admin", createdOn = null)
        assertEquals("test", u.comment)
        assertNull(u.createdOn)
    }

    @Test
    fun programSummaryFields() {
        val ps = ProgramSummary(id = 1, name = "ISS", imageUrl = null, description = "desc", infoUrl = null, wikiUrl = null, type = "Human Spaceflight")
        assertEquals("Human Spaceflight", ps.type)
    }

    @Test
    fun launchAttemptCountsAllNullable() {
        val lac = LaunchAttemptCounts(orbital = null, location = null, pad = null, agency = null, orbitalYear = null, locationYear = null, padYear = null, agencyYear = null)
        assertNull(lac.orbital)
    }
}
