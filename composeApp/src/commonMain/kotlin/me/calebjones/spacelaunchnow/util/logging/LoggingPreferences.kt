package me.calebjones.spacelaunchnow.util.logging

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages logging severity preferences with DataStore
 *
 * Stores severity levels directly - no boolean flag mapping needed!
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
    }

    /**
     * Get current console logging severity
     * Default: WARN
     */
    fun getConsoleSeverity(): Flow<Severity> = dataStore.data.map { prefs ->
        val severityName = prefs[CONSOLE_SEVERITY] ?: Severity.Warn.name
        try {
            Severity.valueOf(severityName)
        } catch (e: IllegalArgumentException) {
            Severity.Warn // Fallback if corrupted
        }
    }

    /**
     * Get current DataDog logging severity
     * Default: WARN
     * Note: This is only used if DataDog is enabled
     */
    fun getDataDogSeverity(): Flow<Severity> = dataStore.data.map { prefs ->
        val severityName = prefs[DATADOG_SEVERITY] ?: Severity.Warn.name
        try {
            Severity.valueOf(severityName)
        } catch (e: IllegalArgumentException) {
            Severity.Warn // Fallback if corrupted
        }
    }

    /**
     * Check if DataDog is enabled
     * Default: false (disabled)
     */
    fun isDataDogEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DATADOG_ENABLED] ?: false
    }

    /**
     * Set console logging severity directly
     */
    suspend fun setConsoleSeverity(severity: Severity) {
        dataStore.edit { prefs ->
            prefs[CONSOLE_SEVERITY] = severity.name
        }
    }

    /**
     * Set DataDog logging severity directly
     */
    suspend fun setDataDogSeverity(severity: Severity) {
        dataStore.edit { prefs ->
            prefs[DATADOG_SEVERITY] = severity.name
        }
    }

    /**
     * Enable or disable DataDog completely
     */
    suspend fun setDataDogEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DATADOG_ENABLED] = enabled
        }
    }
}



