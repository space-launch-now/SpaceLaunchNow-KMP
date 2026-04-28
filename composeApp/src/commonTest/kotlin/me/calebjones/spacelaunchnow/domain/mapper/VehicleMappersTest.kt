package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class VehicleMappersTest {

    @Test
    fun launcherConfigListToVehicleDomainMapsCoreFields() {
        val domain = testLauncherConfigList().toVehicleDomain()
        assertEquals(1, domain.id)
        assertEquals("Falcon 9 Block 5", domain.name)
        assertEquals("Falcon", domain.family)
        assertEquals("Block 5", domain.variant)
        assertEquals(null, domain.imageUrl)
    }

    @Test
    fun launcherConfigListToVehicleDomainHandlesNoFamily() {
        val domain = testLauncherConfigList().copy(families = emptyList()).toVehicleDomain()
        assertEquals(null, domain.family)
    }

    @Test
    fun launcherDetailedToDomainMapsAllFields() {
        val api = LauncherDetailed(
            id = 500,
            url = "https://example.com/launcher/500/",
            serialNumber = "B1058",
            status = LauncherStatus(id = 1, name = "Active"),
            image = testImage(),
            details = "Reused booster",
            successfulLandings = 10,
            attemptedLandings = 10,
            flights = 15,
            lastLaunchDate = Instant.parse("2024-01-01T00:00:00Z"),
            firstLaunchDate = Instant.parse("2020-05-30T00:00:00Z"),
            fastestTurnaround = "30 days",
            launcherConfig = testLauncherConfigList(),
            flightProven = true,
            isPlaceholder = false
        )
        val domain = api.toDomain()
        assertEquals(500, domain.id)
        assertEquals("B1058", domain.serialNumber)
        assertTrue(domain.flightProven)
        assertEquals(15, domain.flights)
        assertEquals("Active", domain.status?.name)
        assertEquals("https://example.com/image.jpg", domain.imageUrl)
        assertNotNull(domain.lastLaunchDate)
    }

    @Test
    fun launcherDetailedFlightProvenDefaultsToFalseWhenNull() {
        val api = LauncherDetailed(
            id = 501,
            url = "https://example.com/launcher/501/",
            serialNumber = null,
            status = null,
            image = null,
            details = null,
            successfulLandings = null,
            attemptedLandings = null,
            flights = null,
            lastLaunchDate = null,
            firstLaunchDate = null,
            fastestTurnaround = null,
            launcherConfig = null,
            flightProven = null,
            isPlaceholder = null
        )
        val domain = api.toDomain()
        assertEquals(false, domain.flightProven)
        assertEquals(null, domain.status)
    }
}
