package me.calebjones.spacelaunchnow.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.compose.ui.graphics.Color
import androidx.glance.layout.Alignment
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import me.calebjones.spacelaunchnow.data.preferences.WidgetThemeSource
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant
import me.calebjones.spacelaunchnow.MainActivity
import me.calebjones.spacelaunchnow.R
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.ui.theme.getWidgetAppearanceBlocking
import kotlin.math.abs
import org.koin.java.KoinJavaComponent.inject as koinInject

class NextUpWidget : GlanceAppWidget() {

    // CRITICAL: Must set state definition to use preferences state for force updates
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        println("=== NextUpWidget: provideGlance START for id: $id ===")
        
        val launch = fetchNextLaunch()
        
        provideContent {
            // Read appearance from Glance state (written by WidgetUpdater)
            val prefs = currentState<Preferences>()
            val forceUpdateTimestamp = prefs[longPreferencesKey("force_update_timestamp")]
            val themeSourceName = prefs[stringPreferencesKey("widget_theme_source")] ?: "FOLLOW_APP_THEME"
            val appThemeMode = prefs[stringPreferencesKey("app_theme_mode")] ?: "System"
            val backgroundAlpha = prefs[floatPreferencesKey("widget_background_alpha")] ?: 1.0f
            val cornerRadius = prefs[intPreferencesKey("widget_corner_radius")] ?: 16
            
            val themeSource = WidgetThemeSource.fromString(themeSourceName)
            println("NextUpWidget: Glance state - timestamp=$forceUpdateTimestamp, source=$themeSource, appTheme=$appThemeMode, alpha=$backgroundAlpha, radius=$cornerRadius")
            
            // Select appropriate ColorProviders based on theme source with alpha applied
            val useDynamicColors = themeSource == WidgetThemeSource.DYNAMIC_COLORS
            val colorProviders = WidgetGlanceColorScheme.getColorProvidersWithAlpha(
                context = context,
                useDynamicColors = useDynamicColors,
                alpha = backgroundAlpha,
                appThemeMode = if (themeSource == WidgetThemeSource.FOLLOW_APP_THEME) appThemeMode else "System"
            )
            
            GlanceTheme(colors = colorProviders) {
                NextUpWidgetContent(
                    launch = launch,
                    cornerRadius = cornerRadius
                )
            }
        }
    }

    private suspend fun fetchNextLaunch(): LaunchNormal? {
        return withContext(Dispatchers.IO) {
            try {
                val launchRepository: LaunchRepository by koinInject(LaunchRepository::class.java)
                val result = launchRepository.getUpcomingLaunchesNormal(limit = 1)
                result.getOrNull()?.results?.firstOrNull()
            } catch (e: Exception) {
                println("Widget: Failed to fetch next launch: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}

@Composable
fun NextUpWidgetContent(
    launch: LaunchNormal?,
    cornerRadius: Int = 16
) {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface) // Use theme surface color (has alpha baked in)
            .cornerRadius(cornerRadius.dp)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        launch?.id?.let { launchId ->
                            putExtra("launch_id", launchId)
                        }
                    }
                )
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (launch != null) {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val launchName = launch.name ?: "Unknown Launch"
                val parts = launchName.split(" | ")
                val title = parts[0]
                val subtitle = if (parts.size > 1) parts[1] else ""

                // Launch Name
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    ),
                    maxLines = 1
                )

                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Agency
                launch.launchServiceProvider.name?.let { agencyName ->
                    Text(
                        text = agencyName,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onBackground
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }

                // Location
                launch.pad?.location?.name?.let { location ->
                    Text(
                        text = location,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }

                // Countdown Display - Main App Style
                launch.net?.let { netTime ->
                    CountdownDisplay(netTime)
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Status
                launch.status?.name?.let { status ->
                    Text(
                        text = status,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = when (launch.status.id) {
                                1 -> GlanceTheme.colors.secondary // Go
                                2 -> GlanceTheme.colors.error // TBD
                                else -> GlanceTheme.colors.onBackground
                            }
                        )
                    )
                }
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher_monochrome),
                    contentDescription = "Space Launch Now",
                    modifier = GlanceModifier.size(32.dp)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "No upcoming launches",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onBackground
                    )
                )
            }
        }
    }
}

@Composable
private fun CountdownDisplay(launchTime: Instant) {
    val now = Clock.System.now()
    val duration = launchTime - now
    val totalSeconds = duration.inWholeSeconds
    val isPast = totalSeconds < 0
    val absoluteSeconds = abs(totalSeconds)

    val days = (absoluteSeconds / 86400).toInt()
    val hours = ((absoluteSeconds % 86400) / 3600).toInt()
    val minutes = ((absoluteSeconds % 3600) / 60).toInt()

    if (isPast) {
        Text(
            text = "Launched",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.tertiary
            )
        )
    } else {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CountdownUnit(value = days, label = "Days")
            CountdownSeparator()
            CountdownUnit(value = hours, label = "Hours")
            CountdownSeparator()
            CountdownUnit(value = minutes, label = "Minutes")
        }
    }
}

@Composable
private fun CountdownUnit(value: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.tertiary
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun CountdownSeparator() {
    Text(
        text = ":",
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = GlanceTheme.colors.tertiary
        ),
        modifier = GlanceModifier.padding(horizontal = 4.dp)
    )
}

class NextUpWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextUpWidget()
}
