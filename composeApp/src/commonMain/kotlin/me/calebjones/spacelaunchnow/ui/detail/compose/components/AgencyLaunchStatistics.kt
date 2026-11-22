package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.ProviderDetail
import me.calebjones.spacelaunchnow.ui.components.StatCard
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_failed_launches
import spacelaunchnow_kmp.composeapp.generated.resources.common_success_rate
import spacelaunchnow_kmp.composeapp.generated.resources.common_successful_launches
import spacelaunchnow_kmp.composeapp.generated.resources.common_total_launches

@Composable
fun AgencyLaunchStatistics(providerDetail: ProviderDetail?) {
    if (providerDetail == null) return
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Launch statistics
        val totalLaunches = providerDetail.totalLaunchCount ?: 0
        if (totalLaunches > 0) {

            val successfulLaunches = providerDetail.successfulLaunches ?: 0
            val failedLaunches = providerDetail.failedLaunches ?: 0
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
                    label = stringResource(Res.string.common_success_rate),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = CustomIcons.RocketLaunch,
                    value = "$totalLaunches",
                    label = stringResource(Res.string.common_total_launches),
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
                    label = stringResource(Res.string.common_successful_launches),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Cancel,
                    value = "$failedLaunches",
                    label = stringResource(Res.string.common_failed_launches),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}