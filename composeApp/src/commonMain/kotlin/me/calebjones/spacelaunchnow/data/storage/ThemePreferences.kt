package me.calebjones.spacelaunchnow.data.storage

import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Premium feature: Theme customization preferences
 * Allows users to customize primary/secondary colors and palette style
 */
class ThemePreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val CUSTOM_PRIMARY_COLOR = longPreferencesKey("custom_primary_color")
        private val CUSTOM_SECONDARY_COLOR = longPreferencesKey("custom_secondary_color")
        private val PALETTE_STYLE = stringPreferencesKey("palette_style")
        private val APPLY_TO_WIDGETS = booleanPreferencesKey("apply_theme_to_widgets")
        
        // Available palette styles from MaterialKolor
        val AVAILABLE_PALETTE_STYLES = listOf(
            "TonalSpot",      // Default, balanced
            "Neutral",        // Subtle, muted
            "Vibrant",        // Bold, saturated
            "Expressive",     // Creative, dynamic
            "Rainbow",        // Playful, colorful
            "FruitSalad",     // Fresh, varied
            "Monochrome",     // Single hue
            "Fidelity",       // True to seed color
            "Content",        // Adaptive to content
            "SchemeContent"   // Content-based scheme
        )
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

    val applyToWidgetsFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[APPLY_TO_WIDGETS] ?: false
    }

    suspend fun updateCustomPrimaryColor(color: Color?) {
        dataStore.edit { preferences ->
            if (color != null) {
                // Store the color value directly as ULong
                preferences[CUSTOM_PRIMARY_COLOR] = color.value.toLong()
            } else {
                preferences.remove(CUSTOM_PRIMARY_COLOR)
            }
        }
    }

    suspend fun updateCustomSecondaryColor(color: Color?) {
        dataStore.edit { preferences ->
            if (color != null) {
                // Store the color value directly as ULong
                preferences[CUSTOM_SECONDARY_COLOR] = color.value.toLong()
            } else {
                preferences.remove(CUSTOM_SECONDARY_COLOR)
            }
        }
    }

    suspend fun updatePaletteStyle(style: String?) {
        dataStore.edit { preferences ->
            if (style != null && AVAILABLE_PALETTE_STYLES.contains(style)) {
                preferences[PALETTE_STYLE] = style
            } else {
                preferences.remove(PALETTE_STYLE)
            }
        }
    }

    suspend fun getCustomPrimaryColor(): Color? {
        val colorLong = dataStore.data.map { it[CUSTOM_PRIMARY_COLOR] }.first()
        return colorLong?.let { Color(it.toULong()) }
    }

    suspend fun getCustomSecondaryColor(): Color? {
        val colorLong = dataStore.data.map { it[CUSTOM_SECONDARY_COLOR] }.first()
        return colorLong?.let { Color(it.toULong()) }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.remove(CUSTOM_PRIMARY_COLOR)
            preferences.remove(CUSTOM_SECONDARY_COLOR)
            preferences.remove(PALETTE_STYLE)
        }
    }
}
