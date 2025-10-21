package me.calebjones.spacelaunchnow.ui.layout.desktop

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.launcher
import me.calebjones.spacelaunchnow.navigation.AboutLibraries
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.DebugSettings
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.NotificationSettings
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.detail.EventDetailScreen
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import me.calebjones.spacelaunchnow.ui.layout.phone.composableWithCompositionLocal
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
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalSharedTransitionApi::class, DependsOnGoogleMobileAds::class)
@Composable
fun TabletDesktopLayout(
    navController: NavHostController,
    themeOption: ThemeOption = ThemeOption.System
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    SpaceLaunchNowTheme(themeOption = themeOption) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                val screens = listOf(Home, Schedule, Settings)
                val items = listOf("Home", "Schedule", "Settings")
                val selectedIcons =
                    listOf(Icons.Filled.Home, Icons.AutoMirrored.Filled.List, Icons.Filled.Settings)

                // Use Column to stack main content and banner ad
                Column(modifier = Modifier.fillMaxSize()) {
                    // Main content area
                    Scaffold(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(Modifier.fillMaxSize()) {
                            // Sidebar Navigation using NavigationRail
                            Column(
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                NavigationRail(
                                    header = {
                                        Image(
                                            painter = painterResource(Res.drawable.launcher),
                                            contentDescription = "App Icon",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(96.dp)
                                                .absolutePadding(
                                                    left = 8.dp,
                                                    right = 8.dp,
                                                    bottom = 32.dp,
                                                    top = 16.dp
                                                )
                                        )
                                    },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    screens.forEachIndexed { index, screen ->
                                        NavigationRailItem(
                                            modifier = Modifier.padding(8.dp),
                                            icon = {
                                                Icon(
                                                    selectedIcons[index], // Always use same icon for simplicity
                                                    contentDescription = items[index]
                                                )
                                            },
                                            label = {
                                                Text(
                                                    items[index],
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
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
                            HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
                            // Main Content
                            Surface(
                                color = MaterialTheme.colorScheme.background
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = Home,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    composableWithCompositionLocal<Home> {
                                        HomeScreen(navController = navController)
                                    }
                                    composable<Schedule> {
                                        ScheduleScreen(
                                            onLaunchClick = { id ->
                                                navController.navigate(
                                                    LaunchDetail(
                                                        id
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    composableWithCompositionLocal<Settings> {
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
                                    composableWithCompositionLocal<NotificationSettings> {
                                        NotificationSettingsScreen(
                                            navController = navController,
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                    composableWithCompositionLocal<DebugSettings> {
                                        DebugSettingsScreen(
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                    composableWithCompositionLocal<LaunchDetail> { backStackEntry ->
                                        val launchDetail = backStackEntry.toRoute<LaunchDetail>()
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
                                    composableWithCompositionLocal<FullscreenVideo> { backStackEntry ->
                                        val fullscreenVideo =
                                            backStackEntry.toRoute<FullscreenVideo>()
                                        FullscreenVideoScreen(
                                            videoUrl = fullscreenVideo.videoUrl,
                                            launchName = fullscreenVideo.launchName,
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                    composableWithCompositionLocal<SupportUs> {
                                        SupportUsScreen(
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                    composableWithCompositionLocal<me.calebjones.spacelaunchnow.navigation.ThemeCustomization> {
                                        ThemeCustomizationScreen(
                                            navController = navController
                                        )
                                    }
                                    composableWithCompositionLocal<CalendarSync> {
                                        CalendarSyncScreen(
                                            navController = navController
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Smart banner ad for tablets/desktop - no "Remove Ads" button in bottom area
                    SmartBannerAd(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        adSize = AdSize.FLUID,
                        showCard = false
                    )
                }
            }
        }
    }
}