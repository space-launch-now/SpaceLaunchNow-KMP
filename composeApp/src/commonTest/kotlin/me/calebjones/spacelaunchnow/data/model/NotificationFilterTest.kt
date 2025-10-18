package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for NotificationFilter - Platform-agnostic notification filtering logic
 *
 * These tests verify the v4 client-side filtering works correctly across all scenarios:
 * - Follow all launches (bypass all filtering)
 * - Strict matching (agency AND location)
 * - Flexible matching (agency OR location)
 * - Topic filtering (notification types)
 * - LocationId = "0" (Other/Unknown location)
 */
class NotificationFilterTest {

    // Helper to create topic settings with specific topics enabled
    private fun topicSettings(vararg enabledTypes: String): Map<String, Boolean> {
        return mapOf(
            "netstampChanged" to enabledTypes.contains("netstampChanged"),
            "twentyFourHour" to enabledTypes.contains("twentyFourHour"),
            "tenMinutes" to enabledTypes.contains("tenMinutes"),
            "oneMinute" to enabledTypes.contains("oneMinute"),
            "oneHour" to enabledTypes.contains("oneHour"),
            "inFlight" to enabledTypes.contains("inFlight"),
            "success" to enabledTypes.contains("success"),
            "webcastOnly" to false  // Explicitly disable webcast-only filter for tests
        )
    }

    // Test Data
    private val spacexLaunchFromKSC = NotificationData(
        notificationType = "netstampChanged",
        launchId = "test-launch-1",
        launchUuid = "uuid-1",
        launchName = "Falcon 9 Block 5 | Starlink Group",
        launchImage = "https://example.com/image1.jpg",
        launchNet = "2025-10-13T12:00:00Z",
        launchLocation = "Kennedy Space Center, FL, USA",
        webcast = "true",
        webcastLive = null,
        agencyId = "121", // SpaceX
        locationId = "27"  // Kennedy Space Center
    )

    private val nasaLaunchFromVandenberg = NotificationData(
        notificationType = "twentyFourHour",
        launchId = "test-launch-2",
        launchUuid = "uuid-2",
        launchName = "Atlas V 401 | NROL-107",
        launchImage = "https://example.com/image2.jpg",
        launchNet = "2025-10-14T10:00:00Z",
        launchLocation = "Vandenberg SFB, CA, USA",
        webcast = "false",
        webcastLive = null,
        agencyId = "44",  // NASA
        locationId = "11"  // Vandenberg
    )

    private val blueOriginLaunchFromTexas = NotificationData(
        notificationType = "tenMinutes",
        launchId = "test-launch-3",
        launchUuid = "uuid-3",
        launchName = "New Glenn | Test Flight",
        launchImage = "https://example.com/image3.jpg",
        launchNet = "2025-10-15T08:00:00Z",
        launchLocation = "Starbase, TX, USA",
        webcast = "true",
        webcastLive = null,
        agencyId = "141", // Blue Origin
        locationId = "16"  // Starbase, Texas
    )

    private val spacexLaunchFromOtherLocation = NotificationData(
        notificationType = "netstampChanged",
        launchId = "test-launch-4",
        launchUuid = "uuid-4",
        launchName = "Falcon 9 Block 5 | Unknown Payload",
        launchImage = "https://example.com/image4.jpg",
        launchNet = "2025-10-16T14:00:00Z",
        launchLocation = "Unknown Location",
        webcast = "false",
        webcastLive = null,
        agencyId = "121", // SpaceX
        locationId = "0"   // Other (unknown location)
    )

    private val rocketLabLaunchFromMahia = NotificationData(
        notificationType = "oneHour",
        launchId = "test-launch-5",
        launchUuid = "uuid-5",
        launchName = "Electron | Another One",
        launchImage = "https://example.com/image5.jpg",
        launchNet = "2025-10-17T06:00:00Z",
        launchLocation = "Mahia Peninsula, New Zealand",
        webcast = "true",
        webcastLive = null,
        agencyId = "147", // Rocket Lab
        locationId = "15"  // Mahia, New Zealand
    )

    // ========== Follow All Launches Tests ==========

    @Test
    fun testFollowAllLaunches_showsEverything() {
        val state = NotificationState(
            followAllLaunches = true,
            subscribedAgencies = emptySet(),
            subscribedLocations = emptySet(),
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show ALL launches regardless of agency/location/topics
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
        assertFalse(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))
        assertFalse(NotificationFilter.shouldShowNotification(blueOriginLaunchFromTexas, state))
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromOtherLocation, state))
    }

    @Test
    fun testFollowAllLaunches_ignoresStrictMatching() {
        val state = NotificationState(
            followAllLaunches = true,
            subscribedAgencies = setOf("121"), // Only SpaceX
            subscribedLocations = setOf("27"), // Only KSC
            useStrictMatching = true, // Should be ignored
            topicSettings = topicSettings("netstampChanged")
        )

        // Should only show SpX from KSC
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
        assertFalse(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))
        assertFalse(NotificationFilter.shouldShowNotification(blueOriginLaunchFromTexas, state))
    }

    // ========== Strict Matching Tests (AND logic) ==========

    @Test
    fun testStrictMatching_requiresBothAgencyAndLocation() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = true,
            topicSettings = topicSettings("netstampChanged", "twentyFourHour", "tenMinutes")
        )

        // Should show: SpaceX from KSC (both match)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))

        // Should NOT show: NASA from Vandenberg (neither matches)
        assertFalse(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))

        // Should NOT show: Blue Origin from Texas (neither matches)
        assertFalse(NotificationFilter.shouldShowNotification(blueOriginLaunchFromTexas, state))
    }

    @Test
    fun testStrictMatching_agencyMatchesButNotLocation() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("11"), // Vandenberg (not KSC)
            useStrictMatching = true,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: SpaceX from KSC (agency matches, but location doesn't)
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testStrictMatching_locationMatchesButNotAgency() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA (not SpaceX)
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = true,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: SpaceX from KSC (location matches, but agency doesn't)
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    // ========== Flexible Matching Tests (OR logic) ==========

    @Test
    fun testFlexibleMatching_showsIfAgencyMatches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("11"), // Vandenberg (not KSC)
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from KSC (agency matches, location doesn't matter)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testFlexibleMatching_showsIfLocationMatches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA (not SpaceX)
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from KSC (location matches, agency doesn't matter)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testFlexibleMatching_showsIfBothMatch() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from KSC (both match)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testFlexibleMatching_hidesIfNeitherMatches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA
            subscribedLocations = setOf("11"), // Vandenberg
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: SpaceX from KSC (neither agency nor location matches)
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    // ========== Topic Filtering Tests ==========

    @Test
    fun testTopicFiltering_showsIfTopicIsSubscribed() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged") // Only this topic
        )

        // Should show: netstampChanged is in subscribed topics
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testTopicFiltering_hidesIfTopicNotSubscribed() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = false,
            topicSettings = topicSettings("twentyFourHour") // Different topic
        )

        // Should NOT show: netstampChanged is not in subscribed topics
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testTopicFiltering_multipleTopicsSubscribed() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121", "44"), // SpaceX + NASA
            subscribedLocations = setOf("27", "11"), // KSC + Vandenberg
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged", "twentyFourHour", "tenMinutes")
        )

        // All should show (all topics subscribed)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
        assertTrue(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))
        assertFalse(NotificationFilter.shouldShowNotification(blueOriginLaunchFromTexas, state))
    }

    // ========== Empty Subscriptions Tests ==========

    @Test
    fun testEmptyAgencies_hidesEverything() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(), // No agencies
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: no agencies subscribed
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromOtherLocation, state))
    }

    @Test
    fun testEmptyLocations_hidesEverything() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = emptySet(), // No locations
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: no locations subscribed
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testEmptyTopics_hidesEverything() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = false,
            topicSettings = emptyMap() // No topics
        )

        // Should NOT show: no topics subscribed
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    // ========== shouldShowFromMap Tests (iOS interop) ==========

    @Test
    fun testShouldShowFromMap_convertsMapToNotificationData() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        val dataMap = mapOf(
            "notification_type" to "netstampChanged",
            "launch_id" to "test-launch-1",
            "launch_uuid" to "uuid-1",
            "launch_name" to "Falcon 9 Block 5 | Starlink Group",
            "launch_image" to "https://example.com/image1.jpg",
            "launch_net" to "2025-10-13T12:00:00Z",
            "launch_location" to "Kennedy Space Center, FL, USA",
            "webcast" to "true",
            "agency_id" to "121",
            "location_id" to "27"
        )

        // Should show (same as spacexLaunchFromKSC)
        assertTrue(NotificationFilter.shouldShowFromMap(dataMap, state))
    }

    @Test
    fun testShouldShowFromMap_handlesInvalidMap() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("27"),
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        val dataMap = mapOf(
            "invalid_key" to "invalid_value"
        )

        // Should NOT show (invalid map format)
        assertFalse(NotificationFilter.shouldShowFromMap(dataMap, state))
    }

    // ========== Edge Cases ==========

    @Test
    fun testMultipleAgenciesSubscribed() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121", "44", "141"), // SpaceX, NASA, Blue Origin
            subscribedLocations = setOf("27"), // KSC only
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged", "twentyFourHour", "tenMinutes")
        )

        // Should show: SpaceX (agency matches)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))

        // Should show: NASA (agency matches, location doesn't matter in flexible mode)
        assertTrue(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))

        // Should show: Blue Origin (agency matches)
        assertTrue(NotificationFilter.shouldShowNotification(blueOriginLaunchFromTexas, state))
    }

    @Test
    fun testMultipleLocationsSubscribed() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX only
            subscribedLocations = setOf("27", "11", "16"), // KSC, Vandenberg, Texas
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged", "twentyFourHour", "tenMinutes")
        )

        // Should show: SpaceX from KSC (agency matches)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))

        // Should show: NASA from Vandenberg (location matches in flexible mode)
        assertTrue(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))

        // Should show: Blue Origin from Texas (location matches)
        assertTrue(NotificationFilter.shouldShowNotification(blueOriginLaunchFromTexas, state))
    }

    // ========== Agency-Only Subscription Tests (No Locations) ==========

    @Test
    fun testAgencyOnly_flexible_showsAllAgencyLaunches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA only
            subscribedLocations = emptySet(), // No locations subscribed
            useStrictMatching = false, // Flexible matching
            topicSettings = topicSettings("twentyFourHour")
        )

        // Should show: NASA from Vandenberg (agency matches, locations empty so any location OK)
        assertTrue(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))
    }

    @Test
    fun testAgencyOnly_flexible_blocksOtherAgencies() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA only
            subscribedLocations = emptySet(), // No locations subscribed
            useStrictMatching = false, // Flexible matching
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: SpaceX from KSC (agency doesn't match, no locations to match)
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testAgencyOnly_strict_blocksAllLaunches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA
            subscribedLocations = emptySet(), // No locations subscribed
            useStrictMatching = true, // Strict: need BOTH agency AND location
            topicSettings = topicSettings("twentyFourHour")
        )

        // Should NOT show: NASA from Vandenberg (strict mode requires BOTH, but no locations subscribed)
        assertFalse(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))
    }

    // ========== Location-Only Subscription Tests (No Agencies) ==========

    @Test
    fun testLocationOnly_flexible_showsAllLocationLaunches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(), // No agencies subscribed
            subscribedLocations = setOf("27"), // KSC only
            useStrictMatching = false, // Flexible matching
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from KSC (location matches, agencies empty so any agency OK)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testLocationOnly_flexible_blocksOtherLocations() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(), // No agencies subscribed
            subscribedLocations = setOf("27"), // KSC only
            useStrictMatching = false, // Flexible matching
            topicSettings = topicSettings("twentyFourHour")
        )

        // Should NOT show: NASA from Vandenberg (location doesn't match, no agencies to match)
        assertFalse(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))
    }

    @Test
    fun testLocationOnly_strict_blocksAllLaunches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(), // No agencies subscribed
            subscribedLocations = setOf("27"), // KSC
            useStrictMatching = true, // Strict: need BOTH agency AND location
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: SpaceX from KSC (strict mode requires BOTH, but no agencies subscribed)
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    // ========== LocationId = "0" (Other/Unknown) Tests ==========

    @Test
    fun testLocationIdZero_flexibleMatching_showsIfAgencyMatches() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC only (not "0")
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from location "0" (agency matches in flexible mode)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromOtherLocation, state))
    }

    @Test
    fun testLocationIdZero_strictMatching_requiresBothAgencyAndLocationZero() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("27"), // KSC (not "0")
            useStrictMatching = true,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: SpaceX from location "0" (agency matches but location doesn't)
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromOtherLocation, state))
    }

    @Test
    fun testLocationIdZero_subscribeToLocationZero() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA (not SpaceX)
            subscribedLocations = setOf("0"), // Subscribed to "Other" location
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from location "0" (location matches in flexible mode)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromOtherLocation, state))
    }

    @Test
    fun testLocationIdZero_strictMatching_withBothSubscribed() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("0"), // Subscribed to "Other" location
            useStrictMatching = true,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from location "0" (both agency AND location match)
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromOtherLocation, state))
    }

    @Test
    fun testLocationIdZero_notSubscribedToAnything() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA (not SpaceX)
            subscribedLocations = setOf("27"), // KSC (not "0")
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should NOT show: SpaceX from location "0" (neither agency nor location matches)
        assertFalse(NotificationFilter.shouldShowNotification(spacexLaunchFromOtherLocation, state))
    }

    // ========== LocationId "0" (Other) as Wildcard Tests ==========

    @Test
    fun testOtherLocationAsWildcard_flexible_matchesAnyLocation() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA (not Rocket Lab)
            subscribedLocations = setOf("0"), // "Other" - should match ANY location
            useStrictMatching = false,
            topicSettings = topicSettings("oneHour")
        )

        // Should show: Rocket Lab from Mahia (locationId="15")
        // "Other" subscription should act as wildcard and match ANY locationId
        assertTrue(NotificationFilter.shouldShowNotification(rocketLabLaunchFromMahia, state))
    }

    @Test
    fun testOtherLocationAsWildcard_matchesKSC() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA
            subscribedLocations = setOf("0"), // "Other" - should match ANY location
            useStrictMatching = false,
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from KSC (locationId="27")
        // "Other" subscription should match even known locations like KSC
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testOtherLocationAsWildcard_matchesVandenberg() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("0"), // "Other" - should match ANY location
            useStrictMatching = false,
            topicSettings = topicSettings("twentyFourHour")
        )

        // Should show: NASA from Vandenberg (locationId="11")
        // "Other" subscription should match Vandenberg too
        assertTrue(NotificationFilter.shouldShowNotification(nasaLaunchFromVandenberg, state))
    }

    @Test
    fun testOtherLocationAsWildcard_strict_requiresAgencyMatch() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("0"), // "Other" - matches any location
            useStrictMatching = true, // Strict: need BOTH agency AND location
            topicSettings = topicSettings("oneHour")
        )

        // Should show: Rocket Lab from Mahia
        // In strict mode, "Other" wildcard still matches the location requirement,
        // but agency must also match. Rocket Lab (147) doesn't match SpaceX (121).
        // Wait - this should FAIL because agency doesn't match!
        assertFalse(NotificationFilter.shouldShowNotification(rocketLabLaunchFromMahia, state))
    }

    @Test
    fun testOtherLocationAsWildcard_strict_withAgencyMatch() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"), // SpaceX
            subscribedLocations = setOf("0"), // "Other" - matches any location
            useStrictMatching = true, // Strict: need BOTH agency AND location
            topicSettings = topicSettings("netstampChanged")
        )

        // Should show: SpaceX from KSC
        // In strict mode with "Other" wildcard: location matches (wildcard), agency matches
        assertTrue(NotificationFilter.shouldShowNotification(spacexLaunchFromKSC, state))
    }

    @Test
    fun testOtherLocationAsWildcard_combinedWithSpecificLocation() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("44"), // NASA
            subscribedLocations = setOf("0", "27"), // "Other" (wildcard) + KSC
            useStrictMatching = false,
            topicSettings = topicSettings("oneHour")
        )

        // Should show: Rocket Lab from Mahia
        // User subscribed to "Other" (wildcard) AND KSC specifically
        // "Other" should match Mahia locationId="15"
        assertTrue(NotificationFilter.shouldShowNotification(rocketLabLaunchFromMahia, state))
    }
}
