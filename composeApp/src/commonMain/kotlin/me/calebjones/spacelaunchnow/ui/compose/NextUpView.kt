package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import org.koin.compose.viewmodel.koinViewModel
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun NextLaunchView() {
    val launchViewModel = koinViewModel<NextUpViewModel>()
    val nextLaunch by launchViewModel.nextLaunch.collectAsState()
    val error by launchViewModel.error.collectAsState()
    val isLoading by launchViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        launchViewModel.fetchNextLaunch()
    }    
    if (error != null) {
        ErrorMessageView(
            errorMessage = error ?: "Unknown error occurred",
            onRetry = { launchViewModel.fetchNextLaunch() }
        )
    } else if (nextLaunch != null) {
        
        // Use the launch image for dynamic theming
        Box {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NextLaunchItemView(nextLaunch!!)
            }
        }
    } else if (isLoading) {
        // Show shimmer loading effect while loading
        NextUpShimmerBox()
    } else {
        Text(text = "No upcoming launches found.")
    }
}


@Composable
fun NextLaunchItemView(launch: LaunchDetailed) {
    // Compute the title based on the provided logic
    val title by remember(launch) {
        mutableStateOf(
            if (launch.rocket?.configuration != null) {
                val lsp = launch.launchServiceProvider
                val providerName = if (
                    lsp?.name?.length!! > 15 &&
                    !lsp.abbrev.isNullOrEmpty()
                ) {
                    lsp.abbrev
                } else {
                    lsp.name
                }
                "$providerName | ${launch.rocket.configuration.name}"
            } else if (launch.name.isNotEmpty()) {
                launch.name
            } else {
                "Unknown Name"
            }
        )
    }    
    // Main content column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Image card (takes up about 70% of the space)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(64.dp)),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            // Create a single Box that contains everything, with the image as the background
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image covering the entire card
                launch.image?.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Launch Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                // Semi-transparent overlay for better readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                )

                // Content and Countdown - All content is now in a single Column overlaid on the image
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Content at the top with padding
                    Column(
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Display the computed title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )

                        launch.pad?.location?.name?.let { locationName ->
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        launch.net?.let { launchNet ->
                            Text(
                                text = launchNet.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
        
        // Spacer to add some separation
        Box(modifier = Modifier.height(16.dp))
        
        // Countdown section in its own card        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp) // Increased vertical padding
                ) {
                    // HorizontalDivider with higher contrast
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth()
                            .align(Alignment.Center) // Align divider to center of Box
                    )

                    // Pill-shaped Button
                    Button(
                        onClick = { /* Handle click */ },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.Center) // Center the button in the Box
                    ) {
                        Text(
                            text = launch.status?.name ?: "Unknown Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }

                // Countdown display
                LaunchCountdown(
                    launchTime = launch.net!!,
                    windowStart = launch.windowStart!!,
                    windowClose = launch.windowEnd!!
                )

                // Divider before countdown
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                        .fillMaxWidth()
                )
            }
        }

        // Spacer between cards
        Box(modifier = Modifier.height(16.dp))

        // Launch Window Indicator Card - now in its own card        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title for the Launch Window card
                Text(
                    text = "Launch Window",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        LaunchWindowIndicator(
                            launchTime = launch.net!!,
                            windowStart = launch.windowStart!!,
                            windowEnd = launch.windowEnd!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = "T + 47",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Max Q",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * A component that displays error messages in a user-friendly way with a retry button
 */
@Composable
fun ErrorMessageView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error card with icon and message
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error title
                Text(
                    text = "API Request Error",
                    style = MaterialTheme.typography.headlineSmall,
                )
                
                // Error message
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Retry button
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}