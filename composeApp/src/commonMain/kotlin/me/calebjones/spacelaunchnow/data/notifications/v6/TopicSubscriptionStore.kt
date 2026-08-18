package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.database.TopicSubscription

data class SubscriptionCounts(
    val confirmed: Long,
    val pendingSubscribe: Long,
    val pendingUnsubscribe: Long,
)

/**
 * Thin wrapper over the generated TopicSubscription queries. Booleans are
 * stored as 0/1 INTEGER (repo convention -- no column adapters anywhere in the
 * schema); this class is the only place that converts.
 */
class TopicSubscriptionStore(private val database: SpaceLaunchDatabase) {

    private val queries get() = database.topicSubscriptionQueries

    /**
     * Atomically rewrite the desired-set. Rows outside [required] flip to
     * desired=0; rows in it are created (desired=1, confirmed=0) or re-marked
     * desired. `confirmed` is untouched -- only FCM callbacks may change it.
     */
    fun replaceDesired(required: Set<String>) {
        database.transaction {
            if (required.isEmpty()) {
                queries.clearAllDesired()
            } else {
                queries.clearDesiredExcept(required)
            }
            required.forEach { topic ->
                queries.insertDesired(topic)
                queries.markDesired(topic)
            }
        }
    }

    fun pendingSubscribes(): List<String> = queries.pendingSubscribes().executeAsList()

    fun pendingUnsubscribes(): List<String> = queries.pendingUnsubscribes().executeAsList()

    fun confirm(topic: String, confirmed: Boolean, nowMillis: Long) {
        queries.confirm(if (confirmed) 1L else 0L, nowMillis, topic)
    }

    fun recordFailure(topic: String, error: String?, nowMillis: Long) {
        queries.recordFailure(error ?: "unknown", nowMillis, topic)
    }

    fun deleteSettled() = queries.deleteSettled()

    fun confirmedTopics(): List<String> = queries.confirmedTopics().executeAsList()

    fun deleteRow(topic: String) = queries.deleteRow(topic)

    fun mismatchedRows(): List<TopicSubscription> = queries.mismatchedRows().executeAsList()

    fun counts(): SubscriptionCounts = SubscriptionCounts(
        confirmed = queries.countConfirmed().executeAsOne(),
        pendingSubscribe = queries.countPendingSubscribes().executeAsOne(),
        pendingUnsubscribe = queries.countPendingUnsubscribes().executeAsOne(),
    )
}
