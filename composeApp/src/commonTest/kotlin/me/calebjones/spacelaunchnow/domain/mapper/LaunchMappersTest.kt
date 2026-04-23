package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LaunchMappersTest {

    @Test
    fun launchBasicMapsBasicFields() {
        val api = testLaunchBasic(
            id = "uuid-1",
            name = "SpaceX | Falcon 9",
            slug = "spacex-falcon-9"
        )
        val domain = api.toDomain()
        assertEquals("uuid-1", domain.id)
        assertEquals("SpaceX | Falcon 9", domain.name)
        assertEquals("spacex-falcon-9", domain.slug)
        assertEquals(44, domain.provider.id)
        assertEquals("SpaceX", domain.provider.name)
    }

    @Test
    fun launchBasicFlattensImageUrl() {
        val api = testLaunchBasic()
        val domain = api.toDomain()
        assertEquals("https://example.com/image.jpg", domain.imageUrl)
        assertEquals("https://example.com/thumb.jpg", domain.thumbnailUrl)
    }

    @Test
    fun launchBasicMapsStatus() {
        val api = testLaunchBasic()
        val domain = api.toDomain()
        assertNotNull(domain.status)
        assertEquals(1, domain.status?.id)
        assertEquals("Go for Launch", domain.status?.name)
    }

    @Test
    fun launchBasicNormalFieldsAreNull() {
        val api = testLaunchBasic()
        val domain = api.toDomain()
        assertNull(domain.rocket)
        assertNull(domain.mission)
        assertNull(domain.probability)
        assertEquals(false, domain.webcastLive)
        assertNull(domain.launchAttemptCounts)
    }

    @Test
    fun launchBasicDetailedFieldsAreEmpty() {
        val api = testLaunchBasic()
        val domain = api.toDomain()
        assertTrue(domain.updates.isEmpty())
        assertTrue(domain.infoUrls.isEmpty())
        assertTrue(domain.vidUrls.isEmpty())
        assertTrue(domain.timeline.isEmpty())
        assertTrue(domain.missionPatches.isEmpty())
        assertNull(domain.rocketDetail)
        assertNull(domain.flightclubUrl)
        assertNull(domain.providerDetail)
    }

    @Test
    fun launchBasicCreatesPadFromLocationName() {
        val api = testLaunchBasic(locationName = "Cape Canaveral, FL")
        val domain = api.toDomain()
        assertNotNull(domain.pad)
        assertEquals("Cape Canaveral, FL", domain.pad?.location?.name)
    }

    @Test
    fun launchBasicPadNullWhenNoLocationName() {
        val api = testLaunchBasic(locationName = null)
        val domain = api.toDomain()
        assertNull(domain.pad)
    }

    @Test
    fun launchNormalMapsRocketAndMission() {
        val api = LaunchNormal(
            id = "uuid-2",
            url = "https://example.com/launch/uuid-2/",
            slug = "test-launch",
            launchDesignator = null,
            status = testLaunchStatus(),
            netPrecision = testNetPrecision(),
            image = testImage(),
            launchServiceProvider = testAgencyNormal(),
            infographic = null,
            rocket = testRocketNormal(),
            mission = testMission(),
            pad = testPadDetailed(),
            program = listOf(testProgramMini()),
            orbitalLaunchAttemptCount = 100,
            locationLaunchAttemptCount = 50,
            padLaunchAttemptCount = 30,
            agencyLaunchAttemptCount = 200,
            orbitalLaunchAttemptCountYear = 10,
            locationLaunchAttemptCountYear = 5,
            padLaunchAttemptCountYear = 3,
            agencyLaunchAttemptCountYear = 20,
            name = "SpaceX | Falcon 9 Block 5",
            webcastLive = true,
            probability = 90,
            weatherConcerns = "None",
            failreason = null,
            hashtag = "#SpaceX"
        )
        val domain = api.toDomain()
        assertNotNull(domain.rocket)
        assertEquals("Falcon 9 Block 5", domain.rocket?.name)
        assertNotNull(domain.mission)
        assertEquals("Starlink Group 6-1", domain.mission?.name)
        assertNotNull(domain.pad)
        assertEquals("Launch Complex 39A", domain.pad?.name)
        assertEquals(1, domain.programs.size)
        assertEquals(true, domain.webcastLive)
        assertEquals(90, domain.probability)
        assertEquals("#SpaceX", domain.hashtag)
    }

    @Test
    fun launchNormalMapsAttemptCounts() {
        val api = LaunchNormal(
            id = "uuid-3",
            url = "https://example.com/launch/uuid-3/",
            slug = "test-launch-2",
            launchDesignator = null,
            status = testLaunchStatus(),
            netPrecision = null,
            image = null,
            launchServiceProvider = testAgencyNormal(),
            infographic = null,
            probability = null,
            weatherConcerns = null,
            failreason = null,
            hashtag = null,
            rocket = null,
            mission = null,
            pad = null,
            program = null,
            orbitalLaunchAttemptCount = 100,
            locationLaunchAttemptCount = 50,
            padLaunchAttemptCount = 30,
            agencyLaunchAttemptCount = 200,
            orbitalLaunchAttemptCountYear = 10,
            locationLaunchAttemptCountYear = 5,
            padLaunchAttemptCountYear = 3,
            agencyLaunchAttemptCountYear = 20,
            name = "Test Launch"
        )
        val domain = api.toDomain()
        assertNotNull(domain.launchAttemptCounts)
        assertEquals(100, domain.launchAttemptCounts?.orbital)
        assertEquals(50, domain.launchAttemptCounts?.location)
        assertEquals(30, domain.launchAttemptCounts?.pad)
        assertEquals(200, domain.launchAttemptCounts?.agency)
    }

    @Test
    fun launchNormalDetailedFieldsAreEmpty() {
        val api = LaunchNormal(
            id = "uuid-4",
            url = "https://example.com/launch/uuid-4/",
            slug = "test-launch-3",
            launchDesignator = null,
            status = testLaunchStatus(),
            netPrecision = null,
            image = null,
            launchServiceProvider = testAgencyNormal(),
            infographic = null,
            probability = null,
            weatherConcerns = null,
            failreason = null,
            hashtag = null,
            rocket = null,
            mission = null,
            pad = null,
            program = null,
            orbitalLaunchAttemptCount = null,
            locationLaunchAttemptCount = null,
            padLaunchAttemptCount = null,
            agencyLaunchAttemptCount = null,
            orbitalLaunchAttemptCountYear = null,
            locationLaunchAttemptCountYear = null,
            padLaunchAttemptCountYear = null,
            agencyLaunchAttemptCountYear = null,
            name = "Test"
        )
        val domain = api.toDomain()
        assertTrue(domain.updates.isEmpty())
        assertTrue(domain.infoUrls.isEmpty())
        assertTrue(domain.vidUrls.isEmpty())
        assertNull(domain.rocketDetail)
        assertNull(domain.providerDetail)
    }

    @Test
    fun paginatedLaunchBasicListMaps() {
        val api = PaginatedLaunchBasicList(
            count = 2,
            results = listOf(testLaunchBasic(id = "a"), testLaunchBasic(id = "b")),
            next = "https://example.com/next",
            previous = null
        )
        val domain = api.toDomain()
        assertEquals(2, domain.count)
        assertEquals(2, domain.results.size)
        assertEquals("a", domain.results[0].id)
        assertEquals("b", domain.results[1].id)
        assertEquals("https://example.com/next", domain.next)
        assertNull(domain.previous)
    }

    @Test
    fun launchBasicNameDefaultsToEmptyString() {
        val api = testLaunchBasic(name = null)
        val domain = api.toDomain()
        assertEquals("", domain.name)
    }
}
