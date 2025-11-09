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
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyDetailed
import me.calebjones.spacelaunchnow.ui.components.StatCard
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch

@Composable
fun AgencyLaunchStatistics(agency: AgencyDetailed) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Launch statistics
        val totalLaunches = agency.totalLaunchCount ?: 0
        if (totalLaunches > 0) {

            val successfulLaunches = agency.successfulLaunches ?: 0
            val failedLaunches = agency.failedLaunches ?: 0
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
        }
    }
}