package me.calebjones.spacelaunchnow.wear.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CachedLaunch(
    val id: String,
    val name: String,
    val net: Instant,
    val statusAbbrev: String? = null,
    val statusName: String? = null,
    val lspName: String? = null,
    val lspAbbrev: String? = null,
    val rocketConfigName: String? = null,
    val missionName: String? = null,
    val missionDescription: String? = null,
    val padLocationName: String? = null,
    val imageUrl: String? = null,
)
