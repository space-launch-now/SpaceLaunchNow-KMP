package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.WikipediaW
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Country
import me.calebjones.spacelaunchnow.api.launchlibrary.models.FirstStageNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Mission
import me.calebjones.spacelaunchnow.api.launchlibrary.models.NetPrecision
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightDetailedSerializerNoLaunch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEvent
import me.calebjones.spacelaunchnow.isLargeScreen
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.compose.LaunchCountdown
import me.calebjones.spacelaunchnow.ui.compose.LaunchVideoPlayer
import me.calebjones.spacelaunchnow.ui.compose.LaunchWindowIndicator
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchTime
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatTimelineRelativeTime
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor
import me.calebjones.spacelaunchnow.util.VideoUtil
import org.koin.compose.koinInject
// Keep only TitleHeight which is used for spacing
private val TitleHeight = 120.dp
private val CompactHeight = 40.dp

// Function to parse ISO 8601 duration to human readable format
private fun parseIsoDurationToHumanReadable(isoDuration: String): String {
    // Pattern: P[n]Y[n]M[n]DT[n]H[n]M[n]S
    // Example: P59DT12H59M = 59 days, 12 hours, 59 minutes

    val regex =
        Regex("""P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?""")
    val matchResult = regex.find(isoDuration) ?: return isoDuration

    val years = matchResult.groupValues[1].toIntOrNull() ?: 0
    val months = matchResult.groupValues[2].toIntOrNull() ?: 0
    val days = matchResult.groupValues[3].toIntOrNull() ?: 0
    val hours = matchResult.groupValues[4].toIntOrNull() ?: 0
    val minutes = matchResult.groupValues[5].toIntOrNull() ?: 0
    val seconds = matchResult.groupValues[6].toIntOrNull() ?: 0

    val parts = mutableListOf<String>()

    if (years > 0) parts.add("${years}y")
    if (months > 0) parts.add("${months}mo")
    if (days > 0) parts.add("${days}d")
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0) parts.add("${minutes}m")
    if (seconds > 0) parts.add("${seconds}s")

    return if (parts.isEmpty()) "0" else parts.joinToString(" ")
}

@Composable
private fun PrecisionInfoDialog(
    netPrecision: NetPrecision?,
    onDismiss: () -> Unit
) {
    val ui = remember(netPrecision) { mapNetPrecisionUi(netPrecision) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Launch Time Precision", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        ui.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = ui.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                ui.secondary?.let { secondary ->
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "The launch NET (No Earlier Than) value is supplied by the provider and precision may vary. NET can represent a fully scheduled time, an approximate window, only a year, only a month, etc. Some launches only have rough or placeholder NET values until closer to launch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
    )
}

@Composable
private fun CombinedLaunchOverviewCard(launch: LaunchDetailed) {
    var showPrecisionDialog by remember { mutableStateOf(false) }
    val useUtc = LocalUseUtc.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Countdown and status (if NET known)
                launch.net?.let { launchTime ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LaunchCountdown(
                            launchTime = launchTime,
                            status = launch.status
                        )
                    }
                }

                // Major launch info summary (like LaunchInfoCardHeroContent)
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // Date/time surface (matches old hero band)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Box {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        launch.net?.let { net ->
                                            val relativeDateText =
                                                me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTimeRelative(
                                                    net,
                                                    useUtc
                                                )
                                            // Extract just the date part (everything before the time)
                                            // e.g., "Today at 12:31pm" -> "Today at"
                                            val dateOnlyText =
                                                if (relativeDateText.contains(" at ")) {
                                                    relativeDateText.substringBefore(" at ") + " at"
                                                } else {
                                                    // Fallback for other formats
                                                    relativeDateText.substringBeforeLast(" ")
                                                }
                                            Text(
                                                text = dateOnlyText,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        Text(
                                            text = launch.net?.let { formatLaunchTime(it, useUtc) }
                                                ?: "TBD",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        launch.probability?.let { prob ->
                                            InfoTile(
                                                icon = Icons.Filled.WbCloudy,
                                                label = "Weather",
                                                value = "$prob%",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                }

                                // Window indicator (if window exists) - now inside the same surface
                                if (launch.windowStart != null && launch.windowEnd != null) {
                                    LaunchWindowIndicator(
                                        launchTime = launch.net ?: launch.windowStart,
                                        windowStart = launch.windowStart,
                                        windowEnd = launch.windowEnd,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )
                                } else {
                                    // Add some bottom padding when there's no window indicator
                                    Spacer(modifier = Modifier.height(0.dp))
                                }
                            }

                            // Info icon in top-right corner of the surface
                            launch.netPrecision?.let {
                                IconButton(
                                    onClick = { showPrecisionDialog = true },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "Launch time precision info",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Metrics grid below
                val tiles = buildList {
                    launch.mission?.name?.takeIf { it.isNotBlank() }
                        ?.let { add(Triple(Icons.Filled.Category, "Mission", it)) }
                    launch.pad?.let { pad ->
                        val site = pad.location?.name ?: pad.name ?: "Unknown"
                        add(
                            Triple(
                                Icons.Filled.LocationOn,
                                "Launch Site",
                                site
                            )
                        )
                    }
                    launch.probability?.let { prob ->
                        add(
                            Triple(
                                Icons.Filled.WbCloudy,
                                "Weather",
                                "$prob%"
                            )
                        )
                    }
                }
                if (tiles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tiles.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (icon, label, value) ->
                                    InfoTile(
                                        icon = icon,
                                        label = label,
                                        value = value,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Precision dialog
    if (showPrecisionDialog) {
        PrecisionInfoDialog(
            netPrecision = launch.netPrecision,
            onDismiss = { showPrecisionDialog = false }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
val snackDetailBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.8f, stiffness = 380f)
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class
)
@Composable
fun LaunchDetailView(
    launch: LaunchDetailed,
    videoPlayerState: me.calebjones.spacelaunchnow.ui.viewmodel.VideoPlayerState,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit
) {
    // Use the shared detail scaffold to unify behavior
    SharedDetailScaffold(
        titleText = launch.name ?: "Unknown Launch",
        taglineText = launch.launchServiceProvider.name,
        imageUrl = launch.image?.imageUrl,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            getLaunchStatusColor(launch.status?.id),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        // Render existing launch detail content inside the shared scaffold body
        LaunchDetailContentInBody(
            launch = launch,
            videoPlayerState = videoPlayerState,
            onSelectVideo = onSelectVideo,
            onSetPlayerVisible = onSetPlayerVisible,
            onNavigateToFullscreen = onNavigateToFullscreen,
            onVideoSelected = onVideoSelected,
            onNavigateToSettings = null // TODO: Pass from parent screen
        )
    }
}


// This composable contains all the detailed launch information
@Composable
private fun LaunchDetailContentInBody(
    launch: LaunchDetailed,
    videoPlayerState: me.calebjones.spacelaunchnow.ui.viewmodel.VideoPlayerState,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val isLargeScreen = isLargeScreen()
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(if (isLargeScreen) CompactHeight else TitleHeight))

        // Split content into two columns on large screens
        if (isLargeScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Combined Launch Overview Card (always full width)
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    CombinedLaunchOverviewCard(launch = launch)
                    Spacer(Modifier.height(16.dp))

                    // 2. Quick Stats Grid (always full width)
                    QuickStatsGrid(launch = launch)
                    Spacer(Modifier.height(8.dp))

                    SmartBannerAd(
                        modifier = Modifier.fillMaxWidth(),
                        placementType = AdPlacementType.CONTENT,
                        showRemoveAdsButton = true,
                        onRemoveAdsClick = onNavigateToSettings
                    )

                    Spacer(Modifier.height(8.dp))
                    // Timeline Card
                    if (launch.timeline.isNotEmpty()) {
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TimelineCard(timeline = launch.timeline)
                    }

                    // Mission Details Card
                    launch.mission?.let { mission ->
                        Text(
                            text = "Mission Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        MissionDetailsCard(mission = mission, launch = launch)
                    }

                    // Spacecraft Details Card
                    if (!launch.rocket?.spacecraftStage.isNullOrEmpty()) {
                        Text(
                            text = "Spacecraft Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        SpacecraftDetailsCard(
                            spacecraftStages = launch.rocket?.spacecraftStage ?: emptyList()
                        )
                    }

                    // Agency Card
                    launch.launchServiceProvider.let { agency ->
                        Text(
                            text = "Launch Service Provider",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        AgencyDetailsCard(agency = agency)
                    }
                }

                // Right Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Video Player Card
                    if (videoPlayerState.availableVideos.isNotEmpty()) {
                        val videoTitle = launch.net?.let { net ->
                            val now = System.now()
                            if (net > now) "Watch Live" else "Watch Replay"
                        } ?: "Watch Launch"

                        Text(
                            text = videoTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        VideoPlayerCard(
                            videoPlayerState = videoPlayerState,
                            launchName = launch.mission?.name ?: "Space Launch",
                            onSetPlayerVisible = onSetPlayerVisible,
                            onNavigateToFullscreen = onNavigateToFullscreen,
                            onVideoSelected = onVideoSelected
                        )
                    }

                    // Launch Vehicle Details Card
                    launch.rocket?.configuration?.let { rocketConfig ->
                        Text(
                            text = "Launch Vehicle Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        LaunchVehicleDetailsCard(rocketConfig = rocketConfig)
                        LaunchVehicleDetailedStatistics(rocketConfig = rocketConfig)
                    }

                    // Landing Details Card
                    run {
                        val landingStages = launch.rocket?.launcherStage ?: emptyList()
                        if (landingStages.any { it.landing != null }) {
                            Text(
                                text = "Landing Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            LandingDetailsCard(launcherStages = landingStages)
                        }
                    }

                    // Agency Statistics
                    launch.launchServiceProvider.let { agency ->
                        Text(
                            text = "Launch Service Provider Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        AgencyLaunchStatistics(agency = agency)
                    }
                }
            }
        } else {
            // Single column layout for small screens (existing layout)
            Column {
                // 1. Combined Launch Overview Card (always full width)
                CombinedLaunchOverviewCard(launch = launch)
                Spacer(Modifier.height(16.dp))

                // 2. Quick Stats Grid (always full width)
                QuickStatsGrid(launch = launch)
                Spacer(Modifier.height(16.dp))

                SmartBannerAd(
                    modifier = Modifier.fillMaxWidth(),
                    placementType = AdPlacementType.CONTENT,
                    showRemoveAdsButton = true,
                    onRemoveAdsClick = onNavigateToSettings
                )
                Spacer(Modifier.height(16.dp))

                // 3. Video Player Card - positioned above timeline
                if (videoPlayerState.availableVideos.isNotEmpty()) {
                    val videoTitle = launch.net?.let { net ->
                        val now = System.now()
                        if (net > now) "Watch Live" else "Watch Replay"
                    } ?: "Watch Launch"

                    Text(
                        text = videoTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    VideoPlayerCard(
                        videoPlayerState = videoPlayerState,
                        launchName = launch.mission?.name ?: "Space Launch",
                        onSetPlayerVisible = onSetPlayerVisible,
                        onNavigateToFullscreen = onNavigateToFullscreen,
                        onVideoSelected = onVideoSelected
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // 4. Timeline Card
                if (launch.timeline.isNotEmpty()) {
                    Text(
                        text = "Timeline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    TimelineCard(timeline = launch.timeline)
                    Spacer(Modifier.height(16.dp))
                }

                // 5. Mission Details Card
                launch.mission?.let { mission ->
                    Text(
                        text = "Mission Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    MissionDetailsCard(mission = mission, launch = launch)
                    Spacer(Modifier.height(16.dp))
                }

                // 6. Launch Vehicle Details Card
                launch.rocket?.configuration?.let { rocketConfig ->
                    Text(
                        text = "Launch Vehicle Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    LaunchVehicleDetailsCard(rocketConfig = rocketConfig)
                    Spacer(Modifier.height(16.dp))
                    LaunchVehicleDetailedStatistics(rocketConfig = rocketConfig)
                    Spacer(Modifier.height(16.dp))
                }

                // 7. Spacecraft Details Card
                if (!launch.rocket?.spacecraftStage.isNullOrEmpty()) {
                    Text(
                        text = "Spacecraft Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    SpacecraftDetailsCard(
                        spacecraftStages = launch.rocket?.spacecraftStage ?: emptyList()
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // 8. Landing Details Card
                run {
                    val landingStages = launch.rocket?.launcherStage ?: emptyList()
                    if (landingStages.any { it.landing != null }) {
                        Text(
                            text = "Landing Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        LandingDetailsCard(launcherStages = landingStages)
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // 9. Agency Card
                launch.launchServiceProvider.let { agency ->
                    Text(
                        text = "Launch Service Provider",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    AgencyDetailsCard(agency = agency)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Launch Service Provider Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    AgencyLaunchStatistics(agency = agency)
                }
            }
        }

        // Bottom spacing
        Spacer(Modifier.height(200.dp))
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun QuickStatsGrid(launch: LaunchDetailed) {
    // Build a dynamic list of facts that are available
    data class Fact(val icon: ImageVector, val value: String, val label: String)

    val currentYear = System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

    val facts = buildList {
        launch.orbitalLaunchAttemptCount?.let { count ->
            add(Fact(Icons.Filled.Public, "#${count}", "Launch\nAll Time"))
        }
        launch.padLaunchAttemptCount?.let { count ->
            add(Fact(Icons.Filled.Place, "#${count}", "Location\nAll Time"))
        }
        launch.orbitalLaunchAttemptCountYear?.let { count ->
            add(Fact(Icons.Filled.CalendarToday, "#${count}", "Total\n$currentYear"))
        }
        launch.agencyLaunchAttemptCountYear?.let { count ->
            add(
                Fact(
                    Icons.Filled.Business,
                    "#${count}",
                    "${launch.launchServiceProvider.name}\n$currentYear"
                )
            )
        }
    }

    if (facts.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Facts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Render in rows of two cards per row
        facts.chunked(2).forEach { rowFacts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFacts.forEach { fact ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = fact.icon,
                        value = fact.value,
                        label = fact.label
                    )
                }
                if (rowFacts.size == 1) {
                    // Balance layout if odd count
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MissionDetailsCard(mission: Mission, launch: LaunchDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = mission.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Locked to Chips layout
            MissionDetailsChipsContent(launch)
        }
    }
}

@Composable
private fun MissionDetailsChipsContent(launch: LaunchDetailed) {
    val mission = launch.mission ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Tag chips row for type and orbit
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            mission.type.takeIf { it.isNotBlank() }?.let { type ->
                item { InfoChip(icon = Icons.Filled.Category, text = type) }
            }
            launch.launchDesignator?.takeIf { it.isNotBlank() }?.let { launchDesignator ->
                item { InfoChip(icon = Icons.Filled.Details, text = launchDesignator) }
            }
            mission.orbit?.name?.takeIf { it.isNotBlank() }?.let { orbitName ->
                item { InfoChip(icon = Icons.Filled.Public, text = orbitName) }
            }
        }

        // Collapsible description
        mission.description?.takeIf { it.isNotBlank() }?.let { desc ->
            var expanded by remember { mutableStateOf(false) }
            var hasOverflow by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    onTextLayout = { result ->
                        // Only update overflow state when not expanded to avoid flicker
                        if (!expanded) hasOverflow = result.hasVisualOverflow
                    }
                )
                if (hasOverflow || expanded) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Read less" else "Read more")
                    }
                }
            }
        }

        // Agencies chips (if present)
        if (!mission.agencies.isNullOrEmpty()) {
            Text(
                text = "Agencies",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mission.agencies) { agency ->
                    AgencyChip(agencyName = agency.name)
                }
            }
        }
    }
}

@Composable
private fun MissionDetailsGridContent(mission: Mission) {
    val tiles = buildList {
        mission.type?.takeIf { it.isNotBlank() }?.let {
            add(Triple(Icons.Filled.Category, "Type", it))
        }
        mission.orbit?.name?.takeIf { it.isNotBlank() }?.let {
            add(Triple(Icons.Filled.Public, "Target Orbit", it))
        }
        if (!mission.agencies.isNullOrEmpty()) {
            val count = mission.agencies.size
            add(Triple(Icons.Filled.Business, if (count == 1) "Agency" else "Agencies", "$count"))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (tiles.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tiles.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (icon, label, value) ->
                            InfoTile(
                                icon = icon,
                                label = label,
                                value = value,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        // Description below grid if present
        mission.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun MissionDetailsSectionedContent(mission: Mission) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // About
        mission.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            var expanded by remember { mutableStateOf(false) }
            var hasOverflow by remember { mutableStateOf(false) }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                onTextLayout = { result ->
                    if (!expanded) hasOverflow = result.hasVisualOverflow
                }
            )
            if (hasOverflow || expanded) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Show less" else "Show more")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // Details
        val details = buildList {
            mission.type?.takeIf { it.isNotBlank() }?.let { add("Mission Type" to it) }
            mission.orbit?.name?.takeIf { it.isNotBlank() }?.let { add("Target Orbit" to it) }
        }
        if (details.isNotEmpty()) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                details.forEach { (label, value) ->
                    InfoRow(
                        icon = if (label.contains("Orbit")) Icons.Filled.Public else Icons.Filled.Category,
                        label = label,
                        value = value
                    )
                }
            }
        }

        // Agencies
        if (!mission.agencies.isNullOrEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = if (mission.agencies.size == 1) "Agency" else "Agencies",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mission.agencies) { agency ->
                    AgencyChip(agencyName = agency.name)
                }
            }
        }
    }
}

@Composable
private fun MissionDetailsLinearContent(mission: Mission) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mission description
        mission.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }

        // Mission type and orbit
        mission.type?.let { type ->
            InfoRow(
                icon = Icons.Filled.Category,
                label = "Mission Type",
                value = type,
            )
        }

        mission.orbit?.name?.let { orbitName ->
            InfoRow(
                icon = Icons.Filled.Public,
                label = "Target Orbit",
                value = orbitName
            )
        }

        // Agencies involved
        if (!mission.agencies.isNullOrEmpty()) {
            Text(
                text = "Agencies",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mission.agencies) { agency ->
                    AgencyChip(agencyName = agency.name)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 96.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun PrecisionBadge(
    netPrecision: NetPrecision?,
) {
    val ui = remember(netPrecision) { mapNetPrecisionUi(netPrecision) }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (ui.secondary != null) {
                    expanded = !expanded
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = ui.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                modifier = Modifier.size(16.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = ui.primary,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AnimatedVisibility(
                    visible = expanded && ui.secondary != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
                ) {
                    ui.secondary?.let { secondary ->
                        Text(
                            text = secondary,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class PrecisionUi(
    val icon: ImageVector,
    val primary: String,
    val secondary: String?
)

private fun mapNetPrecisionUi(netPrecision: NetPrecision?): PrecisionUi {
    // Defaults
    val defaultPrimary = netPrecision?.name?.takeIf { it.isNotBlank() }
        ?: netPrecision?.abbrev?.takeIf { it.isNotBlank() }
        ?: "Unknown"
    val fallbackSecondary = netPrecision?.description?.takeIf { it.isNotBlank() }

    // Choose icon and concise secondary by id when available
    return when (netPrecision?.id) {
        0 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        1 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        2 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        3 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        4 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        5 -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        7 -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        8, 9, 10 -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        else -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )
    }
}

@Composable
private fun LiveBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Red,
            modifier = Modifier.size(8.dp)
        ) {}
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun LaunchVehicleDetailsCard(rocketConfig: LauncherConfigDetailed) {
    val sharingService = koinInject<LaunchSharingService>()
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = rocketConfig.fullName ?: "Unknown Rocket",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Build info tiles and render as two-column grid
            val infoTiles = buildList {
                rocketConfig.manufacturer?.name?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Filled.Factory, "Manufacturer", it))
                }
                rocketConfig.variant?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Filled.Badge, "Variant", it))
                }
                rocketConfig.alias?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Filled.Label, "Alias", it))
                }
                if (!rocketConfig.families.isNullOrEmpty()) {
                    val familyNames = rocketConfig.families.joinToString(", ") { it.name }
                    if (familyNames.isNotBlank()) {
                        add(
                            Triple(
                                Icons.Filled.Category,
                                if (rocketConfig.families.size == 1) "Family" else "Families",
                                familyNames
                            )
                        )
                    }
                }
                val stages = listOfNotNull(rocketConfig.minStage, rocketConfig.maxStage)
                if (stages.isNotEmpty()) {
                    val stageText =
                        if (rocketConfig.minStage != null && rocketConfig.maxStage != null && rocketConfig.minStage != rocketConfig.maxStage) "${rocketConfig.minStage}-${rocketConfig.maxStage}" else (rocketConfig.minStage
                            ?: rocketConfig.maxStage).toString()
                    add(Triple(Icons.Filled.Stairs, "Stages", stageText))
                }
                rocketConfig.maidenFlight?.let {
                    add(
                        Triple(
                            Icons.Filled.CalendarMonth,
                            "Maiden Flight",
                            it.toString()
                        )
                    )
                }
                rocketConfig.fastestTurnaround?.takeIf { it.isNotBlank() }
                    ?.let {
                        add(
                            Triple(
                                Icons.Filled.Timelapse,
                                "Fastest Turnaround",
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
            }
            if (infoTiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoTiles.chunked(2).forEachIndexed { rowIndex, row ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(rowIndex * 150L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (icon, label, value) ->
                                    InfoTile(
                                        icon = icon,
                                        label = label,
                                        value = value,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Description (overflow-aware)
            rocketConfig.description?.takeIf { it.isNotBlank() }?.let { desc ->
                var expanded by remember { mutableStateOf(false) }
                var hasOverflow by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        onTextLayout = { result ->
                            if (!expanded) hasOverflow = result.hasVisualOverflow
                        }
                    )
                    if (hasOverflow || expanded) {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "Show less" else "Show more")
                        }
                    }
                }
            }

            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rocketConfig.active == true) {
                    StatusChip(text = "Active", color = Color(0xFF4CAF50))
                }
                if (rocketConfig.reusable == true) {
                    StatusChip(text = "Reusable", color = Color(0xFF2196F3))
                }
                if (rocketConfig.isPlaceholder == true) {
                    StatusChip(text = "Placeholder", color = MaterialTheme.colorScheme.outline)
                }
            }

            // Specifications section
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Build all specification items in a single list
            val allSpecs = buildList {
                rocketConfig.length?.let { length ->
                    add(Triple("Length", "${length}m", Icons.Filled.Height))
                }
                rocketConfig.diameter?.let { diameter ->
                    add(Triple("Diameter", "${diameter}m", Icons.Filled.ViewColumn))
                }
                rocketConfig.launchMass?.let { mass ->
                    add(Triple("Launch Mass", "${mass.toInt()}kg", Icons.Filled.Scale))
                }
                rocketConfig.launchCost?.let { cost ->
                    add(Triple("Launch Cost", "${cost}", Icons.Filled.AttachMoney))
                }
                rocketConfig.leoCapacity?.let { leo ->
                    add(Triple("LEO", "${leo.toInt()}kg", Icons.Filled.Public))
                }
                rocketConfig.gtoCapacity?.let { gto ->
                    add(Triple("GTO", "${gto.toInt()}kg", Icons.Filled.Satellite))
                }
                rocketConfig.geoCapacity?.let { geo ->
                    add(Triple("GEO", "${geo.toInt()}kg", Icons.Filled.SatelliteAlt))
                }
                rocketConfig.ssoCapacity?.let { sso ->
                    add(Triple("SSO", "${sso.toInt()}kg", Icons.Filled.AltRoute))
                }
                rocketConfig.toThrust?.let { thrust ->
                    add(Triple("Thrust", "${thrust}", Icons.Filled.Speed))
                }
                rocketConfig.apogee?.let { apogee ->
                    add(Triple("Apogee", "$apogee", Icons.Filled.TrendingUp))
                }
            }

            // Display specifications in a 3-column grid
            if (allSpecs.isNotEmpty()) {
                allSpecs.chunked(3).forEach { rowSpecs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSpecs.forEach { (label, value, icon) ->
                            SpecCard(
                                label = label,
                                value = value,
                                icon = icon,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining slots with spacers if needed
                        repeat(3 - rowSpecs.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    if (rowSpecs != allSpecs.chunked(3).last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Links (Info / Wiki)
            if (!rocketConfig.infoUrl.isNullOrBlank() || !rocketConfig.wikiUrl.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rocketConfig.infoUrl?.let { url ->
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    sharingService.shareUrl(url)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Information",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Additional Information")
                        }
                    }
                    rocketConfig.wikiUrl?.let { url ->
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    sharingService.shareUrl(url)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Brands.WikipediaW,
                                contentDescription = "Wikipedia",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wikipedia")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaunchVehicleDetailedStatistics(rocketConfig: LauncherConfigDetailed) {

    Column(
//        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Launch Statistics
        val totalLaunches = rocketConfig.totalLaunchCount ?: 0
        val successfulLaunches = rocketConfig.successfulLaunches ?: 0
        val failedLaunches = rocketConfig.failedLaunches ?: 0
        val pendingLaunches = rocketConfig.pendingLaunches ?: 0
        val consecutiveSuccessful = rocketConfig.consecutiveSuccessfulLaunches ?: 0
        if (totalLaunches > 0) {
            Text(
                text = "Launch Vehicle Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            val successRate = (successfulLaunches * 100.0 / totalLaunches)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.TrendingUp,
                    value = "${successRate.toInt()}%",
                    label = "Success\nRate",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.BarChart,
                    value = "$totalLaunches",
                    label = "Total\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
                    value = "$successfulLaunches",
                    label = "Successful\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Cancel,
                    value = "$failedLaunches",
                    label = "Failed\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pendingLaunches > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Pending,
                        value = "$pendingLaunches",
                        label = "Pending\nLaunches",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (consecutiveSuccessful > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.ThumbUp,
                        value = "$consecutiveSuccessful",
                        label = "Consecutive\nSuccess",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Landing Statistics
        val attemptedLandings = rocketConfig.attemptedLandings ?: 0
        val successfulLandings = rocketConfig.successfulLandings ?: 0
        val failedLandings = rocketConfig.failedLandings ?: 0
        val consecutiveSuccessfulLandings = rocketConfig.consecutiveSuccessfulLandings ?: 0
        if (attemptedLandings + successfulLandings + failedLandings + consecutiveSuccessfulLandings > 0) {
            Text(
                text = "Landing Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (attemptedLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Assessment,
                        value = "$attemptedLandings",
                        label = "Attempted\nLandings",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (successfulLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Check,
                        value = "$successfulLandings",
                        label = "Successful\nLandings",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (failedLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Close,
                        value = "$failedLandings",
                        label = "Failed\nLandings",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (consecutiveSuccessfulLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.ThumbUp,
                        value = "$consecutiveSuccessfulLandings",
                        label = "Consecutive\nSuccess",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SpacecraftDetailsCard(spacecraftStages: List<SpacecraftFlightDetailedSerializerNoLaunch>) {
    if (spacecraftStages.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            spacecraftStages.forEach { spacecraft ->
                SpacecraftItem(spacecraft = spacecraft)
                if (spacecraft != spacecraftStages.last()) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpacecraftItem(spacecraft: SpacecraftFlightDetailedSerializerNoLaunch) {
    val useUtc = LocalUseUtc.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Spacecraft name and destination
        spacecraft.spacecraft?.name?.let { name ->
            InfoRow(
                icon = Icons.Filled.Rocket,
                label = "Spacecraft",
                value = name
            )
        }

        spacecraft.destination?.let { destination ->
            InfoRow(
                icon = Icons.Filled.LocationOn,
                label = "Destination",
                value = destination
            )
        }

        // Mission duration and landing info
        spacecraft.missionEnd?.let { endDate ->
            InfoRow(
                icon = Icons.Filled.Schedule,
                label = "Mission End",
                value = formatLaunchTime(endDate, useUtc)
            )
        }

        spacecraft.landing?.type?.name?.let { landingType ->
            InfoRow(
                icon = Icons.Filled.FlightLand,
                label = "Landing Type",
                value = landingType
            )
        }
    }
}

@Composable
private fun LandingDetailsCard(launcherStages: List<FirstStageNormal>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val stagesWithLanding = launcherStages.filter { it.landing != null }

            // Guard: if nothing to show, skip rendering content
            if (stagesWithLanding.isEmpty()) return@Column

            if (stagesWithLanding.size == 1) {
                // Single landing: show a grid in a single inner card with description
                val stage = stagesWithLanding.first()
                Column(
                    Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header with stage index and serial if available
                    val header = buildString {
                        append("Stage 1")
                        stage.launcher.serialNumber?.takeIf { it.isNotBlank() }?.let {
                            append(" • ")
                            append(it)
                        }
                    }
                    Text(
                        text = header,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    stage.landing?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        var expanded by remember { mutableStateOf(false) }
                        var hasOverflow by remember { mutableStateOf(false) }
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp,
                            onTextLayout = { result ->
                                if (!expanded) hasOverflow = result.hasVisualOverflow
                            }
                        )
                        if (hasOverflow || expanded) {
                            TextButton(onClick = {
                                expanded = !expanded
                            }) { Text(if (expanded) "Read less" else "Read more") }
                        }
                    }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    LandingStageGridContent(stage)

                }
            } else {
                // Multiple landings: show a horizontal scroller of cards
                val listState = rememberLazyListState()
                val flingBehavior = rememberSnapFlingBehavior(
                    lazyListState = listState,
                    snapPosition = SnapPosition.Start
                )
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    flingBehavior = flingBehavior
                ) {
                    itemsIndexed(
                        items = stagesWithLanding,
                        key = { _, stage -> stage.id }
                    ) { index, stage ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .widthIn(min = 260.dp)
                        ) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header with stage index and serial if available
                                val header = buildString {
                                    append("Stage ${index + 1}")
                                    stage.launcher.serialNumber?.takeIf { it.isNotBlank() }?.let {
                                        append(" • ")
                                        append(it)
                                    }
                                }
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LandingStageGridContent(stage)
                                stage.landing?.description?.takeIf { it.isNotBlank() }
                                    ?.let { desc ->
                                        var expanded by remember { mutableStateOf(false) }
                                        var hasOverflow by remember { mutableStateOf(false) }
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = if (expanded) 6 else 3,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 18.sp,
                                            onTextLayout = { result ->
                                                if (!expanded) hasOverflow =
                                                    result.hasVisualOverflow
                                            }
                                        )
                                        if (hasOverflow || expanded) {
                                            TextButton(onClick = { expanded = !expanded }) {
                                                Text(if (expanded) "Read less" else "Read more")
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Landing Details Style enum and implementations

private enum class LandingDetailsStyle { Chips, Grid, Sections, Linear }

@Composable
private fun LandingStylePicker(
    selected: LandingDetailsStyle,
    onSelected: (LandingDetailsStyle) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val options = listOf(
            LandingDetailsStyle.Chips to "Chips",
            LandingDetailsStyle.Grid to "Grid",
            LandingDetailsStyle.Sections to "Sections",
            LandingDetailsStyle.Linear to "Linear",
        )
        options.forEach { (style, label) ->
            val isSelected = style == selected
            if (isSelected) {
                FilledTonalButton(
                    onClick = { onSelected(style) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(label)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelected(style) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun LandingStageChipsContent(stage: FirstStageNormal) {
    val landing = stage.landing
    val useUtc = LocalUseUtc.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { InfoChip(icon = Icons.Filled.Category, text = stage.type) }
            stage.reused?.let { reused ->
                item {
                    InfoChip(
                        icon = Icons.Filled.Repeat,
                        text = if (reused) "Reused" else "New"
                    )
                }
            }
            stage.launcherFlightNumber?.let { num ->
                item {
                    InfoChip(
                        icon = Icons.Filled.Label,
                        text = "Flight #$num"
                    )
                }
            }
            landing?.type?.name?.let { typeName ->
                item {
                    InfoChip(
                        icon = Icons.Filled.FlightLand,
                        text = typeName
                    )
                }
            }
            landing?.success?.let { success ->
                item {
                    InfoChip(
                        icon = if (success) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        text = if (success) "Success" else "Failure"
                    )
                }
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            landing?.landingLocation?.name?.let { name ->
                item {
                    InfoChip(
                        icon = Icons.Filled.LocationOn,
                        text = name
                    )
                }
            }
            landing?.downrangeDistance?.let { dr ->
                item {
                    InfoChip(
                        icon = Icons.Filled.TrendingUp,
                        text = "${dr} km"
                    )
                }
            }
            stage.previousFlightDate?.let { prev ->
                item {
                    InfoChip(
                        icon = Icons.Filled.Schedule,
                        text = me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime(
                            prev,
                            useUtc
                        )
                    )
                }
            }
            stage.turnAroundTime?.takeIf { it.isNotBlank() }
                ?.let { tat ->
                    item {
                        InfoChip(
                            icon = Icons.Filled.Timelapse,
                            text = parseIsoDurationToHumanReadable(tat)
                        )
                    }
                }
        }
        landing?.description?.takeIf { it.isNotBlank() }?.let { desc ->
            var expanded by remember { mutableStateOf(false) }
            var hasOverflow by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    onTextLayout = { result ->
                        if (!expanded) hasOverflow = result.hasVisualOverflow
                    }
                )
                if (hasOverflow || expanded) {
                    TextButton(onClick = {
                        expanded = !expanded
                    }) { Text(if (expanded) "Read less" else "Read more") }
                }
            }
        }
    }
}

@Composable
private fun LandingStageGridContent(stage: FirstStageNormal) {
    val landing = stage.landing
    val useUtc = LocalUseUtc.current
    val tiles = buildList {
        add(Triple(Icons.Filled.Category, "Stage Type", stage.type))
        stage.reused?.let { add(Triple(Icons.Filled.Repeat, "Reused", if (it) "Yes" else "No")) }
        stage.launcherFlightNumber?.let { add(Triple(Icons.Filled.Label, "Flight #", "$it")) }
        stage.previousFlightDate?.let {
            add(
                Triple(
                    Icons.Filled.Schedule,
                    "Prev. Flight",
                    me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime(it, useUtc)
                )
            )
        }
        stage.turnAroundTime?.takeIf { it.isNotBlank() }
            ?.let {
                add(
                    Triple(
                        Icons.Filled.Timelapse,
                        "Turnaround",
                        parseIsoDurationToHumanReadable(it)
                    )
                )
            }
        landing?.landingLocation?.name?.let { add(Triple(Icons.Filled.LocationOn, "Location", it)) }
        landing?.type?.name?.let { add(Triple(Icons.Filled.FlightLand, "Landing Type", it)) }
        landing?.downrangeDistance?.let {
            add(
                Triple(
                    Icons.Filled.TrendingUp,
                    "Downrange",
                    "${it} km"
                )
            )
        }
        landing?.attempt?.let {
            add(
                Triple(
                    Icons.Filled.FlightTakeoff,
                    "Attempt",
                    if (it) "Yes" else "No"
                )
            )
        }
        landing?.success?.let {
            add(
                Triple(
                    if (it) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    "Result",
                    if (it) "Success" else "Failure"
                )
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (icon, label, value) ->
                    InfoTile(
                        icon = icon,
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LandingStageSectionedContent(stage: FirstStageNormal) {
    val landing = stage.landing
    val useUtc = LocalUseUtc.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        landing?.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            var expanded by remember { mutableStateOf(false) }
            var hasOverflow by remember { mutableStateOf(false) }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                onTextLayout = { result -> if (!expanded) hasOverflow = result.hasVisualOverflow }
            )
            if (hasOverflow || expanded) {
                TextButton(onClick = {
                    expanded = !expanded
                }) { Text(if (expanded) "Show less" else "Show more") }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Text(
            text = "Stage",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        val stageDetails = listOfNotNull(
            "Stage Type" to stage.type,
            stage.reused?.let { "Reused" to if (it) "Yes" else "No" },
            stage.launcherFlightNumber?.let { "Flight #" to "$it" },
            stage.previousFlightDate?.let {
                "Prev. Flight" to me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime(
                    it,
                    useUtc
                )
            },
            stage.turnAroundTime?.takeIf { it.isNotBlank() }
                ?.let { "Turnaround" to parseIsoDurationToHumanReadable(it) },
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            stageDetails.forEach { (label, value) ->
                InfoRow(icon = Icons.Filled.Label, label = label, value = value)
            }
        }

        landing?.let { l ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = "Landing",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            val landingDetails = listOfNotNull(
                l.landingLocation?.name?.let { "Location" to it },
                l.type?.name?.let { "Type" to it },
                l.downrangeDistance?.let { "Downrange" to "${it} km" },
                l.attempt?.let { "Attempt" to if (it) "Yes" else "No" },
                l.success?.let { "Result" to if (it) "Success" else "Failure" },
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                landingDetails.forEach { (label, value) ->
                    InfoRow(icon = Icons.Filled.Label, label = label, value = value)
                }
            }
        }
    }
}

@Composable
private fun LandingStageLinearContent(stage: FirstStageNormal) {
    val landing = stage.landing
    val useUtc = LocalUseUtc.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoRow(icon = Icons.Filled.Category, label = "Stage Type", value = stage.type)
        stage.reused?.let {
            InfoRow(
                icon = Icons.Filled.Repeat,
                label = "Reused",
                value = if (it) "Yes" else "No"
            )
        }
        stage.launcherFlightNumber?.let {
            InfoRow(
                icon = Icons.Filled.Label,
                label = "Flight #",
                value = "$it"
            )
        }
        stage.previousFlightDate?.let {
            InfoRow(
                icon = Icons.Filled.Schedule,
                label = "Prev. Flight",
                value = me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime(
                    it,
                    useUtc
                )
            )
        }
        stage.turnAroundTime?.takeIf { it.isNotBlank() }
            ?.let {
                InfoRow(
                    icon = Icons.Filled.Timelapse,
                    label = "Turnaround",
                    value = parseIsoDurationToHumanReadable(it)
                )
            }
        landing?.landingLocation?.name?.let {
            InfoRow(
                icon = Icons.Filled.LocationOn,
                label = "Landing Location",
                value = it
            )
        }
        landing?.type?.name?.let {
            InfoRow(
                icon = Icons.Filled.FlightLand,
                label = "Landing Type",
                value = it
            )
        }
        landing?.downrangeDistance?.let {
            InfoRow(
                icon = Icons.Filled.TrendingUp,
                label = "Downrange",
                value = "${it} km"
            )
        }
        landing?.attempt?.let {
            InfoRow(
                icon = Icons.Filled.FlightTakeoff,
                label = "Attempt",
                value = if (it) "Yes" else "No"
            )
        }
        landing?.success?.let {
            InfoRow(
                icon = if (it) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                label = "Result",
                value = if (it) "Success" else "Failure"
            )
        }
        landing?.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(text = desc, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
        }
    }
}


@Composable
private fun AgencyDetailsCard(agency: AgencyDetailed) {
    val sharingService = koinInject<LaunchSharingService>()
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Agency header (logo larger and centered)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Agency logo
                SubcomposeAsyncImage(
                    model = agency.logo?.imageUrl ?: "",
                    contentDescription = "Agency logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Agency",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Agency",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                )

                Text(
                    text = agency.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid of key details (above countries)
            val infoTiles = buildList {
                agency.type?.name?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Filled.Business, "Type", it))
                }
                agency.foundingYear?.let { year ->
                    add(Triple(Icons.Filled.CalendarToday, "Founded", year.toString()))
                }
                agency.administrator?.takeIf { it.isNotBlank() }?.let { admin ->
                    add(Triple(Icons.Filled.Person, "Administrator", admin))
                }
            }
            if (infoTiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoTiles.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (icon, label, value) ->
                                InfoTile(
                                    icon = icon,
                                    label = label,
                                    value = value,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Countries as chips (moved below grid)
            if (agency.country.isNotEmpty()) {
                CountryInfoRow(countries = agency.country)
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                agency.infoUrl?.let { infoUrl ->
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                sharingService.shareUrl(infoUrl)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("INFO")
                    }
                }

                agency.wikiUrl?.let { wikiUrl ->
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                sharingService.shareUrl(wikiUrl)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("WIKI")
                    }
                }
            }

            // Agency description
            agency.description?.let { description ->
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun AgencyLaunchStatistics(agency: AgencyDetailed) {
    Column(
//        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Launch statistics
        val totalLaunches = agency.totalLaunchCount ?: 0
        if (totalLaunches > 0) {

            val successfulLaunches = agency.successfulLaunches ?: 0
            val failedLaunches = agency.failedLaunches ?: 0
            val successRate =
                if (totalLaunches > 0) (successfulLaunches * 100.0 / totalLaunches) else 0.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.TrendingUp,
                    value = "${successRate.toInt()}%",
                    label = "Success\nRate",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.RocketLaunch,
                    value = "$totalLaunches",
                    label = "Total\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
                    value = "$successfulLaunches",
                    label = "Successful\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Cancel,
                    value = "$failedLaunches",
                    label = "Failed\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpecCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CountryInfoRow(countries: List<Country>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row with icon and label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (countries.size == 1) "Country" else "Countries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // List of countries below the header
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(countries) { country ->
                CountryChip(country = country)
            }
        }
    }
}

@Composable
private fun CountryChip(country: Country) {
    val label = country.name ?: country.alpha2Code ?: "Unknown"
    val flagUrl = country.alpha2Code?.let { code ->
        "https://flagcdn.com/w40/${code.lowercase()}.png"
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = flagUrl?.let { url ->
            {
                AsyncImage(
                    model = url,
                    contentDescription = "Flag of ${country.name}",
                    modifier = Modifier
                        .width(18.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun AgencyChip(agencyName: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Text(
            text = agencyName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// Video Player Card composable (wraps LaunchVideoPlayer with optional picker in a Card)
@Composable
private fun VideoPlayerCard(
    videoPlayerState: me.calebjones.spacelaunchnow.ui.viewmodel.VideoPlayerState,
    launchName: String,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Common video player
            val selectedVideo =
                videoPlayerState.availableVideos[videoPlayerState.selectedVideoIndex]
            LaunchVideoPlayer(
                vidUrl = selectedVideo,
                launchName = launchName,
                isPlayerVisible = videoPlayerState.isPlayerVisible,
                onSetPlayerVisible = onSetPlayerVisible,
                onNavigateToFullscreen = onNavigateToFullscreen,
                modifier = Modifier.fillMaxWidth()
            )

            // Video picker (only show if there are multiple videos)
            if (videoPlayerState.availableVideos.size > 1) {
                VideoPickerDropdown(
                    videos = videoPlayerState.availableVideos,
                    selectedIndex = videoPlayerState.selectedVideoIndex,
                    launchName = launchName,
                    onVideoSelected = onVideoSelected
                )
            }
        }
    }
}

// Separate video picker component
@Composable
private fun VideoPickerDropdown(
    videos: List<me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL>,
    selectedIndex: Int,
    launchName: String,
    onVideoSelected: (Int) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedVideo = videos[selectedIndex]

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { isDropdownExpanded = !isDropdownExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = VideoUtil.getVideoTitle(selectedVideo, launchName),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )

                Icon(
                    imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isDropdownExpanded) "Close menu" else "Open menu"
                )
            }
        }

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            videos.forEachIndexed { index, video ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = VideoUtil.getVideoTitle(video, launchName),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (index == selectedIndex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onVideoSelected(index)
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}

// TimelineCard: shows a vertical timeline with expand/collapse

@Composable
private fun TimelineCard(timeline: List<TimelineEvent>) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Timeline vertical
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val displayItems = if (expanded) timeline else timeline.take(4)
                displayItems.forEachIndexed { idx, event ->
                    TimelineEventRow(
                        event = event,
                        isFirst = idx == 0,
                        isLast = idx == displayItems.lastIndex
                    )
                }
            }

            // Show more button centered at bottom (if there are more than 4 items)
            if (timeline.size > 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Text(if (expanded) "Show less" else "Show more")
                    }
                }
            }
        }
    }
}


@Composable
private fun TimelineEventRow(
    event: TimelineEvent,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator column (dot and connecting lines)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            // Top connecting line (if not first item) - extends from previous item
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }

            // Timeline dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )

            // Bottom connecting line (if not last item) - extends to next item
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content column with event details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 12.dp else 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Event abbreviation/name
                    Text(
                        text = event.type?.abbrev ?: "Event",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Event description
                    if (!event.type?.description.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.type?.description ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Relative time at the end of the line
                Text(
                    text = formatTimelineRelativeTime(event.relativeTime),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun LaunchDetailLoadingContent() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(TitleHeight))

        // 1. Combined Launch Overview Card shimmer
        LoadingCard(height = 200.dp)
        Spacer(Modifier.height(16.dp))

        // 2. Quick Stats Grid shimmer
        Text(
            text = "Quick Facts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // Two rows of stat cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
        }
        Spacer(Modifier.height(16.dp))

        // Ad placeholder shimmer below Quick Facts
        LoadingCard(height = 100.dp)
        Spacer(Modifier.height(16.dp))

        // 3. Video Player Card shimmer (optional)
        LoadingCard(height = 250.dp)
        Spacer(Modifier.height(16.dp))

        // 4. Timeline Card shimmer
        LoadingCard(height = 180.dp)
        Spacer(Modifier.height(16.dp))

        // 5. Mission Details Card shimmer
        LoadingCard(height = 150.dp)
        Spacer(Modifier.height(16.dp))

        // 6. Launch Vehicle Details Card shimmer
        LoadingCard(height = 200.dp)
        Spacer(Modifier.height(16.dp))

        // 7. Agency Details Card shimmer
        LoadingCard(height = 180.dp)
        Spacer(Modifier.height(16.dp))

        // Bottom spacing
        Spacer(Modifier.height(200.dp))
    }
}

@Composable
private fun LoadingCard(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shimmer()
            .height(height),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
fun LaunchDetailErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .size(36.dp)
                .background(
                    color = Color(0xff121212).copy(alpha = 0.32f),
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
            )
        }

        // Error content
        ErrorCard(
            errorMessage = errorMessage,
            onRetry = onRetry
        )
    }
}

@Composable
fun LaunchDetailLoadingView(onNavigateBack: () -> Unit) {
    // Use SharedDetailScaffold to match the responsive behavior of the actual detail view
    SharedDetailScaffold(
        titleText = "Loading...",
        taglineText = null,
        imageUrl = null, // No image for loading state
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        scrollEnabled = false,
    ) {
        // Loading content that matches the structure of LaunchDetailContentInBody
        LaunchDetailLoadingContent()
    }
}