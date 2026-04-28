package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Immutable
data class VehicleConfig(
    val id: Int,
    val name: String,
    val fullName: String?,
    val family: String?,
    val variant: String?,
    val imageUrl: String?,
    val description: String? = null,
    val infoUrl: String? = null,
    val wikiUrl: String? = null,
    val manufacturerName: String? = null,
    val minStage: Int? = null,
    val maxStage: Int? = null,
    val length: Double? = null,
    val diameter: Double? = null,
    val launchMass: Double? = null,
    val leoCapacity: Double? = null,
    val gtoCapacity: Double? = null,
    val geoCapacity: Double? = null,
    val ssoCapacity: Double? = null,
    val toThrust: Double? = null,
    val apogee: Double? = null,
    val launchCost: Int? = null,
    val totalLaunchCount: Int? = null,
    val successfulLaunches: Int? = null,
    val failedLaunches: Int? = null,
    val pendingLaunches: Int? = null,
    val consecutiveSuccessfulLaunches: Int? = null,
    val attemptedLandings: Int? = null,
    val successfulLandings: Int? = null,
    val failedLandings: Int? = null,
    val consecutiveSuccessfulLandings: Int? = null,
    val maidenFlight: LocalDate? = null,
    val active: Boolean? = null,
    val reusable: Boolean? = null
)

data class LauncherStatus(
    val id: Int,
    val name: String?
)

@Immutable
data class LauncherDetail(
    val id: Int,
    val serialNumber: String?,
    val flightProven: Boolean,
    val imageUrl: String?,
    val flights: Int? = null,
    val lastLaunchDate: Instant? = null,
    val firstLaunchDate: Instant? = null,
    val status: LauncherStatus? = null,
    val details: String? = null
)
