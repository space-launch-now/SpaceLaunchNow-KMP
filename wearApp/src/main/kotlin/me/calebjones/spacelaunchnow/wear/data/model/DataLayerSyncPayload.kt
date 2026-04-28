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
    /** When true, no agency/location filtering is applied (show all launches). */
    val followAllLaunches: Boolean = true,
    /** Agency IDs to filter by (LSP), or null/empty for no filter. */
    val agencyIds: List<Int>? = null,
    /** Location IDs to filter by, or null/empty for no filter. */
    val locationIds: List<Int>? = null,
)
