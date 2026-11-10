package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed as LLLaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed as TerminusLaunchDetailed
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.Launch

// Terminus serves the LL2 2.4.0 launch detail shape. Re-reading the Terminus model as the LL
// model reuses the one tested LL -> domain mapping, so both backends produce the same domain
// Launch for the same launch. A field Terminus drops and LL requires fails loudly here
// (SerializationException) instead of mapping to a silent default.
private val bridgeJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun TerminusLaunchDetailed.toDomain(): Launch =
    bridgeJson
        .decodeFromString<LLLaunchDetailed>(bridgeJson.encodeToString(this))
        .toDomain()
