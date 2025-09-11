package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.Other
import me.calebjones.spacelaunchnow.navigation.Settings

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Other,
        Screen.Settings
    )
    
    val routes = listOf(Home, Other, Settings)

    NavigationBar {
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