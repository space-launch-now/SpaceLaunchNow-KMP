package me.calebjones.spacelaunchnow.ui.starship.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch

/**
 * Card for spacecraft configuration (category level)
 */
@Composable
internal fun SpacecraftConfigCard(
    config: SpacecraftConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFullWidth: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (isFullWidth) 2f else 1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            config.imageUrl?.let { imageUrl ->
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = config.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        // Use shimmer instead of CircularProgressIndicator for warm start perf
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .shimmer()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CustomIcons.RocketLaunch,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        }
                    }
                )
            }

            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    config.agency?.name?.let { agencyName ->
                        Text(
                            text = agencyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card for launcher configuration (category level)
 */
@Composable
internal fun LauncherConfigCard(
    config: VehicleConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFullWidth: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (isFullWidth) 2f else 1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            config.imageUrl?.let { imageUrl ->
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = config.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        // Use shimmer instead of CircularProgressIndicator for warm start perf
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .shimmer()
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CustomIcons.RocketLaunch,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        }
                    }
                )
            }

            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = config.fullName ?: config.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    config.manufacturerName?.let { manufacturerName ->
                        Text(
                            text = manufacturerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Launch count badge
                    config.totalLaunchCount?.let { count ->
                        Text(
                            text = "$count launches",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shimmer placeholder for config cards
 */
@Composable
internal fun ConfigCardShimmer(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}
