package me.calebjones.spacelaunchnow.ui.other

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.ktor.client.call.body
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.ui.compose.LaunchCardHeader
import me.calebjones.spacelaunchnow.ui.compose.toLaunchCardData
import org.koin.compose.koinInject

@Composable
fun ScheduleScreen(
    onLaunchClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ScheduleContent(onLaunchClick = onLaunchClick)
    }
}

private enum class ScheduleTab { Upcoming, Previous }

@Composable
private fun ScheduleContent(onLaunchClick: (String) -> Unit) {
    val launchesApi: LaunchesApi = koinInject()

    var selectedTab by remember { mutableStateOf(ScheduleTab.Upcoming) }
    var searchQuery by remember { mutableStateOf("") }

    val launches = remember { mutableStateListOf<LaunchNormal>() }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var endReached by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    suspend fun loadNextPage(reset: Boolean = false) {
        if (isLoading || endReached) return
        isLoading = true
        try {
            val limit = 20
            val offset = if (reset) 0 else launches.size
            val isUpcoming = selectedTab == ScheduleTab.Upcoming
            val isPrevious = selectedTab == ScheduleTab.Previous
            val ordering = if (isUpcoming) "net" else "-net"

            val page: PaginatedLaunchNormalList = if (isPrevious) {
                // direct call with previous=true to get past launches
                launchesApi.launchesList(
                    limit = limit,
                    offset = offset,
                    previous = true,
                    ordering = ordering,
                    search = searchQuery.takeIf { it.isNotBlank() }
                ).body()
            } else {
                // upcoming path
                launchesApi.launchesList(
                    limit = limit,
                    offset = offset,
                    upcoming = true,
                    ordering = ordering,
                    search = searchQuery.takeIf { it.isNotBlank() }
                ).body()
            }

            if (reset) launches.clear()
            launches.addAll(page.results)
            endReached = page.next == null || page.results.isEmpty()
        } catch (t: Throwable) {
            error = t.message ?: t.toString()
        } finally {
            isLoading = false
        }
    }

    fun resetAndLoad() {
        launches.clear()
        endReached = false
        error = null
        scope.launch { loadNextPage(reset = true) }
    }

    LaunchedEffect(selectedTab) { resetAndLoad() }

    // Debounce search
    LaunchedEffect(searchQuery, selectedTab) {
        delay(400)
        resetAndLoad()
    }

    // Infinite scroll trigger when near the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { it != null }
            .map { it!! }
            .distinctUntilChanged()
            .collectLatest { lastIndex ->
                if (lastIndex >= launches.size - 5) {
                    loadNextPage()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tabs
        TabRow(selectedTabIndex = if (selectedTab == ScheduleTab.Upcoming) 0 else 1) {
            Tab(
                selected = selectedTab == ScheduleTab.Upcoming,
                onClick = { selectedTab = ScheduleTab.Upcoming },
                text = { Text("Upcoming") }
            )
            Tab(
                selected = selectedTab == ScheduleTab.Previous,
                onClick = { selectedTab = ScheduleTab.Previous },
                text = { Text("Previous") }
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search launches") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )

        Divider()

        // Content list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (error != null && launches.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Error loading launches", style = MaterialTheme.typography.titleMedium)
                        Text(
                            error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { resetAndLoad() }) { Text("Retry") }
                    }
                }
            }

            if (launches.isEmpty() && isLoading) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            items(launches) { launch ->
                ScheduleLaunchCard(
                    launch = launch,
                    onClick = { onLaunchClick(launch.id) }
                )
            }

            if (isLoading && launches.isNotEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!isLoading && launches.isEmpty() && error == null) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No launches found")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleLaunchCard(
    launch: LaunchNormal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            // Optional header image if available
            val imageUrl = launch.image?.imageUrl
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = if (imageUrl != null) 0.5f else 1f))
                    .padding(12.dp)
            ) {
                // Reuse common header for consistency
                LaunchCardHeader(
                    launchData = launch.toLaunchCardData(),
                )
            }
        }
    }
}