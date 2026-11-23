package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.schedule.ScheduleScreen
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleScreenLayoutTest {
    @Test
    fun scheduleLayout() = ScreenLayoutHarness.assertLayout(
        "schedule",
        koinModules = { listOf(ScreenKoin.common, ScreenKoin.schedule) },
        // The list loads through the view model; wait for the first launch card (names are data, not text).
        settle = { waitUntil(timeoutMillis = 15_000) { onAllNodes(hasText("Crew-8", substring = true)).fetchSemanticsNodes().isNotEmpty() } },
    ) {
        ScheduleScreen(onLaunchClick = {})
    }
}
