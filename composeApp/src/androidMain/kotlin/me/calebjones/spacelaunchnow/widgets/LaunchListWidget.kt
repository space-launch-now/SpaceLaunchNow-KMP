package me.calebjones.spacelaunchnow.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.calebjones.spacelaunchnow.MainActivity
import me.calebjones.spacelaunchnow.R
import me.calebjones.spacelaunchnow.data.preferences.WidgetThemeSource
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import org.koin.java.KoinJavaComponent.inject as koinInject

class LaunchListWidget : GlanceAppWidget() {

    private val log = logger()

    // CRITICAL: Must set state definition to use preferences state for force updates
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        log.d { "provideGlance START for id: $id" }

        // Pre-fetch launches outside composable (need to check access first from state)
        // We'll fetch optimistically - if no access, we just won't show them
        val launches = fetchUpcomingLaunches()

        provideContent {
            // Read appearance from Glance state (written by WidgetUpdater)
            val prefs = currentState<Preferences>()
            val forceUpdateTimestamp = prefs[longPreferencesKey("force_update_timestamp")]
            val themeSourceName =
                prefs[stringPreferencesKey("widget_theme_source")] ?: "FOLLOW_APP_THEME"
            val appThemeMode = prefs[stringPreferencesKey("app_theme_mode")] ?: "System"
            val backgroundAlpha = prefs[floatPreferencesKey("widget_background_alpha")] ?: 1.0f
            val cornerRadius = prefs[intPreferencesKey("widget_corner_radius")] ?: 16
            val hasAccessStr = prefs[stringPreferencesKey("widget_has_access")] ?: "false"
            val hasWidgetAccess = hasAccessStr.toBoolean()

            val themeSource = WidgetThemeSource.fromString(themeSourceName)
            log.d { "Glance state - timestamp=$forceUpdateTimestamp, source=$themeSource, appTheme=$appThemeMode, alpha=$backgroundAlpha, radius=$cornerRadius, access=$hasWidgetAccess" }
            log.v { "RAW hasAccessStr from state: '$hasAccessStr'" }
            log.v { "Parsed hasWidgetAccess boolean: $hasWidgetAccess" }
            log.i { "Will show ${if (hasWidgetAccess) "UNLOCKED" else "LOCKED"} content" }

            // Get appropriate color providers based on theme source with alpha applied
            val useDynamicColors = themeSource == WidgetThemeSource.DYNAMIC_COLORS
            val colorProviders = WidgetGlanceColorScheme.getColorProvidersWithAlpha(
                context = context,
                useDynamicColors = useDynamicColors,
                alpha = backgroundAlpha,
                appThemeMode = if (themeSource == WidgetThemeSource.FOLLOW_APP_THEME) appThemeMode else "System"
            )

            GlanceTheme(colors = colorProviders) {
                if (hasWidgetAccess) {
                    LaunchListWidgetContent(
                        launches = launches,
                        cornerRadius = cornerRadius
                    )
                } else {
                    LaunchListWidgetLockedContent(
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }

    // Removed checkWidgetAccess() - now reads from cached DataStore value

    private suspend fun fetchUpcomingLaunches(): List<Launch> {
        return withContext(Dispatchers.IO) {
            try {
                val launchRepository: LaunchRepository by koinInject(LaunchRepository::class.java)
                val notificationStateStorage: NotificationStateStorage by koinInject(NotificationStateStorage::class.java)
                val launchFilterService: LaunchFilterService by koinInject(LaunchFilterService::class.java)

                val state = notificationStateStorage.stateFlow.first()
                val agencyIds = launchFilterService.getAgencyIds(state)
                val locationIds = launchFilterService.getLocationIds(state)

                val result = launchRepository.getUpcomingLaunchesNormalDomain(
                    limit = 10,
                    agencyIds = agencyIds,
                    locationIds = locationIds
                )
                result.getOrNull()?.data?.results ?: emptyList()
            } catch (e: Exception) {
                log.e(e) { "Failed to fetch launches" }
                emptyList()
            }
        }
    }
}

@Composable
fun LaunchListWidgetLockedContent(
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
                        putExtra("navigate_to", "subscription")
                        // Clear back stack and bring to front if already running
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Image(
                provider = ImageProvider(R.mipmap.ic_launcher_monochrome),
                contentDescription = "Space Launch Now",
                modifier = GlanceModifier.size(64.dp)
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Text(
                text = "🔒 Premium Widget",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "Upgrade to Premium to unlock the Launch List widget",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onBackground
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "Tap to upgrade",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = GlanceTheme.colors.secondary
                )
            )
        }
    }
}

@Composable
fun LaunchListWidgetContent(
    launches: List<Launch>,
    cornerRadius: Int = 16
) {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface) // Use theme surface color (has alpha baked in)
            .cornerRadius(cornerRadius.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java)
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Launches",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Launch List
            if (launches.isNotEmpty()) {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(launches) { launch ->
                        LaunchListItem(launch)
                    }
                }
            } else {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
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
                                fontSize = 11.sp,
                                color = GlanceTheme.colors.onBackground
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LaunchListItem(launch: Launch) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .padding(8.dp)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("launch_id", launch.id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )
            )
    ) {
        // Launch Name
        Text(
            text = launch.name,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Agency
        launch.provider.name.let { agencyName ->
            Text(
                text = agencyName,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }

        // Location
        launch.pad?.location?.name?.let { location ->
            Text(
                text = location,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Countdown and Status Row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Countdown
            launch.net?.let { netTime ->
                val countdown = formatCountdownCompact(netTime)
                Text(
                    text = countdown,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.secondary
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Status
            launch.status?.name?.let { status ->
                Text(
                    text = status,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (launch.status?.id) {
                            1 -> GlanceTheme.colors.secondary // Go
                            2 -> GlanceTheme.colors.error // TBD
                            3, 4, 6 -> GlanceTheme.colors.tertiary // Success/Failure/Hold
                            else -> GlanceTheme.colors.onSurface
                        }
                    )
                )
            }
        }
    }
}

private fun formatCountdownCompact(netTime: Instant): String {
    val now = Clock.System.now()
    val duration = netTime - now

    return when {
        duration.isNegative() -> "Launched"
        duration < 1.minutes -> "<1m"
        duration < 1.hours -> {
            val mins = duration.inWholeMinutes
            "${mins}m"
        }

        duration < 1.days -> {
            val hours = duration.inWholeHours
            val mins = (duration - hours.hours).inWholeMinutes
            "${hours}h ${mins}m"
        }

        duration < 7.days -> {
            val days = duration.inWholeDays
            val hours = (duration - days.days).inWholeHours
            "${days}d ${hours}h"
        }

        else -> {
            val days = duration.inWholeDays
            "${days}d"
        }
    }
}

class LaunchListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LaunchListWidget()
}
