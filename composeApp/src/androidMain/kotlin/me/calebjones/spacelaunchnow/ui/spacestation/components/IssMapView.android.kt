package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.util.LatLng
import me.calebjones.spacelaunchnow.util.SolarCalculator
import com.google.android.gms.maps.model.LatLng as GoogleLatLng

/**
 * Android implementation of IssMapView using Google Maps Compose
 */
@Composable
actual fun IssMapView(
    currentPosition: LatLng?,
    orbitPath: List<LatLng>,
    modifier: Modifier,
    isInteractive: Boolean
) {
    // Log current data
    LaunchedEffect(currentPosition, orbitPath.size) {
        currentPosition?.let {
            co.touchlab.kermit.Logger.d("IssMapView") { "ISS Position: lat=${it.latitude}, lon=${it.longitude}" }
        }
        co.touchlab.kermit.Logger.d("IssMapView") { "Orbit path points: ${orbitPath.size}" }
        if (orbitPath.isNotEmpty()) {
            co.touchlab.kermit.Logger.d("IssMapView") { "First point: lat=${orbitPath.first().latitude}, lon=${orbitPath.first().longitude}" }
            co.touchlab.kermit.Logger.d("IssMapView") { "Last point: lat=${orbitPath.last().latitude}, lon=${orbitPath.last().longitude}" }
        }
    }

    // Split orbit path into segments when crossing the antimeridian or when points are too far apart
    val orbitSegments = remember(orbitPath) {
        co.touchlab.kermit.Logger.d("IssMapView") { "Processing orbit path with ${orbitPath.size} points" }
        
        if (orbitPath.isEmpty()) {
            co.touchlab.kermit.Logger.w("IssMapView") { "Orbit path is EMPTY - no lines will be drawn" }
            return@remember emptyList()
        }
        
        val segments = mutableListOf<List<GoogleLatLng>>()
        var currentSegment = mutableListOf<GoogleLatLng>()
        
        for (i in orbitPath.indices) {
            val point = orbitPath[i]
            
            // Validate point
            if (point.latitude < -90 || point.latitude > 90 || point.longitude < -180 || point.longitude > 180) {
                co.touchlab.kermit.Logger.e("IssMapView") { "Invalid point $i: lat=${point.latitude}, lon=${point.longitude}" }
                continue
            }
            
            currentSegment.add(GoogleLatLng(point.latitude, point.longitude))
            
            // Check if we should break the segment - ONLY at antimeridian crossings
            if (i < orbitPath.size - 1) {
                val nextPoint = orbitPath[i + 1]
                
                // Detect antimeridian crossing: longitude signs are opposite and both are near ±180
                // This catches cases like 170° to -170° (crossing eastward) or -170° to 170° (crossing westward)
                val crossesAntimeridian = (point.longitude > 90 && nextPoint.longitude < -90) ||
                                         (point.longitude < -90 && nextPoint.longitude > 90)
                
                if (crossesAntimeridian) {
                    if (currentSegment.size >= 2) {
                        segments.add(currentSegment.toList())
                        co.touchlab.kermit.Logger.d("IssMapView") { 
                            "Breaking at antimeridian: point $i (${point.longitude}°) to point ${i+1} (${nextPoint.longitude}°)" 
                        }
                    }
                    currentSegment = mutableListOf()
                }
            }
        }
        
        // Add the last segment
        if (currentSegment.size >= 2) {
            segments.add(currentSegment)
        }
        
        co.touchlab.kermit.Logger.i("IssMapView") { "✓ Created ${segments.size} segments for drawing" }
        segments.forEachIndexed { index, segment ->
            co.touchlab.kermit.Logger.d("IssMapView") { "Segment $index: ${segment.size} points" }
        }
        
        if (segments.isEmpty()) {
            co.touchlab.kermit.Logger.e("IssMapView") { "ERROR: No segments created from ${orbitPath.size} points!" }
        }
        
        segments
    }

    val googleCurrentPosition = currentPosition?.let {
        GoogleLatLng(it.latitude, it.longitude)
    }

    // Create marker state that will be updated when position changes
    val markerState = rememberMarkerState()

    // Update marker position when currentPosition changes
    LaunchedEffect(googleCurrentPosition) {
        googleCurrentPosition?.let { newPos ->
            markerState.position = newPos
            co.touchlab.kermit.Logger.d("IssMapView") { "Updated marker to: lat=${newPos.latitude}, lon=${newPos.longitude}" }
        }
    }

    // Track if camera has been initialized to avoid forcing zoom level
    var cameraInitialized by remember { mutableStateOf(false) }

    // Camera state - center on current position if available with lower initial zoom
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            googleCurrentPosition ?: GoogleLatLng(0.0, 0.0),
            if (googleCurrentPosition != null) 3f else 1f
        )
    }

    // Update camera position whenever ISS position changes to keep it centered
    LaunchedEffect(googleCurrentPosition) {
        if (googleCurrentPosition != null) {
            if (!cameraInitialized) {
                // First load: set camera with animation
                cameraPositionState.animate(
                    update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                        googleCurrentPosition,
                        3f
                    ),
                    durationMs = 1000
                )
                cameraInitialized = true
            } else {
                // Subsequent updates: smoothly move camera to new position
                cameraPositionState.animate(
                    update = com.google.android.gms.maps.CameraUpdateFactory.newLatLng(googleCurrentPosition),
                    durationMs = 500
                )
            }
        }
    }



    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.SATELLITE,
            isMyLocationEnabled = false,
            minZoomPreference = 1f,  // Allow zooming out to see whole Earth
            maxZoomPreference = 20f  // Allow zooming in for detail
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = isInteractive,
            compassEnabled = isInteractive,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = isInteractive,
            scrollGesturesEnabled = isInteractive,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = isInteractive,
            mapToolbarEnabled = false
        )
    ) {
        // Draw orbit path as multiple polyline segments (split at antimeridian crossings)
        orbitSegments.forEach { segment ->
            if (segment.size >= 2) {
                Polyline(
                    points = segment,
                    color = androidx.compose.ui.graphics.Color.Cyan,
                    width = 5f
                )
            }
        }

        // Draw current position marker
        if (googleCurrentPosition != null) {
            Marker(
                state = markerState,
                title = "ISS",
                snippet = "Lat: ${
                    String.format(
                        "%.4f",
                        googleCurrentPosition.latitude
                    )
                }°, Lon: ${String.format("%.4f", googleCurrentPosition.longitude)}°"
            )
        }
    }
}
