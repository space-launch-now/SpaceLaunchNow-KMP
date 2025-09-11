package me.calebjones.spacelaunchnow.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CalendarDay
import compose.icons.fontawesomeicons.solid.Newspaper
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.valentinilk.shimmer.shimmer
import compose.icons.fontawesomeicons.solid.CalendarDay
import compose.icons.fontawesomeicons.solid.CalendarPlus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.compose.LatestUpdatesView
import me.calebjones.spacelaunchnow.ui.compose.LaunchListShimmer
import me.calebjones.spacelaunchnow.ui.compose.LaunchListView
import me.calebjones.spacelaunchnow.ui.compose.NextLaunchView
import me.calebjones.spacelaunchnow.ui.compose.NextUpShimmerBox
import me.calebjones.spacelaunchnow.ui.compose.UpdatesShimmer
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val coroutineScope = rememberCoroutineScope()
    
    // Individual loading states to ensure ALL network calls are complete
    val isFeaturedLaunchLoading by homeViewModel.isFeaturedLaunchLoading.collectAsState()
    val isUpcomingLaunchesLoading by homeViewModel.isUpcomingLaunchesLoading.collectAsState()
    val isUpdatesLoading by homeViewModel.isUpdatesLoading.collectAsState()
    val isArticlesLoading by homeViewModel.isArticlesLoading.collectAsState()
    
    // Wait for ALL network calls to complete (success or error)
    val allNetworkCallsComplete = !isFeaturedLaunchLoading && !isUpcomingLaunchesLoading && !isUpdatesLoading && !isArticlesLoading
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var hasInitiallyLoaded by remember { mutableStateOf(false) }
    val pulseScale = remember { Animatable(1f) }
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            // Don't hide content on refresh - keep it visible
            homeViewModel.refreshHomeScreenData()
        }
    )

    // Load all home screen data when the screen is first displayed
    LaunchedEffect(Unit) {
        homeViewModel.loadHomeScreenData()
    }
    
    // Handle completion of network calls
    LaunchedEffect(allNetworkCallsComplete) {
        if (allNetworkCallsComplete) {
            if (isRefreshing) {
                // Pull-to-refresh completed - trigger pulse animation
                delay(200) // Brief delay to show refresh completed
                isRefreshing = false
                
                // Pulse animation
                pulseScale.animateTo(
                    targetValue = 1.015f,
                    animationSpec = tween(durationMillis = 150)
                )
                pulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 150)
                )
            } else if (!hasInitiallyLoaded) {
                // Initial load - show content with fade-in animation
                delay(100)
                showContent = true
                hasInitiallyLoaded = true
            }
        }
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            // Show loading shimmer only on initial load (not during refresh)
            if (!allNetworkCallsComplete && !hasInitiallyLoaded) {
                // Master loading state - show shimmer for entire screen
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item { HomeTopBar() }
                    
                    item {
                        NextUpShimmerBox()
                    }

                    item { SectionTitle(title = "Upcoming Launches", hasAction = true) }
                    item { LaunchListShimmer(cardCount = 3) }

                    item { SectionTitle(title = "Latest Updates", hasAction = true) }
                    item { UpdatesShimmer(cardCount = 4) }

                    item { SectionTitle(title = "News and Events", hasAction = false) }
                    items(3) { 
                        NewsItemShimmer()
                    }
                }
            } else if (hasInitiallyLoaded || showContent) {
                // Show content either after initial load or during refresh
                val contentModifier = if (hasInitiallyLoaded) {
                    // Apply pulse scale during refresh
                    Modifier
                        .fillMaxSize()
                        .scale(pulseScale.value)
                } else {
                    Modifier.fillMaxSize()
                }
                
                // Content with conditional animation wrapper
                if (hasInitiallyLoaded) {
                    // Already loaded - show content directly with pulse scale
                    LazyColumn(
                        modifier = contentModifier,
                    ) {
                        item { HomeTopBar() }
                        item { NextLaunchView(navController = navController) }
                        item { SectionTitle(title = "Upcoming Launches", hasAction = true) }
                        item { UpcomingHorizontalScrollableList() }
                        item { SectionTitle(title = "Latest Updates", hasAction = true) }
                        item { LatestUpdatesView() }
                        item { SectionTitle(title = "Latest News", hasAction = false) }
                        item { ArticlesView() }
                        item { SectionTitle(title = "Upcoming Events", hasAction = true) }
                        item { EventsView() }
                        item { Spacer(modifier = Modifier.height(32.dp))}
                    }
                } else {
                    // Initial load - show with fade-in animation
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 500,
                                delayMillis = 0
                            )
                        ) + slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            initialOffsetY = { it / 3 }
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 0
                            )
                        ) + slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            targetOffsetY = { it / 3 }
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item { HomeTopBar() }
                            item { NextLaunchView(navController = navController) }
                            item { SectionTitle(title = "Upcoming Launches", hasAction = true) }
                            item { UpcomingHorizontalScrollableList() }
                            item { SectionTitle(title = "Latest Updates", hasAction = true) }
                            item { LatestUpdatesView() }
                            item { SectionTitle(title = "Latest News", hasAction = false) }
                            item { ArticlesView() }
                            item { SectionTitle(title = "Upcoming Events", hasAction = true) }
                            item { EventsView() }
                        }
                    }
                }
            }
            
            // Pull-to-refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Space Launch Now", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, fontSize = 36.sp)
        IconButton(
            onClick = { /* Handle click */ },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.Notifications, 
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SearchBar() {
    val searchQuery = remember { TextFieldValue("") }
    BasicTextField(
        value = searchQuery,
        onValueChange = { /* Handle text change */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 8.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (searchQuery.text.isEmpty()) {
                    Text(text = "Search", color = Color.Gray)
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun SectionTitle(title: String, hasAction: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (hasAction) {
            TextButton(onClick = { /* Handle action click */ }) {
                Text(text = "See All", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun HorizontalScrollableList() {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
         contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(5) { PlaceholderCard() }
    }
}

@Composable
fun UpcomingHorizontalScrollableList() {
    val homeViewModel = koinViewModel<HomeViewModel>()

    LaunchListView(homeViewModel)
}

@Composable
fun PlaceholderCard() {
    Card(
        modifier = Modifier.size(150.dp, 150.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Content", color = Color.Gray)
        }
    }
}

@Composable
fun LargePlaceholderCard() {
    Card(
        modifier = Modifier.size(360.dp, 240.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Content", color = Color.Gray)
        }
    }
}

@Composable
fun NewsItem() {
    val homeViewModel = koinViewModel<HomeViewModel>()

    Row(
        modifier = Modifier.fillMaxWidth().absolutePadding(top = 8.dp, left = 16.dp, right = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = Color.LightGray
        ) { }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Headline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Description goes here in a short sentence.", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun NewsItemShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth().absolutePadding(top = 8.dp, left = 16.dp, right = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) { }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp),
            )
        }
    }
}

@Composable
fun ArticlesView() {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val articles by homeViewModel.articles.collectAsState()
    val isLoading by homeViewModel.isArticlesLoading.collectAsState()
    val error by homeViewModel.articlesError.collectAsState()

    when {
        isLoading && articles.isEmpty() -> {
            Column {
                repeat(3) {
                    NewsItemShimmer()
                }
            }
        }
        error != null -> {
            ArticleErrorCard(error = error!!)
        }
        articles.isNotEmpty() -> {
            Column {
                articles.take(5).forEach { article ->
                    ArticleItem(article = article)
                }
            }
        }
        else -> {
            Column {
                repeat(3) {
                    NewsItemShimmer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleItem(article: Article) {
    Card(
        onClick = { /* TODO: Open article URL */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val image = article.imageUrl
            SubcomposeAsyncImage(
                model = image,
                contentDescription = "Article Image",
                modifier = Modifier
                    .size(96.dp)
                    .clip(shape = RoundedCornerShape(2.dp)),
                contentScale = ContentScale.Crop,
                error = {
                    // Show Font Awesome newspaper icon as fallback
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Newspaper,
                            contentDescription = "Article Icon",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = article.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.summary.replace(".\n", " ").replace("\n", ""),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        maxLines = 3,
                        minLines = 3,
                        overflow = TextOverflow.Visible,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = article.newsSite,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = formatPublishedDate(article.publishedAt),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleErrorCard(error: String) {
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
                text = "Failed to load articles",
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
fun EventsView() {
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
                    EventItem(event = event)
                }
            }
        }
        else -> {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItem(event: EventEndpointNormal) {
    Card(
        onClick = { /* TODO: Open event details */ },
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
                error = {
                    // Show Font Awesome calendar icon as fallback
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.CalendarDay,
                            contentDescription = "Event Icon",
                            modifier = Modifier.size(96.dp),
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
                    text = formatLaunchDateTime(date), // Simple date formatting
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

/**
 * Format published date to human readable format
 */
private fun formatPublishedDate(instant: Instant): String {
    return try {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name.lowercase()
        "${month.replaceFirstChar { it.uppercase()}} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    } catch (e: Exception) {
        "Unknown date"
    }
}