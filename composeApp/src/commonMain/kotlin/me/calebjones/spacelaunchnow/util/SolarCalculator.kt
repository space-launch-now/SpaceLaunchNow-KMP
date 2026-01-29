package me.calebjones.spacelaunchnow.util

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Solar position calculator for day/night terminator overlay
 * Based on NOAA solar calculations
 * 
 * Ported from nite-overlay.js by Rossen Georgiev
 * https://github.com/rossengeorgiev/nite-overlay
 */
object SolarCalculator {
    
    private const val RAD = PI / 180.0
    private const val EARTH_RADIUS_METERS = 6371008.0
    
    /**
     * Position data for the sun and shadow
     */
    data class SolarPosition(
        val sunLatitude: Double,
        val sunLongitude: Double,
        val shadowLatitude: Double,
        val shadowLongitude: Double
    )
    
    /**
     * Shadow circle data for rendering twilight zones
     */
    data class ShadowCircle(
        val centerLatitude: Double,
        val centerLongitude: Double,
        val radiusMeters: Double,
        val opacity: Float
    )
    
    /**
     * Complete twilight data for rendering
     */
    data class TwilightOverlay(
        val civilTwilight: ShadowCircle,      // 0.566666° - just below horizon
        val nauticalTwilight: ShadowCircle,   // 6° - horizon barely visible
        val astronomicalTwilight: ShadowCircle, // 12° - sky getting dark
        val night: ShadowCircle               // 18° - full darkness
    )
    
    /**
     * Calculate Julian day from Unix timestamp (milliseconds)
     */
    private fun jday(timestampMs: Long): Double {
        return (timestampMs / 86400000.0) + 2440587.5
    }
    
    /**
     * Calculate the position of the sun at a given time
     * Returns latitude and longitude where the sun is directly overhead (subsolar point)
     * 
     * @param timestampMs Unix timestamp in milliseconds (defaults to current time)
     * @return SolarPosition with sun and shadow coordinates
     */
    fun calculateSunPosition(timestampMs: Long = Clock.System.now().toEpochMilliseconds()): SolarPosition {
        // Convert to date components in UTC
        val instant = Instant.fromEpochMilliseconds(timestampMs)
        val dateTime = instant.toLocalDateTime(TimeZone.UTC)
        
        val hours = dateTime.hour
        val minutes = dateTime.minute
        val seconds = dateTime.second
        val millis = dateTime.nanosecond / 1_000_000  // Convert nanoseconds to milliseconds
        
        val msPastMidnight = ((hours * 60 + minutes) * 60 + seconds) * 1000 + millis
        
        // Julian century
        val jc = (jday(timestampMs) - 2451545) / 36525
        
        // Mean longitude of the sun
        val meanLongSun = (280.46646 + jc * (36000.76983 + jc * 0.0003032)) % 360
        
        // Mean anomaly of the sun
        val meanAnomSun = 357.52911 + jc * (35999.05029 - 0.0001537 * jc)
        
        // Sun equation of center
        val sunEq = sin(RAD * meanAnomSun) * (1.914602 - jc * (0.004817 + 0.000014 * jc)) +
                sin(RAD * 2 * meanAnomSun) * (0.019993 - 0.000101 * jc) +
                sin(RAD * 3 * meanAnomSun) * 0.000289
        
        // Sun true longitude
        val sunTrueLong = meanLongSun + sunEq
        
        // Sun apparent longitude
        val sunAppLong = sunTrueLong - 0.00569 - 0.00478 * sin(RAD * 125.04 - 1934.136 * jc)
        
        // Mean obliquity of the ecliptic
        val meanObliqEcliptic = 23 + (26 + ((21.448 - jc * (46.815 + jc * (0.00059 - jc * 0.001813)))) / 60) / 60
        
        // Corrected obliquity
        val obliqCorr = meanObliqEcliptic + 0.00256 * cos(RAD * 125.04 - 1934.136 * jc)
        
        // Sun declination (latitude)
        val lat = asin(sin(RAD * obliqCorr) * sin(RAD * sunAppLong)) / RAD
        
        // Eccentricity of Earth's orbit
        val eccent = 0.016708634 - jc * (0.000042037 + 0.0000001267 * jc)
        
        // y factor for equation of time
        val y = tan(RAD * (obliqCorr / 2)) * tan(RAD * (obliqCorr / 2))
        
        // Equation of time (minutes)
        val eqOfTime = 4 * ((y * sin(2 * RAD * meanLongSun) -
                2 * eccent * sin(RAD * meanAnomSun) +
                4 * eccent * y * sin(RAD * meanAnomSun) * cos(2 * RAD * meanLongSun) -
                0.5 * y * y * sin(4 * RAD * meanLongSun) -
                1.25 * eccent * eccent * sin(2 * RAD * meanAnomSun)) / RAD)
        
        // True solar time in degrees
        val trueSolarTimeInDeg = ((msPastMidnight + eqOfTime * 60000) % 86400000) / 240000.0
        
        // Sun longitude
        val lng = if (trueSolarTimeInDeg < 0) {
            -(trueSolarTimeInDeg + 180)
        } else {
            -(trueSolarTimeInDeg - 180)
        }
        
        // Shadow position is opposite to sun (where it's dark)
        // Matching the JS implementation exactly: (-lat, lng + 180)
        val shadowLat = -lat
        var shadowLng = lng + 180
        // Normalize to [-180, 180] if needed
        if (shadowLng > 180) shadowLng -= 360
        if (shadowLng < -180) shadowLng += 360
        
        return SolarPosition(
            sunLatitude = lat,
            sunLongitude = lng,
            shadowLatitude = shadowLat,
            shadowLongitude = shadowLng
        )
    }
    
    /**
     * Calculate shadow radius from angle below horizon
     * 
     * Circles are centered on the SHADOW (antisolar) point.
     * Radius is half Earth circumference MINUS the twilight angle offset.
     * This creates smaller circles as we go deeper into night.
     * 
     * @param angle Degrees below horizon (0 = horizon, 6 = nautical twilight, etc.)
     * @return Radius in meters
     */
    private fun getShadowRadiusFromAngle(angle: Double): Double {
        // Half Earth circumference (from shadow center to terminator line)
        val halfCircumference = EARTH_RADIUS_METERS * PI
        // Distance per degree on Earth's surface
        val metersPerDegree = (EARTH_RADIUS_METERS * 2 * PI) / 360
        // Subtract the twilight angle - larger angles = smaller (darker) circles
        return halfCircumference - (metersPerDegree * angle)
    }
    
    /**
     * Get complete twilight overlay data for rendering on a map
     * 
     * Circles are centered on the SHADOW (antisolar) point with radii
     * that decrease as we go deeper into night (smaller = darker).
     * 
     * @param timestampMs Unix timestamp in milliseconds (defaults to current time)
     * @return TwilightOverlay with all shadow circles
     */
    fun getTwilightOverlay(timestampMs: Long = Clock.System.now().toEpochMilliseconds()): TwilightOverlay {
        val position = calculateSunPosition(timestampMs)
        
        // Center on SHADOW position, with decreasing radius for darker zones
        return TwilightOverlay(
            civilTwilight = ShadowCircle(
                centerLatitude = position.shadowLatitude,
                centerLongitude = position.shadowLongitude,
                radiusMeters = getShadowRadiusFromAngle(0.566666),
                opacity = 0.15f
            ),
            nauticalTwilight = ShadowCircle(
                centerLatitude = position.shadowLatitude,
                centerLongitude = position.shadowLongitude,
                radiusMeters = getShadowRadiusFromAngle(6.0),
                opacity = 0.25f
            ),
            astronomicalTwilight = ShadowCircle(
                centerLatitude = position.shadowLatitude,
                centerLongitude = position.shadowLongitude,
                radiusMeters = getShadowRadiusFromAngle(12.0),
                opacity = 0.35f
            ),
            night = ShadowCircle(
                centerLatitude = position.shadowLatitude,
                centerLongitude = position.shadowLongitude,
                radiusMeters = getShadowRadiusFromAngle(18.0),
                opacity = 0.5f
            )
        )
    }
}
