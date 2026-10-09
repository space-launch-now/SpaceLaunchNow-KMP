package me.calebjones.spacelaunchnow.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
/**
 * `kotlinx.datetime.Instant` is a deprecated typealias for `kotlin.time.Instant` (see
 * kotlinx-datetime 0.8.0), which kotlinx-datetime no longer ships a ready-made
 * `@Serializable` object for (only the abstract `FormattedInstantSerializer` base remains).
 * `Instant.toString()`/`Instant.parse()` already round-trip ISO-8601, so this is a minimal
 * wrapper rather than a custom format. Used to make the domain `Launch` graph (ADR-0004:
 * docs/architecture/adr/0004-cache-schema-versioning.md) serializable for the launch cache.
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("me.calebjones.spacelaunchnow.domain.model.Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
@Serializable
data class Country(
    val id: Int,
    val name: String?,
    val alpha2Code: String?,
    val alpha3Code: String?,
    val nationalityName: String?,
    val nationalityNameComposed: String?
)

@Serializable
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

@Serializable
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

@Serializable
data class RocketFamily(
    val id: Int,
    val name: String
)

@Serializable
data class RocketManufacturer(
    val id: Int,
    val name: String?
)

@Serializable
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
    @Serializable(with = LocalDateIso8601Serializer::class)
    val maidenFlight: LocalDate? = null,
    val fastestTurnaround: String? = null,
    val infoUrl: String? = null,
    val wikiUrl: String? = null
)

@Serializable
data class RocketDetail(
    val stages: List<RocketStage>,
    val spacecraftFlights: List<SpacecraftFlightSummary>,
    val payloads: List<PayloadSummary>
)

@Serializable
data class RocketStage(
    val id: Int,
    val type: String?,
    val reused: Boolean?,
    val launcherFlightNumber: Int?,
    val launcher: LauncherSummary?,
    val landingAttempt: LandingAttemptSummary?,
    @Serializable(with = InstantSerializer::class)
    val previousFlightDate: Instant? = null,
    val turnAroundTime: String? = null
)

@Serializable
data class LauncherSummary(
    val id: Int,
    val serialNumber: String?,
    val flightProven: Boolean,
    val imageUrl: String?
)

@Serializable
data class LandingLocationSummary(
    val id: Int,
    val name: String?
)

@Serializable
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

@Serializable
data class SpacecraftFlightSummary(
    val id: Int,
    val serialNumber: String?,
    val spacecraftName: String?,
    val destination: String?,
    @Serializable(with = InstantSerializer::class)
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

@Serializable
data class CrewMemberSummary(
    val astronautId: Int,
    val astronautName: String?,
    val imageUrl: String?,
    val role: String?
)

@Serializable
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

@Serializable
data class SpacecraftLandingSummary(
    val type: LandingTypeSummary? = null,
    val landingLocation: LandingLocationSummary? = null
)

@Serializable
data class LandingTypeSummary(val id: Int, val name: String?)

@Serializable
data class SpacecraftDockingEventSummary(
    val id: Int,
    @Serializable(with = InstantSerializer::class)
    val docking: Instant,
    @Serializable(with = InstantSerializer::class)
    val departure: Instant? = null,
    val dockingLocation: DockingLocationRef,
    val spaceStationTarget: SpaceStationRef? = null
)

@Serializable
data class DockingLocationRef(val id: Int, val name: String)
@Serializable
data class SpaceStationRef(val id: Int, val name: String)

@Serializable
data class PayloadSummary(
    val id: Int,
    val name: String?,
    val description: String?
)

@Serializable
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

@Serializable
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

@Serializable
data class Mission(
    val id: Int,
    val name: String?,
    val description: String?,
    val type: String?,
    val orbit: Orbit?,
    val imageUrl: String?
)

@Serializable
data class Orbit(
    val id: Int,
    val name: String,
    val abbrev: String
)

@Serializable
data class LaunchStatus(
    val id: Int,
    val name: String,
    val abbrev: String?,
    val description: String?
)

@Serializable
data class NetPrecision(
    val id: Int,
    val name: String?,
    val abbrev: String?,
    val description: String?
)

@Serializable
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

@Serializable
data class ProgramSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val description: String?,
    val infoUrl: String?,
    val wikiUrl: String?,
    val type: String?
)

@Serializable
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

@Serializable
data class InfoLink(
    val url: String,
    val title: String?,
    val source: String?,
    val description: String?,
    val featureImage: String?,
    val type: String?,
    val priority: Int?
)

@Serializable
data class MissionPatchSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val priority: Int?
)

@Serializable
data class TimelineEntry(
    val type: String?,
    val relativeTime: String?
)

@Serializable
data class UpdateEventRef(
    val id: Int,
    val name: String
)

// Trantor's standalone updates feed denormalizes the related launch to launch_id/launch_name
// rather than embedding a full launch (unlike LL's UpdateEndpoint, which nested a real
// LaunchBasic). A ref matches what's actually available; callers only ever use id/name.
@Serializable
data class LaunchRef(
    val id: String,
    val name: String
)
@Serializable
data class Update(
    val id: Int,
    val profileImage: String?,
    val comment: String?,
    val infoUrl: String?,
    val createdBy: String?,
    @Serializable(with = InstantSerializer::class)
    val createdOn: Instant?,
    val launch: LaunchRef? = null,
    val event: UpdateEventRef? = null,
    val program: ProgramSummary? = null
)
