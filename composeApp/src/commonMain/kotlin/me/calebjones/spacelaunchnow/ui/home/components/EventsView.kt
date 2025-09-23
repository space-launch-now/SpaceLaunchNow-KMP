package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CalendarDay
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.navigation.EventDetail
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme as M3
import me.calebjones.spacelaunchnow.ui.detail.compose.snackDetailBoundsTransform
import me.calebjones.spacelaunchnow.ui.SharedElementType
import me.calebjones.spacelaunchnow.ui.EventSharedElementKey
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalNavAnimatedVisibilityScope

@Composable
fun EventsView(navController: NavController) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val events by homeViewModel.events.collectAsState()
    val isLoading by homeViewModel.isEventsLoading.collectAsState()
    val error by homeViewModel.eventsError.collectAsState()

    when {
        isLoading && events.isEmpty() -> {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) {
                    EventItemShimmer()
                }
            }
        }
        error != null -> {
            EventErrorCard(error = error!!)
        }
        events.isNotEmpty() -> {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    EventItem(event = event, navController = navController)
                }
            }
        }
        else -> {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(all = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) {
                    EventItemShimmer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EventItem(event: EventEndpointNormal, navController: NavController) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Card(
                onClick = { navController.navigate(EventDetail(event.id)) },
                modifier = Modifier
                    .width(280.dp)
                    .sharedBounds(
                        rememberSharedContentState(
                            key = EventSharedElementKey(
                                eventId = event.id,
                                type = SharedElementType.Bounds,
                            ),
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = snackDetailBoundsTransform,
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Event image with icon fallback
                    val image = event.image?.imageUrl
                    SubcomposeAsyncImage(
                        model = image,
                        contentDescription = "Event Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .sharedBounds(
                                rememberSharedContentState(
                                    key = EventSharedElementKey(
                                        eventId = event.id,
                                        type = SharedElementType.Image,
                                    ),
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = snackDetailBoundsTransform,
                            ),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        error = {
                            // Show Font Awesome calendar icon as fallback
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.CalendarDay,
                                    contentDescription = "Event Icon",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Event title
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.sharedBounds(
                            rememberSharedContentState(
                                key = EventSharedElementKey(
                                    eventId = event.id,
                                    type = SharedElementType.Title,
                                ),
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = snackDetailBoundsTransform,
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Event description
                    event.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            minLines = 3,
                            overflow = TextOverflow.Clip,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Event type
                    Text(
                        text = event.type.name ?: "Unknown Event",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium
                    )

                    // Event date (if available)
                    event.date?.let { date ->
                        Text(
                            text = formatLaunchDateTime(date),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            return
        }
    }
    Card(
        onClick = { navController.navigate(EventDetail(event.id)) },
        modifier = Modifier.width(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Event image with icon fallback
            val image = event.image?.imageUrl
            SubcomposeAsyncImage(
                model = image,
                contentDescription = "Event Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    // Show Font Awesome calendar icon as fallback
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.CalendarDay,
                            contentDescription = "Event Icon",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Event title
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Event description
            event.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    minLines = 3,
                    overflow = TextOverflow.Clip,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Event type
            Text(
                text = event.type.name ?: "Unknown Event",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium
            )
            
            // Event date (if available)
            event.date?.let { date ->
                Text(
                    text = formatLaunchDateTime(date),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun EventItemShimmer() {
    Card(
        modifier = Modifier
            .width(280.dp)
            .shimmer(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Shimmer image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Shimmer title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Shimmer description placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Shimmer type placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(10.dp)
            )
        }
    }
}

@Composable
fun EventErrorCard(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                text = "Failed to load events",
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
