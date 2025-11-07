package me.calebjones.spacelaunchnow.ui.rockets.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold

private val TitleHeight = 128.dp

@Composable
fun RocketDetailView(
    rocket: LauncherConfigDetailed,
    onNavigateBack: () -> Unit
) {
    SharedDetailScaffold(
        titleText = rocket.fullName ?: rocket.name,
        taglineText = rocket.manufacturer?.name,
        imageUrl = rocket.image?.imageUrl,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        ),
    ) {
        RocketDetailContent(rocket)
    }
}

@Composable
private fun RocketDetailContent(rocket: LauncherConfigDetailed) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(TitleHeight - 28.dp))

        // Basic Info Card
        RocketInfoCard(rocket)

        Spacer(Modifier.height(16.dp))

        // Banner Ad
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.CONTENT
        )

        Spacer(Modifier.height(16.dp))

        // Description
        rocket.description?.let { description ->
            DescriptionCard(description)
            Spacer(Modifier.height(16.dp))
        }

        // Performance Stats
        if (rocket.length != null || rocket.diameter != null || rocket.launchMass != null) {
            PerformanceCard(rocket)
            Spacer(Modifier.height(16.dp))
        }

        // Capacity Stats
        if (rocket.leoCapacity != null || rocket.gtoCapacity != null || rocket.geoCapacity != null || rocket.ssoCapacity != null) {
            CapacityCard(rocket)
            Spacer(Modifier.height(16.dp))
        }

        // Launch Statistics
        if (rocket.totalLaunchCount != null) {
            LaunchStatsCard(rocket)
            Spacer(Modifier.height(16.dp))
        }

        // Landing Statistics (if applicable)
        if (rocket.attemptedLandings != null && rocket.attemptedLandings!! > 0) {
            LandingStatsCard(rocket)
            Spacer(Modifier.height(16.dp))
        }

        // Bottom padding
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RocketInfoCard(rocket: LauncherConfigDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Rocket Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Full Name", value = rocket.fullName ?: rocket.name)
            rocket.variant?.let { InfoRow(label = "Variant", value = it) }
            rocket.manufacturer?.name?.let { InfoRow(label = "Manufacturer", value = it) }
            rocket.maidenFlight?.let { InfoRow(label = "Maiden Flight", value = it.toString()) }
            InfoRow(label = "Status", value = if (rocket.active == true) "Active" else "Inactive")
            InfoRow(label = "Reusable", value = if (rocket.reusable == true) "Yes" else "No")

            rocket.minStage?.let { min ->
                rocket.maxStage?.let { max ->
                    InfoRow(label = "Stages", value = if (min == max) "$min" else "$min-$max")
                }
            }
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PerformanceCard(rocket: LauncherConfigDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.length?.let { InfoRow(label = "Length", value = "${"%.1f".format(it)} m") }
            rocket.diameter?.let { InfoRow(label = "Diameter", value = "${"%.1f".format(it)} m") }
            rocket.launchMass?.let {
                InfoRow(
                    label = "Launch Mass",
                    value = "${"%.0f".format(it)} kg"
                )
            }
            rocket.toThrust?.let {
                InfoRow(
                    label = "Liftoff Thrust",
                    value = "${"%.0f".format(it)} kN"
                )
            }
            rocket.launchCost?.let {
                InfoRow(
                    label = "Launch Cost",
                    value = "$${"%.0f".format(it.toDouble())}M"
                )
            }
        }
    }
}

@Composable
private fun CapacityCard(rocket: LauncherConfigDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Payload Capacity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.leoCapacity?.let { InfoRow(label = "LEO", value = "${"%.0f".format(it)} kg") }
            rocket.gtoCapacity?.let { InfoRow(label = "GTO", value = "${"%.0f".format(it)} kg") }
            rocket.geoCapacity?.let { InfoRow(label = "GEO", value = "${"%.0f".format(it)} kg") }
            rocket.ssoCapacity?.let { InfoRow(label = "SSO", value = "${"%.0f".format(it)} kg") }
        }
    }
}

@Composable
private fun LaunchStatsCard(rocket: LauncherConfigDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Launch Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.totalLaunchCount?.let {
                InfoRow(
                    label = "Total Launches",
                    value = it.toString()
                )
            }
            rocket.successfulLaunches?.let { InfoRow(label = "Successful", value = it.toString()) }
            rocket.failedLaunches?.let { InfoRow(label = "Failed", value = it.toString()) }
            rocket.pendingLaunches?.let { InfoRow(label = "Pending", value = it.toString()) }
            rocket.consecutiveSuccessfulLaunches?.let {
                InfoRow(
                    label = "Consecutive Successes",
                    value = it.toString()
                )
            }
        }
    }
}

@Composable
private fun LandingStatsCard(rocket: LauncherConfigDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Landing Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.attemptedLandings?.let { InfoRow(label = "Attempted", value = it.toString()) }
            rocket.successfulLandings?.let { InfoRow(label = "Successful", value = it.toString()) }
            rocket.failedLandings?.let { InfoRow(label = "Failed", value = it.toString()) }
            rocket.consecutiveSuccessfulLandings?.let {
                InfoRow(
                    label = "Consecutive Successes",
                    value = it.toString()
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
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
