package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.util.LatLng
import me.calebjones.spacelaunchnow.util.NumberFormatUtil
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.common_location
import spacelaunchnow_kmp.composeapp.generated.resources.common_value_kilometers
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_altitude
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_degrees
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_latitude
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_loading
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_longitude
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_speed_value
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_title
import spacelaunchnow_kmp.composeapp.generated.resources.station_position_velocity

/**
 * Card displaying current ISS position and velocity
 */
@Composable
fun PositionCard(
    position: LatLng?,
    altitude: Double?,
    velocity: Double?,
    orbitInfo: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.station_position_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (position != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(Res.string.common_location),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = stringResource(Res.string.station_position_latitude),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(Res.string.station_position_degrees, NumberFormatUtil.formatDecimal(position.latitude, 4)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(Res.string.common_location),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = stringResource(Res.string.station_position_longitude),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(Res.string.station_position_degrees, NumberFormatUtil.formatDecimal(position.longitude, 4)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                // Orbit info
                if (orbitInfo != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column {
                            Text(
                                text = stringResource(Res.string.station_position_orbit),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = orbitInfo,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                if (altitude != null || velocity != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        altitude?.let {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.station_position_altitude),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(Res.string.common_value_kilometers, NumberFormatUtil.formatDecimal(it, 1)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        velocity?.let {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = stringResource(Res.string.station_position_velocity),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(
                                            text = stringResource(Res.string.station_position_velocity),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(Res.string.station_position_speed_value, NumberFormatUtil.formatDecimal(it, 0)),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(Res.string.station_position_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
