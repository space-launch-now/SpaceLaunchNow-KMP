package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.ui.compose.PlainShimmerCard
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState

/**
 * A vertical column of featured launches displayed below the hero launch card.
 * Shows up to 3 additional upcoming launches in compact, wide cards stacked vertically.
 *
 * Features:
 * - Vertical Column layout with cards stacked 1, 2, 3
 * - Shimmer loading placeholders during loading state
 * - Hides entirely if no additional launches are available
 */
@Composable
fun FeaturedLaunchesRow(
    launchesState: ViewState<List<LaunchNormal>>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val launches = launchesState.data
    val isLoading = launchesState.isLoading

    // Don't show anything if not loading and no launches available
    if (!isLoading && launches.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Content: Column with cards or shimmer placeholders
        if (isLoading && launches.isEmpty()) {
            // Shimmer loading state
            FeaturedLaunchesColumnShimmer()
        } else {
            // Actual content - vertical stack of cards
            launches.take(3).forEach { launch ->
                FeaturedLaunchRowCard(
                    launch = launch,
                    navController = navController,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Shimmer loading placeholder for the featured launches column.
 * Shows 3 placeholder cards stacked vertically.
 */
@Composable
private fun FeaturedLaunchesColumnShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            PlainShimmerCard(
                height = 130,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
