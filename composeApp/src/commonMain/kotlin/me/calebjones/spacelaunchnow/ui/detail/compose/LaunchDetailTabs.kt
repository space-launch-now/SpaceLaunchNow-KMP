package me.calebjones.spacelaunchnow.ui.detail.compose

import org.jetbrains.compose.resources.StringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_tab_agency
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_tab_mission
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_tab_overview
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_tab_rocket

/**
 * Sealed class representing the different tabs available in the launch detail view
 * for phone/small screen layouts.
 */
sealed class LaunchDetailTab(val titleRes: StringResource) {
    data object Overview : LaunchDetailTab(Res.string.launch_detail_tab_overview)
    data object Mission : LaunchDetailTab(Res.string.launch_detail_tab_mission)
    data object Agency : LaunchDetailTab(Res.string.launch_detail_tab_agency)
    data object Rocket : LaunchDetailTab(Res.string.launch_detail_tab_rocket)

    companion object {
        /**
         * Get all tabs in the order they should be displayed
         */
        fun values(): List<LaunchDetailTab> = listOf(
            Overview,
            Mission,
            Agency,
            Rocket
        )

        /**
         * Get tab by index
         */
        fun fromIndex(index: Int): LaunchDetailTab = values()[index]
    }
}

