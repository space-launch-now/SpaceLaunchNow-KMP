package me.calebjones.spacelaunchnow.ui.layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import me.calebjones.spacelaunchnow.navigation.Explore
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LiveOnboarding
import me.calebjones.spacelaunchnow.navigation.NewsEvents
import me.calebjones.spacelaunchnow.navigation.Onboarding
import me.calebjones.spacelaunchnow.navigation.Preload
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Settings
import kotlin.reflect.KClass

/**
 * Data class for a navigation tab item. Replaces the parallel lists of
 * labels/icons/routes that existed in PhoneLayout and TabletDesktopLayout.
 */
data class NavigationTabItem(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

/**
 * The four main navigation tab items.
 */
val mainNavigationItems = listOf(
    NavigationTabItem(
        route = Home,
        label = "Home",
        icon = Icons.Filled.Home,
        contentDescription = "Home tab"
    ),
    NavigationTabItem(
        route = Schedule,
        label = "Schedule",
        icon = Icons.AutoMirrored.Filled.List,
        contentDescription = "Schedule tab"
    ),
    NavigationTabItem(
        route = Explore,
        label = "Explore",
        icon = Icons.Filled.Explore,
        contentDescription = "Explore tab"
    ),
    NavigationTabItem(
        route = NewsEvents,
        label = "News",
        icon = Icons.Filled.Newspaper,
        contentDescription = "News and Events tab"
    ),
    NavigationTabItem(
        route = Settings,
        label = "Settings",
        icon = Icons.Filled.Settings,
        contentDescription = "Settings tab"
    )
)

/**
 * Centralized route metadata for navigation visibility.
 *
 * Uses a **positive-list pattern**: only the 4 main tab routes show navigation.
 * All other routes (detail screens, system flows, settings sub-pages) hide it.
 *
 * Adding a new detail/system screen requires NO changes to this config.
 * Adding a new main tab requires adding ONE entry to [mainTabRoutes].
 */
object NavigationRouteConfig {

    /** Routes where primary navigation (bar/rail/drawer) is visible */
    private val mainTabRoutes: Set<KClass<*>> = setOf(
        Home::class,
        Schedule::class,
        Explore::class,
        NewsEvents::class,
        Settings::class
    )

    /** Routes where ads should be hidden (onboarding / preload flows) */
    private val adFreeRoutes: Set<KClass<*>> = setOf(
        Preload::class,
        LiveOnboarding::class,
        Onboarding::class
    )

    /**
     * Returns true if navigation should be shown for the given route.
     * @param route The qualified route name from NavBackStackEntry.destination.route
     */
    fun shouldShowNavigation(route: String?): Boolean {
        if (route == null) return false
        return mainTabRoutes.any { it.qualifiedName == route }
    }

    /**
     * Returns true if ads should be shown for the given route.
     * Ads are hidden during onboarding and preload flows.
     */
    fun shouldShowAds(route: String?): Boolean {
        if (route == null) return false
        return adFreeRoutes.none { it.qualifiedName == route }
    }
}
