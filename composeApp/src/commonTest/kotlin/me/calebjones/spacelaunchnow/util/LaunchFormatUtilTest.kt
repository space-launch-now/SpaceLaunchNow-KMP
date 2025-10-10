package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.api.launchlibrary.models.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for LaunchFormatUtil
 * 
 * Tests cover:
 * - Standard format with rocket configuration: "<LSP> | <Rocket>"
 * - LSP abbreviation logic for long names (>15 chars)
 * - Fallback to launch name when rocket is unavailable
 * - Fallback to "Unknown Name" when all values are null/empty
 * - All method overloads (manual parameters, LaunchDetailed, LaunchNormal, LaunchBasic)
 * - Null and empty value handling
 * - Edge cases and boundary conditions
 */
class LaunchFormatUtilTest {
    
    // ========== Tests for formatLaunchTitle with manual parameters ==========
    
    /**
     * Test: Standard formatting with rocket configuration
     * Given: Valid LSP name and rocket configuration name
     * When: formatLaunchTitle is called
     * Then: Returns "<LSP> | <RocketConfig>" format
     */
    @Test
    fun testFormatLaunchTitle_WithRocketConfiguration_ReturnsStandardFormat() {
        // Arrange
        val lspName = "SpaceX"
        val lspAbbrev = "SpX"
        val rocketName = "Falcon 9 Block 5"
        val launchName = "Starlink 1-23"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = launchName
        )
        
        // Assert
        assertEquals("SpaceX | Falcon 9 Block 5", result)
    }
    
    /**
     * Test: LSP name is used when less than or equal to 15 characters
     * Given: LSP name with 15 characters and abbreviation available
     * When: formatLaunchTitle is called
     * Then: Uses full LSP name, not abbreviation
     */
    @Test
    fun testFormatLaunchTitle_WithShortLspName_UsesFullName() {
        // Arrange
        val lspName = "NASA"  // 4 chars, well under 15
        val lspAbbrev = "N"
        val rocketName = "SLS Block 1"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = "Artemis I"
        )
        
        // Assert
        assertEquals("NASA | SLS Block 1", result)
    }
    
    /**
     * Test: LSP abbreviation is used when name exceeds 15 characters
     * Given: LSP name with more than 15 characters and abbreviation available
     * When: formatLaunchTitle is called
     * Then: Uses abbreviation instead of full name
     */
    @Test
    fun testFormatLaunchTitle_WithLongLspName_UsesAbbreviation() {
        // Arrange
        val lspName = "Very Long Company Name Corporation"  // >15 chars
        val lspAbbrev = "VLCNC"
        val rocketName = "Rocket X"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = "Mission 1"
        )
        
        // Assert
        assertEquals("VLCNC | Rocket X", result)
    }
    
    /**
     * Test: Boundary case - exactly 15 characters uses full name
     * Given: LSP name with exactly 15 characters
     * When: formatLaunchTitle is called
     * Then: Uses full name (condition is > 15, not >= 15)
     */
    @Test
    fun testFormatLaunchTitle_WithExactly15CharLspName_UsesFullName() {
        // Arrange
        val lspName = "Fifteen-Char-Co"  // Exactly 15 chars
        val lspAbbrev = "FCC"
        val rocketName = "Atlas V"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = "Test Mission"
        )
        
        // Assert
        assertEquals("Fifteen-Char-Co | Atlas V", result)
    }
    
    /**
     * Test: Boundary case - 16 characters triggers abbreviation use
     * Given: LSP name with exactly 16 characters and abbreviation available
     * When: formatLaunchTitle is called
     * Then: Uses abbreviation
     */
    @Test
    fun testFormatLaunchTitle_With16CharLspName_UsesAbbreviation() {
        // Arrange
        val lspName = "Sixteen-Chars-Co"  // Exactly 16 chars
        val lspAbbrev = "SCC"
        val rocketName = "Delta IV Heavy"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = "Test"
        )
        
        // Assert
        assertEquals("SCC | Delta IV Heavy", result)
    }
    
    /**
     * Test: Long LSP name without abbreviation uses full name
     * Given: LSP name >15 characters but null abbreviation
     * When: formatLaunchTitle is called
     * Then: Uses full name since abbreviation not available
     */
    @Test
    fun testFormatLaunchTitle_WithLongLspNameNoAbbrev_UsesFullName() {
        // Arrange
        val lspName = "Really Long Company Name Without Abbreviation"
        val lspAbbrev = null
        val rocketName = "Proton-M"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = "Mission"
        )
        
        // Assert
        assertEquals("Really Long Company Name Without Abbreviation | Proton-M", result)
    }
    
    /**
     * Test: Empty abbreviation string is treated as null
     * Given: LSP name >15 characters with empty string abbreviation
     * When: formatLaunchTitle is called
     * Then: Uses full name since empty string is considered invalid
     */
    @Test
    fun testFormatLaunchTitle_WithEmptyAbbreviation_UsesFullName() {
        // Arrange
        val lspName = "Another Very Long Company Name Here"
        val lspAbbrev = ""
        val rocketName = "Soyuz 2.1a"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = "Progress MS-21"
        )
        
        // Assert
        assertEquals("Another Very Long Company Name Here | Soyuz 2.1a", result)
    }
    
    /**
     * Test: Fallback to launch name when rocket is null
     * Given: Null rocket configuration name but valid launch name
     * When: formatLaunchTitle is called
     * Then: Returns launch name as fallback
     */
    @Test
    fun testFormatLaunchTitle_WithNullRocket_ReturnsLaunchName() {
        // Arrange
        val lspName = "NASA"
        val lspAbbrev = null
        val rocketName = null
        val launchName = "Apollo 11"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = lspAbbrev,
            rocketConfigurationName = rocketName,
            launchName = launchName
        )
        
        // Assert
        assertEquals("Apollo 11", result)
    }
    
    /**
     * Test: Fallback to launch name when rocket is empty string
     * Given: Empty string rocket configuration name but valid launch name
     * When: formatLaunchTitle is called
     * Then: Should still try to use rocket (empty) with LSP format (edge case)
     */
    @Test
    fun testFormatLaunchTitle_WithEmptyRocket_UsesEmptyRocketFormat() {
        // Arrange
        val lspName = "ULA"
        val rocketName = ""  // Empty string, not null
        val launchName = "NROL-107"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = null,
            rocketConfigurationName = rocketName,
            launchName = launchName
        )
        
        // Assert
        // Empty string is not null, so format still applies: "ULA | "
        assertEquals("ULA | ", result)
    }
    
    /**
     * Test: Fallback to "Unknown Name" when all optional values are null
     * Given: Null rocket name and null launch name
     * When: formatLaunchTitle is called
     * Then: Returns "Unknown Name" as final fallback
     */
    @Test
    fun testFormatLaunchTitle_WithAllNulls_ReturnsUnknownName() {
        // Arrange
        val lspName = "Rocket Lab"
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = null,
            rocketConfigurationName = null,
            launchName = null
        )
        
        // Assert
        assertEquals("Unknown Name", result)
    }
    
    /**
     * Test: Empty launch name is treated as null for fallback
     * Given: Null rocket and empty string launch name
     * When: formatLaunchTitle is called
     * Then: Returns "Unknown Name" (empty string treated as invalid)
     */
    @Test
    fun testFormatLaunchTitle_WithEmptyLaunchName_ReturnsUnknownName() {
        // Arrange
        val lspName = "ISRO"
        val launchName = ""
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = lspName,
            launchServiceProviderAbbrev = null,
            rocketConfigurationName = null,
            launchName = launchName
        )
        
        // Assert
        assertEquals("Unknown Name", result)
    }
    
    // ========== Tests for LaunchDetailed overload ==========
    
    /**
     * Test: LaunchDetailed with complete data
     * Given: LaunchDetailed with all required fields
     * When: formatLaunchTitle is called
     * Then: Returns properly formatted title
     */
    @Test
    fun testFormatLaunchTitle_LaunchDetailed_WithCompleteData() {
        // Arrange
        val launch = createMockLaunchDetailed(
            lspName = "SpaceX",
            lspAbbrev = "SpX",
            rocketConfigName = "Falcon 9 Block 5",
            launchName = "Starlink 4-1"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("SpaceX | Falcon 9 Block 5", result)
    }
    
    /**
     * Test: LaunchDetailed with null rocket
     * Given: LaunchDetailed with null rocket
     * When: formatLaunchTitle is called
     * Then: Falls back to launch name
     */
    @Test
    fun testFormatLaunchTitle_LaunchDetailed_WithNullRocket() {
        // Arrange
        val launch = createMockLaunchDetailed(
            lspName = "Blue Origin",
            lspAbbrev = "BO",
            rocketConfigName = null,
            launchName = "NS-23"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("NS-23", result)
    }
    
    /**
     * Test: LaunchDetailed with long LSP name uses abbreviation
     * Given: LaunchDetailed with LSP name >15 chars
     * When: formatLaunchTitle is called
     * Then: Uses abbreviation
     */
    @Test
    fun testFormatLaunchTitle_LaunchDetailed_WithLongLspName() {
        // Arrange
        val launch = createMockLaunchDetailed(
            lspName = "China Aerospace Science and Technology Corporation",
            lspAbbrev = "CASC",
            rocketConfigName = "Long March 5B",
            launchName = "Tianhe Core Module"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("CASC | Long March 5B", result)
    }
    
    // ========== Tests for LaunchNormal overload ==========
    
    /**
     * Test: LaunchNormal with complete data
     * Given: LaunchNormal with all required fields
     * When: formatLaunchTitle is called
     * Then: Returns properly formatted title
     */
    @Test
    fun testFormatLaunchTitle_LaunchNormal_WithCompleteData() {
        // Arrange
        val launch = createMockLaunchNormal(
            lspName = "Arianespace",
            lspAbbrev = "ASpace",
            rocketConfigName = "Ariane 5 ECA",
            launchName = "VA256"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("Arianespace | Ariane 5 ECA", result)
    }
    
    /**
     * Test: LaunchNormal with null rocket configuration
     * Given: LaunchNormal with null rocket.configuration
     * When: formatLaunchTitle is called
     * Then: Falls back to launch name
     */
    @Test
    fun testFormatLaunchTitle_LaunchNormal_WithNullRocketConfig() {
        // Arrange
        val launch = createMockLaunchNormal(
            lspName = "Virgin Orbit",
            lspAbbrev = "VO",
            rocketConfigName = null,
            launchName = "Tubular Bells Part 1"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("Tubular Bells Part 1", result)
    }
    
    // ========== Tests for LaunchBasic overload ==========
    
    /**
     * Test: LaunchBasic with pipe-separated name
     * Given: LaunchBasic with name in format "LSP | Rocket"
     * When: formatLaunchTitle is called
     * Then: Extracts rocket name from after the pipe
     */
    @Test
    fun testFormatLaunchTitle_LaunchBasic_WithPipeSeparatedName() {
        // Arrange
        val launch = createMockLaunchBasic(
            lspName = "SpaceX",
            lspAbbrev = "SpX",
            launchName = "SpaceX Falcon 9 Block 5 | Starlink Group 6-28"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        // Should extract " Starlink Group 6-28" (with leading space)
        assertEquals("SpaceX |  Starlink Group 6-28", result)
    }
    
    /**
     * Test: LaunchBasic with simple name (no pipe)
     * Given: LaunchBasic with simple name without pipe separator
     * When: formatLaunchTitle is called
     * Then: Uses the whole name as fallback
     */
    @Test
    fun testFormatLaunchTitle_LaunchBasic_WithSimpleName() {
        // Arrange
        val launch = createMockLaunchBasic(
            lspName = "JAXA",
            lspAbbrev = null,
            launchName = "HTV-9"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("HTV-9", result)
    }
    
    /**
     * Test: LaunchBasic with null name
     * Given: LaunchBasic with null name
     * When: formatLaunchTitle is called
     * Then: Returns "Unknown Name"
     */
    @Test
    fun testFormatLaunchTitle_LaunchBasic_WithNullName() {
        // Arrange
        val launch = createMockLaunchBasic(
            lspName = "ESA",
            lspAbbrev = null,
            launchName = null
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("Unknown Name", result)
    }
    
    /**
     * Test: LaunchBasic with empty name
     * Given: LaunchBasic with empty string name
     * When: formatLaunchTitle is called
     * Then: Returns "Unknown Name"
     */
    @Test
    fun testFormatLaunchTitle_LaunchBasic_WithEmptyName() {
        // Arrange
        val launch = createMockLaunchBasic(
            lspName = "CNSA",
            lspAbbrev = null,
            launchName = ""
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("Unknown Name", result)
    }
    
    /**
     * Test: LaunchBasic with multiple pipes in name
     * Given: LaunchBasic with name containing multiple pipe characters
     * When: formatLaunchTitle is called
     * Then: Takes everything after the last pipe
     */
    @Test
    fun testFormatLaunchTitle_LaunchBasic_WithMultiplePipes() {
        // Arrange
        val launch = createMockLaunchBasic(
            lspName = "ULA",
            lspAbbrev = null,
            launchName = "ULA | Atlas V 551 | NROL-107 | Classified"
        )
        
        // Act
        val result = LaunchFormatUtil.formatLaunchTitle(launch)
        
        // Assert
        assertEquals("ULA |  Classified", result)
    }
    
    // ========== Helper methods for creating mock objects ==========
    
    private fun createMockLaunchDetailed(
        lspName: String,
        lspAbbrev: String?,
        rocketConfigName: String?,
        launchName: String
    ): LaunchDetailed {
        val lsp = AgencyDetailed(
            responseMode = "detailed",
            id = 1,
            url = "https://test.com",
            name = lspName,
            featured = false,
            type = null,
            countryCode = "USA",
            abbrev = lspAbbrev,
            description = null,
            administrator = null,
            foundingYear = null,
            launchers = null,
            spacecraftCount = null,
            launcherCount = null,
            totalLaunchCount = null,
            consecutiveSuccessfulLaunches = null,
            successfulLaunches = null,
            failedLaunches = null,
            pendingLaunches = null,
            consecutiveSuccessfulLandings = null,
            successfulLandings = null,
            failedLandings = null,
            attemptedLandings = null,
            infoUrl = null,
            wikiUrl = null,
            logoUrl = null,
            imageUrl = null,
            nationUrl = null
        )
        
        val rocketConfig = rocketConfigName?.let {
            LauncherConfigDetailedSerializerNoManufacturer(
                responseMode = "detailed",
                id = 1,
                url = "https://test.com",
                name = it,
                families = null,
                fullName = it,
                variant = null,
                alias = null,
                minStage = null,
                maxStage = null,
                length = null,
                diameter = null,
                maidenFlight = null,
                launchCost = null,
                launchMass = null,
                leoCapacity = null,
                gtoCapacity = null,
                toThrust = null,
                apogee = null,
                vehicleRange = null,
                imageUrl = null,
                infoUrl = null,
                wikiUrl = null,
                totalLaunchCount = null,
                consecutiveSuccessfulLaunches = null,
                successfulLaunches = null,
                failedLaunches = null,
                pendingLaunches = null,
                attemptLandingCount = null,
                successfulLandingCount = null,
                failedLandingCount = null,
                consecutiveSuccessfulLandings = null,
                description = null,
                program = null
            )
        }
        
        val rocket = rocketConfig?.let {
            RocketDetailed(
                responseMode = "detailed",
                id = 1,
                configuration = it,
                spacecraftStage = null
            )
        }
        
        return LaunchDetailed(
            id = "test-id",
            url = "https://test.com",
            responseMode = "detailed",
            slug = "test-slug",
            launchDesignator = null,
            status = null,
            netPrecision = null,
            image = null,
            launchServiceProvider = lsp,
            infographic = null,
            name = launchName,
            net = "2024-01-01T00:00:00Z",
            windowEnd = "2024-01-01T01:00:00Z",
            windowStart = "2024-01-01T00:00:00Z",
            failReason = null,
            hashtag = null,
            holdreason = null,
            lastUpdated = "2024-01-01T00:00:00Z",
            webcastLive = false,
            location = null,
            mission = null,
            pad = null,
            probability = null,
            program = null,
            rocket = rocket,
            updates = null,
            videos = null
        )
    }
    
    private fun createMockLaunchNormal(
        lspName: String,
        lspAbbrev: String?,
        rocketConfigName: String?,
        launchName: String
    ): LaunchNormal {
        val lsp = AgencyNormal(
            responseMode = "normal",
            id = 1,
            url = "https://test.com",
            name = lspName,
            featured = false,
            type = null,
            countryCode = "USA",
            abbrev = lspAbbrev,
            description = null,
            administrator = null,
            foundingYear = null,
            launchers = null,
            spacecraftCount = null,
            launcherCount = null,
            totalLaunchCount = null,
            consecutiveSuccessfulLaunches = null,
            successfulLaunches = null,
            failedLaunches = null,
            pendingLaunches = null,
            consecutiveSuccessfulLandings = null,
            successfulLandings = null,
            failedLandings = null,
            attemptedLandings = null,
            infoUrl = null,
            wikiUrl = null,
            logoUrl = null,
            imageUrl = null,
            nationUrl = null
        )
        
        val rocketConfig = rocketConfigName?.let {
            LauncherConfigNormal(
                responseMode = "normal",
                id = 1,
                url = "https://test.com",
                name = it,
                families = null,
                fullName = it,
                variant = null,
                alias = null
            )
        }
        
        val rocket = rocketConfig?.let {
            RocketNormal(
                responseMode = "normal",
                id = 1,
                configuration = it,
                spacecraftStage = null
            )
        }
        
        return LaunchNormal(
            id = "test-id",
            url = "https://test.com",
            responseMode = "normal",
            slug = "test-slug",
            launchDesignator = null,
            status = null,
            netPrecision = null,
            image = null,
            launchServiceProvider = lsp,
            infographic = null,
            name = launchName,
            net = "2024-01-01T00:00:00Z",
            windowEnd = "2024-01-01T01:00:00Z",
            windowStart = "2024-01-01T00:00:00Z",
            failReason = null,
            hashtag = null,
            holdreason = null,
            lastUpdated = "2024-01-01T00:00:00Z",
            webcastLive = false,
            location = null,
            mission = null,
            pad = null,
            probability = null,
            program = null,
            rocket = rocket,
            updates = null,
            videos = null
        )
    }
    
    private fun createMockLaunchBasic(
        lspName: String,
        lspAbbrev: String?,
        launchName: String?
    ): LaunchBasic {
        val lsp = AgencyMini(
            responseMode = "list",
            id = 1,
            url = "https://test.com",
            name = lspName,
            type = null,
            abbrev = lspAbbrev
        )
        
        return LaunchBasic(
            id = "test-id",
            url = "https://test.com",
            responseMode = "list",
            slug = "test-slug",
            launchDesignator = null,
            status = null,
            netPrecision = null,
            image = null,
            launchServiceProvider = lsp,
            infographic = null,
            name = launchName,
            net = "2024-01-01T00:00:00Z",
            windowEnd = "2024-01-01T01:00:00Z",
            windowStart = "2024-01-01T00:00:00Z",
            failReason = null,
            hashtag = null,
            holdreason = null,
            lastUpdated = "2024-01-01T00:00:00Z",
            webcastLive = false,
            location = null,
            mission = null,
            pad = null,
            probability = null,
            program = null,
            rocket = null
        )
    }
}
