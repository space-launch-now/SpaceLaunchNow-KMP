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
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_agencies_content_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_agencies_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_astronauts_content_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_astronauts_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_iss_content_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_iss_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_iss_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_news_events_content_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_news_events_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_rockets_content_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_rockets_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_starship_content_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_explore_card_starship_description
import spacelaunchnow_kmp.composeapp.generated.resources.common_agencies
import spacelaunchnow_kmp.composeapp.generated.resources.common_astronauts
import spacelaunchnow_kmp.composeapp.generated.resources.common_news_and_events
import spacelaunchnow_kmp.composeapp.generated.resources.common_rockets
import spacelaunchnow_kmp.composeapp.generated.resources.common_starship

object ExploreSections {
    val sections = listOf(
        ExploreSection(
            id = "iss_tracking",
            title = Res.string.app_explore_card_iss_title,
            description = Res.string.app_explore_card_iss_description,
            icon = Icons.Filled.Satellite,
            route = SpaceStationDetail(4), // ISS has ID 4
            contentDescription = Res.string.app_explore_card_iss_content_description
        ),
        ExploreSection(
            id = "agencies",
            title = Res.string.common_agencies,
            description = Res.string.app_explore_card_agencies_description,
            icon = Icons.Filled.Business,
            route = Agencies,
            contentDescription = Res.string.app_explore_card_agencies_content_description
        ),
        ExploreSection(
            id = "astronauts",
            title = Res.string.common_astronauts,
            description = Res.string.app_explore_card_astronauts_description,
            icon = Icons.Filled.Person,
            route = Astronauts,
            contentDescription = Res.string.app_explore_card_astronauts_content_description
        ),
        ExploreSection(
            id = "rockets",
            title = Res.string.common_rockets,
            description = Res.string.app_explore_card_rockets_description,
            icon = Icons.Filled.Rocket,
            route = Rockets,
            contentDescription = Res.string.app_explore_card_rockets_content_description
        ),
        ExploreSection(
            id = "starship",
            title = Res.string.common_starship,
            description = Res.string.app_explore_card_starship_description,
            icon = Icons.Filled.RocketLaunch,
            route = Starship,
            contentDescription = Res.string.app_explore_card_starship_content_description
        ),
        ExploreSection(
            id = "news_events",
            title = Res.string.common_news_and_events,
            description = Res.string.app_explore_card_news_events_description,
            icon = Icons.Filled.Newspaper,
            route = NewsEvents(),
            contentDescription = Res.string.app_explore_card_news_events_content_description
        )
    )
}
