package me.calebjones.spacelaunchnow.util.logging

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Remote diagnostics control (REMOTE_LOG_SAMPLING_SPEC Phases 1-2).
 *
 * A Firebase Remote Config JSON key ("diagnostics_config") can set a default
 * Datadog sample rate for all installs plus per-RC-user-id overrides of sample
 * rate and diagnostic level, each optionally expiring. Parsing and resolution
 * are pure so they can be unit-tested; any parse problem resolves to "no remote
 * influence" (fall back to local values). v1 matches the raw rc_user_id —
 * salted-hash matching is Phase 3 hardening.
 */
@Serializable
data class DiagnosticsConfig(
    val version: Int = 1,
    @SerialName("default_sample_rate") val defaultSampleRate: Float? = null,
    val overrides: List<DiagnosticsOverride> = emptyList(),
)

@Serializable
data class DiagnosticsOverride(
    val match: DiagnosticsMatch? = null,
    @SerialName("sample_rate") val sampleRate: Float? = null,
    @SerialName("diagnostic_level") val diagnosticLevel: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class DiagnosticsMatch(
    @SerialName("rc_user_id") val rcUserId: String? = null,
)

/** What remote config wants for this device; null fields mean "no remote influence". */
data class ResolvedDiagnostics(
    val sampleRate: Float?,
    val diagnosticLevel: DiagnosticLevel?,
)

private val diagnosticsConfigJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/** Null on blank/malformed JSON or unknown schema version (safe-by-default). */
fun parseDiagnosticsConfig(jsonString: String?): DiagnosticsConfig? {
    if (jsonString.isNullOrBlank()) return null
    return try {
        diagnosticsConfigJson.decodeFromString<DiagnosticsConfig>(jsonString).takeIf { it.version == 1 }
    } catch (e: Exception) {
        null
    }
}

/**
 * First non-expired override matching [rcUserId] wins; otherwise the config
 * default sample rate applies with no level influence. Rates coerced to 0-100.
 */
fun resolveDiagnostics(
    config: DiagnosticsConfig?,
    rcUserId: String?,
    nowEpochSeconds: Long,
): ResolvedDiagnostics {
    if (config == null) return ResolvedDiagnostics(null, null)
    val match = rcUserId?.let { id ->
        config.overrides.firstOrNull { override ->
            override.match?.rcUserId == id && !override.isExpired(nowEpochSeconds)
        }
    }
    return if (match != null) {
        ResolvedDiagnostics(
            sampleRate = match.sampleRate?.coerceIn(0f, 100f),
            diagnosticLevel = match.diagnosticLevel?.toDiagnosticLevelOrNull(),
        )
    } else {
        ResolvedDiagnostics(config.defaultSampleRate?.coerceIn(0f, 100f), null)
    }
}

/**
 * Read-time gate for the persisted remote level override: null when unset,
 * unknown, or past its expiry backstop.
 */
fun resolveRemoteLevelOverride(
    remoteName: String?,
    remoteExpiresAtEpochSeconds: Long?,
    nowEpochSeconds: Long,
): DiagnosticLevel? {
    val level = remoteName?.toDiagnosticLevelOrNull() ?: return null
    if (remoteExpiresAtEpochSeconds != null && nowEpochSeconds >= remoteExpiresAtEpochSeconds) return null
    return level
}

private fun String.toDiagnosticLevelOrNull(): DiagnosticLevel? =
    DiagnosticLevel.entries.firstOrNull { it.name == this }

private fun DiagnosticsOverride.isExpired(nowEpochSeconds: Long): Boolean {
    val expiry = expiresAt ?: return false
    return try {
        nowEpochSeconds >= Instant.parse(expiry).epochSeconds
    } catch (e: Exception) {
        true // malformed expiry: treat the entry as lapsed rather than open-ended
    }
}
