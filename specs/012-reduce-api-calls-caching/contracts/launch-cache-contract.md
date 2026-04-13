# Contract: Launch Cache Extensions

**Feature**: `012-reduce-api-calls-caching`

## StatsLocalDataSource

New class following existing `LocalDataSource` conventions.

```kotlin
class StatsLocalDataSource(
    private val queries: StatsCacheQueries,
    private val preferences: AppPreferences
) {
    private val log = logger()
    
    private val cacheDuration = 10.minutes
    private val debugCacheDuration = 1.minutes
    
    private fun getEffectiveTtl(): Duration =
        if (preferences.isDebugShortCacheTtlEnabled()) debugCacheDuration else cacheDuration
    
    /**
     * Get cached stat count. Returns null if cache miss or expired.
     */
    suspend fun getStatCount(key: String): Int? {
        val now = Clock.System.now().toEpochMilliseconds()
        return queries.getStatCount(key, now).executeAsOneOrNull()?.toInt()
    }
    
    /**
     * Get stale stat count (ignores TTL). Used for stale-while-revalidate.
     * Returns Pair(count, cachedAt) or null if never cached.
     */
    suspend fun getStatCountStale(key: String): Pair<Int, Long>? {
        val row = queries.getStatCountStale(key).executeAsOneOrNull() ?: return null
        return Pair(row.count.toInt(), row.cached_at)
    }
    
    /**
     * Cache a stat count value.
     */
    suspend fun cacheStat(key: String, count: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        val expiresAt = now + getEffectiveTtl().inWholeMilliseconds
        queries.upsertStat(key, count.toLong(), now, expiresAt)
    }
    
    /**
     * Delete expired stats entries. Called by CacheCleanupService.
     */
    suspend fun deleteExpiredStats() {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.deleteExpiredStats(now)
    }
}
```

## LaunchRepository — New Method

```kotlin
interface LaunchRepository {
    // ... existing methods ...
    
    /**
     * Get cached stats count for a time range. Uses StatsCache for lightweight caching.
     * Returns DataResult with count and DataSource indicator.
     */
    suspend fun getStatsCount(
        key: String,
        netGt: Instant,
        netLt: Instant,
        forceRefresh: Boolean = false
    ): Result<DataResult<Int>>
}
```

## LaunchRepositoryImpl — getStatsCount Implementation

```kotlin
override suspend fun getStatsCount(
    key: String,
    netGt: Instant,
    netLt: Instant,
    forceRefresh: Boolean
): Result<DataResult<Int>> {
    return try {
        val now = Clock.System.now().toEpochMilliseconds()
        
        // 1. Check stale data for fallback
        val staleResult = statsLocalDataSource?.getStatCountStale(key)
        
        // 2. Try fresh cache
        if (!forceRefresh) {
            val cached = statsLocalDataSource?.getStatCount(key)
            if (cached != null) {
                log.d { "Stats cache hit for $key: $cached" }
                return Result.success(DataResult(data = cached, source = DataSource.CACHE, timestamp = now))
            }
            // Try stale
            if (staleResult != null) {
                log.d { "Stats stale cache hit for $key: ${staleResult.first}" }
                return Result.success(DataResult(data = staleResult.first, source = DataSource.STALE_CACHE, timestamp = staleResult.second))
            }
        }
        
        // 3. Fetch from API
        val response = launchesApi.getLaunchMiniList(
            limit = 1,
            upcoming = true,
            netGt = netGt.toString(),
            netLt = netLt.toString()
        )
        val count = response.body().count ?: 0
        
        // 4. Cache the count
        statsLocalDataSource?.cacheStat(key, count)
        
        Result.success(DataResult(data = count, source = DataSource.NETWORK, timestamp = now))
    } catch (e: Exception) {
        // 5. Fallback to stale
        val staleResult = statsLocalDataSource?.getStatCountStale(key)
        if (staleResult != null) {
            Result.success(DataResult(data = staleResult.first, source = DataSource.STALE_CACHE, timestamp = staleResult.second))
        } else {
            Result.failure(e)
        }
    }
}
```

## LaunchRepositoryImpl — getInFlightLaunches Modified

```kotlin
override suspend fun getInFlightLaunches(
    forceRefresh: Boolean,
    agencyIds: List<Int>?,
    locationIds: List<Int>?
): Result<DataResult<PaginatedLaunchNormalList>> {
    return try {
        val now = Clock.System.now().toEpochMilliseconds()
        val cacheKey = buildCacheKey("in_flight", agencyIds, locationIds)
        
        // 1. Check stale for fallback
        val staleCached = localDataSource?.getInFlightNormalLaunchesStale(5)
        val hasStaleData = staleCached != null && staleCached.isNotEmpty()
        
        // 2. Fresh cache
        if (!forceRefresh) {
            val cached = localDataSource?.getInFlightNormalLaunches(5)
            if (cached != null && cached.isNotEmpty()) {
                return Result.success(DataResult(
                    data = PaginatedLaunchNormalList(count = cached.size, results = cached, next = null, previous = null),
                    source = DataSource.CACHE, timestamp = now
                ))
            }
            if (hasStaleData) {
                return Result.success(DataResult(
                    data = PaginatedLaunchNormalList(count = staleCached!!.size, results = staleCached, next = null, previous = null),
                    source = DataSource.STALE_CACHE, timestamp = now
                ))
            }
        }
        
        // 3. API call (existing logic)
        val response = launchesApi.getLaunchList(statusIds = listOf(6), limit = 5, ordering = "net")
        val launches = response.body()
        
        // 4. Cache results
        localDataSource?.cacheNormalLaunches(launches.results)
        
        Result.success(DataResult(data = launches, source = DataSource.NETWORK, timestamp = now))
    } catch (e: Exception) {
        val staleCached = localDataSource?.getInFlightNormalLaunchesStale(5)
        if (staleCached != null && staleCached.isNotEmpty()) {
            Result.success(DataResult(
                data = PaginatedLaunchNormalList(count = staleCached.size, results = staleCached, next = null, previous = null),
                source = DataSource.STALE_CACHE, timestamp = Clock.System.now().toEpochMilliseconds()
            ))
        } else {
            Result.failure(e)
        }
    }
}
```
