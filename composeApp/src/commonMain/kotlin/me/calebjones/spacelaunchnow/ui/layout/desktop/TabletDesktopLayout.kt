package me.calebjones.spacelaunchnow.ui.layout.desktop

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Explore
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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
import me.calebjones.spacelaunchnow.navigation.AboutLibraries
import me.calebjones.spacelaunchnow.navigation.Agencies
import me.calebjones.spacelaunchnow.navigation.AgencyDetail
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.DebugSettings
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.Explore
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.NotificationSettings
import me.calebjones.spacelaunchnow.navigation.Roadmap
import me.calebjones.spacelaunchnow.navigation.RocketDetail
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Screen
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.agencies.AgencyDetailScreen
import me.calebjones.spacelaunchnow.ui.agencies.AgencyListScreen
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.event.EventDetailScreen
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import me.calebjones.spacelaunchnow.ui.layout.phone.composableWithCompositionLocal
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
import org.jetbrains.compose.resources.painterResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launcher

@Deprecated(
    message = "Use AdaptiveAppScaffold which automatically selects NavigationBar/Rail/Drawer",
    replaceWith = ReplaceWith(
        "AdaptiveAppScaffold(navController, themeOption, content)",
        "me.calebjones.spacelaunchnow.ui.layout.AdaptiveAppScaffold"
    )
)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TabletDesktopLayout(
    navController: NavHostController,
    themeOption: ThemeOption = ThemeOption.System,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if the side navigation rail should be shown
    val showSideNavigation = when (navBackStackEntry?.destination?.route) {
        LaunchDetail::class.qualifiedName -> false // Hide for LaunchDetail
        EventDetail::class.qualifiedName -> false // Hide for EventDetail
        RocketDetail::class.qualifiedName -> false // Hide for RocketDetail
        AgencyDetail::class.qualifiedName -> false // Hide for AgencyDetail
        FullscreenVideo::class.qualifiedName -> false // Hide for FullscreenVideo
        NotificationSettings::class.qualifiedName -> false // Hide for NotificationSettings
        DebugSettings::class.qualifiedName -> false // Hide for DebugSettings
        Roadmap::class.qualifiedName -> false // Hide for Roadmap

        else -> {
            // For routes with arguments, check if it contains certain patterns
            val currentRoute = navBackStackEntry?.destination?.route
            currentRoute?.contains("LaunchDetail") != true &&
                    currentRoute?.contains("EventDetail") != true &&
                    currentRoute?.contains("AgencyDetail") != true &&
                    currentRoute?.contains("RocketDetail") != true &&
                    currentRoute?.contains("FullscreenVideo") != true &&
                    currentRoute?.contains("NotificationSettings") != true &&
                    currentRoute?.contains("DebugSettings") != true
        }
    }

    SpaceLaunchNowTheme(themeOption = themeOption) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                val screens = listOf(Home, Schedule, Explore, Settings)
                val items = listOf("Home", "Schedule", "Explore", "Settings")
                val selectedIcons =
                    listOf(Icons.Filled.Home, Icons.AutoMirrored.Filled.List, Icons.Filled.Explore, Icons.Filled.Settings)

                val windowSizeInfo = currentWindowAdaptiveInfo()
                val windowSize = windowSizeInfo.windowSizeClass

                // Use Column to stack main content and banner ad
                Column(modifier = Modifier.fillMaxSize()) {
                    // Main content area
                    Scaffold(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(Modifier.fillMaxSize()) {
                            // Sidebar Navigation using NavigationRail - conditionally shown
                            if (showSideNavigation) {
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
                            }
                            // Main Content - Accept hoisted NavHost content
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                content()
                            }
                        }
                    }

                    // Smart banner ad for tablets/desktop - placement-aware sizing
                    SmartBannerAd(
                        modifier = Modifier.padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        ),
                        placementType = AdPlacementType.NAVIGATION, // Navigation context
                        showCard = false,
                    )
                }
            }
        }
    }
}