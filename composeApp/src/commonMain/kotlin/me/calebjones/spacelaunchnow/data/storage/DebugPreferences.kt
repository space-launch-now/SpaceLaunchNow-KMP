package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

/**
 * Debug-specific preferences for development and testing
 */
class DebugPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val CUSTOM_API_BASE_URL = stringPreferencesKey("debug_custom_api_base_url")
        private val USE_CUSTOM_API_URL = booleanPreferencesKey("debug_use_custom_api_url")
        private val USE_FCM_DEBUG_TOPICS = booleanPreferencesKey("debug_use_fcm_debug_topics")

        // Default URLs for quick switching
        const val PROD_API_URL = "https://spacelaunchnow.app"
        const val DEV_API_URL = "https://staging.spacelaunchnow.app"
        const val LOCAL_API_URL = "http://localhost:8000"
    }

    /**
     * Flow of debug settings
     */
    val debugSettingsFlow: Flow<DebugSettings> = dataStore.data.map { preferences ->
        DebugSettings(
            useCustomApiUrl = preferences[USE_CUSTOM_API_URL] ?: false,
            customApiBaseUrl = preferences[CUSTOM_API_BASE_URL] ?: PROD_API_URL,
            useDebugTopics = preferences[USE_FCM_DEBUG_TOPICS] ?: true
        )
    }

    /**
     * Get current debug settings
     */
    suspend fun getDebugSettings(): DebugSettings {
        return debugSettingsFlow.first()
    }

    /**
     * Update whether to use custom API URL
     */
    suspend fun setUseCustomApiUrl(use: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_CUSTOM_API_URL] = use
        }
    }

    /**
     * Set custom API base URL
     */
    suspend fun setCustomApiBaseUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[CUSTOM_API_BASE_URL] = url
            preferences[USE_CUSTOM_API_URL] = true
        }
    }

    /**
     * Set whether to use debug topics (debug_v3) or production topics (prod_v3)
     */
    suspend fun setUseDebugTopics(useDebug: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_FCM_DEBUG_TOPICS] = useDebug
        }
    }

    /**
     * Quick method to switch to production URL
     */
    suspend fun switchToProdUrl() {
        setCustomApiBaseUrl(PROD_API_URL)
    }

    /**
     * Quick method to switch to development URL
     */
    suspend fun switchToDevUrl() {
        setCustomApiBaseUrl(DEV_API_URL)
    }

    /**
     * Quick method to switch to local URL
     */
    suspend fun switchToLocalUrl() {
        setCustomApiBaseUrl(LOCAL_API_URL)
    }

    /**
     * Get the effective API base URL (either custom or default)
     */
    suspend fun getEffectiveApiBaseUrl(): String {
        val settings = getDebugSettings()
        return if (settings.useCustomApiUrl) {
            settings.customApiBaseUrl
        } else {
            PROD_API_URL
        }
    }
}

/**
 * Data class representing debug settings
 */
data class DebugSettings(
    val useCustomApiUrl: Boolean = false,
    val customApiBaseUrl: String = DebugPreferences.PROD_API_URL,
    val useDebugTopics: Boolean = true
)