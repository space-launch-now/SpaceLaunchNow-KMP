package me.calebjones.spacelaunchnow.wear.ui.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.wear.R
import me.calebjones.spacelaunchnow.wear.ui.theme.wearHorizontalPadding
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailUiState
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailViewModel
import kotlin.time.Clock
import kotlin.time.Instant

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
    val hPadding = wearHorizontalPadding()

    ScreenScaffold(
        scrollState = columnState,
        edgeButton = {
            EdgeButton(onClick = onOpenOnPhone) {
                Text("Open on Phone")
            }
        },
    ) { contentPadding ->
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
                        modifier = Modifier.padding(horizontal = hPadding),
                    )
                }
            }

            uiState.launch != null -> {
                val launch = uiState.launch
                val (title, subtitle) = uiState.formattedTitle
                    .split("|", limit = 2)
                    .map { it.trim() }
                    .let { parts -> parts.getOrElse(0) { "" } to parts.getOrElse(1) { "" } }
                TransformingLazyColumn(
                    state = columnState,
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                        start = hPadding,
                        end = hPadding,
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {

                    // ── Launch artwork with title overlay ─────────────────
                    if (!launch.imageUrl.isNullOrBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 12.dp)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .transformedHeight(this, transformationSpec),
                            ) {
                                AsyncImage(
                                    model = launch.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.75f),
                                                ),
                                            )
                                        ),
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = subtitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    } else {
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
                    }

                    // ── Countdown card (live-ticking segments) ─────────────
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            CountdownSegments(net = launch.net)
                        }
                    }

                    // ── Mission / date / status ────────────────────────────
                    item {
                        val netFormatted = formatNetDateTime(launch.net)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
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

                    // ── Launch site ────────────────────────────────────────
                    launch.padLocationName?.let { location ->
                        item {
                            IconRow(
                                iconRes = R.drawable.ic_location_on,
                                text = location,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            )
                        }
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
        "${local.month.name.take(3)} ${local.day}, ${local.year} " +
                "${local.hour.toString().padStart(2, '0')}:" +
                local.minute.toString().padStart(2, '0')
    } catch (_: Exception) {
        net.toString()
    }
}

@Composable
private fun IconRow(
    iconRes: Int,
    text: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
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
        val laidOutWidth =
            if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
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
