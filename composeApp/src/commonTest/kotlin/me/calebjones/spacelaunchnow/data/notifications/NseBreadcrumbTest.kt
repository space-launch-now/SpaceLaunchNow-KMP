package me.calebjones.spacelaunchnow.data.notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NseBreadcrumbTest {

    @Test
    fun parse_validEntry() {
        val b = NseBreadcrumb.parse("1751600000|oneHour|suppressed|v5_launch_filter")
        assertEquals(1751600000L, b?.timestampEpochSeconds)
        assertEquals("oneHour", b?.type)
        assertEquals("suppressed", b?.decision)
        assertEquals("v5_launch_filter", b?.reason)
    }

    @Test
    fun parse_rejectsMalformed() {
        assertNull(NseBreadcrumb.parse("not-a-breadcrumb"))
        assertNull(NseBreadcrumb.parse("abc|type|decision|reason")) // bad timestamp
        assertNull(NseBreadcrumb.parse("123|only|three"))
    }

    @Test
    fun parse_toleratesExtraSeparatorsInReason() {
        val b = NseBreadcrumb.parse("123|t|d|reason/with/slashes")
        assertEquals("reason/with/slashes", b?.reason)
    }

    @Test
    fun parse_emptyReasonIsValid() {
        val b = NseBreadcrumb.parse("123|t|d|")
        assertEquals("", b?.reason)
    }

    @Test
    fun parse_rawPipeInReasonIsPreserved() {
        val b = NseBreadcrumb.parse("1|t|d|r|x")
        assertEquals("r|x", b?.reason)
    }

    @Test
    fun parse_negativeTimestampAccepted() {
        // Parser does not editorialize on values; writer guarantees sanity.
        assertEquals(-5L, NseBreadcrumb.parse("-5|t|d|r")?.timestampEpochSeconds)
    }

    @Test
    fun parse_emptyStringRejected() {
        assertNull(NseBreadcrumb.parse(""))
    }
}
