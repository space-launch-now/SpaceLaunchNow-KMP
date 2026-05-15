package me.calebjones.spacelaunchnow.wear.ui.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import me.calebjones.spacelaunchnow.wear.ui.theme.wearHorizontalPadding
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchListUiState
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchListViewModel
import kotlin.time.Clock

@Composable
fun LaunchListScreen(
    viewModel: LaunchListViewModel,
    onLaunchClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchListContent(
        uiState = uiState,
        onLaunchClick = onLaunchClick,
    )
}

@Composable
private fun LaunchListContent(
    uiState: LaunchListUiState,
    onLaunchClick: (String) -> Unit,
) {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val hPadding = wearHorizontalPadding()
    ScreenScaffold(scrollState = columnState) { contentPadding ->
        if (uiState.isLoading && uiState.launches.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading launches...",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else if (uiState.launches.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = uiState.error ?: "No upcoming launches",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
//                contentPadding = PaddingValues(
//                    top = contentPadding.calculateTopPadding(),
//                    bottom = contentPadding.calculateBottomPadding(),
//                    start = hPadding,
//                    end = hPadding,
//                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    ) {
                        Text(text = "Upcoming Launches")
                    }
                }
                items(uiState.launches, key = { it.id }) { launch ->
                    LaunchCard(
                        launch = launch,
                        onClick = { onLaunchClick(launch.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                        transformation = SurfaceTransformation(transformationSpec)
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchCard(
    launch: CachedLaunch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation,
) {
    val title = remember(launch) { formatWearTitle(launch) }
    val countdown = remember(launch) { formatWearCountdown(launch.net) }

    Card(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        transformation = transformation
    ) {
        Text(
            text = title,
            maxLines = 2,
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(end=12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = countdown,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            launch.statusAbbrev?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        launch.padLocationName?.let { location ->
            Text(
                text = location,
                maxLines = 1,
                style = MaterialTheme.typography.bodyExtraSmall,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

internal fun formatWearTitle(launch: CachedLaunch): String {
    val lspDisplay = launch.lspName?.let { name ->
        if (name.length > 15 && !launch.lspAbbrev.isNullOrEmpty()) {
            launch.lspAbbrev
        } else {
            name
        }
    }
    return when {
        lspDisplay != null && launch.rocketConfigName != null ->
            "$lspDisplay | ${launch.rocketConfigName}"

        launch.rocketConfigName != null -> launch.rocketConfigName
        else -> launch.name
    }
}

internal fun formatWearCountdown(net: kotlin.time.Instant): String {
    val now = Clock.System.now()
    val duration = net - now
    if (duration.isNegative()) return "Launched"

    val totalMinutes = duration.inWholeMinutes
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours >= 24 -> "T-${hours / 24}d ${hours % 24}h"
        hours > 0 -> "T-${hours}h ${minutes}m"
        minutes > 0 -> "T-${minutes}m"
        else -> "T-0"
    }
}
