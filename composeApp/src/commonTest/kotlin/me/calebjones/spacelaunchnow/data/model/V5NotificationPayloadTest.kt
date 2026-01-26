package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for V5 notification payload parsing
 */
class V5NotificationPayloadTest {

    // MARK: - Test Data
    
    private val validV5Payload = mapOf(
        "notification_type" to "tenMinutes",
        "title" to "Launch in 10 minutes!",
        "body" to "SpaceX Falcon 9 launching Starlink satellites",
        "launch_uuid" to "550e8400-e29b-41d4-a716-446655440000",
        "launch_id" to "12345",
        "launch_name" to "Falcon 9 Block 5 | Starlink Group 6-22",
        "launch_image" to "https://example.com/image.jpg",
        "launch_net" to "2025-01-26T12:00:00Z",
        "launch_location" to "Kennedy Space Center, FL",
        "webcast" to "true",
        "webcast_live" to "false",
        "lsp_id" to "121",
        "location_id" to "27",
        "program_ids" to "1,2,3",
        "status_id" to "1",
        "orbit_id" to "8",
        "mission_type_id" to "10",
        "launcher_family_id" to "5"
    )

    private val validV4Payload = mapOf(
        "notification_type" to "tenMinutes",
        "launch_uuid" to "550e8400-e29b-41d4-a716-446655440000",
        "launch_id" to "12345",
        "launch_name" to "Falcon 9 Block 5 | Starlink Group 6-22",
        "launch_image" to "https://example.com/image.jpg",
        "launch_net" to "2025-01-26T12:00:00Z",
        "launch_location" to "Kennedy Space Center, FL",
        "webcast" to "true",
        "webcast_live" to "false",
        "agency_id" to "121",
        "location_id" to "27"
    )

    // MARK: - V5 Detection Tests
    
    @Test
    fun testIsV5Payload_withLspId_returnsTrue() {
        val payload = mapOf("lsp_id" to "121", "notification_type" to "test")
        assertTrue(V5NotificationPayload.isV5Payload(payload))
    }

    @Test
    fun testIsV5Payload_withoutLspId_returnsFalse() {
        val payload = mapOf("agency_id" to "121", "notification_type" to "test")
        assertFalse(V5NotificationPayload.isV5Payload(payload))
    }

    @Test
    fun testIsV5Payload_emptyPayload_returnsFalse() {
        assertFalse(V5NotificationPayload.isV5Payload(emptyMap()))
    }

    // MARK: - V5 Parsing Tests
    
    @Test
    fun testFromMap_validV5Payload_parsesSuccessfully() {
        val result = V5NotificationPayload.fromMap(validV5Payload)
        
        assertNotNull(result)
        assertEquals("tenMinutes", result.notificationType)
        assertEquals("Launch in 10 minutes!", result.title)
        assertEquals("SpaceX Falcon 9 launching Starlink satellites", result.body)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", result.launchUuid)
        assertEquals("12345", result.launchId)
        assertEquals("Falcon 9 Block 5 | Starlink Group 6-22", result.launchName)
        assertEquals("https://example.com/image.jpg", result.launchImage)
        assertEquals("2025-01-26T12:00:00Z", result.launchNet)
        assertEquals("Kennedy Space Center, FL", result.launchLocation)
        assertTrue(result.webcast)
        assertFalse(result.webcastLive)
        assertEquals(121, result.lspId)
        assertEquals(27, result.locationId)
        assertEquals(listOf(1, 2, 3), result.programIds)
        assertEquals(1, result.statusId)
        assertEquals(8, result.orbitId)
        assertEquals(10, result.missionTypeId)
        assertEquals(5, result.launcherFamilyId)
    }

    @Test
    fun testFromMap_missingNotificationType_returnsNull() {
        val payload = validV5Payload.toMutableMap().apply { remove("notification_type") }
        assertNull(V5NotificationPayload.fromMap(payload))
    }

    @Test
    fun testFromMap_missingLaunchUuid_returnsNull() {
        val payload = validV5Payload.toMutableMap().apply { remove("launch_uuid") }
        assertNull(V5NotificationPayload.fromMap(payload))
    }

    @Test
    fun testFromMap_missingLaunchName_returnsNull() {
        val payload = validV5Payload.toMutableMap().apply { remove("launch_name") }
        assertNull(V5NotificationPayload.fromMap(payload))
    }

    @Test
    fun testFromMap_missingLaunchNet_returnsNull() {
        val payload = validV5Payload.toMutableMap().apply { remove("launch_net") }
        assertNull(V5NotificationPayload.fromMap(payload))
    }

    @Test
    fun testFromMap_titleFallsBackToLaunchName() {
        val payload = validV5Payload.toMutableMap().apply { remove("title") }
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertEquals("Falcon 9 Block 5 | Starlink Group 6-22", result.title)
    }

    @Test
    fun testFromMap_emptyBody_parsesSuccessfully() {
        val payload = validV5Payload.toMutableMap().apply { remove("body") }
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertEquals("", result.body)
    }

    @Test
    fun testFromMap_missingOptionalFilterIds_parsesWithNulls() {
        val payload = mapOf(
            "notification_type" to "tenMinutes",
            "title" to "Test",
            "launch_uuid" to "550e8400-e29b-41d4-a716-446655440000",
            "launch_name" to "Test Launch",
            "launch_net" to "2025-01-26T12:00:00Z",
            "lsp_id" to "121"
        )
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertEquals(121, result.lspId)
        assertNull(result.locationId)
        assertTrue(result.programIds.isEmpty())
        assertNull(result.statusId)
        assertNull(result.orbitId)
        assertNull(result.missionTypeId)
        assertNull(result.launcherFamilyId)
    }

    @Test
    fun testFromMap_programIds_parsesSingleId() {
        val payload = validV5Payload.toMutableMap().apply {
            this["program_ids"] = "42"
        }
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertEquals(listOf(42), result.programIds)
    }

    @Test
    fun testFromMap_programIds_handlesSpaces() {
        val payload = validV5Payload.toMutableMap().apply {
            this["program_ids"] = "1, 2, 3"
        }
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertEquals(listOf(1, 2, 3), result.programIds)
    }

    @Test
    fun testFromMap_programIds_ignoresInvalidEntries() {
        val payload = validV5Payload.toMutableMap().apply {
            this["program_ids"] = "1,invalid,3"
        }
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertEquals(listOf(1, 3), result.programIds)
    }

    @Test
    fun testFromMap_webcast_caseInsensitive() {
        val payloadUpper = validV5Payload.toMutableMap().apply {
            this["webcast"] = "TRUE"
        }
        val resultUpper = V5NotificationPayload.fromMap(payloadUpper)
        assertNotNull(resultUpper)
        assertTrue(resultUpper.webcast)

        val payloadMixed = validV5Payload.toMutableMap().apply {
            this["webcast"] = "True"
        }
        val resultMixed = V5NotificationPayload.fromMap(payloadMixed)
        assertNotNull(resultMixed)
        assertTrue(resultMixed.webcast)
    }

    @Test
    fun testFromMap_webcast_defaultsFalse() {
        val payload = validV5Payload.toMutableMap().apply { remove("webcast") }
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertFalse(result.webcast)
    }

    // MARK: - Helper Method Tests
    
    @Test
    fun testHasFilteringIds_withLspId_returnsTrue() {
        val result = V5NotificationPayload.fromMap(validV5Payload)
        assertNotNull(result)
        assertTrue(result.hasFilteringIds())
    }

    @Test
    fun testHasFilteringIds_noFilterIds_returnsFalse() {
        val payload = mapOf(
            "notification_type" to "tenMinutes",
            "title" to "Test",
            "launch_uuid" to "550e8400-e29b-41d4-a716-446655440000",
            "launch_name" to "Test Launch",
            "launch_net" to "2025-01-26T12:00:00Z"
        )
        val result = V5NotificationPayload.fromMap(payload)
        
        assertNotNull(result)
        assertFalse(result.hasFilteringIds())
    }

    @Test
    fun testToDebugString_containsEssentialInfo() {
        val result = V5NotificationPayload.fromMap(validV5Payload)
        assertNotNull(result)
        
        val debugString = result.toDebugString()
        assertTrue(debugString.contains("tenMinutes"))
        assertTrue(debugString.contains("Falcon 9"))
        assertTrue(debugString.contains("lspId=121"))
    }

    // MARK: - NotificationData Compatibility Tests

    @Test
    fun testNotificationData_isV5Payload_delegatesToV5Payload() {
        assertTrue(NotificationData.isV5Payload(validV5Payload))
        assertFalse(NotificationData.isV5Payload(validV4Payload))
    }

    @Test
    fun testNotificationData_parseAny_parsesV5() {
        val result = NotificationData.parseAny(validV5Payload)
        
        assertNotNull(result)
        assertTrue(result is ParsedNotification.V5)
        assertEquals("tenMinutes", result.notificationType)
        assertEquals("Falcon 9 Block 5 | Starlink Group 6-22", result.launchName)
    }

    @Test
    fun testNotificationData_parseAny_parsesV4() {
        val result = NotificationData.parseAny(validV4Payload)
        
        assertNotNull(result)
        assertTrue(result is ParsedNotification.V4)
        assertEquals("tenMinutes", result.notificationType)
        assertEquals("Falcon 9 Block 5 | Starlink Group 6-22", result.launchName)
    }

    @Test
    fun testParsedNotification_asV5_returnsPayloadForV5() {
        val result = NotificationData.parseAny(validV5Payload)
        
        assertNotNull(result)
        assertTrue(result.isV5())
        assertNotNull(result.asV5())
        assertNull(result.asV4())
    }

    @Test
    fun testParsedNotification_asV4_returnsDataForV4() {
        val result = NotificationData.parseAny(validV4Payload)
        
        assertNotNull(result)
        assertFalse(result.isV5())
        assertNull(result.asV5())
        assertNotNull(result.asV4())
    }
}
