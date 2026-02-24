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
import me.calebjones.spacelaunchnow.api.launchlibrary.models.FirstStageNormal
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.InfoTileHorizontal
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable

@Composable
        fun LandingDetailsCard(launcherStages: List<FirstStageNormal>) {
    val stagesWithLanding = launcherStages.filter { it.landing != null }

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
                        append("Stage #${index + 1}")
                        stage.launcher.serialNumber?.takeIf { it.isNotBlank() }?.let {
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
                                text = "Flight #$flightNum",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                stage.landing?.description?.takeIf { it.isNotBlank() }?.let { desc ->
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
                        }) { Text(if (expanded) "Read less" else "Read more") }
                    }
                }

                val useUtc = LocalUseUtc.current
                stage.previousFlightDate?.let {
                    InfoTileHorizontal(
                        icon = Icons.Filled.Schedule,
                        label = "Previous Flight",
                        value = DateTimeUtil.formatLaunchDateTime(it, useUtc)
                    )
                }
                stage.turnAroundTime?.takeIf { it.isNotBlank() }?.let {
                    InfoTileHorizontal(
                        icon = Icons.Filled.Timelapse,
                        label = "Turnaround",
                        value = parseIsoDurationToHumanReadable(it)
                    )
                }
                if (stage.landing != null) {
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
fun LandingStageGridContent(stage: FirstStageNormal) {
    val landing = stage.landing

    val tiles = buildList {
        add(Triple(Icons.Filled.Category, "Stage Type", stage.type))
        landing?.type?.name?.let { add(Triple(Icons.Filled.FlightLand, "Landing Type", it)) }
        landing?.attempt?.let {
            if (it) {
                add(
                    Triple(
                        Icons.Filled.FlightTakeoff,
                        "Attempt",
                        if (it) "Yes" else "No"
                    )
                )

                landing.success?.let { success ->
                    add(
                        Triple(
                            if (success) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                            "Result",
                            if (success) "Success" else "Failure"
                        )
                    )
                }
                if (landing.success == null) {
                    add(
                        Triple(
                            Icons.Filled.ChangeCircle,
                            "Result",
                            "TBD"
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
                text = "Landing Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            InfoTileHorizontal(
                icon = Icons.Filled.LocationOn,
                label = "Location",
                value = landing?.landingLocation?.name
            )
            InfoTileHorizontal(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "Downrange",
                value = "${landing?.downrangeDistance ?: "N/A"} km"
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