package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * E10(P5): `/configurations` joins `manufacturer_name` (and `manufacturer_abbrev`, unused - no
 * UI reads a manufacturer abbrev) beside `manufacturer_id` at read time.
 */
class VehicleMappersTest {

    @Test
    fun manufacturerNameIsMappedWhenPresent() {
        val api = LauncherConfigSummary(
            id = 1,
            name = "Falcon 9",
            manufacturerId = 44,
            manufacturerName = "SpaceX",
            manufacturerAbbrev = "SpX"
        )
        val domain = api.toVehicleDomain()
        assertEquals("SpaceX", domain.manufacturerName)
    }

    @Test
    fun manufacturerNameIsNullWhenAbsent() {
        val api = LauncherConfigSummary(id = 1, name = "Falcon 9")
        val domain = api.toVehicleDomain()
        assertNull(domain.manufacturerName)
    }
}
