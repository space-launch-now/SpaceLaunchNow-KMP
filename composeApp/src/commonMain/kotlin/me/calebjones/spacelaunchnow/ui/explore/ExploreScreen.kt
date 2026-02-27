package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import me.calebjones.spacelaunchnow.isTabletOrDesktop
import me.calebjones.spacelaunchnow.ui.explore.components.ExploreCard

/**
 * Explore screen displaying a grid of discovery sections.
 * 
 * Users can browse and navigate to:
 * - ISS Tracking
 * - Agencies
 * - Astronauts
 * - Rockets
 * - Starship
 * 
 * Layout adapts based on screen size:
 * - Phone: 2 columns (Fixed)
 * - Tablet/Desktop: 2-3 columns (Adaptive 200dp)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isTablet = isTabletOrDesktop()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
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
        LazyVerticalGrid(
            columns = if (isTablet) {
                // Adaptive columns on tablet/desktop (2-3 columns depending on width)
                GridCells.Adaptive(250.dp)
            } else {
                // Fixed 2 columns on phone
                GridCells.Fixed(2)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            verticalArrangement = Arrangement.spacedBy(gridSpacing)
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
                        GridItemSpan(2)
                    } else {
                        GridItemSpan(1)
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
