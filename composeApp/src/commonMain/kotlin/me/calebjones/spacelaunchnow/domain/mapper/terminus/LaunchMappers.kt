package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed as LLLaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList as LLPaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList as LLPaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed as TerminusLaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchBasicList as TerminusPaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchNormalList as TerminusPaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

// Terminus serves the LL2 2.4.0 launch detail shape. Re-reading the Terminus model as the LL
// model reuses the one tested LL -> domain mapping, so both backends produce the same domain
// Launch for the same launch. A field Terminus drops and LL requires fails loudly here
// (SerializationException) instead of mapping to a silent default.
internal val bridgeJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun TerminusLaunchDetailed.toDomain(): Launch =
    bridgeJson
        .decodeFromString<LLLaunchDetailed>(bridgeJson.encodeToString(this))
        .toDomain()

/** Terminus `GET /launches/mini/` page (LaunchBasic results) to the domain page. */
fun TerminusPaginatedLaunchBasicList.toDomain(): PaginatedResult<Launch> =
    bridgeJson
        .decodeFromString<LLPaginatedLaunchBasicList>(bridgeJson.encodeToString(this))
        .toDomain()

/** Terminus `GET /launches/` page (LaunchNormal results) to the domain page. */
fun TerminusPaginatedLaunchNormalList.toDomain(): PaginatedResult<Launch> =
    bridgeJson
        .decodeFromString<LLPaginatedLaunchNormalList>(bridgeJson.encodeToString(this))
        .toDomain()
