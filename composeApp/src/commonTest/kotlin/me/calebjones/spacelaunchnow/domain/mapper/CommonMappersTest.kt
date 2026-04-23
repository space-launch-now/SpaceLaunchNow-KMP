package me.calebjones.spacelaunchnow.domain.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CommonMappersTest {

    @Test
    fun agencyMiniMapsToProvider() {
        val api = testAgencyMini(id = 44, name = "SpaceX", abbrev = "SpX")
        val domain = api.toDomain()
        assertEquals(44, domain.id)
        assertEquals("SpaceX", domain.name)
        assertEquals("SpX", domain.abbrev)
        assertEquals("Government", domain.type)
        assertNull(domain.countryCode)
        assertNull(domain.logoUrl)
        assertNull(domain.imageUrl)
    }

    @Test
    fun launchStatusMaps() {
        val api = testLaunchStatus(id = 1, name = "Go for Launch", abbrev = "Go")
        val domain = api.toDomain()
        assertEquals(1, domain.id)
        assertEquals("Go for Launch", domain.name)
        assertEquals("Go", domain.abbrev)
    }

    @Test
    fun netPrecisionMaps() {
        val api = testNetPrecision(id = 2, name = "Hour", abbrev = "HR")
        val domain = api.toDomain()
        assertEquals(2, domain.id)
        assertEquals("Hour", domain.name)
        assertEquals("HR", domain.abbrev)
    }

    @Test
    fun countryCodeUsesAlpha2First() {
        val country = testCountry(alpha2Code = "US")
        assertEquals("US", country.toCountryCode())
    }

    @Test
    fun countryCodeFallsToAlpha3() {
        val country = testCountry(alpha2Code = null).copy(alpha3Code = "USA")
        assertEquals("USA", country.toCountryCode())
    }

    @Test
    fun countryCodeFallsToName() {
        val country = testCountry(alpha2Code = null).copy(alpha3Code = null, name = "France")
        assertEquals("France", country.toCountryCode())
    }

    @Test
    fun padDetailedMaps() {
        val api = testPadDetailed(id = 39, name = "LC-39A", latitude = 28.6, longitude = -80.6)
        val domain = api.toDomain()
        assertEquals(39, domain.id)
        assertEquals("LC-39A", domain.name)
        assertEquals(28.6, domain.latitude)
        assertEquals(-80.6, domain.longitude)
        assertEquals(50, domain.totalLaunchCount)
        assertNotNull(domain.location)
        assertEquals("Kennedy Space Center, FL, USA", domain.location?.name)
    }

    @Test
    fun locationListMaps() {
        val api = testLocationList(id = 10, name = "Cape Canaveral")
        val domain = api.toDomain()
        assertEquals(10, domain.id)
        assertEquals("Cape Canaveral", domain.name)
        assertEquals("US", domain.countryCode)
    }

    @Test
    fun missionMaps() {
        val api = testMission(
            id = 5,
            name = "Starlink 6-1",
            type = "Communications",
            description = "60 satellites"
        )
        val domain = api.toDomain()
        assertEquals(5, domain.id)
        assertEquals("Starlink 6-1", domain.name)
        assertEquals("Communications", domain.type)
        assertEquals("60 satellites", domain.description)
        assertNotNull(domain.orbit)
        assertEquals("LEO", domain.orbit?.abbrev)
    }

    @Test
    fun orbitMaps() {
        val api = testOrbit(id = 1, name = "Low Earth Orbit", abbrev = "LEO")
        val domain = api.toDomain()
        assertEquals(1, domain.id)
        assertEquals("Low Earth Orbit", domain.name)
        assertEquals("LEO", domain.abbrev)
    }

    @Test
    fun vidURLMaps() {
        val api = testVidURL(
            url = "https://youtube.com/watch?v=abc",
            title = "Live Stream",
            live = true
        )
        val domain = api.toDomain()
        assertEquals("https://youtube.com/watch?v=abc", domain.url)
        assertEquals("Live Stream", domain.title)
        assertEquals(true, domain.live)
    }

    @Test
    fun vidURLLiveDefaultsToFalse() {
        val api = testVidURL(live = null)
        val domain = api.toDomain()
        assertEquals(false, domain.live)
    }

    @Test
    fun infoURLMaps() {
        val api = testInfoURL(url = "https://example.com", title = "Info")
        val domain = api.toDomain()
        assertEquals("https://example.com", domain.url)
        assertEquals("Info", domain.title)
    }

    @Test
    fun updateMaps() {
        val api = testUpdate(id = 7, comment = "All systems go")
        val domain = api.toDomain()
        assertEquals(7, domain.id)
        assertEquals("All systems go", domain.comment)
        assertEquals("Admin", domain.createdBy)
    }

    @Test
    fun timelineEventMaps() {
        val api = testTimelineEvent()
        val domain = api.toDomain()
        assertEquals("LIFTOFF", domain.type)
        assertEquals("T+0s", domain.relativeTime)
    }

    @Test
    fun missionPatchMaps() {
        val api = testMissionPatch(id = 1, name = "Patch", imageUrl = "https://example.com/p.png")
        val domain = api.toDomain()
        assertEquals(1, domain.id)
        assertEquals("Patch", domain.name)
        assertEquals("https://example.com/p.png", domain.imageUrl)
    }

    @Test
    fun programMiniMaps() {
        val api = testProgramMini(id = 3, name = "ISS")
        val domain = api.toDomain()
        assertEquals(3, domain.id)
        assertEquals("ISS", domain.name)
        assertNull(domain.description)
        assertNull(domain.type)
    }

    @Test
    fun launcherConfigListMaps() {
        val api = testLauncherConfigList(id = 1, name = "Falcon 9", fullName = "Falcon 9 Block 5")
        val domain = api.toDomain()
        assertEquals(1, domain.id)
        assertEquals("Falcon 9", domain.name)
        assertEquals("Falcon 9 Block 5", domain.fullName)
        assertEquals("Falcon", domain.family)
        assertEquals("Block 5", domain.variant)
        assertNull(domain.imageUrl)
        assertNull(domain.active)
        assertNull(domain.reusable)
    }
}
