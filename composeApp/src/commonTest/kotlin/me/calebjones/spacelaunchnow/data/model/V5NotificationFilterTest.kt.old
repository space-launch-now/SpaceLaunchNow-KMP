package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for V5NotificationFilter
 */
class V5NotificationFilterTest {

    // MARK: - Test Data Builders

    private fun createPayload(
        notificationType: String = "tenMinutes",
        lspId: Int? = 121,
        locationId: Int? = 27,
        programIds: List<Int> = emptyList(),
        orbitId: Int? = null,
        missionTypeId: Int? = null,
        launcherFamilyId: Int? = null,
        webcast: Boolean = false
    ): V5NotificationPayload {
        return V5NotificationPayload(
            notificationType = notificationType,
            title = "Test Title",
            body = "Test Body",
            launchUuid = "550e8400-e29b-41d4-a716-446655440000",
            launchId = "12345",
            launchName = "Test Launch",
            launchImage = null,
            launchNet = "2025-01-26T12:00:00Z",
            launchLocation = "Test Location",
            webcast = webcast,
            webcastLive = false,
            lspId = lspId,
            locationId = locationId,
            programIds = programIds,
            statusId = null,
            orbitId = orbitId,
            missionTypeId = missionTypeId,
            launcherFamilyId = launcherFamilyId
        )
    }

    private fun createPreferences(
        enableNotifications: Boolean = true,
        enabledNotificationTypes: Set<String> = V5FilterPreferences.DEFAULT.enabledNotificationTypes,
        subscribedLspIds: Set<Int>? = null,
        subscribedLocationIds: Set<Int>? = null,
        subscribedProgramIds: Set<Int>? = null,
        subscribedOrbitIds: Set<Int>? = null,
        subscribedMissionTypeIds: Set<Int>? = null,
        subscribedLauncherFamilyIds: Set<Int>? = null,
        useStrictMatching: Boolean = false,
        webcastOnly: Boolean = false
    ): V5FilterPreferences {
        return V5FilterPreferences(
            enableNotifications = enableNotifications,
            enabledNotificationTypes = enabledNotificationTypes,
            subscribedLspIds = subscribedLspIds,
            subscribedLocationIds = subscribedLocationIds,
            subscribedProgramIds = subscribedProgramIds,
            subscribedOrbitIds = subscribedOrbitIds,
            subscribedMissionTypeIds = subscribedMissionTypeIds,
            subscribedLauncherFamilyIds = subscribedLauncherFamilyIds,
            useStrictMatching = useStrictMatching,
            webcastOnly = webcastOnly
        )
    }

    // MARK: - Master Enable/Disable Tests (T013)

    @Test
    fun testMasterDisabled_blocksAll() {
        val payload = createPayload()
        val preferences = createPreferences(enableNotifications = false)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result is FilterResult.Blocked)
        assertEquals("Notifications disabled globally", result.getBlockReason())
    }

    @Test
    fun testMasterEnabled_allowsThrough() {
        val payload = createPayload()
        val preferences = createPreferences(enableNotifications = true)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    // MARK: - Notification Type Filtering Tests (T014)

    @Test
    fun testNotificationType_enabled_allows() {
        val payload = createPayload(notificationType = "oneHour")
        val preferences = createPreferences(
            enabledNotificationTypes = setOf("oneHour", "tenMinutes")
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testNotificationType_disabled_blocks() {
        val payload = createPayload(notificationType = "twentyFourHour")
        val preferences = createPreferences(
            enabledNotificationTypes = setOf("oneHour", "tenMinutes")
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertTrue(result.getBlockReason()!!.contains("twentyFourHour"))
    }

    @Test
    fun testNotificationType_emptySet_blocksAll() {
        val payload = createPayload(notificationType = "tenMinutes")
        val preferences = createPreferences(
            enabledNotificationTypes = emptySet()
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
    }

    // MARK: - Webcast-Only Filter Tests (T015)

    @Test
    fun testWebcastOnly_withWebcast_allows() {
        val payload = createPayload(webcast = true)
        val preferences = createPreferences(webcastOnly = true)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testWebcastOnly_withoutWebcast_blocks() {
        val payload = createPayload(webcast = false)
        val preferences = createPreferences(webcastOnly = true)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertEquals("Webcast-only filter enabled, launch has no webcast", result.getBlockReason())
    }

    @Test
    fun testWebcastOnly_disabled_allowsWithoutWebcast() {
        val payload = createPayload(webcast = false)
        val preferences = createPreferences(webcastOnly = false)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    // MARK: - LSP Filter Tests (T016)

    @Test
    fun testLspFilter_null_allowsAll() {
        val payload = createPayload(lspId = 999)
        val preferences = createPreferences(subscribedLspIds = null)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testLspFilter_empty_blocksAll() {
        val payload = createPayload(lspId = 121)
        val preferences = createPreferences(subscribedLspIds = emptySet())

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertEquals("No LSPs subscribed", result.getBlockReason())
    }

    @Test
    fun testLspFilter_matches_allows() {
        val payload = createPayload(lspId = 121)
        val preferences = createPreferences(subscribedLspIds = setOf(121, 122))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testLspFilter_noMatch_blocks() {
        val payload = createPayload(lspId = 999)
        val preferences = createPreferences(subscribedLspIds = setOf(121, 122))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
    }

    @Test
    fun testLspFilter_payloadNull_treatedAsNoMatch() {
        val payload = createPayload(lspId = null)
        val preferences = createPreferences(subscribedLspIds = setOf(121))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
    }

    // MARK: - Location Filter Tests (T017)

    @Test
    fun testLocationFilter_null_allowsAll() {
        val payload = createPayload(locationId = 999)
        val preferences = createPreferences(subscribedLocationIds = null)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testLocationFilter_empty_blocksAll() {
        val payload = createPayload(locationId = 27)
        val preferences = createPreferences(subscribedLocationIds = emptySet())

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertEquals("No locations subscribed", result.getBlockReason())
    }

    @Test
    fun testLocationFilter_matches_allows() {
        val payload = createPayload(locationId = 27)
        val preferences = createPreferences(subscribedLocationIds = setOf(27, 28))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testLocationFilter_noMatch_blocks() {
        val payload = createPayload(locationId = 999)
        val preferences = createPreferences(subscribedLocationIds = setOf(27, 28))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
    }

    // MARK: - Program Filter Tests (T018)

    @Test
    fun testProgramFilter_null_allowsAll() {
        val payload = createPayload(programIds = listOf(1, 2, 3))
        val preferences = createPreferences(subscribedProgramIds = null)

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testProgramFilter_empty_blocksAll() {
        val payload = createPayload(programIds = listOf(1))
        val preferences = createPreferences(subscribedProgramIds = emptySet())

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertEquals("No programs subscribed", result.getBlockReason())
    }

    @Test
    fun testProgramFilter_anyMatch_allows() {
        val payload = createPayload(programIds = listOf(1, 2, 3))
        val preferences = createPreferences(subscribedProgramIds = setOf(3, 4, 5))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())  // Program 3 matches
    }

    @Test
    fun testProgramFilter_noMatch_blocks() {
        val payload = createPayload(programIds = listOf(1, 2, 3))
        val preferences = createPreferences(subscribedProgramIds = setOf(4, 5, 6))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
    }

    @Test
    fun testProgramFilter_emptyPayloadPrograms_blocks() {
        val payload = createPayload(programIds = emptyList())
        val preferences = createPreferences(subscribedProgramIds = setOf(1, 2, 3))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
    }

    // MARK: - Orbit, Mission Type, Launcher Family Filter Tests (T019)

    @Test
    fun testOrbitFilter_matches_allows() {
        val payload = createPayload(orbitId = 8)
        val preferences = createPreferences(subscribedOrbitIds = setOf(8, 9))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testOrbitFilter_empty_blocksAll() {
        val payload = createPayload(orbitId = 8)
        val preferences = createPreferences(subscribedOrbitIds = emptySet())

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertEquals("No orbits subscribed", result.getBlockReason())
    }

    @Test
    fun testMissionTypeFilter_matches_allows() {
        val payload = createPayload(missionTypeId = 10)
        val preferences = createPreferences(subscribedMissionTypeIds = setOf(10, 11))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testMissionTypeFilter_empty_blocksAll() {
        val payload = createPayload(missionTypeId = 10)
        val preferences = createPreferences(subscribedMissionTypeIds = emptySet())

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertEquals("No mission types subscribed", result.getBlockReason())
    }

    @Test
    fun testLauncherFamilyFilter_matches_allows() {
        val payload = createPayload(launcherFamilyId = 5)
        val preferences = createPreferences(subscribedLauncherFamilyIds = setOf(5, 6))

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    @Test
    fun testLauncherFamilyFilter_empty_blocksAll() {
        val payload = createPayload(launcherFamilyId = 5)
        val preferences = createPreferences(subscribedLauncherFamilyIds = emptySet())

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())
        assertEquals("No launcher families subscribed", result.getBlockReason())
    }

    // MARK: - Strict vs Flexible Matching Tests (T020)

    @Test
    fun testFlexibleMatching_anyMatch_allows() {
        val payload = createPayload(lspId = 121, locationId = 999)  // LSP matches, location doesn't
        val preferences = createPreferences(
            subscribedLspIds = setOf(121),
            subscribedLocationIds = setOf(27),  // Doesn't match
            useStrictMatching = false
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())  // Flexible = OR logic, LSP matches
    }

    @Test
    fun testFlexibleMatching_noMatch_blocks() {
        val payload = createPayload(lspId = 999, locationId = 888)
        val preferences = createPreferences(
            subscribedLspIds = setOf(121),
            subscribedLocationIds = setOf(27),
            useStrictMatching = false
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())  // Neither matches
    }

    @Test
    fun testStrictMatching_allMatch_allows() {
        val payload = createPayload(lspId = 121, locationId = 27)
        val preferences = createPreferences(
            subscribedLspIds = setOf(121),
            subscribedLocationIds = setOf(27),
            useStrictMatching = true
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())  // Both match
    }

    @Test
    fun testStrictMatching_partialMatch_blocks() {
        val payload = createPayload(lspId = 121, locationId = 999)  // Only LSP matches
        val preferences = createPreferences(
            subscribedLspIds = setOf(121),
            subscribedLocationIds = setOf(27),
            useStrictMatching = true
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertFalse(result.shouldShow())  // Strict = AND logic, both must match
    }

    @Test
    fun testStrictMatching_noFilters_allows() {
        val payload = createPayload()
        val preferences = createPreferences(
            subscribedLspIds = null,
            subscribedLocationIds = null,
            useStrictMatching = true
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())  // No filters active
    }

    // MARK: - Combined Filter Tests

    @Test
    fun testMultipleFilters_flexible_anyMatchAllows() {
        val payload = createPayload(
            lspId = 121,
            locationId = 999,  // No match
            programIds = listOf(1),
            orbitId = 999  // No match
        )
        val preferences = createPreferences(
            subscribedLspIds = setOf(121),  // Match
            subscribedLocationIds = setOf(27),
            subscribedProgramIds = setOf(2, 3),  // No match
            subscribedOrbitIds = setOf(8),
            useStrictMatching = false
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())  // LSP matches, that's enough for flexible
    }

    @Test
    fun testMultipleFilters_strict_allMustMatch() {
        val payload = createPayload(
            lspId = 121,
            locationId = 27,
            programIds = listOf(3)
        )
        val preferences = createPreferences(
            subscribedLspIds = setOf(121),
            subscribedLocationIds = setOf(27),
            subscribedProgramIds = setOf(3),
            useStrictMatching = true
        )

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())  // All match
    }

    @Test
    fun testNoFiltersActive_allowsAll() {
        val payload = createPayload(lspId = 999, locationId = 888)
        val preferences = createPreferences()  // All null = no filtering

        val result = V5NotificationFilter.shouldShow(payload, preferences)

        assertTrue(result.shouldShow())
    }

    // MARK: - Convenience Method Tests

    @Test
    fun testShouldShowNotification_boolean_returnsCorrectValue() {
        val payload = createPayload()
        val allowPrefs = createPreferences(enableNotifications = true)
        val blockPrefs = createPreferences(enableNotifications = false)

        assertTrue(V5NotificationFilter.shouldShowNotification(payload, allowPrefs))
        assertFalse(V5NotificationFilter.shouldShowNotification(payload, blockPrefs))
    }

    // MARK: - FilterResult Tests

    @Test
    fun testFilterResult_allowed_shouldShowTrue() {
        val result = FilterResult.Allowed

        assertTrue(result.shouldShow())
        assertNull(result.getBlockReason())
    }

    @Test
    fun testFilterResult_blocked_shouldShowFalse() {
        val result = FilterResult.Blocked("Test reason")

        assertFalse(result.shouldShow())
        assertEquals("Test reason", result.getBlockReason())
    }

    @Test
    fun testFilterResult_factoryMethods() {
        val allowed = FilterResult.allowed()
        val blocked = FilterResult.blocked("Reason")

        assertTrue(allowed is FilterResult.Allowed)
        assertTrue(blocked is FilterResult.Blocked)
        assertEquals("Reason", (blocked as FilterResult.Blocked).reason)
    }
}
