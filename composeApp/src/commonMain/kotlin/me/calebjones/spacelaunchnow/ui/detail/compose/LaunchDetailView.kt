package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed

@Composable
fun LaunchDetailView(
    viewModel: LaunchViewModel, 
    launchId: String,
    preloadedLaunchDetailed: LaunchDetailed? = null
) {
    val launchDetails by viewModel.launchDetails.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Use preloaded data immediately if available, otherwise use fetched data
    val displayLaunchDetails = preloadedLaunchDetailed ?: launchDetails

    LaunchedEffect(launchId, preloadedLaunchDetailed) {
        // Only fetch if we don't have preloaded data
        if (preloadedLaunchDetailed == null) {
            viewModel.fetchLaunchDetails(launchId)
        } else {
            // Set the preloaded data in the ViewModel so other parts of the app can access it
            viewModel.setLaunchDetails(preloadedLaunchDetailed)
            println("Using preloaded launch details for ${preloadedLaunchDetailed.name} - no API call needed!")
        }
    }

    if (error != null) {
        Text(text = "Error: $error")
    } else if (displayLaunchDetails != null) {
        Column(modifier = Modifier.padding(16.dp)) {
            displayLaunchDetails.name?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) }
            Text(text = displayLaunchDetails.windowStart.toString(), style = MaterialTheme.typography.bodySmall)
            // Add more details as needed
            
            // Show a small indicator if we're using preloaded data
            if (preloadedLaunchDetailed != null) {
                Text(
                    text = "✨ Instantly loaded from cache", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }
}