package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Update
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.ui.home.components.UpdateCard

/**
 * Displays a vertical list of launch updates with expandable show more/less functionality
 * Shows the last 5 updates initially with option to expand to all updates
 *
 * Uses the existing UpdateCard component by converting Update to UpdateEndpoint
 */
@Composable
fun LaunchUpdatesSection(
    updates: List<Update>,
    modifier: Modifier = Modifier
) {
    var showAll by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    // Sort by newest first and convert to UpdateEndpoint
    val sortedUpdates = remember(updates) {
        updates.sortedByDescending { it.createdOn }
            .map { it.toUpdateEndpoint() }
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

/**
 * Convert Update to UpdateEndpoint for use with existing UpdateCard component
 * Update doesn't have launch/event/program references, so those are null
 */
private fun Update.toUpdateEndpoint(): UpdateEndpoint {
    return UpdateEndpoint(
        id = this.id,
        profileImage = this.profileImage,
        comment = this.comment,
        infoUrl = this.infoUrl,
        createdBy = this.createdBy,
        launch = null,
        event = null,
        program = null,
        createdOn = this.createdOn
    )
}
