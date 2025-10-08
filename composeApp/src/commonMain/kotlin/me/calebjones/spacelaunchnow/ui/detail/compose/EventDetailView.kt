package me.calebjones.spacelaunchnow.ui.detail.compose

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationNormal
import me.calebjones.spacelaunchnow.ui.EventSharedElementKey
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime
import org.koin.compose.viewmodel.koinViewModel

private val TitleHeight = 128.dp

@Composable
fun EventDetailView(
    event: EventEndpointDetailed,
    onNavigateBack: () -> Unit
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
        EventDetailContentInBody(event)
    }
}

@Composable
private fun EventDetailContentInBody(event: EventEndpointDetailed) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(TitleHeight - 28.dp))

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
            // Description
            event.description?.let { desc ->
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(desc, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
            }
            if (meta.isNotEmpty()) {
                Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                style = MaterialTheme.typography.titleLarge,
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
                    Text(u.title ?: "Info")
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
                if (agencies.size == 1) "Agency" else "Agencies",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            agencies.forEach { agency ->
                AgencyRowWithLogo(agency = agency, onClick = { /* TODO open agency */ })
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
        val logoUrl = detailed?.logo?.imageUrl
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            astronauts.forEach { astro ->
                RowWithImage(
                    title = astro.name ?: "Astronaut",
                    subtitle = astro.nationality.firstOrNull()?.name ?: "",
                    imageUrl = astro.image?.imageUrl,
                    fallbackText = astro.name?.firstOrNull()?.uppercase() ?: "",
                    onClick = { /* TODO open astronaut */ }
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            stations.forEach { ss ->
                RowWithImage(
                    title = ss.name,
                    subtitle = ss.orbit ?: "",
                    imageUrl = ss.image?.imageUrl,
                    fallbackText = ss.name.firstOrNull()?.uppercase() ?: "",
                    onClick = { /* TODO open station */ }
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            launches.forEach { l ->
                RowWithImage(
                    title = l.name ?: "Launch",
                    subtitle = l.status?.name ?: "",
                    imageUrl = l.image?.imageUrl,
                    fallbackText = l.name?.firstOrNull()?.uppercase() ?: "",
                    onClick = { /* TODO open launch */ }
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
