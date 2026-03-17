package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigNormal
import me.calebjones.spacelaunchnow.ui.agencies.compose.AgencyListItem
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautCard
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPage
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketListItem
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Onboarding page — shows live astronaut, rocket, and agency cards inside a device frame.
 */
@Composable
fun ExplorePage(
    modifier: Modifier = Modifier,
    astronauts: List<AstronautEndpointNormal> = emptyList(),
    rockets: List<LauncherConfigNormal> = emptyList(),
    agencies: List<AgencyNormal> = emptyList()
) {
    OnboardingPage(
        title = "Explore Space",
        subtitle = "Dive into the world of spaceflight with detailed profiles and data.",
        icon = Icons.Default.Rocket,
        modifier = modifier
    ) {
        ExplorePreviewContent(
            astronauts = astronauts,
            rockets = rockets,
            agencies = agencies
        )
    }
}

@Composable
private fun ExplorePreviewContent(
    astronauts: List<AstronautEndpointNormal>,
    rockets: List<LauncherConfigNormal>,
    agencies: List<AgencyNormal>
) {
    val isLoading = astronauts.isEmpty() && rockets.isEmpty() && agencies.isEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (astronauts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = Icons.Default.Person,
                            title = "Astronauts"
                        )
                    }
                    items(astronauts) { astronaut ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            AstronautCard(
                                astronaut = astronaut,
                                onClick = {}
                            )
                        }
                    }
                }

                if (rockets.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            icon = Icons.Default.Rocket,
                            title = "Rockets"
                        )
                    }
                    items(rockets) { rocket ->
                        Box(modis d                                fier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            RocketListItem(
                                rocket = rocket,
                                onClick = {}
                            )
                        }
                    }
                }

                if (agencies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            icon = Icons.Default.Business,
                            title = "Agencies"
                        )
                    }
                    items(agencies) { agency ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            AgencyListItem(
                                agency = agency,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun ExplorePagePreview() {
    SpaceLaunchNowPreviewTheme {
        ExplorePage()
    }
}

@Preview
@Composable
private fun ExplorePageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        ExplorePage()
    }
}
