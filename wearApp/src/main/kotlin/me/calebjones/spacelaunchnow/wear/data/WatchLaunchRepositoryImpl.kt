package me.calebjones.spacelaunchnow.wear.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlin.time.Clock
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import me.calebjones.spacelaunchnow.wear.data.model.DataLayerSyncPayload
import me.calebjones.spacelaunchnow.wear.data.model.DataSource
import me.calebjones.spacelaunchnow.wear.data.model.WatchLaunchCache

class WatchLaunchRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
) : WatchLaunchRepository {

    private val log = Logger.withTag("WatchLaunchRepo")

    private val json = Json { ignoreUnknownKeys = true }

    private val launchApi = WatchLaunchApi(
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    )

    companion object {
        private const val PATH_SYNC = "/spacelaunchnow/sync"
        val LAUNCH_CACHE_KEY = stringPreferencesKey("launch_cache_json")
        val FILTER_PREFS_KEY = stringPreferencesKey("watch_filter_prefs_json")
    }

    override val launches: Flow<List<CachedLaunch>> = dataStore.data.map { prefs ->
        val cacheJson = prefs[LAUNCH_CACHE_KEY] ?: return@map emptyList()
        try {
            json.decodeFromString<WatchLaunchCache>(cacheJson).launches
        } catch (e: Exception) {
            log.w(e) { "Failed to parse launch cache" }
            emptyList()
        }
    }

    override val dataSource: Flow<DataSource> = dataStore.data.map { prefs ->
        val cacheJson = prefs[LAUNCH_CACHE_KEY] ?: return@map DataSource.STALE_CACHE
        try {
            json.decodeFromString<WatchLaunchCache>(cacheJson).dataSource
        } catch (e: Exception) {
            DataSource.STALE_CACHE
        }
    }

    override suspend fun refreshLaunches(limit: Int): Result<List<CachedLaunch>> {
        // Tier 1: Direct API call (watch WiFi/LTE)
        tryDirectApi(limit)?.let { return Result.success(it) }

        // Tier 2: DataLayer (phone sync data)
        tryDataLayer()?.let { return Result.success(it) }

        // Tier 3: Stale cache
        val cached = tryStaleCache()
        return if (cached != null) {
            Result.success(cached)
        } else {
            Result.failure(Exception("No launch data available from any source"))
        }
    }

    override suspend fun getLaunchById(launchId: String): CachedLaunch? {
        return getCurrentCache()?.launches?.find { it.id == launchId }
    }

    override suspend fun getNextLaunch(): CachedLaunch? {
        val now = Clock.System.now()
        return getCurrentCache()?.launches
            ?.filter { it.net > now }
            ?.minByOrNull { it.net }
    }

    private suspend fun tryDirectApi(limit: Int): List<CachedLaunch>? {
        return try {
            log.d { "Tier 1: Attempting direct API call" }
            val filterPrefs = loadFilterPreferences()
            val backendConfig = WatchBackendStore.load(dataStore)
            val launches = launchApi.fetchUpcoming(backendConfig, filterPrefs, limit)
            log.i { "Tier 1 SUCCESS: Fetched ${launches.size} launches via direct API (${backendConfig.backend}, filtered: ${!filterPrefs.followAllLaunches})" }
            updateCache(launches, DataSource.DIRECT_API)
            launches
        } catch (e: Exception) {
            log.w(e) { "Tier 1 FAILED: Direct API call failed" }
            null
        }
    }

    private suspend fun tryDataLayer(): List<CachedLaunch>? {
        return try {
            log.d { "Tier 2: Attempting DataLayer read" }
            withContext(Dispatchers.IO) {
                val dataClient = Wearable.getDataClient(context)
                val uri = Uri.Builder()
                    .scheme("wear")
                    .authority("*")
                    .path(PATH_SYNC)
                    .build()
                val dataItemBuffer = Tasks.await(dataClient.getDataItems(uri))
                try {
                    if (dataItemBuffer.count > 0) {
                        val dataItem = dataItemBuffer[0]
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        val payloadBytes = dataMap.getByteArray("payload")
                        if (payloadBytes != null) {
                            val payload = json.decodeFromString<DataLayerSyncPayload>(
                                payloadBytes.decodeToString()
                            )
                            // Persist filter preferences from phone for use in direct API calls
                            saveFilterPreferences(WatchFilterPreferences(
                                followAllLaunches = payload.followAllLaunches,
                                agencyIds = payload.agencyIds,
                                locationIds = payload.locationIds,
                            ))
                            val launches = payload.launches.mapNotNull { syncLaunch ->
                                try {
                                    CachedLaunch(
                                        id = syncLaunch.id,
                                        name = syncLaunch.name,
                                        net = Instant.parse(syncLaunch.net),
                                        statusAbbrev = syncLaunch.statusAbbrev,
                                        statusName = syncLaunch.statusName,
                                        lspName = syncLaunch.lspName,
                                        lspAbbrev = syncLaunch.lspAbbrev,
                                        rocketConfigName = syncLaunch.rocketConfigName,
                                        missionName = syncLaunch.missionName,
                                        missionDescription = syncLaunch.missionDescription,
                                        padLocationName = syncLaunch.padLocationName,
                                        imageUrl = syncLaunch.imageUrl,
                                    )
                                } catch (e: Exception) {
                                    log.w(e) { "Skipping invalid sync launch: ${syncLaunch.id}" }
                                    null
                                }
                            }
                            log.i { "Tier 2 SUCCESS: Read ${launches.size} launches from DataLayer (filtered: ${!payload.followAllLaunches})" }
                            updateCache(launches, DataSource.PHONE_SYNC)
                            launches
                        } else null
                    } else null
                } finally {
                    dataItemBuffer.release()
                }
            }
        } catch (e: Exception) {
            log.w(e) { "Tier 2 FAILED: DataLayer read failed" }
            null
        }
    }

    private suspend fun tryStaleCache(): List<CachedLaunch>? {
        return try {
            log.d { "Tier 3: Attempting stale cache read" }
            val cache = getCurrentCache()
            if (cache != null && cache.launches.isNotEmpty()) {
                log.i { "Tier 3 SUCCESS: Returning ${cache.launches.size} cached launches (source: ${cache.dataSource})" }
                updateCache(cache.launches, DataSource.STALE_CACHE)
                cache.launches
            } else {
                log.w { "Tier 3 FAILED: No cached data available" }
                null
            }
        } catch (e: Exception) {
            log.w(e) { "Tier 3 FAILED: Cache read failed" }
            null
        }
    }

    private suspend fun getCurrentCache(): WatchLaunchCache? {
        return try {
            val prefs = dataStore.data.first()
            val cacheJson = prefs[LAUNCH_CACHE_KEY] ?: return null
            json.decodeFromString<WatchLaunchCache>(cacheJson)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun updateCache(launches: List<CachedLaunch>, source: DataSource) {
        val cache = WatchLaunchCache(
            launches = launches,
            lastUpdated = Clock.System.now(),
            dataSource = source,
        )
        dataStore.edit { prefs ->
            prefs[LAUNCH_CACHE_KEY] = json.encodeToString(WatchLaunchCache.serializer(), cache)
        }
    }

    private suspend fun loadFilterPreferences(): WatchFilterPreferences {
        return try {
            val prefs = dataStore.data.first()
            val json = prefs[FILTER_PREFS_KEY] ?: return WatchFilterPreferences()
            this.json.decodeFromString<WatchFilterPreferences>(json)
        } catch (e: Exception) {
            WatchFilterPreferences()
        }
    }

    private suspend fun saveFilterPreferences(filterPrefs: WatchFilterPreferences) {
        try {
            dataStore.edit { prefs ->
                prefs[FILTER_PREFS_KEY] = json.encodeToString(WatchFilterPreferences.serializer(), filterPrefs)
            }
        } catch (e: Exception) {
            log.w(e) { "Failed to save filter preferences" }
        }
    }
}
