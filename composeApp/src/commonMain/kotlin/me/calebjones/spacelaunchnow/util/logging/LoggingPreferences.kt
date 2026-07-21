package me.calebjones.spacelaunchnow.util.logging

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Effective diagnostic settings: resolved level plus verbose expiry (null unless VERBOSE is active). */
data class DiagnosticSettings(
    val level: DiagnosticLevel,
    val verboseExpiresAtEpochSeconds: Long?,
)

/**
 * Manages logging severity preferences with DataStore.
 *
 * [diagnostic_level] is the primary control: calling [setDiagnosticLevel] atomically
 * writes the new [DiagnosticLevel] name AND overwrites the legacy keys
 * ([console_severity], [datadog_severity], [datadog_enabled]) so that any remaining
 * reader of those old knobs observes the same effective configuration (downgrade coherence).
 *
 * The legacy keys are write-only coherence targets — they are written by [setDiagnosticLevel]
 * and read by [DiagnosticLevel.fromStorage] during migration, but have no public setters
 * or getters. [DiagnosticLevelController] is the sole authority for applying severity changes.
 *
 * Default behavior:
 * - Console: WARN (always shows critical issues in production)
 * - DataDog: Disabled by default (user must enable in Settings)
 * - DataDog Severity: WARN when enabled (configurable in Debug menu)
 */
class LoggingPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val CONSOLE_SEVERITY = stringPreferencesKey("console_severity")
        private val DATADOG_SEVERITY = stringPreferencesKey("datadog_severity")
        private val DATADOG_ENABLED = booleanPreferencesKey("datadog_enabled")
        private val DIAGNOSTIC_LEVEL = stringPreferencesKey("diagnostic_level")
        private val VERBOSE_EXPIRES_AT = longPreferencesKey("verbose_expires_at_epoch_seconds")
        private val VERBOSE_REVERT_LEVEL = stringPreferencesKey("verbose_revert_level")

        // Server-driven level override (REMOTE_LOG_SAMPLING_SPEC §4.3). Stored apart
        // from the user's own diagnostic_level so their choice is never clobbered and
        // restores automatically when the override is cleared or its backstop lapses.
        private val REMOTE_LEVEL_OVERRIDE = stringPreferencesKey("remote_diagnostic_level_override")
        private val REMOTE_LEVEL_EXPIRES_AT = longPreferencesKey("remote_diagnostic_level_expires_at")

        /** Safety backstop: a remote override dies after 72h unless re-asserted. */
        const val REMOTE_OVERRIDE_BACKSTOP_SECONDS: Long = VERBOSE_AUTO_REVERT_SECONDS
    }

    private fun readRevertLevel(prefs: Preferences): DiagnosticLevel? =
        prefs[VERBOSE_REVERT_LEVEL]?.let { name -> DiagnosticLevel.entries.firstOrNull { it.name == name } }

    /** Write [level] plus the coherent legacy keys. Callers manage the expiry keys. */
    private fun writeLevel(prefs: MutablePreferences, level: DiagnosticLevel) {
        val policy = level.policy()
        prefs[DIAGNOSTIC_LEVEL] = level.name
        prefs[DATADOG_ENABLED] = level != DiagnosticLevel.OFF
        prefs[CONSOLE_SEVERITY] = policy.consoleSeverity.name
        prefs[DATADOG_SEVERITY] = policy.remoteSeverity.name
    }

    fun getDiagnosticSettings(): Flow<DiagnosticSettings> = dataStore.data.map { prefs ->
        val now = Clock.System.now().epochSeconds
        val stored = DiagnosticLevel.fromStorage(prefs[DIAGNOSTIC_LEVEL], prefs[DATADOG_ENABLED])
        val resolved = resolveVerboseExpiry(
            stored = stored,
            verboseExpiresAtEpochSeconds = prefs[VERBOSE_EXPIRES_AT],
            revertLevel = readRevertLevel(prefs),
            nowEpochSeconds = now,
        )
        // A live remote override wins over the user's own level; expiry is checked at
        // read time so a stale override can never outlive its backstop across launches.
        val remote = resolveRemoteLevelOverride(
            remoteName = prefs[REMOTE_LEVEL_OVERRIDE],
            remoteExpiresAtEpochSeconds = prefs[REMOTE_LEVEL_EXPIRES_AT],
            nowEpochSeconds = now,
        )
        DiagnosticSettings(
            level = remote ?: resolved.level,
            verboseExpiresAtEpochSeconds = when {
                remote == DiagnosticLevel.VERBOSE -> prefs[REMOTE_LEVEL_EXPIRES_AT]
                remote == null && resolved.level == DiagnosticLevel.VERBOSE -> prefs[VERBOSE_EXPIRES_AT]
                else -> null
            },
        )
    }

    /**
     * Single diagnostic control. Migrates from the legacy datadog_enabled boolean
     * when the new key is absent (old toggle ON -> STANDARD).
     * VERBOSE auto-reverts after 72h — see resolveVerboseExpiry.
     */
    fun getDiagnosticLevel(): Flow<DiagnosticLevel> = getDiagnosticSettings().map { it.level }

    /**
     * Set the diagnostic level and keep the legacy keys coherent so any remaining
     * reader of the old knobs observes the same effective configuration.
     *
     * Coherence is one-directional: only [setDiagnosticLevel] updates all keys atomically.
     *
     * When setting VERBOSE, stamps a 72h expiry window and remembers the previous level
     * as the revert target. Re-selecting VERBOSE while already VERBOSE restarts the 72h
     * window but keeps the original revert target. Selecting OFF or STANDARD clears both
     * expiry keys.
     */
    suspend fun setDiagnosticLevel(level: DiagnosticLevel) {
        dataStore.edit { prefs ->
            if (level == DiagnosticLevel.VERBOSE) {
                val previous = DiagnosticLevel.fromStorage(prefs[DIAGNOSTIC_LEVEL], prefs[DATADOG_ENABLED])
                if (previous != DiagnosticLevel.VERBOSE) {
                    prefs[VERBOSE_REVERT_LEVEL] = previous.name
                }
                // (Re)start the 72h window; keep the original revert target when already VERBOSE.
                prefs[VERBOSE_EXPIRES_AT] = Clock.System.now().epochSeconds + VERBOSE_AUTO_REVERT_SECONDS
            } else {
                prefs.remove(VERBOSE_EXPIRES_AT)
                prefs.remove(VERBOSE_REVERT_LEVEL)
            }
            writeLevel(prefs, level)
        }
    }

    /**
     * Set (or clear, with null) the server-driven level override. Writing stamps a
     * fresh 72h backstop; RemoteDiagnosticsController re-asserts periodically while
     * the remote config keeps the override active, so a forgotten config entry can
     * never leave a device verbose forever. The user's own diagnostic_level keys
     * are untouched.
     */
    suspend fun setRemoteDiagnosticLevelOverride(level: DiagnosticLevel?) {
        dataStore.edit { prefs ->
            if (level == null) {
                prefs.remove(REMOTE_LEVEL_OVERRIDE)
                prefs.remove(REMOTE_LEVEL_EXPIRES_AT)
            } else {
                prefs[REMOTE_LEVEL_OVERRIDE] = level.name
                prefs[REMOTE_LEVEL_EXPIRES_AT] =
                    Clock.System.now().epochSeconds + REMOTE_OVERRIDE_BACKSTOP_SECONDS
            }
        }
    }

    /**
     * Persist the verbose auto-revert when the 72h window has lapsed; stamps a
     * missing expiry for VERBOSE states persisted by older builds. Returns the
     * effective level after enforcement.
     */
    suspend fun enforceVerboseExpiry(): DiagnosticLevel {
        var effective = DiagnosticLevel.OFF
        dataStore.edit { prefs ->
            val stored = DiagnosticLevel.fromStorage(prefs[DIAGNOSTIC_LEVEL], prefs[DATADOG_ENABLED])
            effective = stored
            if (stored != DiagnosticLevel.VERBOSE) return@edit
            val now = Clock.System.now().epochSeconds
            val expiry = prefs[VERBOSE_EXPIRES_AT]
            if (expiry == null) {
                prefs[VERBOSE_EXPIRES_AT] = now + VERBOSE_AUTO_REVERT_SECONDS
                return@edit
            }
            if (now >= expiry) {
                val revert = readRevertLevel(prefs) ?: DiagnosticLevel.STANDARD
                prefs.remove(VERBOSE_EXPIRES_AT)
                prefs.remove(VERBOSE_REVERT_LEVEL)
                writeLevel(prefs, revert)
                effective = revert
            }
        }
        return effective
    }
}
