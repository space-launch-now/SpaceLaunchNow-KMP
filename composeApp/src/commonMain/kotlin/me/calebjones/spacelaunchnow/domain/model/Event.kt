package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class Event(
    val id: Int,
    val name: String,
    val slug: String,
    val type: EventType,
    val description: String?,
    val date: Instant?,
    val location: String?,
    val imageUrl: String?,
    val webcastLive: Boolean,
    val lastUpdated: Instant?,
    val duration: String?,
    val datePrecision: NetPrecision?,
    val infoUrls: List<InfoLink> = emptyList(),
    val vidUrls: List<VideoLink> = emptyList(),
    val updates: List<Update> = emptyList(),
    // Detailed-only fields (empty when mapped from EventEndpointNormal)
    val agencies: List<Provider> = emptyList(),
    val launches: List<Launch> = emptyList(),
    val expeditions: List<ExpeditionSummary> = emptyList(),
    val spaceStations: List<SpaceStationSummary> = emptyList(),
    val programs: List<ProgramSummary> = emptyList(),
    val astronauts: List<AstronautSummary> = emptyList()
)

data class EventType(
    val id: Int,
    val name: String?
)

data class ExpeditionSummary(
    val id: Int,
    val name: String?,
    val start: Instant?,
    val end: Instant?,
    val imageUrl: String? = null
)

data class SpaceStationSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val orbit: String? = null
)

data class AstronautSummary(
    val id: Int,
    val name: String,
    val nationality: String?,
    val profileImageUrl: String?,
    val status: String?
)
