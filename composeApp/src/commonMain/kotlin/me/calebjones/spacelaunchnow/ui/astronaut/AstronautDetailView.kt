package me.calebjones.spacelaunchnow.ui.astronaut

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautFlightHistoryCard
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautInfoCard
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautLinksCard
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautStatsCard
import me.calebjones.spacelaunchnow.ui.compose.LocalDetailScaffoldCollapsed
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.ui.viewmodel.AstronautDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Height constant for SharedDetailScaffold title area.
 * 
 * IMPORTANT: When using SharedDetailScaffold, content MUST include a top spacer
 * to prevent overlap with the collapsing header. Use: Spacer(Modifier.height(TitleHeight - 28.dp))
 * 
 * This 28.dp offset accounts for the internal padding/margins in SharedDetailScaffold.
 * See: RocketDetailView.kt, SpaceStationDetailView.kt for reference implementations.
 */
private val TitleHeight = 110.dp

/**
 * Detail screen for viewing comprehensive astronaut information.
 *
 * Displays:
 * - Profile header with photo
 * - Career statistics
 * - Biography
 * - Flight history
 */
@Composable
fun AstronautDetailView(
    astronautId: Int,
    onLaunchClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AstronautDetailViewModel> { parametersOf(astronautId) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(astronautId) {
        // ViewModel loads data in init block
    }

    when {
        uiState.error != null -> {
            ErrorView(
                errorMessage = uiState.error ?: "Unknown error",
                onRetry = { viewModel.retry() },
                onNavigateBack = onNavigateBack
            )
        }

        uiState.astronaut != null -> {
            val astronaut = uiState.astronaut!!
            
            SharedDetailScaffold(
                titleText = astronaut.name ?: "Unknown",
                taglineText = astronaut.agencyAbbrev,
                imageUrl = astronaut.imageUrl,
                onNavigateBack = onNavigateBack
            ) {
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
                    
                    // Career Statistics Card (includes status, age, nationality, and career stats)
                    AstronautStatsCard(astronaut = astronaut)
                    
                    // Biography Card
                    if (!astronaut.bio.isNullOrBlank()) {
                        AstronautInfoCard(astronaut = astronaut)
                    }
                    
                    // Links Card (Wikipedia and Social Media)
                    AstronautLinksCard(astronaut = astronaut)
                    
                    // Flight History Card
                    if (astronaut.flights.isNotEmpty()) {
                        AstronautFlightHistoryCard(
                            flights = astronaut.flights,
                            onLaunchClick = onLaunchClick
                        )
                    }

                    //TODO Add Agency Card
                    
                    // Bottom padding
                    Box(modifier = Modifier.padding(bottom = 200.dp))
                }
            }
        }

        else -> {
            LoadingView(onNavigateBack = onNavigateBack)
        }
    }
}

@Composable
private fun LoadingView(onNavigateBack: () -> Unit) {
    SharedDetailScaffold(
        titleText = "Loading...",
        taglineText = null,
        imageUrl = null,
        onNavigateBack = onNavigateBack
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    SharedDetailScaffold(
        titleText = "Error",
        taglineText = null,
        imageUrl = null,
        onNavigateBack = onNavigateBack
    ) {
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
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Failed to load astronaut",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
