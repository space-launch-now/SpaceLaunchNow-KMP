package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil

/**
 * Responsive home quick view that adapts to screen size:
 * - Phone: Bidirectional carousel with recent and upcoming launches
 * - Tablet/Desktop: Side-by-side layout with last launch, next launch hero card, and upcoming preview
 */
@Composable
fun HomeQuickView(
    viewModel: HomeViewModel,
    previousLaunches: List<LaunchNormal>,
    upcomingLaunches: List<LaunchNormal>,
    isTabletOrDesktop: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (isTabletOrDesktop) {
        TabletQuickView(
            previousLaunch = previousLaunches.firstOrNull(),
            upcomingLaunches = upcomingLaunches,
            navController = navController,
            modifier = modifier
        )
    } else {
        PhoneQuickView(
            viewModel = viewModel,
            navController = navController
        )
    }
}

/**
 * Tablet/Desktop layout: Side-by-side cards with preview row
 */
@Composable
private fun TabletQuickView(
    previousLaunch: LaunchNormal?,
    upcomingLaunches: List<LaunchNormal>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Side-by-side: Last Launch + Next Up
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: Last successful launch (smaller)
            if (previousLaunch != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Last Launch",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LastLaunchCompactCard(
                        launch = previousLaunch,
                        navController = navController
                    )
                }
            }

            // Right: Next upcoming launch (larger hero card)
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Next Up",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                upcomingLaunches.firstOrNull()?.let { launch ->
                    NextUpHeroCard(
                        launch = launch,
                        navController = navController
                    )
                }
            }
        }

        // Preview row: Coming soon launches
        if (upcomingLaunches.size > 1) {
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Coming Soon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = { navController.navigate(Schedule) }
                ) {
                    Text("See All")
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(upcomingLaunches.drop(1).take(5)) { launch ->
                    CompactUpcomingCard(
                        launch = launch,
                        onClick = { navController.navigate("launch/${launch.id}") }
                    )
                }

                // "See all" card at the end
                item {
                    OutlinedCard(
                        onClick = { navController.navigate(Schedule) },
                        modifier = Modifier.width(180.dp).height(100.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "View Full\nSchedule",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Phone layout: Bidirectional carousel
 */
@Composable
private fun PhoneQuickView(
    viewModel: HomeViewModel,
    navController: NavController
) {
    // Simply delegate to the existing LaunchListView which has the bidirectional carousel
    LaunchListView(
        viewModel = viewModel,
        navController = navController
    )
}

/**
 * Compact card for the last launch (tablet)
 */
@Composable
internal fun LastLaunchCompactCard(
    launch: LaunchNormal,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { navController.navigate("launch/${launch.id}") },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                LaunchFormatUtil.formatLaunchTitle(launch),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))

            launch.status?.name?.let { status ->
                Badge(
                    containerColor = when {
                        status.contains("Success", ignoreCase = true) ->
                            MaterialTheme.colorScheme.tertiary

                        else -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        status.uppercase(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Unknown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Hero card for next upcoming launch (tablet)
 */
@Composable
private fun NextUpHeroCard(
    launch: LaunchNormal,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { navController.navigate("launch/${launch.id}") },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                LaunchFormatUtil.formatLaunchTitle(launch),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            // Countdown or time
            Text(
                DateTimeUtil.formatLaunchDate(launch.net!!),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            // Location
            launch.pad?.location?.name?.let { location ->
                Text(
                    location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Compact card for upcoming launches preview row (tablet)
 */
@Composable
private fun CompactUpcomingCard(
    launch: LaunchNormal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.width(180.dp).height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                LaunchFormatUtil.formatLaunchTitle(launch),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )

            Text(
                DateTimeUtil.formatLaunchDate(launch.net!!),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}