package me.calebjones.spacelaunchnow.api.iss

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ISS position data from wheretheiss.at API
 * GET https://api.wheretheiss.at/v1/satellites/25544
 */
@Serializable
data class IssPosition(
    @SerialName("name") val name: String,
    @SerialName("id") val id: Int,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("altitude") val altitude: Double, // kilometers
    @SerialName("velocity") val velocity: Double, // km/h
    @SerialName("visibility") val visibility: String, // "daylight", "eclipsed"
    @SerialName("footprint") val footprint: Double, // km diameter
    @SerialName("timestamp") val timestamp: Long, // Unix timestamp
    @SerialName("daynum") val daynum: Double, // Julian day number
    @SerialName("solar_lat") val solarLat: Double,
    @SerialName("solar_lon") val solarLon: Double,
    @SerialName("units") val units: String // "kilometers" or "miles"
)
