package me.calebjones.spacelaunchnow.wear.ui.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.wear.R
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailUiState
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailViewModel
import kotlin.time.Clock
import kotlin.time.Instant

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

                    // ── Launch artwork (circular hero image) ───────────────
                    launch.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = launch.missionName
                                        ?: uiState.formattedTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                )
                            }
                        }
                    }

                    // ── Vehicle / agency title (with rocket icon) ──────────
                    item {
                        ListHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = uiState.formattedTitle,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    // ── Countdown card (live-ticking segments) ─────────────
                    item {
                        TitleCard(
                            onClick = {},
                            title = {
                                IconLabel(
                                    iconRes = R.drawable.ic_schedule,
                                    text = "Countdown",
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        ) {
                            CountdownSegments(net = launch.net)
                        }
                    }

                    // ── Mission / date / status card ───────────────────────
                    item {
                        val netFormatted = formatNetDateTime(launch.net)
                        TitleCard(
                            onClick = {},
                            title = {
                                IconLabel(
                                    iconRes = R.drawable.ic_rocket_launch,
                                    text = launch.missionName ?: uiState.formattedTitle,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconRow(
                                    iconRes = R.drawable.ic_schedule,
                                    text = netFormatted,
                                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                launch.statusName?.let { status ->
                                    IconRow(
                                        iconRes = R.drawable.ic_check_circle,
                                        text = status,
                                        iconTint = MaterialTheme.colorScheme.tertiary,
                                        textColor = MaterialTheme.colorScheme.tertiary,
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
                                    IconLabel(
                                        iconRes = R.drawable.ic_location_on,
                                        text = "Launch Site",
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
                                    IconLabel(
                                        iconRes = R.drawable.ic_rocket_launch,
                                        text = "Mission",
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

/** Small icon next to a label — used as a card title row. */
@Composable
private fun IconLabel(
    iconRes: Int,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

/** Small icon next to body text — used inside cards (date, status, etc). */
@Composable
private fun IconRow(
    iconRes: Int,
    text: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Live-ticking segmented countdown — `DD : HH : MM` with small caps labels under each
 * pair of digits. Mirrors the phone's home-screen format. Recomputes once per second so
 * the minutes update visibly.
 */
@Composable
private fun CountdownSegments(net: Instant) {
    var now by remember { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(net) {
        while (true) {
            now = Clock.System.now()
            delay(1_000)
        }
    }
    val segments = remember(now, net) { computeDetailSegments(net, now) }

    ScaleToFitHorizontally(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CountdownSegment(segments.days, "DAYS")
            CountdownColon()
            CountdownSegment(segments.hours, "HOURS")
            CountdownColon()
            CountdownSegment(segments.minutes, "MIN")
        }
    }
}

/**
 * Measures [content] at its natural width, then uniformly downscales it (via a graphics
 * layer) so the result fits within the parent's max width. Lets the countdown digits scale
 * with the user's system font size while guaranteeing the row never overflows the watch
 * bezel — required by Wear OS quality guidelines for large-font support.
 */
@Composable
private fun ScaleToFitHorizontally(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val placeable = measurables.first().measure(Constraints())
        val scale = if (placeable.width > constraints.maxWidth && constraints.maxWidth > 0) {
            constraints.maxWidth.toFloat() / placeable.width
        } else 1f
        val laidOutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
        val scaledHeight = (placeable.height * scale).toInt()
        layout(laidOutWidth, scaledHeight) {
            val offsetX = (laidOutWidth - placeable.width) / 2
            val offsetY = (scaledHeight - placeable.height) / 2
            placeable.placeWithLayer(offsetX, offsetY) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
        }
    }
}

@Composable
private fun CountdownSegment(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CountdownColon() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = ":",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        // Invisible spacer matching the label line so the colon's baseline aligns
        // with the digit row in the adjacent segments.
        Text(
            text = " ",
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private data class DetailCountdownSegments(
    val days: Long,
    val hours: Long,
    val minutes: Long,
)

private fun computeDetailSegments(net: Instant, now: Instant): DetailCountdownSegments {
    val duration = net - now
    if (duration.isNegative()) return DetailCountdownSegments(0, 0, 0)
    val totalMinutes = duration.inWholeMinutes
    return DetailCountdownSegments(
        days = totalMinutes / (24 * 60),
        hours = (totalMinutes / 60) % 24,
        minutes = totalMinutes % 60,
    )
}
