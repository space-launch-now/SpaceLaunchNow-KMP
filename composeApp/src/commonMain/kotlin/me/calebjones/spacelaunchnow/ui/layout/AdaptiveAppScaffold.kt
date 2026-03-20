package me.calebjones.spacelaunchnow.ui.layout

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import org.jetbrains.compose.resources.painterResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launcher

/**
 * Unified adaptive app scaffold that uses NavigationSuiteScaffold to automatically
 * select the correct navigation component based on window width:
 * - Compact (< 600dp): NavigationBar (bottom)
 * - Medium (600-840dp): NavigationRail (side, icon-only)
 * - Expanded (≥ 840dp): NavigationRail with labels or NavigationDrawer
 *
 * Replaces the separate [me.calebjones.spacelaunchnow.ui.layout.phone.PhoneLayout] and
 * [me.calebjones.spacelaunchnow.ui.layout.desktop.TabletDesktopLayout] composables.
 *
 * Navigation visibility is controlled by [NavigationRouteConfig] — navigation shows
 * only for the 4 main tab routes (Home, Schedule, Explore, Settings).
 *
 * KMP Dependencies:
 * - org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.10.0-alpha05
 * - org.jetbrains.compose.material3.adaptive:adaptive:1.2.0 (existing)
 */
@OptIn(
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun AdaptiveAppScaffold(
    navController: NavHostController,
    themeOption: ThemeOption = ThemeOption.System,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showNavigation = NavigationRouteConfig.shouldShowNavigation(currentRoute)
    val showAds = NavigationRouteConfig.shouldShowAds(currentRoute)

    SpaceLaunchNowTheme(themeOption = themeOption) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Main content area with optional navigation
                    Box(modifier = Modifier.weight(1f)) {
                        if (showNavigation) {
                            val layoutState = rememberAdaptiveLayoutState()

                            if (layoutState.tier == LayoutTier.COMPACT) {
                                // Phone: Scaffold with ad above NavigationBar
                                Scaffold(
                                    bottomBar = {
                                        Column {
                                            if (showAds) SmartBannerAd(
                                                placementType = AdPlacementType.NAVIGATION,
                                                showCard = false,
                                            )
                                            NavigationBar {
                                                mainNavigationItems.forEach { tab ->
                                                    NavigationBarItem(
                                                        icon = {
                                                            Icon(
                                                                tab.icon,
                                                                contentDescription = tab.contentDescription
                                                            )
                                                        },
                                                        label = { Text(tab.label) },
                                                        selected = currentRoute == tab.route::class.qualifiedName,
                                                        onClick = {
                                                            navController.navigate(tab.route) {
                                                                launchSingleTop = true
                                                                restoreState = true
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                ) { innerPadding ->
                                    Box(modifier = Modifier.padding(innerPadding)) {
                                        content()
                                    }
                                }
                            } else {
                                // Tablet/Desktop: ModalWideNavigationRail with expand/collapse toggle
                                val railState = rememberWideNavigationRailState(
                                    initialValue = if (layoutState.isDesktop)
                                        WideNavigationRailValue.Expanded
                                    else
                                        WideNavigationRailValue.Collapsed
                                )
                                val scope = rememberCoroutineScope()
                                val headerDescription =
                                    if (railState.targetValue == WideNavigationRailValue.Expanded) {
                                        "Collapse rail"
                                    } else {
                                        "Expand rail"
                                    }
                                Row(modifier = Modifier.fillMaxSize()) {
                                    ModalWideNavigationRail(
                                        state = railState,
                                        expandedHeaderTopPadding = 64.dp,
                                        header = {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Image(
                                                    painter = painterResource(Res.drawable.launcher),
                                                    contentDescription = "Space Launch Now",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                )
                                            }
                                        },
                                    ) {
                                        mainNavigationItems.forEach { tab ->
                                            WideNavigationRailItem(
                                                selected = currentRoute == tab.route::class.qualifiedName,
                                                onClick = {
                                                    navController.navigate(tab.route) {
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                },
                                                icon = {
                                                    Icon(
                                                        tab.icon,
                                                        contentDescription = tab.contentDescription
                                                    )
                                                },
                                                label = { Text(tab.label) },
                                                railExpanded = railState.targetValue == WideNavigationRailValue.Expanded
                                            )
                                        }
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.weight(1f).fillMaxSize()
                                    ) {
                                        content()
                                    }
                                }
                            }
                        } else {
                            // No navigation for detail/system screens
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                content()
                            }
                        }
                    }

                    // Banner ad for tablet/desktop and non-navigation screens
                    // (Phone/compact ad is placed above NavigationBar in the Scaffold bottomBar)
                    if (showAds && !(showNavigation && rememberAdaptiveLayoutState().tier == LayoutTier.COMPACT)) {
                        SmartBannerAd(
                            modifier = Modifier.padding(
                                bottom = WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            ),
                            placementType = AdPlacementType.NAVIGATION,
                            showCard = false,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdaptiveAppScaffoldPreview() {
    SpaceLaunchNowPreviewTheme {
        val navController = rememberNavController()
        AdaptiveAppScaffold(navController = navController) {
            Text("Preview content")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdaptiveAppScaffoldDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        val navController = rememberNavController()
        AdaptiveAppScaffold(navController = navController) {
            Text("Preview content (dark)")
        }
    }
}
