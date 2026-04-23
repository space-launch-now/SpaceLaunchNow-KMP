package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.ui.components.StatCard

@Composable
fun QuickStatsGrid(launch: Launch) {
    // Build a dynamic list of facts that are available
    data class Fact(val icon: ImageVector, val value: String, val label: String)

    val currentYear = launch.net?.toLocalDateTime(TimeZone.currentSystemDefault())?.year

    val facts = buildList {
        launch.launchAttemptCounts?.orbital?.let { count ->
            add(Fact(Icons.Filled.Public, "#${count}", "Launch\nAll Time"))
        }
        launch.launchAttemptCounts?.pad?.let { count ->
            add(Fact(Icons.Filled.Place, "#${count}", "Location\nAll Time"))
        }
        launch.launchAttemptCounts?.orbitalYear?.let { count ->
            add(Fact(Icons.Filled.CalendarToday, "#${count}", "Total\n$currentYear"))
        }
        launch.launchAttemptCounts?.agencyYear?.let { count ->
            add(
                Fact(
                    Icons.Filled.Business,
                    "#${count}",
                    "${launch.provider.abbrev}\n$currentYear"
                )
            )
        }
    }

    if (facts.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Facts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Render in rows of two cards per row
        facts.chunked(2).forEach { rowFacts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFacts.forEach { fact ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = fact.icon,
                        value = fact.value,
                        label = fact.label
                    )
                }
                if (rowFacts.size == 1) {
                    // Balance layout if odd count
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
