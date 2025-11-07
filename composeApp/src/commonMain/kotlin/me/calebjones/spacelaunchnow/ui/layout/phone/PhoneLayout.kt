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
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.DebugSettings
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.NotificationSettings
import me.calebjones.spacelaunchnow.navigation.RocketDetail
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Settings
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
    themeOption: ThemeOption = ThemeOption.System
) {
    // Observe the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Determine if the bottom bar should be shown
    val showBottomBar = when (navBackStackEntry?.destination?.route) {
        LaunchDetail::class.qualifiedName -> false // Hide for LaunchDetail
        EventDetail::class.qualifiedName -> false // Hide for EventDetail
        RocketDetail::class.qualifiedName -> false // Hide for RocketDetail
        Rockets::class.qualifiedName -> false // Hide for Rockets list
        Agencies::class.qualifiedName -> false // Hide for Agencies list
        AgencyDetail::class.qualifiedName -> false // Hide for AgencyDetail
        FullscreenVideo::class.qualifiedName -> false // Hide for FullscreenVideo
        NotificationSettings::class.qualifiedName -> false // Hide for NotificationSettings
        DebugSettings::class.qualifiedName -> false // Hide for DebugSettings
        AboutLibraries::class.qualifiedName -> false // Hide for AboutLibrariesScreen
        SupportUs::class.qualifiedName -> false // Hide for SupportUsScreen
        ThemeCustomization::class.qualifiedName -> false // Hide for ThemeCustomization
        CalendarSync::class.qualifiedName -> false // Hide for CalendarSync

        else -> {
            // For routes with arguments, check if it starts with LaunchDetail pattern
            val currentRoute = navBackStackEntry?.destination?.route

            // If the route contains LaunchDetail, EventDetail, AgencyDetail, or FullscreenVideo, hide bottom bar
            currentRoute?.contains("LaunchDetail") != true &&
                    currentRoute?.contains("EventDetail") != true &&
                    currentRoute?.contains("AgencyDetail") != true &&
                    currentRoute?.contains("Rockets") != true &&
                    currentRoute?.contains("Agencies") != true &&
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
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        modifier = Modifier
                    ) {
                        composableWithCompositionLocal<Home> {
                            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                                HomeScreen(navController = navController)
                            }
                        }
                        composableWithCompositionLocal<Schedule> {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                ScheduleScreen(
                                    onLaunchClick = { id -> navController.navigate(LaunchDetail(id)) }
                                )
                            }
                        }
                        composableWithCompositionLocal<Settings> {
                            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                                SettingsScreen(
                                    navController = navController,
                                    onOpenNotificationSettings = {
                                        navController.navigate(NotificationSettings)
                                    },
                                    onOpenDebugSettings = {
                                        navController.navigate(DebugSettings)
                                    },
                                    onOpenAboutLibraries = {
                                        navController.navigate(AboutLibraries)
                                    }
                                )
                            }
                        }
                        composableWithCompositionLocal<LaunchDetail> { backStackEntry ->
                            val launchDetail = backStackEntry.toRoute<LaunchDetail>()
                            // LaunchDetailScreen gets full screen access (no padding)
                            LaunchDetailScreen(
                                launchId = launchDetail.launchId,
                                onNavigateBack = { navController.popBackStack() },
                                navController = navController
                            )
                        }
                        composableWithCompositionLocal<EventDetail> { backStackEntry ->
                            val eventDetail = backStackEntry.toRoute<EventDetail>()
                            EventDetailScreen(
                                eventId = eventDetail.eventId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<AgencyDetail> { backStackEntry ->
                            val agencyDetail = backStackEntry.toRoute<AgencyDetail>()
                            AgencyDetailScreen(
                                agencyId = agencyDetail.agencyId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<FullscreenVideo> { backStackEntry ->
                            val fullscreenVideo = backStackEntry.toRoute<FullscreenVideo>()
                            FullscreenVideoScreen(
                                videoUrl = fullscreenVideo.videoUrl,
                                launchName = fullscreenVideo.launchName,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<NotificationSettings> {
                            // NotificationSettingsScreen gets full screen access (no padding)
                            NotificationSettingsScreen(
                                navController = navController,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<DebugSettings> {
                            // DebugSettingsScreen gets full screen access (no padding)
                            DebugSettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<AboutLibraries> {
                            AboutLibrariesScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composableWithCompositionLocal<SupportUs> {
                            SupportUsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<ThemeCustomization> {
                            ThemeCustomizationScreen(
                                navController = navController
                            )
                        }
                        composableWithCompositionLocal<CalendarSync> {
                            CalendarSyncScreen(
                                navController = navController
                            )
                        }
                        composableWithCompositionLocal<Rockets> {
                            RocketListScreen(
                                onNavigateToRocketDetail = { id ->
                                    navController.navigate(
                                        RocketDetail(id)
                                    )
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<RocketDetail> { backStackEntry ->
                            val rocketDetail = backStackEntry.toRoute<RocketDetail>()
                            RocketDetailScreen(
                                rocketId = rocketDetail.rocketId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<Agencies> {
                            AgencyListScreen(
                                onNavigateToAgencyDetail = { id ->
                                    navController.navigate(
                                        AgencyDetail(id)
                                    )
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composableWithCompositionLocal<AgencyDetail> { backStackEntry ->
                            val agencyDetail = backStackEntry.toRoute<AgencyDetail>()
                            AgencyDetailScreen(
                                agencyId = agencyDetail.agencyId,
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