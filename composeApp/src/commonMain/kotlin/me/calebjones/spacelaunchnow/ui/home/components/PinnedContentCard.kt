package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.PinnedContentData
import me.calebjones.spacelaunchnow.ui.viewmodel.PinnedEventContent
import me.calebjones.spacelaunchnow.ui.viewmodel.PinnedLaunchContent
import me.calebjones.spacelaunchnow.ui.viewmodel.PinnedMotdContent
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Featured/Pinned content color - Amber 600 (#FFB300)
 * Used for pinned content card visual distinction.
 */
private val FeaturedAmber = Color(0xFFFFB300)

/**
 * Card displaying a pinned/featured launch or event from Firebase Remote Config.
 * 
 * Features:
 * - Amber border (Featured status color)
 * - "FEATURED" chip with optional custom message
 * - Same layout as LiveLaunchCard for consistency
 * - Medium card height (130dp) matching LiveLaunchCard
 * - Clickable - navigates to launch or event detail page
 */
@Composable
fun PinnedContentCard(
    pinnedContent: PinnedContentData,
    navController: NavController,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get title based on content type
    val title = when (pinnedContent) {
        is PinnedLaunchContent -> LaunchFormatUtil.formatLaunchTitle(pinnedContent.launch)
        is PinnedEventContent -> pinnedContent.event.name ?: "Unknown Event"
        is PinnedMotdContent -> pinnedContent.name
    }

    // Get the mission/subtitle text
    val subtitle = pinnedContent.customMessage ?: when (pinnedContent) {
        is PinnedLaunchContent -> pinnedContent.launch.mission?.name ?: pinnedContent.launch.name ?: "Unknown Mission"
        is PinnedEventContent -> pinnedContent.event.type?.name ?: "Event"
        is PinnedMotdContent -> ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp)
            .clickable {
                when (pinnedContent) {
                    is PinnedLaunchContent -> navController.navigate(LaunchDetail(pinnedContent.launch.id))
                    is PinnedEventContent -> navController.navigate(EventDetail(pinnedContent.event.id))
                    is PinnedMotdContent -> Unit
                }
            }
            .semantics {
                contentDescription = "Featured: $title. ${pinnedContent.customMessage ?: ""}. Tap to view details."
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = FeaturedAmber.copy(alpha = 0.08f)
        ),
        border = BorderStroke(2.dp, FeaturedAmber),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left side: Image thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(130.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                ) {
                    pinnedContent.imageUrl?.let { url ->
                    SubcomposeAsyncImage(
                        model = url,
                        contentDescription = "Launch image for $title",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmer()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CustomIcons.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CustomIcons.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                } ?: run {
                    // No image URL - show placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CustomIcons.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Right side: Launch info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Mission name or custom message
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Location
                    pinnedContent.location?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom row: FEATURED chip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FEATURED chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FeaturedAmber,
                        contentColor = Color.Black
                    ) {
                        Text(
                            text = "FEATURED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
        
            // Dismiss button in top-right corner
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss featured launch",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Dismissable banner for a Message of the Day from Firebase Remote Config.
 *
 * Displayed at the very top of the home screen as a slim, full-width info strip.
 * Unlike [PinnedContentCard] this banner carries no image and is not tappable
 * (there is nothing to navigate to).
 */
@Composable
fun MessageOfTheDayBanner(
    motd: PinnedMotdContent,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = motd.customMessage ?: return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { contentDescription = "Message of the Day: $message" },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss message",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Composable to render the pinned/featured content section on home screen.
 * Shows nothing if there is no pinned content.
 * Dispatches to [MessageOfTheDayBanner] for MOTD content and [PinnedContentCard] for
 * launch/event content.
 */
@Composable
fun PinnedContentSection(
    pinnedContent: PinnedContentData?,
    navController: NavController,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pinnedContent != null) {
        Column(modifier = modifier) {
            when (pinnedContent) {
                is PinnedMotdContent -> MessageOfTheDayBanner(
                    motd = pinnedContent,
                    onDismiss = onDismiss
                )
                else -> PinnedContentCard(
                    pinnedContent = pinnedContent,
                    navController = navController,
                    onDismiss = onDismiss
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ========================================
// Previews (without NavController)
// ========================================

@Composable
private fun PinnedContentCardPreviewContent(
    pinnedContent: PinnedLaunchContent
) {
    val launch = pinnedContent.launch
    val title by remember(launch) {
        mutableStateOf(LaunchFormatUtil.formatLaunchTitle(launch))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp)
            .semantics {
                contentDescription = "Featured launch: $title. ${pinnedContent.customMessage ?: ""}. Tap to view details."
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = FeaturedAmber.copy(alpha = 0.08f)
        ),
        border = BorderStroke(2.dp, FeaturedAmber),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left side: Launch image placeholder for preview
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CustomIcons.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            // Right side: Launch info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Mission name or custom message
                    Text(
                        text = pinnedContent.customMessage 
                            ?: launch.mission?.name 
                            ?: launch.name 
                            ?: "Unknown Mission",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Location
                    launch.pad?.location?.name?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom row: FEATURED chip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FEATURED chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FeaturedAmber,
                        contentColor = Color.Black
                    ) {
                        Text(
                            text = "FEATURED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PinnedContentCardPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinnedContentCardPreviewContent(
                pinnedContent = PreviewData.pinnedLaunchContent
            )
        }
    }
}

@Preview
@Composable
private fun PinnedContentCardWithCustomMessagePreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinnedContentCardPreviewContent(
                pinnedContent = PreviewData.pinnedLaunchContentWithMessage
            )
        }
    }
}

@Preview
@Composable
private fun PinnedContentCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinnedContentCardPreviewContent(
                pinnedContent = PreviewData.pinnedLaunchContent
            )
        }
    }
}

@Preview
@Composable
private fun PinnedContentCardWithCustomMessageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinnedContentCardPreviewContent(
                pinnedContent = PreviewData.pinnedLaunchContentWithMessage
            )
        }
    }
}

@Preview
@Composable
private fun MessageOfTheDayBannerPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                MessageOfTheDayBanner(
                    motd = PreviewData.motdContent,
                    onDismiss = {}
                )
            }
        }
    }
}

@Preview
@Composable
private fun MessageOfTheDayBannerDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                MessageOfTheDayBanner(
                    motd = PreviewData.motdContent,
                    onDismiss = {}
                )
            }
        }
    }
}
