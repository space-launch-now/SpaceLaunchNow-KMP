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
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LandingDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchLocationCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.MissionDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.PadQuickStatsRow
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_landing_details
import spacelaunchnow_kmp.composeapp.generated.resources.common_launch_location
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_mission_details

/**
 * Mission tab content for launch detail view.
 *
 * Displays mission details, launch location, pad statistics, and landing details.
 * Parent scaffold handles scrolling.
 */
@Composable
fun MissionTabContent(
    launch: Launch,
    openUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mission Details Card
        launch.mission?.let { mission ->
            Text(
                text = stringResource(Res.string.launch_detail_section_mission_details),
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
                text = stringResource(Res.string.common_launch_location),
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
        val landingStages = launch.rocketDetail?.stages ?: emptyList()
        if (landingStages.any { it.landingAttempt != null }) {
            Text(
                text = stringResource(Res.string.common_landing_details),
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
            showCard = true,
            refreshKey = launch.id
        )

        // Bottom spacing for better scrolling
        Spacer(Modifier.height(100.dp))
    }
}

