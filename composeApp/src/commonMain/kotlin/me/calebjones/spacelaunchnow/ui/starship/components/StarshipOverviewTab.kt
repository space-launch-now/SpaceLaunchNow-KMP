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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.ui.compose.StarshipOverviewShimmer
import me.calebjones.spacelaunchnow.ui.detail.compose.components.VideoPlayer
import me.calebjones.spacelaunchnow.ui.home.components.LaunchItemView
import me.calebjones.spacelaunchnow.ui.home.components.SectionTitle
import me.calebjones.spacelaunchnow.ui.home.components.UpdateCard
import me.calebjones.spacelaunchnow.ui.layout.rememberAdaptiveLayoutState
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
    historyLaunchesState: ViewState<List<LaunchNormal>>,
    updatesState: ViewState<List<UpdateEndpoint>>,
    videoPlayerState: VideoPlayerState,
    navController: NavController,
    onRefresh: () -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allUpdates = updatesState.data

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
                val isLargeScreen = rememberAdaptiveLayoutState().isMediumOrLarger
                
                if (isLargeScreen) {
                    // Tablet layout: side-by-side video + program info
                    TabletOverviewLayout(
                        programState = programState,
                        nextLaunchState = nextLaunchState,
                        historyLaunchesState = historyLaunchesState,
                        updatesState = updatesState,
                        videoPlayerState = videoPlayerState,
                        navController = navController,
                        onSetPlayerVisible = onSetPlayerVisible,
                        onVideoSelected = onVideoSelected,
                        onNavigateToFullscreen = onNavigateToFullscreen
                    )
                } else {
                    // Phone layout: stacked vertical
                    PhoneOverviewLayout(
                        programState = programState,
                        nextLaunchState = nextLaunchState,
                        historyLaunchesState = historyLaunchesState,
                        allUpdates = allUpdates,
                        videoPlayerState = videoPlayerState,
                        navController = navController,
                        onSetPlayerVisible = onSetPlayerVisible,
                        onVideoSelected = onVideoSelected,
                        onNavigateToFullscreen = onNavigateToFullscreen
                    )
                }
            }
        }
    }
}

/**
 * Tablet layout with side-by-side video player and program info
 */
@Composable
private fun TabletOverviewLayout(
    programState: ViewState<ProgramNormal?>,
    nextLaunchState: ViewState<LaunchNormal?>,
    historyLaunchesState: ViewState<List<LaunchNormal>>,
    updatesState: ViewState<List<UpdateEndpoint>>,
    videoPlayerState: VideoPlayerState,
    navController: NavController,
    onSetPlayerVisible: (Boolean) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    val allUpdates = updatesState.data
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top spacer for tablet
        Spacer(modifier = Modifier.height(24.dp))
        
        // Staleness indicator (if showing stale data)
        if (programState.isStale || nextLaunchState.isStale) {
            StalenessIndicator(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Side-by-side: Video (60%) + Program Info (40%)
        val hasVideo = videoPlayerState.availableVideos.isNotEmpty()
        val hasProgram = programState.data != null
        
        if (hasVideo || hasProgram) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Video Player (60%) - wrapped in card on tablet
                if (hasVideo) {
                    Card(
                        modifier = Modifier.weight(0.6f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        VideoPlayer(
                            videoPlayerState = videoPlayerState,
                            launchName = programState.data?.name ?: "Starship",
                            onSetPlayerVisible = onSetPlayerVisible,
                            onNavigateToFullscreen = onNavigateToFullscreen,
                            onVideoSelected = onVideoSelected
                        )
                    }
                }
                
                // Program Info Card (40%)
                programState.data?.let { program ->
                    ProgramInfoCard(
                        program = program,
                        modifier = if (hasVideo) Modifier.weight(0.4f) else Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Side-by-side: Timeline (60%) + Updates (40%)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Program Timeline Section (60%)
            Column(modifier = Modifier.weight(0.6f)) {
                StarshipHistoryTimeline(
                    nextLaunch = nextLaunchState.data,
                    historyLaunchesState = historyLaunchesState,
                    onLaunchClick = { launchId ->
                        navController.navigate(LaunchDetail(launchId))
                    }
                )
            }
            
            // Updates Section (40%) - vertical list on tablet
            Column(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(
                    title = "Updates",
                    hasAction = false
                )
                
                if (allUpdates.isNotEmpty()) {
                    allUpdates.forEach { update ->
                        UpdateCard(
                            update = update,
                            navController = navController,
                            fillMaxWidth = true
                        )
                    }
                } else {
                    Text(
                        text = "No updates available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Empty state (no data at all)
        if (programState.data == null && nextLaunchState.data == null &&
            updatesState.data.isEmpty() && !programState.isLoading
        ) {
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

/**
 * Phone layout with stacked vertical content
 */
@Composable
private fun PhoneOverviewLayout(
    programState: ViewState<ProgramNormal?>,
    nextLaunchState: ViewState<LaunchNormal?>,
    historyLaunchesState: ViewState<List<LaunchNormal>>,
    allUpdates: List<UpdateEndpoint>,
    videoPlayerState: VideoPlayerState,
    navController: NavController,
    onSetPlayerVisible: (Boolean) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit
) {
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

        // Status Updates Section (horizontal scrolling)
        if (allUpdates.isNotEmpty()) {
            item {
                Column {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionTitle(
                            title = "Updates",
                            hasAction = false
                        )
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(allUpdates) { update ->
                            UpdateCard(
                                update = update,
                                navController = navController,
                                fillMaxWidth = false
                            )
                        }
                    }
                }
            }
        }

        // Program Timeline Section
        item {
            StarshipHistoryTimeline(
                nextLaunch = nextLaunchState.data,
                historyLaunchesState = historyLaunchesState,
                onLaunchClick = { launchId ->
                    navController.navigate(LaunchDetail(launchId))
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Empty state (no data at all)
        if (programState.data == null && nextLaunchState.data == null &&
            allUpdates.isEmpty() && !programState.isLoading
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
