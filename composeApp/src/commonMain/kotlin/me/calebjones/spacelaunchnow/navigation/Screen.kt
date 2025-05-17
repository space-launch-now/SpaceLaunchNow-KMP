package me.calebjones.spacelaunchnow.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Other : Screen("other", "Schedule", Icons.Filled.List)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}