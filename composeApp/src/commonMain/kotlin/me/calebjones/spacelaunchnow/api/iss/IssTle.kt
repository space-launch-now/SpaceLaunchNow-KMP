package me.calebjones.spacelaunchnow.api.iss

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * TLE (Two-Line Element) data for ISS from wheretheiss.at API
 * GET https://api.wheretheiss.at/v1/satellites/25544/tles
 */
@Serializable
data class IssTle(
    @SerialName("requested_timestamp") val requestedTimestamp: Long,
    @SerialName("tle_timestamp") val tleTimestamp: Long,
    @SerialName("id") val id: String, // NORAD ID as string
    @SerialName("name") val name: String,
    @SerialName("header") val header: String, // TLE header line
    @SerialName("line1") val line1: String, // TLE line 1
    @SerialName("line2") val line2: String // TLE line 2
)
