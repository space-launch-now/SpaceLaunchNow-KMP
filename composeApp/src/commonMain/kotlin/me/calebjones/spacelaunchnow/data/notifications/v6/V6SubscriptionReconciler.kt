package me.calebjones.spacelaunchnow.data.notifications.v6

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.util.logging.logger

data class V6ReconcileResult(
    val attempted: Int,
    val failed: Int,
    val skipped: Boolean = false,
) {
    val clean: Boolean get() = !skipped && failed == 0
}

/**
 * The dumb loop over the TopicSubscription table. All thinking lives in
 * V6Topics.requiredTopics; this class only writes `desired`, walks the pending
 * queries, and records FCM outcomes.
 *
 * One invariant: `confirmed` is written only in an FCM success callback. A
 * failure leaves the row disagreeing, so the next reconciliation retries it --
 * no retry queue, no backoff bookkeeping.
 *
 * Unsubscribes run first, always. The audience class is baked into every type
 * topic, so a class switch rewrites that whole dimension; if subscribes ran
 * first and the unsubscribes then failed, the device would hold two classes
 * and receive every launch twice until the next reconcile. Failing toward a
 * brief gap instead of lasting duplicates is the correct direction.
 */
class V6SubscriptionReconciler(
    private val store: TopicSubscriptionStore,
    private val messaging: TopicMessaging,
    private val platform: String?,                    // "android" | "ios" | null: no-op (desktop)
    private val envProvider: suspend () -> String,    // "prod" | "debug"
    private val stateProvider: suspend () -> NotificationState,
    private val markChangeoverComplete: suspend () -> Unit,
    private val nowMillis: () -> Long,
) {
    private val log = logger()
    private val mutex = Mutex()

    suspend fun reconcile(): V6ReconcileResult {
        val platform = platform ?: return V6ReconcileResult(0, 0, skipped = true)
        mutex.withLock {
            val state = stateProvider()
            if (!state.hasCompletedV6Changeover) runChangeover(platform)
            return reconcileLocked(state, platform)
        }
    }

    private suspend fun reconcileLocked(state: NotificationState, platform: String): V6ReconcileResult {
        val required = V6Topics.requiredTopics(state, envProvider(), platform)
        store.replaceDesired(required)

        var attempted = 0
        var failed = 0

        store.pendingUnsubscribes().forEach { topic ->
            attempted++
            messaging.unsubscribe(topic)
                .onSuccess { store.confirm(topic, confirmed = false, nowMillis = nowMillis()) }
                .onFailure { failed++; store.recordFailure(topic, it.message, nowMillis()) }
        }
        store.deleteSettled()

        store.pendingSubscribes().forEach { topic ->
            attempted++
            messaging.subscribe(topic)
                .onSuccess { store.confirm(topic, confirmed = true, nowMillis = nowMillis()) }
                .onFailure { failed++; store.recordFailure(topic, it.message, nowMillis()) }
        }

        if (failed > 0) log.w { "V6 reconcile: $failed of $attempted FCM operations failed; will retry next pass" }
        return V6ReconcileResult(attempted, failed)
    }

    /**
     * One-time V5/V4 changeover: unsubscribe the legacy topics so this device
     * stops receiving the V5 broadcast the server still dual-sends. Marked
     * complete only when every legacy unsubscribe succeeded -- a partial
     * changeover retries on the next reconcile (transient double delivery is
     * collapsed by apns-collapse-id / collapse_key).
     */
    private suspend fun runChangeover(platform: String) {
        val legacy = listOf("prod_v5_$platform", "debug_v5_$platform", "k_prod_v4", "k_debug_v4")
        // map-then-all: every topic must be attempted; .all{} would short-circuit.
        val outcomes = legacy.map { topic -> messaging.unsubscribe(topic).isSuccess }
        if (outcomes.all { it }) {
            markChangeoverComplete()
            log.i { "V6 changeover complete: legacy topics unsubscribed ($legacy)" }
        } else {
            log.w { "V6 changeover incomplete; will retry next reconcile" }
        }
    }

    /**
     * The only correct reset (spec: "resubscribe from scratch"): explicitly
     * unsubscribe every topic we hold a confirmed row for, then rebuild from
     * settings. Never clear the table without unsubscribing -- we can only
     * unsubscribe from topics we can name, so a wiped record makes stale
     * subscriptions permanently invisible.
     */
    suspend fun forceResubscribe(): V6ReconcileResult {
        val platform = platform ?: return V6ReconcileResult(0, 0, skipped = true)
        mutex.withLock {
            store.confirmedTopics().forEach { topic ->
                messaging.unsubscribe(topic)
                    .onSuccess { store.deleteRow(topic) }
                    .onFailure { store.recordFailure(topic, it.message, nowMillis()) }
            }
        }
        return reconcile()
    }
}
