package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed as LLLaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed as TerminusLaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.RocketDetailed as TerminusRocketDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.TimelineEvent as TerminusTimelineEvent

// Terminus serves the LL2 2.4.0 shapes. Re-reading a Terminus model as the LL model reuses the
// one tested LL -> domain mapping, so both backends produce the same domain object for the same
// data. A field Terminus drops and LL requires fails loudly (SerializationException) instead of
// mapping to a silent default. Every mapper in this package goes through [bridge].
internal val bridgeJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** Re-reads this Terminus model as the LL model [R] through [bridgeJson]. */
internal inline fun <reified T, reified R> T.bridge(): R =
    bridgeJson.decodeFromString<R>(bridgeJson.encodeToString(this))

/**
 * Builds the LL launch detail from the three Terminus reads. The launch's own `rocket` and
 * `timeline` keys are replaced: the full [rocket] and the [timeline] win over whatever the launch
 * call carries, so the result is the same before and after Terminus slims `GET /launches/{id}/`.
 */
internal fun bridgeLaunchDetailed(
    launch: TerminusLaunchDetailed,
    rocket: TerminusRocketDetailed?,
    timeline: List<TerminusTimelineEvent>
): LLLaunchDetailed {
    val merged = JsonObject(
        bridgeJson.encodeToJsonElement(launch).jsonObject + mapOf(
            "rocket" to (rocket?.let { bridgeJson.encodeToJsonElement(it) } ?: JsonNull),
            "timeline" to bridgeJson.encodeToJsonElement(timeline)
        )
    )
    return bridgeJson.decodeFromJsonElement<LLLaunchDetailed>(merged)
}
