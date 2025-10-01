package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption

class AppPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val THEME_OPTION = stringPreferencesKey("theme_option")
        private val USE_UTC = booleanPreferencesKey("use_utc")
        private val HIDE_TBD_LAUNCHES = booleanPreferencesKey("hide_tbd_launches")
        private val KEEP_LAUNCHES_FOR_24_HOURS = booleanPreferencesKey("keep_launches_for_24_hours")
    }

    val themeFlow: Flow<ThemeOption> = dataStore.data.map { preferences ->
        val themeString = preferences[THEME_OPTION] ?: ThemeOption.System.name
        ThemeOption.entries.firstOrNull { it.name == themeString } ?: ThemeOption.System
    }

    val useUtcFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_UTC] ?: false
    }

    val hideTbdLaunchesFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HIDE_TBD_LAUNCHES] ?: false
    }

    val keepLaunchesFor24HoursFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEEP_LAUNCHES_FOR_24_HOURS] ?: true
    }

    suspend fun updateTheme(theme: ThemeOption) {
        dataStore.edit { preferences ->
            preferences[THEME_OPTION] = theme.name
        }
    }

    suspend fun updateUseUtc(useUtc: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_UTC] = useUtc
        }
    }

    suspend fun updateHideTbdLaunches(hideTbd: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIDE_TBD_LAUNCHES] = hideTbd
        }
    }

    suspend fun updateKeepLaunchesFor24Hours(keep: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEEP_LAUNCHES_FOR_24_HOURS] = keep
        }
    }

    suspend fun getTheme(): ThemeOption {
        val themeString = dataStore.data.map { it[THEME_OPTION] }.first() ?: ThemeOption.System.name
        return ThemeOption.entries.firstOrNull { it.name == themeString } ?: ThemeOption.System
    }

    suspend fun getUseUtc(): Boolean {
        return dataStore.data.map { it[USE_UTC] }.first() ?: false
    }

    suspend fun getHideTbdLaunches(): Boolean {
        return dataStore.data.map { it[HIDE_TBD_LAUNCHES] }.first() ?: false
    }

    suspend fun getKeepLaunchesFor24Hours(): Boolean {
        return dataStore.data.map { it[KEEP_LAUNCHES_FOR_24_HOURS] }.first() ?: true
    }
}