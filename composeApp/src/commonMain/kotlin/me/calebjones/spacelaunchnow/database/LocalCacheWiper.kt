package me.calebjones.spacelaunchnow.database

import coil3.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.calebjones.spacelaunchnow.cache.LaunchCache

/**
 * Empties every local cache so the next fetch comes from whichever backend the DataBackend
 * flag names — the debug-menu lever for swapping between LL and Trantor mid-migration.
 *
 * Scope is caches only. Preference stores (including the Trantor URL and backend override
 * in DebugPreferences) and the TopicSubscription ledger (real FCM server-side subscription
 * state, not a cache) are left untouched.
 */
interface CacheWiper {
    suspend fun clearAll(imageLoader: ImageLoader? = null)
}

class LocalCacheWiper(
    private val database: SpaceLaunchDatabase,
    private val launchCache: LaunchCache
) : CacheWiper {

    override suspend fun clearAll(imageLoader: ImageLoader?) {
        withContext(Dispatchers.Default) {
            database.transaction {
                with(database.launchQueries) {
                    clearAllNormal()
                    clearAllDetailed()
                    clearAllStarshipHistory()
                }
                database.eventQueries.clearAllEvents()
                database.articleQueries.clearAllArticles()
                database.updateQueries.clearAllUpdates()
                database.programQueries.clearAllPrograms()
                database.spacecraftQueries.clearAllSpacecraft()
                with(database.spaceStationQueries) {
                    clearAllSpaceStations()
                    clearAllExpeditions()
                    clearAllTle()
                }
                database.statsQueries.deleteAllStats()
                with(database.filterOptionsQueries) {
                    clearAllAgencies()
                    clearAllPrograms()
                    clearAllRockets()
                    clearAllLocations()
                    clearAllStatuses()
                    clearAllOrbits()
                    clearAllMissionTypes()
                    clearAllLauncherConfigFamilies()
                }
            }
        }
        launchCache.clearCache()
        imageLoader?.let { loader ->
            loader.memoryCache?.clear()
            withContext(Dispatchers.Default) { loader.diskCache?.clear() }
        }
    }
}
