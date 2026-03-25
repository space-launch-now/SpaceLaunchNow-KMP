package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Satellite
import me.calebjones.spacelaunchnow.navigation.Agencies
import me.calebjones.spacelaunchnow.navigation.Astronauts
import me.calebjones.spacelaunchnow.navigation.NewsEvents
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.navigation.SpaceStationDetail
import me.calebjones.spacelaunchnow.navigation.Starship

object ExploreSections {
    val sections = listOf(
        ExploreSection(
            id = "iss_tracking",
            title = "ISS Tracking",
            description = "Live tracking and crew info",
            icon = Icons.Filled.Satellite,
            route = SpaceStationDetail(4), // ISS has ID 4
            contentDescription = "Navigate to ISS Tracking with live position and crew information"
        ),
        ExploreSection(
            id = "agencies",
            title = "Agencies",
            description = "Space agencies and missions",
            icon = Icons.Filled.Business,
            route = Agencies,
            contentDescription = "Navigate to Agencies list to explore space organizations"
        ),
        ExploreSection(
            id = "astronauts",
            title = "Astronauts",
            description = "Browse astronaut profiles",
            icon = Icons.Filled.Person,
            route = Astronauts,
            contentDescription = "Navigate to Astronauts list to view career stats and missions"
        ),
        ExploreSection(
            id = "rockets",
            title = "Rockets",
            description = "Launcher configurations",
            icon = Icons.Filled.Rocket,
            route = Rockets,
            contentDescription = "Navigate to Rockets list to explore launch vehicles"
        ),
        ExploreSection(
            id = "starship",
            title = "Starship",
            description = "SpaceX Starship development",
            icon = Icons.Filled.RocketLaunch,
            route = Starship,
            contentDescription = "Navigate to Starship dashboard for updates and launches"
        ),
        ExploreSection(
            id = "news_events",
            title = "News & Events",
            description = "Space news and upcoming events",
            icon = Icons.Filled.Newspaper,
            route = NewsEvents,
            contentDescription = "Navigate to News and Events to browse articles and upcoming space events"
        )
    )
}
