package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.Update
import me.calebjones.spacelaunchnow.ui.home.components.UpdateCard
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Displays a vertical list of launch updates with expandable show more/less functionality
 * Shows the last 5 updates initially with option to expand to all updates
 *
 * Uses the shared UpdateCard component.
 */
@Composable
fun LaunchUpdatesSection(
    updates: List<Update>,
    modifier: Modifier = Modifier
) {
    var showAll by remember { mutableStateOf(false) }

    // Sort by newest first
    val sortedUpdates = remember(updates) {
        updates.sortedByDescending { it.createdOn }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val displayedUpdates = if (showAll) sortedUpdates else sortedUpdates.take(5)

        displayedUpdates.forEach { update ->
            UpdateCard(
                update = update,
                navController = null,
                fillMaxWidth = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Show "Load More" button if there are more than 5 updates
        if (sortedUpdates.size > 5 && !showAll) {
            TextButton(
                onClick = { showAll = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Show More (${sortedUpdates.size - 5} more)",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Show "Show Less" button if currently showing all
        if (showAll && sortedUpdates.size > 5) {
            TextButton(
                onClick = { showAll = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Show Less",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// region Preview Data

/**
 * Sample updates for preview purposes
 */
private val sampleUpdates = listOf(
    Update(
        id = 1,
        profileImage = null,
        comment = "Vehicle is vertical on the pad. Weather looking good for tomorrow's launch attempt.",
        infoUrl = "https://example.com/update1",
        createdBy = "SpaceX",
        createdOn = Clock.System.now() - 30.minutes
    ),
    Update(
        id = 2,
        profileImage = null,
        comment = "Static fire test completed successfully. All systems nominal.",
        infoUrl = null,
        createdBy = "Launch Director",
        createdOn = Clock.System.now() - 2.hours
    ),
    Update(
        id = 3,
        profileImage = null,
        comment = "Weather forecast updated: 80% chance of favorable conditions for launch window.",
        infoUrl = "https://example.com/weather",
        createdBy = "45th Weather Squadron",
        createdOn = Clock.System.now() - 1.days
    ),
    Update(
        id = 4,
        profileImage = null,
        comment = "Payload integration complete. Fairing encapsulation scheduled for tomorrow.",
        infoUrl = null,
        createdBy = "Mission Integration Team",
        createdOn = Clock.System.now() - 2.days
    ),
    Update(
        id = 5,
        profileImage = null,
        comment = "Launch date confirmed. All teams ready for final countdown.",
        infoUrl = "https://example.com/announcement",
        createdBy = "NASA",
        createdOn = Clock.System.now() - 3.days
    ),
    Update(
        id = 6,
        profileImage = null,
        comment = "Range safety approval received. Green light for launch operations.",
        infoUrl = null,
        createdBy = "Range Safety Officer",
        createdOn = Clock.System.now() - 5.days
    ),
    Update(
        id = 7,
        profileImage = null,
        comment = "Crew arrived at launch site for final preparations.",
        infoUrl = null,
        createdBy = "Crew Operations",
        createdOn = Clock.System.now() - 7.days
    )
)

// endregion

// region Previews

@Preview
@Composable
private fun LaunchUpdatesSectionPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LaunchUpdatesSection(
                updates = sampleUpdates.take(3),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LaunchUpdatesSectionWithMorePreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LaunchUpdatesSection(
                updates = sampleUpdates,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LaunchUpdatesSectionEmptyPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LaunchUpdatesSection(
                updates = emptyList(),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LaunchUpdatesSectionSinglePreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LaunchUpdatesSection(
                updates = sampleUpdates.take(1),
                modifier = Modifier.padding(16.dp)
                
            )
        }
    }
}

// endregion
