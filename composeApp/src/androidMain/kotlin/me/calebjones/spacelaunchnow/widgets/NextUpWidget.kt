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
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject as koinInject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class NextUpWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val launch = fetchNextLaunch()

        provideContent {
            GlanceTheme {
                NextUpWidgetContent(launch)
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
fun NextUpWidgetContent(launch: LaunchNormal?) {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java)
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (launch != null) {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "🚀 NEXT LAUNCH",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onBackground
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Launch Name
                Text(
                    text = launch.name ?: "Unknown Launch",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    ),
                    maxLines = 2
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Agency
                launch.launchServiceProvider.name?.let { agencyName ->
                    Text(
                        text = agencyName,
                        style = TextStyle(
                            fontSize = 11.sp,
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

                // Countdown
                launch.net?.let { netTime ->
                    val countdown = formatCountdown(netTime)
                    Text(
                        text = countdown,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.secondary
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Status
                launch.status?.name?.let { status ->
                    Text(
                        text = status,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = when (launch.status?.id) {
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
                Text(
                    text = "🚀",
                    style = TextStyle(fontSize = 32.sp)
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

private fun formatCountdown(netTime: Instant): String {
    val now = Clock.System.now()
    val duration = netTime - now

    return when {
        duration.isNegative() -> "Launched"
        duration < 1.minutes -> "Less than a minute"
        duration < 1.hours -> {
            val mins = duration.inWholeMinutes
            "T-${mins}m"
        }

        duration < 1.days -> {
            val hours = duration.inWholeHours
            val mins = (duration - hours.hours).inWholeMinutes
            "T-${hours}h ${mins}m"
        }

        duration < 7.days -> {
            val days = duration.inWholeDays
            val hours = (duration - days.days).inWholeHours
            "T-${days}d ${hours}h"
        }

        else -> {
            val days = duration.inWholeDays
            "T-${days} days"
        }
    }
}

class NextUpWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextUpWidget()
}
