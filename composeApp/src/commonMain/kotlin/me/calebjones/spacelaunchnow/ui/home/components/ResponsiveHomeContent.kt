package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.isDesktop
import me.calebjones.spacelaunchnow.isLandscape
import me.calebjones.spacelaunchnow.isTablet
import me.calebjones.spacelaunchnow.navigation.Schedule

@Composable
fun ResponsiveHomeContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if ((isTablet() && isLandscape()) || isDesktop()) {
        // Landscape layout - side-by-side content
        Column {
            HomeTopBar()
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left side - Featured launch (smaller)
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NextLaunchView(navController = navController)
                }

                // Right side - Other content
                LazyColumn(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                ) {
                    item {
                        SectionTitle(
                            title = "Launch Schedule",
                            hasAction = true,
                            onActionClick = {
                                navController.navigate(Schedule) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                    }
                    item { UpcomingHorizontalScrollableList(navController = navController) }
                    item { SectionTitle(title = "Latest Updates", hasAction = false) }
                    item { LatestUpdatesView(navController = navController) }
                    item { SectionTitle(title = "Latest News", hasAction = false) }
                    item { ArticlesView() }
                    item { SectionTitle(title = "Upcoming Events", hasAction = false) }
                    item { EventsView(navController = navController) }
                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
        ) {
            item { HomeTopBar() }
            item { NextLaunchView(navController = navController) }
            item {
                SectionTitle(
                    title = "Launch Schedule",
                    hasAction = true,
                    onActionClick = {
                        navController.navigate(Schedule) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            item { UpcomingHorizontalScrollableList(navController = navController) }
            item { SectionTitle(title = "Latest Updates", hasAction = false) }
            item { LatestUpdatesView(navController = navController) }
            item { SectionTitle(title = "Latest News", hasAction = false) }
            item { ArticlesView() }
            item { SectionTitle(title = "Upcoming Events", hasAction = false) }
            item { EventsView(navController = navController) }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
