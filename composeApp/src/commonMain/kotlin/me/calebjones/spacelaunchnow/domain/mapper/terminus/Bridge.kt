package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
