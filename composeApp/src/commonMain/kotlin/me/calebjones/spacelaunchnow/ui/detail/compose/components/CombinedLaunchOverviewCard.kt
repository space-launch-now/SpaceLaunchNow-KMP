package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.compose.LaunchCountdown
import me.calebjones.spacelaunchnow.ui.compose.LaunchWindowIndicator
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchTime

/**
 * A combined overview card that displays key launch information.
 *
 * This card is displayed above the tabs on phone layouts and at the top
 * of the left column on tablet layouts. It includes:
 * - Launch countdown timer
 * - Launch date and time
 * - Launch window indicator
 * - Weather probability (if available)
 * - Mission and location quick info tiles
 * - Precision info dialog trigger
 *
 * @param launch The detailed launch information to display
 */
@Composable
fun CombinedLaunchOverviewCard(launch: LaunchDetailed) {
    var showPrecisionDialog by remember { mutableStateOf(false) }
    val useUtc = LocalUseUtc.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Countdown and status (if NET known)
                launch.net?.let { launchTime ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LaunchCountdown(
                            launchTime = launchTime,
                            status = launch.status,
                            precision = launch.netPrecision
                        )
                    }
                }

                // Major launch info summary (like LaunchInfoCardHeroContent)
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // Date/time surface (matches old hero band)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Box {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        launch.net?.let { net ->
                                            val relativeDateText =
                                                DateTimeUtil.formatLaunchDateTimeRelative(
                                                    net,
                                                    useUtc
                                                )
                                            // Extract just the date part (everything before the time)
                                            // e.g., "Today at 12:31pm" -> "Today at"
                                            val dateOnlyText =
                                                if (relativeDateText.contains(" at ")) {
                                                    relativeDateText.substringBefore(" at ") + " at"
                                                } else {
                                                    // Fallback for other formats
                                                    relativeDateText.substringBeforeLast(" ")
                                                }
                                            Text(
                                                text = dateOnlyText,
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        Text(
                                            text = launch.net?.let { formatLaunchTime(it, useUtc) }
                                                ?: "TBD",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        launch.probability?.let { prob ->
                                            if (prob > 0) {
                                                InfoTile(
                                                    icon = Icons.Filled.WbCloudy,
                                                    label = "Weather",
                                                    value = "$prob%",
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }

                                }
                                val windowAllowed = launch.netPrecision?.id in 0..4

                                // Window indicator (if window exists) - now inside the same surface
                                if (launch.windowStart != null && launch.windowEnd != null && windowAllowed) {
                                    LaunchWindowIndicator(
                                        launchTime = launch.net ?: launch.windowStart,
                                        windowStart = launch.windowStart,
                                        windowEnd = launch.windowEnd,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )
                                } else {
                                    // Add some bottom padding when there's no window indicator
                                    Spacer(modifier = Modifier.height(0.dp))
                                }
                            }

                            // Info icon in top-right corner of the surface
                            launch.netPrecision?.let {
                                IconButton(
                                    onClick = { showPrecisionDialog = true },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "Launch time precision info",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Metrics grid below
                val tiles = buildList {
                    launch.mission?.name?.takeIf { it.isNotBlank() }
                        ?.let { add(Triple(Icons.Filled.Category, "Mission", it)) }
                    launch.pad?.let { pad ->
                        val site = pad.location?.name ?: pad.name ?: "Unknown"
                        add(
                            Triple(
                                Icons.Filled.LocationOn,
                                "Launch Site",
                                site
                            )
                        )
                    }

                    // Landing information
                    val landingStages = launch.rocket?.launcherStage ?: emptyList()
                    val landingsWithAttempt = landingStages.filter { it.landing != null }
                    if (landingsWithAttempt.isNotEmpty()) {
                        val successfulLandings =
                            landingsWithAttempt.count { it.landing?.success == true }
                        val totalAttempts = landingsWithAttempt.size

                        val landingText = buildString {
                            if (successfulLandings > 0) {
                                append("$successfulLandings/$totalAttempts Success")
                            } else if (totalAttempts == 1) {
                                append("Attempt")
                            } else {
                                append("$totalAttempts Attempt${if (totalAttempts != 1) "s" else ""}")
                            }
                            for (landing in landingsWithAttempt) {
                                if (landing.landing?.landingLocation?.abbrev != null) {
                                    append("\n${landing.landing.landingLocation.abbrev}")
                                }
                            }
                        }

                        add(
                            Triple(
                                Icons.Filled.FlightLand,
                                "Landing",
                                landingText
                            )
                        )
                    }

                    launch.probability?.let { prob ->
                        add(
                            Triple(
                                Icons.Filled.WbCloudy,
                                "Weather",
                                "$prob%"
                            )
                        )
                    }
                }
                if (tiles.isNotEmpty()) {
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
    }

    // Precision dialog
    if (showPrecisionDialog) {
        PrecisionInfoDialog(
            netPrecision = launch.netPrecision,
            onDismiss = { showPrecisionDialog = false }
        )
    }
}
