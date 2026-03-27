package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.ui.explore.components.ExploreCard
import me.calebjones.spacelaunchnow.ui.layout.rememberAdaptiveLayoutState

/**
 * Explore screen displaying a grid of discovery sections.
 *
 * Users can browse and navigate to:
 * - ISS Tracking
 * - Agencies
 * - Astronauts
 * - Rockets
 * - Starship
 * - News & Events
 *
 * Layout adapts based on screen size:
 * - Phone: 2 columns (Fixed)
 * - Tablet/Desktop: Adaptive columns with 180dp minimum cell width
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isTablet = rememberAdaptiveLayoutState().isExpanded

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        val gridSpacing = if (isTablet) 16.dp else 12.dp
        val horizontalPadding = if (isTablet) 16.dp else 16.dp

        LazyVerticalStaggeredGrid(
            columns = if (isTablet) {
                // Adaptive columns on tablet/desktop - fills available width efficiently
                StaggeredGridCells.Fixed(3)
            } else {
                // Fixed 2 columns on phone
                StaggeredGridCells.Fixed(2)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            verticalItemSpacing = gridSpacing
        ) {
            val sections = ExploreSections.sections
            val sectionCount = sections.size
            // Show full width for last item if odd count and using Fixed columns (phone)
            val showFullWidthLast = !isTablet && sectionCount % 2 == 1

            itemsIndexed(
                items = sections,
                key = { _, section -> section.id },
                span = { index, _ ->
                    if (showFullWidthLast && index == sectionCount - 1) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                }
            ) { _, section ->
                ExploreCard(
                    section = section,
                    isLargeScreen = isTablet,
                    onClick = {
                        navController.navigate(section.route)
                    }
                )
            }
        }
    }
}
