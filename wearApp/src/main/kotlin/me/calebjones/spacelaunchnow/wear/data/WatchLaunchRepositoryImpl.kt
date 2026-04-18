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
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import me.calebjones.spacelaunchnow.wear.data.model.DataLayerSyncPayload
import me.calebjones.spacelaunchnow.wear.data.model.DataSource
import me.calebjones.spacelaunchnow.wear.data.model.WatchLaunchCache

/**
 * Minimal API response models for direct Launch Library API calls from the watch.
 * These are intentionally lightweight — only fields needed for CachedLaunch mapping.
 */
@Serializable
private data class ApiLaunchListResponse(
    val count: Int,
    val results: List<ApiLaunchBasic>,
    val next: String? = null,
    val previous: String? = null,
)

@Serializable
private data class ApiLaunchBasic(
    val id: String,
    val name: String? = null,
    val net: String? = null,
    val status: ApiLaunchStatus? = null,
    @SerialName("launch_service_provider") val launchServiceProvider: ApiAgencyMini? = null,
    @SerialName("launch_designator") val launchDesignator: String? = null,
    @SerialName("location_name") val locationName: String? = null,
    val image: ApiImage? = null,
)

@Serializable
private data class ApiLaunchStatus(
    val id: Int? = null,
    val name: String? = null,
    val abbrev: String? = null,
)

@Serializable
private data class ApiAgencyMini(
    val id: Int? = null,
    val name: String? = null,
    val abbrev: String? = null,
)

@Serializable
private data class ApiImage(
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)

class WatchLaunchRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
) : WatchLaunchRepository {

    private val log = Logger.withTag("WatchLaunchRepo")

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    companion object {
        private const val LL_API_BASE_URL = "https://ll.thespacedevs.com/2.4.0/launches/upcoming/"
        private const val PATH_SYNC = "/spacelaunchnow/sync"
        val LAUNCH_CACHE_KEY = stringPreferencesKey("launch_cache_json")
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
            val response = httpClient.get(LL_API_BASE_URL) {
                parameter("limit", limit)
                parameter("mode", "list")
                parameter("format", "json")
            }
            val apiResponse = response.body<ApiLaunchListResponse>()
            val launches = apiResponse.results.mapNotNull { it.toCachedLaunch() }
            log.i { "Tier 1 SUCCESS: Fetched ${launches.size} launches via direct API" }
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
                            log.i { "Tier 2 SUCCESS: Read ${launches.size} launches from DataLayer" }
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
}

private fun ApiLaunchBasic.toCachedLaunch(): CachedLaunch? {
    val parsedNet = net?.let {
        try {
            Instant.parse(it)
        } catch (e: Exception) {
            null
        }
    } ?: return null

    return CachedLaunch(
        id = id,
        name = name ?: "Unknown",
        net = parsedNet,
        statusAbbrev = status?.abbrev,
        statusName = status?.name,
        lspName = launchServiceProvider?.name,
        lspAbbrev = launchServiceProvider?.abbrev,
        rocketConfigName = launchDesignator,
        missionName = null, // Not available in basic/list mode
        missionDescription = null, // Not available in basic/list mode
        padLocationName = locationName,
        imageUrl = image?.imageUrl,
    )
}
