package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.calebjones.spacelaunchnow.util.LatLng

/**
 * Platform-specific map view for displaying ISS orbit and current position
 * 
 * @param currentPosition Current ISS position (marker)
 * @param orbitPath List of positions forming the orbit ground track (polyline)
 * @param modifier Compose modifier
 * @param isInteractive Whether the map responds to user gestures (pan, zoom, etc.)
 */
@Composable
expect fun IssMapView(
    currentPosition: LatLng?,
    orbitPath: List<LatLng>,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = false
)
