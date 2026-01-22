package me.calebjones.spacelaunchnow.ui.detail.compose

/**
 * Sealed class representing the different tabs available in the launch detail view
 * for phone/small screen layouts.
 */
sealed class LaunchDetailTab(val displayName: String) {
    data object Overview : LaunchDetailTab("Overview")
    data object Mission : LaunchDetailTab("Mission")
    data object Agency : LaunchDetailTab("Agency")
    data object Rocket : LaunchDetailTab("Rocket")

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

