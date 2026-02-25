package me.calebjones.spacelaunchnow.ui.rockets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketFilterSheet
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketListItem
import me.calebjones.spacelaunchnow.ui.viewmodel.RocketSortOrder
import me.calebjones.spacelaunchnow.ui.viewmodel.RocketViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main screen for browsing rockets.
 *
 * Features:
 * - Paginated list of rockets
 * - Infinite scroll loading
 * - Search by name
 * - Sort by name or family
 * - Error and empty states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RocketListScreen(
    onNavigateToRocketDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<RocketViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Filter sheet state
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Calculate active filter count for badge
    val activeFilterCount = remember(
        uiState.searchQuery,
        uiState.selectedSortOrder,
        uiState.selectedProgramIds,
        uiState.selectedFamilies,
        uiState.activeFilter,
        uiState.reusableFilter
    ) {
        var count = 0
        if (uiState.searchQuery.isNotBlank()) count++
        if (uiState.selectedSortOrder != RocketSortOrder.NAME_ASC) count++
        if (uiState.selectedProgramIds.isNotEmpty()) count++
        if (uiState.selectedFamilies.isNotEmpty()) count++
        if (uiState.activeFilter != null) count++
        if (uiState.reusableFilter != null) count++
        count
    }
    
    // Trigger load more when near bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            lastVisibleItemIndex >= totalItemsCount - 5 && uiState.hasMore && !uiState.isLoadingMore
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val hasFilters = activeFilterCount > 0
                    Text(
                        text = if (uiState.totalCount > 0 && hasFilters) {
                            "Rockets (${uiState.totalCount})"
                        } else {
                            "Rockets"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Filter button with badge
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge {
                                        Text(activeFilterCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter rockets"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Error state
                uiState.error != null && uiState.rockets.isEmpty() -> {
                    ErrorContent(
                        errorMessage = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.refresh() }
                    )
                }

                // Loading state (first load)
                uiState.isLoading && uiState.rockets.isEmpty() -> {
                    LoadingContent()
                }

                // Empty state
                uiState.rockets.isEmpty() && !uiState.isLoading -> {
                    EmptyContent()
                }

                // Content loaded
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.rockets,
                            key = { it.id }
                        ) { rocket ->
                            RocketListItem(
                                rocket = rocket,
                                onClick = { onNavigateToRocketDetail(rocket.id) }
                            )
                        }

                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            RocketFilterSheet(
                searchQuery = uiState.searchQuery,
                selectedSortOrder = uiState.selectedSortOrder,
                selectedProgramIds = uiState.selectedProgramIds,
                selectedFamilies = uiState.selectedFamilies,
                activeFilter = uiState.activeFilter,
                reusableFilter = uiState.reusableFilter,
                programOptions = uiState.programOptions,
                familyOptions = uiState.familyOptions,
                isLoadingFilterOptions = uiState.isLoadingFilterOptions,
                onSearchQueryChange = { query ->
                    viewModel.updateSearchQuery(query)
                },
                onSortOrderChange = { sortOrder ->
                    viewModel.updateSortOrder(sortOrder)
                },
                onProgramsChange = { programIds ->
                    viewModel.updateProgramFilter(programIds)
                },
                onFamiliesChange = { families ->
                    viewModel.updateFamilyFilter(families)
                },
                onActiveChange = { active ->
                    viewModel.updateActiveFilter(active)
                },
                onReusableChange = { reusable ->
                    viewModel.updateReusableFilter(reusable)
                },
                onReloadOptions = {
                    viewModel.reloadFilterOptions()
                },
                onClearAll = {
                    viewModel.clearAllFilters()
                },
                onClose = {
                    scope.launch {
                        sheetState.hide()
                        showFilterSheet = false
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading rockets...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
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
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Failed to load rockets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Rocket,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No rockets found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Try adjusting your filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
