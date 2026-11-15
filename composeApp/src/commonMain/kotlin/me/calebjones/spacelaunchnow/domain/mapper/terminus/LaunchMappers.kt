package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed as LLLaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList as LLPaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList as LLPaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed as TerminusLaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.RocketDetailed as TerminusRocketDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.TimelineEvent as TerminusTimelineEvent
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchBasicList as TerminusPaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchNormalList as TerminusPaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * One launch detail as Terminus serves it in three reads (Tasks#1078): the launch
 * (`GET /launches/{id}/`), its full rocket (`GET /launches/{id}/rocket/`) and its timeline
 * (`GET /launches/{id}/timeline/`). After Terminus slims the launch call, [launch] carries a
 * short rocket and no timeline; [rocket] and [timeline] hold the rest. [rocket] is null when the
 * launch has no rocket.
 */
@Serializable
data class TerminusLaunchDetail(
    val launch: TerminusLaunchDetailed,
    val rocket: TerminusRocketDetailed?,
    val timeline: List<TerminusTimelineEvent>
)

/** The launch with [TerminusLaunchDetail.rocket] and [TerminusLaunchDetail.timeline] filled in. */
fun TerminusLaunchDetail.toDomain(): Launch =
    bridgeLaunchDetailed(launch, rocket, timeline)
        .toDomain()

/** Terminus `GET /launches/mini/` page (LaunchBasic results) to the domain page. */
fun TerminusPaginatedLaunchBasicList.toDomain(): PaginatedResult<Launch> =
    bridge<_, LLPaginatedLaunchBasicList>()
        .toDomain()

/** Terminus `GET /launches/` page (LaunchNormal results) to the domain page. */
fun TerminusPaginatedLaunchNormalList.toDomain(): PaginatedResult<Launch> =
    bridge<_, LLPaginatedLaunchNormalList>()
        .toDomain()
