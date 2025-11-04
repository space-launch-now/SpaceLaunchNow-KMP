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
        
        // Premium theme customization
        private val CUSTOM_PRIMARY_COLOR = longPreferencesKey("custom_primary_color")
        private val CUSTOM_SECONDARY_COLOR = longPreferencesKey("custom_secondary_color")
        private val PALETTE_STYLE = stringPreferencesKey("palette_style")
        
        // Debug menu unlock (for release builds)
        private val DEBUG_MENU_UNLOCKED = booleanPreferencesKey("debug_menu_unlocked")
        
        // Beta warning dialog shown flag
        private val BETA_WARNING_SHOWN = booleanPreferencesKey("beta_warning_shown")
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

    val customPrimaryColorFlow: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[CUSTOM_PRIMARY_COLOR]
    }

    val customSecondaryColorFlow: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[CUSTOM_SECONDARY_COLOR]
    }

    val paletteStyleFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PALETTE_STYLE]
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
    
    // Debug menu unlock methods
    val debugMenuUnlockedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DEBUG_MENU_UNLOCKED] ?: false
    }
    
    suspend fun setDebugMenuUnlocked(unlocked: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEBUG_MENU_UNLOCKED] = unlocked
        }
    }
    
    suspend fun isDebugMenuUnlocked(): Boolean {
        return dataStore.data.map { it[DEBUG_MENU_UNLOCKED] }.first() ?: false
    }
    
    // Beta warning dialog methods
    val betaWarningShownFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BETA_WARNING_SHOWN] ?: false
    }
    
    suspend fun setBetaWarningShown(shown: Boolean) {
        dataStore.edit { preferences ->
            preferences[BETA_WARNING_SHOWN] = shown
        }
    }
    
    suspend fun isBetaWarningShown(): Boolean {
        return dataStore.data.map { it[BETA_WARNING_SHOWN] }.first() ?: false
    }
}