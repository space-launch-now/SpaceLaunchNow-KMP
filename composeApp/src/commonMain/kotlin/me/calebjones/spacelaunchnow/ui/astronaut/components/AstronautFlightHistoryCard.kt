package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.ui.schedule.components.ScheduleLaunchView
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_flight_history_mission_count_one
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_flight_history_mission_count_other
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_flight_history_title

/**
 * Card displaying astronaut flight history using the schedule list component.
 *
 * Shows a vertical scrollable list of all missions using the existing ScheduleLaunchCard component.
 */
@Composable
fun AstronautFlightHistoryCard(
    flights: List<Launch>,
    onLaunchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (flights.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.astronaut_flight_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (flights.size == 1) {
                        stringResource(Res.string.astronaut_flight_history_mission_count_one, flights.size)
                    } else {
                        stringResource(Res.string.astronaut_flight_history_mission_count_other, flights.size)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vertical list using the existing ScheduleLaunchCard
            flights.forEach { flight ->
                ScheduleLaunchView(
                    launch = flight,
                    onClick = { onLaunchClick(flight.id) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun AstronautFlightHistoryCardPreview() {
    SpaceLaunchNowPreviewTheme {
        // Preview requires NavController but we can't easily create one in preview
        // Create a simplified version for preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.astronaut_flight_history_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "2 missions", // i18n-ignore: preview sample
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Preview: NavController required for full display", // i18n-ignore: preview-only text
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
