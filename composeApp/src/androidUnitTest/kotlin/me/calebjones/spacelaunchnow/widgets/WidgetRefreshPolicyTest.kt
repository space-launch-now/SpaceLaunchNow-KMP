package me.calebjones.spacelaunchnow.widgets

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class WidgetRefreshPolicyTest {

    private val now = Instant.parse("2026-11-20T12:00:00Z")

    private fun listWith(netOffset: Duration?, statusId: Int = 1) =
        listOf(widgetLaunch("a", netOffset?.let { now + it }, statusId))

    private fun maxAge(netOffset: Duration?, statusId: Int = 1) =
        WidgetRefreshPolicy.maxCacheAge(listWith(netOffset, statusId), now)

    private fun due(netOffset: Duration?, cacheAge: Duration, statusId: Int = 1) =
        WidgetRefreshPolicy.isRefreshDue(listWith(netOffset, statusId), now - cacheAge, now)

    // -- Row 1: more than 24 hours away, 6 hours -------------------------------

    @Test
    fun farLaunchAllowsSixHours() {
        assertEquals(6.hours, maxAge(24.hours + 1.seconds))
        assertEquals(6.hours, maxAge(10.days))
        assertFalse(due(2.days, cacheAge = 6.hours))
        assertTrue(due(2.days, cacheAge = 6.hours + 1.seconds))
    }

    @Test
    fun exactly24HoursAwayIsTheOneHourRow() {
        assertEquals(1.hours, maxAge(24.hours))
    }

    // -- Row 2: 24 hours to 1 hour away, 1 hour --------------------------------

    @Test
    fun nearLaunchAllowsOneHour() {
        assertEquals(1.hours, maxAge(23.hours))
        assertEquals(1.hours, maxAge(1.hours))
        assertFalse(due(5.hours, cacheAge = 1.hours))
        assertTrue(due(5.hours, cacheAge = 1.hours + 1.seconds))
        assertFalse(due(5.hours, cacheAge = 59.minutes))
    }

    @Test
    fun justUnderOneHourAwayIsTheFifteenMinuteRow() {
        assertEquals(15.minutes, maxAge(1.hours - 1.seconds))
    }

    // -- Row 3: under 1 hour, Hold, In Flight, NET past by under 1 hour --------

    @Test
    fun imminentLaunchAllowsFifteenMinutes() {
        assertEquals(15.minutes, maxAge(30.minutes))
        assertEquals(15.minutes, maxAge(Duration.ZERO))
        assertFalse(due(30.minutes, cacheAge = 15.minutes))
        assertTrue(due(30.minutes, cacheAge = 15.minutes + 1.seconds))
    }

    @Test
    fun netPassedUnderOneHourAgoIsImminent() {
        assertEquals(15.minutes, maxAge(-(1.hours - 1.seconds)))
        assertTrue(due(-30.minutes, cacheAge = 16.minutes))
    }

    @Test
    fun netPassedOneHourAgoOrMoreFallsBackToOneHour() {
        assertEquals(1.hours, maxAge(-1.hours))
        assertEquals(1.hours, maxAge(-5.hours))
    }

    @Test
    fun holdAndInFlightAreImminentWhateverTheNet() {
        assertEquals(15.minutes, maxAge(3.days, statusId = 5))
        assertEquals(15.minutes, maxAge(-3.hours, statusId = 6))
        assertEquals(15.minutes, maxAge(null, statusId = 6))
        assertTrue(due(3.days, cacheAge = 16.minutes, statusId = 5))
    }

    @Test
    fun theNearestLaunchDecides() {
        val list = listOf(
            widgetLaunch("far", now + 3.days),
            widgetLaunch("soon", now + 2.hours),
            widgetLaunch("later", now + 5.days)
        )
        assertEquals(1.hours, WidgetRefreshPolicy.maxCacheAge(list, now))
    }

    // -- Empty cache, no NET ---------------------------------------------------

    @Test
    fun noCachedListIsDue() {
        assertTrue(WidgetRefreshPolicy.isRefreshDue(emptyList(), cachedAt = null, now = now))
        assertTrue(WidgetRefreshPolicy.isRefreshDue(listWith(5.hours), cachedAt = null, now = now))
    }

    @Test
    fun listWithNoNetAllowsSixHours() {
        assertEquals(6.hours, maxAge(null))
        assertFalse(due(null, cacheAge = 6.hours))
        assertTrue(due(null, cacheAge = 6.hours + 1.seconds))
    }

    @Test
    fun cachedEmptyListUsesTheSixHourRow() {
        assertEquals(6.hours, WidgetRefreshPolicy.maxCacheAge(emptyList(), now))
        assertFalse(WidgetRefreshPolicy.isRefreshDue(emptyList(), now - 5.hours, now))
    }

    @Test
    fun listStoredInTheFutureIsDue() {
        assertTrue(WidgetRefreshPolicy.isRefreshDue(listWith(3.days), now + 1.hours, now))
    }

    // -- Who needs the list ----------------------------------------------------

    @Test
    fun lockedLaunchListWidgetNeedsNoList() {
        assertFalse(WidgetRefreshPolicy.widgetsNeedList(nextUpPlaced = false, launchListPlaced = true, hasAccess = false))
        assertTrue(WidgetRefreshPolicy.widgetsNeedList(nextUpPlaced = false, launchListPlaced = true, hasAccess = true))
        assertTrue(WidgetRefreshPolicy.widgetsNeedList(nextUpPlaced = true, launchListPlaced = true, hasAccess = false))
        assertFalse(WidgetRefreshPolicy.widgetsNeedList(nextUpPlaced = false, launchListPlaced = false, hasAccess = true))
    }
}
