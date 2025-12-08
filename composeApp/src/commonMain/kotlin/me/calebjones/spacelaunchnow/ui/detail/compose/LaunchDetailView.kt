package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.isLargeScreen
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.compose.LaunchCountdown
import me.calebjones.spacelaunchnow.ui.compose.LaunchWindowIndicator
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.ui.detail.compose.components.AgencyDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.AgencyLaunchStatistics
import me.calebjones.spacelaunchnow.ui.detail.compose.components.FlightClubCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LandingDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchLocationCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchUpdatesSection
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchVehicleDetailedStatistics
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchVehicleDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.MissionDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.PadQuickStatsRow
import me.calebjones.spacelaunchnow.ui.detail.compose.components.PrecisionInfoDialog
import me.calebjones.spacelaunchnow.ui.detail.compose.components.QuickStatsGrid
import me.calebjones.spacelaunchnow.ui.detail.compose.components.RelatedNewsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.SpacecraftDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.TimelineCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.VideoPlayerCard
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchTime
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor
import kotlin.time.Clock.System

// Keep only TitleHeight which is used for spacing
private val TitleHeight = 120.dp
private val CompactHeight = 40.dp

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
                            status = launch.status,
                            precision = launch.netPrecision
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
                                                DateTimeUtil.formatLaunchDateTimeRelative(
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
                                                style = MaterialTheme.typography.headlineMedium,
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
                                            if (prob > 0) {
                                                InfoTile(
                                                    icon = Icons.Filled.WbCloudy,
                                                    label = "Weather",
                                                    value = "$prob%",
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }

                                }
                                val windowAllowed = launch.netPrecision?.id in 0..4

                                // Window indicator (if window exists) - now inside the same surface
                                if (launch.windowStart != null && launch.windowEnd != null && windowAllowed) {
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
    videoPlayerState: VideoPlayerState,
    relatedNews: List<me.calebjones.spacelaunchnow.api.snapi.models.Article>,
    isNewsLoading: Boolean,
    newsError: String?,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
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
        // Use LaunchDetailOpenUrlProvider to provide a working openUrl to the body content
        LaunchDetailOpenUrlProvider(
            launch = launch,
            videoPlayerState = videoPlayerState,
            relatedNews = relatedNews,
            isNewsLoading = isNewsLoading,
            newsError = newsError,
            onSelectVideo = onSelectVideo,
            onSetPlayerVisible = onSetPlayerVisible,
            onNavigateBack = onNavigateBack,
            onNavigateToFullscreen = onNavigateToFullscreen,
            onVideoSelected = onVideoSelected,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}


// This composable contains all the detailed launch information
@Composable
fun LaunchDetailOpenUrlProvider(
    launch: LaunchDetailed,
    videoPlayerState: VideoPlayerState,
    relatedNews: List<me.calebjones.spacelaunchnow.api.snapi.models.Article>,
    isNewsLoading: Boolean,
    newsError: String?,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val openUrl: (String) -> Unit = { url ->
        try {
            uriHandler.openUri(url)
        } catch (_: Throwable) {
        }
    }
    LaunchDetailContentInBody(
        launch = launch,
        videoPlayerState = videoPlayerState,
        relatedNews = relatedNews,
        isNewsLoading = isNewsLoading,
        newsError = newsError,
        onSelectVideo = onSelectVideo,
        onSetPlayerVisible = onSetPlayerVisible,
        onNavigateToFullscreen = onNavigateToFullscreen,
        onVideoSelected = onVideoSelected,
        onNavigateToSettings = onNavigateToSettings,
        openUrl = openUrl
    )
}

@Composable
private fun LaunchDetailContentInBody(
    launch: LaunchDetailed,
    videoPlayerState: VideoPlayerState,
    relatedNews: List<me.calebjones.spacelaunchnow.api.snapi.models.Article>,
    isNewsLoading: Boolean,
    newsError: String?,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    openUrl: (String) -> Unit
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
                        placementType = AdPlacementType.INTERSTITIAL,
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
                        
                        // Flight Club link
                        launch.flightclubUrl?.let { url ->
                            Spacer(Modifier.height(8.dp))
                            FlightClubCard(flightClubUrl = url)
                        }
                    }

                    // Updates Section
                    if (launch.updates.isNotEmpty()) {
                        Text(
                            text = "Updates",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        LaunchUpdatesSection(updates = launch.updates)
                    }

                    // Related News Section
                    if (relatedNews.isNotEmpty() || isNewsLoading || newsError != null) {
                        Text(
                            text = "Related News",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        RelatedNewsCard(
                            articles = relatedNews,
                            isLoading = isNewsLoading,
                            error = newsError
                        )
                    }

                    // Mission Details Card
                    launch.mission?.let { mission ->
                        Text(
                            text = "Mission Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        MissionDetailsCard(
                            mission = mission,
                            missionPatchUrl = launch.missionPatches.firstOrNull()?.imageUrl
                        )
                    }

                    launch.pad?.let { pad ->
                        Text(
                            text = "Launch Location",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        LaunchLocationCard(
                            location = pad.location,
                            pad = pad,
                            openUrl = openUrl
                        )
                        Spacer(Modifier.height(8.dp))
                        PadQuickStatsRow(pad)
                        Spacer(Modifier.height(16.dp))
                    }

                    // Spacecraft Details Card
                    if (!launch.rocket?.spacecraftStage.isNullOrEmpty()) {
                        Text(
                            text = "Spacecraft Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        SpacecraftDetailsCard(
                            spacecraftStages = launch.rocket.spacecraftStage
                        )
                    }

                    // Agency Card
                    launch.launchServiceProvider.let { agency ->
                        Text(
                            text = "Launch Service Provider",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        AgencyDetailsCard(agency = agency, openUrl = openUrl)
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
                        LaunchVehicleDetailsCard(rocketConfig = rocketConfig, openUrl = openUrl)
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
                    placementType = AdPlacementType.INTERSTITIAL,
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
                    
                    // Flight Club link
                    launch.flightclubUrl?.let { url ->
                        Spacer(Modifier.height(8.dp))
                        FlightClubCard(flightClubUrl = url)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // 4.5. Updates Section
                if (launch.updates.isNotEmpty()) {
                    Text(
                        text = "Updates",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    LaunchUpdatesSection(updates = launch.updates)
                    Spacer(Modifier.height(16.dp))
                }

                // Related News Section
                if (relatedNews.isNotEmpty() || isNewsLoading || newsError != null) {
                    Text(
                        text = "Related News",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    RelatedNewsCard(
                        articles = relatedNews,
                        isLoading = isNewsLoading,
                        error = newsError
                    )
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
                    MissionDetailsCard(
                        mission = mission,
                        missionPatchUrl = launch.missionPatches.firstOrNull()?.imageUrl
                    )
                    Spacer(Modifier.height(16.dp))
                }

                launch.pad?.let { pad ->
                    Text(
                        text = "Launch Location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    LaunchLocationCard(
                        location = pad.location,
                        pad = pad,
                        openUrl = openUrl
                    )
                    Spacer(Modifier.height(8.dp))
                    PadQuickStatsRow(pad)
                    Spacer(Modifier.height(16.dp))
                }

                // 🚀 INLINE AD #2: After mission/location info, before vehicle details
                SmartBannerAd(
                    modifier = Modifier.fillMaxWidth(),
                    placementType = AdPlacementType.FEED,
                    showRemoveAdsButton = false,
                    showCard = true
                )
                Spacer(Modifier.height(16.dp))

                // 6. Launch Vehicle Details Card
                launch.rocket?.configuration?.let { rocketConfig ->
                    Text(
                        text = "Launch Vehicle Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    LaunchVehicleDetailsCard(rocketConfig = rocketConfig, openUrl = openUrl)
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
                        spacecraftStages = launch.rocket.spacecraftStage
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

                // 🚀 INLINE AD #3: After spacecraft/landing info, before agency details
                SmartBannerAd(
                    modifier = Modifier.fillMaxWidth(),
                    placementType = AdPlacementType.CONTENT,
                    showRemoveAdsButton = false,
                    showCard = true
                )
                Spacer(Modifier.height(16.dp))

                // 9. Agency Card
                launch.launchServiceProvider.let { agency ->
                    Text(
                        text = "Launch Service Provider",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    AgencyDetailsCard(agency = agency, openUrl = openUrl)
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