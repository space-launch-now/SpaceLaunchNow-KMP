package me.calebjones.spacelaunchnow.wear.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class SyncSource {
    PHONE_SYNC,
    LOCAL_CACHE,
    DEFAULT,
}

@Serializable
data class WearEntitlementState(
    val hasWearOs: Boolean = false,
    val lastSyncTimestamp: Instant = Instant.DISTANT_PAST,
    val source: SyncSource = SyncSource.DEFAULT,
    val expiresAt: Instant? = null,
)
