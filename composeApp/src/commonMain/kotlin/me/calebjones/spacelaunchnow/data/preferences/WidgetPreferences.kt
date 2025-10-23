package me.calebjones.spacelaunchnow.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Widget theme source options.
 */
enum class WidgetThemeSource {
    FOLLOW_APP_THEME,    // Use app's custom theme (ThemePreferences)
    FOLLOW_SYSTEM,       // Use system light/dark theme
    DYNAMIC_COLORS;      // Use Material You wallpaper colors (Android 12+)
    
    companion object {
        fun fromString(value: String?): WidgetThemeSource {
            return entries.find { it.name == value } ?: FOLLOW_APP_THEME
        }
    }
}

/**
 * Manages widget appearance preferences including theme source, transparency, and corner radius.
 * These settings are separate from the app theme and apply only to Android widgets.
 */
class WidgetPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val WIDGET_THEME_SOURCE = stringPreferencesKey("widget_theme_source")
        private val WIDGET_BACKGROUND_ALPHA = floatPreferencesKey("widget_background_alpha")
        private val WIDGET_CORNER_RADIUS = intPreferencesKey("widget_corner_radius")
        private val WIDGET_ACCESS_GRANTED = longPreferencesKey("widget_access_granted")
        
        // Default values
        private const val DEFAULT_ALPHA = 0.95f // Slightly transparent by default
        private const val DEFAULT_CORNER_RADIUS = 16 // dp
    }

    /**
     * Flow of the selected widget theme source.
     */
    val widgetThemeSourceFlow: Flow<WidgetThemeSource> = dataStore.data.map { preferences ->
        WidgetThemeSource.fromString(preferences[WIDGET_THEME_SOURCE])
    }

    /**
     * Flow of the widget background alpha/transparency (0.0 = fully transparent, 1.0 = fully opaque).
     */
    val widgetBackgroundAlphaFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[WIDGET_BACKGROUND_ALPHA] ?: DEFAULT_ALPHA
    }

    /**
     * Flow of the widget corner radius in dp.
     */
    val widgetCornerRadiusFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[WIDGET_CORNER_RADIUS] ?: DEFAULT_CORNER_RADIUS
    }

    /**
     * Flow of the widget access status (timestamp when access was granted, 0 = no access).
     * This is cached from the main app's premium check to avoid RevenueCat calls in widget context.
     */
    val widgetAccessGrantedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        val timestamp = preferences[WIDGET_ACCESS_GRANTED] ?: 0L
        timestamp > 0L
    }

    /**
     * Updates the widget theme source.
     * @param source The theme source to use.
     */
    suspend fun updateWidgetThemeSource(source: WidgetThemeSource) {
        dataStore.edit { preferences ->
            preferences[WIDGET_THEME_SOURCE] = source.name
        }
    }

    /**
     * Updates the widget background alpha/transparency.
     * @param alpha The alpha value (0.0 to 1.0).
     */
    suspend fun updateWidgetBackgroundAlpha(alpha: Float) {
        dataStore.edit { preferences ->
            preferences[WIDGET_BACKGROUND_ALPHA] = alpha.coerceIn(0f, 1f)
        }
    }

    /**
     * Updates the widget corner radius.
     * @param radius The corner radius in dp (0 to 48).
     */
    suspend fun updateWidgetCornerRadius(radius: Int) {
        dataStore.edit { preferences ->
            preferences[WIDGET_CORNER_RADIUS] = radius.coerceIn(0, 48)
        }
    }

    /**
     * Updates the widget access status (cached from main app's premium check).
     * @param hasAccess Whether the user has widget access.
     */
    suspend fun updateWidgetAccessGranted(hasAccess: Boolean) {
        dataStore.edit { preferences ->
            if (hasAccess) {
                preferences[WIDGET_ACCESS_GRANTED] = Clock.System.now().toEpochMilliseconds()
            } else {
                preferences.remove(WIDGET_ACCESS_GRANTED)
            }
        }
    }

    /**
     * Resets all widget appearance settings to defaults.
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.remove(WIDGET_THEME_SOURCE)
            preferences.remove(WIDGET_BACKGROUND_ALPHA)
            preferences.remove(WIDGET_CORNER_RADIUS)
            preferences.remove(WIDGET_ACCESS_GRANTED)
        }
    }
}
