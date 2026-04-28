package me.calebjones.spacelaunchnow.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Country(
    val id: Int,
    val name: String?,
    val alpha2Code: String?,
    val alpha3Code: String?,
    val nationalityName: String?,
    val nationalityNameComposed: String?
)

data class Provider(
    val id: Int,
    val name: String,
    val abbrev: String?,
    val type: String?,
    val countryCode: String?,
    val logoUrl: String?,
    val socialLogo: String?,
    val imageUrl: String?
)

data class ProviderDetail(
    val description: String?,
    val administrator: String?,
    val foundingYear: Int?,
    val totalLaunchCount: Int?,
    val successfulLaunches: Int?,
    val failedLaunches: Int?,
    val pendingLaunches: Int?,
    val consecutiveSuccessfulLaunches: Int?,
    val successfulLandings: Int?,
    val failedLandings: Int?,
    val attemptedLandings: Int?,
    val consecutiveSuccessfulLandings: Int?,
    val infoUrl: String?,
    val wikiUrl: String?
)

data class RocketFamily(
    val id: Int,
    val name: String
)

data class RocketManufacturer(
    val id: Int,
    val name: String?
)

data class RocketConfig(
    val id: Int,
    val name: String,
    val fullName: String?,
    val family: String?,
    val variant: String?,
    val imageUrl: String?,
    val active: Boolean?,
    val reusable: Boolean?,
    val description: String? = null,
    val alias: String? = null,
    val families: List<RocketFamily> = emptyList(),
    val manufacturer: RocketManufacturer? = null,
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
    val fastestTurnaround: String? = null,
    val infoUrl: String? = null,
    val wikiUrl: String? = null
)

data class RocketDetail(
    val stages: List<RocketStage>,
    val spacecraftFlights: List<SpacecraftFlightSummary>,
    val payloads: List<PayloadSummary>
)

data class RocketStage(
    val id: Int,
    val type: String?,
    val reused: Boolean?,
    val launcherFlightNumber: Int?,
    val launcher: LauncherSummary?,
    val landingAttempt: LandingAttemptSummary?,
    val previousFlightDate: Instant? = null,
    val turnAroundTime: String? = null
)

data class LauncherSummary(
    val id: Int,
    val serialNumber: String?,
    val flightProven: Boolean,
    val imageUrl: String?
)

data class LandingLocationSummary(
    val id: Int,
    val name: String?
)

data class LandingAttemptSummary(
    val id: Int,
    val attempt: Boolean?,
    val success: Boolean?,
    val downrangeDistance: Double?,
    val landingLocation: LandingLocationSummary?,
    val outcome: String?,
    val description: String?,
    val location: String?,
    val type: String?
)

data class SpacecraftFlightSummary(
    val id: Int,
    val serialNumber: String?,
    val spacecraftName: String?,
    val destination: String?,
    val missionEnd: Instant?,
    val spacecraft: SpacecraftFlightVehicle? = null,
    val duration: String? = null,
    val turnAroundTime: String? = null,
    val landing: SpacecraftLandingSummary? = null,
    val dockingEvents: List<SpacecraftDockingEventSummary> = emptyList(),
    val launchCrew: List<CrewMemberSummary> = emptyList(),
    val onboardCrew: List<CrewMemberSummary> = emptyList(),
    val landingCrew: List<CrewMemberSummary> = emptyList()
)

data class CrewMemberSummary(
    val astronautId: Int,
    val astronautName: String?,
    val imageUrl: String?,
    val role: String?
)

data class SpacecraftFlightVehicle(
    val id: Int,
    val name: String,
    val status: SpacecraftStatus? = null,
    val serialNumber: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val inSpace: Boolean? = null,
    val isPlaceholder: Boolean? = null,
    val flightsCount: Int? = null,
    val missionEndsCount: Int? = null,
    val timeInSpace: String? = null,
    val timeDocked: String? = null,
    val fastestTurnaround: String? = null
)

data class SpacecraftLandingSummary(
    val type: LandingTypeSummary? = null,
    val landingLocation: LandingLocationSummary? = null
)

data class LandingTypeSummary(val id: Int, val name: String?)

data class SpacecraftDockingEventSummary(
    val id: Int,
    val docking: Instant,
    val departure: Instant? = null,
    val dockingLocation: DockingLocationRef,
    val spaceStationTarget: SpaceStationRef? = null
)

data class DockingLocationRef(val id: Int, val name: String)
data class SpaceStationRef(val id: Int, val name: String)

data class PayloadSummary(
    val id: Int,
    val name: String?,
    val description: String?
)

data class Pad(
    val id: Int,
    val name: String?,
    val latitude: Double?,
    val longitude: Double?,
    val mapUrl: String?,
    val mapImage: String?,
    val totalLaunchCount: Int?,
    val location: Location?,
    val imageUrl: String? = null,
    val description: String? = null,
    val fastestTurnaround: String? = null,
    val orbitalLaunchAttemptCount: Int? = null,
    val infoUrl: String? = null,
    val wikiUrl: String? = null
)

data class Location(
    val id: Int,
    val name: String?,
    val countryCode: String?,
    val countryName: String? = null,
    val countryAlpha2: String? = null,
    val celestialBodyName: String? = null,
    val imageUrl: String? = null,
    val mapImage: String? = null,
    val timezoneName: String? = null,
    val description: String? = null
)

data class Mission(
    val id: Int,
    val name: String?,
    val description: String?,
    val type: String?,
    val orbit: Orbit?,
    val imageUrl: String?
)

data class Orbit(
    val id: Int,
    val name: String,
    val abbrev: String
)

data class LaunchStatus(
    val id: Int,
    val name: String,
    val abbrev: String?,
    val description: String?
)

data class NetPrecision(
    val id: Int,
    val name: String?,
    val abbrev: String?,
    val description: String?
)

data class LaunchAttemptCounts(
    val orbital: Int?,
    val location: Int?,
    val pad: Int?,
    val agency: Int?,
    val orbitalYear: Int?,
    val locationYear: Int?,
    val padYear: Int?,
    val agencyYear: Int?
)

data class ProgramSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val description: String?,
    val infoUrl: String?,
    val wikiUrl: String?,
    val type: String?
)

data class VideoLink(
    val url: String,
    val title: String?,
    val source: String?,
    val publisher: String? = null,
    val description: String?,
    val featureImage: String?,
    val live: Boolean? = null,
    val priority: Int?
)

data class InfoLink(
    val url: String,
    val title: String?,
    val source: String?,
    val description: String?,
    val featureImage: String?,
    val type: String?,
    val priority: Int?
)

data class MissionPatchSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val priority: Int?
)

data class TimelineEntry(
    val type: String?,
    val relativeTime: String?
)

data class UpdateEventRef(
    val id: Int,
    val name: String
)

data class Update(
    val id: Int,
    val profileImage: String?,
    val comment: String?,
    val infoUrl: String?,
    val createdBy: String?,
    val createdOn: Instant?,
    val launch: Launch? = null,
    val event: UpdateEventRef? = null,
    val program: ProgramSummary? = null
)
