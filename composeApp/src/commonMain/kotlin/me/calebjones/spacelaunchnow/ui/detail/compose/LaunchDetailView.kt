package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.max
import kotlin.math.min
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.ui.compose.LaunchCountdown
import me.calebjones.spacelaunchnow.ui.compose.LaunchCardHeaderOverlay
import me.calebjones.spacelaunchnow.ui.compose.toLaunchCardData
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchTime
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchDetailView(
    viewModel: LaunchViewModel,
    launchId: String,
    preloadedLaunchDetailed: LaunchDetailed? = null,
) {
    val launchDetails by viewModel.launchDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Use preloaded data if available, otherwise use fetched data
    val currentLaunch = preloadedLaunchDetailed ?: launchDetails

    LaunchedEffect(launchId) {
        if (preloadedLaunchDetailed != null) {
            // We have preloaded data, set it immediately to avoid loading state
            viewModel.setLaunchDetails(preloadedLaunchDetailed)
        } else {
            // No preloaded data, fetch from API
            viewModel.fetchLaunchDetails(launchId)
        }
    }

    when {
        error != null -> {
            ErrorCard(
                errorMessage = error ?: "Unknown error occurred",
                onRetry = { viewModel.fetchLaunchDetails(launchId) }
            )
        }
        currentLaunch != null -> {
            LaunchDetailContent(
                launch = currentLaunch
            )
        }
        isLoading -> {
            LaunchDetailShimmer()
        }
        else -> {
            ErrorCard(
                errorMessage = "Launch not found",
                onRetry = { viewModel.fetchLaunchDetails(launchId) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LaunchDetailContent(
    launch: LaunchDetailed
) {
    val scrollState = rememberLazyListState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                LaunchDetailHeroSection(launch = launch)
            }

            // Add spacing for the overlaid agency logo
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }

            // 1. Launch Info Card - With padding
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LaunchInfoCard(launch = launch)
                }
            }

            // 2. Countdown Card (if upcoming) - With padding
            item {
                launch.net?.let { launchTime ->
                    if (launchTime > Clock.System.now()) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            CountdownCard(launch = launch)
                        }
                    }
                }
            }

            // 3. Quick Stats Grid - With padding
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    QuickStatsGrid(launch = launch)
                }
            }

            // 4. Mission Details Card - With padding
            launch.mission?.let { mission ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MissionDetailsCard(mission = mission)
                    }
                }
            }

            // 5. Launch Vehicle Details Card - With padding
            launch.rocket?.configuration?.let { rocketConfig ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        LaunchVehicleDetailsCard(rocketConfig = rocketConfig)
                    }
                }
            }

            // 6. Spacecraft Details Card - With padding
            if (!launch.rocket?.spacecraftStage.isNullOrEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SpacecraftDetailsCard(spacecraftStages = launch.rocket?.spacecraftStage ?: emptyList())
                    }
                }
            }

            // 7. Landing Details Card - With padding
            if (!launch.rocket?.launcherStage.isNullOrEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        LandingDetailsCard(launcherStages = launch.rocket?.launcherStage ?: emptyList())
                    }
                }
            }

            // 8. Agency Card - With padding
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    AgencyDetailsCard(agency = launch.launchServiceProvider)
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LaunchDetailHeroSection(launch: LaunchDetailed) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        // Background image - full width
        AsyncImage(
            model = launch.image?.imageUrl ?: "",
            contentDescription = "Launch image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        
        // Launch info at the bottom of hero section
        LaunchCardHeaderOverlay(
            launchData = launch.toLaunchCardData(),
            useRelativeTime = true,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}

@Composable
private fun LaunchInfoCard(launch: LaunchDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mission name
            launch.mission?.name?.let { missionName ->
                InfoRow(
                    icon = Icons.Filled.Rocket,
                    label = "Mission",
                    value = missionName
                )
            }

            // Launch designation
            launch.launchDesignator?.let { designator ->
                InfoRow(
                    icon = Icons.Filled.Tag,
                    label = "Designation",
                    value = designator
                )
            }

            // Launch site
            launch.pad?.let { pad ->
                InfoRow(
                    icon = Icons.Filled.LocationOn,
                    label = "Launch Site",
                    value = "${pad.name} - ${pad.location?.name ?: ""}"
                )
            }

            // Launch time
            launch.net?.let { launchTime ->
                InfoRow(
                    icon = Icons.Filled.Schedule,
                    label = "Launch Time",
                    value = formatLaunchTime(launchTime)
                )
            }

            // Status
            launch.status?.let { status ->
                InfoRow(
                    icon = Icons.Filled.Flag,
                    label = "Status",
                    value = status.name
                )
            }
        }
    }
}

@Composable
private fun CountdownCard(launch: LaunchDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Launch Countdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            launch.net?.let { launchTime ->
                LaunchCountdown(
                    launchTime = launchTime,
                    statusId = launch.status?.id,
                    statusName = launch.status?.name
                )
            }

            // Launch window progress bar
            launch.windowStart?.let { windowStart ->
                launch.windowEnd?.let { windowEnd ->
                    LaunchWindowCard(
                        windowStart = windowStart,
                        windowEnd = windowEnd,
                        currentTime = launch.net
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(launch: LaunchDetailed) {
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
                text = "Quick Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Success probability
                launch.probability?.let { prob ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.TrendingUp,
                        value = "${prob}%",
                        label = "Success\nProb."
                    )
                }

                // Orbital attempts
                launch.orbitalLaunchAttemptCount?.let { count ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Public,
                        value = "#$count",
                        label = "Orbital\nAttempt"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pad attempts
                launch.padLaunchAttemptCount?.let { count ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Place,
                        value = "#$count",
                        label = "Pad\nAttempt"
                    )
                }

                // Year attempts
                launch.orbitalLaunchAttemptCountYear?.let { count ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.CalendarToday,
                        value = "#$count",
                        label = "Year\nAttempt"
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionDetailsCard(mission: me.calebjones.spacelaunchnow.api.launchlibrary.models.Mission) {
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
                text = "🎯 Mission Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

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
                    value = type
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
}

// Helper Composables
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
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
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
    label: String
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun StatusBadge(
    statusName: String,
    statusId: Int?
) {
    val backgroundColor = when (statusId) {
        1, 3 -> Color(0xFF4CAF50) // Go/Success - Green
        2, 4 -> Color(0xFFF44336) // Hold/Failure - Red
        6 -> Color(0xFF2196F3)    // In Flight - Blue
        else -> Color(0xFFFF9800) // Default - Orange
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = statusName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
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
private fun LaunchWindowCard(
    windowStart: Instant,
    windowEnd: Instant,
    currentTime: Instant?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Launch Window",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )

            // Progress bar
            val windowDuration = windowEnd.epochSeconds - windowStart.epochSeconds
            val progress = if (currentTime != null && windowDuration > 0) {
                val elapsed = maxOf(0L, currentTime.epochSeconds - windowStart.epochSeconds)
                (elapsed.toFloat() / windowDuration.toFloat()).coerceIn(0f, 1f)
            } else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun LaunchVehicleDetailsCard(rocketConfig: me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed) {
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
                text = "🚀 Launch Vehicle Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Vehicle name and family
            InfoRow(
                icon = Icons.Filled.Rocket,
                label = "Vehicle",
                value = rocketConfig.fullName ?: rocketConfig.name
            )

            // Handle families as a list
            if (!rocketConfig.families.isNullOrEmpty()) {
                val familyNames = rocketConfig.families.mapNotNull { it.name }.joinToString(", ")
                if (familyNames.isNotEmpty()) {
                    InfoRow(
                        icon = Icons.Filled.Category,
                        label = if (rocketConfig.families.size == 1) "Family" else "Families",
                        value = familyNames
                    )
                }
            }

            // Maiden flight
            rocketConfig.maidenFlight?.let { maidenFlight ->
                InfoRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = "Maiden Flight",
                    value = maidenFlight.toString()
                )
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
            }

            // Specifications section
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Specifications grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rocketConfig.length?.let { length ->
                    item {
                        SpecCard(
                            label = "Length",
                            value = "${length}m",
                            icon = Icons.Filled.Height
                        )
                    }
                }
                rocketConfig.diameter?.let { diameter ->
                    item {
                        SpecCard(
                            label = "Diameter",
                            value = "${diameter}m",
                            icon = Icons.Filled.ViewColumn
                        )
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rocketConfig.leoCapacity?.let { leo ->
                    item {
                        SpecCard(
                            label = "LEO Capacity",
                            value = "${leo.toInt()}kg",
                            icon = Icons.Filled.Public
                        )
                    }
                }
                rocketConfig.gtoCapacity?.let { gto ->
                    item {
                        SpecCard(
                            label = "GTO Capacity",
                            value = "${gto.toInt()}kg",
                            icon = Icons.Filled.Satellite
                        )
                    }
                }
            }

            // Success statistics
            val totalLaunches = rocketConfig.totalLaunchCount ?: 0
            val successfulLaunches = rocketConfig.successfulLaunches ?: 0
            if (totalLaunches > 0) {
                val successRate = (successfulLaunches * 100.0 / totalLaunches)

                Text(
                    text = "Launch Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.TrendingUp,
                        value = "${successRate.toInt()}%",
                        label = "Success\nRate"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.BarChart,
                        value = "$totalLaunches",
                        label = "Total\nLaunches"
                    )
                }
            }
        }
    }
}

@Composable
private fun SpacecraftDetailsCard(spacecraftStages: List<me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightDetailedSerializerNoLaunch>) {
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
            Text(
                text = "🛰️ Spacecraft Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

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
private fun SpacecraftItem(spacecraft: me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightDetailedSerializerNoLaunch) {
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
                value = formatLaunchTime(endDate)
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
private fun LandingDetailsCard(launcherStages: List<me.calebjones.spacelaunchnow.api.launchlibrary.models.FirstStageNormal>) {
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
                text = "🛬 Landing Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            if (launcherStages.isEmpty() || launcherStages.all { it.landing == null }) {
                Text(
                    text = "No landing information available for this mission.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                return@Column
            }

            launcherStages.forEach { stage ->
                stage.landing?.let { landing ->
                    LauncherStageItem(stage = stage, landing = landing)
                }
            }
        }
    }
}

@Composable
private fun LauncherStageItem(
    stage: me.calebjones.spacelaunchnow.api.launchlibrary.models.FirstStageNormal,
    landing: me.calebjones.spacelaunchnow.api.launchlibrary.models.Landing
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Landing attempt status
        landing.attempt?.let { attempt ->
            InfoRow(
                icon = if (attempt) Icons.Filled.FlightTakeoff else Icons.Filled.Cancel,
                label = "Landing Attempt",
                value = if (attempt) "Yes" else "No"
            )
        }

        // Landing location
        landing.landingLocation?.name?.let { location ->
            InfoRow(
                icon = Icons.Filled.LocationOn,
                label = "Landing Location",
                value = location
            )
        }

        // Landing type
        landing.type?.name?.let { type ->
            InfoRow(
                icon = Icons.Filled.Category,
                label = "Landing Type",
                value = type
            )
        }

        // Landing success
        landing.success?.let { success ->
            InfoRow(
                icon = if (success) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                label = "Landing Status",
                value = if (success) "Successful" else "Failed"
            )
        }

        // Previous flights (if reused booster)
        stage.launcher?.flights?.let { flights ->
            if (flights > 1) {
                InfoRow(
                    icon = Icons.Filled.Repeat,
                    label = "Previous Flights",
                    value = "${flights - 1}"
                )
            }
        }
    }
}

@Composable
private fun AgencyDetailsCard(agency: me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyDetailed) {
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
                text = "🏢 Launch Service Provider",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Agency header with logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Agency logo
                AsyncImage(
                    model = agency.logo?.imageUrl ?: "",
                    contentDescription = "Agency logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = agency.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    agency.abbrev?.let { abbrev ->
                        Text(
                            text = abbrev,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Agency details
            agency.type?.name?.let { type ->
                InfoRow(
                    icon = Icons.Filled.Business,
                    label = "Type",
                    value = type
                )
            }

            // Country with flag
            if (!agency.country.isNullOrEmpty()) {
                CountryInfoRow(countries = agency.country)
            }

            agency.foundingYear?.let { year ->
                InfoRow(
                    icon = Icons.Filled.CalendarToday,
                    label = "Founded",
                    value = year.toString()
                )
            }

            agency.administrator?.let { admin ->
                InfoRow(
                    icon = Icons.Filled.Person,
                    label = "Administrator",
                    value = admin
                )
            }

            // Launch statistics
            val totalLaunches = agency.totalLaunchCount ?: 0
            if (totalLaunches > 0) {
                Text(
                    text = "Launch Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                val successfulLaunches = agency.successfulLaunches ?: 0
                val failedLaunches = agency.failedLaunches ?: 0
                val successRate = if (totalLaunches > 0) (successfulLaunches * 100.0 / totalLaunches) else 0.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.TrendingUp,
                        value = "${successRate.toInt()}%",
                        label = "Success\nRate"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.RocketLaunch,
                        value = "$totalLaunches",
                        label = "Total\nLaunches"
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
                        label = "Successful\nLaunches"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Cancel,
                        value = "$failedLaunches",
                        label = "Failed\nLaunches"
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                agency.infoUrl?.let { infoUrl ->
                    Button(
                        onClick = { /* TODO: Open info URL */ },
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
                        onClick = { /* TODO: Open wiki URL */ },
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
private fun SpecCard(
    label: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
private fun CountryInfoRow(countries: List<me.calebjones.spacelaunchnow.api.launchlibrary.models.Country>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Flag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (countries.size == 1) "Country" else "Countries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(countries) { country ->
                    CountryChip(country = country)
                }
            }
        }
    }
}

@Composable
private fun CountryChip(country: me.calebjones.spacelaunchnow.api.launchlibrary.models.Country) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Country flag using flagcdn.com API
            country.alpha2Code?.let { countryCode ->
                val countryFlag = "https://flagcdn.com/w80/${countryCode.lowercase()}.png"
                println(countryFlag)
                AsyncImage(
                    model = countryFlag,
                    contentDescription = "Flag of ${country.name}",
                    modifier = Modifier
                        .width(24.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            country.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
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
                style = MaterialTheme.typography.titleMedium,
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
private fun LaunchDetailShimmer() {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Hero section shimmer
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Info card shimmer - With padding
            repeat(3) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}