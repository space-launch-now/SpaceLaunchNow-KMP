package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.pointed
import kotlinx.cinterop.get
import me.calebjones.spacelaunchnow.util.LatLng
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSSelectorFromString
import platform.MapKit.MKCircle
import platform.MapKit.MKCircleRenderer
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapTypeSatellite
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPinAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.UIKit.UIColor
import platform.darwin.NSObject

/**
 * iOS implementation of IssMapView using Apple Maps (MKMapView).
 * 
 * Uses native MapKit framework directly via Kotlin/Native interop.
 * No additional SDK setup required - MapKit is built into iOS.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun IssMapView(
    currentPosition: LatLng?,
    orbitPath: List<LatLng>,
    modifier: Modifier,
    isInteractive: Boolean
) {
    val log = remember { Logger.withTag("IssMapView") }
    
    // Debug: Log what we received
    log.i { "IssMapView called with currentPosition=$currentPosition, orbitPath.size=${orbitPath.size}, isInteractive=$isInteractive" }
    log.i { "DEBUG: currentPosition is ${if (currentPosition == null) "NULL" else "lat=${currentPosition.latitude}, lon=${currentPosition.longitude}"}" }
    
    // Create the map delegate for polyline and circle rendering
    val mapDelegate = remember { IssMapViewDelegate() }
    
    // Track the current annotation and overlays for updates
    val issAnnotation = remember { MKPointAnnotation() }
    
    // Track the ISS position circle marker (using a simple var in remember)
    val issCircleHolder = remember { mutableListOf<MKCircle?>(null) }
    
    // Track added polylines for removal on update
    val currentPolylines = remember { mutableListOf<MKPolyline>() }
    
    UIKitView(
        factory = {
            log.d { "Creating MKMapView" }
            MKMapView().apply {
                // Configure map type
                mapType = MKMapTypeSatellite
                
                // Set delegate for overlay rendering
                delegate = mapDelegate
                
                // Configure interaction based on parameter
                scrollEnabled = isInteractive
                zoomEnabled = isInteractive
                rotateEnabled = isInteractive
                pitchEnabled = false // Disable 3D tilt
                
                // Show compass only in interactive mode
                showsCompass = isInteractive
                
                // Set initial region if we have a position
                currentPosition?.let { pos ->
                    log.i { "currentPosition: ISS position lat=${pos.latitude}, lon=${pos.longitude}" }
                    val coordinate = CLLocationCoordinate2DMake(pos.latitude, pos.longitude)
                    
                    // Add ISS annotation with proper coordinate
                    issAnnotation.setTitle("ISS")
                    issAnnotation.setCoordinate(coordinate)
                    addAnnotation(issAnnotation)
                    
                    // Center map on ISS with zoom level to see ISS clearly
                    val region = MKCoordinateRegionMakeWithDistance(
                        coordinate,
                        5_000_000.0, // 5,000 km span - closer zoom to see ISS
                        5_000_000.0
                    )
                    setRegion(region, animated = false)
                    log.i { "Set map region centered on ISS at lat=${pos.latitude}, lon=${pos.longitude}" }
                } ?: run {
                    log.w { "Factory: No ISS position available - will show default location" }
                }
                
                // Add initial orbit path
                addOrbitPolylines(this, orbitPath, currentPolylines, log)
                
                // Add ISS circle AFTER polylines so it renders on top
                currentPosition?.let { pos ->
                    val coordinate = CLLocationCoordinate2DMake(pos.latitude, pos.longitude)
                    val circle = MKCircle.circleWithCenterCoordinate(
                        coord = coordinate,
                        radius = 100_000.0 // 100 km radius
                    )
                    issCircleHolder[0] = circle
                    addOverlayCompat(circle as MKOverlayProtocol)
                    log.i { "currentPosition: Added ISS circle overlay at lat=${pos.latitude}, lon=${pos.longitude}" }
                }
            }
        },
        modifier = modifier,
        update = { mapView ->
            // Update ISS position and circle marker
            currentPosition?.let { pos ->
                log.i { "Update: ISS position lat=${pos.latitude}, lon=${pos.longitude}" }
                val coordinate = CLLocationCoordinate2DMake(pos.latitude, pos.longitude)
                issAnnotation.setCoordinate(coordinate)
                
                // Remove old circle and add new one at updated position
                issCircleHolder[0]?.let { oldCircle ->
                    mapView.removeOverlayCompat(oldCircle as MKOverlayProtocol)
                    log.d { "Removed old ISS circle" }
                }
                
                val newCircle = MKCircle.circleWithCenterCoordinate(
                    coord = coordinate,
                    radius = 100_000.0 // 100 km radius
                )
                issCircleHolder[0] = newCircle
                mapView.addOverlayCompat(newCircle as MKOverlayProtocol)
                log.i { "Updated ISS circle to lat=${pos.latitude}, lon=${pos.longitude}" }
                
                // Always re-center map on ISS position (even in interactive mode for now to debug)
                val region = MKCoordinateRegionMakeWithDistance(
                    coordinate,
                    5_000_000.0,
                    5_000_000.0
                )
                mapView.setRegion(region, animated = !isInteractive)
                log.i { "Re-centered map on ISS at lat=${pos.latitude}, lon=${pos.longitude}" }
            } ?: run {
                log.w { "Update: No ISS position available!" }
            }
            
            // Update interaction settings
            mapView.scrollEnabled = isInteractive
            mapView.zoomEnabled = isInteractive
            mapView.rotateEnabled = isInteractive
            mapView.showsCompass = isInteractive
            
            // Update orbit path - remove old polylines and add new ones
            if (currentPolylines.isNotEmpty()) {
                currentPolylines.forEach { polyline ->
                    mapView.removeOverlayCompat(polyline as MKOverlayProtocol)
                }
            }
            currentPolylines.clear()
            addOrbitPolylines(mapView, orbitPath, currentPolylines, log)
        }
    )
}

/**
 * Adds orbit polylines to the map, splitting at antimeridian crossings.
 * This prevents lines from wrapping around the entire map.
 */
@OptIn(ExperimentalForeignApi::class)
private fun addOrbitPolylines(
    mapView: MKMapView, 
    orbitPath: List<LatLng>, 
    polylineTracker: MutableList<MKPolyline>,
    log: Logger
) {
    if (orbitPath.isEmpty()) {
        log.w { "Orbit path is empty - no lines will be drawn" }
        return
    }
    
    log.d { "Processing orbit path with ${orbitPath.size} points" }
    
    // Split path into segments at antimeridian crossings
    val segments = mutableListOf<List<LatLng>>()
    var currentSegment = mutableListOf<LatLng>()
    
    for (i in orbitPath.indices) {
        val point = orbitPath[i]
        
        // Validate point
        if (point.latitude < -90 || point.latitude > 90 || 
            point.longitude < -180 || point.longitude > 180) {
            log.e { "Invalid point $i: lat=${point.latitude}, lon=${point.longitude}" }
            continue
        }
        
        currentSegment.add(point)
        
        // Check for antimeridian crossing
        if (i < orbitPath.size - 1) {
            val nextPoint = orbitPath[i + 1]
            
            // Detect antimeridian crossing: longitude signs are opposite and both are near ±180
            val crossesAntimeridian = (point.longitude > 90 && nextPoint.longitude < -90) ||
                                       (point.longitude < -90 && nextPoint.longitude > 90)
            
            if (crossesAntimeridian) {
                if (currentSegment.size >= 2) {
                    segments.add(currentSegment.toList())
                    log.d { "Breaking at antimeridian: point $i (${point.longitude}°) to point ${i+1} (${nextPoint.longitude}°)" }
                }
                currentSegment = mutableListOf()
            }
        }
    }
    
    // Add the last segment
    if (currentSegment.size >= 2) {
        segments.add(currentSegment)
    }
    
    log.i { "✓ Created ${segments.size} segments for drawing" }
    
    segments.forEachIndexed { index, segment ->
        memScoped {
            val coordinates = allocArray<CLLocationCoordinate2D>(segment.size)
            segment.forEachIndexed { idx, point ->
                coordinates[idx].latitude = point.latitude
                coordinates[idx].longitude = point.longitude
            }

            val polyline = MKPolyline.polylineWithCoordinates(
                coords = coordinates,
                count = segment.size.toULong()
            )

            mapView.addOverlayCompat(polyline as MKOverlayProtocol)
            polylineTracker.add(polyline)
            log.d { "Segment $index: ${segment.size} points added" }
        }
    }
}

private fun MKMapView.addOverlayCompat(overlay: MKOverlayProtocol) {
    performSelector(NSSelectorFromString("addOverlay:"), withObject = overlay)
}

private fun MKMapView.removeOverlayCompat(overlay: MKOverlayProtocol) {
    performSelector(NSSelectorFromString("removeOverlay:"), withObject = overlay)
}

/**
 * MKMapView delegate for customizing overlay rendering.
 * Renders orbit polylines with cyan color and ISS position circle with bright red fill.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IssMapViewDelegate : NSObject(), MKMapViewDelegateProtocol {
    
    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        val log = Logger.withTag("IssMapViewDelegate")
        
        return when (rendererForOverlay) {
            is MKPolyline -> {
                log.d { "Rendering polyline" }
                MKPolylineRenderer(polyline = rendererForOverlay).apply {
                    strokeColor = UIColor.cyanColor
                    lineWidth = 3.0
                }
            }
            is MKCircle -> {
                log.i { "Rendering ISS circle with dark red color" }
                MKCircleRenderer(circle = rendererForOverlay).apply {
                    // Dark red fill - RGB(139, 0, 0) = Dark Red
                    fillColor = UIColor(red = 139.0/255.0, green = 0.0, blue = 0.0, alpha = 1.0)
                    strokeColor = UIColor.whiteColor
                    lineWidth = 3.0
                }
            }
            else -> {
                log.w { "Rendering unknown overlay type: ${rendererForOverlay::class.simpleName}" }
                MKOverlayRenderer(overlay = rendererForOverlay)
            }
        }
    }
}
