package me.calebjones.spacelaunchnow.util.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RemoteDiagnosticsTest {

    private val now = 1_752_000_000L // 2025-07-08T18:40:00Z
    private val future = "2027-01-01T00:00:00Z"
    private val past = "2020-01-01T00:00:00Z"

    private fun configJson(overridesJson: String = "[]", defaultRate: String = "5") = """
        {"version":1,"default_sample_rate":$defaultRate,"overrides":$overridesJson}
    """.trimIndent()

    @Test
    fun parsesValidConfig() {
        val config = parseDiagnosticsConfig(
            configJson("""[{"match":{"rc_user_id":"u1"},"sample_rate":100,"diagnostic_level":"VERBOSE","expires_at":"$future"}]""")
        )
        assertNotNull(config)
        assertEquals(5f, config.defaultSampleRate)
        assertEquals("u1", config.overrides.single().match?.rcUserId)
    }

    @Test
    fun blankMalformedAndUnknownVersionReturnNull() {
        assertNull(parseDiagnosticsConfig(null))
        assertNull(parseDiagnosticsConfig(""))
        assertNull(parseDiagnosticsConfig("not json at all {"))
        assertNull(parseDiagnosticsConfig("""{"version":99,"default_sample_rate":5}"""))
    }

    @Test
    fun matchingOverrideWinsWithCoercionAndLevel() {
        val config = parseDiagnosticsConfig(
            configJson("""[{"match":{"rc_user_id":"u1"},"sample_rate":250,"diagnostic_level":"VERBOSE","expires_at":"$future"}]""")
        )
        val resolved = resolveDiagnostics(config, "u1", now)
        assertEquals(100f, resolved.sampleRate)
        assertEquals(DiagnosticLevel.VERBOSE, resolved.diagnosticLevel)
    }

    @Test
    fun unknownLevelNameResolvesToNullLevel() {
        val config = parseDiagnosticsConfig(
            configJson("""[{"match":{"rc_user_id":"u1"},"diagnostic_level":"SUPER_VERBOSE"}]""")
        )
        assertNull(resolveDiagnostics(config, "u1", now).diagnosticLevel)
    }

    @Test
    fun expiredAndMalformedExpiryEntriesAreIgnored() {
        val config = parseDiagnosticsConfig(
            configJson(
                """[
                    {"match":{"rc_user_id":"u1"},"sample_rate":100,"expires_at":"$past"},
                    {"match":{"rc_user_id":"u1"},"sample_rate":90,"expires_at":"not-a-date"}
                ]"""
            )
        )
        val resolved = resolveDiagnostics(config, "u1", now)
        assertEquals(5f, resolved.sampleRate) // falls through to default
        assertNull(resolved.diagnosticLevel)
    }

    @Test
    fun firstMatchWins() {
        val config = parseDiagnosticsConfig(
            configJson(
                """[
                    {"match":{"rc_user_id":"u1"},"sample_rate":80,"expires_at":"$future"},
                    {"match":{"rc_user_id":"u1"},"sample_rate":20,"expires_at":"$future"}
                ]"""
            )
        )
        assertEquals(80f, resolveDiagnostics(config, "u1", now).sampleRate)
    }

    @Test
    fun noMatchFallsBackToDefaultRateAndNullLevel() {
        val config = parseDiagnosticsConfig(
            configJson("""[{"match":{"rc_user_id":"someone-else"},"sample_rate":100,"diagnostic_level":"VERBOSE"}]""")
        )
        val resolved = resolveDiagnostics(config, "u1", now)
        assertEquals(5f, resolved.sampleRate)
        assertNull(resolved.diagnosticLevel)
    }

    @Test
    fun nullConfigOrUserProducesNoInfluence() {
        assertEquals(ResolvedDiagnostics(null, null), resolveDiagnostics(null, "u1", now))
        val config = parseDiagnosticsConfig(configJson())
        assertEquals(5f, resolveDiagnostics(config, null, now).sampleRate)
        assertNull(resolveDiagnostics(config, null, now).diagnosticLevel)
    }

    @Test
    fun remoteLevelOverrideHonorsExpiry() {
        assertEquals(DiagnosticLevel.VERBOSE, resolveRemoteLevelOverride("VERBOSE", now + 100, now))
        assertNull(resolveRemoteLevelOverride("VERBOSE", now - 100, now))
        assertNull(resolveRemoteLevelOverride("NOT_A_LEVEL", now + 100, now))
        assertNull(resolveRemoteLevelOverride(null, null, now))
        assertEquals(DiagnosticLevel.STANDARD, resolveRemoteLevelOverride("STANDARD", null, now))
    }
}
