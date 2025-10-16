package me.calebjones.spacelaunchnow.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import org.koin.java.KoinJavaComponent.inject
import kotlinx.coroutines.flow.first

/**
 * Utility to trigger widget updates when appearance settings change.
 */
object WidgetUpdater {
    
    // Keys for widget appearance stored in Glance state
    private val FORCE_UPDATE_KEY = longPreferencesKey("force_update_timestamp")
    private val THEME_SOURCE_KEY = stringPreferencesKey("widget_theme_source")
    private val APP_THEME_MODE_KEY = stringPreferencesKey("app_theme_mode") // System/Light/Dark from app settings
    private val BACKGROUND_ALPHA_KEY = floatPreferencesKey("widget_background_alpha")
    private val CORNER_RADIUS_KEY = intPreferencesKey("widget_corner_radius")
    private val HAS_ACCESS_KEY = stringPreferencesKey("widget_has_access") // Using string for boolean
    
    /**
     * Updates all widgets of a specific type.
     */
    suspend fun updateWidget(context: Context, widgetClass: Class<out androidx.glance.appwidget.GlanceAppWidget>) {
        withContext(Dispatchers.Main) {
            try {
                println("WidgetUpdater: Updating widget: ${widgetClass.simpleName}")
                when (widgetClass.simpleName) {
                    "NextUpWidget" -> {
                        val manager = GlanceAppWidgetManager(context)
                        val widgetIds = manager.getGlanceIds(NextUpWidget::class.java)
                        println("WidgetUpdater: Found ${widgetIds.size} NextUpWidget instances")
                        NextUpWidget().updateAll(context)
                    }
                    "LaunchListWidget" -> {
                        val manager = GlanceAppWidgetManager(context)
                        val widgetIds = manager.getGlanceIds(LaunchListWidget::class.java)
                        println("WidgetUpdater: Found ${widgetIds.size} LaunchListWidget instances")
                        LaunchListWidget().updateAll(context)
                    }
                    else -> {
                        // Update all widgets
                        println("WidgetUpdater: Updating all widgets")
                        NextUpWidget().updateAll(context)
                        LaunchListWidget().updateAll(context)
                    }
                }
                println("WidgetUpdater: Widget update completed for ${widgetClass.simpleName}")
            } catch (e: Exception) {
                println("WidgetUpdater: ERROR updating widgets: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Updates all app widgets (NextUp and LaunchList).
     * MUST be called from Main dispatcher or coroutine scope.
     * 
     * Forces recomposition by updating widget state to trigger provideGlance().
     */
    suspend fun updateAllWidgets(context: Context) {
        withContext(Dispatchers.Main) {
            try {
                println("WidgetUpdater: Starting updateAllWidgets()")
                
                // Read current appearance values from DataStore ONCE
                val widgetPreferences: WidgetPreferences by inject(WidgetPreferences::class.java)
                val appPreferences: AppPreferences by inject(AppPreferences::class.java)
                
                val themeSource = widgetPreferences.widgetThemeSourceFlow.first()
                val backgroundAlpha = widgetPreferences.widgetBackgroundAlphaFlow.first()
                val cornerRadius = widgetPreferences.widgetCornerRadiusFlow.first()
                val hasAccess = widgetPreferences.widgetAccessGrantedFlow.first()
                val appThemeMode = appPreferences.themeFlow.first().name // System/Light/Dark
                
                println("WidgetUpdater: Read from DataStore - Source: $themeSource, AppTheme: $appThemeMode, Alpha: $backgroundAlpha, Radius: $cornerRadius, Access: $hasAccess")
                
                val glanceManager = GlanceAppWidgetManager(context)
                val currentTime = System.currentTimeMillis()
                
                // Check for NextUpWidget instances
                val nextUpIds = glanceManager.getGlanceIds(NextUpWidget::class.java)
                println("WidgetUpdater: Found ${nextUpIds.size} NextUpWidget instances on home screen")
                
                // Check for LaunchListWidget instances
                val launchListIds = glanceManager.getGlanceIds(LaunchListWidget::class.java)
                println("WidgetUpdater: Found ${launchListIds.size} LaunchListWidget instances on home screen")
                
                if (nextUpIds.isEmpty() && launchListIds.isEmpty()) {
                    println("WidgetUpdater: WARNING - No widgets found on home screen! User needs to add widgets first.")
                    return@withContext
                }
                
                // Update NextUp widgets - Write appearance values directly to Glance state
                if (nextUpIds.isNotEmpty()) {
                    println("WidgetUpdater: Updating ${nextUpIds.size} NextUpWidget instances...")
                    val nextUpWidget = NextUpWidget()
                    
                    nextUpIds.forEach { glanceId ->
                        // Write appearance values to Glance widget state (eliminates DataStore timing issues)
                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                            prefs.toMutablePreferences().apply {
                                this[FORCE_UPDATE_KEY] = currentTime
                                this[THEME_SOURCE_KEY] = themeSource.name
                                this[APP_THEME_MODE_KEY] = appThemeMode // Add app's theme mode
                                this[BACKGROUND_ALPHA_KEY] = backgroundAlpha
                                this[CORNER_RADIUS_KEY] = cornerRadius
                                this[HAS_ACCESS_KEY] = hasAccess.toString()
                            }
                        }
                        // Trigger update with the new state
                        nextUpWidget.update(context, glanceId)
                        println("WidgetUpdater: Updated NextUpWidget instance: $glanceId")
                    }
                    println("WidgetUpdater: All NextUpWidget updates complete")
                }
                
                // Update LaunchList widgets - Same pattern
                if (launchListIds.isNotEmpty()) {
                    println("WidgetUpdater: Updating ${launchListIds.size} LaunchListWidget instances...")
                    val launchListWidget = LaunchListWidget()
                    
                    launchListIds.forEach { glanceId ->
                        // Write appearance values to Glance widget state
                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                            prefs.toMutablePreferences().apply {
                                this[FORCE_UPDATE_KEY] = currentTime
                                this[THEME_SOURCE_KEY] = themeSource.name
                                this[APP_THEME_MODE_KEY] = appThemeMode // Add app's theme mode
                                this[BACKGROUND_ALPHA_KEY] = backgroundAlpha
                                this[CORNER_RADIUS_KEY] = cornerRadius
                                this[HAS_ACCESS_KEY] = hasAccess.toString()
                            }
                        }
                        // Trigger update with the new state
                        launchListWidget.update(context, glanceId)
                        println("WidgetUpdater: Updated LaunchListWidget instance: $glanceId")
                    }
                    println("WidgetUpdater: All LaunchListWidget updates complete")
                }
                
                println("WidgetUpdater: All widget updates completed successfully")
            } catch (e: Exception) {
                println("WidgetUpdater: FATAL ERROR updating all widgets: ${e.message}")
                println("WidgetUpdater: Exception type: ${e.javaClass.simpleName}")
                println("WidgetUpdater: Stack trace:")
                e.printStackTrace()
            }
        }
    }
}
