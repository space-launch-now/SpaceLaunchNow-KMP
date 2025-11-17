package me.calebjones.spacelaunchnow.ui.home.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.isLargeScreen
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.components.OfflineBanner
import me.calebjones.spacelaunchnow.ui.compose.PlainShimmerCard
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock.System

@Composable
fun ResponsiveHomeContent(
    navController: NavController,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false,
    oldestCacheTimestamp: Long? = null,
    onRetry: () -> Unit = {}
) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    // Collect state for the new HomeQuickView
    val previousLaunches by homeViewModel.previousLaunches.collectAsStateWithLifecycle()
    val upcomingLaunches by homeViewModel.upcomingLaunches.collectAsStateWithLifecycle()

    // Collect all ViewStates
    val featuredLaunchState by homeViewModel.featuredLaunchState.collectAsStateWithLifecycle()
    val upcomingLaunchesState by homeViewModel.upcomingLaunchesState.collectAsStateWithLifecycle()
    val updatesState by homeViewModel.updatesState.collectAsStateWithLifecycle()
    val articlesState by homeViewModel.articlesState.collectAsStateWithLifecycle()
    val eventsState by homeViewModel.eventsState.collectAsStateWithLifecycle()
    val historyState by homeViewModel.historyState.collectAsStateWithLifecycle()

    // Check if ANY view is loading
    val isAnyViewLoading = remember(
        featuredLaunchState,
        upcomingLaunchesState,
        updatesState,
        articlesState,
        eventsState,
        historyState
    ) {
        listOf(
            featuredLaunchState,
            upcomingLaunchesState,
            updatesState,
            articlesState,
            eventsState,
            historyState
        ).any { it.isLoading }
    }

    // Collect accurate quick stats counts
    val next24HoursCount by homeViewModel.next24HoursCount.collectAsStateWithLifecycle()
    val nextWeekCount by homeViewModel.nextWeekCount.collectAsStateWithLifecycle()
    val nextMonthCount by homeViewModel.nextMonthCount.collectAsStateWithLifecycle()

    // Get current day and month for "This Day in History"
    val currentDate = remember {
        System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
    val currentDay = currentDate.dayOfMonth
    val currentMonth = currentDate.monthNumber

    // Load history launches on composition using NEW ViewState pattern
    LaunchedEffect(currentDay, currentMonth) {
        if (historyState.data.count == 0 && !historyState.isLoading && historyState.error == null) {
            homeViewModel.loadHistoryLaunchesNew(day = currentDay, month = currentMonth)
        }
    }

    val isTabletOrDesktop = isLargeScreen()

    if (isTabletOrDesktop) {
        // Tablet landscape layout - full width scrollable content
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            item { HomeTopBar(navController = navController) }

            // Single offline banner when any data is stale
            if (isOffline) {
                item {
                    OfflineBanner(
                        modifier = Modifier.padding(bottom = 16.dp),
                        dataSource = me.calebjones.spacelaunchnow.data.model.DataSource.STALE_CACHE,
                        cacheTimestamp = oldestCacheTimestamp,
                        onRetry = onRetry
                    )
                }
            }

            // Show loading indicator when any view is loading
            if (isAnyViewLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            }

            // Hero cards row: Last Launch + Stats + Next Up (countdown)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left column: Last Launch + Quick Stats

                    Column(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // Last Launch - Always render
                        Card {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Last Launch",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                if (previousLaunches.isNotEmpty()) {
                                    val previousLaunch = previousLaunches.first()
                                    LaunchItemView(
                                        launch = previousLaunch,
                                        navController = navController,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp)
                                    )
                                } else {
                                    // Loading placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Loading...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // This Day in History Card - Always render
                        Card {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "This Day in History",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }

                                when {
                                    // Error state - show error with retry OR data with error indicator
                                    historyState.error != null -> {
                                        if (historyState.data.launches.isNotEmpty()) {
                                            // Show stale data with error indicator
                                            Column {
                                                // Error banner
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "Showing cached data",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                                // Show stale launches
                                                LazyRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                                ) {
                                                    items(historyState.data.launches) { launch ->
                                                        LaunchItemView(
                                                            launch = launch,
                                                            navController = navController,
                                                            modifier = Modifier
                                                                .width(320.dp)
                                                                .height(240.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            // No cached data, just show error
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp),
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
                                                        text = "Failed to load history launches",
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Text(
                                                        text = historyState.error!!,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(
                                                            alpha = 0.8f
                                                        ),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Has data (no error)
                                    historyState.data.launches.isNotEmpty() && historyState.error == null -> {
                                        // Scrollable carousel of history launches
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) {
                                            items(historyState.data.launches) { launch ->
                                                LaunchItemView(
                                                    launch = launch,
                                                    navController = navController,
                                                    modifier = Modifier
                                                        .width(320.dp)
                                                        .height(240.dp)
                                                )
                                            }
                                        }
                                    }
                                    // Loading or empty state
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                if (historyState.isLoading) "Loading launches..."
                                                else if (historyState.data.count > 0) "Loading..."
                                                else "No launches on this day",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.6f
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right: Next Up with countdown - larger to accommodate countdown
                    Box(modifier = Modifier.weight(0.6f)) {
                        NextLaunchView(navController = navController)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {                         // Quick Stats
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Quick Stats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    // Stats Grid (2x2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatItem(
                            icon = Icons.Default.Today,
                            label = "Next 24 Hours",
                            value = if (next24HoursCount > 0) next24HoursCount.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            icon = Icons.Default.DateRange,
                            label = "Next 7 Days",
                            value = if (nextWeekCount > 0) nextWeekCount.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            icon = Icons.Default.Schedule,
                            label = "Next 30 Days",
                            value = if (nextMonthCount > 0) nextMonthCount.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            icon = Icons.Default.History,
                            label = "This Day in History",
                            value = if (historyState.data.count > 0) historyState.data.count.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Large Ad between This Day in History and Launch Schedule (only for non-premium users)
            if (!hasAdFree) {
                item {
                    SmartBannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        placementType = AdPlacementType.INTERSTITIAL, // Between content sections
                        showRemoveAdsButton = true,
                        onRemoveAdsClick = {
                            // Navigate to settings - navController is available in this scope
                            navController.navigate(SupportUs)
                        }
                    )
                }
            }

            item {
                SectionTitle(
                    title = "Launch Schedule",
                    hasAction = true,
                    onActionClick = {
                        navController.navigate(Schedule) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
            }
            // Bidirectional scrollable launch list
            item {
                LaunchListView(
                    viewModel = homeViewModel,
                    navController = navController
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { SectionTitle(title = "Latest Updates", hasAction = false) }
            item { LatestUpdatesView(navController = navController) }
            item { SectionTitle(title = "Latest News", hasAction = false) }
            item { ArticlesView() }
            item { SectionTitle(title = "Upcoming Events", hasAction = false) }
            item { EventsView(navController = navController) }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    } else {
        // Phone layout - use bidirectional carousel
        LazyColumn(
            modifier = modifier,
        ) {
            item { HomeTopBar(navController = navController) }

            // Single offline banner when any data is stale
            if (isOffline) {
                item {
                    OfflineBanner(
                        modifier = Modifier.padding(bottom = 16.dp),
                        dataSource = me.calebjones.spacelaunchnow.data.model.DataSource.STALE_CACHE,
                        cacheTimestamp = oldestCacheTimestamp,
                        onRetry = onRetry
                    )
                }
            }

            // Show loading indicator when any view is loading
            if (isAnyViewLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            }

            item { NextLaunchView(navController = navController) }

            // Quick Stats
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Quick Stats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Stats Grid (2x2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatItem(
                            icon = Icons.Default.Today,
                            label = "Next 24 Hours",
                            value = if (next24HoursCount > 0) next24HoursCount.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            icon = Icons.Default.DateRange,
                            label = "Next 7 Days",
                            value = if (nextWeekCount > 0) nextWeekCount.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatItem(
                            icon = Icons.Default.Schedule,
                            label = "Next 30 Days",
                            value = if (nextMonthCount > 0) nextMonthCount.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            icon = Icons.Default.History,
                            label = "This Day in History",
                            value = if (historyState.data.count > 0) historyState.data.count.toString() else "-",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // This Day in History Card
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "This Day in History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        // Error state with or without cached data
                        historyState.error != null -> {
                            if (historyState.data.launches.isNotEmpty()) {
                                // Show stale data with error indicator
                                Column {
                                    // Error banner
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = "Showing cached data",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    // Show stale launches
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        items(historyState.data.launches) { launch ->
                                            LaunchItemView(
                                                launch = launch,
                                                navController = navController,
                                                modifier = Modifier
                                                    .width(300.dp)
                                                    .height(220.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // No cached data, just show error
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
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
                                            text = "Failed to load history launches",
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = historyState.error!!,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(
                                                alpha = 0.8f
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        // Has data (no error)
                        historyState.data.launches.isNotEmpty() && historyState.error == null -> {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                items(historyState.data.launches) { launch ->
                                    LaunchItemView(
                                        launch = launch,
                                        navController = navController,
                                        modifier = Modifier
                                            .width(300.dp)
                                            .height(220.dp)
                                    )
                                }
                            }
                        }
                        // Loading or empty state
                        else -> {
                            PlainShimmerCard()
                        }
                    }
                }
            }

            // Large Ad between This Day in History and Launch Schedule (only for non-premium users)
            if (!hasAdFree) {
                item {
                    SmartBannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        placementType = AdPlacementType.INTERSTITIAL, // Between content sections
                        showRemoveAdsButton = true,
                        onRemoveAdsClick = {
                            // Navigate to settings - navController is available in this scope
                            navController.navigate(SupportUs)
                        }
                    )
                }
            }

            item {
                SectionTitle(
                    title = "Launch Schedule",
                    hasAction = true,
                    onActionClick = {
                        navController.navigate(Schedule) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            // Use new HomeQuickView for phone (bidirectional carousel)
            item {
                LaunchListView(
                    viewModel = homeViewModel,
                    navController = navController
                )
            }
            item { SectionTitle(title = "Latest Updates", hasAction = false) }
            item { LatestUpdatesView(navController = navController) }
            item { SectionTitle(title = "Latest News", hasAction = false) }
            item { ArticlesView() }
            item { SectionTitle(title = "Upcoming Events", hasAction = false) }
            item { EventsView(navController = navController) }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

/**
 * Individual stat item for the quick stats card - matches detail page StatCard style
 */
@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 96.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
