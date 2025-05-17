package me.calebjones.spacelaunchnow.ui.layout.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.ui.compose.BottomNavigationBar
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.other.OtherScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import org.koin.compose.KoinApplication

@Composable
fun PhoneLayout() {
    val navController = rememberNavController()
    SpaceLaunchNowTheme {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen()
                }
                composable(Screen.Other.route) {
                    OtherScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }
}