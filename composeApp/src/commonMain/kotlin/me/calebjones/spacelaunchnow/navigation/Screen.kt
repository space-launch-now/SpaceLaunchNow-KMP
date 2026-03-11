package me.calebjones.spacelaunchnow.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

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
data object AboutLibraries

@Serializable
data object SupportUs

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
data object Onboarding

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
data class FullscreenVideo(
    val launchId: String,
    val videoUrl: String,
    val launchName: String,
    val videoIndex: Int = 0
)

// Legacy sealed class for UI metadata only
sealed class Screen(val label: String, val icon: ImageVector) {
    data object Home : Screen("Home", Icons.Filled.Home)
    data object Schedule : Screen("Schedule", Icons.AutoMirrored.Filled.List)
    data object Explore : Screen("Explore", Icons.Filled.Explore)
    data object Settings : Screen("Settings", Icons.Filled.Settings)
}