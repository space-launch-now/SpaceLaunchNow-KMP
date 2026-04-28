package me.calebjones.spacelaunchnow.wear.data

import android.content.ComponentName
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import co.touchlab.kermit.Logger
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import me.calebjones.spacelaunchnow.wear.complication.NextLaunchComplicationService
import me.calebjones.spacelaunchnow.wear.tile.NextLaunchTileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import me.calebjones.spacelaunchnow.wear.data.model.DataLayerSyncPayload
import me.calebjones.spacelaunchnow.wear.data.model.DataSource
import me.calebjones.spacelaunchnow.wear.data.model.WatchLaunchCache
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class DataLayerListenerService : WearableListenerService() {

    private val entitlementSyncManager: EntitlementSyncManager by inject()
    private val dataStore: DataStore<Preferences> by inject(named("WearLaunchCacheDataStore"))
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path ?: return@forEach
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                when (path) {
                    PATH_SYNC -> handleLaunchSync(dataMap)
                    PATH_ENTITLEMENT -> handleEntitlementSync(dataMap)
                }
            }
        }
    }

    private fun handleLaunchSync(dataMap: DataMap) {
        val payloadBytes = dataMap.getByteArray("payload")
        if (payloadBytes == null) {
            Logger.w { "Launch sync data missing payload" }
            return
        }

        try {
            val payload = json.decodeFromString<DataLayerSyncPayload>(payloadBytes.decodeToString())
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
                    Logger.w(e) { "Skipping invalid sync launch: ${syncLaunch.id}" }
                    null
                }
            }

            val cache = WatchLaunchCache(
                launches = launches,
                lastUpdated = Clock.System.now(),
                dataSource = DataSource.PHONE_SYNC,
            )

            serviceScope.launch {
                try {
                    dataStore.edit { prefs ->
                        prefs[WatchLaunchRepositoryImpl.LAUNCH_CACHE_KEY] =
                            json.encodeToString(WatchLaunchCache.serializer(), cache)
                    }
                    Logger.i { "DataLayer sync: Cached ${launches.size} launches from phone" }
                    requestComplicationUpdate()
                    requestTileUpdate()
                } catch (e: Exception) {
                    Logger.e(e) { "Failed to persist launch sync data" }
                }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to parse launch sync payload" }
        }
    }

    private fun handleEntitlementSync(dataMap: DataMap) {
        val active = dataMap.getBoolean(KEY_ACTIVE, false)
        val expiresAtStr = dataMap.getString(KEY_EXPIRES_AT, "")
        val expiresAt = if (expiresAtStr.isNullOrEmpty()) null else {
            try {
                Instant.parse(expiresAtStr)
            } catch (e: Exception) {
                Logger.w(e) { "Failed to parse expiresAt: $expiresAtStr" }
                null
            }
        }

        Logger.i { "Received entitlement sync: active=$active, expiresAt=$expiresAt" }

        serviceScope.launch {
            try {
                entitlementSyncManager.onEntitlementReceived(active, expiresAt)
            } catch (e: Exception) {
                Logger.e(e) { "Failed to process entitlement sync" }
            }
        }
    }

    private fun requestTileUpdate() {
        try {
            TileService.getUpdater(this)
                .requestUpdate(NextLaunchTileService::class.java)
            Logger.d { "Tile update requested from DataLayerListener" }
        } catch (e: Exception) {
            Logger.w(e) { "Failed to request tile update" }
        }
    }

    private fun requestComplicationUpdate() {
        try {
            val requester = ComplicationDataSourceUpdateRequester.create(
                this,
                ComponentName(this, NextLaunchComplicationService::class.java)
            )
            requester.requestUpdateAll()
            Logger.d { "Complication update requested from DataLayerListener" }
        } catch (e: Exception) {
            Logger.w(e) { "Failed to request complication update" }
        }
    }

    companion object {
        private const val PATH_SYNC = "/spacelaunchnow/sync"
        private const val PATH_ENTITLEMENT = "/spacelaunchnow/entitlement"
        private const val KEY_ACTIVE = "active"
        private const val KEY_EXPIRES_AT = "expiresAt"
    }
}
