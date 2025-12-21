package me.calebjones.spacelaunchnow.util

/**
 * Geographic coordinate for mapping
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

/**
 * Simplified orbit ground track calculator for satellite tracking
 * 
 * This uses a simplified approach that approximates the satellite ground track
 * using basic orbital mechanics. For the ISS with its ~51.6° inclination and 
 * ~92 minute orbital period, this provides good visualization.
 * 
 * The approach:
 * 1. Parse TLE to get inclination and mean motion
 * 2. Calculate the ground track using simplified orbital geometry
 * 3. Account for Earth's rotation under the satellite
 * 4. Align the path to pass through the current known position
 */
class OrbitPropagator {

    companion object {
        private const val MINUTES_PER_DAY = 1440.0
        private const val EARTH_ROTATION_DEG_PER_MIN = 360.0 / MINUTES_PER_DAY // 0.25°/min
        private const val DEG_TO_RAD = kotlin.math.PI / 180.0

        /**
         * Generate orbit ground track points aligned with current position
         *
         * @param line1 TLE line 1
         * @param line2 TLE line 2  
         * @param currentPosition Current known position of the satellite
         * @param durationMinutes Total duration in minutes
         * @param numPoints Number of points to generate
         * @return List of LatLng points forming the ground track
         */
        fun generateOrbitPath(
            line1: String,
            line2: String,
            currentPosition: LatLng,
            durationMinutes: Double = 180.0,
            numPoints: Int = 90
        ): List<LatLng> {
            try {
                // Parse TLE for orbital parameters
                val inclination = line2.substring(8, 16).trim().toDouble() // degrees
                val meanMotion = line2.substring(52, 63).trim().toDouble() // rev/day
                
                // Orbital period in minutes
                val orbitalPeriodMin = MINUTES_PER_DAY / meanMotion
                
                // Angular velocity of satellite (degrees per minute)
                val satelliteAngularVelocity = 360.0 / orbitalPeriodMin
                
                co.touchlab.kermit.Logger.d("OrbitPropagator") { 
                    "Inclination: $inclination°, Period: ${orbitalPeriodMin.toInt()} min" 
                }
                co.touchlab.kermit.Logger.d("OrbitPropagator") { 
                    "Current position: lat=${currentPosition.latitude}, lon=${currentPosition.longitude}" 
                }
                
                // Calculate current orbital phase from the current latitude
                // latitude = inclination * sin(orbitalAngle)
                // orbitalAngle = asin(latitude / inclination)
                val latRatio = (currentPosition.latitude / inclination).coerceIn(-1.0, 1.0)
                val currentOrbitalAngle = kotlin.math.asin(latRatio)
                
                // Determine if satellite is heading north or south
                // This affects whether we use the angle directly or (PI - angle)
                // For now, we'll use a simple heuristic based on the current latitude
                
                val startMinutes = -durationMinutes / 2.0
                val intervalMinutes = durationMinutes / numPoints
                
                return (0 until numPoints).map { i ->
                    val minutesFromCenter = startMinutes + (i * intervalMinutes)
                    
                    // Satellite's orbital angle relative to current position
                    val deltaAngle = (satelliteAngularVelocity * minutesFromCenter) * DEG_TO_RAD
                    val orbitalAngle = currentOrbitalAngle + deltaAngle
                    
                    // Latitude oscillates between +/- inclination as satellite orbits
                    val latitude = inclination * kotlin.math.sin(orbitalAngle)
                    
                    // Longitude changes due to:
                    // 1. Satellite moving in its orbit (eastward component)
                    // 2. Earth rotating underneath (westward drift of ground track)
                    val earthRotationDrift = EARTH_ROTATION_DEG_PER_MIN * minutesFromCenter
                    
                    // Satellite's eastward progress depends on orbital position
                    // cos(orbitalAngle) gives the fraction of motion that's eastward
                    val eastwardProgress = (satelliteAngularVelocity * minutesFromCenter) * 
                        kotlin.math.cos(inclination * DEG_TO_RAD)
                    
                    // Net longitude change from current position
                    val deltaLongitude = eastwardProgress - earthRotationDrift
                    var longitude = currentPosition.longitude + deltaLongitude
                    
                    // Normalize longitude to [-180, 180]
                    longitude = ((longitude % 360.0) + 540.0) % 360.0 - 180.0
                    
                    LatLng(latitude, longitude)
                }
            } catch (e: Exception) {
                co.touchlab.kermit.Logger.e("OrbitPropagator", e) { "Error generating orbit path" }
                return emptyList()
            }
        }
        
        /**
         * Legacy method signature for compatibility
         */
        fun generateOrbitPath(
            line1: String,
            line2: String,
            centerTimestamp: Long,
            durationMinutes: Double = 180.0,
            numPoints: Int = 90
        ): List<LatLng> {
            // Without current position, return empty - caller should use the new method
            co.touchlab.kermit.Logger.w("OrbitPropagator") { 
                "generateOrbitPath called without currentPosition - use new signature" 
            }
            return emptyList()
        }
    }
}
