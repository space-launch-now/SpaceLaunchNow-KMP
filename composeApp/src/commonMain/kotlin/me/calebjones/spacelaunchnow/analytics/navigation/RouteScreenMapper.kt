package me.calebjones.spacelaunchnow.analytics.navigation

/**
 * Maps serialized navigation route strings to human-readable screen names for analytics.
 *
 * Route strings from type-safe navigation look like the fully-qualified class name of the
 * destination object/class, e.g. `me.calebjones.spacelaunchnow.navigation.LaunchDetail/{launchId}`.
 * We match on the simple class name fragment at the end of the route.
 */
object RouteScreenMapper {

    fun mapRouteToScreenName(route: String): String = when {
        route.contains("Home") -> "Home"
        route.contains("Schedule") -> "Schedule"
        route.contains("Explore") -> "Explore"
        route.contains("Settings") && route.contains("Notification") -> "Notification Settings"
        route.contains("Settings") && route.contains("Debug") -> "Debug Settings"
        route.contains("Settings") -> "Settings"
        route.contains("LaunchDetail") -> "Launch Detail"
        route.contains("EventDetail") -> "Event Detail"
        route.contains("AgencyDetail") -> "Agency Detail"
        route.contains("Agencies") -> "Agencies"
        route.contains("RocketDetail") -> "Rocket Detail"
        route.contains("Rockets") -> "Rockets"
        route.contains("AstronautDetail") -> "Astronaut Detail"
        route.contains("Astronauts") -> "Astronauts"
        route.contains("SpaceStationDetail") -> "Space Station Detail"
        route.contains("FullscreenVideo") -> "Fullscreen Video"
        route.contains("AboutLibraries") -> "About Libraries"
        route.contains("SupportUs") -> "Support Us"
        route.contains("ThemeCustomization") -> "Theme Customization"
        route.contains("CalendarSync") -> "Calendar Sync"
        route.contains("Roadmap") -> "Roadmap"
        route.contains("Starship") -> "Starship"
        route.contains("NewsEvents") -> "News & Events"
        route.contains("LiveOnboarding") -> "Live Onboarding"
        route.contains("Onboarding") -> "Onboarding"
        route.contains("Preload") -> "Preload"
        else -> route
    }
}
