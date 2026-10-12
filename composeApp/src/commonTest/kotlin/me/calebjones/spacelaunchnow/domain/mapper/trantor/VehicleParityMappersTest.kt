package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigFull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** E14(P5): configuration detail gains GEO/SSO capacity and the consecutive/landing counters. */
class VehicleParityMappersTest {

    @Test
    fun capacitiesAndCountersAreMapped() {
        val domain = LauncherConfigFull(
            id = 1,
            name = "Falcon 9",
            geoCapacity = 5500.0,
            ssoCapacity = 15000.0,
            consecutiveSuccessfulLaunches = 12,
            consecutiveSuccessfulLandings = 7,
            attemptedLandings = 20
        ).toVehicleDomain()
        assertEquals(5500.0, domain.geoCapacity)
        assertEquals(15000.0, domain.ssoCapacity)
        assertEquals(12, domain.consecutiveSuccessfulLaunches)
        assertEquals(7, domain.consecutiveSuccessfulLandings)
        assertEquals(20, domain.attemptedLandings)
    }

    @Test
    fun nullCapacitiesStayNull() {
        val domain = LauncherConfigFull(
            id = 1,
            name = "Falcon 9",
            consecutiveSuccessfulLaunches = null,
            attemptedLandings = null
        ).toVehicleDomain()
        assertNull(domain.geoCapacity)
        assertNull(domain.ssoCapacity)
        assertNull(domain.consecutiveSuccessfulLaunches)
        assertNull(domain.attemptedLandings)
    }
}
