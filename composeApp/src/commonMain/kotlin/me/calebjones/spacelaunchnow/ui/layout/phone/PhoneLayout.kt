package me.calebjones.spacelaunchnow.ui.layout.phone


import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.Other
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.ui.compose.BottomNavigationBar
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.other.OtherScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.detail.EventDetailScreen
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PhoneLayout() {
    val navController = rememberNavController()
    
    // Observe the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Determine if the bottom bar should be shown
    val showBottomBar = when (navBackStackEntry?.destination?.route) {
        LaunchDetail::class.qualifiedName -> false // Hide for LaunchDetail
        EventDetail::class.qualifiedName -> false // Hide for EventDetail
        else -> {
            // For routes with arguments, check if it starts with LaunchDetail pattern
            val currentRoute = navBackStackEntry?.destination?.route
            // If the route contains LaunchDetail class name or pattern, hide bottom bar
            currentRoute?.contains("LaunchDetail") != true && currentRoute?.contains("EventDetail") != true
        }
    }
    
    SpaceLaunchNowTheme {
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
            ) {

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        modifier = Modifier
                    ) {
                        composableWithCompositionLocal<Home> {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                HomeScreen(navController = navController)
                            }
                        }
                        composableWithCompositionLocal<Other> {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                OtherScreen()
                            }
                        }
                        composableWithCompositionLocal<Settings> {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                SettingsScreen()
                            }
                        }
                        composableWithCompositionLocal<LaunchDetail> { backStackEntry ->
                            val launchDetail = backStackEntry.toRoute<LaunchDetail>()
                            // LaunchDetailScreen gets full screen access (no padding)
                            LaunchDetailScreen(
                                launchId = launchDetail.launchId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<EventDetail> { backStackEntry ->
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

// Custom composable function that provides AnimatedVisibilityScope via CompositionLocalProvider
inline fun <reified T : Any> NavGraphBuilder.composableWithCompositionLocal(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T> {
        CompositionLocalProvider(
            LocalNavAnimatedVisibilityScope provides this@composable
        ) {
            content(it)
        }
    }
}

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }