package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailErrorView
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailLoadingView
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LaunchDetailScreen(
    launchId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<LaunchViewModel>()
    val launchCache = koinInject<LaunchCache>()
    
    // Check if we have pre-loaded detailed data in cache
    val cachedLaunchDetailed = remember(launchId) { launchCache.getCachedLaunchDetailed(launchId) }
    val launchDetails by viewModel.launchDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Determine current launch data
    val currentLaunch = cachedLaunchDetailed ?: launchDetails

    // Handle loading logic
    LaunchedEffect(launchId) {
        if (cachedLaunchDetailed != null) {
            // We have preloaded data, set it immediately to avoid loading state
            viewModel.setLaunchDetails(cachedLaunchDetailed)
        } else if (launchDetails == null && !isLoading) {
            // No preloaded data and not currently loading, fetch from API
            viewModel.fetchLaunchDetails(launchId)
        }
    }

    // Only render the view when we have launch data, show loading/error states otherwise
    val errorMessage = error
    when {
        errorMessage != null -> {
            LaunchDetailErrorView(
                errorMessage = errorMessage,
                onRetry = { viewModel.fetchLaunchDetails(launchId) },
                onNavigateBack = onNavigateBack
            )
        }
        currentLaunch != null -> {
            // We have launch data, pass non-null launch to view
            LaunchDetailView(
                launch = currentLaunch,
                onNavigateBack = onNavigateBack
            )
        }
        else -> {
            // Show loading state
            LaunchDetailLoadingView(onNavigateBack = onNavigateBack)
        }
    }
}


