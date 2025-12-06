package me.calebjones.spacelaunchnow.ui.starship.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.ui.compose.StarshipOverviewShimmer
import me.calebjones.spacelaunchnow.ui.detail.compose.components.VideoPlayer
import me.calebjones.spacelaunchnow.ui.home.components.LaunchItemView
import me.calebjones.spacelaunchnow.ui.home.components.SectionTitle
import me.calebjones.spacelaunchnow.ui.home.components.UpdateCard
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState

/**
 * Overview tab for Starship Dashboard
 *
 * Displays:
 * - Program info
 * - Livestream video player
 * - Next Starship launch
 * - Status updates
 *
 * Uses ViewState pattern for per-section loading states:
 * - Initial load: Shows shimmer skeleton
 * - Error with no data: Shows error message
 * - Has data: Shows content (possibly with staleness indicator)
 */
@Composable
fun StarshipOverviewTab(
    programState: ViewState<ProgramNormal?>,
    nextLaunchState: ViewState<LaunchNormal?>,
    updatesState: ViewState<List<UpdateEndpoint>>,
    videoPlayerState: VideoPlayerState,
    navController: NavController,
    onRefresh: () -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track whether to show all updates or just the first 5
    var showAllUpdates by remember { mutableStateOf(false) }
    val initialUpdateCount = 5
    val allUpdates = updatesState.data
    val displayedUpdates = if (showAllUpdates) allUpdates else allUpdates.take(initialUpdateCount)
    val hasMoreUpdates = allUpdates.size > initialUpdateCount

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // State 1: Initial load - show shimmer
            programState.isInitialLoading && nextLaunchState.isInitialLoading -> {
                StarshipOverviewShimmer()
            }
            // State 2: Error with no data - show error
            programState.hasErrorWithNoData && nextLaunchState.hasErrorWithNoData -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = programState.error ?: "Failed to load data",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Tap to retry",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { onRefresh() }
                        )
                    }
                }
            }
            // State 3: Has data - show content
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Staleness indicator (if showing stale data)
                    if (programState.isStale || nextLaunchState.isStale) {
                        item {
                            StalenessIndicator(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Livestream Video Player (if available) - Edge to Edge
                    if (videoPlayerState.availableVideos.isNotEmpty()) {
                        item {
                            Column {
                                VideoPlayer(
                                    videoPlayerState = videoPlayerState,
                                    launchName = programState.data?.name ?: "Starship",
                                    onSetPlayerVisible = onSetPlayerVisible,
                                    onNavigateToFullscreen = onNavigateToFullscreen,
                                    onVideoSelected = onVideoSelected
                                )
                            }
                        }
                    }

                    // Program Info Card
                    programState.data?.let { program ->
                        item {
                            ProgramInfoCard(
                                program = program,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Next Launch Section
                    nextLaunchState.data?.let { launch ->
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                SectionTitle(title = "Next Launch", hasAction = false)
                                Spacer(modifier = Modifier.height(8.dp))
                                LaunchItemView(
                                    launch = launch,
                                    navController = navController,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                        }
                    }

                    // Status Updates Section
                    if (allUpdates.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                SectionTitle(
                                    title = "Updates",
                                    hasAction = false
                                )
                            }
                        }

                        items(displayedUpdates) { update ->
                            UpdateCard(
                                update = update,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                fillMaxWidth = true
                            )
                        }

                        // Show more/less button - only if there are more than initialUpdateCount updates
                        if (hasMoreUpdates) {
                            item {
                                TextButton(
                                    onClick = { showAllUpdates = !showAllUpdates },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (showAllUpdates) "Show less" else "Show ${allUpdates.size - initialUpdateCount} more updates",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            imageVector = if (showAllUpdates)
                                                Icons.Default.KeyboardArrowUp
                                            else
                                                Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (showAllUpdates) "Collapse" else "Expand",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Empty state (no data at all)
                    if (programState.data == null && nextLaunchState.data == null &&
                        updatesState.data.isEmpty() && !programState.isLoading
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Starship data available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Indicator shown when displaying stale cached data
 */
@Composable
internal fun StalenessIndicator(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Showing cached data • Pull to refresh",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Card displaying program information
 */
@Composable
private fun ProgramInfoCard(
    program: me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Program image if available
            program.image?.imageUrl?.let { imageUrl ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = program.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            Text(
                text = program.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            program.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
