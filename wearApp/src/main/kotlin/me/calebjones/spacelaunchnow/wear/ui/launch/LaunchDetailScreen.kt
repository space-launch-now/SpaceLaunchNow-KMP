package me.calebjones.spacelaunchnow.wear.ui.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailUiState
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailViewModel

/** Horizontal padding that keeps text clear of the round bezel on all watch sizes. */
private val HorizontalContentPadding = 14.dp

@Composable
fun LaunchDetailScreen(
    viewModel: LaunchDetailViewModel,
    launchId: String,
    onOpenOnPhone: () -> Unit = {},
) {
    LaunchedEffect(launchId) { viewModel.loadLaunch(launchId) }
    val uiState by viewModel.uiState.collectAsState()
    LaunchDetailContent(
        uiState = uiState,
        onOpenOnPhone = { viewModel.openOnPhone(launchId) },
    )
}

@Composable
private fun LaunchDetailContent(
    uiState: LaunchDetailUiState,
    onOpenOnPhone: () -> Unit,
) {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator() }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = HorizontalContentPadding),
                    )
                }
            }

            uiState.launch != null -> {
                val launch = uiState.launch
                TransformingLazyColumn(
                    state = columnState,
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                        start = HorizontalContentPadding,
                        end = HorizontalContentPadding,
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {

                    // ── Vehicle / agency title ─────────────────────────────
                    item {
                        ListHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        ) {
                            Text(
                                text = uiState.formattedTitle,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    // ── Countdown card ─────────────────────────────────────
                    item {
                        TitleCard(
                            onClick = {},
                            title = {
                                Text(
                                    text = "Countdown",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        ) {
                            Text(
                                text = uiState.countdown,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // ── Mission / date / status card ───────────────────────
                    item {
                        val netFormatted = formatNetDateTime(launch.net)
                        TitleCard(
                            onClick = {},
                            title = {
                                Text(
                                    text = launch.missionName ?: uiState.formattedTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = netFormatted,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                launch.statusName?.let { status ->
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                        }
                    }

                    // ── Launch site card ───────────────────────────────────
                    launch.padLocationName?.let { location ->
                        item {
                            TitleCard(
                                onClick = {},
                                title = {
                                    Text(
                                        text = "Launch Site",
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            ) {
                                Text(
                                    text = location,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    // ── Mission description card ───────────────────────────
                    launch.missionDescription?.let { description ->
                        item {
                            TitleCard(
                                onClick = {},
                                title = {
                                    Text(
                                        text = "Mission",
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            ) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    // ── Open on phone ──────────────────────────────────────
                    item {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onOpenOnPhone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            label = {
                                Text(
                                    text = "Open on Phone",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            },
                        )
                    }

                    // Breathing room so button scrolls clear of bezel
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

private fun formatNetDateTime(net: kotlin.time.Instant): String {
    return try {
        val tz = TimeZone.currentSystemDefault()
        val local = net.toLocalDateTime(tz)
        "${local.month.name.take(3)} ${local.day}, ${local.year} " +
                "${local.hour.toString().padStart(2, '0')}:" +
                local.minute.toString().padStart(2, '0')
    } catch (_: Exception) {
        net.toString()
    }
}
