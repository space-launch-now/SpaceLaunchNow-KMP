package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.max
import kotlin.math.min
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.ui.SharedElementKey
import me.calebjones.spacelaunchnow.ui.SharedElementType
import me.calebjones.spacelaunchnow.ui.compose.LaunchCountdown
import me.calebjones.spacelaunchnow.ui.compose.LaunchCardHeaderOverlay
import me.calebjones.spacelaunchnow.ui.compose.toLaunchCardData
import me.calebjones.spacelaunchnow.ui.compose.LaunchWindowIndicator
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalNavAnimatedVisibilityScope
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Country
import me.calebjones.spacelaunchnow.api.launchlibrary.models.FirstStageNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Landing
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Mission
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightDetailedSerializerNoLaunch
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchTime
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.api.launchlibrary.models.NetPrecision
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor
import kotlin.time.Duration.Companion.seconds

// Keep only TitleHeight which is used for spacing
private val TitleHeight = 128.dp

@OptIn(ExperimentalSharedTransitionApi::class)
val snackDetailBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.8f, stiffness = 380f)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun LaunchDetailView(
    launch: LaunchDetailed,
    onNavigateBack: () -> Unit
) {
    // Use the shared detail scaffold to unify behavior
    SharedDetailScaffold(
        keyProvider = { type -> SharedElementKey(launch.id, type) },
        titleText = LaunchFormatUtil.formatLaunchTitle(launch),
        taglineText = launch.launchServiceProvider.name,
        imageUrl = launch.image?.imageUrl,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            getLaunchStatusColor(launch.status?.id)
        ),
    ) {
        // Render existing launch detail content inside the shared scaffold body
        LaunchDetailContentInBody(launch = launch)
    }
}


// This composable contains all the detailed launch information
@Composable
private fun LaunchDetailContentInBody(launch: LaunchDetailed) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(TitleHeight - 28.dp))

        // 2. Countdown Card (if upcoming)
        launch.net?.let { launchTime ->
                CountdownCard(launch = launch)
                Spacer(Modifier.height(16.dp))
        }

        // 2b. Launch Window Card (separate card if window exists)
        launch.windowStart?.let { ws ->
            launch.windowEnd?.let { we ->
                LaunchWindowIndicatorCard(
                    launchTime = launch.net ?: ws,
                    windowStart = ws,
                    windowEnd = we
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Now include all the detailed content - no null checks needed
        // 1. Launch Info Card
        LaunchInfoCard(launch = launch)
        Spacer(Modifier.height(16.dp))

        // 3. Quick Stats Grid
        QuickStatsGrid(launch = launch)
        Spacer(Modifier.height(16.dp))
        
        // 4. Mission Details Card
        launch.mission?.let { mission ->
            MissionDetailsCard(mission = mission)
            Spacer(Modifier.height(16.dp))
        }
        
        // 5. Launch Vehicle Details Card
        launch.rocket?.configuration?.let { rocketConfig ->
            LaunchVehicleDetailsCard(rocketConfig = rocketConfig)
            Spacer(Modifier.height(16.dp))
        }
        
        // 6. Spacecraft Details Card
        if (!launch.rocket?.spacecraftStage.isNullOrEmpty()) {
            SpacecraftDetailsCard(spacecraftStages = launch.rocket?.spacecraftStage ?: emptyList())
            Spacer(Modifier.height(16.dp))
        }
        
        // 7. Landing Details Card
        run {
            val landingStages = launch.rocket?.launcherStage ?: emptyList()
            if (landingStages.any { it.landing != null }) {
                LandingDetailsCard(launcherStages = landingStages)
                Spacer(Modifier.height(16.dp))
            }
        }
        
        // 8. Agency Card
        launch.launchServiceProvider?.let { agency ->
            AgencyDetailsCard(agency = agency)
            Spacer(Modifier.height(16.dp))
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
private fun LaunchInfoCardHeroContent(launch: LaunchDetailed) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Hero band
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = launch.net?.let { formatLaunchTime(it) } ?: "TBD",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    launch.net?.let {
                        Text(
                            text = me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTimeRelative(
                                it
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PrecisionBadge(
                        netPrecision = launch.netPrecision,
                    )
                }
            }
        }

        // Metrics grid below
        val tiles = buildList {
            launch.mission?.name?.takeIf { it.isNotBlank() }
                ?.let { add(Triple(Icons.Filled.Rocket, "Mission", it)) }
            launch.launchDesignator?.takeIf { it.isNotBlank() }
                ?.let { add(Triple(Icons.Filled.Tag, "Designation", it)) }
            launch.pad?.let { pad ->
                val site = listOfNotNull(pad.name, pad.location?.name).joinToString(" - ")
                if (site.isNotBlank()) add(Triple(Icons.Filled.LocationOn, "Launch Site", site))
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
            // Hero layout selected by default
            LaunchInfoCardHeroContent(launch)
        }
    }
}

@Composable
private fun LaunchInfoCardLinearContent(launch: LaunchDetailed) {
    Column(
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
            launch.net?.let { launchTime ->
                LaunchCountdown(
                    launchTime = launchTime,
                    statusId = launch.status?.id,
                    statusName = launch.status?.name
                )
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(launch: LaunchDetailed) {
    // Build a dynamic list of facts that are available
    data class Fact(val icon: ImageVector, val value: String, val label: String)

    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

    val facts = buildList {
        launch.probability?.let { prob ->
            add(Fact(Icons.AutoMirrored.Filled.TrendingUp, "${prob}%", "Weather\nProb."))
        }
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
            add(Fact(Icons.Filled.Business, "#${count}", "${launch.launchServiceProvider.name}\n$currentYear"))
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
private fun MissionDetailsCard(mission: Mission) {
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
                text = "Mission Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Locked to Chips layout
            MissionDetailsChipsContent(mission)
        }
    }
}

private enum class MissionDetailsStyle { Chips, Grid, Sections, Linear }

@Composable
private fun MissionStylePicker(
    selected: MissionDetailsStyle,
    onSelected: (MissionDetailsStyle) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val options = listOf(
            MissionDetailsStyle.Chips to "Chips",
            MissionDetailsStyle.Grid to "Grid",
            MissionDetailsStyle.Sections to "Sections",
            MissionDetailsStyle.Linear to "Linear",
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
private fun MissionDetailsChipsContent(mission: Mission) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Tag chips row for type and orbit
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            mission.type?.takeIf { it.isNotBlank() }?.let { type ->
                item { InfoChip(icon = Icons.Filled.Category, text = type) }
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
private fun PrecisionBadge(
    netPrecision: NetPrecision?,
) {

    val ui = remember(netPrecision) { mapNetPrecisionUi(netPrecision) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
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
                    text = "NET: ${ui.primary}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ui.secondary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
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
    val defaultPrimary = netPrecision?.abbrev?.takeIf { it.isNotBlank() }
        ?: netPrecision?.name?.takeIf { it.isNotBlank() }
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
private fun LaunchWindowIndicatorCard(
    launchTime: Instant,
    windowStart: Instant,
    windowEnd: Instant,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        LaunchWindowIndicator(
            launchTime = launchTime,
            windowStart = windowStart,
            windowEnd = windowEnd,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun LaunchVehicleDetailsCard(rocketConfig: LauncherConfigDetailed) {
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
                style = MaterialTheme.typography.titleLarge,
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
                    ?.let { add(Triple(Icons.Filled.Timelapse, "Fastest Turnaround", it)) }
            }
            if (infoTiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoTiles.chunked(2).forEach { row ->
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

            // Specifications section - Dimensions & Mass
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
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
                rocketConfig.launchMass?.let { mass ->
                    item {
                        SpecCard(
                            label = "Launch Mass",
                            value = "${mass.toInt()}kg",
                            icon = Icons.Filled.Scale
                        )
                    }
                }
                rocketConfig.launchCost?.let { cost ->
                    item {
                        SpecCard(
                            label = "Launch Cost",
                            value = "${cost}",
                            icon = Icons.Filled.AttachMoney
                        )
                    }
                }
            }

            // Specifications section - Performance
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rocketConfig.leoCapacity?.let { leo ->
                    item {
                        SpecCard(
                            label = "LEO",
                            value = "${leo.toInt()}kg",
                            icon = Icons.Filled.Public
                        )
                    }
                }
                rocketConfig.gtoCapacity?.let { gto ->
                    item {
                        SpecCard(
                            label = "GTO",
                            value = "${gto.toInt()}kg",
                            icon = Icons.Filled.Satellite
                        )
                    }
                }
                rocketConfig.geoCapacity?.let { geo ->
                    item {
                        SpecCard(
                            label = "GEO",
                            value = "${geo.toInt()}kg",
                            icon = Icons.Filled.SatelliteAlt
                        )
                    }
                }
                rocketConfig.ssoCapacity?.let { sso ->
                    item {
                        SpecCard(
                            label = "SSO",
                            value = "${sso.toInt()}kg",
                            icon = Icons.Filled.AltRoute
                        )
                    }
                }
                rocketConfig.toThrust?.let { thrust ->
                    item {
                        SpecCard(
                            label = "Thrust",
                            value = "${thrust}",
                            icon = Icons.Filled.Speed
                        )
                    }
                }
                rocketConfig.apogee?.let { apogee ->
                    item {
                        SpecCard(
                            label = "Apogee",
                            value = "$apogee",
                            icon = Icons.Filled.TrendingUp
                        )
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
                            onClick = { /* TODO: Open info URL */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("INFO") }
                    }
                    rocketConfig.wikiUrl?.let { url ->
                        Button(
                            onClick = { /* TODO: Open wiki URL */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("WIKI") }
                    }
                }
            }

            // Launch Statistics
            val totalLaunches = rocketConfig.totalLaunchCount ?: 0
            val successfulLaunches = rocketConfig.successfulLaunches ?: 0
            val failedLaunches = rocketConfig.failedLaunches ?: 0
            val pendingLaunches = rocketConfig.pendingLaunches ?: 0
            val consecutiveSuccessful = rocketConfig.consecutiveSuccessfulLaunches ?: 0
            if (totalLaunches > 0) {
                Text(
                    text = "Launch Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
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
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.BarChart,
                        value = "$totalLaunches",
                        label = "Total\nLaunches",
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Cancel,
                        value = "$failedLaunches",
                        label = "Failed\nLaunches",
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                    if (consecutiveSuccessful > 0) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.ThumbUp,
                            value = "$consecutiveSuccessful",
                            label = "Consecutive\nSuccess",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
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
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                    if (successfulLandings > 0) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Check,
                            value = "$successfulLandings",
                            label = "Successful\nLandings",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                    if (consecutiveSuccessfulLandings > 0) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.ThumbUp,
                            value = "$consecutiveSuccessfulLandings",
                            label = "Consecutive\nSuccess",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                }
            }

            // Fastest turnaround (already shown in grid if present)
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
private fun SpacecraftItem(spacecraft: SpacecraftFlightDetailedSerializerNoLaunch) {
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
            Text(
                text = "Landing Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

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
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LandingStageGridContent(stage)
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
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
                            prev
                        )
                    )
                }
            }
            stage.turnAroundTime?.takeIf { it.isNotBlank() }
                ?.let { tat -> item { InfoChip(icon = Icons.Filled.Timelapse, text = tat) } }
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
    val tiles = buildList {
        add(Triple(Icons.Filled.Category, "Stage Type", stage.type))
        stage.reused?.let { add(Triple(Icons.Filled.Repeat, "Reused", if (it) "Yes" else "No")) }
        stage.launcherFlightNumber?.let { add(Triple(Icons.Filled.Label, "Flight #", "$it")) }
        stage.previousFlightDate?.let {
            add(
                Triple(
                    Icons.Filled.Schedule,
                    "Prev. Flight",
                    me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime(it)
                )
            )
        }
        stage.turnAroundTime?.takeIf { it.isNotBlank() }
            ?.let { add(Triple(Icons.Filled.Timelapse, "Turnaround", it)) }
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
                    it
                )
            },
            stage.turnAroundTime?.takeIf { it.isNotBlank() }?.let { "Turnaround" to it },
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
                value = me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime(it)
            )
        }
        stage.turnAroundTime?.takeIf { it.isNotBlank() }
            ?.let { InfoRow(icon = Icons.Filled.Timelapse, label = "Turnaround", value = it) }
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
                text = "Launch Service Provider",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

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
                    infoTiles.chunked(2).forEach { row ->
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
            if (!agency.country.isNullOrEmpty()) {
                CountryInfoRow(countries = agency.country)
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
                        label = "Success\nRate",
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.RocketLaunch,
                        value = "$totalLaunches",
                        label = "Total\nLaunches",
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Cancel,
                        value = "$failedLaunches",
                        label = "Failed\nLaunches",
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
private fun CountryInfoRow(countries: List<Country>) {
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
private fun LaunchDetailShimmer() {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Info card shimmer - With padding
            repeat(1) {
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
//        // Back button
//        IconButton(
//            onClick = onNavigateBack,
//            modifier = Modifier
//                .align(Alignment.TopStart)
//                .statusBarsPadding()
//                .padding(16.dp)
//                .size(36.dp)
//                .background(
//                    color = Color(0xff121212).copy(alpha = 0.32f),
//                    shape = CircleShape,
//                ),
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
//                contentDescription = "Back",
//            )
//        }
        
        // Loading content
        LaunchDetailShimmer()
    }
}