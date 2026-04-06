package me.calebjones.spacelaunchnow.analytics.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager

/**
 * Observes the NavController back-stack and fires a screen-view event on every destination
 * change. Mount this composable inside the same composition as [NavHost] — it does not emit
 * any visible UI.
 */
@Composable
fun AnalyticsScreenTracker(
    navController: NavHostController,
    analyticsManager: AnalyticsManager
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    LaunchedEffect(route) {
        if (route != null) {
            val screenName = RouteScreenMapper.mapRouteToScreenName(route)
            analyticsManager.trackScreenView(screenName = screenName, screenClass = route)
        }
    }
}
