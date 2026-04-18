package me.calebjones.spacelaunchnow.wear.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncLaunch(
    val id: String,
    val name: String,
    val net: String,
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

@Serializable
data class DataLayerSyncPayload(
    val launches: List<SyncLaunch>,
    val entitlementActive: Boolean,
    val syncTimestamp: String,
    val phoneAppVersion: String,
)
