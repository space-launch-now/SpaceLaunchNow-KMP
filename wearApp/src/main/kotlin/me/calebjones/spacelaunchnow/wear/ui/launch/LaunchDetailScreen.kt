package me.calebjones.spacelaunchnow.wear.ui.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailUiState
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailViewModel

@Composable
fun LaunchDetailScreen(
    viewModel: LaunchDetailViewModel,
    launchId: String,
    onOpenOnPhone: () -> Unit = {},
) {
    LaunchedEffect(launchId) {
        viewModel.loadLaunch(launchId)
    }
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
                ) {
                    CircularProgressIndicator()
                }
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
                    )
                }
            }
            uiState.launch != null -> {
                val launch = uiState.launch
                TransformingLazyColumn(
                    state = columnState,
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize(),
                ) {
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
                            )
                        }
                    }
                    item {
                        Text(
                            text = uiState.countdown,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        )
                    }
                    launch.missionName?.let { mission ->
                        item {
                            Text(
                                text = mission,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.transformedHeight(this, transformationSpec),
                            )
                        }
                    }
                    item {
                        val netFormatted = formatNetDateTime(launch.net)
                        Text(
                            text = netFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.transformedHeight(this, transformationSpec),
                        )
                    }
                    launch.statusName?.let { status ->
                        item {
                            Text(
                                text = "Status: $status",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.transformedHeight(this, transformationSpec),
                            )
                        }
                    }
                    launch.padLocationName?.let { location ->
                        item {
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.transformedHeight(this, transformationSpec),
                            )
                        }
                    }
                    launch.missionDescription?.let { description ->
                        item {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.transformedHeight(this, transformationSpec),
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = onOpenOnPhone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            label = { Text("Open on Phone") },
                        )
                    }
                }
            }
        }
    }
}

private fun formatNetDateTime(net: kotlin.time.Instant): String {
    return try {
        val tz = TimeZone.currentSystemDefault()
        val local = net.toLocalDateTime(tz)
        "${local.month.name.take(3)} ${local.dayOfMonth}, ${local.year} ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    } catch (_: Exception) {
        net.toString()
    }
}
