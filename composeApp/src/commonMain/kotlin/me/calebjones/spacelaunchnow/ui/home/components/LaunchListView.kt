package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Mission
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramMini
import me.calebjones.spacelaunchnow.ui.compose.LaunchCardHeader
import me.calebjones.spacelaunchnow.ui.compose.LaunchListShimmer
import me.calebjones.spacelaunchnow.ui.compose.toLaunchCardData
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor

// Constants for layout dimensions
private val CARD_WIDTH = 340.dp
private val CARD_HEIGHT = 240.dp
private val CARD_SPACING = 16.dp

@Composable
fun LaunchListView(viewModel: HomeViewModel) {
    val launches by viewModel.upcomingLaunches.collectAsState()
    val error by viewModel.upcomingLaunchesError.collectAsState()
    val isLoading by viewModel.isUpcomingLaunchesLoading.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Track if we're currently dragging
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (launches.isEmpty() && !isLoading && error == null) {
            viewModel.loadUpcomingLaunches(limit = 10)
        }
    }

    if (error != null) {
        LaunchListErrorCard(
            error = error!!,
            onRetry = { viewModel.loadUpcomingLaunches(limit = 10, forceRefresh = true) }
        )
    } else if (launches.isNotEmpty()) {
        val launchNormalList = launches

        LazyRow(
            modifier = Modifier.fillMaxWidth().draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    isDragging = true
                    coroutineScope.launch {
                        scrollState.scrollBy(-delta)
                    }
                },
                onDragStopped = {
                    // When dragging stops, snap to the closest item
                    isDragging = false
                    coroutineScope.launch {
                        val itemWidth = 340 + 16 // Card width (340dp) + spacing (16dp)
                        val firstVisibleItemIndex = scrollState.firstVisibleItemIndex
                        val firstVisibleItemOffset = scrollState.firstVisibleItemScrollOffset
                        
                        // Calculate if we should snap forward or backward
                        val snapForward = firstVisibleItemOffset > itemWidth / 2
                        
                        if (snapForward) {
                            // Snap to the next item
                            scrollState.animateScrollToItem(firstVisibleItemIndex + 1)
                        } else {
                            // Snap to the current item
                            scrollState.animateScrollToItem(firstVisibleItemIndex)
                        }
                    }
                }
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            state = scrollState,
                
        ) {
            items(launchNormalList) { launch ->
                LaunchItemView(launch)
            }
        }
    } else if (isLoading) {
        LaunchListShimmer()
    }
}

@Composable
fun LaunchItemView(launch: LaunchNormal) {
    Card(
        modifier = Modifier
            .size(width = 340.dp, height = 240.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Background Image
            launch.image?.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Launch Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Semi-transparent overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            )

            // Use the common LaunchCardHeader composable
            LaunchCardHeader(
                launchData = launch.toLaunchCardData(),
                showAgencyLogo = true,
                logoSize = 56.dp,
                modifier = Modifier.fillMaxSize()
            )

            // Chips in bottom right corner
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Program chip (left side)
                ProgramChip(
                    programs = launch.program,
                    modifier = Modifier
                )
                
                // Mission type chip (middle)
                MissionTypeChip(
                    mission = launch.mission,
                    modifier = Modifier
                )
                
                // Status chip (right side)
                LaunchStatusChip(
                    status = launch.status,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun LaunchStatusChip(
    status: LaunchStatus?,
    modifier: Modifier = Modifier
) {
    if (status != null) {
        val statusColor = getLaunchStatusColor(status.id)
        
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = statusColor.copy(),
            contentColor = Color.White
        ) {
            Text(
                text = status.abbrev?.uppercase() ?: status.name.uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun MissionTypeChip(
    mission: Mission?,
    modifier: Modifier = Modifier
) {
    if (mission != null) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Text(
                text = mission.type,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun ProgramChip(
    programs: List<ProgramMini>?,
    modifier: Modifier = Modifier
) {
    val program = programs?.firstOrNull()
    if (program != null) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ) {
            Text(
                text = program.name,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun LaunchListErrorCard(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Failed to load launches",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Retry")
            }
        }
    }
}
