package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit

/**
 * Test suite for simplified V5 notification filtering
 *
 * Tests the bug fix where V5 filters now use String-based IDs and reuse NotificationState.
 * The bug: Users with custom filters (e.g., "SpaceX + Florida") were receiving ALL notifications.
 *
 * Expected behavior after fix:
 * - V5NotificationPayload uses String IDs (matching server format)
 * - V5NotificationFilter uses NotificationState (String-based, like V4)
 * - Filtering logic matches V4 pattern: simple String membership checks
 *
 * Test scenarios:
 * 1. SpaceX from Florida - ALLOW when subscribed
 * 2. China from Jiuquan - BLOCK when not subscribed (bug validation)
 * 3. ULA from Florida flexible - ALLOW when location matches
 * 4. ULA from Florida strict - BLOCK when agency doesn't match
 * 5. Follow all launches - ALLOW everything
 * 6. Notifications disabled - BLOCK everything
 * 7. Multiple agencies - ALLOW any match
 */
class V5NotificationFilterSimplifiedTest {

    @BeforeTest
    fun setup() {
        // Initialize logger for tests
        TestSpaceLoggerInit.ensureInitialized()
    }

    // MARK: - Test Helper Functions

    /**
     * Create a test V5NotificationPayload with String IDs
     *
     * Note: After the fix, all ID fields should be String? (not Int?)
     */
    private fun createTestPayload(
        notificationType: String = "twentyFourHour",
        title: String = "Test Launch",
        body: String = "Test notification",
        launchUuid: String = "12345678-1234-1234-1234-123456789abc",
        launchId: String = "1234",
        launchName: String = "Falcon 9 Block 5 | Starlink Group 6-42",
        launchImage: String? = null,
        launchNet: String = "2026-02-20T12:00:00Z",
        launchLocation: String = "Cape Canaveral SLC-40, Florida",
        webcast: Boolean = true,
        webcastLive: Boolean = false,
        lspId: String? = null,              // String, not Int
        locationId: String? = null,          // String, not Int
        programId: String? = null,           // String, not List<Int>
        statusId: String? = null,
        orbitId: String? = null,
        missionTypeId: String? = null,
        launcherFamilyId: String? = null
    ): V5NotificationPayload {
        // Note: This won't compile until V5NotificationPayload is changed to use String fields
        return V5NotificationPayload(
            notificationType = notificationType,
            title = title,
            body = body,
            launchUuid = launchUuid,
            launchId = launchId,
            launchName = launchName,
            launchImage = launchImage,
            launchNet = launchNet,
            launchLocation = launchLocation,
            webcast = webcast,
            webcastLive = webcastLive,
            lspId = lspId,
            locationId = locationId,
            programId = programId,
            statusId = statusId,
            orbitId = orbitId,
            missionTypeId = missionTypeId,
            launcherFamilyId = launcherFamilyId
        )
    }

    /**
     * Create a test NotificationState with String-based subscriptions
     */
    private fun createTestState(
        enableNotifications: Boolean = true,
        followAllLaunches: Boolean = false,
        useStrictMatching: Boolean = false,
        subscribedAgencies: Set<String> = emptySet(),
        subscribedLocations: Set<String> = emptySet(),
        webcastOnly: Boolean = false
    ): NotificationState {
        val topicSettings = if (webcastOnly) {
            mapOf(NotificationTopic.WEBCAST_ONLY.id to true)
        } else {
            NotificationTopic.getDefaultTopicSettings()
        }
        
        return NotificationState(
            enableNotifications = enableNotifications,
            followAllLaunches = followAllLaunches,
            useStrictMatching = useStrictMatching,
            subscribedAgencies = subscribedAgencies,
            subscribedLocations = subscribedLocations,
            topicSettings = topicSettings
        )
    }

    // MARK: - Test Cases

    /**
     * Test 1: SpaceX from Florida - ALLOW when subscribed
     *
     * Scenario: User subscribes to SpaceX (121) + Florida (12)
     * Notification: SpaceX (121) launch from Florida (12)
     * Expected: ALLOWED ✅
     */
    @Test
    fun `test SpaceX from Florida - ALLOW when subscribed`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "121",      // SpaceX
            locationId = "12"   // Cape Canaveral, Florida
        )
        val state = createTestState(
            subscribedAgencies = setOf("121"),  // SpaceX subscribed
            subscribedLocations = setOf("12")   // Florida subscribed
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Allowed, "SpaceX from Florida should be ALLOWED when subscribed")
    }

    /**
     * Test 2: China from Jiuquan - BLOCK when not subscribed (BUG VALIDATION)
     *
     * Scenario: User subscribes to SpaceX (121) + Florida (12)
     * Notification: China (96) launch from Jiuquan (17)
     * Expected: BLOCKED ✅
     *
     * This is the bug fix validation test - before the fix, this would PASS (incorrect behavior)
     */
    @Test
    fun `test China from Jiuquan - BLOCK when not subscribed`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "96",       // China Aerospace Science and Technology Corporation (CASC)
            locationId = "17"   // Jiuquan Satellite Launch Center, China
        )
        val state = createTestState(
            subscribedAgencies = setOf("121"),  // SpaceX subscribed (not China)
            subscribedLocations = setOf("12")   // Florida subscribed (not Jiuquan)
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Blocked, "China from Jiuquan should be BLOCKED when not subscribed")
        val blocked = result as FilterResult.Blocked
        assertTrue(
            blocked.reason.contains("criteria not met", ignoreCase = true) ||
            blocked.reason.contains("no match", ignoreCase = true),
            "Block reason should indicate filter criteria not met"
        )
    }

    /**
     * Test 3: ULA from Florida flexible - ALLOW when location matches
     *
     * Scenario: User subscribes to SpaceX (121) + Florida (12), flexible matching (OR logic)
     * Notification: ULA (124) launch from Florida (12)
     * Expected: ALLOWED ✅ (location matches, flexible mode uses OR)
     */
    @Test
    fun `test ULA from Florida flexible - ALLOW when location matches`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "124",      // United Launch Alliance (ULA)
            locationId = "12"   // Cape Canaveral, Florida
        )
        val state = createTestState(
            subscribedAgencies = setOf("121"),  // SpaceX subscribed (not ULA)
            subscribedLocations = setOf("12"),  // Florida subscribed
            useStrictMatching = false           // Flexible mode: agency OR location
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Allowed, "ULA from Florida should be ALLOWED in flexible mode (location matches)")
    }

    /**
     * Test 4: ULA from Florida strict - BLOCK when agency doesn't match
     *
     * Scenario: User subscribes to SpaceX (121) + Florida (12), strict matching (AND logic)
     * Notification: ULA (124) launch from Florida (12)
     * Expected: BLOCKED ✅ (agency doesn't match, strict mode requires BOTH)
     */
    @Test
    fun `test ULA from Florida strict - BLOCK when agency doesn't match`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "124",      // United Launch Alliance (ULA)
            locationId = "12"   // Cape Canaveral, Florida
        )
        val state = createTestState(
            subscribedAgencies = setOf("121"),  // SpaceX subscribed (not ULA)
            subscribedLocations = setOf("12"),  // Florida subscribed
            useStrictMatching = true            // Strict mode: agency AND location
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Blocked, "ULA from Florida should be BLOCKED in strict mode (agency doesn't match)")
    }

    /**
     * Test 5: Follow all launches - ALLOW everything
     *
     * Scenario: User enables "Follow All Launches"
     * Notification: Any launch (China from Jiuquan)
     * Expected: ALLOWED ✅ (bypass all filters)
     */
    @Test
    fun `test Follow all launches - ALLOW everything`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "96",       // China
            locationId = "17"   // Jiuquan
        )
        val state = createTestState(
            followAllLaunches = true,           // Master bypass enabled
            subscribedAgencies = setOf("121"),  // Even with specific subscriptions...
            subscribedLocations = setOf("12")   // ...follow all should bypass filters
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Allowed, "Follow all launches should ALLOW everything (bypass filters)")
    }

    /**
     * Test 6: Notifications disabled - BLOCK everything
     *
     * Scenario: User disables notifications globally
     * Notification: Any launch (SpaceX from Florida, even if subscribed)
     * Expected: BLOCKED ✅ (master switch off)
     */
    @Test
    fun `test Notifications disabled - BLOCK everything`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "121",      // SpaceX
            locationId = "12"   // Florida
        )
        val state = createTestState(
            enableNotifications = false,        // Master switch off
            subscribedAgencies = setOf("121"),  // Even with subscriptions...
            subscribedLocations = setOf("12")   // ...disabled should block all
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Blocked, "Notifications disabled should BLOCK everything")
        val blocked = result as FilterResult.Blocked
        assertTrue(
            blocked.reason.contains("disabled", ignoreCase = true),
            "Block reason should indicate notifications are disabled"
        )
    }

    /**
     * Test 7: Multiple agencies - ALLOW any match
     *
     * Scenario: User subscribes to SpaceX (121) + ULA (124) + Florida (12)
     * Notification: ULA (124) from Texas (143)
     * Expected: ALLOWED ✅ (agency matches one of the subscribed agencies)
     */
    @Test
    fun `test Multiple agencies - ALLOW any match`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "124",      // ULA (one of subscribed agencies)
            locationId = "143"  // Texas (not subscribed)
        )
        val state = createTestState(
            subscribedAgencies = setOf("121", "124", "44"),  // SpaceX, ULA, NASA
            subscribedLocations = setOf("12"),               // Florida only
            useStrictMatching = false                        // Flexible: OR logic
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Allowed, "ULA launch should be ALLOWED when ULA is in subscribed agencies")
    }

    // MARK: - Edge Cases

    /**
     * Test 8: Empty filters - BLOCK when both filters empty
     *
     * Scenario: User has no agencies or locations subscribed
     * Notification: Any launch
     * Expected: BLOCKED ✅ (nothing subscribed = block all)
     */
    @Test
    fun `test Empty filters - BLOCK when both filters empty`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "121",
            locationId = "12"
        )
        val state = createTestState(
            subscribedAgencies = emptySet(),  // No agencies
            subscribedLocations = emptySet()  // No locations
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Blocked, "Empty filters should BLOCK all notifications")
    }

    /**
     * Test 9: Webcast-only filter - BLOCK when no webcast
     *
     * Scenario: User enables "Webcast Only"
     * Notification: Launch with no webcast
     * Expected: BLOCKED ✅
     */
    @Test
    fun `test Webcast-only filter - BLOCK when no webcast`() {
        // Arrange
        val payload = createTestPayload(
            lspId = "121",
            locationId = "12",
            webcast = false  // No webcast
        )
        val state = createTestState(
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("12"),
            webcastOnly = true  // Webcast-only filter enabled
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Blocked, "Webcast-only filter should BLOCK launches without webcast")
    }

    /**
     * Test 10: Null payload IDs - BLOCK when payload lacks filtering IDs
     *
     * Scenario: Notification has null lspId and locationId (malformed payload)
     * Expected: BLOCKED ✅ (can't match empty IDs)
     */
    @Test
    fun `test Null payload IDs - BLOCK when payload lacks filtering IDs`() {
        // Arrange
        val payload = createTestPayload(
            lspId = null,       // Missing
            locationId = null   // Missing
        )
        val state = createTestState(
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("12")
        )

        // Act
        val result = V5NotificationFilter.shouldShow(payload, state)

        // Assert
        assertTrue(result is FilterResult.Blocked, "Payload with null IDs should be BLOCKED")
    }
}
