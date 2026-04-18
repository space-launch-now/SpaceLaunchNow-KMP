package me.calebjones.spacelaunchnow.wear.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class DataSource {
    DIRECT_API,
    PHONE_SYNC,
    STALE_CACHE,
}

@Serializable
data class WatchLaunchCache(
    val launches: List<CachedLaunch> = emptyList(),
    val lastUpdated: Instant = Instant.DISTANT_PAST,
    val dataSource: DataSource = DataSource.STALE_CACHE,
)
