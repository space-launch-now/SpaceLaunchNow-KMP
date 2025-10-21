package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType

@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Schedule,
        Screen.Settings
    )

    val routes = listOf(Home, Schedule, Settings)

    // Column to stack banner ad above navigation bar
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        // Smart banner ad - no "Remove Ads" button in bottom nav area
        SmartBannerAd(
            showCard = false,
            placementType = AdPlacementType.NAVIGATION // Navigation context
        )

        // Navigation Bar
        Box(modifier = Modifier.navigationBarsPadding()) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry.value?.destination

                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == routes[index]::class.qualifiedName } == true,
                        onClick = {
                            // Navigate only if not already on the destination
                            if (currentDestination?.route != routes[index]::class.qualifiedName) {
                                navController.navigate(routes[index]) {
                                    // Pop up to the start destination of the graph to avoid building up a large back stack
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}