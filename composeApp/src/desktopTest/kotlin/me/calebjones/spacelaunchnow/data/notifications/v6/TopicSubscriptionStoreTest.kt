package me.calebjones.spacelaunchnow.data.notifications.v6

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopicSubscriptionStoreTest {

    private fun newStore(): TopicSubscriptionStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return TopicSubscriptionStore(SpaceLaunchDatabase(driver))
    }

    @Test
    fun replaceDesiredCreatesPendingSubscribeRows() {
        val store = newStore()
        store.replaceDesired(setOf("a", "b"))
        assertEquals(setOf("a", "b"), store.pendingSubscribes().toSet())
        assertEquals(emptyList(), store.pendingUnsubscribes())
    }

    @Test
    fun droppedTopicBecomesPendingUnsubscribeAndSettlesAfterConfirm() {
        val store = newStore()
        store.replaceDesired(setOf("a", "b"))
        store.confirm("a", confirmed = true, nowMillis = 1)
        store.confirm("b", confirmed = true, nowMillis = 1)

        store.replaceDesired(setOf("a"))
        assertEquals(listOf("b"), store.pendingUnsubscribes())

        store.confirm("b", confirmed = false, nowMillis = 2)
        store.deleteSettled()
        assertEquals(emptyList(), store.pendingUnsubscribes())
        assertEquals(listOf("a"), store.confirmedTopics())
    }

    @Test
    fun emptyRequiredSetMarksEverythingUndesired() {
        val store = newStore()
        store.replaceDesired(setOf("a"))
        store.confirm("a", confirmed = true, nowMillis = 1)
        store.replaceDesired(emptySet())
        assertEquals(listOf("a"), store.pendingUnsubscribes())
    }

    @Test
    fun failureRecordsErrorAndCountsAttempts_confirmClearsBoth() {
        val store = newStore()
        store.replaceDesired(setOf("a"))
        store.recordFailure("a", "boom", nowMillis = 5)
        store.recordFailure("a", "boom again", nowMillis = 6)

        val row = store.mismatchedRows().single()
        assertEquals("a", row.topic)
        assertEquals(2L, row.attempts)
        assertEquals("boom again", row.last_error)
        assertEquals(6L, row.last_attempt)

        store.confirm("a", confirmed = true, nowMillis = 7)
        assertEquals(emptyList(), store.mismatchedRows())
        assertEquals(listOf("a"), store.confirmedTopics())
    }

    @Test
    fun countsReflectTableState() {
        val store = newStore()
        store.replaceDesired(setOf("a", "b", "c"))
        store.confirm("a", confirmed = true, nowMillis = 1)
        store.replaceDesired(setOf("a", "b"))   // c -> undesired but unconfirmed
        store.confirm("c", confirmed = true, nowMillis = 1)  // simulate it was confirmed earlier
        val counts = store.counts()
        assertEquals(2L, counts.confirmed)          // a, c
        assertEquals(1L, counts.pendingSubscribe)   // b
        assertEquals(1L, counts.pendingUnsubscribe) // c
        assertTrue(store.mismatchedRows().isNotEmpty())
    }
}
