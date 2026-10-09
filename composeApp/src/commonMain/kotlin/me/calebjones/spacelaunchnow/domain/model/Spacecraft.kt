package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

// @Serializable here only: SpacecraftStatus is reachable from the domain Launch graph via
// RocketDetail -> SpacecraftFlightSummary -> SpacecraftFlightVehicle.status, so it must be
// serializable for the launch cache (ADR-0004) even though SpacecraftLocalDataSource itself
// is out of this unit's scope. Spacecraft/SpacecraftConfig below are untouched.
@Serializable
data class SpacecraftStatus(
    val id: Int,
    val name: String?
)

@Immutable
@Serializable
data class Spacecraft(
    val id: Int,
    val name: String,
    val serialNumber: String?,
    val status: SpacecraftStatus? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val config: SpacecraftConfig? = null
)

@Immutable
@Serializable
data class SpacecraftConfig(
    val id: Int,
    val name: String,
    val type: String? = null,
    val agency: Provider? = null,
    val imageUrl: String? = null,
    val inUse: Boolean? = null,
    val capability: String? = null,
    val history: String? = null,
    val details: String? = null,
    val maidenFlight: LocalDate? = null,
    val humanRated: Boolean? = null,
    val crewCapacity: Int? = null,
    val payloadCapacity: Int? = null
)
