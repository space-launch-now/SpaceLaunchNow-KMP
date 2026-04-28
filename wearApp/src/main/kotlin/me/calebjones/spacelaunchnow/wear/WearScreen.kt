package me.calebjones.spacelaunchnow.wear

sealed class WearScreen(val route: String) {
    data object LaunchList : WearScreen("launch_list")
    data class LaunchDetail(val launchId: String) : WearScreen("launch_detail/{launchId}") {
        companion object {
            const val ROUTE_PATTERN = "launch_detail/{launchId}"
            const val ARG_LAUNCH_ID = "launchId"
        }
    }
    data object PremiumGate : WearScreen("premium_gate")
    data object Settings : WearScreen("settings")
}
