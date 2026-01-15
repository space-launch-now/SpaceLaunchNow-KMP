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
        private val DATADOG_SAMPLE_RATE = floatPreferencesKey("debug_datadog_sample_rate")

        // Debug subscription simulation keys
        private val DEBUG_SUBSCRIPTION_ACTIVE = booleanPreferencesKey("debug_subscription_active")
        private val DEBUG_SUBSCRIPTION_TYPE = stringPreferencesKey("debug_subscription_type")
        private val DEBUG_SUBSCRIPTION_PRODUCT_ID =
            stringPreferencesKey("debug_subscription_product_id")

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
            useDebugTopics = preferences[USE_FCM_DEBUG_TOPICS] ?: false,
            datadogSampleRate = preferences[DATADOG_SAMPLE_RATE] ?: 1f,
            debugSubscriptionActive = preferences[DEBUG_SUBSCRIPTION_ACTIVE] ?: false,
            debugSubscriptionType = preferences[DEBUG_SUBSCRIPTION_TYPE],
            debugSubscriptionProductId = preferences[DEBUG_SUBSCRIPTION_PRODUCT_ID]
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
     * Set Datadog remote logging sample rate (0-100%)
     * Controls what percentage of logs are sent to Datadog servers
     */
    suspend fun setDatadogSampleRate(rate: Float) {
        dataStore.edit { preferences ->
            preferences[DATADOG_SAMPLE_RATE] = rate.coerceIn(0f, 100f)
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

    /**
     * Enable debug subscription simulation
     */
    suspend fun setDebugSubscriptionSimulation(
        isActive: Boolean,
        subscriptionType: String? = null,
        productId: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[DEBUG_SUBSCRIPTION_ACTIVE] = isActive
            if (subscriptionType != null) {
                preferences[DEBUG_SUBSCRIPTION_TYPE] = subscriptionType
            } else {
                preferences.remove(DEBUG_SUBSCRIPTION_TYPE)
            }
            if (productId != null) {
                preferences[DEBUG_SUBSCRIPTION_PRODUCT_ID] = productId
            } else {
                preferences.remove(DEBUG_SUBSCRIPTION_PRODUCT_ID)
            }
        }
    }

    /**
     * Clear debug subscription simulation
     */
    suspend fun clearDebugSubscriptionSimulation() {
        dataStore.edit { preferences ->
            preferences.remove(DEBUG_SUBSCRIPTION_ACTIVE)
            preferences.remove(DEBUG_SUBSCRIPTION_TYPE)
            preferences.remove(DEBUG_SUBSCRIPTION_PRODUCT_ID)
        }
    }
}

/**
 * Data class representing debug settings
 */
data class DebugSettings(
    val useCustomApiUrl: Boolean = false,
    val customApiBaseUrl: String = DebugPreferences.PROD_API_URL,
    val useDebugTopics: Boolean = false,
    val datadogSampleRate: Float = 1f,
    val debugSubscriptionActive: Boolean = false,
    val debugSubscriptionType: String? = null,
    val debugSubscriptionProductId: String? = null
)