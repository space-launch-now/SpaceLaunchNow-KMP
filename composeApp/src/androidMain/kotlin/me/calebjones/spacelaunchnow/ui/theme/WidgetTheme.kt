package me.calebjones.spacelaunchnow.ui.theme

import android.content.Context
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.preferences.WidgetThemeSource
import org.koin.java.KoinJavaComponent.inject

/**
 * Widget appearance configuration.
 */
data class WidgetAppearance(
    val themeSource: WidgetThemeSource,
    val backgroundAlpha: Float,
    val cornerRadius: Int,
    val hasAccess: Boolean
)

/**
 * Retrieves widget appearance preferences.
 */
suspend fun getWidgetAppearanceWithColors(context: Context): WidgetAppearance {
    val widgetPreferences: WidgetPreferences by inject(WidgetPreferences::class.java)
    
    println("WidgetTheme: Reading widget appearance from DataStore...")
    
    val themeSource = widgetPreferences.widgetThemeSourceFlow.first()
    val backgroundAlpha = widgetPreferences.widgetBackgroundAlphaFlow.first()
    val cornerRadius = widgetPreferences.widgetCornerRadiusFlow.first()
    val hasAccess = widgetPreferences.widgetAccessGrantedFlow.first()
    
    println("WidgetTheme: Read values - Source: $themeSource, Alpha: $backgroundAlpha, Radius: $cornerRadius, Access: $hasAccess")
    
    return WidgetAppearance(
        themeSource = themeSource,
        backgroundAlpha = backgroundAlpha,
        cornerRadius = cornerRadius,
        hasAccess = hasAccess
    )
}

/**
 * Blocking version for use in Composables (runs on widget's background thread).
 */
fun getWidgetAppearanceBlocking(context: Context): WidgetAppearance {
    return try {
        runBlocking {
            getWidgetAppearanceWithColors(context)
        }
    } catch (e: Exception) {
        // Return default values if preferences can't be read
        println("Widget: Failed to read widget appearance preferences: ${e.message}")
        e.printStackTrace()
        WidgetAppearance(
            themeSource = WidgetThemeSource.FOLLOW_APP_THEME,
            backgroundAlpha = 1.0f,
            cornerRadius = 16,
            hasAccess = false  // Default to no access if can't read preferences
        )
    }
}

/**
 * Applies corner radius to a GlanceModifier.
 */
fun GlanceModifier.applyWidgetCornerRadius(cornerRadius: Int): GlanceModifier {
    return this.cornerRadius(cornerRadius)
}
