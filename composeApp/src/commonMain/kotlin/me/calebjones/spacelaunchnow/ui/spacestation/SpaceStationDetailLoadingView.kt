package me.calebjones.spacelaunchnow.ui.spacestation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.compose.PlainShimmerCard
import me.calebjones.spacelaunchnow.ui.compose.LocalDetailScaffoldCollapsed
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold

private val TitleHeight = 128.dp

/**
 * Loading view for space station details with shimmer placeholders.
 * Uses SharedDetailScaffold pattern with collapsed header to match other detail screens.
 */
@Composable
fun SpaceStationDetailLoadingView(onNavigateBack: () -> Unit) {
    SharedDetailScaffold(
        titleText = "Loading...",
        taglineText = null,
        imageUrl = null,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        scrollEnabled = false
    ) {
        // Shimmer content matching SpaceStationDetailView layout
        val isCollapsed = LocalDetailScaffoldCollapsed.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isCollapsed) {
                Spacer(Modifier.height(TitleHeight))
            }
            // NASA live stream placeholder
            PlainShimmerCard(height = 200)

            // Active expedition card
            PlainShimmerCard(height = 180)

            // ISS Map with position info
            PlainShimmerCard(height = 400)

            // Station specs card
            PlainShimmerCard(height = 160)
        }
    }
}

/**
 * Error view for space station details
 */
@Composable
fun SpaceStationDetailErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            // Top bar with back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }

            // Error message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Failed to load station details",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
