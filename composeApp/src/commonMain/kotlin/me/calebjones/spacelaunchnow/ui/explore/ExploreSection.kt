package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a discoverable section in the Explore screen
 */
data class ExploreSection(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: Any, // @Serializable navigation object
    val contentDescription: String = "Navigate to $title"
)
