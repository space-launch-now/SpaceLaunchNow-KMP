package me.calebjones.spacelaunchnow.ui.roadmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.calebjones.spacelaunchnow.data.model.RoadmapData
import me.calebjones.spacelaunchnow.data.model.RoadmapItem
import me.calebjones.spacelaunchnow.data.model.RoadmapStatus
import org.koin.compose.viewmodel.koinViewModel
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_error_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_last_updated
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_loading
import spacelaunchnow_kmp.composeapp.generated.resources.common_back
import spacelaunchnow_kmp.composeapp.generated.resources.common_retry
import spacelaunchnow_kmp.composeapp.generated.resources.common_roadmap
import org.jetbrains.compose.resources.stringResource

/**
 * Roadmap screen showing planned features and development timeline
 * 
 * Features:
 * - Timeline-style layout with status indicators
 * - Categorized roadmap items
 * - Status badges (Completed, In Progress, Planned, Considering)
 * - Remotely configurable via Firebase (see ViewModel for setup)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoadmapViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Fallback items are resolved here because their text comes from string resources.
    val roadmapData = uiState.placeholderData?.toRoadmapData() ?: uiState.roadmapData

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.common_roadmap),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.errorMessage != null -> {
                    ErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
                roadmapData != null -> {
                    RoadmapContent(
                        roadmapData = roadmapData
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LoadingIndicator()
            Text(
                text = stringResource(Res.string.app_roadmap_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(Res.string.app_roadmap_error_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            androidx.compose.material3.Button(onClick = onRetry) {
                Text(stringResource(Res.string.common_retry))
            }
        }
    }
}

@Composable
private fun RoadmapContent(
    roadmapData: me.calebjones.spacelaunchnow.data.model.RoadmapData
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header message
        item {
            Spacer(Modifier.height(8.dp))
            
            roadmapData.message?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            
            roadmapData.lastUpdated?.let { updated ->
                Text(
                    text = stringResource(Res.string.app_roadmap_last_updated, updated),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Roadmap items with timeline
        items(roadmapData.items.size) { index ->
            val item = roadmapData.items[index]
            val isLast = index == roadmapData.items.size - 1
            RoadmapTimelineItem(
                item = item,
                isLast = isLast
            )
        }

        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RoadmapTimelineItem(
    item: RoadmapItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(item.status)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getStatusIcon(item.status),
                    contentDescription = stringResource(item.status.label),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Timeline line - show unless it's the last item
            if (!isLast) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }

        // Content card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, bottom = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header with title and status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        StatusBadge(status = item.status)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Description
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // Footer with quarter and category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.quarter,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Text(
                            text = stringResource(item.category.label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: RoadmapStatus) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(getStatusColor(status).copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(status.label),
            style = MaterialTheme.typography.labelSmall,
            color = getStatusColor(status),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun getStatusColor(status: RoadmapStatus): Color {
    return when (status) {
        RoadmapStatus.COMPLETED -> Color(0xFF4CAF50) // Green
        RoadmapStatus.IN_TESTING -> Color(0xFF9C27B0) // Purple
        RoadmapStatus.IN_PROGRESS -> Color(0xFF2196F3) // Blue
        RoadmapStatus.PLANNED -> Color(0xFFFFC107) // Amber
        RoadmapStatus.BACKLOG -> Color(0xFF9E9E9E) // Grey
    }
}

private fun getStatusIcon(status: RoadmapStatus) = when (status) {
    RoadmapStatus.COMPLETED -> Icons.Filled.CheckCircle
    RoadmapStatus.IN_TESTING -> Icons.Filled.Science
    RoadmapStatus.IN_PROGRESS -> Icons.Filled.Pending
    RoadmapStatus.PLANNED -> Icons.Filled.Schedule
    RoadmapStatus.BACKLOG -> Icons.Filled.ThumbUp
}

@Composable
private fun PlaceholderRoadmapData.toRoadmapData(): RoadmapData = RoadmapData(
    items = items.map { item ->
        RoadmapItem(
            id = item.id,
            title = stringResource(item.title),
            description = stringResource(item.description),
            status = item.status,
            quarter = stringResource(item.quarter),
            category = item.category,
            priority = item.priority
        )
    },
    lastUpdated = stringResource(lastUpdated),
    message = stringResource(message)
)
