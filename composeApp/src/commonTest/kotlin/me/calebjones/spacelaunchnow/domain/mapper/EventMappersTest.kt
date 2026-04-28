package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedEventEndpointNormalList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EventMappersTest {

    @Test
    fun eventEndpointNormalMapsBaseFields() {
        val api = testEventEndpointNormal(id = 5, name = "ISS Docking", slug = "iss-docking")
        val domain = api.toDomain()
        assertEquals(5, domain.id)
        assertEquals("ISS Docking", domain.name)
        assertEquals("iss-docking", domain.slug)
        assertEquals("PT1H30M", domain.duration)
    }

    @Test
    fun eventEndpointNormalMapsType() {
        val api = testEventEndpointNormal()
        val domain = api.toDomain()
        assertEquals(1, domain.type.id)
        assertEquals("Launch", domain.type.name)
    }

    @Test
    fun eventEndpointNormalMapsMediaLists() {
        val api = testEventEndpointNormal()
        val domain = api.toDomain()
        assertEquals(1, domain.infoUrls.size)
        assertEquals("https://example.com/info", domain.infoUrls[0].url)
        assertEquals(1, domain.vidUrls.size)
        assertEquals("https://youtube.com/watch?v=test", domain.vidUrls[0].url)
        assertEquals(1, domain.updates.size)
        assertEquals("Launch is on track", domain.updates[0].comment)
    }

    @Test
    fun eventEndpointNormalFlattensImageUrl() {
        val api = testEventEndpointNormal()
        val domain = api.toDomain()
        assertEquals("https://example.com/image.jpg", domain.imageUrl)
    }

    @Test
    fun eventEndpointNormalDetailFieldsAreEmpty() {
        val api = testEventEndpointNormal()
        val domain = api.toDomain()
        assertTrue(domain.agencies.isEmpty())
        assertTrue(domain.launches.isEmpty())
        assertTrue(domain.expeditions.isEmpty())
        assertTrue(domain.spaceStations.isEmpty())
        assertTrue(domain.programs.isEmpty())
        assertTrue(domain.astronauts.isEmpty())
    }

    @Test
    fun eventEndpointDetailedMapsDetailFields() {
        val api = testEventEndpointDetailed()
        val domain = api.toDomain()
        assertEquals(1, domain.agencies.size)
        assertEquals("SpaceX", domain.agencies[0].name)
        assertEquals(1, domain.launches.size)
        assertEquals("SpaceX | Falcon 9 Block 5", domain.launches[0].name)
    }

    @Test
    fun eventEndpointDetailedNullableListsDefaultToEmpty() {
        val api = testEventEndpointDetailed()
        val domain = api.toDomain()
        // program and astronauts are null in the test factory
        assertTrue(domain.programs.isEmpty())
        assertTrue(domain.astronauts.isEmpty())
    }

    @Test
    fun paginatedEventListMaps() {
        val api = PaginatedEventEndpointNormalList(
            count = 1,
            results = listOf(testEventEndpointNormal(id = 42)),
            next = null,
            previous = null
        )
        val domain = api.toDomain()
        assertEquals(1, domain.count)
        assertEquals(1, domain.results.size)
        assertEquals(42, domain.results[0].id)
        assertNull(domain.next)
    }

    @Test
    fun eventDatePrecisionMaps() {
        val api = testEventEndpointNormal()
        val domain = api.toDomain()
        assertEquals(1, domain.datePrecision?.id)
        assertEquals("Minute", domain.datePrecision?.name)
    }
}
