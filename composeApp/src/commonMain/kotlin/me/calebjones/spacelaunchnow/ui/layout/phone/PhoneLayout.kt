package me.calebjones.spacelaunchnow.ui.layout.phone


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.Other
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.ui.compose.BottomNavigationBar
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.other.OtherScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme

@Composable
fun PhoneLayout() {
    val navController = rememberNavController()
    SpaceLaunchNowTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Home,
                modifier = Modifier
            ) {
                composable<Home> {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        HomeScreen(navController = navController)
                    }
                }
                composable<Other> {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        OtherScreen()
                    }
                }
                composable<Settings> {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        SettingsScreen()
                    }
                }
                composable<LaunchDetail> { backStackEntry ->
                    val launchDetail = backStackEntry.toRoute<LaunchDetail>()
                    // LaunchDetailScreen gets full screen access (no padding)
                    LaunchDetailScreen(
                        launchId = launchDetail.launchId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}