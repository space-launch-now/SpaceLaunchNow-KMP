package me.calebjones.spacelaunchnow.wear.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Which server the watch reads launches from. The phone resolves the real value (Remote Config
 * `data_backend` plus the debug override) and sends it over the Wearable Data Layer; the watch
 * has no Firebase. Until a value arrives the watch stays on [LL], like the phone's
 * `DataBackend.DEFAULT`.
 */
enum class WatchDataBackend(val value: String) {
    LL("ll"),
    TERMINUS("terminus");

    companion object {
        val DEFAULT: WatchDataBackend = LL

        fun fromString(raw: String?): WatchDataBackend =
            entries.find { it.value == raw } ?: DEFAULT
    }
}

/** The backend choice plus the Terminus host the phone talks to (prod, or staging in debug). */
data class WatchBackendConfig(
    val backend: WatchDataBackend = WatchDataBackend.DEFAULT,
    val terminusBaseUrl: String = TERMINUS_PROD_URL,
) {
    companion object {
        const val TERMINUS_PROD_URL = "https://terminus.spacelaunchnow.app"
        const val TERMINUS_STAGING_URL = "https://staging-terminus.spacelaunchnow.app"

        /** Only the two known Terminus hosts are accepted; anything else falls back to prod. */
        fun sanitizeBaseUrl(raw: String?): String =
            listOf(TERMINUS_PROD_URL, TERMINUS_STAGING_URL).find { it == raw } ?: TERMINUS_PROD_URL
    }
}

/** Persists the phone-sent [WatchBackendConfig] in the watch DataStore. */
object WatchBackendStore {
    val BACKEND_KEY = stringPreferencesKey("data_backend")
    val TERMINUS_URL_KEY = stringPreferencesKey("terminus_base_url")

    suspend fun save(dataStore: DataStore<Preferences>, backend: String?, terminusBaseUrl: String?) {
        dataStore.edit { prefs ->
            prefs[BACKEND_KEY] = WatchDataBackend.fromString(backend).value
            prefs[TERMINUS_URL_KEY] = WatchBackendConfig.sanitizeBaseUrl(terminusBaseUrl)
        }
    }

    /** No stored value yet means LL. */
    suspend fun load(dataStore: DataStore<Preferences>): WatchBackendConfig {
        val prefs = dataStore.data.first()
        return WatchBackendConfig(
            backend = WatchDataBackend.fromString(prefs[BACKEND_KEY]),
            terminusBaseUrl = WatchBackendConfig.sanitizeBaseUrl(prefs[TERMINUS_URL_KEY]),
        )
    }
}
