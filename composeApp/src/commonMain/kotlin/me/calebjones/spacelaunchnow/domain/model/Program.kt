package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

/**
 * Full domain representation of a space program, expanding on the lightweight
 * [ProgramSummary] used as a nested reference in other domain types.
 */
@Immutable
data class Program(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val infoUrl: String? = null,
    val wikiUrl: String? = null,
    val type: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val agencies: List<Provider> = emptyList(),
    val missionPatches: List<MissionPatchSummary> = emptyList(),
    val vidUrls: List<VideoLink> = emptyList()
)
