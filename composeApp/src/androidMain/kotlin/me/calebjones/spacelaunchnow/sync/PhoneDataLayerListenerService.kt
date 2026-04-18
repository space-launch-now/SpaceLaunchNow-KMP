package me.calebjones.spacelaunchnow.sync

import co.touchlab.kermit.Logger
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import org.koin.core.context.GlobalContext

class PhoneDataLayerListenerService : WearableListenerService() {

    private val log = Logger.withTag("PhoneDataLayerListener")

    override fun onMessageReceived(messageEvent: MessageEvent) {
        log.d { "Received message: ${messageEvent.path}" }
        if (messageEvent.path == PhoneDataLayerService.PATH_REQUEST_SYNC) {
            runBlocking {
                try {
                    val koin = GlobalContext.get()
                    val dataLayerSync: PhoneDataLayerSync = koin.get()

                    // Sync launch data
                    dataLayerSync.syncToWatch()

                    // Sync entitlement state from SubscriptionRepository (respects debug overrides)
                    val subscriptionRepo: SubscriptionRepository = koin.get()
                    val subscriptionState = subscriptionRepo.state.value
                    val hasWearOs = subscriptionState.hasFeature(PremiumFeature.WEAR_OS)
                    val expiresAt = subscriptionState.expiresAt?.let {
                        Instant.fromEpochMilliseconds(it)
                    }
                    dataLayerSync.syncEntitlementToWatch(
                        active = hasWearOs,
                        expiresAt = expiresAt,
                    )
                    log.i { "Synced launches + entitlement (hasWearOs=$hasWearOs) to watch" }
                } catch (e: Exception) {
                    log.e(e) { "Failed to handle sync request from watch" }
                }
            }
        }
    }
}
