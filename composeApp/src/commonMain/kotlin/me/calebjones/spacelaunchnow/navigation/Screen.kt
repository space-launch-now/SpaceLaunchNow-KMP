package me.calebjones.spacelaunchnow.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Serializable routes for type-safe navigation (modern multiplatform approach)
@Serializable
data object Home

@Serializable
data object Schedule

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
data class LaunchDetail(val launchId: String)

@Serializable
data class EventDetail(val eventId: Int)

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
    data object Settings : Screen("Settings", Icons.Filled.Settings)
}