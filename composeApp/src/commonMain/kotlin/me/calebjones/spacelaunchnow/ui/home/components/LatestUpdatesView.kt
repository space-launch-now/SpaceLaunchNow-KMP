package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.InfoCircle
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.NetPrecision
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.ui.compose.UpdatesShimmer
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun LatestUpdatesView(
    state: ViewState<List<UpdateEndpoint>>,
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    Column {
        when {
            // STATE 4: Error State - show cached data with banner OR just error
            state.error != null -> {
                if (state.data.isNotEmpty()) {
                    // Show stale data with error indicator
                    Column {
                        // Error banner
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Showing cached data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        // Show stale updates
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(all = 16.dp)
                        ) {
                            items(state.data) { update ->
                                UpdateCard(update = update, navController = navController)
                            }
                        }
                    }
                } else {
                    // No cached data, just show error
                    ErrorCard(error = state.error!!)
                }
            }

            // STATE 2 & 3: Loading with existing data
            state.isLoading && state.data.isNotEmpty() -> {
                Box {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(all = 16.dp)
                    ) {
                        items(state.data) { update ->
                            UpdateCard(update = update, navController = navController)
                        }
                    }

                    // Show loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(24.dp)
                    )
                }
            }

            // Data available (not loading)
            state.data.isNotEmpty() -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(all = 16.dp)
                ) {
                    items(state.data) { update ->
                        UpdateCard(update = update, navController = navController)
                    }
                }
            }

            // STATE 1: Fresh load, no data - show shimmer
            state.isLoading -> {
                UpdatesShimmer()
            }

            // Fallback
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
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    fillMaxWidth: Boolean = false
) {
    val comment = update.comment ?: "No comment"
    val uriHandler = LocalUriHandler.current

    val cardModifier = if (fillMaxWidth) {
        modifier
            .fillMaxWidth()
            .height(100.dp)
    } else {
        modifier.size(320.dp, 100.dp)
    }

    Card(
        modifier = cardModifier
            .clickable {
                if (navController != null) {
                    if (update.launch != null) {
                        navController.navigate(LaunchDetail(update.launch.id))
                    } else if (update.event != null) {
                        navController.navigate(EventDetail(update.event.id))
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Author profile image
                    SubcomposeAsyncImage(
                        model = update.profileImage ?: "",
                        contentDescription = "Author profile",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Profile placeholder",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = update.createdBy ?: "Unknown",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            update.createdOn?.let { createdOn ->
                                Text(
                                    text = formatUpdateDate(createdOn),
                                    fontSize = 10.sp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.7f
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        // Update subject (what it's about)
                        val updateSubject: String? = when {
                            update.launch != null -> update.launch.name
                            update.event != null -> update.event.name
                            update.program != null -> update.program.name
                            else -> null
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
                            text = comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (updateSubject.isNullOrEmpty()) 3 else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )

                    }
                }
                // Source link at bottom with icon and URL
                update.infoUrl?.let { url ->
                    Row(

                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Throwable) {
                                // Silently handle URL opening errors
                            }
                        }
                            .fillMaxWidth()
                            .padding(start = 56.dp)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.InfoCircle,
                            contentDescription = "Source",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = url,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                        .shimmer()
                        .height(12.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(6.dp)),
                )
                Box(
                    modifier = Modifier
                        .shimmer()
                        .height(14.dp)
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(7.dp)),
                )
                Box(
                    modifier = Modifier
                        .shimmer()
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
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Failed to load updates",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )

            onRetry?.let { retry ->
                Button(
                    onClick = retry,
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

@OptIn(ExperimentalTime::class)
private fun formatUpdateDate(createdOn: Instant): String {
    // Simple date formatting - you might want to use a more sophisticated formatter
    return try {
        val now = System.now()
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

// region Preview Data

/**
 * Sample updates for preview purposes
 */
private val sampleUpdateWithInfoUrl = UpdateEndpoint(
    id = 1,
    profileImage = null,
    comment = "Vehicle is vertical on the pad. Weather looking good for tomorrow's launch attempt. All systems nominal.",
    infoUrl = "https://example.com/update1",
    createdBy = "SpaceX",
    launch = null,
    event = null,
    program = null,
    createdOn = System.now() - 30.minutes
)

private val sampleUpdateWithoutInfoUrl = UpdateEndpoint(
    id = 2,
    profileImage = null,
    comment = "Static fire test completed successfully. All systems nominal.",
    infoUrl = null,
    createdBy = "Launch Director",
    launch = null,
    event = null,
    program = null,
    createdOn = System.now() - 2.hours
)

private val sampleUpdateLongComment = UpdateEndpoint(
    id = 3,
    profileImage = null,
    comment = "Weather forecast updated: 80% chance of favorable conditions for launch window. Upper level winds are within acceptable limits. Ground weather team confirms go for propellant loading.",
    infoUrl = "https://example.com/weather",
    createdBy = "45th Weather Squadron",
    launch = LaunchBasic(
        id = "1",
        name = "Falcon 9 Test Flight",
        status = LaunchStatus(
            id = 1,
            name = "Test"
        ),
        net = System.now() - 3.days,
        windowEnd = null,
        windowStart = null,
        url = "TODO()",
        slug = "TODO()",
        launchDesignator = "TODO()",
        netPrecision = NetPrecision(
            id = 1
        ),
        image = null,
        launchServiceProvider = AgencyMini(
            id = 1,
            url = "TODO()",
            name = "TODO",
            type = AgencyType(
                id = 1
            )
        ),
        infographic = "TODO()",
        locationName = "TODO()",
    ),
    event = null,
    program = null,
    createdOn = System.now() - 1.days
)

private val sampleUpdateOld = UpdateEndpoint(
    id = 4,
    profileImage = null,
    comment = "Payload integration complete. Fairing encapsulation scheduled for tomorrow.",
    infoUrl = null,
    createdBy = "Mission Integration Team",
    launch = LaunchBasic(
        id = "1",
        name = "Falcon 9 Test Flight",
        status = LaunchStatus(
            id = 1,
            name = "Test"
        ),
        net = System.now() - 3.days,
        windowEnd = null,
        windowStart = null,
        url = "TODO()",
        slug = "TODO()",
        launchDesignator = "TODO()",
        netPrecision = NetPrecision(
            id = 1
        ),
        image = null,
        launchServiceProvider = AgencyMini(
            id = 1,
            url = "TODO()",
            name = "TODO",
            type = AgencyType(
                id = 1
            )
        ),
        infographic = "TODO()",
        locationName = "TODO()",
    ),
    event = null,
    program = null,
    createdOn = System.now() - 5.days
)

// endregion

// region Previews

@Preview
@Composable
private fun UpdateCardWithInfoUrlPreview() {
    MaterialTheme {
        UpdateCard(
            update = sampleUpdateWithInfoUrl,
            navController = null,
            fillMaxWidth = false
        )
    }
}

@Preview
@Composable
private fun UpdateCardWithoutInfoUrlPreview() {
    MaterialTheme {
        UpdateCard(
            update = sampleUpdateWithoutInfoUrl,
            navController = null,
            fillMaxWidth = false
        )
    }
}

@Preview
@Composable
private fun UpdateCardFullWidthWithInfoUrlPreview() {
    MaterialTheme {
        UpdateCard(
            update = sampleUpdateWithInfoUrl,
            navController = null,
            fillMaxWidth = true
        )
    }
}

@Preview
@Composable
private fun UpdateCardFullWidthWithoutInfoUrlPreview() {
    MaterialTheme {
        UpdateCard(
            update = sampleUpdateWithoutInfoUrl,
            navController = null,
            fillMaxWidth = true
        )
    }
}

@Preview
@Composable
private fun UpdateCardLongCommentPreview() {
    MaterialTheme {
        UpdateCard(
            update = sampleUpdateLongComment,
            navController = null,
            fillMaxWidth = true
        )
    }
}

@Preview
@Composable
private fun UpdateCardOldDatePreview() {
    MaterialTheme {
        UpdateCard(
            update = sampleUpdateOld,
            navController = null,
            fillMaxWidth = false
        )
    }
}

@Preview
@Composable
private fun UpdateLoadingCardPreview() {
    MaterialTheme {
        UpdateLoadingCard()
    }
}

@Preview
@Composable
private fun ErrorCardPreview() {
    MaterialTheme {
        ErrorCard(
            error = "Network connection failed",
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun EmptyUpdatesCardPreview() {
    MaterialTheme {
        EmptyUpdatesCard()
    }
}

// endregion
