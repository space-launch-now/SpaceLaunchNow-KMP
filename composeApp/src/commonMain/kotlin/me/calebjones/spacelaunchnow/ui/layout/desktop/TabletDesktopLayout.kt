package me.calebjones.spacelaunchnow.ui.layout.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.other.ScheduleScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.detail.EventDetailScreen
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme

@Composable
fun TabletDesktopLayout() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    SpaceLaunchNowTheme {
        val screens = listOf(Home, Schedule, Settings)
        val items = listOf("Home", "Schedule", "Settings")
        val selectedIcons = listOf(Icons.Filled.Home, Icons.AutoMirrored.Filled.List, Icons.Filled.Settings)

        Scaffold {
            Row(Modifier.fillMaxSize()) {
                // Sidebar Navigation using NavigationRail
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NavigationRail(
                        header = {
                            Icon(
                                Icons.Filled.RocketLaunch,
                                contentDescription = "App",
                                modifier = Modifier.absolutePadding(left = 8.dp, right = 8.dp, bottom = 32.dp, top = 16.dp)
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ){
                        screens.forEachIndexed { index, screen ->
                            NavigationRailItem(
                                modifier = Modifier.padding(8.dp),
                                icon = {
                                    Icon(
                                        selectedIcons[index], // Always use same icon for simplicity
                                        contentDescription = items[index]
                                    )
                                },
                                label = { Text(items[index], color = MaterialTheme.colorScheme.onSurface) },
                                selected = currentDestination?.route == screen::class.qualifiedName,
                                onClick = { 
                                    navController.navigate(screen) {
                                        // Avoid multiple copies of the same destination
                                        launchSingleTop = true
                                        // Restore state when returning to a previously selected screen
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
                // Main Content
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable<Home> {
                            HomeScreen(navController = navController)
                        }
                        composable<Schedule> {
                            ScheduleScreen(
                                onLaunchClick = { id -> navController.navigate(LaunchDetail(id)) }
                            )
                        }
                        composable<Settings> {
                            SettingsScreen()
                        }
                        composable<LaunchDetail> { backStackEntry ->
                            val launchDetail = backStackEntry.toRoute<LaunchDetail>()
                            LaunchDetailScreen(
                                launchId = launchDetail.launchId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<EventDetail> { backStackEntry ->
                            val eventDetail = backStackEntry.toRoute<EventDetail>()
                            EventDetailScreen(
                                eventId = eventDetail.eventId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}