package me.calebjones.spacelaunchnow.sync

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage

/**
 * Observes phone-side launch filter preferences and pushes a full launch sync to the watch
 * whenever the user changes agency or location filters.
 *
 * Uses debounce to avoid spamming the DataLayer when many toggles happen in quick succession
 * (e.g. selecting several agencies one by one).
 */
class WearFilterSyncPusher(
    private val notificationStateStorage: NotificationStateStorage,
    private val phoneDataLayerSync: PhoneDataLayerSync,
    private val scope: CoroutineScope,
) {
    private val log = Logger.withTag("WearFilterSyncPusher")

    @OptIn(FlowPreview::class)
    fun start() {
        scope.launch {
            notificationStateStorage.stateFlow
                .map { state ->
                    // Only react to changes in the fields that affect launch filtering
                    Triple(state.followAllLaunches, state.subscribedAgencies, state.subscribedLocations)
                }
                .distinctUntilChanged()
                .debounce(1_500) // Wait 1.5 s after last change before syncing
                .collect { (followAll, agencies, locations) ->
                    try {
                        log.i { "Filter prefs changed — pushing updated launches to watch (followAll=$followAll, agencies=${agencies.size}, locations=${locations.size})" }
                        phoneDataLayerSync.syncToWatch()
                    } catch (e: Exception) {
                        log.w(e) { "Failed to push filter update to watch (watch may not be connected)" }
                    }
                }
        }
    }
}

