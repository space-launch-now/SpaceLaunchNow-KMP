package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.domain.model.RocketStage
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.InfoTileHorizontal
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_details_heading
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_downrange
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_downrange_value
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_flight_number
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_location
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_previous_flight
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_result_failure
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_result_success
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_stage_number
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_tile_attempt
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_tile_landing_type
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_tile_result
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_tile_stage_type
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_turnaround
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_read_less
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_read_more
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_value_na
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_value_no
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_value_tbd
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_value_yes

@Composable
        fun LandingDetailsCard(launcherStages: List<RocketStage>) {
    val stagesWithLanding = launcherStages.filter { it.landingAttempt != null }

    // Guard: if nothing to show, return early
    if (stagesWithLanding.isEmpty()) return

    // Each stage gets its own card
    stagesWithLanding.forEachIndexed { index, stage ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with stage index, serial, and flight number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val header = buildString {
                        append(stringResource(Res.string.launch_detail_landing_stage_number, index + 1))
                        stage.launcher?.serialNumber?.takeIf { it.isNotBlank() }?.let {
                            append(" • ")
                            append(it)
                        }
                    }
                    Text(
                        text = header,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Flight number tag
                    stage.launcherFlightNumber?.let { flightNum ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = stringResource(Res.string.launch_detail_landing_flight_number, flightNum),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                stage.landingAttempt?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    var expanded by remember { mutableStateOf(false) }
                    var hasOverflow by remember { mutableStateOf(false) }
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        onTextLayout = { result ->
                            if (!expanded) hasOverflow = result.hasVisualOverflow
                        }
                    )
                    if (hasOverflow || expanded) {
                        TextButton(onClick = {
                            expanded = !expanded
                        }) { Text(if (expanded) stringResource(Res.string.launch_detail_read_less) else stringResource(Res.string.launch_detail_read_more)) }
                    }
                }

                val useUtc = LocalUseUtc.current
                stage.previousFlightDate?.let {
                    InfoTileHorizontal(
                        icon = Icons.Filled.Schedule,
                        label = stringResource(Res.string.launch_detail_landing_previous_flight),
                        value = DateTimeUtil.formatLaunchDateTime(it, useUtc)
                    )
                }
                stage.turnAroundTime?.takeIf { it.isNotBlank() }?.let {
                    InfoTileHorizontal(
                        icon = Icons.Filled.Timelapse,
                        label = stringResource(Res.string.launch_detail_landing_turnaround),
                        value = parseIsoDurationToHumanReadable(it)
                    )
                }
                if (stage.landingAttempt != null) {
                    LandingStageGridContent(stage)
                }
            }
        }
        if (stage != stagesWithLanding.last()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LandingStageGridContent(stage: RocketStage) {
    val landing = stage.landingAttempt

    val tiles = buildList {
        add(Triple(Icons.Filled.Category, stringResource(Res.string.launch_detail_landing_tile_stage_type), stage.type))
        landing?.type?.let { add(Triple(Icons.Filled.FlightLand, stringResource(Res.string.launch_detail_landing_tile_landing_type), it)) }
        landing?.attempt?.let {
            if (it) {
                add(
                    Triple(
                        Icons.Filled.FlightTakeoff,
                        stringResource(Res.string.launch_detail_landing_tile_attempt),
                        if (it) stringResource(Res.string.launch_detail_value_yes) else stringResource(Res.string.launch_detail_value_no)
                    )
                )

                landing.success?.let { success ->
                    add(
                        Triple(
                            if (success) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                            stringResource(Res.string.launch_detail_landing_tile_result),
                            if (success) stringResource(Res.string.launch_detail_landing_result_success) else stringResource(Res.string.launch_detail_landing_result_failure)
                        )
                    )
                }
                if (landing.success == null) {
                    add(
                        Triple(
                            Icons.Filled.ChangeCircle,
                            stringResource(Res.string.launch_detail_landing_tile_result),
                            stringResource(Res.string.launch_detail_value_tbd)
                        )
                    )
                }
            }
        }
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.launch_detail_landing_details_heading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            InfoTileHorizontal(
                icon = Icons.Filled.LocationOn,
                label = stringResource(Res.string.launch_detail_landing_location),
                value = landing?.landingLocation?.name
            )
            InfoTileHorizontal(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = stringResource(Res.string.launch_detail_landing_downrange),
                value = stringResource(Res.string.launch_detail_landing_downrange_value, landing?.downrangeDistance ?: stringResource(Res.string.launch_detail_value_na))
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tiles.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (icon, label, value) ->
                            InfoTile(
                                icon = icon,
                                label = label,
                                value = value,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}