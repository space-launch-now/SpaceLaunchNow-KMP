package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.data.model.NotificationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V6TopicsTest {

    // Flexible matching, SpaceX (121) + Florida (27), only tenMinutes enabled,
    // webcastOnly off, all broadcast toggles off. Topic strings are asserted
    // against literals on purpose: a format helper shared with production code
    // would drift with it and prove nothing.
    private fun baseState() = NotificationState(
        enableNotifications = true,
        followAllLaunches = false,
        useStrictMatching = false,
        topicSettings = mapOf(
            "tenMinutes" to true,
            "twentyFourHour" to false, "oneHour" to false, "oneMinute" to false,
            "netstampChanged" to false, "webcastLive" to false, "inFlight" to false,
            "success" to false, "failure" to false, "partial_failure" to false,
            "webcastOnly" to false,
            "events" to false, "featured_news" to false, "announcements" to false,
        ),
        subscribedAgencies = setOf("121"),
        subscribedLocations = setOf("27"),
    )

    @Test
    fun audienceClassCoversAllEightSettingCombinations() {
        fun state(followAll: Boolean, strict: Boolean, webcast: Boolean) = baseState().copy(
            followAllLaunches = followAll,
            useStrictMatching = strict,
            topicSettings = baseState().topicSettings + ("webcastOnly" to webcast),
        )
        assertEquals("all", V6Topics.audienceClass(state(true, false, false)))
        assertEquals("all_w", V6Topics.audienceClass(state(true, false, true)))
        // Follow-all wins over strict, matching the UI which disables the
        // strict toggle when follow-all is on.
        assertEquals("all", V6Topics.audienceClass(state(true, true, false)))
        assertEquals("all_w", V6Topics.audienceClass(state(true, true, true)))
        assertEquals("strict", V6Topics.audienceClass(state(false, true, false)))
        assertEquals("strict_w", V6Topics.audienceClass(state(false, true, true)))
        assertEquals("flex", V6Topics.audienceClass(state(false, false, false)))
        assertEquals("flex_w", V6Topics.audienceClass(state(false, false, true)))
    }

    @Test
    fun flexClassEmitsTypeAndAttributeTopics() {
        assertEquals(
            setOf("v6_prod_ios_flex_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            V6Topics.requiredTopics(baseState(), env = "prod", platform = "ios"),
        )
    }

    @Test
    fun disabledNotificationsDeriveTheEmptySet() {
        val state = baseState().copy(enableNotifications = false)
        assertEquals(emptySet(), V6Topics.requiredTopics(state, "prod", "ios"))
    }

    @Test
    fun followAllEmitsNoAttributeTopics() {
        val state = baseState().copy(followAllLaunches = true)
        assertEquals(
            setOf("v6_prod_ios_all_tenMinutes"),
            V6Topics.requiredTopics(state, "prod", "ios"),
        )
    }

    @Test
    fun muteStarlinkEmitsTheOptOutTopic() {
        val state = baseState().copy(muteStarlink = true)
        assertEquals(
            setOf("v6_prod_ios_flex_tenMinutes", "v6_prod_spacex", "v6_prod_florida", "v6_prod_starlinkMuted"),
            V6Topics.requiredTopics(state, "prod", "ios"),
        )
    }

    @Test
    fun muteStarlinkAppliesUnderFollowAllToo() {
        // Follow-all receives every launch, so the mute matters there most.
        val state = baseState().copy(followAllLaunches = true, muteStarlink = true)
        assertEquals(
            setOf("v6_prod_ios_all_tenMinutes", "v6_prod_starlinkMuted"),
            V6Topics.requiredTopics(state, "prod", "ios"),
        )
    }

    @Test
    fun muteStarlinkOffEmitsNoOptOutTopic() {
        assertTrue(V6Topics.requiredTopics(baseState(), "prod", "ios").none { it.contains("starlinkMuted") })
    }

    @Test
    fun muteStarlinkTopicIsEnvScopedAndNotPlatformScoped() {
        val state = baseState().copy(muteStarlink = true)
        assertTrue("v6_debug_starlinkMuted" in V6Topics.requiredTopics(state, "debug", "android"))
    }

    @Test
    fun webcastOnlyIsAClassSuffixNotATypeTopic() {
        val state = baseState().copy(
            topicSettings = baseState().topicSettings + ("webcastOnly" to true)
        )
        val topics = V6Topics.requiredTopics(state, "prod", "ios")
        assertEquals(
            setOf("v6_prod_ios_flex_w_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            topics,
        )
        assertTrue(topics.none { it.endsWith("_webcastOnly") })
    }

    @Test
    fun broadcastTogglesTranslateSettingIdsToWireTokens() {
        val state = baseState().copy(
            topicSettings = baseState().topicSettings +
                mapOf("events" to true, "featured_news" to true, "announcements" to true)
        )
        val topics = V6Topics.requiredTopics(state, "prod", "android")
        assertTrue("v6_prod_android_events" in topics)
        // featured_news -> news, announcements -> announce. Subscribing to
        // v6_prod_android_featured_news would reach nothing.
        assertTrue("v6_prod_android_news" in topics)
        assertTrue("v6_prod_android_announce" in topics)
        assertTrue(topics.none { it.contains("featured_news") || it.contains("announcements") })
    }

    @Test
    fun everyEnabledLaunchTypeGetsAClassScopedTypeTopic() {
        val allOn = baseState().copy(
            topicSettings = baseState().topicSettings + mapOf(
                "twentyFourHour" to true, "oneHour" to true, "oneMinute" to true,
                "netstampChanged" to true, "webcastLive" to true, "inFlight" to true,
                "success" to true, "failure" to true, "partial_failure" to true,
            )
        )
        val typeTopics = V6Topics.requiredTopics(allOn, "debug", "android")
            .filter { it.startsWith("v6_debug_android_flex_") }
        assertEquals(
            setOf(
                "v6_debug_android_flex_twentyFourHour", "v6_debug_android_flex_oneHour",
                "v6_debug_android_flex_tenMinutes", "v6_debug_android_flex_oneMinute",
                "v6_debug_android_flex_netstampChanged", "v6_debug_android_flex_webcastLive",
                "v6_debug_android_flex_inFlight", "v6_debug_android_flex_success",
                "v6_debug_android_flex_failure", "v6_debug_android_flex_partial_failure",
            ),
            typeTopics.toSet(),
        )
    }

    @Test
    fun attributeTopicsAreSharedAcrossPlatforms() {
        val ios = V6Topics.requiredTopics(baseState(), "prod", "ios")
        val android = V6Topics.requiredTopics(baseState(), "prod", "android")
        assertTrue("v6_prod_spacex" in ios)
        assertTrue("v6_prod_spacex" in android)
    }

    @Test
    fun otherAgencySelectionDerivesItsAttributeTopic() {
        val state = baseState().copy(subscribedAgencies = setOf("-1"))
        assertTrue("v6_prod_otherAgency" in V6Topics.requiredTopics(state, "prod", "ios"))
    }

    @Test
    fun unknownStoredIdsAreSkippedNotCrashed() {
        val state = baseState().copy(
            subscribedAgencies = setOf("121", "99999"),
            subscribedLocations = setOf("27", "99999"),
        )
        assertEquals(
            setOf("v6_prod_ios_flex_tenMinutes", "v6_prod_spacex", "v6_prod_florida"),
            V6Topics.requiredTopics(state, "prod", "ios"),
        )
    }
}
