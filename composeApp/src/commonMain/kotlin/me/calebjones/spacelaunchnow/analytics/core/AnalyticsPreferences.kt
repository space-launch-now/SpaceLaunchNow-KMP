package me.calebjones.spacelaunchnow.analytics.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists per-provider analytics consent using the shared [AppSettingsDataStore].
 *
 * Keys follow the pattern `analytics_provider_enabled_<providerName>`. The global
 * `analytics_enabled` key gates the entire analytics system; when false, all providers
 * are treated as disabled regardless of their individual setting.
 */
class AnalyticsPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        private const val PROVIDER_KEY_PREFIX = "analytics_provider_enabled_"
    }

    val isAnalyticsEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ANALYTICS_ENABLED] ?: true
    }

    /**
     * Returns a [Flow] that emits `true` when the given provider is enabled globally and
     * the per-provider preference is enabled (defaults to `true` if never set).
     */
    fun isProviderEnabled(providerName: String): Flow<Boolean> {
        val key = booleanPreferencesKey("$PROVIDER_KEY_PREFIX$providerName")
        return dataStore.data.map { prefs ->
            val globalEnabled = prefs[ANALYTICS_ENABLED] ?: true
            val providerEnabled = prefs[key] ?: true
            globalEnabled && providerEnabled
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[ANALYTICS_ENABLED] = enabled }
    }

    suspend fun setProviderEnabled(providerName: String, enabled: Boolean) {
        val key = booleanPreferencesKey("$PROVIDER_KEY_PREFIX$providerName")
        dataStore.edit { prefs -> prefs[key] = enabled }
    }
}
