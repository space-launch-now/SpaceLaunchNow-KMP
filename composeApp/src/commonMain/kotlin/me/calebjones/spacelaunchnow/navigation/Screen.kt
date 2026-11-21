package me.calebjones.spacelaunchnow.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import spacelaunchnow_kmp.composeapp.generated.resources.*

// Serializable routes for type-safe navigation (modern multiplatform approach)
@Serializable
data object Home

@Serializable
data object Schedule

@Serializable
data object Explore

@Serializable
data object Settings

@Serializable
data object NotificationSettings

@Serializable
data object DebugSettings

@Serializable
data object Diagnostics

@Serializable
data object AboutLibraries

@Serializable
data class SupportUs(
    /** [me.calebjones.spacelaunchnow.analytics.events.PaywallSource.value] of the entry that opened the paywall. */
    val source: String = me.calebjones.spacelaunchnow.analytics.events.PaywallSource.SUPPORT_US.value
)

@Serializable
data object ThemeCustomization

@Serializable
data object CalendarSync

@Serializable
data object Roadmap

@Serializable
data class LaunchDetail(val launchId: String)

@Serializable
data class EventDetail(val eventId: Int)

@Serializable
data class NewsDetail(val url: String, val title: String = "")

@Serializable
data object Onboarding

@Serializable
data object Preload

@Serializable
data object LiveOnboarding

@Serializable
data object Rockets

@Serializable
data class RocketDetail(val rocketId: Int)

@Serializable
data object Agencies

@Serializable
data class AgencyDetail(val agencyId: Int)

@Serializable
data object Astronauts

@Serializable
data class AstronautDetail(val astronautId: Int)

@Serializable
data class SpaceStationDetail(val stationId: Int)

@Serializable
data object Starship

@Serializable
data class NewsEvents(
    /** Index of the tab to open: 0 = News, 1 = Events. Defaults to News. */
    val initialTab: Int = 0
)

@Serializable
data class FullscreenVideo(
    val launchId: String,
    val videoUrl: String,
    val launchName: String,
    val videoIndex: Int = 0
)

// Legacy sealed class for UI metadata only
sealed class Screen(val label: StringResource, val icon: ImageVector) {
    data object Home : Screen(Res.string.app_layout_nav_home_label, Icons.Filled.Home)
    data object Schedule : Screen(Res.string.app_layout_nav_schedule_label, Icons.AutoMirrored.Filled.List)
    data object Explore : Screen(Res.string.common_explore, Icons.Filled.Explore)
    data object NewsEvents : Screen(Res.string.common_news, Icons.Filled.Newspaper)
    data object Settings : Screen(Res.string.common_settings, Icons.Filled.Settings)
}