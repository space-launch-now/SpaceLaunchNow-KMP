package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class Launch(
    val id: String,
    val name: String,
    val slug: String,
    val net: Instant?,
    val windowStart: Instant?,
    val windowEnd: Instant?,
    val lastUpdated: Instant?,
    val status: LaunchStatus?,
    val provider: Provider,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val infographic: String?,
    val netPrecision: NetPrecision?,
    // Normal+ fields (null when mapped from LaunchBasic)
    val rocket: RocketConfig? = null,
    val mission: Mission? = null,
    val pad: Pad? = null,
    val programs: List<ProgramSummary> = emptyList(),
    val probability: Int? = null,
    val weatherConcerns: String? = null,
    val failreason: String? = null,
    val hashtag: String? = null,
    val webcastLive: Boolean = false,
    val launchAttemptCounts: LaunchAttemptCounts? = null,
    // Detailed-only fields (null/empty when mapped from Basic or Normal)
    val updates: List<Update> = emptyList(),
    val infoUrls: List<InfoLink> = emptyList(),
    val vidUrls: List<VideoLink> = emptyList(),
    val timeline: List<TimelineEntry> = emptyList(),
    val missionPatches: List<MissionPatchSummary> = emptyList(),
    val rocketDetail: RocketDetail? = null,
    val flightclubUrl: String? = null,
    val padTurnaround: String? = null,
    val providerDetail: ProviderDetail? = null
)
