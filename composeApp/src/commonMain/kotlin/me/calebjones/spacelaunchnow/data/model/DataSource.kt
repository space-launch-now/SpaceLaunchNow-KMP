package me.calebjones.spacelaunchnow.data.model

/**
 * Indicates where data originated from
 */
enum class DataSource {
    /** Fresh data from network API */
    NETWORK,
    
    /** Fresh data from cache (within TTL) */
    CACHE,
    
    /** Stale cached data used as fallback when network fails */
    STALE_CACHE
}

/**
 * Wrapper for repository results that includes data source metadata
 * This allows ViewModels and UI to distinguish between fresh and stale data
 * 
 * @param data The actual data payload
 * @param source Where the data came from (network, cache, or stale cache)
 * @param timestamp When the data was originally fetched/cached (epoch milliseconds)
 */
data class DataResult<T>(
    val data: T,
    val source: DataSource,
    val timestamp: Long? = null
)
