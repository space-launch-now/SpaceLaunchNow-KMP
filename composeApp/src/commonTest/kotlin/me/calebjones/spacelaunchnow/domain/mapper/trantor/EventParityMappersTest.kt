package me.calebjones.spacelaunchnow.domain.mapper.trantor

import kotlin.time.Instant
import me.calebjones.spacelaunchnow.api.trantor.models.EventDetail
import me.calebjones.spacelaunchnow.api.trantor.models.EventList
import me.calebjones.spacelaunchnow.api.trantor.models.EventProgram
import me.calebjones.spacelaunchnow.api.trantor.models.UpdateList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** E14(P5): event list description; detail duration, last_updated, programs[].description, updates[]. */
class EventParityMappersTest {

    @Test
    fun listRowMapsDescription() {
        assertEquals("Static fire", EventList(id = 1, name = "E", description = "Static fire").toDomain().description)
        assertNull(EventList(id = 1, name = "E").toDomain().description)
    }

    @Test
    fun detailMapsDurationLastUpdatedProgramDescriptionAndUpdates() {
        val domain = EventDetail(
            id = 1,
            name = "E",
            duration = "01:30:00",
            lastUpdated = Instant.parse("2026-02-03T04:05:06Z"),
            programs = listOf(EventProgram(id = 2, name = "Starlink", description = "Constellation")),
            updates = listOf(UpdateList(id = 7, comment = "Slipped", profileImage = "https://p.png"))
        ).toDomain()
        assertEquals("01:30:00", domain.duration)
        assertEquals(Instant.parse("2026-02-03T04:05:06Z"), domain.lastUpdated)
        assertEquals("Constellation", domain.programs.single().description)
        assertEquals("Slipped", domain.updates.single().comment)
        assertEquals("https://p.png", domain.updates.single().profileImage)
    }

    @Test
    fun detailFieldsAreNullOrEmptyWhenAbsent() {
        val domain = EventDetail(id = 1, name = "E", programs = listOf(EventProgram(id = 2, name = "P"))).toDomain()
        assertNull(domain.duration)
        assertNull(domain.lastUpdated)
        assertNull(domain.programs.single().description)
        assertTrue(domain.updates.isEmpty())
    }
}
