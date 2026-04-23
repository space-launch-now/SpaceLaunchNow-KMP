package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Immutable
data class AstronautListItem(
    val id: Int,
    val name: String?,
    val statusName: String?,
    val statusId: Int?,
    val agencyName: String?,
    val agencyAbbrev: String?,
    val agencyId: Int?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val age: Int?,
    val bio: String?,
    val typeName: String?,
    val nationality: List<Country>
)

@Immutable
data class AstronautDetail(
    val id: Int,
    val name: String?,
    val statusName: String?,
    val statusId: Int?,
    val agencyName: String?,
    val agencyAbbrev: String?,
    val agencyId: Int?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val age: Int?,
    val bio: String?,
    val typeName: String?,
    val nationality: List<Country>,
    val inSpace: Boolean?,
    val timeInSpace: String?,
    val evaTime: String?,
    val dateOfBirth: LocalDate?,
    val dateOfDeath: LocalDate?,
    val wikiUrl: String?,
    val lastFlight: Instant?,
    val firstFlight: Instant?,
    val socialMediaLinks: List<SocialMediaLink>,
    val flightsCount: Int?,
    val landingsCount: Int?,
    val spacewalksCount: Int?,
    val flights: List<Launch>,
    val landings: List<SpacecraftFlightSummary>,
    val spacewalks: List<SpacewalkSummary>
)

data class SocialMediaLink(
    val id: Int,
    val url: String?,
    val platformName: String?,
    val platformLogoUrl: String?
)

data class SpacewalkSummary(
    val id: Int,
    val name: String?,
    val start: Instant?,
    val end: Instant?,
    val duration: String?
)

data class CrewMember(
    val id: Int,
    val role: String?,
    val astronaut: AstronautListItem
)
