package me.calebjones.spacelaunchnow.sync

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.DataBackend

/**
 * Sends the phone's resolved [DataBackend] (and the Terminus host) to the watch, which has no
 * Firebase Remote Config of its own. The value is fixed for the life of the Koin graph (no hot
 * swap), so one push at start-up covers it; the watch's sync request pushes it again so a freshly
 * installed watch learns it. Until a value arrives the watch reads LL.
 */
class WearBackendPusher(
    private val dataBackend: DataBackend,
    private val terminusBaseUrl: String,
    private val phoneDataLayerSync: PhoneDataLayerSync,
    private val scope: CoroutineScope,
) {
    private val log = Logger.withTag("WearBackendPusher")

    fun start() {
        scope.launch { pushNow() }
    }

    suspend fun pushNow() {
        try {
            phoneDataLayerSync.syncBackendToWatch(dataBackend, terminusBaseUrl)
        } catch (e: Exception) {
            log.w(e) { "Failed to push data backend to watch (watch may not be connected)" }
        }
    }
}
