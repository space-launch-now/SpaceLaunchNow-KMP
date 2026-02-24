package me.calebjones.spacelaunchnow.ui.explore.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.ui.explore.ExploreSection
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card component for displaying a discoverable section in the Explore screen.
 * Based on StatCard pattern but optimized for navigation sections.
 *
 * @param section The explore section to display
 * @param onClick Callback when the card is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun ExploreCard(
    section: ExploreSection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .semantics {
                contentDescription = section.contentDescription
            }
            .clickable(
                enabled = true,
                role = Role.Button,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Circular icon surface
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null, // Decorative, card has contentDescription
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                // Title
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Description
                Text(
                    text = section.description,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExploreCardPreview() {
    SpaceLaunchNowPreviewTheme {
        ExploreCard(
            section = ExploreSection(
                id = "rockets",
                title = "Rockets",
                description = "Browse launcher configurations and technical specifications",
                icon = Icons.Filled.Rocket,
                route = Rockets,
                contentDescription = "Navigate to Rockets"
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun ExploreCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        ExploreCard(
            section = ExploreSection(
                id = "rockets",
                title = "Rockets",
                description = "Browse launcher configurations and technical specifications",
                icon = Icons.Filled.Rocket,
                route = Rockets,
                contentDescription = "Navigate to Rockets"
            ),
            onClick = {}
        )
    }
}
