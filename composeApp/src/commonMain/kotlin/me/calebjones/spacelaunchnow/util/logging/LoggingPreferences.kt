package me.calebjones.spacelaunchnow.util.logging

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages user logging preferences with DataStore
 *
 * Default behavior:
 * - Console: WARN+ (always shows critical issues)
 * - DataDog: WARN+ (minimizes remote logging costs)
 *
 * User enables logging:
 * - Console: INFO+ (shows user actions)
 * - DataDog: INFO+ (track user flows for support)
 *
 * Debug mode (dev menu):
 * - Console: ALL (verbose debugging)
 * - DataDog: DEBUG+ (full diagnostics)
 */
class LoggingPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val USER_LOGGING_ENABLED = booleanPreferencesKey("user_logging_enabled")
        private val DEBUG_MODE_ENABLED = booleanPreferencesKey("debug_logging_enabled")
    }

    /**
     * Whether user has enabled logging (Settings -> Enable Logging)
     * Default: false (only WARN+ logs)
     */
    val isUserLoggingEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[USER_LOGGING_ENABLED] ?: false
    }

    /**
     * Whether debug mode is active (Debug Menu -> Debug Logging)
     * Default: false
     */
    val isDebugModeEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DEBUG_MODE_ENABLED] ?: false
    }

    /**
     * Get effective severity for console logging
     */
    fun getConsoleSeverity(isDebugBuild: Boolean): Flow<Severity> = dataStore.data.map { prefs ->
        when {
            // Debug menu override (highest priority)
            prefs[DEBUG_MODE_ENABLED] == true -> Severity.Verbose

            // Debug builds always show DEBUG+
            isDebugBuild -> Severity.Debug

            // User enabled logging shows INFO+
            prefs[USER_LOGGING_ENABLED] == true -> Severity.Info

            // Default: only critical issues (WARN+)
            else -> Severity.Warn
        }
    }

    /**
     * Get effective severity for DataDog remote logging
     * More conservative to reduce costs and noise
     */
    fun getDataDogSeverity(): Flow<Severity> = dataStore.data.map { prefs ->
        when {
            // Debug mode: full diagnostics
            prefs[DEBUG_MODE_ENABLED] == true -> Severity.Debug

            // User enabled: track flows
            prefs[USER_LOGGING_ENABLED] == true -> Severity.Info

            // Default: only errors (WARN, ERROR, ASSERT)
            else -> Severity.Warn
        }
    }

    /**
     * Enable/disable user logging (Settings screen)
     */
    suspend fun setUserLoggingEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[USER_LOGGING_ENABLED] = enabled
        }
    }

    /**
     * Enable/disable debug mode (Debug menu)
     */
    suspend fun setDebugModeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DEBUG_MODE_ENABLED] = enabled
        }
    }
}

