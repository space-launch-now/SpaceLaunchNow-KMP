package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Immutable
data class SpaceStationDetail(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val statusName: String?,
    val statusId: Int?,
    val founded: LocalDate?,
    val deorbited: LocalDate?,
    val description: String?,
    val orbit: String?,
    val typeName: String?,
    val owners: List<Agency>,
    val activeExpeditions: List<ExpeditionMiniItem>,
    val dockingLocations: List<DockingLocation>,
    val height: Double?,
    val width: Double?,
    val mass: Double?,
    val volume: Double?,
    val onboardCrew: Int?,
    val dockedVehicles: Int?
)

@Immutable
data class ExpeditionDetailItem(
    val id: Int,
    val name: String?,
    val start: Instant?,
    val end: Instant?,
    val crew: List<CrewMember>,
    val missionPatches: List<MissionPatchSummary>,
    val spacewalks: List<SpacewalkSummary>
)

data class ExpeditionMiniItem(
    val id: Int,
    val name: String?,
    val start: Instant?,
    val end: Instant?
)

data class DockingLocation(
    val id: Int,
    val name: String,
    val currentlyDocked: DockingEvent?
)

data class DockingEvent(
    val id: Int,
    val docking: Instant,
    val departure: Instant?,
    val vehicleName: String?,
    val vehicleConfigName: String?,
    val vehicleImageUrl: String?
)
