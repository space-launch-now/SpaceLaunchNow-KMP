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
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.common_error
import spacelaunchnow_kmp.composeapp.generated.resources.common_loading
import spacelaunchnow_kmp.composeapp.generated.resources.common_retry
import spacelaunchnow_kmp.composeapp.generated.resources.station_detail_error_title
import spacelaunchnow_kmp.composeapp.generated.resources.station_detail_navigate_back

private val TitleHeight = 110.dp

/**
 * Loading view for space station details with shimmer placeholders.
 * Uses SharedDetailScaffold pattern with collapsed header to match other detail screens.
 */
@Composable
fun SpaceStationDetailLoadingView(onNavigateBack: () -> Unit) {
    SharedDetailScaffold(
        titleText = stringResource(Res.string.common_loading),
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
                    contentDescription = stringResource(Res.string.station_detail_navigate_back)
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
                        contentDescription = stringResource(Res.string.common_error),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(Res.string.station_detail_error_title),
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
                        Text(stringResource(Res.string.common_retry))
                    }
                }
            }
        }
    }
}
