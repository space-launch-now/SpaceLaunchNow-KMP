package me.calebjones.spacelaunchnow.ui.detail.compose.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.detail.compose.components.AgencyDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.AgencyLaunchStatistics

/**
 * Agency tab content for launch detail view.
 *
 * Displays launch service provider details and statistics.
 * Parent scaffold handles scrolling.
 */
@Composable
fun AgencyTabContent(
    launch: LaunchDetailed,
    openUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Agency Card
        Text(
            text = "Launch Service Provider",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        AgencyDetailsCard(agency = launch.launchServiceProvider, openUrl = openUrl)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Launch Service Provider Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        AgencyLaunchStatistics(agency = launch.launchServiceProvider)
        Spacer(Modifier.height(16.dp))

        // Ad placement
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.CONTENT,
            showRemoveAdsButton = false,
            showCard = true
        )

        // Bottom spacing for better scrolling
        Spacer(Modifier.height(100.dp))
    }
}

