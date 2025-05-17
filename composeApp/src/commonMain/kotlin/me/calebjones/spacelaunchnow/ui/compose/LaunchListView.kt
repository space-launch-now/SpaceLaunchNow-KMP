package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.client.models.LaunchNormal
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel

// Constants for layout dimensions
private val CARD_WIDTH = 340.dp
private val CARD_HEIGHT = 240.dp
private val CARD_SPACING = 16.dp

fun getStatusColor(launch: LaunchNormal): Color {
    return when (launch.status?.id) {
        1 -> {
            Color.Green
        }
        2 -> {
            Color.Red
        }
        3 ->{
            Color.Green
        }
        4 ->{
            Color.Red
        }
        5 -> {
            Color.Yellow
        }
        6 -> {
            Color.Magenta
        }
        7 -> {
            Color.Red
        }
        8 -> {
            Color.Yellow
        }
        9 -> {
            Color.Green
        }
        else -> {
            Color.Red
        }
    }

}

@Composable
fun LaunchListView(viewModel: LaunchViewModel) {    val launches by viewModel.upcomingLaunches.collectAsState()
    val error by viewModel.error.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Track if we're currently dragging
    var isDragging by remember { mutableStateOf(false) }
    // Track the last scroll position to determine scroll direction
    var lastScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.fetchUpcomingLaunches(limit = 10)
    }

    if (error != null) {
        Text(text = "Error: $error")
    } else if (launches != null) {
        val launchNormalList = launches!!.results.filterIsInstance<LaunchNormal>().drop(1)

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
                LaunchItemView(launch, viewModel)
            }
        }
    } else {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun LaunchItemView(launch: LaunchNormal, viewModel: LaunchViewModel) {
    val agencyId = launch.launchServiceProvider?.id ?: return

    // Fetch agency data for this specific launch
    LaunchedEffect(agencyId) {
        viewModel.fetchAgencyData(agencyId)
    }

    // Observe the agency data for this specific launch
    val agencyDataMap by viewModel.agencyDataMap.collectAsState()
    val agencyData = agencyDataMap[agencyId]

    val agencyLogo = agencyData?.socialLogo?.imageUrl ?: launch.image?.imageUrl

    // Compute the title based on the provided logic
    val title by remember(launch) {
        mutableStateOf(
            if (launch.rocket?.configuration != null) {
                val lsp = launch.launchServiceProvider
                val providerName = if (
                    lsp.name.length > 15 &&
                    !lsp.abbrev.isNullOrEmpty()
                ) {
                    lsp.abbrev
                } else {
                    lsp.name
                }
                "$providerName | ${launch.rocket.configuration.name}"
            } else if (launch.name.isNotEmpty()) {
                launch.name
            } else {
                "Unknown Name"
            }
        )
    }

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
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
            )

            Row {
                // Agency Logo
                agencyLogo?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Agency Logo",
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp)
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, getStatusColor(launch), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Overlay with Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Display the computed title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    launch.pad?.location?.name?.let { locationName ->
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    launch.net?.let { launchNet ->
                        Text(
                            text = launchNet.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
