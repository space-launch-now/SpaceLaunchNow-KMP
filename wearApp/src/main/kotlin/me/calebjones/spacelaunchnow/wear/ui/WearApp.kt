package me.calebjones.spacelaunchnow.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import me.calebjones.spacelaunchnow.wear.WearScreen
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.data.model.WearEntitlementState
import me.calebjones.spacelaunchnow.wear.ui.launch.LaunchDetailScreen
import me.calebjones.spacelaunchnow.wear.ui.launch.LaunchListScreen
import me.calebjones.spacelaunchnow.wear.ui.premium.PremiumGateScreen
import me.calebjones.spacelaunchnow.wear.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailViewModel
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchListViewModel
import co.touchlab.kermit.Logger
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

private val log = Logger.withTag("WearApp")

@Composable
fun WearApp(deepLinkLaunchId: String? = null) {
    val entitlementSyncManager = koinInject<EntitlementSyncManager>()
    val entitlement by entitlementSyncManager.entitlementState.collectAsState(
        initial = null
    )

    // Request entitlement sync from phone on startup
    LaunchedEffect(Unit) {
        entitlementSyncManager.requestSync()
    }

    // Wait for the first real emission from DataStore before deciding the start destination.
    // This prevents flashing PremiumGateScreen while the cached entitlement is still loading.
    val resolvedEntitlement = entitlement ?: return

    log.d { "Entitlement state: hasWearOs=${resolvedEntitlement.hasWearOs}" }

    val navController = rememberSwipeDismissableNavController()

    // Navigate reactively when entitlement changes (e.g. phone syncs while app is open)
    LaunchedEffect(resolvedEntitlement.hasWearOs) {
        val target = if (resolvedEntitlement.hasWearOs) {
            WearScreen.LaunchList.route
        } else {
            WearScreen.PremiumGate.route
        }
        log.i { "Navigating to $target (hasWearOs=${resolvedEntitlement.hasWearOs})" }
        navController.navigate(target) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }

        // If launched from a complication tap, deep-link straight to the launch detail
        if (resolvedEntitlement.hasWearOs && deepLinkLaunchId != null) {
            log.i { "Deep-linking to launch detail: $deepLinkLaunchId" }
            navController.navigate("launch_detail/$deepLinkLaunchId")
        }
    }

    val startDestination = if (resolvedEntitlement.hasWearOs) {
        WearScreen.LaunchList.route
    } else {
        WearScreen.PremiumGate.route
    }

    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(WearScreen.LaunchList.route) {
                val viewModel = koinViewModel<LaunchListViewModel>()
                LaunchListScreen(
                    viewModel = viewModel,
                    onLaunchClick = { launchId ->
                        navController.navigate("launch_detail/$launchId")
                    },
                    onSettingsClick = {
                        navController.navigate(WearScreen.Settings.route)
                    },
                )
            }
            composable(
                route = WearScreen.LaunchDetail.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(WearScreen.LaunchDetail.ARG_LAUNCH_ID) {
                        type = NavType.StringType
                    }
                ),
            ) { backStackEntry ->
                val launchId = backStackEntry.arguments
                    ?.getString(WearScreen.LaunchDetail.ARG_LAUNCH_ID) ?: return@composable
                val viewModel = koinViewModel<LaunchDetailViewModel>()
                LaunchDetailScreen(
                    viewModel = viewModel,
                    launchId = launchId,
                )
            }
            composable(WearScreen.PremiumGate.route) {
                PremiumGateScreen()
            }
            composable(WearScreen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
