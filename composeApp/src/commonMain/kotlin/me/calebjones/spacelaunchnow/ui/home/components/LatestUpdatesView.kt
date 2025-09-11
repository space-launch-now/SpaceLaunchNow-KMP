package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.ui.compose.UpdatesShimmer
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LatestUpdatesView(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val updates by homeViewModel.updates.collectAsState()
    val isLoading by homeViewModel.isUpdatesLoading.collectAsState()
    val error by homeViewModel.updatesError.collectAsState()


    LaunchedEffect(Unit) {
        if (updates.isEmpty() && !isLoading && error == null) {
            homeViewModel.loadUpdates(10)
        }
    }

    Column(modifier = modifier) {
        when {
            error != null -> {
                ErrorCard(error = error!!)
            }
            updates.isNotEmpty() -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(all = 16.dp)
                ) {
                    items(updates) { update ->
                        UpdateCard(update = update)
                    }
                }
            }
            else -> {
                UpdatesShimmer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCard(
    update: UpdateEndpoint,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(280.dp, 120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Author profile image
                AsyncImage(
                    model = update.profileImage ?: "",
                    contentDescription = "Author profile",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    fallback = rememberAsyncImagePainter(
                        model = "https://via.placeholder.com/48x48/CCCCCC/000000?text=${update.createdBy?.firstOrNull()?.uppercaseChar() ?: "?"}"
                    )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Author name
                    Text(
                        text = update.createdBy ?: "Unknown",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Update subject (what it's about)
                    val updateSubject: String? = when {
                        update.launch != null -> update.launch.name
                        update.event != null -> update.event.name
                        update.program != null -> update.program.name
                        else -> "General Update"
                    }

                    if (updateSubject != null) {
                        Text(
                            text = updateSubject,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Comment
                    Text(
                        text = update.comment ?: "No comment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 8.dp)
                    )
                }
            }
            
            // Created date aligned to bottom right corner
            update.createdOn?.let { createdOn ->
                Text(
                    text = formatUpdateDate(createdOn),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
fun UpdateLoadingCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(280.dp, 120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile image placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Loading placeholders
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(6.dp)),
                )
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(7.dp)),
                )
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(7.dp)),
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    error: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Failed to load updates",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EmptyUpdatesCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No updates available",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatUpdateDate(createdOn: Instant): String {
    // Simple date formatting - you might want to use a more sophisticated formatter
    return try {
        val now = Clock.System.now()
        val duration = now - createdOn
        when {
            duration.inWholeDays > 0 -> "${duration.inWholeDays}d ago"
            duration.inWholeHours > 0 -> "${duration.inWholeHours}h ago"
            duration.inWholeMinutes > 0 -> "${duration.inWholeMinutes}m ago"
            else -> "now"
        }
    } catch (e: Exception) {
        "recently"
    }
}
