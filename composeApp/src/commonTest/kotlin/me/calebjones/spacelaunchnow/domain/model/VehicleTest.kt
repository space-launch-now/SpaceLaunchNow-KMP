package me.calebjones.spacelaunchnow.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.domain.model.LauncherStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VehicleTest {

    @Test
    fun minimalVehicleConfig() {
        val config = VehicleConfig(
            id = 1,
            name = "Falcon 9",
            fullName = "Falcon 9 Block 5",
            family = "Falcon",
            variant = "Block 5",
            imageUrl = null
        )
        assertEquals("Falcon 9", config.name)
        assertNull(config.description)
        assertNull(config.maidenFlight)
        assertNull(config.active)
    }

    @Test
    fun fullyPopulatedVehicleConfig() {
        val config = VehicleConfig(
            id = 164,
            name = "Falcon 9",
            fullName = "Falcon 9 Block 5",
            family = "Falcon",
            variant = "Block 5",
            imageUrl = "https://example.com/falcon9.png",
            description = "Reusable two-stage orbital launch vehicle",
            infoUrl = "https://spacex.com/falcon9",
            wikiUrl = "https://en.wikipedia.org/wiki/Falcon_9",
            length = 70.0,
            diameter = 3.7,
            launchMass = 549.0,
            leoCapacity = 22800.0,
            gtoCapacity = 8300.0,
            toThrust = 7607.0,
            apogee = null,
            totalLaunchCount = 300,
            consecutiveSuccessfulLaunches = 250,
            maidenFlight = LocalDate(2018, 5, 11),
            active = true,
            reusable = true
        )
        assertEquals(70.0, config.length)
        assertEquals(true, config.reusable)
        assertEquals(300, config.totalLaunchCount)
    }

    @Test
    fun minimalLauncherDetail() {
        val launcher = LauncherDetail(
            id = 1,
            serialNumber = "B1049",
            flightProven = true,
            imageUrl = null
        )
        assertEquals("B1049", launcher.serialNumber)
        assertTrue(launcher.flightProven)
        assertNull(launcher.flights)
        assertNull(launcher.lastLaunchDate)
    }

    @Test
    fun fullyPopulatedLauncherDetail() {
        val launcher = LauncherDetail(
            id = 1,
            serialNumber = "B1058",
            flightProven = true,
            imageUrl = "https://example.com/b1058.png",
            flights = 12,
            lastLaunchDate = Instant.parse("2023-01-15T00:00:00Z"),
            firstLaunchDate = Instant.parse("2020-05-30T00:00:00Z"),
            status = LauncherStatus(id = 1, name = "active"),
            details = "Demo-2 booster"
        )
        assertEquals(12, launcher.flights)
        assertEquals("active", launcher.status?.name)
    }
}
