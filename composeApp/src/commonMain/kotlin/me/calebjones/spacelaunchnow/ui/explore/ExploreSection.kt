package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res

/**
 * Represents a discoverable section in the Explore screen
 */
data class ExploreSection(
    val id: String,
    val title: StringResource,
    val description: StringResource,
    val icon: ImageVector,
    val route: Any, // @Serializable navigation object
    val contentDescription: StringResource
)
