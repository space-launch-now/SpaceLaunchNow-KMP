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
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.MainActivity
import me.calebjones.spacelaunchnow.R
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject as koinInject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class LaunchListWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Check if user has premium entitlement for widgets
        val hasWidgetAccess = checkWidgetAccess()
        val launches = if (hasWidgetAccess) fetchUpcomingLaunches() else emptyList()

        provideContent {
            GlanceTheme {
                if (hasWidgetAccess) {
                    LaunchListWidgetContent(launches)
                } else {
                    LaunchListWidgetLockedContent()
                }
            }
        }
    }

    private suspend fun checkWidgetAccess(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val subscriptionRepository: SubscriptionRepository by koinInject(SubscriptionRepository::class.java)
                subscriptionRepository.hasFeature(PremiumFeature.ADVANCED_WIDGETS)
            } catch (e: Exception) {
                println("Widget: Failed to check widget access: ${e.message}")
                e.printStackTrace()
                false // Default to locked if check fails
            }
        }
    }

    private suspend fun fetchUpcomingLaunches(): List<LaunchNormal> {
        return withContext(Dispatchers.IO) {
            try {
                val launchRepository: LaunchRepository by koinInject(LaunchRepository::class.java)
                val result = launchRepository.getUpcomingLaunchesNormal(limit = 5)
                result.getOrNull()?.results ?: emptyList()
            } catch (e: Exception) {
                println("Widget: Failed to fetch launches: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
}

@Composable
fun LaunchListWidgetLockedContent() {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
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
fun LaunchListWidgetContent(launches: List<LaunchNormal>) {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
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
fun LaunchListItem(launch: LaunchNormal) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .background(GlanceTheme.colors.surface)
            .padding(8.dp)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("launch_id", launch.id)
                    }
                )
            )
    ) {
        // Launch Name
        Text(
            text = launch.name ?: "Unknown Launch",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Agency
        launch.launchServiceProvider.name.let { agencyName ->
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
                        color = when (launch.status.id) {
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
