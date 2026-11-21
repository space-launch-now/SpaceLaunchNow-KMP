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
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.ads.rememberScreenVisitKey
import me.calebjones.spacelaunchnow.ui.compose.LocalDetailScaffoldCollapsed
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.util.NumberFormatUtil
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_capacity_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_description_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_info_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_attempted
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_consecutive_successes
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_diameter
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_failed
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_format
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_full_name
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_geo
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_gto
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_launch_cost
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_launch_mass
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_length
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_leo
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_liftoff_thrust
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_maiden_flight
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_manufacturer
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_pending
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_reusable
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_sso
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_stages
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_status
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_successful
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_total_launches
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_label_variant
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_landing_stats_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_launch_stats_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_performance_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_stages_range
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_value_cost_millions
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_value_kilograms
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_value_kilonewtons
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_value_meters
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_value_no
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_detail_value_yes
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_status_active
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_status_inactive

private val TitleHeight = 110.dp

@Composable
fun RocketDetailView(
    rocket: VehicleConfig,
    onNavigateBack: () -> Unit
) {
    SharedDetailScaffold(
        titleText = rocket.fullName ?: rocket.name,
        taglineText = rocket.manufacturerName,
        imageUrl = rocket.imageUrl,
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
private fun RocketDetailContent(rocket: VehicleConfig) {
    val isCollapsed = LocalDetailScaffoldCollapsed.current
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (!isCollapsed) {
            Spacer(Modifier.height(TitleHeight))
        }

        // Basic Info Card
        RocketInfoCard(rocket)

        Spacer(Modifier.height(16.dp))

        // Banner Ad
        val visitKey = rememberScreenVisitKey()
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.CONTENT,
            refreshKey = rocket.id to visitKey
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
        if (rocket.attemptedLandings != null && rocket.attemptedLandings > 0) {
            LandingStatsCard(rocket)
            Spacer(Modifier.height(16.dp))
        }

        // Bottom padding
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RocketInfoCard(rocket: VehicleConfig) {
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
                text = stringResource(Res.string.rockets_detail_info_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = stringResource(Res.string.rockets_detail_label_full_name), value = rocket.fullName ?: rocket.name)
            rocket.variant?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_variant), value = it) }
            rocket.manufacturerName?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_manufacturer), value = it) }
            rocket.maidenFlight?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_maiden_flight), value = it.toString()) }
            InfoRow(label = stringResource(Res.string.rockets_detail_label_status), value = if (rocket.active == true) stringResource(Res.string.rockets_status_active) else stringResource(Res.string.rockets_status_inactive))
            InfoRow(label = stringResource(Res.string.rockets_detail_label_reusable), value = if (rocket.reusable == true) stringResource(Res.string.rockets_detail_value_yes) else stringResource(Res.string.rockets_detail_value_no))

            rocket.minStage?.let { min ->
                rocket.maxStage?.let { max ->
                    InfoRow(label = stringResource(Res.string.rockets_detail_label_stages), value = if (min == max) min.toString() else stringResource(Res.string.rockets_detail_stages_range, min, max))
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
                text = stringResource(Res.string.rockets_detail_description_title),
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
private fun PerformanceCard(rocket: VehicleConfig) {
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
                text = stringResource(Res.string.rockets_detail_performance_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.length?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_length), value = stringResource(Res.string.rockets_detail_value_meters, NumberFormatUtil.formatDecimal(it, 1))) }
            rocket.diameter?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_diameter), value = stringResource(Res.string.rockets_detail_value_meters, NumberFormatUtil.formatDecimal(it, 1))) }
            rocket.launchMass?.let {
                InfoRow(
                    label = stringResource(Res.string.rockets_detail_label_launch_mass),
                    value = stringResource(Res.string.rockets_detail_value_kilograms, NumberFormatUtil.formatDecimal(it, 0))
                )
            }
            rocket.toThrust?.let {
                InfoRow(
                    label = stringResource(Res.string.rockets_detail_label_liftoff_thrust),
                    value = stringResource(Res.string.rockets_detail_value_kilonewtons, NumberFormatUtil.formatDecimal(it, 0))
                )
            }
            rocket.launchCost?.let {
                InfoRow(
                    label = stringResource(Res.string.rockets_detail_label_launch_cost),
                    value = stringResource(Res.string.rockets_detail_value_cost_millions, NumberFormatUtil.formatDecimal(it.toDouble(), 0))
                )
            }
        }
    }
}

@Composable
private fun CapacityCard(rocket: VehicleConfig) {
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
                text = stringResource(Res.string.rockets_detail_capacity_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.leoCapacity?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_leo), value = stringResource(Res.string.rockets_detail_value_kilograms, NumberFormatUtil.formatDecimal(it, 0))) }
            rocket.gtoCapacity?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_gto), value = stringResource(Res.string.rockets_detail_value_kilograms, NumberFormatUtil.formatDecimal(it, 0))) }
            rocket.geoCapacity?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_geo), value = stringResource(Res.string.rockets_detail_value_kilograms, NumberFormatUtil.formatDecimal(it, 0))) }
            rocket.ssoCapacity?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_sso), value = stringResource(Res.string.rockets_detail_value_kilograms, NumberFormatUtil.formatDecimal(it, 0))) }
        }
    }
}

@Composable
private fun LaunchStatsCard(rocket: VehicleConfig) {
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
                text = stringResource(Res.string.rockets_detail_launch_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.totalLaunchCount?.let {
                InfoRow(
                    label = stringResource(Res.string.rockets_detail_label_total_launches),
                    value = it.toString()
                )
            }
            rocket.successfulLaunches?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_successful), value = it.toString()) }
            rocket.failedLaunches?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_failed), value = it.toString()) }
            rocket.pendingLaunches?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_pending), value = it.toString()) }
            rocket.consecutiveSuccessfulLaunches?.let {
                InfoRow(
                    label = stringResource(Res.string.rockets_detail_label_consecutive_successes),
                    value = it.toString()
                )
            }
        }
    }
}

@Composable
private fun LandingStatsCard(rocket: VehicleConfig) {
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
                text = stringResource(Res.string.rockets_detail_landing_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            rocket.attemptedLandings?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_attempted), value = it.toString()) }
            rocket.successfulLandings?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_successful), value = it.toString()) }
            rocket.failedLandings?.let { InfoRow(label = stringResource(Res.string.rockets_detail_label_failed), value = it.toString()) }
            rocket.consecutiveSuccessfulLandings?.let {
                InfoRow(
                    label = stringResource(Res.string.rockets_detail_label_consecutive_successes),
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
            text = stringResource(Res.string.rockets_detail_label_format, label),
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
