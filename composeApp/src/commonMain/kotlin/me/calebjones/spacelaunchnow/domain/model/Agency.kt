package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Agency(
    val id: Int,
    val name: String,
    val abbrev: String?,
    val typeName: String?,
    val countries: List<Country>,
    val imageUrl: String?,
    val logoUrl: String?,
    val socialLogoUrl: String?,
    val description: String?,
    val administrator: String?,
    val foundingYear: Int?,
    // Detail-tier fields (null for AgencyNormal, populated from AgencyEndpointDetailed)
    val featured: Boolean? = null,
    val infoUrl: String? = null,
    val wikiUrl: String? = null,
    val launchersDescription: String? = null,
    val spacecraftDescription: String? = null,
    val totalLaunchCount: Int? = null,
    val consecutiveSuccessfulLaunches: Int? = null,
    val successfulLaunches: Int? = null,
    val failedLaunches: Int? = null,
    val pendingLaunches: Int? = null,
    val attemptedLandings: Int? = null,
    val successfulLandings: Int? = null,
    val failedLandings: Int? = null,
    val consecutiveSuccessfulLandings: Int? = null,
    val attemptedLandingsSpacecraft: Int? = null,
    val successfulLandingsSpacecraft: Int? = null,
    val failedLandingsSpacecraft: Int? = null
)
