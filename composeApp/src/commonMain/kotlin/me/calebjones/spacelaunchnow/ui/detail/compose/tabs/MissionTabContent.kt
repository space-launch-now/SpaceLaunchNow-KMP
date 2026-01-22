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
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LandingDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchLocationCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.MissionDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.PadQuickStatsRow

/**
 * Mission tab content for launch detail view.
 *
 * Displays mission details, launch location, pad statistics, and landing details.
 * Parent scaffold handles scrolling.
 */
@Composable
fun MissionTabContent(
    launch: LaunchDetailed,
    openUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mission Details Card
        launch.mission?.let { mission ->
            Text(
                text = "Mission Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            MissionDetailsCard(
                mission = mission,
                missionPatchUrl = launch.missionPatches.firstOrNull()?.imageUrl
            )
            Spacer(Modifier.height(16.dp))
        }

        // Launch Location
        launch.pad?.let { pad ->
            Text(
                text = "Launch Location",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            LaunchLocationCard(
                location = pad.location,
                pad = pad,
                openUrl = openUrl
            )
            Spacer(Modifier.height(8.dp))
            PadQuickStatsRow(pad)
            Spacer(Modifier.height(16.dp))
        }

        // Landing Details Card
        val landingStages = launch.rocket?.launcherStage ?: emptyList()
        if (landingStages.any { it.landing != null }) {
            Text(
                text = "Landing Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            LandingDetailsCard(launcherStages = landingStages)
            Spacer(Modifier.height(16.dp))
        }

        // Ad placement
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.FEED,
            showRemoveAdsButton = false,
            showCard = true
        )

        // Bottom spacing for better scrolling
        Spacer(Modifier.height(100.dp))
    }
}

