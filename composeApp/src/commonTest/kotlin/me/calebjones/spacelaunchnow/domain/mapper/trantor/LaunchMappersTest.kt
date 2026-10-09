package me.calebjones.spacelaunchnow.domain.mapper.trantor

import kotlin.time.Instant
import me.calebjones.spacelaunchnow.api.trantor.models.AgencySummary
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchDetail
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * E12(P5) mapper coverage: the launches list row and detail gain `net_precision`/
 * `net_precision_id` and `location_id`/`location_name` (D1), and `AgencySummary` gains
 * `social_logo_url` (D2). Mapping is verbatim (D5) - no cross-substitution between fields.
 */
class LaunchMappersTest {

    private fun testLaunchList(
        netPrecisionId: Int? = null,
        netPrecision: String? = null,
        locationId: Int? = null,
        locationName: String? = null,
        padId: Int? = 20
    ) = LaunchList(
        id = "row-1",
        name = "Falcon 9 | Starlink",
        net = Instant.parse("2026-01-01T00:00:00Z"),
        slug = "falcon-9-starlink",
        status = "Go",
        statusId = 1,
        padId = padId,
        padName = "SLC-40",
        locationId = locationId,
        locationName = locationName,
        netPrecision = netPrecision,
        netPrecisionId = netPrecisionId
    )

    @Test
    fun launchListLeavesNetPrecisionNullWhenIdIsNull() {
        val domain = testLaunchList(netPrecisionId = null, netPrecision = "Second").toDomain()
        assertNull(domain.netPrecision)
    }

    @Test
    fun launchListFillsNetPrecisionWhenIdIsPresent() {
        val domain = testLaunchList(netPrecisionId = 0, netPrecision = "Second").toDomain()
        assertEquals(0, domain.netPrecision?.id)
        assertEquals("Second", domain.netPrecision?.name)
        assertNull(domain.netPrecision?.abbrev)
        assertNull(domain.netPrecision?.description)
    }

    @Test
    fun launchListLeavesPadLocationNullWhenLocationIdIsNull() {
        val domain = testLaunchList(locationId = null, locationName = "CCSFS, FL, USA").toDomain()
        assertNull(domain.pad?.location)
    }

    @Test
    fun launchListFillsPadLocationWhenLocationIdIsPresent() {
        val domain = testLaunchList(locationId = 30, locationName = "CCSFS, FL, USA").toDomain()
        assertEquals(30, domain.pad?.location?.id)
        assertEquals("CCSFS, FL, USA", domain.pad?.location?.name)
        assertNull(domain.pad?.location?.countryCode)
    }

    private fun testLaunchDetail(
        netPrecisionId: Int? = null,
        netPrecision: String? = null
    ) = LaunchDetail(
        id = "row-1",
        name = "Falcon 9 | Starlink",
        net = Instant.parse("2026-01-01T00:00:00Z"),
        slug = "falcon-9-starlink",
        status = "Go",
        statusId = 1,
        netPrecision = netPrecision,
        netPrecisionId = netPrecisionId
    )

    @Test
    fun launchDetailLeavesNetPrecisionNullWhenIdIsNull() {
        val domain = testLaunchDetail(netPrecisionId = null, netPrecision = "Month").toDomain()
        assertNull(domain.netPrecision)
    }

    @Test
    fun launchDetailFillsNetPrecisionWhenIdIsPresent() {
        val domain = testLaunchDetail(netPrecisionId = 4, netPrecision = "Month").toDomain()
        assertEquals(4, domain.netPrecision?.id)
        assertEquals("Month", domain.netPrecision?.name)
    }

    private fun testAgencySummary(socialLogoUrl: String? = null) = AgencySummary(
        abbrev = "SpX",
        id = 44,
        name = "SpaceX",
        logoUrl = "https://example.com/logo.png",
        socialLogoUrl = socialLogoUrl
    )

    @Test
    fun agencySummaryLeavesSocialLogoNullWhenAbsent() {
        val domain = testAgencySummary(socialLogoUrl = null).toDomain()
        assertNull(domain.socialLogo)
        assertEquals("https://example.com/logo.png", domain.logoUrl)
    }

    @Test
    fun agencySummaryFillsSocialLogoWhenPresent() {
        val domain = testAgencySummary(socialLogoUrl = "https://example.com/social.png").toDomain()
        assertEquals("https://example.com/social.png", domain.socialLogo)
        assertEquals("https://example.com/logo.png", domain.logoUrl)
    }
}
