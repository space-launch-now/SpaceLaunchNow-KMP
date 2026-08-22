package me.calebjones.spacelaunchnow.data.notifications

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import me.calebjones.spacelaunchnow.data.model.FilterResult
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.model.V5NotificationPayload
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit

/**
 * The client-side filter gate. Under V6 the server targets by topic, so a device whose
 * V5->V6 changeover has completed must show what it receives (kill switch aside); a device
 * still on the V5 broadcast must keep the legacy filter or every launch would show.
 */
class LocalFilterPolicyTest {

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
    }

    // Strict-matching SpaceX-from-Florida user: the legacy filter blocks anything else.
    private fun strictSpaceXOnly(changeoverComplete: Boolean) = NotificationState.DEFAULT.copy(
        enableNotifications = true,
        followAllLaunches = false,
        useStrictMatching = true,
        subscribedAgencies = setOf("121"),
        subscribedLocations = setOf("27"),
        hasCompletedV6Changeover = changeoverComplete,
    )

    // A Chinese launch from Jiuquan -- not SpaceX, not Florida.
    private fun chineseLaunch() = V5NotificationPayload(
        notificationType = "tenMinutes",
        title = "Long March 2D | Test",
        body = "Launching in 10 minutes",
        launchUuid = "12345678-1234-1234-1234-123456789abc",
        launchId = "1234",
        launchName = "Long March 2D | Test",
        launchImage = null,
        launchNet = "2026-08-22T12:00:00Z",
        launchLocation = "Jiuquan Satellite Launch Center",
        webcast = true,
        webcastLive = false,
        lspId = "88",
        locationId = "17",
        programId = null,
        statusId = null,
        orbitId = null,
        missionTypeId = null,
        launcherFamilyId = null,
    )

    @Test
    fun `local filter is active until the changeover completes`() {
        assertTrue(LocalFilterPolicy.isActive(strictSpaceXOnly(changeoverComplete = false)))
        assertFalse(LocalFilterPolicy.isActive(strictSpaceXOnly(changeoverComplete = true)))
    }

    @Test
    fun `a launch the legacy filter would block is shown once the device is on V6`() {
        val decision = LocalFilterPolicy.launchDecision(chineseLaunch(), strictSpaceXOnly(changeoverComplete = true))
        assertTrue(decision.shouldShow(), "server-targeted sends must not be re-filtered locally")
    }

    @Test
    fun `a device still on the V5 broadcast keeps running the legacy launch filter`() {
        val decision = LocalFilterPolicy.launchDecision(chineseLaunch(), strictSpaceXOnly(changeoverComplete = false))
        assertFalse(decision.shouldShow())
        assertTrue(!decision.getBlockReason().isNullOrBlank(), "a legacy block must carry its reason")
    }

    @Test
    fun `the kill switch still blocks on a V6 device`() {
        val off = strictSpaceXOnly(changeoverComplete = true).copy(enableNotifications = false)
        val decision = LocalFilterPolicy.launchDecision(chineseLaunch(), off)
        assertFalse(decision.shouldShow())
        assertEquals(FilterResult.Companion.Reasons.NOTIFICATIONS_DISABLED, decision.getBlockReason())
        assertEquals(LocalFilterPolicy.BroadcastBlock.KILL_SWITCH, LocalFilterPolicy.broadcastBlock(NotificationTopic.EVENTS, off))
    }

    @Test
    fun `a broadcast per-type toggle only blocks while the local filter is active`() {
        val eventsOff = strictSpaceXOnly(changeoverComplete = false).withTopicEnabled(NotificationTopic.EVENTS, false)
        assertEquals(LocalFilterPolicy.BroadcastBlock.TOGGLE_OFF, LocalFilterPolicy.broadcastBlock(NotificationTopic.EVENTS, eventsOff))

        val onV6 = eventsOff.copy(hasCompletedV6Changeover = true)
        assertNull(LocalFilterPolicy.broadcastBlock(NotificationTopic.EVENTS, onV6), "the subscription already encodes the toggle")
    }

    @Test
    fun `a broadcast with its toggle on is shown either way`() {
        assertNull(LocalFilterPolicy.broadcastBlock(NotificationTopic.EVENTS, strictSpaceXOnly(changeoverComplete = false)))
        assertNull(LocalFilterPolicy.broadcastBlock(NotificationTopic.EVENTS, strictSpaceXOnly(changeoverComplete = true)))
    }

    @Test
    fun `the map entry point fails open on V6 and keeps failing closed on V5`() {
        // An unparseable payload: the legacy path suppresses it (today's behaviour); the V6 path
        // never parses for filtering, so it shows -- the NSE/AppDelegate render what they can.
        assertTrue(LocalFilterPolicy.legacyLaunchAllowedFromMap(emptyMap(), strictSpaceXOnly(changeoverComplete = true)))
        assertFalse(LocalFilterPolicy.legacyLaunchAllowedFromMap(emptyMap(), strictSpaceXOnly(changeoverComplete = false)))
    }
}
