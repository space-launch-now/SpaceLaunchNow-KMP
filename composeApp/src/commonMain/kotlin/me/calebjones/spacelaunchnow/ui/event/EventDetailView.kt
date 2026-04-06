package me.calebjones.spacelaunchnow.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Update
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.compose.LocalDetailScaffoldCollapsed
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.ui.detail.compose.components.VideoPlayerCard
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable
import org.koin.compose.viewmodel.koinViewModel

private val TitleHeight = 110.dp

@Composable
fun EventDetailView(
    event: EventEndpointDetailed,
    onNavigateBack: () -> Unit,
    videoPlayerState: VideoPlayerState = VideoPlayerState(),
    onSelectVideo: (Int) -> Unit = {},
    onSetPlayerVisible: (Boolean) -> Unit = {},
    onNavigateToFullscreen: (String, String) -> Unit = { _, _ -> },
    onAgencyClick: ((Int) -> Unit)? = null,
    onLaunchClick: ((String) -> Unit)? = null,
    onAstronautClick: ((Int) -> Unit)? = null,
    onSpaceStationClick: ((Int) -> Unit)? = null,
    onOpenUrl: (String) -> Unit = {},
    onExternalVideoOpened: ((String, String) -> Unit)? = null
) {
    SharedDetailScaffold(
        titleText = event.name,
        taglineText = event.type.name,
        imageUrl = event.image?.imageUrl,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        ),
    ) {
        EventDetailContentInBody(
            event = event,
            videoPlayerState = videoPlayerState,
            onSelectVideo = onSelectVideo,
            onSetPlayerVisible = onSetPlayerVisible,
            onNavigateToFullscreen = onNavigateToFullscreen,
            onAgencyClick = onAgencyClick,
            onLaunchClick = onLaunchClick,
            onAstronautClick = onAstronautClick,
            onSpaceStationClick = onSpaceStationClick,
            onOpenUrl = onOpenUrl,
            onExternalVideoOpened = onExternalVideoOpened
        )
    }
}

@Composable
private fun EventDetailContentInBody(
    event: EventEndpointDetailed,
    videoPlayerState: VideoPlayerState = VideoPlayerState(),
    onSelectVideo: (Int) -> Unit = {},
    onSetPlayerVisible: (Boolean) -> Unit = {},
    onNavigateToFullscreen: (String, String) -> Unit = { _, _ -> },
    onAgencyClick: ((Int) -> Unit)? = null,
    onLaunchClick: ((String) -> Unit)? = null,
    onAstronautClick: ((Int) -> Unit)? = null,
    onSpaceStationClick: ((Int) -> Unit)? = null,
    onOpenUrl: (String) -> Unit = {},
    onExternalVideoOpened: ((String, String) -> Unit)? = null
) {
    val isCollapsed = LocalDetailScaffoldCollapsed.current
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (!isCollapsed) {
            Spacer(Modifier.height(TitleHeight - 28.dp))
        }

        // Video player (if videos available)
        if (videoPlayerState.availableVideos.isNotEmpty()) {
            VideoPlayerCard(
                videoPlayerState = videoPlayerState,
                launchName = event.name,
                onSetPlayerVisible = onSetPlayerVisible,
                onNavigateToFullscreen = onNavigateToFullscreen,
                onVideoSelected = onSelectVideo,
                onExternalVideoOpened = onExternalVideoOpened
            )
            Spacer(Modifier.height(16.dp))
        }

        // Feature image (only show if no videos — video player takes this slot)
        if (videoPlayerState.availableVideos.isEmpty()) {
            event.image?.imageUrl?.let { imageUrl ->
                EventFeatureImage(imageUrl = imageUrl, contentDescription = event.name)
                Spacer(Modifier.height(16.dp))
            }
        }

        // Meta card
        EventInfoCard(event)

        Spacer(Modifier.height(16.dp))


        // Banner Ad - positioned after description
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.FEED
        )

        Spacer(Modifier.height(16.dp))

        // Info links card (video links removed — handled by video player above)
        if (event.infoUrls.isNotEmpty()) {
            EventLinksCard(event, onOpenUrl = onOpenUrl)
            Spacer(Modifier.height(16.dp))
        }

        // Programs
        event.program?.takeIf { it.isNotEmpty() }?.let { programs ->
            ProgramsCard(programs)
            Spacer(Modifier.height(16.dp))
        }

        // Updates
        if (event.updates.isNotEmpty()) {
            UpdatesCard(event.updates)
            Spacer(Modifier.height(16.dp))
        }

        // Agencies
        event.agencies.takeIf { it.isNotEmpty() }?.let { list ->
            AgenciesCard(list, onAgencyClick = onAgencyClick)
            Spacer(Modifier.height(16.dp))
        }
        // Astronauts
        event.astronauts?.takeIf { it.isNotEmpty() }?.let { list ->
            AstronautsCard(list, onAstronautClick = onAstronautClick)
            Spacer(Modifier.height(16.dp))
        }

        // Space Stations
        if (event.spacestations.isNotEmpty()) {
            SpaceStationsCard(event.spacestations, onSpaceStationClick = onSpaceStationClick)
            Spacer(Modifier.height(16.dp))
        }

        // Expeditions
        if (event.expeditions.isNotEmpty()) {
            ExpeditionsCard(event.expeditions)
            Spacer(Modifier.height(16.dp))
        }

        // Related Launches
        if (event.launches.isNotEmpty()) {
            RelatedLaunchesCard(event.launches, onLaunchClick = onLaunchClick)
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(200.dp))
    }
}

@Composable
private fun EventFeatureImage(imageUrl: String, contentDescription: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun EventInfoCard(event: EventEndpointDetailed) {
    val useUtc = LocalUseUtc.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description
            event.description?.let { desc ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(desc, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                }
            }

            // Event Details Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Event Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                event.date?.let { date ->
                    InfoRow(
                        label = "Date",
                        value = formatLaunchDateTime(date, useUtc)
                    )
                }

                event.location?.let { location ->
                    InfoRow(label = "Location", value = location)
                }

                event.duration?.let { duration ->
                    InfoRow(label = "Duration", value = parseIsoDurationToHumanReadable(duration))
                }

                if (event.webcastLive == true) {
                    InfoRow(label = "Status", value = "Live Now 🔴")
                }

                event.lastUpdated?.let { updated ->
                    InfoRow(
                        label = "Last Updated",
                        value = formatLaunchDateTime(updated, useUtc)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventLinksCard(event: EventEndpointDetailed, onOpenUrl: (String) -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Links",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            event.infoUrls.forEach { u ->
                Button(onClick = {
                    onOpenUrl(u.url)
                }) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(u.title ?: "Info", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun AgenciesCard(agencies: List<AgencyMini>, onAgencyClick: ((Int) -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (agencies.size == 1) "Agency" else "Agencies",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            agencies.forEach { agency ->
                AgencyRowWithLogo(
                    agency = agency,
                    onClick = { onAgencyClick?.invoke(agency.id) }
                )
            }
        }
    }
}

@Composable
private fun AgencyRowWithLogo(agency: AgencyMini, onClick: () -> Unit) {
    val launchViewModel = koinViewModel<LaunchViewModel>()
    val agencyMap by launchViewModel.agencyDataMap.collectAsState()
    val detailed = agencyMap[agency.id]

    LaunchedEffect(agency.id) {
        if (detailed == null) {
            launchViewModel.fetchAgencyData(agency.id)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Logo or fallback avatar
        val logoUrl = detailed?.socialLogo?.imageUrl
        if (!logoUrl.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = "Agency logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = agency.abbrev?.uppercase() ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                agency.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            agency.abbrev?.let { ab ->
                Text(
                    ab,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AstronautsCard(
    astronauts: List<AstronautNormal>,
    onAstronautClick: ((Int) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Astronauts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            astronauts.forEach { astro ->
                RowWithImage(
                    title = astro.name ?: "Astronaut",
                    subtitle = astro.nationality.firstOrNull()?.name ?: "",
                    imageUrl = astro.image?.imageUrl,
                    fallbackText = astro.name?.firstOrNull()?.uppercase() ?: "",
                    onClick = { onAstronautClick?.invoke(astro.id) }
                )
            }
        }
    }
}

@Composable
private fun SpaceStationsCard(
    stations: List<SpaceStationNormal>,
    onSpaceStationClick: ((Int) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Space Stations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            stations.forEach { ss ->
                RowWithImage(
                    title = ss.name,
                    subtitle = ss.orbit ?: "",
                    imageUrl = ss.image?.imageUrl,
                    fallbackText = ss.name.firstOrNull()?.uppercase() ?: "",
                    onClick = { onSpaceStationClick?.invoke(ss.id) }
                )
            }
        }
    }
}

@Composable
private fun ExpeditionsCard(expeditions: List<ExpeditionNormal>) {
    val useUtc = LocalUseUtc.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Expeditions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            expeditions.forEach { exp ->
                val subtitle =
                    exp.end?.let { formatLaunchDateTime(it, useUtc) } ?: exp.spacestation.name
                RowWithImage(
                    title = exp.name ?: "Expedition",
                    subtitle = subtitle,
                    imageUrl = exp.spacestation.image?.imageUrl,
                    fallbackText = exp.name?.firstOrNull()?.uppercase() ?: "",
                    onClick = { /* TODO open expedition */ }
                )
            }
        }
    }
}

@Composable
private fun RelatedLaunchesCard(
    launches: List<LaunchBasic>,
    onLaunchClick: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Related Launches",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            launches.forEach { l ->
                RowWithImage(
                    title = l.name ?: "Launch",
                    subtitle = l.status?.name ?: "",
                    imageUrl = l.image?.imageUrl,
                    fallbackText = l.name?.firstOrNull()?.uppercase() ?: "",
                    onClick = { onLaunchClick?.invoke(l.id) }
                )
            }
        }
    }
}

@Composable
private fun RowWithImage(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    fallbackText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fallbackText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
private fun ProgramsCard(programs: List<ProgramNormal>) {
    val useUtc = LocalUseUtc.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        programs.forEach { program ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO open program */ }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Centered agency logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Program image if available
                        program.image?.imageUrl?.let { imageUrl ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                SubcomposeAsyncImage(
                                    model = imageUrl,
                                    contentDescription = program.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().shimmer(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Public,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.3f
                                                )
                                            )
                                        }
                                    },
                                    error = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Error,
                                                contentDescription = "Mission",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        program.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    program.description?.let { desc ->
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }

                    // Date range if available
                    if (program.startDate != null || program.endDate != null) {
                        val dateRange = buildString {
                            program.startDate?.let { append(formatLaunchDateTime(it, useUtc)) }
                            if (program.startDate != null && program.endDate != null) {
                                append(" → ")
                            }
                            program.endDate?.let { append(formatLaunchDateTime(it, useUtc)) }
                        }
                        Text(
                            dateRange,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Agencies
                    if (program.agencies.isNotEmpty()) {
                        Text(
                            "Agencies: ${program.agencies.joinToString(", ") { it.abbrev ?: it.name }}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdatesCard(updates: List<Update>) {
    val useUtc = LocalUseUtc.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Updates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            updates.take(10).forEach { update ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile image or placeholder
                    if (!update.profileImage.isNullOrBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            AsyncImage(
                                model = update.profileImage,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = update.createdBy?.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        // Created by and date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            update.createdBy?.let { author ->
                                Text(
                                    author,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            update.createdOn?.let { date ->
                                Text(
                                    formatLaunchDateTime(date, useUtc),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Comment
                        update.comment?.let { comment ->
                            Text(
                                comment,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (updates.size > 10) {
                Text(
                    "+ ${updates.size - 10} more updates",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EventDetailErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(36.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Oops! Something went wrong",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onRetry) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
fun EventDetailLoadingView(onNavigateBack: () -> Unit) {
    SharedDetailScaffold(
        titleText = "Loading...",
        taglineText = null,
        imageUrl = null,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        scrollEnabled = false,
    ) {
        // Loading content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
