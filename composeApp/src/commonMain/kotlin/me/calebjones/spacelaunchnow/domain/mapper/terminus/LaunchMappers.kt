package me.calebjones.spacelaunchnow.domain.mapper.terminus

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed as LLLaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList as LLPaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList as LLPaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed as TerminusLaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchBasicList as TerminusPaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchNormalList as TerminusPaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

fun TerminusLaunchDetailed.toDomain(): Launch =
    bridge<_, LLLaunchDetailed>()
        .toDomain()

/** Terminus `GET /launches/mini/` page (LaunchBasic results) to the domain page. */
fun TerminusPaginatedLaunchBasicList.toDomain(): PaginatedResult<Launch> =
    bridge<_, LLPaginatedLaunchBasicList>()
        .toDomain()

/** Terminus `GET /launches/` page (LaunchNormal results) to the domain page. */
fun TerminusPaginatedLaunchNormalList.toDomain(): PaginatedResult<Launch> =
    bridge<_, LLPaginatedLaunchNormalList>()
        .toDomain()
