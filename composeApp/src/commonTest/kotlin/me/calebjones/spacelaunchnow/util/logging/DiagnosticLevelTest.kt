package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiagnosticLevelTest {

    @Test
    fun fromStorage_prefersStoredLevel() {
        assertEquals(DiagnosticLevel.VERBOSE, DiagnosticLevel.fromStorage("VERBOSE", legacyDatadogEnabled = false))
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage("OFF", legacyDatadogEnabled = true))
    }

    @Test
    fun fromStorage_migratesLegacyEnabledToStandard() {
        assertEquals(DiagnosticLevel.STANDARD, DiagnosticLevel.fromStorage(null, legacyDatadogEnabled = true))
    }

    @Test
    fun fromStorage_defaultsToOff() {
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage(null, legacyDatadogEnabled = null))
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage(null, legacyDatadogEnabled = false))
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage("garbage", legacyDatadogEnabled = false))
    }

    @Test
    fun policy_offRevokesConsentAndSilencesRemote() {
        val p = DiagnosticLevel.OFF.policy()
        assertFalse(p.remoteConsentGranted)
        assertEquals(Severity.Assert, p.remoteSeverity)
        assertEquals(Severity.Warn, p.consoleSeverity)
        assertEquals(Severity.Info, p.fileSeverity) // file log ALWAYS captures Info+
    }

    @Test
    fun policy_standardGrantsConsentAtWarn() {
        val p = DiagnosticLevel.STANDARD.policy()
        assertTrue(p.remoteConsentGranted)
        assertEquals(Severity.Warn, p.remoteSeverity)
        assertEquals(Severity.Info, p.fileSeverity)
    }

    @Test
    fun policy_verboseGrantsConsentAtDebugEverywhere() {
        val p = DiagnosticLevel.VERBOSE.policy()
        assertTrue(p.remoteConsentGranted)
        assertEquals(Severity.Debug, p.remoteSeverity)
        assertEquals(Severity.Debug, p.consoleSeverity)
        assertEquals(Severity.Debug, p.fileSeverity)
    }
}
