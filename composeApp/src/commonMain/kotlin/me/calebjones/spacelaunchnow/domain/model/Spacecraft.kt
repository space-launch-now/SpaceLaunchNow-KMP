package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

data class SpacecraftStatus(
    val id: Int,
    val name: String?
)

@Immutable
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
