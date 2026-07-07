package me.calebjones.spacelaunchnow.util.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VerboseExpiryTest {

    @Test
    fun nonVerboseLevelsPassThroughUnchanged() {
        for (level in listOf(DiagnosticLevel.OFF, DiagnosticLevel.STANDARD)) {
            val r = resolveVerboseExpiry(level, verboseExpiresAtEpochSeconds = 0, revertLevel = DiagnosticLevel.OFF, nowEpochSeconds = 100)
            assertEquals(level, r.level)
            assertFalse(r.expired)
        }
    }

    @Test
    fun verboseBeforeExpiryStaysVerbose() {
        val r = resolveVerboseExpiry(DiagnosticLevel.VERBOSE, 1000, DiagnosticLevel.STANDARD, nowEpochSeconds = 999)
        assertEquals(DiagnosticLevel.VERBOSE, r.level)
        assertFalse(r.expired)
    }

    @Test
    fun verboseAtOrAfterExpiryRevertsToPreviousLevel() {
        val atExpiry = resolveVerboseExpiry(DiagnosticLevel.VERBOSE, 1000, DiagnosticLevel.OFF, nowEpochSeconds = 1000)
        assertEquals(DiagnosticLevel.OFF, atExpiry.level)
        assertTrue(atExpiry.expired)
        val after = resolveVerboseExpiry(DiagnosticLevel.VERBOSE, 1000, DiagnosticLevel.STANDARD, nowEpochSeconds = 5000)
        assertEquals(DiagnosticLevel.STANDARD, after.level)
        assertTrue(after.expired)
    }

    @Test
    fun missingRevertTargetFallsBackToStandard() {
        val r = resolveVerboseExpiry(DiagnosticLevel.VERBOSE, 1000, revertLevel = null, nowEpochSeconds = 2000)
        assertEquals(DiagnosticLevel.STANDARD, r.level)
        assertTrue(r.expired)
    }

    @Test
    fun verboseWithoutRecordedExpiryStaysVerbose() {
        // Legacy state from a build before auto-revert existed; enforceVerboseExpiry stamps the clock.
        val r = resolveVerboseExpiry(DiagnosticLevel.VERBOSE, verboseExpiresAtEpochSeconds = null, revertLevel = null, nowEpochSeconds = Long.MAX_VALUE)
        assertEquals(DiagnosticLevel.VERBOSE, r.level)
        assertFalse(r.expired)
    }
}
