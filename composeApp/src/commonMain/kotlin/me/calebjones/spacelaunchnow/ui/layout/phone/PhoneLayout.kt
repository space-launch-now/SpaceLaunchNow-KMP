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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import me.calebjones.spacelaunchnow.navigation.AboutLibraries
import me.calebjones.spacelaunchnow.navigation.Agencies
import me.calebjones.spacelaunchnow.navigation.AgencyDetail
import me.calebjones.spacelaunchnow.navigation.AstronautDetail
import me.calebjones.spacelaunchnow.navigation.Astronauts
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.DebugSettings
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.Explore
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.LiveOnboarding
import me.calebjones.spacelaunchnow.navigation.NotificationSettings
import me.calebjones.spacelaunchnow.navigation.Onboarding
import me.calebjones.spacelaunchnow.navigation.Preload
import me.calebjones.spacelaunchnow.navigation.Roadmap
import me.calebjones.spacelaunchnow.navigation.RocketDetail
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.SpaceStationDetail
import me.calebjones.spacelaunchnow.navigation.Starship
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.navigation.ThemeCustomization
import me.calebjones.spacelaunchnow.ui.about.AboutLibrariesScreen
import me.calebjones.spacelaunchnow.ui.agencies.AgencyDetailScreen
import me.calebjones.spacelaunchnow.ui.agencies.AgencyListScreen
import me.calebjones.spacelaunchnow.ui.compose.BottomNavigationBar
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.event.EventDetailScreen
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.rockets.RocketDetailScreen
import me.calebjones.spacelaunchnow.ui.rockets.RocketListScreen
import me.calebjones.spacelaunchnow.ui.roadmap.RoadmapScreen
import me.calebjones.spacelaunchnow.ui.schedule.ScheduleScreen
import me.calebjones.spacelaunchnow.ui.settings.CalendarSyncScreen
import me.calebjones.spacelaunchnow.ui.settings.DebugSettingsScreen
import me.calebjones.spacelaunchnow.ui.settings.NotificationSettingsScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.ui.settings.ThemeCustomizationScreen
import me.calebjones.spacelaunchnow.ui.subscription.SupportUsScreen
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import me.calebjones.spacelaunchnow.ui.video.FullscreenVideoScreen
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PhoneLayout(
    navController: NavHostController,
    themeOption: ThemeOption = ThemeOption.System,
    content: @Composable () -> Unit
) {
    // Observe the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Determine if the bottom bar should be shown
    // Default to hidden when route is null (initial composition) to prevent flash
    val showBottomBar = when (navBackStackEntry?.destination?.route) {
        null -> false // Hide until navigation is resolved to prevent flash
        LaunchDetail::class.qualifiedName -> false // Hide for LaunchDetail
        EventDetail::class.qualifiedName -> false // Hide for EventDetail
        RocketDetail::class.qualifiedName -> false // Hide for RocketDetail
        Rockets::class.qualifiedName -> false // Hide for Rockets list
        Agencies::class.qualifiedName -> false // Hide for Agencies list
        AgencyDetail::class.qualifiedName -> false // Hide for AgencyDetail
        FullscreenVideo::class.qualifiedName -> false // Hide for FullscreenVideo
        AstronautDetail::class.qualifiedName -> false // Hide for AstronautDetail
        Astronauts::class.qualifiedName -> false
        SpaceStationDetail::class.qualifiedName -> false // Hide for SpaceStationDetail
        Starship::class.qualifiedName -> false // Hide for Starship
        NotificationSettings::class.qualifiedName -> false // Hide for NotificationSettings
        DebugSettings::class.qualifiedName -> false // Hide for DebugSettings
        AboutLibraries::class.qualifiedName -> false // Hide for AboutLibrariesScreen
        SupportUs::class.qualifiedName -> false // Hide for SupportUsScreen
        ThemeCustomization::class.qualifiedName -> false // Hide for ThemeCustomization
        CalendarSync::class.qualifiedName -> false // Hide for CalendarSync
        Roadmap::class.qualifiedName -> false // Hide for Roadmap
        Onboarding::class.qualifiedName -> false // Hide for full-screen Onboarding paywall
        LiveOnboarding::class.qualifiedName -> false // Hide for LiveOnboarding
        Preload::class.qualifiedName -> false // Hide for Preload

        else -> {
            // For routes with arguments, check if it starts with LaunchDetail pattern
            val currentRoute = navBackStackEntry?.destination?.route

            // If the route contains LaunchDetail, EventDetail, AgencyDetail, or FullscreenVideo, hide bottom bar
            currentRoute?.contains("Detail") != true &&
                    currentRoute?.contains("Rockets") != true &&
//                    currentRoute?.contains("Agencies") != true &&
                    currentRoute?.contains("Astronauts") != true &&
                    currentRoute?.contains("FullscreenVideo") != true &&
                    currentRoute?.contains("NotificationSettings") != true &&
                    currentRoute?.contains("DebugSettings") != true
        }
    }

    SpaceLaunchNowTheme(themeOption = themeOption) {
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
                    // Accept hoisted NavHost content and apply phone-specific padding
                    PhoneContentWrapper(
                        navController = navController,
                        innerPadding = innerPadding,
                        content = content
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneContentWrapper(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    content: @Composable () -> Unit
) {
    // Observe the current back stack entry to determine padding needs
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Determine which screens need special padding
    val needsBottomPadding = when (navBackStackEntry?.destination?.route) {
        // Full screen routes (no padding)
        LaunchDetail::class.qualifiedName,
        EventDetail::class.qualifiedName,
        RocketDetail::class.qualifiedName,
        AgencyDetail::class.qualifiedName,
        Agencies::class.qualifiedName,
        Rockets::class.qualifiedName,
        Astronauts::class.qualifiedName,
        Starship::class.qualifiedName,
        AstronautDetail::class.qualifiedName,
        SpaceStationDetail::class.qualifiedName,
        FullscreenVideo::class.qualifiedName,
        NotificationSettings::class.qualifiedName,
        DebugSettings::class.qualifiedName -> false

        // Default padding for main routes
        Home::class.qualifiedName,
        Settings::class.qualifiedName,
        Explore::class.qualifiedName,
        Schedule::class.qualifiedName -> true


        else -> {
            // For routes with arguments, check if it starts with certain patterns
            val currentRoute = navBackStackEntry?.destination?.route
            currentRoute?.contains("Detail") != true &&
                    currentRoute?.contains("FullscreenVideo") != true &&
                    currentRoute?.contains("NotificationSettings") != true &&
                    currentRoute?.contains("DebugSettings") != true
        }
    }

    val needsAllPadding = when (navBackStackEntry?.destination?.route) {
        Schedule::class.qualifiedName -> true
        else -> false
    }

    // Apply appropriate padding based on route
    Box(
        modifier = when {
            needsAllPadding -> Modifier.padding(innerPadding)
            needsBottomPadding -> Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            else -> Modifier // No padding for full screen routes
        }
    ) {
        content()
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