package me.calebjones.spacelaunchnow.ui.agencies.compose

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.brands.WikipediaW
import compose.icons.fontawesomeicons.solid.InfoCircle
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.components.CountryChip
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.InfoTileData
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.ui.detail.compose.components.CountryInfoRow
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch

private val TitleHeight = 128.dp

@Composable
fun AgencyDetailView(
    agency: AgencyEndpointDetailed,
    onNavigateBack: () -> Unit
) {
    SharedDetailScaffold(
        titleText = agency.name,
        taglineText = agency.type?.name,
        imageUrl = agency.logo?.imageUrl,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        AgencyDetailContentInBody(agency)
    }
}

@Composable
private fun AgencyDetailContentInBody(agency: AgencyEndpointDetailed) {
    val uriHandler = LocalUriHandler.current
    val openUrl: (String) -> Unit = { url ->
        try {
            uriHandler.openUri(url)
        } catch (_: Throwable) {
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(TitleHeight - 28.dp))

        // Agency Overview Card
        AgencyOverviewCard(agency, openUrl)

        Spacer(Modifier.height(16.dp))

        // Banner Ad
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.CONTENT
        )

        Spacer(Modifier.height(16.dp))

        // Launch Statistics
        val totalLaunches = agency.totalLaunchCount ?: 0
        if (totalLaunches > 0) {
            Text(
                text = "Launch Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            AgencyLaunchStatistics(agency)
            Spacer(Modifier.height(16.dp))
        }

        // Landing Statistics
        val attemptedLandings = agency.attemptedLandings ?: 0
        val successfulLandings = agency.successfulLandings ?: 0
        val failedLandings = agency.failedLandings ?: 0
        val consecutiveSuccessfulLandings = agency.consecutiveSuccessfulLandings ?: 0
        if (attemptedLandings + successfulLandings + failedLandings + consecutiveSuccessfulLandings > 0) {
            Text(
                text = "Landing Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            AgencyLandingStatistics(agency)
            Spacer(Modifier.height(16.dp))
        }

        // Spacecraft Landing Statistics
        val attemptedLandingsSpacecraft = agency.attemptedLandingsSpacecraft ?: 0
        val successfulLandingsSpacecraft = agency.successfulLandingsSpacecraft ?: 0
        val failedLandingsSpacecraft = agency.failedLandingsSpacecraft ?: 0
        if (attemptedLandingsSpacecraft + successfulLandingsSpacecraft + failedLandingsSpacecraft > 0) {
            Text(
                text = "Spacecraft Landing Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            AgencySpacecraftLandingStatistics(agency)
            Spacer(Modifier.height(16.dp))
        }

        // Launchers and Spacecraft Info
        if (!agency.launchers.isNullOrBlank() || !agency.spacecraft.isNullOrBlank()) {
            Text(
                text = "Vehicles & Spacecraft",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            VehiclesCard(agency)
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(200.dp))
    }
}

@Composable
private fun AgencyOverviewCard(
    agency: AgencyEndpointDetailed,
    openUrl: (String) -> Unit
) {
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
            // Agency logo - centered
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SubcomposeAsyncImage(
                    model = agency.logo?.imageUrl ?: "",
                    contentDescription = "Agency logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Agency",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                )
            }

            // Grid of key details
            val infoTiles = buildList {
                agency.type?.name?.takeIf { it.isNotBlank() }?.let {
                    add(InfoTileData(Icons.Filled.Business, "Type", it))
                }
                agency.abbrev?.takeIf { it.isNotBlank() }?.let {
                    add(InfoTileData(Icons.Filled.Business, "Abbreviation", it))
                }
                agency.foundingYear?.let { year ->
                    add(InfoTileData(Icons.Filled.CalendarToday, "Founded", year.toString()))
                }
                agency.administrator?.takeIf { it.isNotBlank() }?.let { admin ->
                    add(InfoTileData(Icons.Filled.Person, "Administrator", admin))
                }
                if (agency.country.isNotEmpty() && agency.country.size == 1) {
                    add(
                        InfoTileData(
                            icon = Icons.Filled.Flag,
                            label = "Country",
                            value = null,
                            customComposable = { CountryChip(agency.country.first()) }
                        )
                    )
                }
            }
            if (infoTiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoTiles.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { tile ->
                                InfoTile(
                                    icon = tile.icon,
                                    label = tile.label,
                                    value = tile.value,
                                    customComposable = tile.customComposable,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Countries as chips (if multiple countries)
            if (agency.country.isNotEmpty() && agency.country.size > 1) {
                CountryInfoRow(countries = agency.country)
            }

            // Description
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

            // Info & Wiki links
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                agency.infoUrl?.let { url ->
                    Button(
                        onClick = { openUrl(url) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.InfoCircle,
                            contentDescription = "Information",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Website")
                    }
                }
                agency.wikiUrl?.let { url ->
                    Button(
                        onClick = { openUrl(url) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Brands.WikipediaW,
                            contentDescription = "Wikipedia",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Wikipedia")
                    }
                }
            }
        }
    }
}

@Composable
private fun AgencyLaunchStatistics(agency: AgencyEndpointDetailed) {
    val totalLaunches = agency.totalLaunchCount ?: 0
    val successfulLaunches = agency.successfulLaunches ?: 0
    val failedLaunches = agency.failedLaunches ?: 0
    val pendingLaunches = agency.pendingLaunches ?: 0
    val consecutiveSuccessful = agency.consecutiveSuccessfulLaunches ?: 0
    val successRate = if (totalLaunches > 0) (successfulLaunches * 100.0 / totalLaunches) else 0.0

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                icon = CustomIcons.RocketLaunch,
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
                    icon = Icons.Filled.Public,
                    value = "$pendingLaunches",
                    label = "Pending\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            if (consecutiveSuccessful > 0) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.TrendingUp,
                    value = "$consecutiveSuccessful",
                    label = "Consecutive\nSuccess",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AgencyLandingStatistics(agency: AgencyEndpointDetailed) {
    val attemptedLandings = agency.attemptedLandings ?: 0
    val successfulLandings = agency.successfulLandings ?: 0
    val failedLandings = agency.failedLandings ?: 0
    val consecutiveSuccessfulLandings = agency.consecutiveSuccessfulLandings ?: 0

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (attemptedLandings > 0) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = CustomIcons.RocketLaunch,
                    value = "$attemptedLandings",
                    label = "Attempted\nLandings",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            if (successfulLandings > 0) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
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
                    icon = Icons.Filled.Cancel,
                    value = "$failedLandings",
                    label = "Failed\nLandings",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            if (consecutiveSuccessfulLandings > 0) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.TrendingUp,
                    value = "$consecutiveSuccessfulLandings",
                    label = "Consecutive\nSuccess",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AgencySpacecraftLandingStatistics(agency: AgencyEndpointDetailed) {
    val attemptedLandings = agency.attemptedLandingsSpacecraft ?: 0
    val successfulLandings = agency.successfulLandingsSpacecraft ?: 0
    val failedLandings = agency.failedLandingsSpacecraft ?: 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (attemptedLandings > 0) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Satellite,
                value = "$attemptedLandings",
                label = "Attempted",
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        if (successfulLandings > 0) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CheckCircle,
                value = "$successfulLandings",
                label = "Successful",
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        if (failedLandings > 0) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Cancel,
                value = "$failedLandings",
                label = "Failed",
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun VehiclesCard(agency: AgencyEndpointDetailed) {
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
            agency.launchers?.takeIf { it.isNotBlank() }?.let { launchers ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Launch Vehicles",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = launchers,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }

            agency.spacecraft?.takeIf { it.isNotBlank() }?.let { spacecraft ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Spacecraft",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = spacecraft,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }
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
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AgencyDetailErrorView(
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
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
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
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
fun AgencyDetailLoadingView(onNavigateBack: () -> Unit) {
    SharedDetailScaffold(
        titleText = "Loading...",
        taglineText = null,
        imageUrl = null,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        scrollEnabled = false,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
