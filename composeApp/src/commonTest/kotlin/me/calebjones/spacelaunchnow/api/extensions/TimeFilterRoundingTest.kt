package me.calebjones.spacelaunchnow.api.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TimeFilterRoundingTest {

    private val edge = Instant.parse("2026-10-01T14:10:00Z")

    @Test
    fun `bucket is one minute`() {
        assertEquals(1.minutes, TimeFilterRounding.BUCKET)
    }

    @Test
    fun `floor on a bucket edge returns the edge`() {
        assertEquals(edge, TimeFilterRounding.floor(edge))
    }

    @Test
    fun `ceil on a bucket edge returns the edge`() {
        assertEquals(edge, TimeFilterRounding.ceil(edge))
    }

    @Test
    fun `floor just after an edge returns that edge`() {
        assertEquals(edge, TimeFilterRounding.floor(edge + 1.nanoseconds))
    }

    @Test
    fun `ceil just after an edge returns the next edge`() {
        assertEquals(edge + 1.minutes, TimeFilterRounding.ceil(edge + 1.nanoseconds))
    }

    @Test
    fun `floor just before an edge returns the previous edge`() {
        assertEquals(edge - 1.minutes, TimeFilterRounding.floor(edge - 1.nanoseconds))
    }

    @Test
    fun `ceil just before an edge returns that edge`() {
        assertEquals(edge, TimeFilterRounding.ceil(edge - 1.nanoseconds))
    }

    @Test
    fun `rounded instants print whole seconds with no fraction`() {
        // The shape the app sent during the 2026-10-01 incident.
        val sent = Instant.parse("2026-10-01T14:14:12.895063Z")

        assertEquals("2026-10-01T14:14:00Z", TimeFilterRounding.floor(sent).toString())
        assertEquals("2026-10-01T14:15:00Z", TimeFilterRounding.ceil(sent).toString())
    }

    @Test
    fun `floor never moves later and ceil never moves earlier`() {
        val samples = listOf(
            "2026-10-01T00:00:00Z",
            "2026-10-01T14:14:12.895063Z",
            "2026-10-01T14:14:59.999999999Z",
            "2026-12-31T23:59:59.5Z",
            "1969-12-31T23:58:01.25Z",
        ).map(Instant::parse)

        samples.forEach { instant ->
            val floor = TimeFilterRounding.floor(instant)
            val ceil = TimeFilterRounding.ceil(instant)
            assertTrue(floor <= instant, "floor($instant) = $floor moved later")
            assertTrue(ceil >= instant, "ceil($instant) = $ceil moved earlier")
            assertTrue(instant - floor < 1.minutes, "floor($instant) = $floor moved a whole bucket")
            assertTrue(ceil - instant < 1.minutes, "ceil($instant) = $ceil moved a whole bucket")
        }
    }
}
