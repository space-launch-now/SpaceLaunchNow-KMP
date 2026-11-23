package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketDetailView
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RocketDetailScreenLayoutTest {
    @Test
    fun rocketDetailLayout() = ScreenLayoutHarness.assertLayout("rocket_detail", koinModules = { listOf(ScreenKoin.common) }) {
        RocketDetailView(rocket = SAMPLE_ROCKET, onNavigateBack = {})
    }

    private companion object {
        val SAMPLE_ROCKET = VehicleConfig(
            id = 1,
            name = "Falcon 9 Block 5",
            fullName = "Falcon 9 Block 5",
            family = "Falcon",
            variant = "Block 5",
            imageUrl = null,
            description = "A two-stage, partially reusable medium-lift launch vehicle.",
            manufacturerName = "Space Exploration Technologies Corp.",
            minStage = 1, maxStage = 2,
            length = 70.0, diameter = 3.7, launchMass = 549.0,
            leoCapacity = 22800.0, gtoCapacity = 8300.0, toThrust = 7607.0,
            launchCost = 67, totalLaunchCount = 400, successfulLaunches = 398,
            failedLaunches = 1, pendingLaunches = 20,
            attemptedLandings = 350, successfulLandings = 345,
            active = true, reusable = true,
        )
    }
}
