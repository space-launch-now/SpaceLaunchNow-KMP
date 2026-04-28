package me.calebjones.spacelaunchnow.sync

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository

/**
 * Observes phone-side subscription state and pushes entitlement changes to the watch.
 *
 * This ensures the watch receives updates from ALL entitlement sources:
 * - RevenueCat real-time callbacks (via AndroidBillingManager)
 * - Debug menu simulated subscriptions (via SimpleSubscriptionRepository)
 * - Subscription syncer background refreshes
 */
class WearEntitlementPusher(
    private val subscriptionRepository: SubscriptionRepository,
    private val phoneDataLayerSync: PhoneDataLayerSync,
    private val scope: CoroutineScope,
) {
    private val log = Logger.withTag("WearEntitlementPusher")

    fun start() {
        scope.launch {
            subscriptionRepository.state
                .map { state ->
                    val hasWearOs = state.hasFeature(PremiumFeature.WEAR_OS)
                    val expiresAt = state.expiresAt
                    hasWearOs to expiresAt
                }
                .distinctUntilChanged()
                .collect { (hasWearOs, expiresAtMs) ->
                    try {
                        val expiresAt = expiresAtMs?.let { Instant.fromEpochMilliseconds(it) }
                        phoneDataLayerSync.syncEntitlementToWatch(
                            active = hasWearOs,
                            expiresAt = expiresAt,
                        )
                        log.i { "Pushed entitlement to watch: hasWearOs=$hasWearOs" }
                    } catch (e: Exception) {
                        log.w(e) { "Failed to push entitlement to watch (watch may not be connected)" }
                    }
                }
        }
    }
}
