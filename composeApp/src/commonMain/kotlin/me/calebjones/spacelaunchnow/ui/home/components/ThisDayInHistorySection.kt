package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.ui.compose.PlainShimmerCard
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.HistoryViewModel.HistoryData
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState
import org.jetbrains.compose.ui.tooling.preview.Preview

private val HistoryCardWidth = 380.dp
private val HistoryCardHeight = 200.dp

/**
 * Displays the "This Day in History" section with a horizontal carousel of past launches.
 * Handles loading, error (with/without cached data), and empty states.
 */
@Composable
fun ThisDayInHistorySection(
    historyState: ViewState<HistoryData>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when {
            // Error state - show error with retry OR data with error indicator
            historyState.error != null -> {
                val errorMessage = historyState.error
                if (historyState.data.launches.isNotEmpty()) {
                    // Show stale data with error indicator
                    Column {
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
                                        .width(HistoryCardWidth)
                                        .height(HistoryCardHeight)
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
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage,
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
            }
            // Has data (no error)
            historyState.data.launches.isNotEmpty() -> {
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
                                .width(HistoryCardWidth)
                                .height(HistoryCardHeight)
                        )
                    }
                }
            }
            // Loading or empty state
            else -> {
                if (historyState.isLoading) {
                    PlainShimmerCard()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(HistoryCardHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No launches on this day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// region Previews

@Preview
@Composable
private fun ThisDayInHistorySectionPreview() {
    SpaceLaunchNowPreviewTheme {
        ThisDayInHistorySection(
            historyState = ViewState(
                data = HistoryData(
                    count = 2,
                    launches = listOf(
                        PreviewData.launchNormalSpaceX,
                        PreviewData.launchNormalULA
                    )
                )
            ),
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun ThisDayInHistorySectionDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        ThisDayInHistorySection(
            historyState = ViewState(
                data = HistoryData(
                    count = 2,
                    launches = listOf(
                        PreviewData.launchNormalSpaceX,
                        PreviewData.launchNormalULA
                    )
                )
            ),
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun ThisDayInHistorySectionErrorPreview() {
    SpaceLaunchNowPreviewTheme {
        ThisDayInHistorySection(
            historyState = ViewState(
                data = HistoryData(count = 0, launches = emptyList()),
                error = "Network error: Unable to reach server"
            ),
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun ThisDayInHistorySectionLoadingPreview() {
    SpaceLaunchNowPreviewTheme {
        ThisDayInHistorySection(
            historyState = ViewState(
                data = HistoryData(count = 0, launches = emptyList()),
                isLoading = true
            ),
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun ThisDayInHistorySectionEmptyPreview() {
    SpaceLaunchNowPreviewTheme {
        ThisDayInHistorySection(
            historyState = ViewState(
                data = HistoryData(count = 0, launches = emptyList())
            ),
            navController = rememberNavController()
        )
    }
}

// endregion
