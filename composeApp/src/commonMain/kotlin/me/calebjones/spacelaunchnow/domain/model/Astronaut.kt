package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Immutable
@Serializable
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
    val flights: List<AstronautFlight>,
    val landings: List<SpacecraftFlightSummary>,
    val spacewalks: List<SpacewalkSummary>
)

/**
 * A lightweight reference to one of an astronaut's flights (launch id/name/date only).
 * Deliberately narrower than the domain [Launch] type — neither the retired Launch Library
 * astronaut-detail payload nor Trantor's embedded astronaut-flights payload carries enough
 * to build a full [Launch] (e.g. a real provider/slug), so this type exists to avoid
 * fabricating those fields. Not to be confused with the generated API models of the same
 * name in `api.launchlibrary.models`/`api.trantor.models`.
 */
data class AstronautFlight(
    val launchId: String,
    val launchName: String,
    val net: Instant?
)

data class SocialMediaLink(
    val id: Int,
    val url: String?,
    val platformName: String?,
    val platformLogoUrl: String?
)

@Serializable
data class SpacewalkSummary(
    val id: Int,
    val name: String?,
    val start: Instant?,
    val end: Instant?,
    val duration: String?
)

@Serializable
data class CrewMember(
    val id: Int,
    val role: String?,
    val astronaut: AstronautListItem
)
