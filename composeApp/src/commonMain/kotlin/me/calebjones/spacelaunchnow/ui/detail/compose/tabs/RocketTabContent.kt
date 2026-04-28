package me.calebjones.spacelaunchnow.ui.detail.compose.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchVehicleDetailedStatistics
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchVehicleDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.SpacecraftDetailsCard

/**
 * Rocket tab content for launch detail view.
 *
 * Displays launch vehicle and spacecraft details.
 * Parent scaffold handles scrolling.
 */
@Composable
fun RocketTabContent(
    launch: Launch,
    openUrl: (String) -> Unit,
    onAstronautClick: ((Int) -> Unit)? = null
) {
    val hasRocket = launch.rocket != null
    val hasSpacecraft = !launch.rocketDetail?.spacecraftFlights.isNullOrEmpty()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!hasRocket && !hasSpacecraft) {
            // Empty state
            Spacer(Modifier.height(32.dp))
            Text(
                text = "No rocket or spacecraft details available for this launch.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(80.dp))
            return@Column
        }

        // Launch Vehicle Details Card
        launch.rocket?.let { rocketConfig ->
            Text(
                text = "Launch Vehicle Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            LaunchVehicleDetailsCard(rocketConfig = rocketConfig, openUrl = openUrl)
            Spacer(Modifier.height(16.dp))
            LaunchVehicleDetailedStatistics(rocketConfig = rocketConfig)
            Spacer(Modifier.height(16.dp))
        }

        // Ad placement between rocket and spacecraft
        if (hasRocket && hasSpacecraft) {
            SmartBannerAd(
                modifier = Modifier.fillMaxWidth(),
                placementType = AdPlacementType.FEED,
                showRemoveAdsButton = false,
                showCard = true
            )
            Spacer(Modifier.height(16.dp))
        }

        // Spacecraft Details Card
        if (hasSpacecraft) {
            launch.rocketDetail?.spacecraftFlights?.let { spacecraftStages ->
                Text(
                    text = "Spacecraft Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                SpacecraftDetailsCard(
                    spacecraftStages = spacecraftStages
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Bottom spacing for navigation bar
        Spacer(Modifier.height(100.dp))
    }
}

