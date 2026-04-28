package me.calebjones.spacelaunchnow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LaunchTest {

    private fun minimalLaunch(id: String = "test-id") = Launch(
        id = id,
        name = "Test Launch",
        slug = "test-launch",
        net = null,
        windowStart = null,
        windowEnd = null,
        lastUpdated = null,
        status = null,
        provider = Provider(id = 1, name = "SpaceX", abbrev = "SpX", type = null, countryCode = null, logoUrl = null, imageUrl = null),
        imageUrl = null,
        thumbnailUrl = null,
        infographic = null,
        netPrecision = null
    )

    @Test
    fun constructionWithMinimalFields() {
        val launch = minimalLaunch()
        assertEquals("test-id", launch.id)
        assertEquals("Test Launch", launch.name)
    }

    @Test
    fun normalFieldsDefaultCorrectly() {
        val launch = minimalLaunch()
        assertNull(launch.rocket)
        assertNull(launch.mission)
        assertNull(launch.pad)
        assertTrue(launch.programs.isEmpty())
        assertNull(launch.probability)
        assertNull(launch.weatherConcerns)
        assertNull(launch.failreason)
        assertNull(launch.hashtag)
        assertEquals(false, launch.webcastLive)
        assertNull(launch.launchAttemptCounts)
    }

    @Test
    fun detailedFieldsDefaultCorrectly() {
        val launch = minimalLaunch()
        assertTrue(launch.updates.isEmpty())
        assertTrue(launch.infoUrls.isEmpty())
        assertTrue(launch.vidUrls.isEmpty())
        assertTrue(launch.timeline.isEmpty())
        assertTrue(launch.missionPatches.isEmpty())
        assertNull(launch.rocketDetail)
        assertNull(launch.flightclubUrl)
        assertNull(launch.padTurnaround)
        assertNull(launch.providerDetail)
    }

    @Test
    fun copyPreservesAndOverrides() {
        val launch = minimalLaunch(id = "original")
        val copied = launch.copy(id = "copied", name = "Copied Launch")
        assertEquals("original", launch.id)
        assertEquals("copied", copied.id)
        assertEquals("Copied Launch", copied.name)
        assertEquals(launch.provider, copied.provider)
    }
}
