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
    
    val themeSource = widgetPreferences.widgetThemeSourceFlow.first()
    val backgroundAlpha = widgetPreferences.widgetBackgroundAlphaFlow.first()
    val cornerRadius = widgetPreferences.widgetCornerRadiusFlow.first()
    val hasAccess = widgetPreferences.widgetAccessGrantedFlow.first()
    
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
