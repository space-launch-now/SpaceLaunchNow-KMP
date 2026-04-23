package me.calebjones.spacelaunchnow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EventTest {

    private fun minimalEvent(id: Int = 1) = Event(
        id = id,
        name = "Test Event",
        slug = "test-event",
        type = EventType(id = 1, name = "Launch"),
        description = null,
        date = null,
        location = null,
        imageUrl = null,
        webcastLive = false,
        lastUpdated = null,
        duration = null,
        datePrecision = null
    )

    @Test
    fun constructionWithMinimalFields() {
        val event = minimalEvent()
        assertEquals(1, event.id)
        assertEquals("Test Event", event.name)
        assertEquals("Launch", event.type.name)
    }

    @Test
    fun listFieldsDefaultToEmpty() {
        val event = minimalEvent()
        assertTrue(event.infoUrls.isEmpty())
        assertTrue(event.vidUrls.isEmpty())
        assertTrue(event.updates.isEmpty())
        assertTrue(event.agencies.isEmpty())
        assertTrue(event.launches.isEmpty())
        assertTrue(event.expeditions.isEmpty())
        assertTrue(event.spaceStations.isEmpty())
        assertTrue(event.programs.isEmpty())
        assertTrue(event.astronauts.isEmpty())
    }

    @Test
    fun copyPreservesAndOverrides() {
        val event = minimalEvent(id = 1)
        val copied = event.copy(id = 2, name = "Copied")
        assertEquals(1, event.id)
        assertEquals(2, copied.id)
        assertEquals("Copied", copied.name)
        assertEquals(event.type, copied.type)
    }

    @Test
    fun nullableFieldsAreNull() {
        val event = minimalEvent()
        assertNull(event.description)
        assertNull(event.date)
        assertNull(event.location)
        assertNull(event.imageUrl)
        assertNull(event.lastUpdated)
        assertNull(event.duration)
        assertNull(event.datePrecision)
    }
}
