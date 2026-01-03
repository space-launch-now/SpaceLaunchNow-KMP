package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.util.LatLng

/**
 * Desktop stub implementation of IssMapView
 * Shows placeholder text since Google Maps is not available on Desktop
 */
@Composable
actual fun IssMapView(
    currentPosition: LatLng?,
    orbitPath: List<LatLng>,
    modifier: Modifier,
    isInteractive: Boolean
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Map view not available on Desktop\n\n" +
                    currentPosition?.let {
                        "Current Position:\nLat: ${String.format("%.2f", it.latitude)}°\nLon: ${String.format("%.2f", it.longitude)}°"
                    } ?: "Loading position...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}
