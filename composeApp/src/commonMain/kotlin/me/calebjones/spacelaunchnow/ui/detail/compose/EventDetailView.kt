package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.ui.SharedElementType
import me.calebjones.spacelaunchnow.ui.EventSharedElementKey
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalNavAnimatedVisibilityScope
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EventDetailView(
    event: EventEndpointDetailed,
    onNavigateBack: () -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No Scope found")

    val roundedCornerAnim by animatedVisibilityScope.transition
        .animateDp(label = "rounded corner") { enterExit ->
            when (enterExit) {
                androidx.compose.animation.EnterExitState.PreEnter -> 20.dp
                androidx.compose.animation.EnterExitState.Visible -> 0.dp
                androidx.compose.animation.EnterExitState.PostExit -> 20.dp
            }
        }

    with(sharedTransitionScope) {
        Box(
            Modifier
                .clip(RoundedCornerShape(roundedCornerAnim))
                .sharedBounds(
                    rememberSharedContentState(
                        key = EventSharedElementKey(
                            eventId = event.id,
                            type = SharedElementType.Bounds,
                        ),
                    ),
                    animatedVisibilityScope,
                    clipInOverlayDuringTransition =
                        OverlayClip(RoundedCornerShape(roundedCornerAnim)),
                    boundsTransform = snackDetailBoundsTransform,
                )
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            val scroll = rememberScrollState(0)
            EventHeader(event.id, event)
            EventBody(event, scroll)
            EventTitle(event) { scroll.value }
            EventImage(event) { scroll.value }
            EventUp(onNavigateBack)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EventHeader(eventId: Int, event: EventEndpointDetailed) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalArgumentException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")

    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .sharedBounds(
                    rememberSharedContentState(
                        key = EventSharedElementKey(
                            eventId = eventId,
                            type = SharedElementType.Background,
                        ),
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = snackDetailBoundsTransform,
                )
                .height(280.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {}
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.EventUp(upPress: () -> Unit) {
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")
    with(animatedVisibilityScope) {
        IconButton(
            onClick = upPress,
            modifier = Modifier
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 3f)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .size(36.dp)
                .background(
                    color = Color(0xff121212).copy(alpha = 0.32f),
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EventBody(
    event: EventEndpointDetailed,
    scroll: androidx.compose.foundation.ScrollState
) {
    val sharedTransitionScope =
        LocalSharedTransitionScope.current ?: throw IllegalStateException("No scope found")
    with(sharedTransitionScope) {
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )

            Column(
                modifier = Modifier.verticalScroll(scroll),
            ) {
                Spacer(Modifier.height(180.dp))
                Spacer(Modifier.height(115.dp))
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    EventDetailContentInBody(event)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EventTitle(event: EventEndpointDetailed, scrollProvider: () -> Int) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalArgumentException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")

    with(sharedTransitionScope) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .sharedBounds(
                        rememberSharedContentState(
                            key = EventSharedElementKey(
                                eventId = event.id,
                                type = SharedElementType.Title,
                            ),
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = snackDetailBoundsTransform,
                    ),
            )
            event.type.name?.let { typeName ->
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .sharedBounds(
                            rememberSharedContentState(
                                key = EventSharedElementKey(
                                    eventId = event.id,
                                    type = SharedElementType.Tagline,
                                ),
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = snackDetailBoundsTransform,
                        ),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EventImage(
    event: EventEndpointDetailed,
    scrollProvider: () -> Int,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No sharedTransitionScope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No animatedVisibilityScope found")

    with(sharedTransitionScope) {
        AsyncImage(
            model = event.image?.imageUrl ?: "",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(CircleShape)
                .sharedBounds(
                    rememberSharedContentState(
                        key = EventSharedElementKey(
                            eventId = event.id,
                            type = SharedElementType.Image,
                        ),
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = snackDetailBoundsTransform,
                )
                .size(300.dp),
        )
    }
}

@Composable
private fun EventDetailContentInBody(event: EventEndpointDetailed) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(128.dp))

        // Meta card
        EventInfoCard(event)

        Spacer(Modifier.height(16.dp))

        // Links card
        if (event.vidUrls.isNotEmpty() || event.infoUrls.isNotEmpty()) {
            EventLinksCard(event)
            Spacer(Modifier.height(16.dp))
        }

        // Agencies
        if (event.agencies.isNotEmpty()) {
            AgenciesCard(event.agencies)
            Spacer(Modifier.height(16.dp))
        }

        // Astronauts
        event.astronauts?.takeIf { it.isNotEmpty() }?.let { list ->
            AstronautsCard(list)
            Spacer(Modifier.height(16.dp))
        }

        // Space Stations
        if (event.spacestations.isNotEmpty()) {
            SpaceStationsCard(event.spacestations)
            Spacer(Modifier.height(16.dp))
        }

        // Expeditions
        if (event.expeditions.isNotEmpty()) {
            ExpeditionsCard(event.expeditions)
            Spacer(Modifier.height(16.dp))
        }

        // Related Launches
        if (event.launches.isNotEmpty()) {
            RelatedLaunchesCard(event.launches)
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(200.dp))
    }
}

@Composable
private fun EventInfoCard(event: EventEndpointDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date, location, duration
            val meta = buildList {
                event.date?.let { add(formatLaunchDateTime(it)) }
                event.location?.let { add(it) }
                if (event.duration != null) add("Duration: ${event.duration}")
                if (event.webcastLive == true) add("Live Now")
            }.joinToString(" • ")
            if (meta.isNotEmpty()) {
                Text(meta, style = MaterialTheme.typography.bodyMedium)
            }

            // Description
            event.description?.let { desc ->
                Text(desc, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun EventLinksCard(event: EventEndpointDetailed) {
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            event.vidUrls.forEach { v ->
                Button(onClick = { /* open video */ }) {
                    Icon(Icons.Filled.Link, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(v.title ?: "Video")
                }
            }
            event.infoUrls.forEach { u ->
                Button(onClick = { /* open info */ }) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(u.name ?: "Info")
                }
            }
        }
    }
}

@Composable
private fun AgenciesCard(agencies: List<AgencyMini>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Agencies",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            agencies.forEach { agency ->
                RowWithAction(
                    title = agency.name ?: "Agency",
                    subtitle = agency.abbrev ?: "",
                    onDetails = { /* TODO: open Agency detail */ }
                )
            }
        }
    }
}

@Composable
private fun AstronautsCard(astronauts: List<AstronautNormal>) {
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            astronauts.forEach { astro ->
                RowWithAction(
                    title = astro.name ?: "Astronaut",
                    subtitle = astro.nationality ?: "",
                    onDetails = { /* TODO: open Astronaut detail */ }
                )
            }
        }
    }
}

@Composable
private fun SpaceStationsCard(stations: List<SpaceStationNormal>) {
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            stations.forEach { ss ->
                RowWithAction(
                    title = ss.name ?: "Space Station",
                    subtitle = ss.orbit ?: "",
                    onDetails = { /* TODO: open Space Station detail */ }
                )
            }
        }
    }
}

@Composable
private fun ExpeditionsCard(expeditions: List<ExpeditionNormal>) {
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
                val subtitle = exp.end?.let { formatLaunchDateTime(it) } ?: exp.spacestation.name
                RowWithAction(
                    title = exp.name ?: "Expedition",
                    subtitle = subtitle,
                    onDetails = { /* TODO: open Expedition detail */ }
                )
            }
        }
    }
}

@Composable
private fun RelatedLaunchesCard(launches: List<LaunchBasic>) {
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            launches.forEach { l ->
                val subtitle: String? = l.status?.name ?: ""
                RowWithAction(
                    title = l.name ?: "Launch",
                    subtitle = subtitle,
                    onDetails = { /* TODO: open Launch detail */ }
                )
            }
        }
    }
}

@Composable
private fun RowWithAction(title: String, subtitle: String?, onDetails: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Button(onClick = onDetails) {
            Text("DETAILS")
        }
    }
}

@Composable
private fun RowItem(title: String, subtitle: String?) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
